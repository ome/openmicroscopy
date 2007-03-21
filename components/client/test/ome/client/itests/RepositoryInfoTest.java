package ome.client.itests;

import ome.api.IRepositoryInfo;
import ome.system.Login;
import ome.system.ServiceFactory;

import org.testng.annotations.Configuration;
import org.testng.annotations.Test;

import junit.framework.TestCase;

/**
 * Simple client-side test using the server for the 
 * ome.api.IRepositoryInfo service.
 * 
 * <p>
 * Copyright 2007 Glencoe Software Inc. All rights reserved.
 * Use is subject to license terms supplied in LICENSE.txt 
 * <p/>
 *
 * @author David L. Whitehurst &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:david@glencoesoftware.com">david@glencoesoftware.com</a>
 * @version $Revision$
 * @since 3.0
 */
@Test(groups = { "ticket:39", "integration" })
public class RepositoryInfoTest extends TestCase{

	/* service interface */
	IRepositoryInfo iRepositoryInfo;
	
    @Override
    @Configuration(beforeTestClass = true)
    protected void setUp() throws Exception {

    	ServiceFactory sf = new ServiceFactory();
        iRepositoryInfo = sf.getRepositoryInfoService();
        
    }

    @Test
    public void testRepositoryFree() throws Exception {
        assertTrue(iRepositoryInfo.getFreeSpaceInKilobytes() > 0);
    }	

    @Test
    public void testRepositoryUsed() throws Exception {
        assertTrue(iRepositoryInfo.getUsedSpaceInKilobytes() > 0);
    }	

    @Test
    public void testRepositoryPercentUsed() throws Exception {
        assertTrue(iRepositoryInfo.getUsageFraction() < 0.95);
    }	
    
}
