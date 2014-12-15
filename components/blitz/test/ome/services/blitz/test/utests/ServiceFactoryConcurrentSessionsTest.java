/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.services.blitz.test.utests;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.sf.ehcache.Ehcache;
import ome.logic.HardWiredInterceptor;
import ome.services.blitz.impl.ServiceFactoryI;
import ome.services.sessions.SessionManager;
import ome.services.sessions.state.CacheFactory;
import ome.services.util.Executor;
import ome.system.OmeroContext;
import ome.system.Principal;
import omero.api.IAdminPrx;
import omero.api.IAdminPrxHelper;
import omero.constants.CLIENTUUID;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test
public class ServiceFactoryConcurrentSessionsTest extends MockObjectTestCase {

    protected Executor executor = new Executor.Impl(null, null, null, null) {
        @Override
        public Object execute(Principal p, Work work) {
            return work.doWork(null, null);
        }
    };

    Ehcache cache1, cache2;
    ServiceFactoryI sf1, sf2;
    Ice.Identity id1 = Ice.Util.stringToIdentity("user1/session1");
    Ice.Identity id2 = Ice.Util.stringToIdentity("user2/session2");
    Ice.Current curr1 = new Ice.Current();
    Ice.Current curr2 = new Ice.Current();
    Mock mockAdapter, mockManager;
    SessionManager manager;
    IAdminPrxHelper admin1, admin2;
    Ice.ObjectAdapter adapter;
    OmeroContext ctx;
    Ice.Current current = new Ice.Current();
    {
        current.ctx = new HashMap<String, String>();
        current.ctx.put(CLIENTUUID.value, "clientuuid");
    }

    @Override
    @BeforeMethod
    protected void setUp() throws Exception {

        ctx = new OmeroContext(new String[] {
                "classpath:ome/config.xml",
                "classpath:omero/test.xml",
                "classpath:ome/services/throttling/throttling.xml",
                "classpath:ome/services/blitz-servantDefinitions.xml",
                "classpath:ome/services/blitz-graph-rules.xml" });

        CacheFactory factory1 = new CacheFactory(), factory2 = new CacheFactory();
        factory1.setOverflowToDisk(false);
        factory1.setBeanName(this.getClass().getName() + "1");
        cache1 = factory1.createCache();
        factory2.setOverflowToDisk(false);
        factory2.setBeanName(this.getClass().getName() + "2");
        cache2 = factory2.createCache();
        assertTrue(cache1 != cache2);

        admin1 = new IAdminPrxHelper();
        admin1.setup(new Ref());
        admin2 = new IAdminPrxHelper();
        admin2.setup(new Ref());
        mockAdapter = mock(Ice.ObjectAdapter.class);
        mockManager = mock(SessionManager.class);
        adapter = (Ice.ObjectAdapter) mockAdapter.proxy();
        manager = (SessionManager) mockManager.proxy();
        mockManager.expects(once()).method("inMemoryCache").with(eq("user1"))
                .will(returnValue(cache1));
        mockManager.expects(once()).method("inMemoryCache").with(eq("user2"))
                .will(returnValue(cache2));

        Principal p = new Principal("user1", "group", "event");

        // set adapter before instantiating SF
        current.adapter = adapter;

        curr1.id = id1;
        curr1.adapter = adapter;
        sf1 = new ServiceFactoryI(current,
                new omero.util.ServantHolder("session1"),
                null, ctx, manager, executor, p,
                new ArrayList<HardWiredInterceptor>(), null, null);

        curr2.id = id2;
        curr2.adapter = adapter;
        Principal p2 = new Principal("user2", "group", "event");
        sf2 = new ServiceFactoryI(current,
                new omero.util.ServantHolder("session2"),
                null, ctx, manager, executor, p2,
                new ArrayList<HardWiredInterceptor>(), null, null);

    }

    @Test
    void testActiveServicesListsOnlyOwnServices() throws Exception {

        // on returned null, new services will be created and added
        mockAdapter.expects(once()).method("find").will(returnValue(null));
        mockAdapter.expects(once()).method("add").will(returnValue(null));
        mockAdapter.expects(once()).method("createDirectProxy").will(
                returnValue(admin1));
        mockAdapter.expects(once()).method("find").will(returnValue(null));
        mockAdapter.expects(once()).method("add").will(returnValue(null));
        mockAdapter.expects(once()).method("createDirectProxy").will(
                returnValue(admin2));
        IAdminPrx prx1 = sf1.getAdminService(curr1);
        IAdminPrx prx2 = sf2.getAdminService(curr2);

        // First we make sure that only one service is seen by this session
        assertEquals(1, sf1.activeServices(null).size());

        // which implies that closing will not close another session's services
        mockAdapter.expects(once()).method("find").will(returnValue(null));
        // This is important, that the proxy actually gets removed.
        List<String> ids = sf2.activeServices(curr2);
        mockAdapter.expects(once()).method("remove").with(
                eq(Ice.Util.stringToIdentity(ids.get(0))));
        sf2.close(curr2);
        assertTrue(sf1.activeServices(curr1).size() == 1);
    }

}
