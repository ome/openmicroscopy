/*
 * ome.server.utests.ChgrpMockTest
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.server.utests;

import org.testng.annotations.*;

import ome.conditions.SecurityViolation;

/**
 * @author Josh Moore &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @since Omero 2.0
 */
public class ChgrpMockTest extends AbstractChangeDetailsMockTest {

    // Factors:
    // 1. new or managed
    // 2. root or user
    // 3. change to user or root
    // 5. TODO even laterer: allowing changes based on group privileges.

    // ~ Nonroot / New Image
    // =========================================================================

    @Test(expectedExceptions = SecurityViolation.class)
    public void test_non_root_new_image_chgrp_to_other_group() throws Exception {
        userImageChgrp(_USER, _NEW, 2L);
        filter.filter(null, i);
        super.verify();
    }

    @Test(expectedExceptions = SecurityViolation.class)
    public void test_non_root_new_image_chgrp_to_system() throws Exception {
        userImageChgrp(_USER, _NEW, SYS_GROUP_ID);
        filter.filter(null, i);
        super.verify();
    }

    // ~ Nonroot / Managed image
    // =========================================================================

    @Test(expectedExceptions = SecurityViolation.class)
    public void test_managed_image_non_root_chgrp_to_other_group()
            throws Exception {
        userImageChgrp(_USER, _MANAGED, 2L);
        willLoadImage(managedImage());
        filter.filter(null, i);
        super.verify();
    }

    @Test(expectedExceptions = SecurityViolation.class)
    public void test_managed_image_non_root_chgrp_to_system() throws Exception {
        userImageChgrp(_USER, _MANAGED, SYS_GROUP_ID);
        // willLoadImage( managedImage() );
        filter.filter(null, i);
        super.verify();
    }

    // ~ Root / new image
    // =========================================================================
    @Test
    public void test_root_new_image_chgrp_to_other_group() throws Exception {
        userImageChgrp(_ROOT, _NEW, 2L);
        // willLoadUser( 0L );
        willLoadGroup(0L);
        willLoadGroup(2L);
        willLoadEventType(0L);
        // willLoadEvent( 0L );
        filter.filter(null, i);
        super.verify();
    }

    // ~ Root / managed image
    // =========================================================================
    @Test
    public void test_root_managed_image_chgrp_to_other_group() throws Exception {
        userImageChgrp(_ROOT, _MANAGED, 2L);
        willLoadImage(managedImage());
        willLoadUser(0L);
        willLoadGroup(0L);
        willLoadGroup(2L);
        willLoadEventType(0L);
        // willLoadEvent( 0L );
        filter.filter(null, i);
        super.verify();
    }

}
