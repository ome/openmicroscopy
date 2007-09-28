/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.icy.service.utests;

import java.util.ArrayList;
import java.util.List;

import net.sf.ehcache.Ehcache;
import ome.logic.HardWiredInterceptor;
import ome.services.blitz.fire.SessionPrincipal;
import ome.services.blitz.impl.ServiceFactoryI;
import ome.system.OmeroContext;
import omero.api.IAdminPrx;
import omero.api.IAdminPrxHelper;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.testng.annotations.Configuration;
import org.testng.annotations.Test;

@Test
public class ServiceFactoryConcurrentSessionsTest extends MockObjectTestCase {

    Ehcache cache;
    ServiceFactoryI sf1, sf2;
    Ice.Identity id1 = Ice.Util.stringToIdentity("user1/session1");
    Ice.Identity id2 = Ice.Util.stringToIdentity("user2/session2");
    Ice.Current curr1 = new Ice.Current();
    Ice.Current curr2 = new Ice.Current();
    Mock mockAdapter;
    IAdminPrxHelper admin1, admin2;
    Ice.ObjectAdapter adapter;
    OmeroContext ctx= new OmeroContext(new String[]{
        "classpath:omero/test.xml",
        "classpath:ome/services/blitz-servantDefinitions.xml"
    });
    
    @Override
    @Configuration(beforeTestMethod = true)
    protected void setUp() throws Exception {
        cache = new TestCache();
        admin1 = new IAdminPrxHelper();
        admin1.setup(new Ref());
        admin2 = new IAdminPrxHelper();
        admin2.setup(new Ref());
        mockAdapter = mock(Ice.ObjectAdapter.class);
        adapter = (Ice.ObjectAdapter) mockAdapter.proxy();

        sf1 = new ServiceFactoryI(cache);
        sf1.setApplicationContext(ctx);
        sf1.setInterceptors(new ArrayList<HardWiredInterceptor>());
        sf1.setPrincipal(new SessionPrincipal(
                "user1","group","event","session1"));
        curr1.id = id1;
        curr1.adapter = adapter;

        sf2 = new ServiceFactoryI(cache);
        sf2.setApplicationContext(ctx);
        sf2.setInterceptors(new ArrayList<HardWiredInterceptor>());
        sf2.setPrincipal(new SessionPrincipal(
                "user2","group","event","session2"));
        curr2.id = id2;
        curr2.adapter = adapter;
    }

    @Test
    void testActiveServicesListsOnlyOwnServices() throws Exception {
        
        // on returned null, new services will be created and added
        mockAdapter.expects(once()).method("find").will(returnValue(null));
        mockAdapter.expects(once()).method("add").will(returnValue(null));
        mockAdapter.expects(once()).method("createProxy").will(
            returnValue(admin1));
        mockAdapter.expects(once()).method("find").will(returnValue(null));
        mockAdapter.expects(once()).method("add").will(returnValue(null));
        mockAdapter.expects(once()).method("createProxy").will(
            returnValue(admin2));
        IAdminPrx prx1 = sf1.getAdminService(curr1);
        IAdminPrx prx2 = sf2.getAdminService(curr2);

        // First we make sure that only one service is seen by this session
        assertTrue(sf1.activeServices(curr1).size() == 1);

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
