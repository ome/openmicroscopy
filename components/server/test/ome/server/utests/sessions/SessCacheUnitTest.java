/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.server.utests.sessions;

import ome.model.meta.Session;
import ome.services.sessions.SessionCache;
import ome.services.sessions.SessionCacheImpl;
import ome.services.sessions.SessionContext;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

/**
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta2
 */
public class SessCacheUnitTest extends MockObjectTestCase {

	private static final long ID1 = 1L;
	private static final String UUID1 = "uuid1";
	Mock mockCtx;
	SessionContext ctx;
	SessionCache cache;
	SessionCacheImpl impl;
	Session sess;
	
    @BeforeTest
    public void config() {
    	impl = new SessionCacheImpl();
    	cache = impl;
    	mockCtx = mock(SessionContext.class);
    	ctx = (SessionContext) mockCtx.proxy();
    	sess = new Session();
    	sess.setId(ID1);
    	sess.setUuid(UUID1);
    }

    @Test
    public void testCacheCanBeGivenSomething() {
        mockCtx.expects(atLeastOnce()).method("getSession").will(returnValue(sess));
    	cache.put(ctx);
        assertTrue(cache.contains(UUID1));
        assertTrue(cache.contains(ID1));
    }

    @Test
    public void testCacheWillGiveSomethingBack() {
    	testCacheCanBeGivenSomething();
    	assertTrue(ctx == cache.get(ID1));
    	assertTrue(ctx == cache.get(UUID1));
    }
    
    @Test
    public void testCacheHaveSomethingTakenAway() {
    	testCacheCanBeGivenSomething();
    	cache.remove(ID1);
    	assertFalse(cache.contains(UUID1));
    	assertFalse(cache.contains(ID1));
    	testCacheCanBeGivenSomething();
    	cache.remove(UUID1);
    	assertFalse(cache.contains(UUID1));
    	assertFalse(cache.contains(ID1));
    }
    
    @Test
    public void testCacheHaveEverythingTakenAway() {
    	testCacheCanBeGivenSomething();
    	assertTrue(1 == cache.clear());
    }
    
    @Test
    public void testCacheKeepsUpWithTimestamp() {
    	testCacheCanBeGivenSomething();
    	assertTrue(System.currentTimeMillis() >= cache.timestamp(ID1));
    }

    @Test
    public void testCacheSetTimestamp() {
    	testCacheKeepsUpWithTimestamp();
    	long old = cache.timestamp(ID1);
    	cache.touch(ID1);
    	assertTrue(cache.timestamp(ID1) >= old);
    }

    
}
