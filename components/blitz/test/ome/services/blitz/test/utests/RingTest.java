/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.services.blitz.test.utests;

import java.util.HashSet;

import ome.model.meta.Node;
import ome.services.blitz.fire.Registry;
import ome.services.blitz.fire.Ring;
import ome.services.util.Executor;
import ome.system.OmeroContext;
import omero.grid.ClusterNodePrx;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author Josh Moore, josh at glencoesoftware.com
 * @since Beta4
 */
public class RingTest extends MockObjectTestCase {

    Executor ex;
    Registry reg;
    OmeroContext ctx;
    SimpleJdbcTemplate jdbc;
    Ice.ObjectAdapter oa;
    Ice.Communicator ic;
    Mock mockIc, mockOa, mockEx, mockReg;

    @BeforeMethod
    public void setupMethod() throws Exception {
        mockIc = mock(Ice.Communicator.class);
        ic = (Ice.Communicator) mockIc.proxy();
        mockOa = mock(Ice.ObjectAdapter.class);
        oa = (Ice.ObjectAdapter) mockOa.proxy();
        mockEx = mock(Executor.class);
        ex = (Executor) mockEx.proxy();
        mockReg = mock(Registry.class);
        reg = (Registry) mockReg.proxy();
    }

    @Test
    public void testFirstTakesOver() throws Exception {

        prepareInit("one");
        Ring one = new Ring("one", ex);
        one.setRegistry(reg);
        one.init(oa, "one");

        prepareInit("two");
        Ring two = new Ring("two", ex);
        two.setRegistry(reg);
        two.init(oa, "two");

    }

    private void prepareInit(String uuid) {
        mockOa.expects(once()).method("getCommunicator").will(returnValue(ic));

        mockReg.expects(once()).method("lookupClusterNodes").will(
                returnValue(new ClusterNodePrx[0]));
        mockEx.expects(once()).method("execute").will(
                returnValue(new HashSet<String>())).id(uuid + "lookup");
        mockEx.expects(once()).method("execute").after(uuid + "lookup").will(
                returnValue(new Node())).id(uuid + "createManager");
        mockEx.expects(once()).method("execute").after(uuid + "createManager")
                .will(returnValue(Boolean.TRUE))
                .id(uuid + "initializeRedirect");
        mockEx.expects(once()).method("execute").after(
                uuid + "initializeRedirect").will(returnValue(uuid)).id(
                uuid + "getRedirect");

        mockIc.expects(once()).method("stringToIdentity").will(
                returnValue(new Ice.Identity()));
        mockOa.expects(once()).method("add");
        mockOa.expects(once()).method("createDirectProxy");
        mockReg.expects(once()).method("addObject");
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
