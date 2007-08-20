/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.icy.service.utests;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import ome.services.blitz.impl.ServiceFactoryI;
import omero.api.ServiceInterfacePrx;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.testng.annotations.Configuration;
import org.testng.annotations.Test;

@Test
public class ServiceFactoryKeepAliveUnitTest extends MockObjectTestCase {

	Element elt = new Element(null,null);
	Mock cacheMock, proxyMock;
	Ehcache cache; 
	ServiceFactoryI sf;
	ServiceInterfacePrx prx;
	Ice.Identity id = Ice.Util.stringToIdentity("test");
	
    @Override
    @Configuration(beforeTestMethod = true)
    protected void setUp() throws Exception {
    	cacheMock = mock(Ehcache.class);
    	cache = (Ehcache) cacheMock.proxy();
    	proxyMock = mock(ServiceInterfacePrx.class);
    	prx = (ServiceInterfacePrx) proxyMock.proxy();
    	sf = new ServiceFactoryI(cache);
    }

    @Test
    void testKeepAliveReturnsAllOnesOnNull() {
    	assertTrue(-1==sf.keepAlive(null,null));
    	assertTrue(-1==sf.keepAlive(new ServiceInterfacePrx[]{}));
    }
    
    @Test
    void testKeepAliveReturnsNonNullIfMissing() {
    	cacheMock.expects(once()).method("get").will(returnValue(null));
    	proxyMock.expects(once()).method("ice_getIdentity").will(returnValue(id));
    	long rv = sf.keepAlive(new ServiceInterfacePrx[]{prx});
    	assertTrue((rv & 1<<0) == 1<<0);
    }
    
    @Test
    void testIsAliveReturnsFalseIfMissing() {
    	cacheMock.expects(once()).method("get").will(returnValue(null));
    	proxyMock.expects(once()).method("ice_getIdentity").will(returnValue(id));
    	assertFalse(sf.isAlive(prx));
    }
    
    @Test
    void testKeepAliveReturnsZeroIfPresent() {
    	cacheMock.expects(once()).method("get").will(returnValue(elt));
    	proxyMock.expects(once()).method("ice_getIdentity").will(returnValue(id));
    	long rv = sf.keepAlive(new ServiceInterfacePrx[]{prx});
    	assertTrue(rv == 0);
    }
    
    @Test
    void testIsAliveReturnsTrueIfPresent() {
    	cacheMock.expects(once()).method("get").will(returnValue(elt));
    	proxyMock.expects(once()).method("ice_getIdentity").will(returnValue(id));
    	assertTrue(sf.isAlive(prx));
    }
    
}
