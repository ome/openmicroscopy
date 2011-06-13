/*
 *   Copyright (C) 2007-2011 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.server.itests;

import ome.api.IRepositoryInfo;
import ome.conditions.InternalException;

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
    }

    @Test(expectedExceptions={InternalException.class})
    public void testRepositoryUsed() throws Exception {

        IRepositoryInfo iRepositoryInfo = factory.getRepositoryInfoService();
        iRepositoryInfo.getUsedSpaceInKilobytes();
    }

    @Test(expectedExceptions={InternalException.class})
    public void testRepositoryPercentUsed() throws Exception {

        IRepositoryInfo iRepositoryInfo = factory.getRepositoryInfoService();
        iRepositoryInfo.getUsageFraction();
    }

    /**
     * Query database eventlog table for objects marked for delete and
     * remove unused artifacts from file repository
     * 
     * @throws Exception
     */
    @Test
    public void testRemoveUnusedFiles() throws Exception {

        IRepositoryInfo iRepositoryInfo = factory.getRepositoryInfoService();
        iRepositoryInfo.removeUnusedFiles();
    }

}
