/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.services.blitz.test.utests;

import javax.sql.DataSource;

import ome.services.blitz.fire.Ring;
import ome.services.sessions.state.SessionCache;
import ome.services.util.Executor;
import ome.system.OmeroContext;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

/**
 * @author Josh Moore, josh at glencoesoftware.com
 * @since Beta4
 */
public class RingTest extends MockObjectTestCase {

    OmeroContext ctx;
    Executor ex;
    SimpleJdbcTemplate jdbc;
    Ice.ObjectAdapter oa;
    Ice.Communicator ic;
    Mock mockIc, mockOa, mockEx;

    @BeforeTest
    public void setupMethod() throws Exception {
        mockIc = mock(Ice.Communicator.class);
        ic = (Ice.Communicator) mockIc.proxy();
        mockOa = mock(Ice.ObjectAdapter.class);
        oa = (Ice.ObjectAdapter) mockOa.proxy();
        mockEx = mock(Executor.class);
        ex = (Executor) mockEx.proxy();
    }

    @Test
    public void testFirstTakesOver() throws Exception {
        Ring one = new Ring("one", ex, new SessionCache());
        one.init(oa, "one");
        assertEquals("one", one.getRedirect());
        Ring two = new Ring("two", ex, new SessionCache());
        two.init(oa, "two");
        assertEquals("one", two.getRedirect());
    }

    @Test
    public void testSeveralThreadsStartAndOnlyOneValueIsSet() throws Exception {
        fail();
    }

    @Test
    public void testHandlesMissingServers() throws Exception {
        fail();
    }

    @Test
    public void testRemovesUnreachable() throws Exception {
        fail();
    }

    @Test
    public void testReaddsSelfIfTemporarilyUnreachable() throws Exception {
        fail();
    }

    @Test
    public void testAllSessionRemovedIfDiscoveryFails() throws Exception {
        fail();
    }

    @Test
    public void testAllSessionsReassertedIfSessionComesBackOnline()
            throws Exception {
        fail();
    }
}