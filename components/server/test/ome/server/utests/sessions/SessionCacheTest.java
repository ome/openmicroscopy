/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.server.utests.sessions;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import junit.framework.TestCase;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import ome.conditions.InternalException;
import ome.conditions.SessionException;
import ome.model.meta.Session;
import ome.services.sessions.SessionCallback;
import ome.services.sessions.SessionContext;
import ome.services.sessions.SessionContextImpl;
import ome.services.sessions.state.SessionCache;
import ome.services.sessions.state.SessionCache.StaleCacheListener;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta2
 */
@Test(groups = "sessions")
public class SessionCacheTest extends TestCase {

    SessionCache cache;
    final boolean[] called = new boolean[2];

    @BeforeMethod
    public void setup() throws Exception {
        initCache(1);
        called[0] = called[0] = false;
    }

    private void initCache(int timeToIdle) {
        cache = new SessionCache();
        cache.setCacheManager(CacheManager.getInstance());
    }

    public void testSimpleListener() {
        long before, after;
        called[0] = false;
        StaleCacheListener doesSomething = new StaleCacheListener() {
            public SessionContext reload(SessionContext context) {
                called[0] = true;
                return context;
            }
        };
        StaleCacheListener doesNothing = new StaleCacheListener() {
            public SessionContext reload(SessionContext context) {
                called[0] = true;
                throw new RuntimeException();
            }
        };

        cache.setStaleCacheListener(doesSomething);
        before = cache.getLastUpdated();
        cache.setNeedsUpdate(true);
        cache.getIds();
        after = cache.getLastUpdated();
        assertTrue(called[0]);
        assertTrue(after > before);
        assertFalse(cache.getNeedsUpdate());

        cache.setStaleCacheListener(doesNothing);

        before = cache.getLastUpdated();
        try {
            cache.setNeedsUpdate(true);
            fail("This should fail");
        } catch (InternalException e) {
            // ok
        }

        after = cache.getLastUpdated();
        assertTrue(called[0]);
        assertEquals(after, before);
        assertTrue(cache.getNeedsUpdate());
    }

    List<Long> ids = Arrays.asList(1L);
    List<String> roles = Arrays.asList("");

    public void testSimpleTimeout() throws Exception {
        initCache(1);
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
        assertGetFails(s);
        assertTrue(called[0]);
    }

    private void assertGetFails(final Session s) {
        try {
            cache.getSessionContext(s.getUuid());
            fail("Exception must be thrown");
        } catch (SessionException se) {

        }
    }

    public void testPutNonSerializable() {
        initCache(10000);
        final Session s = sess();
        int size = cache.getIds().size();
        cache.putSession(s.getUuid(), sc(s));
        assertTrue(cache.getIds().size() == (size + 1));
    }

    public void testNoSuccessfulListenersThrowsExceptionOnUpdate() {
        initCache(1000);
        try {
            cache.setNeedsUpdate(true);
            fail("Should throw internal exception");
        } catch (InternalException ie) {
            // ok
        }

    }

    // Now the cache still needs an update
    final boolean done[] = new boolean[] { false, false };

    class TryUpdate extends Thread {
        int i;

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
                cache.getSessionContext("anything");
            } catch (Exception e) {
            }
            done[i] = true;
        }
    }

    public void testIfFirstThreadFailsSecondThreadAlsoTriesToUpdate()
            throws Exception {
        initCache(10000);
        Session s = sess();
        cache.putSession(s.getUuid(), sc(s));
        cache.setStaleCacheListener(new StaleCacheListener() {
            public SessionContext reload(SessionContext context) {
                throw new RuntimeException();
            }
        });

        try {
            cache.setNeedsUpdate(true);
            cache.getIds();
            fail("throw!");
        } catch (InternalException ie) {
            // ok.
        }

        Thread t1 = new TryUpdate(0), t2 = new TryUpdate(1);
        t1.start();
        t1.join();
        t2.start();
        t2.join();
        assertTrue(done[0]);
        assertTrue(done[1]);

    }

    public void testOneThreadSuccessfulThenOtherReturns() throws Exception {
        initCache(10000);
        final Session s = sess();
        cache.putSession(s.getUuid(), sc(s));
        try {
            cache.setNeedsUpdate(true);
            fail("throw!");
        } catch (InternalException ie) {
            // ok.
        }

        done[0] = done[1] = false;
        final int[] count = new int[] { 0 };
        cache.setStaleCacheListener(new StaleCacheListener() {
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
        Thread t1 = new TryUpdate(0, "0-try-thread"), t2 = new TryUpdate(1,
                "1-try-thread");
        t1.start();
        t2.start();
        t1.join();
        t2.join();
        assertTrue(done[0]);
        assertTrue(done[1]);
        assertEquals(1, count[0]);

    }

    @Test
    public void testInMemoryAndOnDiskAreProperlyDisposed() {
        initCache(10000);
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

    // Helpers
    // ====================

    Session sess() {
        Session s = new Session();
        s.setStarted(new Timestamp(System.currentTimeMillis()));
        s.setTimeToIdle(0L);
        s.setTimeToLive(0L);
        s.setUuid(UUID.randomUUID().toString());
        return s;
    }

    SessionContext sc(Session s) {
        return new SessionContextImpl(s, Collections.singletonList(1L),
                Collections.singletonList(1L), Collections.singletonList(""));
    }
}
