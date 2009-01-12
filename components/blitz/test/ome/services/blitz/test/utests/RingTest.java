/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.services.blitz.test.utests;

import javax.sql.DataSource;

import ome.services.blitz.fire.Ring;
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
    SimpleJdbcTemplate jdbc;
    Ice.ObjectAdapter oa;
    Ice.Communicator ic;
    Mock mockIc, mockOa;

    @BeforeTest
    public void setupMethod() throws Exception {
        mockIc = mock(Ice.Communicator.class);
        ic = (Ice.Communicator) mockIc.proxy();
        mockOa = mock(Ice.ObjectAdapter.class);
        oa = (Ice.ObjectAdapter) mockOa.proxy();
        ctx = new OmeroContext(new String[] {
                "classpath:ome/config.xml",
                "classpath:ome/services/datalayer.xml" });
        DataSource dataSource = (DataSource) ctx.getBean("dataSource");
        jdbc = new SimpleJdbcTemplate(dataSource);

    }

    @Test
    public void testFirstTakesOver() throws Exception {
        Ring one = new Ring(jdbc);
        one.setApplicationEventPublisher(ctx);
        one.init(oa, "one");
        assertEquals("one", one.getRedirect());
        Ring two = new Ring(jdbc);
        two.setApplicationEventPublisher(ctx);
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