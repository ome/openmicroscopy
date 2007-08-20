/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.icy.model.itests.manual;

import ome.icy.fixtures.BlitzServerFixture;
import omero.api.RenderingEnginePrx;
import omero.api.ServiceFactoryPrx;
import omero.api.ServiceInterfacePrx;

import org.jmock.MockObjectTestCase;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test( groups = {"integration","manual"})
/**
 * Note: Using the {@link Router} wrapper class can cause processes to
 * be orphaned on the OS.
 */
public class ServiceTimeoutTest extends MockObjectTestCase {

	BlitzServerFixture fixture;

	@BeforeMethod
	public void setUp() throws Exception {
		fixture = new BlitzServerFixture();
	}
	
	@AfterMethod
	public void tearDown() throws Exception {
		fixture.tearDown();
	}
	
    @Test
    public void testkeepAllAliveKeepsSessionAlive() throws Exception {
    	fixture.setServiceTimeout(3);
    	fixture.setSessionTimeout(3);        
    	fixture.startServer();
    	ServiceFactoryPrx session = fixture.createSession();

        RenderingEnginePrx prx = session.createRenderingEngine();
        assertNotNull( prx );
        session.ice_ping();
        assertTrue( session.keepAlive(prx) );
        Thread.sleep(900L);
        assertTrue( session.keepAlive(prx) );
        Thread.sleep(900L);
        assertTrue( session.keepAlive(prx) );
        Thread.sleep(900L);
        assertTrue( session.keepAlive(prx) );
        Thread.sleep(900L);
        assertTrue( session.keepAlive(prx) );

    }
    
    @Test
    public void testkeepAllAliveAndkeepAliveWorkAfterPause() throws Exception {
    	fixture.setServiceTimeout(2);
    	fixture.setSessionTimeout(200); // this is not what we're testing

    	fixture.msMock.expects(once()).method("isActive").will(returnValue(true));
    	fixture.msMock.expects(once()).method("checkMethod");
    	fixture.secSysMock.expects(once()).method("login");
    	fixture.secSysMock.expects(once()).method("logout");
    	fixture.reMock.expects(once()).method("close");
    	
    	fixture.startServer();

    	ServiceFactoryPrx session = fixture.createSession();
    	
        RenderingEnginePrx prx1 = session.createRenderingEngine();
        RenderingEnginePrx prx2 = session.createRenderingEngine();
        assertNotNull( prx1 );
        assertNotNull( prx2 );
        assertTrue( session.keepAlive(prx1));
        assertTrue( 0==session.keepAllAlive(new ServiceInterfacePrx[]{prx1}));
        Thread.sleep(1500L);
        assertTrue( session.keepAlive(prx1));
        assertTrue( 0==session.keepAllAlive(new ServiceInterfacePrx[]{prx1}));
        Thread.sleep(1500L);
        assertTrue( session.keepAlive(prx1));
        assertTrue( 0==session.keepAllAlive(new ServiceInterfacePrx[]{prx1}));
        Thread.sleep(1500L);
        assertTrue( session.keepAlive(prx1));
        assertTrue( 0==session.keepAllAlive(new ServiceInterfacePrx[]{prx1}));
        Thread.sleep(1500L);
        assertFalse( session.keepAlive(prx2));
        assertFalse( 0==session.keepAllAlive(new ServiceInterfacePrx[]{prx2}));
        
    }
    

    
}
