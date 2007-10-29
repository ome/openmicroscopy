/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.icy.model.utests;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import ome.icy.service.utests.Ref;
import ome.logic.HardWiredInterceptor;
import ome.services.blitz.fire.SessionPrincipal;
import ome.services.blitz.impl.ServiceFactoryI;
import ome.services.blitz.util.UnregisterServantMessage;
import ome.system.OmeroContext;
import omero.api.IAdminPrxHelper;

import org.aopalliance.intercept.MethodInvocation;
import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import Ice.OperationMode;

public class ServiceFactoryServiceCreationDestructionTest extends
        MockObjectTestCase {

    Ice.Current curr;
    Mock mockCache, mockAdapter;
    Ehcache cache;
    Ice.ObjectAdapter adapter;
    ServiceFactoryI sf;

    // FIXME OmeroContext should be an interface!
    // OmeroContext context = new OmeroContext(new String[]{
    // "classpath:ome/icy/model/utests/ServiceFactoryTest.xml",
    // "classpath:ome/services/blitz-servantDefinitions.xml"});
    //    
    OmeroContext context = OmeroContext.getInstance("OMERO.blitz.test");

    Ice.Identity adminServiceId = Ice.Util
            .stringToIdentity("sessionuuid/ome.api.IAdmin");
    Element adminElt = new Element(adminServiceId, new Object());
    Ice.Identity reServiceId = Ice.Util
            .stringToIdentity("sessionuuid/uuid-omeis.providers.re.RenderingEngine");
    Element reElt = new Element(reServiceId, new Object());

    @Override
    @AfterMethod
    protected void tearDown() throws Exception {
        sf = null;
    }

    @Override
    @BeforeMethod
    protected void setUp() throws Exception {

        context.refresh(); // Repairing from other methods

        mockCache = mock(Ehcache.class);
        cache = (Ehcache) mockCache.proxy();

        mockAdapter = mock(Ice.ObjectAdapter.class);
        adapter = (Ice.ObjectAdapter) mockAdapter.proxy();

        curr = new Ice.Current();
        curr.adapter = adapter;
        curr.id = Ice.Util.stringToIdentity("username/sessionuuid");

        List<HardWiredInterceptor> hwi = new ArrayList<HardWiredInterceptor>();
        hwi.add(new HardWiredInterceptor() {
            public Object invoke(MethodInvocation arg0) throws Throwable {
                return arg0.proceed();
            }
        });

        sf = new ServiceFactoryI(cache);
        sf.setApplicationContext(context);
        sf.setInterceptors(hwi);
        sf
                .setPrincipal(new SessionPrincipal("user", "group", "type",
                        "session"));
    };

    @Test
    public void testDoStatelessAddsServantToServantListCacheAndAdapter()
            throws Exception {
        IAdminPrxHelper admin = new IAdminPrxHelper();
        admin.setup(new Ref());

        callsActiveServices(Collections.singletonList(adminServiceId));
        mockCache.expects(once()).method("get").will(returnValue(adminElt));
        mockCache.expects(once()).method("put");
        mockAdapter.expects(once()).method("add").will(returnValue(null));
        mockAdapter.expects(once()).method("find").will(returnValue(null));
        mockAdapter.expects(once()).method("createProxy").will(
                returnValue(admin));
        sf.getAdminService(curr);
        List<String> ids = sf.activeServices(curr);
        assertTrue(ids.toString(), ids.size() == 1);
        assertTrue(ids.toString(), ids.get(0).endsWith("Admin"));
    }

    @Test
    public void testDoStatefulAddsServantToServantListCacheAndAdapter()
            throws Exception {

        callsActiveServices(Collections.singletonList(reServiceId));
        mockCache.expects(once()).method("put");
        mockAdapter.expects(once()).method("add").will(returnValue(null));
        mockAdapter.expects(once()).method("createProxy").will(
                returnValue(null));
        mockAdapter.expects(once()).method("find").will(returnValue(null));
        sf.createRenderingEngine(curr);
        List<String> ids = sf.activeServices(curr);
        assertTrue(ids.toString(), ids.size() == 1);
        assertTrue(ids.toString(), ids.get(0).endsWith("RenderingEngine"));
    }

    @Test
    void testCallingCloseOnSessionClosesAllProxies() throws Exception {
        testDoStatefulAddsServantToServantListCacheAndAdapter();
        Mock closeMock = mock(omero.api.RenderingEngine.class);
        omero.api.RenderingEngine close = (omero.api.RenderingEngine) closeMock
                .proxy();
        mockAdapter.expects(once()).method("find").will(returnValue(close));
        mockAdapter.expects(once()).method("remove").will(returnValue(close));
        mockCache.expects(once()).method("remove").will(returnValue(true));
        callsActiveServices(Collections.singletonList(reServiceId));
        Ice.Current curr = new Ice.Current();
        curr.id = Ice.Util.stringToIdentity("username/sessionuuid");
        curr.adapter = adapter;
        curr.mode = OperationMode.Idempotent; // FIXME Due to Ice bug
        sf.close(curr);
    }

    @Test
    void testUnregisterEventCallsClose() throws Exception {
        testDoStatefulAddsServantToServantListCacheAndAdapter();
        Mock closeMock = mock(omero.api.RenderingEngine.class);
        omero.api.RenderingEngine close = (omero.api.RenderingEngine) closeMock
                .proxy();
        mockAdapter.expects(once()).method("remove").will(returnValue(close));
        mockCache.expects(once()).method("remove").will(returnValue(true));
        callsActiveServices(Collections.singletonList(reServiceId));
        String id = sf.activeServices(curr).get(0).toString();
        Ice.Current curr = new Ice.Current();
        curr.id = Ice.Util.stringToIdentity("username/sessionid");
        curr.adapter = adapter;
        sf.onApplicationEvent(new UnregisterServantMessage(this, id, curr));
    }

    private void callsActiveServices(List<Ice.Identity> idList) {
        mockCache.expects(once()).method("getKeysWithExpiryCheck").will(
                returnValue(idList));
    }

}
