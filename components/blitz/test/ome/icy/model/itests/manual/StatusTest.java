/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.icy.model.itests.manual;

import ome.icy.fixtures.BlitzServerFixture;
import ome.services.blitz.Status;

import org.jmock.MockObjectTestCase;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;


@Test( groups = {"integration","manual"})
public class StatusTest extends MockObjectTestCase {

	BlitzServerFixture fixture;

	@BeforeTest
	public void setUp() throws Exception {
		fixture = new BlitzServerFixture();
    	fixture.startServer();
	}
	
	@AfterTest
	public void tearDown() throws Exception {
		fixture.tearDown();
	}
	
    @Test
    public void testStatus() throws Exception {
    	String[] args = new String[]{};
    	Status status = new Status(args);
    	status.run();
    }

}
