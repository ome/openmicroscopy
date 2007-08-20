/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.icy.model.utests;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
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

import Ice.BooleanHolder;
import Ice.ConnectionI;
import Ice.EndpointSelectionType;
import Ice.InitializationData;
import Ice.LocatorPrx;
import Ice.RouterPrx;
import IceInternal.EndpointI;
import IceInternal.Reference;

public class ServiceFactoryServiceCreationDestructionTest extends MockObjectTestCase {

    Ice.Current curr;
    Mock mockCache, mockAdapter;
    Ehcache cache;
    Ice.ObjectAdapter adapter;
    ServiceFactoryI sf;
    
    // FIXME OmeroContext should be an interface!
//    OmeroContext context = new OmeroContext(new String[]{
//    		"classpath:ome/icy/model/utests/ServiceFactoryTest.xml",
//    		"classpath:ome/services/blitz-servantDefinitions.xml"});
//    
    OmeroContext context = OmeroContext.getInstance("OMERO.blitz.test");
    
    Ice.Identity adminServiceId = new Ice.Identity("omero.api.IAdmin","someuuid");
    Element adminElt = new Element(adminServiceId,new Object());
    Ice.Identity reServiceId = new Ice.Identity("omere.api.RenderingEngine","someuuid");
    Element reElt = new Element(reServiceId,new Object());

    @AfterMethod
    protected void tearDown() throws Exception {
        sf = null;
    }
    
    @BeforeMethod
    protected void setUp() throws Exception {

        mockCache = mock(Ehcache.class);
        cache = (Ehcache) mockCache.proxy();

        mockAdapter = mock(Ice.ObjectAdapter.class);
        adapter = (Ice.ObjectAdapter) mockAdapter.proxy();
        
        curr = new Ice.Current();
        curr.id = new Ice.Identity();
        curr.id.name = "test";
        curr.id.category = "cat";
        curr.adapter = adapter;

        List<HardWiredInterceptor> hwi = new ArrayList<HardWiredInterceptor>();
        hwi.add(new HardWiredInterceptor(){
           public Object invoke(MethodInvocation arg0) throws Throwable {
                return arg0.proceed();
            } 
        });
        
        sf = new ServiceFactoryI(cache);
        sf.setApplicationContext(context);
        sf.setInterceptors(hwi);
        sf.setPrincipal(new SessionPrincipal("user","group","type","session"));
    };
    
    @Test
    public void testDoStatelessAddsServantToServantListCacheAndAdapter() throws Exception {
        IAdminPrxHelper admin = new IAdminPrxHelper();
        admin.setup(new Ref());
        
        callsActiveServices(Collections.singletonList(adminServiceId));
    	mockCache.expects(once()).method("get").will(returnValue(adminElt));
    	mockCache.expects(once()).method("put");
        mockAdapter.expects(once()).method("add").will(returnValue(null));
        mockAdapter.expects(once()).method("find").will(returnValue(null));
        mockAdapter.expects(once()).method("createProxy").will(returnValue(admin));
        sf.getAdminService(curr);
        List<String> ids = sf.activeServices(null);
        assertTrue(ids.toString(), ids.size()==1);
        assertTrue(ids.toString(),ids.get(0).endsWith("Admin"));
    }
    
    @Test
    public void testDoStatefulAddsServantToServantListCacheAndAdapter() throws Exception {
        
    	callsActiveServices(Collections.singletonList(reServiceId));
    	mockCache.expects(once()).method("put");
        mockAdapter.expects(once()).method("add").will(returnValue(null));
        mockAdapter.expects(once()).method("createProxy").will(returnValue(null));
        mockAdapter.expects(once()).method("find").will(returnValue(null));
        sf.createRenderingEngine(curr);
        List<String> ids = sf.activeServices(null);
        assertTrue(ids.toString(), ids.size()==1);
        assertTrue(ids.toString(),ids.get(0).endsWith("RenderingEngine"));
    }
   
    
    @Test
    void testCallingCloseOnSessionClosesAllProxies() throws Exception{
    	testDoStatefulAddsServantToServantListCacheAndAdapter();
    	Mock closeMock = mock(omero.api.RenderingEngine.class);
		omero.api.RenderingEngine close = (omero.api.RenderingEngine) closeMock.proxy(); 
		mockAdapter.expects(once()).method("find").will(returnValue(close));
   		mockAdapter.expects(once()).method("remove").will(returnValue(close));
   		mockCache.expects(once()).method("remove").will(returnValue(true));
   		callsActiveServices(Collections.singletonList(reServiceId));
    	Ice.Current curr = new Ice.Current();
    	curr.adapter = adapter;
   		sf.close(curr);
    }
    
    @Test
    void testUnregisterEventCallsClose() throws Exception {
    	testDoStatefulAddsServantToServantListCacheAndAdapter();
    	Mock closeMock = mock(omero.api.RenderingEngine.class);
    	omero.api.RenderingEngine close = (omero.api.RenderingEngine) closeMock.proxy(); 
    	mockAdapter.expects(once()).method("remove").will(returnValue(close));
    	mockCache.expects(once()).method("remove").will(returnValue(true));
    	callsActiveServices(Collections.singletonList(reServiceId));
    	String id = sf.activeServices(null).get(0).toString();
    	Ice.Current curr = new Ice.Current();
    	curr.adapter = adapter;
    	sf.onApplicationEvent(new UnregisterServantMessage(this,id,curr));
    }
    
	private void callsActiveServices(List<Ice.Identity> idList) {
		mockCache.expects(once()).method("getKeysWithExpiryCheck").will(returnValue(idList));
	}
    
}

class Ref extends IceInternal.Reference {

	
	public Ref() {
		super(inst(), communicator(), Ice.Util.stringToIdentity("test:test"), new HashMap(), "facet", 0);
	}

	static IceInternal.Instance inst() {
		IceInternal.Instance i = new IceInternal.Instance(null, new InitializationData());
		return i;
	}
	
	static Ice.Communicator communicator() {
		return null;
	}
	
	@Override
	public Reference changeAdapterId(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Reference changeCacheConnection(boolean arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Reference changeCollocationOptimization(boolean arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Reference changeCompress(boolean arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Reference changeConnectionId(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Reference changeEndpointSelection(EndpointSelectionType arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Reference changeEndpoints(EndpointI[] arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Reference changeLocator(LocatorPrx arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Reference changeLocatorCacheTimeout(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Reference changePreferSecure(boolean arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Reference changeRouter(RouterPrx arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Reference changeSecure(boolean arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Reference changeThreadPerConnection(boolean arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Reference changeTimeout(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getAdapterId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean getCacheConnection() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean getCollocationOptimization() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public ConnectionI getConnection(BooleanHolder arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public EndpointSelectionType getEndpointSelection() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public EndpointI[] getEndpoints() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getLocatorCacheTimeout() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean getPreferSecure() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean getSecure() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean getThreadPerConnection() {
		// TODO Auto-generated method stub
		return false;
	}
	
}
