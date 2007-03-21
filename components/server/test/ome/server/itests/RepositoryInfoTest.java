package ome.server.itests;

import ome.api.IRepositoryInfo;
import ome.logic.RepositoryInfoImpl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.Test;

/**
 * Simple server-side test of the ome.api.IRepositoryInfo service.
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
public class RepositoryInfoTest extends AbstractManagedContextTest {
	
    @Test
    public void testRepositoryFree() throws Exception {

        IRepositoryInfo iRepositoryInfo = factory.getRepositoryInfoService();
        assertTrue(iRepositoryInfo.getFreeSpaceInKilobytes() > 0);
        System.out.println("testRepositoryFree successful");
    }	

    @Test
    public void testRepositoryUsed() throws Exception {

        IRepositoryInfo iRepositoryInfo = factory.getRepositoryInfoService();
        assertTrue(iRepositoryInfo.getUsedSpaceInKilobytes() > 0);
        System.out.println("testRepositoryUsed successful");
    }	

    @Test
    public void testRepositoryPercentUsed() throws Exception {

        IRepositoryInfo iRepositoryInfo = factory.getRepositoryInfoService();
    	
        assertTrue(iRepositoryInfo.getUsageFraction() < 0.95);
        System.out.println("testRepositoryPercentUsed successful");
    }	

}
