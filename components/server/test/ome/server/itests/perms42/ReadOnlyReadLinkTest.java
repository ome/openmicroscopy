/*
 *   $Id$
 *
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.server.itests.perms42;

import ome.conditions.GroupSecurityViolation;
import ome.conditions.SecurityViolation;
import ome.model.annotations.TagAnnotation;
import ome.model.core.Image;
import ome.model.internal.Permissions;
import ome.model.meta.Experimenter;

import org.testng.annotations.Test;

/**
 * Tests the new interpretation of groups' READ and WRITE permissions as defined
 * in ticket 1992. rwr--- is now intended to mean "read-only" (i.e. no linking)
 * while rwrw---- means "read-link" (i.e. linking but no editing of data). In
 * order to have the previous rwrw-- login (READ-WRITE), add a user as a group
 * owner.
 *
 * @since Beta-4.2.0
 *
 */
@Test(groups = { "ticket:1434", "ticket:1992" })
public class ReadOnlyReadLinkTest extends PermissionsTest {

    // READ-ONLY

    @Test
    public void testROOwnerCanLink() {
        setup(Permissions.COLLAB_READONLY);
        tryLink(fixture.user);
    }

    @Test
    public void testROOwnerCanEdit() {
        setup(Permissions.COLLAB_READONLY);
        tryEdit(fixture.user);
    }

    @Test(expectedExceptions = GroupSecurityViolation.class)
    public void testROPICantLink() {
        setup(Permissions.COLLAB_READONLY);
        tryLink(pi());
    }

    @Test
    public void testROPICanEdit() {
        setup(Permissions.COLLAB_READONLY);
        tryEdit(pi());
    }

    @Test(expectedExceptions = GroupSecurityViolation.class)
    public void testROAdminCantLink() {
        setup(Permissions.COLLAB_READONLY);
        tryLink(admin());
    }

    @Test
    public void testROAdminCanEdit() {
        setup(Permissions.COLLAB_READONLY);
        tryEdit(admin());
    }

    @Test(expectedExceptions = GroupSecurityViolation.class)
    public void testROMembersCantLink() {
        setup(Permissions.COLLAB_READONLY);
        tryLink(member());
    }

    @Test(expectedExceptions = SecurityViolation.class)
    public void testROMembersCantEdit() {
        setup(Permissions.COLLAB_READONLY);
        tryEdit(member());
    }

    // READ-LINK

    @Test
    public void testRLOwnerCanLink() {
        setup(Permissions.COLLAB_READLINK);
        tryLink(fixture.user);
    }

    @Test
    public void testRLOwnerCanEdit() {
        setup(Permissions.COLLAB_READLINK);
        tryEdit(fixture.user);
    }

    @Test
    public void testRLPICanLink() {
        setup(Permissions.COLLAB_READLINK);
        tryLink(pi());
    }

    @Test
    public void testRLPICanEdit() {
        setup(Permissions.COLLAB_READLINK);
        tryEdit(pi());
    }

    @Test
    public void testRLAdminCanLink() {
        setup(Permissions.COLLAB_READLINK);
        tryLink(admin());
    }

    @Test
    public void testRLAdminCanEdit() {
        setup(Permissions.COLLAB_READLINK);
        tryEdit(admin());
    }

    @Test
    public void testRLMembersCanLink() {
        setup(Permissions.COLLAB_READLINK);
        tryLink(member());
    }

    @Test(expectedExceptions = SecurityViolation.class)
    public void testRLMembersCantEdit() {
        setup(Permissions.COLLAB_READLINK);
        tryEdit(member());
    }

    // Helpers
    // =========================================================================

    private Experimenter member() {
        Experimenter e = loginNewUserInOtherUsersGroup(fixture.user);
        return e;
    }

    private Experimenter pi() {
        Experimenter e = member();
        loginRoot();
        iAdmin.addGroupOwners(fixture.group(), e);
        return e;
    }

    private Experimenter admin() {
        return iAdmin.getExperimenter(0);
    }

    private void tryLink(Experimenter e) {
        fixture.log_in();
        Image i = fixture.saveImage();

        // Now other user tries to link
        loginUser(e.getOmeName(), fixture.groupName);
        i.linkAnnotation(new TagAnnotation());
        iUpdate.saveAndReturnObject(i);
    }

    private void tryEdit(Experimenter e) {
        fixture.log_in();
        Image i = fixture.saveImage();

        // Now other user tries to edit
        loginUser(e.getOmeName(), fixture.groupName);
        i.setName(uuid());
        iUpdate.saveAndReturnObject(i);
    }

}
