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

import org.jmock.MockObjectTestCase;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;


@Test( groups = {"integration","manual"})
public class SessionTimeoutTest extends MockObjectTestCase {

	BlitzServerFixture fixture;

	@BeforeTest
	public void setUp() throws Exception {
		fixture = new BlitzServerFixture();
    	fixture.setServiceTimeout(3);
    	fixture.setSessionTimeout(3);        
    	fixture.startServer();
	}
	
	@AfterTest
	public void tearDown() throws Exception {
		fixture.tearDown();
	}
	
    @Test
    public void testkeepAllAliveKeepsSessionAlive() throws Exception {
    	ServiceFactoryPrx session = fixture.createSession();

        RenderingEnginePrx prx = session.createRenderingEngine();
        assertNotNull( prx );
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
    public void testSessionStillDiesThough() throws Exception {
    	ServiceFactoryPrx session = fixture.createSession();

        RenderingEnginePrx prx = session.createRenderingEngine();
        assertNotNull( prx );
        assertTrue( session.keepAlive(prx) );
        Thread.sleep(4000L);
        try { 
        	session.keepAlive(prx);
        	fail("This shouldn't succeed.");
        } catch (Exception e) {
        	// ok
        }
    }
    
}
