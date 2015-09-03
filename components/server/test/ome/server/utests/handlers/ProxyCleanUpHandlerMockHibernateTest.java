/*
 * ome.server.utests.handlers.SessionHandlerMockHibernateTest
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.server.utests.handlers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ome.tools.hibernate.ProxyCleanupFilter;

import org.hibernate.collection.PersistentBag;
import org.hibernate.collection.PersistentIdentifierBag;
import org.hibernate.collection.PersistentList;
import org.hibernate.collection.PersistentMap;
import org.hibernate.collection.PersistentSet;
import org.hibernate.engine.PersistenceContext;
import org.hibernate.engine.SessionImplementor;
import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author Josh Moore &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @since Omero 2.0
 */
@Test(groups = { "ticket:61", "hibernate", "priority" })
public class ProxyCleanUpHandlerMockHibernateTest extends MockObjectTestCase {

    ProxyCleanupFilter filter;

    SessionImplementor session;

    PersistenceContext ctx;

    Mock mockSession, mockCtx;

    @Override
    @BeforeMethod
    protected void setUp() throws Exception {
        super.setUp();
        filter = new ProxyCleanupFilter();
        mockSession = mock(SessionImplementor.class);
        mockCtx = mock(PersistenceContext.class);
        session = (SessionImplementor) mockSession.proxy();
        ctx = (PersistenceContext) mockCtx.proxy();
    }

    @Override
    @AfterMethod
    protected void tearDown() throws Exception {
        super.verify();
        super.tearDown();
    }

    // ~ Tests
    // =========================================================================

    @Test
    public void testPersistentMap() throws Exception {
        Map m = new PersistentMap(session, new HashMap());
        m = filter.filter(null, m);
        assertFalse(m instanceof PersistentMap);
    }

    @Test
    public void testPersistentSet() throws Exception {
        Set s = new PersistentSet();
        s = (Set) filter.filter(null, s);
        assertFalse(s instanceof PersistentSet);
    }

    @Test
    public void testPersistentList() throws Exception {
        List l = new PersistentList();
        l = (List) filter.filter(null, l);
        assertFalse(l instanceof PersistentList);
    }

    @Test
    public void testPersistentBag() throws Exception {
        List l = new PersistentBag();
        l = (List) filter.filter(null, l);
        assertFalse(l instanceof PersistentBag);
    }

    @Test
    public void testPersistentIdBag() throws Exception {
        List l = new PersistentIdentifierBag();
        l = (List) filter.filter(null, l);
        assertFalse(l instanceof PersistentIdentifierBag);
    }

    /*
     * exist also as subclasses of AbstractPersistentCollection -
     * PersistentArrayHolder -- deprecated - PersistentElementHolder -- ?? -
     * PersistentIndexedElementHolder -- ?? - subclasses of the already tested
     * items
     */

}
