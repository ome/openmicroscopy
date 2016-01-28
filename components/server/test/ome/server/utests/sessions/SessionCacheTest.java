/*
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.server.utests.sessions;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CyclicBarrier;

import junit.framework.TestCase;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;

import ome.conditions.RemovedSessionException;
import ome.conditions.SessionException;
import ome.conditions.SessionTimeoutException;
import ome.model.internal.Permissions;
import ome.model.meta.ExperimenterGroup;
import ome.model.meta.Session;
import ome.services.sessions.SessionCallback;
import ome.services.sessions.SessionContext;
import ome.services.sessions.SessionContextImpl;
import ome.services.sessions.events.UserGroupUpdateEvent;
import ome.services.sessions.state.SessionCache.StaleCacheListener;
import ome.services.sessions.state.SessionCache;
import ome.services.sessions.stats.NullSessionStats;
import ome.system.OmeroContext;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta2
 */
@Test(groups = "sessions")
public class SessionCacheTest extends TestCase {

    OmeroContext ctx = new OmeroContext(
            new String[] { "classpath:ome/services/messaging.xml" });
    SessionCache cache;
    final boolean[] called = new boolean[2];

    @BeforeMethod
    public void setup() throws Exception {
        initCache();
        called[0] = called[0] = false;
    }

    private void initCache() {
        cache = new SessionCache();
        cache.setApplicationContext(ctx);
        cache.setStaleCacheListener(new NoOpStaleCacheListener());
        cache.setCacheManager(CacheManager.getInstance());
        // Waiting a second to let the fresh cache cool down.
        while (cache.getLastUpdated() == System.currentTimeMillis()) {
            try {
                Thread.sleep(10L);
            } catch (InterruptedException e) {
                // ok
            }
        }
    }

    List<Long> ids = Arrays.asList(1L);
    List<String> roles = Arrays.asList("");

    public void testSimpleTimeout() throws Exception {
        initCache();
        called[0] = false;
        final Session s = sess();
        s.setTimeToIdle(1L);
        cache.putSession(s.getUuid(), sc(s));
        cache.addSessionCallback(s.getUuid(), new SessionCallback() {
            public void close() {
                called[0] = true;
            }

            public String getName() {
                return null;
            }

            public Object getObject() {
                return null;
            }

            public void join(String session) {
            }
        });
        Thread.sleep(2000L);
        throwsSessionTimeout(s.getUuid());
        throwsSessionTimeout(s.getUuid());
        cache.updateEvent(new UserGroupUpdateEvent(this));
        cache.setStaleCacheListener(new NullStaleCacheListener());
        cache.doUpdate(); // Now it will be removed and the event published.
        assertTrue(called[0]);
    }

    public void testPutNonSerializable() {
        initCache();
        final Session s = sess();
        int size = cache.getIds().size();
        cache.putSession(s.getUuid(), sc(s));
        assertTrue(cache.getIds().size() == (size + 1));
    }

    // Now the cache still needs an update
    final boolean done[] = new boolean[] { false, false };

    class TryUpdate extends Thread {
        int i;
        Exception ex;
        CyclicBarrier barrier = new CyclicBarrier(2);

        TryUpdate(int i) {
            this(i, "try-thread");
        }

        TryUpdate(int i, String name) {
            super(name);
            this.i = i;
        }

        @Override
        public void run() {
            try {
                barrier.await();
                cache.getSessionContext(this.getName());
            } catch (Exception e) {
                this.ex = e;
            }
            done[i] = true;
        }
    }

    public void testOneThreadSuccessfulThenOtherReturns() throws Exception {

        final Session s = sess();
        cache.putSession(s.getUuid(), sc(s));

        done[0] = done[1] = false;
        final int[] count = new int[] { 0 };
        cache.setStaleCacheListener(new StaleCacheListener() {
            public void prepareReload() {
                // noop
            }

            public SessionContext reload(SessionContext context) {
                try {
                    Thread.sleep(1000L);
                    if (context.getSession().getUuid().equals(s.getUuid())) {
                        count[0]++;
                    }
                } catch (InterruptedException e) {
                }
                return context;
            }
        });
        cache.updateEvent(new UserGroupUpdateEvent(this)); // Locks & blocks
        TryUpdate t1 = new TryUpdate(0, "0-try-thread"), t2 = new TryUpdate(1,
                "1-try-thread");
        t1.start();
        t2.start();
        t1.barrier.await();
        t2.barrier.await();
        cache.doUpdate(); // This should release the lock
        t1.join();
        t2.join();
        assertTrue(done[0]);
        assertTrue(done[1]);
        assertEquals(1, count[0]);

    }

