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
import ome.model.meta.Session;
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
        cache = new SessionCache();
        cf = new CacheFactory();
        cf.setOverflowToDisk(false);
        cf.setCacheName("test");
        cf.setTimeToIdle(1);
        cache.setCacheFactory(cf);
        called[0] = called[0] = false;
    }

    public void testUnderstandingListeners() throws Exception {
        cf.setCacheName("understanding");
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
        cache.setNeedsUpdate(true);
        try {
            cache.getIds();
            fail("This should fail");
        } catch (Exception e) {
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
        called[0] = false;
        final Session s = sess();
        SessionContextImpl sc = new SessionContextImpl(s, ids, ids, roles) {
            // @Override
            // public void close() {
            // called[0] = true;
            // super.close();
            // }
        };
        cache.putSession(s.getUuid(), sc);
        Thread.sleep(2L);
        assertTrue(called[0]);
    }

    // Helpers
    // ====================

    Session sess() {
        Session s = new Session();
        s.setUuid(UUID.randomUUID().toString());
        return s;
    }

}
