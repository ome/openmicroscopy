/*
 *   $Id$
 *
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.server.itests.perms42;

import ome.conditions.GroupSecurityViolation;
import ome.conditions.PermissionMismatchGroupSecurityViolation;
import ome.model.core.Image;
import ome.model.internal.Permissions;

import org.testng.annotations.Test;

/**
 * Test of setting permissions on non-group objects
 *
 * @since Beta-4.2.0
 *
 */
@Test(groups = "ticket:1776")
public class SetOtherPermissionsTest extends PermissionsTest {

    @Test(expectedExceptions = PermissionMismatchGroupSecurityViolation.class)
    public void testUserTriesToCreateObjectWithWorldRead() throws Exception {
        setup(Permissions.COLLAB_READONLY);
        Image i = fixture.saveImage(Permissions.PUBLIC);
        fail("This shouldn't be allowed");
    }

    @Test
    public void testUserTriesToRemoveReadForSharedGroup() throws Exception {
        setup(Permissions.COLLAB_READONLY);
        Image i = fixture.saveImage();
        assertShared(i);
        assertCantSetPermissionsTo(i, Permissions.PRIVATE);
        assertShared(i);
    }

    @Test
    public void testUserTriesToRemoveGroupReadForSharedWorld() throws Exception {
        setup(Permissions.PUBLIC);
        Image i = fixture.saveImage();
        assertPublic(i);
        assertCantSetPermissionsTo(i, Permissions.PRIVATE);
        assertPublic(i);
    }

    @Test
    public void testUserTriesToRemoveWorldReadForSharedWorld() throws Exception {
        setup(Permissions.PUBLIC);
        Image i = fixture.saveImage();
        assertPublic(i);
        assertCantSetPermissionsTo(i, Permissions.COLLAB_READONLY);
        assertPublic(i);
    }

    @Test
    public void testUserTriesToAddReadToPrivateGroup() throws Exception {
        setup(Permissions.PRIVATE);
        Image i = fixture.saveImage();
        assertPrivate(i);
        assertCantSetPermissionsTo(i, Permissions.COLLAB_READONLY);
        assertPrivate(i);
    }

    // Helpers
    // =========================================================================

    private void assertCantSetPermissionsTo(Image i, Permissions p) {
        i.getDetails().setPermissions(p);
        try {
            iUpdate.saveObject(i);
            fail("ticket:1776 - iupdate");
        } catch (GroupSecurityViolation gsv) {
            // ok.
        }
        try {
            iAdmin.changePermissions(i, p);
            fail("ticket:1776 - iadmin");
        } catch (GroupSecurityViolation gsv) {
            // ok
        }
    }

}
