/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.icy.model.itests.manual;

import java.util.List;

import ome.icy.fixtures.BlitzServerFixture;
import omero.api.RenderingEnginePrx;
import omero.api.ServiceFactoryPrx;
import omero.api.ServiceInterfacePrx;

import org.jmock.MockObjectTestCase;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

@Test( groups = {"integration","manual"})
public class RegisteredServicesTest extends MockObjectTestCase {

	BlitzServerFixture fixture;

	@BeforeTest
	public void setUp() throws Exception {
		fixture = new BlitzServerFixture();
	}
	
	@AfterTest
	public void tearDown() throws Exception {
		fixture.tearDown();
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
        
        List<String> idsA = session.activeServices();
        
        prx1.close();
        
        List<String> idsB = session.activeServices();
        
        assertTrue(idsA.size() == 2);
        assertTrue(idsB.size() == 1);

    }
        
}
