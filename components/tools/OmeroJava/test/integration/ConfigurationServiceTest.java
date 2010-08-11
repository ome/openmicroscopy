/*
 * $Id$
 *
 *   Copyright 2006-2010 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package integration;


//Java imports

//Third-party libraries
import omero.api.IConfigPrx;
import omero.api.IContainerPrx;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

//Application-internal dependencies

/** 
 * Collections of tests for the <code>IConfig</code> service.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
@Test(groups = { "client", "integration", "blitz" })
public class ConfigurationServiceTest 
	extends AbstractTest
{

	 /** Helper reference to the <code>IConfig</code> service. */
    private IConfigPrx iConfig;
    
    /**
     * Initializes the various services.
     * 
     * @throws Exception Thrown if an error occurred.
     */
    @Override
    @BeforeClass
    protected void setUp() 
    	throws Exception 
    {   
    	super.setUp();
    	iConfig = factory.getConfigService();
    }
    
	/**
	 * Tests the <code>getServerTime</code> method. Access the method as 
	 * a non admin user.
	 * @throws Exception Thrown if an error occurred.
	 */
	@Test
    public void testServerTime() 
    	throws Exception
    {
		 assertNotNull(iConfig.getServerTime());
    }
	
	/**
	 * Tests the <code>getDatabaseTime</code> method. Access the method as 
	 * a non admin user.
	 * @throws Exception Thrown if an error occurred.
	 */
	@Test
    public void testDatabaseTime() 
    	throws Exception
    {
		 assertNotNull(iConfig.getDatabaseTime());
    }
	
	/**
	 * Tests the <code>getServerTime</code> method. Access the method as 
	 * an admin user.
	 * @throws Exception Thrown if an error occurred.
	 */
	@Test
    public void testServerTimeAsAdmin() 
    	throws Exception
    {
		IConfigPrx svc = root.getSession().getConfigService();
		assertNotNull(svc.getServerTime());
    }
	
	/**
	 * Tests the <code>getDatabaseTime</code> method. Access the method as 
	 * an admin user.
	 * @throws Exception Thrown if an error occurred.
	 */
	@Test
    public void testDatabaseTimeAsAdmin() 
    	throws Exception
    {
		IConfigPrx svc = root.getSession().getConfigService();
		assertNotNull(svc.getDatabaseTime());
    }
	
	/**
	 * Tests the <code>getDatabaseTime</code> method. Access the method as 
	 * an admin user.
	 * @throws Exception Thrown if an error occurred.
	 */
	@Test
    public void testDatabaseUUIDAsAdmin() 
    	throws Exception
    {
		IConfigPrx svc = root.getSession().getConfigService();
		assertNotNull(svc.getDatabaseUuid());
    }
	
	/**
	 * Tests the <code>getDatabaseUuid</code> method. Access the method as 
	 * a non admin user.
	 * @throws Exception Thrown if an error occurred.
	 */
	@Test
    public void testDatabaseUUID() 
    	throws Exception
    {
		assertNotNull(iConfig.getDatabaseUuid());
    }
	
	/**
	 * Tests the <code>setConfigValue</code> method. Access the method as 
	 * an admin user.
	 * @throws Exception Thrown if an error occurred.
	 */
	@Test
	public void testSetConfigValueAsAdmin()
		throws Exception
	{
		String key = "test";
		String value = "test2";
		IConfigPrx svc = root.getSession().getConfigService();
		svc.setConfigValue(key, value);
		assertNotNull(svc.getConfigValue(key));
		assertTrue(svc.getConfigValue(key).equals(value));
	}
	
	/**
	 * Tests the <code>setConfigValue</code> method. Access the method as 
	 * a non admin user.
	 * @throws Exception Thrown if an error occurred.
	 */
	@Test
	public void testSetConfigValue()
		throws Exception
	{
		String key = "test1";
		String value = "test3";
		try {
			iConfig.setConfigValue(key, value);
			fail("A non admin user cannot configure the server");
		} catch (Exception e) {
		}
	}
	
}