    @Test
    public void testInMemoryAndOnDiskAreProperlyDisposed() {
        initCache();
        Ehcache inmemory, ondisk;
        try {
            inmemory = cache.inMemoryCache("doesnotexist");
            fail("should fail");
        } catch (SessionException se) {
            // ok
        }

        // Create a session and attach in-memory info to it
        final Session s = sess();
        cache.putSession(s.getUuid(), sc(s));
        inmemory = cache.inMemoryCache(s.getUuid());
        ondisk = cache.onDiskCache(s.getUuid());
        inmemory.put(new Element("a", "b"));
        ondisk.put(new Element("c", "d"));
        cache.removeSession(s.getUuid());

        // Now recreate the same session and cache should be gone.
        cache.putSession(s.getUuid(), sc(s));
        inmemory = cache.inMemoryCache(s.getUuid());
        assertFalse(inmemory.isKeyInCache("a"));
        ondisk = cache.onDiskCache(s.getUuid());
        assertFalse(ondisk.isKeyInCache("c"));

    }

    @Test
    public void testMessageShouldBeRaisedOnRemoveSession() throws Exception {

        class Listener implements ApplicationListener {
            boolean called = false;

            public void onApplicationEvent(ApplicationEvent arg0) {
                called = true;
            }
        }

        String uuid = UUID.randomUUID().toString();
        Listener listener = new Listener();
        ApplicationEventMulticaster multicaster = mc();
        multicaster.addApplicationListener(listener);
        Session s = sess();
        cache.putSession(uuid, sc(s));
        cache.removeSession(uuid);
        assertTrue(listener.called);
    }

    @Test
    public void testMessageShouldBeRaisedOnTimeout() throws Exception {

        class Listener implements ApplicationListener {
            boolean called = false;

            public void onApplicationEvent(ApplicationEvent arg0) {
                called = true;
            }
        }

        String uuid = UUID.randomUUID().toString();
        Listener listener = new Listener();
        ApplicationEventMulticaster multicaster = mc();
        multicaster.addApplicationListener(listener);
        Session s = sess();
        s.setTimeToLive(1L);
        cache.putSession(uuid, sc(s));
        long start = System.currentTimeMillis();
        while (System.currentTimeMillis() - start < 1000L) {
            Thread.sleep(200L);
        }
        throwsSessionTimeout(uuid);
        throwsSessionTimeout(uuid);
        throwsSessionTimeout(uuid);
        // doUpdate() is no longer called automatically
        // so multiple calls to getSession will throw SessTimeout
        // until the background thread runs
        cache.updateEvent(new UserGroupUpdateEvent(this));
        cache.setStaleCacheListener(new NullStaleCacheListener());
        cache.doUpdate();
        assertTrue(listener.called);
        throwsRemovedSession(uuid);
    }

    @Test
    public void testMessageShouldBeRaisedOnUpdateNeeded() throws Exception {

        class Listener implements ApplicationListener {
            boolean called = false;

            public void onApplicationEvent(ApplicationEvent arg0) {
                called = true;
            }
        }

        String uuid = UUID.randomUUID().toString();
        Listener listener = new Listener();
        ApplicationEventMulticaster multicaster = mc();
        multicaster.addApplicationListener(listener);
        Session s = sess();
        s.setTimeToLive(1L);
        cache.putSession(uuid, sc(s));
        long start = System.currentTimeMillis();
        while (System.currentTimeMillis() - start < 1000L) {
            Thread.sleep(200L);
        }
        cache.updateEvent(new UserGroupUpdateEvent(this));
        cache.setStaleCacheListener(new NullStaleCacheListener());
        cache.doUpdate();
        assertTrue(listener.called);
    }

    @Test(timeOut=10000)
    public void testGetSessionDoesUpdateTheTimestamp() throws Exception {
        final Session s = sess();
        s.setTimeToIdle(5 * 100L);
        cache.putSession(s.getUuid(), sc(s));
        for (int i = 0; i < 10; i++) {
            Thread.sleep(1 * 100L);
            try {
                cache.getSessionContext(s.getUuid());
            } catch (RemovedSessionException rse) {
                fail("Removed session on loop " + i);
            }
        }
    }

