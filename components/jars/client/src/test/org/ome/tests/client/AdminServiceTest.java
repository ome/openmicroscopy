/*
 * Created on Feb 10, 2005
 */
package org.ome.tests.client;

import junit.framework.TestCase;

import org.ome.interfaces.AdministrationService;
;

/**
 * @author josh
 */
public class AdminServiceTest extends TestCase {

	AdministrationService a;
	
	
	
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		a = (AdministrationService) SpringTestHarness.ctx.getBean("administrationService");
	}
}
