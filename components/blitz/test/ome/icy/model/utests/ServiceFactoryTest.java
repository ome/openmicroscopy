/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.icy.model.utests;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;

import ome.logic.HardWiredInterceptor;
import ome.services.blitz.impl.ServiceFactoryI;
import ome.system.OmeroContext;

import org.aopalliance.intercept.MethodInvocation;
import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.springframework.cache.ehcache.EhCacheFactoryBean;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class ServiceFactoryTest extends MockObjectTestCase {

    Ice.Current curr;
    Mock mockCache, mockAdapter;
    Ehcache cache;
    Ice.ObjectAdapter adapter;
    ServiceFactoryI sf;
    
    // FIXME OmeroContext should be an interface!
    OmeroContext context = new OmeroContext("classpath:ome/icy/model/utests/ServiceFactoryTest.xml");
    
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
    };
    
    @Test
    public void testDoStatelessAddsServantToServantListCacheAndAdapter() throws Exception {
        mockCache.expects(once()).method("get").will(returnValue(null));
        mockCache.expects(once()).method("put");
        mockAdapter.expects(once()).method("add").will(returnValue(null));
        mockAdapter.expects(once()).method("createProxy").will(returnValue(null));
        sf.getAdminService(curr);
        List<String> ids = sf.getIds();
        assertTrue(ids.toString(), ids.size()==1);
        assertTrue(ids.toString(),ids.get(0).endsWith("Admin"));
    }
    
    @Test
    public void testDoStatefulAddsServantToServantListCacheAndAdapter() throws Exception {
        mockCache.expects(once()).method("put");
        mockAdapter.expects(once()).method("add").will(returnValue(null));
        mockAdapter.expects(once()).method("createProxy").will(returnValue(null));
        sf.createRenderingEngine(curr);
        List<String> ids = sf.getIds();
        assertTrue(ids.toString(), ids.size()==1);
        assertTrue(ids.toString(),ids.get(0).endsWith("RenderingEngine"));
    }
   
}
