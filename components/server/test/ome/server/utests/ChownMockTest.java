/*
 * ome.server.utests.ChownMockTest
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.server.utests;

import ome.conditions.SecurityViolation;

import org.testng.annotations.Test;

/**
 * @author Josh Moore &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @since Omero 2.0
 */
@Test(groups = "broken")
public class ChownMockTest extends AbstractChangeDetailsMockTest {

    // Factors:
    // 1. new or managed
    // 2. root or user
    // 3. change to user or root
    // 5. TODO even laterer: allowing changes based on group privileges.

    // ~ Nonroot / New Image
    // =========================================================================

    @Test(expectedExceptions = SecurityViolation.class)
    public void test_non_root_new_image_chmod_to_other_user() throws Exception {
        userImageChmod(_USER, _NEW, 2L);
        filter.filter(null, i);
        super.verify();
    }

    @Test(expectedExceptions = SecurityViolation.class)
    public void test_non_root_new_image_chmod_to_root() throws Exception {
        userImageChmod(_USER, _NEW, ROOT_OWNER_ID);
        filter.filter(null, i);
        super.verify();
    }

    // ~ Nonroot / Managed image
    // =========================================================================

    @Test(expectedExceptions = SecurityViolation.class)
    public void test_managed_image_non_root_chmod_to_other_user()
            throws Exception {
        userImageChmod(_USER, _MANAGED, 2L);
        willLoadImage(managedImage());
        filter.filter(null, i);
        super.verify();
    }

    @Test(expectedExceptions = SecurityViolation.class)
    public void test_managed_image_non_root_chmod_to_root() throws Exception {
        userImageChmod(_USER, _MANAGED, 0L);
        willLoadImage(managedImage());
        filter.filter(null, i);
        super.verify();
    }

    // ~ Root / new image
    // =========================================================================
    @Test
    public void test_root_new_image_chmod_to_other_user() throws Exception {
        userImageChmod(_ROOT, _NEW, 2L);
        willLoadUser(2L);
        willLoadGroup(0L);
        willLoadUser(0L);
        willLoadEventType(0L);
        // willLoadEvent( 0L );
        filter.filter(null, i);
        super.verify();
    }

    // ~ Root / managed image
    // =========================================================================
    @Test
    public void test_root_managed_image_chmod_to_other_user() throws Exception {
        userImageChmod(_ROOT, _MANAGED, 2L);
        willLoadImage(managedImage());
        willLoadUser(2L);
        willLoadUser(0L);
        willLoadGroup(0L);
        willLoadEventType(0L);
        // willLoadEvent( 0L );
        filter.filter(null, i);
        super.verify();
    }

}
