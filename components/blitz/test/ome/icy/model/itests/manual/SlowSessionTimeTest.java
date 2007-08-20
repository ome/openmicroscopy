/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.icy.model.itests.manual;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import junit.framework.TestCase;
import ome.services.blitz.Main;
import ome.services.blitz.Router;
import ome.services.blitz.client.IceServiceFactory;
import omero.api.RenderingEnginePrx;
import omero.api.ServiceFactoryPrx;
import omero.api.ServiceInterfacePrx;

import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

@Test( groups = {"integration","manual"})
public class SlowSessionTimeTest extends TestCase {

	Thread t;
	Main m;
	Router r;
    IceServiceFactory ice;

    @BeforeTest
    public void startServer() throws Exception {
    	m = new Main("OMERO.blitz.test");
    	t = new Thread(m);
    	r = new Router();
    	r.setTimeout(3); // one second
    	m.setRouter(r);
    	t.start();
    	assertTrue("Startup must succeed", m.waitForStartup() );
    	
    }
    
    @AfterTest
    public void stopServer() throws Exception {
    	m.stop();
    }

    @Test
    public void testKeepAliveKeepsSessionAlive() throws Exception {
        ice = new IceServiceFactory(null, null, null);
        ice.createSession();
        ServiceFactoryPrx session = ice.getProxy();
        RenderingEnginePrx prx = ice.createRenderingEngine(null);
        assertNotNull( prx );
        assertTrue( session.isAlive(prx));
        Thread.sleep(1000L);
        assertTrue( session.isAlive(prx));
        Thread.sleep(1000L);
        assertTrue( session.isAlive(prx));
        Thread.sleep(1000L);
        assertTrue( session.isAlive(prx));
        Thread.sleep(1000L);
        assertTrue( session.isAlive(prx));
        ice.destroy();
        
    }
    
    @Test
    public void testKeepAliveAndIsAliveWorkAfterPause() throws Exception {
        ice = new IceServiceFactory(null, null, null);
        ice.createSession();
        ServiceFactoryPrx session = ice.getProxy();
        RenderingEnginePrx prx = ice.createRenderingEngine(null);
        assertNotNull( prx );
        assertTrue( session.isAlive(prx));
        assertTrue( 0==session.keepAlive(new ServiceInterfacePrx[]{prx}));
        Thread.sleep(3000L);
        assertFalse( session.isAlive(prx));
        assertFalse( 0==session.keepAlive(new ServiceInterfacePrx[]{prx}));
        ice.destroy();
        
    }
    
}
