/*
 * $Id$
 *
 *   Copyright 2006-2010 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package integration;


//Java imports

//Third-party libraries
import org.testng.annotations.Test;

//Application-internal dependencies
import omero.InternalException;
import omero.api.IRepositoryInfoPrx;

/** 
 *  Collections of tests for the <code>Repository</code> service.
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
public class RepositoryServiceTest 
	extends AbstractTest
{

	/**
	 * Tests the <code>getFreeSpaceInKilobytes</code> method.
	 * @throws Exception Thrown if an error occurred.
	 */
	@Test
    public void testFreeSpace() 
    	throws Exception
    {
		IRepositoryInfoPrx svc = root.getSession().getRepositoryInfoService();
		assertTrue(svc.getFreeSpaceInKilobytes() > 0);
    }
	
	/**
	 * Tests the <code>getUsedSpaceInKilobytes</code> method.
	 * @throws Exception Thrown if an error occurred.
	 */
	@Test(expectedExceptions={InternalException.class})
    public void testUsedSpace() 
    	throws Exception
    {
		IRepositoryInfoPrx svc = root.getSession().getRepositoryInfoService();
		svc.getUsedSpaceInKilobytes();
    }
	
	/**
	 * Tests the <code>getUsageFraction</code> method.
	 * @throws Exception Thrown if an error occurred.
	 */
	@Test(expectedExceptions={InternalException.class})
    public void testUsageFraction() 
    	throws Exception
    {
		IRepositoryInfoPrx svc = root.getSession().getRepositoryInfoService();
		svc.getUsageFraction();
    }
	
}
