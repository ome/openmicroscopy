/*
 * Created on Feb 10, 2005
 */
package org.ome.tests.client;

import junit.framework.TestCase;

import org.ome.client.TemporaryFactoryFactory;
import org.ome.client.rmi.ServiceFactoryImpl;
import org.ome.interfaces.AdministrationService;
import org.ome.interfaces.ServiceFactory;
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
		ServiceFactory factory = TemporaryFactoryFactory.getServiceFactory();
		a = factory.getAdministrationService();
	}
}
