/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.icy.model.itests.manual;

import java.util.List;

import ome.icy.fixtures.BlitzServerFixture;
import omero.api.IAdminPrx;
import omero.api.ServiceFactoryPrx;

import org.jmock.MockObjectTestCase;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

@Test( groups = {"integration","manual"})
public class ApiArrayHandlingTest extends MockObjectTestCase {

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
    public void testAdminPassArrays() throws Exception {
    	
      	fixture.startServer();
    	fixture.methodCall();

    	
    	fixture.adminMock.expects(once()).method("containedExperimenters")
    		.will(returnValue(new ome.model.meta.Experimenter[]{
    				new ome.model.meta.Experimenter(1l,false),
    				new ome.model.meta.Experimenter(2l,false),
    		}));
    	
    	ServiceFactoryPrx session = fixture.createSession();
    	IAdminPrx admin = session.getAdminService();
    	List<omero.model.Experimenter> l = admin.containedExperimenters(1l);
    	
        assertTrue(l.size() == 2);
        assertTrue(l.get(0).id.val == 1l);
        assertTrue(l.get(1).id.val == 2l);
        
        session.close();
    }
        
}
