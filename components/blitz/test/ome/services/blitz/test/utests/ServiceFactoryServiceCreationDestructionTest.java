/*
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.blitz.test.utests;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import ome.logic.HardWiredInterceptor;
import ome.services.blitz.impl.ServiceFactoryI;
import ome.services.sessions.SessionManager;
import ome.services.util.Executor;
import ome.system.OmeroContext;
import ome.system.Principal;
import omero.api.IAdminPrxHelper;
import omero.api._IAdminTie;
import omero.constants.CLIENTUUID;

import org.aopalliance.intercept.MethodInvocation;
import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import Ice.OperationMode;

public class ServiceFactoryServiceCreationDestructionTest extends
        MockObjectTestCase {

    protected Executor executor = new Executor.Impl(null, null, null, null) {
        @Override
        public Object execute(Principal p, Work work) {
            return work.doWork(null, null);
        }
    };

    Ice.Current curr;
    Mock mockCache, mockAdapter, mockManager;
    Ehcache cache;
    Ice.ObjectAdapter adapter;
    SessionManager manager;
    ServiceFactoryI sf;
    Map<String, Ice.Object> map;
    Ice.Current current = new Ice.Current();
    {
        current.ctx = new HashMap<String, String>();
        current.ctx.put(CLIENTUUID.value, "clientuuid");
    }

    // FIXME OmeroContext should be an interface!
    // OmeroContext context = new OmeroContext(new String[]{
    // "classpath:ome.services.blitz.test.utests/ServiceFactoryTest.xml",
    // "classpath:ome/services/blitz-servantDefinitions.xml"});
    //    
    OmeroContext context;

    String adminServiceId = "sessionuuid/omero.api.IAdmin";
    String reServiceId = "sessionuuid/uuid-omero.api.RenderingEngine";

    @Override
    @AfterMethod
    protected void tearDown() throws Exception {
        sf = null;
    }

    @Override
    @BeforeMethod
    protected void setUp() throws Exception {

        context = OmeroContext.getInstance("OMERO.blitz.test");
        context.refresh(); // Repairing from other methods

        map = new HashMap<String, Ice.Object>();

        mockCache = mock(Ehcache.class);
        cache = (Ehcache) mockCache.proxy();

        mockAdapter = mock(Ice.ObjectAdapter.class);
        adapter = (Ice.ObjectAdapter) mockAdapter.proxy();

        mockManager = mock(SessionManager.class);
        manager = (SessionManager) mockManager.proxy();
        mockManager.expects(once()).method("inMemoryCache").will(
                returnValue(cache));
        mockCache.expects(once()).method("isKeyInCache")
                .will(returnValue(true));
        mockCache.expects(once()).method("get").will(
                returnValue(new Element("activeServants", map)));

        curr = new Ice.Current();
        curr.adapter = adapter;
        curr.id = Ice.Util.stringToIdentity("username/sessionuuid");

        List<HardWiredInterceptor> hwi = new ArrayList<HardWiredInterceptor>();
        hwi.add(new HardWiredInterceptor() {
            public Object invoke(MethodInvocation arg0) throws Throwable {
                return arg0.proceed();
            }
        });

        Principal p = new Principal("session", "group", "type");
        sf = new ServiceFactoryI(current,
                new omero.util.ServantHolder("session"),
                null, context, manager, executor, p, hwi, null, null);
    };

    @Test
    public void testDoStatelessAddsServantToServantListCacheAndAdapter()
            throws Exception {
        IAdminPrxHelper admin = new IAdminPrxHelper();
        admin.setup(new Ref());

        callsActiveServices(Collections.singletonList(adminServiceId));
        map.put(adminServiceId, new _IAdminTie());
        mockAdapter.expects(once()).method("add").will(returnValue(null));
        mockAdapter.expects(once()).method("find").will(returnValue(null));
        mockAdapter.expects(once()).method("createDirectProxy").will(
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
        mockAdapter.expects(once()).method("add").will(returnValue(null));
        mockAdapter.expects(once()).method("createDirectProxy").will(
                returnValue(null));
        mockAdapter.expects(once()).method("find").will(returnValue(null));
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
        callsActiveServices(Collections.singletonList(reServiceId));
        String id = sf.activeServices(curr).get(0).toString();
        // Events now called by SessionManagerI
        sf.unregisterServant(Ice.Util.stringToIdentity(id));
    }

    private void callsActiveServices(List<String> idList) {

    }

}