    /**
     * For {@link #testGetSessionDoesUpdateTheTimestamp()} we changed from
     * cache.putQuiet(new Element) to cache.put(new Element) but we want to make
     * REAL sure that timeouts are still in effect.
     */
    @Test
    public void testSessionsTimeoutDispiteTheNewElementPutCall()
            throws Exception {
        initCache();
        StaleCacheListener stale = new NoOpStaleCacheListener();
        cache.setStaleCacheListener(stale);

        called[0] = false;
        final Session s1 = sess(); // One to keep alive
        final Session s2 = sess(); // One to let die
        s1.setTimeToIdle(5 * 100L);
        s2.setTimeToIdle(5 * 100L);
        cache.putSession(s1.getUuid(), sc(s1));
        cache.putSession(s2.getUuid(), sc(s2));
        for (int i = 0; i < 10; i++) {
            Thread.sleep(1 * 100L);
            try {
                cache.getSessionContext(s1.getUuid());
            } catch (RemovedSessionException rse) {
                fail("Removed session on loop " + i);
            } catch (SessionTimeoutException ste) {
                fail("Session timeout on loop " + i);
            }
        }

        // Make sure that clean up happened.
        cache.updateEvent(new UserGroupUpdateEvent(this));
        cache.doUpdate();
        try {
            cache.getSessionContext(s2.getUuid());
            fail("Should fail for " + s2.getUuid());
        } catch (RemovedSessionException rse) {
            // ok.
        } catch (SessionTimeoutException ste) {
            // ok.
        }
    }

    /**
     * Note: the listener logic was removed from the cache. The new semantics
     * of when things should be cleaned up needs to be removed along with the
     * methodIn() methodOut() test.
     * @throws Exception
     */
    @Test(groups = "broken")
    public void testExpiredSessionRemainsInCacheTilCleanup() throws Exception {
        initCache();
        cache.setStaleCacheListener(new NoOpStaleCacheListener());
        Session s = sess();
        s.setTimeToIdle(1L);
        cache.putSession(s.getUuid(), sc(s));
        Thread.sleep(2L);
        try {
            cache.getSessionContext(s.getUuid());
            fail("should time out");
        } catch (SessionException se) {
            // Good.
        }
        Cache internal = CacheManager.getInstance().getCache("SessionCache");
        assertTrue(internal.isKeyInCache(s.getUuid()));
    }

    // Helpers
    // ====================

    Session sess() {
        Session s = new Session();
        s.setStarted(new Timestamp(System.currentTimeMillis()));
        s.setTimeToIdle(0L);
        s.setTimeToLive(0L);
        s.setUuid(UUID.randomUUID().toString());
        ExperimenterGroup g = new ExperimenterGroup();
        g.getDetails().setPermissions(Permissions.PRIVATE);
        s.getDetails().setGroup(g);
        return s;
    }

    SessionContext sc(Session s) {
        return new SessionContextImpl(s, Collections.singletonList(1L),
                Collections.singletonList(1L), Collections.singletonList(""),
                new NullSessionStats(), null);
    }

    private ApplicationEventMulticaster mc() {
        ApplicationEventMulticaster multicaster = (ApplicationEventMulticaster) ctx
                .getBean("applicationEventMulticaster");
        return multicaster;
    }

    private void throwsSessionTimeout(String uuid) {
        try {
            cache.getSessionContext(uuid);
            fail("Should throw");
        } catch (SessionTimeoutException ste) {
            // ok;
        }
    }

    private void throwsRemovedSession(String uuid) {
        try {
            cache.getSessionContext(uuid);
            fail("Should throw");
        } catch (RemovedSessionException rse) {
            // ok;
        }
    }

    private final class NoOpStaleCacheListener implements StaleCacheListener {

        boolean called = false;

        public void prepareReload() {
            // noop
        }

        public SessionContext reload(SessionContext context) {
            called = true;
            return context;
        }
    }

    private final class NullStaleCacheListener implements StaleCacheListener {

        boolean called = false;

        public void prepareReload() {
            // noop
        }

        public SessionContext reload(SessionContext context) {
            called = true;
            return null;
        }
    }

    private final class ThrowsStaleCacheListener implements StaleCacheListener {
        public void prepareReload() {
            // noop.
        }

        public SessionContext reload(SessionContext context) {
            throw new RuntimeException();
        }
    }
}
