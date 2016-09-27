/*
 * $Id$
 *
 *   Copyright 2006-2010 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package integration;

import omero.InternalException;
import omero.api.IRepositoryInfoPrx;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Collections of tests for the <code>Repository</code> service.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:donald@lifesci.dundee.ac.uk"
 *         >donald@lifesci.dundee.ac.uk</a>
 * @version 3.0 <small> (<b>Internal version:</b> $Revision: $Date: $) </small>
 * @since 3.0-Beta4
 */
public class RepositoryServiceTest extends AbstractServerTest {

    /**
     * Tests the <code>getFreeSpaceInKilobytes</code> method.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testFreeSpace() throws Exception {
        IRepositoryInfoPrx svc = root.getSession().getRepositoryInfoService();
        Assert.assertTrue(svc.getFreeSpaceInKilobytes() > 0);
    }

    /**
     * Tests the <code>getUsedSpaceInKilobytes</code> method.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test(expectedExceptions = { InternalException.class })
    public void testUsedSpace() throws Exception {
        IRepositoryInfoPrx svc = root.getSession().getRepositoryInfoService();
        svc.getUsedSpaceInKilobytes();
    }

    /**
     * Tests the <code>getUsageFraction</code> method.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test(expectedExceptions = { InternalException.class })
    public void testUsageFraction() throws Exception {
        IRepositoryInfoPrx svc = root.getSession().getRepositoryInfoService();
        svc.getUsageFraction();
    }

}
