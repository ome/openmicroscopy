/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.server.utests.sessions;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import junit.framework.TestCase;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import ome.conditions.InternalException;
import ome.model.meta.Session;
import ome.services.sessions.SessionCallback;
import ome.services.sessions.SessionContextImpl;
import ome.services.sessions.state.CacheFactory;
import ome.services.sessions.state.CacheListener;
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

    CacheFactory cf;
    SessionCache cache;
    final boolean[] called = new boolean[2];

    @BeforeMethod
    public void setup() throws Exception {
        initCache(1);
        called[0] = called[0] = false;
    }

    private void initCache(int timeToIdle) {
        cache = new SessionCache();
        cf = new CacheFactory();
        cf.setOverflowToDisk(false);
        cf.setCacheName("time-to-idle-" + timeToIdle);
        cf.setTimeToIdle(timeToIdle);
        cache.setCacheFactory(cf);
    }

    public void testUnderstandingListeners() throws Exception {
        cf.setCacheName("understanding");
        cf.setTimeToIdle(1);
        Ehcache c = cf.createCache(new CacheListener() {
            @Override
            public void notifyElementExpired(Ehcache arg0, Element arg1) {
                called[0] = true;
            }

            @Override
            public void notifyElementRemoved(Ehcache arg0, Element arg1)
                    throws CacheException {
                called[1] = true;
            }
        });
        c.put(new Element(new Object(), new Object()));
        Thread.sleep(2500L);
        c.evictExpiredElements();
        assertTrue(called[0]);
        assertTrue(called[1]);

    }

    public void testSimpleListener() {
        long before, after;
        called[0] = false;
        StaleCacheListener doesSomething = new StaleCacheListener() {
            public boolean attemptCacheUpdate() {
                called[0] = true;
                return true;
            }
        };
        StaleCacheListener doesNothing = new StaleCacheListener() {
            public boolean attemptCacheUpdate() {
                called[0] = true;
                return false;
            }
        };

        cache.addStaleCacheListener(doesSomething);
        before = cache.getLastUpdated();
        cache.setNeedsUpdate(true);
        cache.getIds();
        after = cache.getLastUpdated();
        assertTrue(called[0]);
        assertTrue(after > before);
        assertFalse(cache.getNeedsUpdate());

        cache.removeStaleCacheListener(doesSomething);
        cache.addStaleCacheListener(doesNothing);

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
        assertTrue(called[0]);
    }

    public void testPutNonSerializable() {
        initCache(10000);
        final Session s = sess();
        cache.putSession(sess().getUuid(), new SessionContextImpl(s, ids, ids,
                roles) {

        });
        assertTrue(cache.getIds().size() == 1);
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

    public void testIfFirstThreadFailsSecondThreadAlsoTriesToUpdate()
            throws Exception {
        initCache(10000);
        try {
            cache.setNeedsUpdate(true);
            fail("throw!");
        } catch (InternalException ie) {
            // ok.
        }

        // Now the cache still needs an update
        final boolean done[] = new boolean[] { false, false };

        class TryUpdate extends Thread {
            int i;

            TryUpdate(int i) {
                this.i = i;
            }

            @Override
            public void run() {
                try {
                    cache.getSessionContext("anything");
                } catch (InternalException ie) {
                    done[i] = true;
                }
            }
        }

        Thread t1 = new TryUpdate(0), t2 = new TryUpdate(1);
        t1.start();
        t2.start();
        t1.join();
        t2.join();
        assertTrue(done[0]);
        assertTrue(done[1]);

    }

    // Helpers
    // ====================

    Session sess() {
        Session s = new Session();
        s.setUuid(UUID.randomUUID().toString());
        return s;
    }

}
