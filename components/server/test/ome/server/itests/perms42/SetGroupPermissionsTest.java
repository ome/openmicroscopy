/*
 *   $Id$
 *
 *   Copyright 2010-2014 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.server.itests.perms42;

import ome.conditions.SecurityViolation;
import ome.model.core.Image;
import ome.model.internal.Permissions;
import ome.model.meta.ExperimenterGroup;

import org.testng.annotations.Test;

/**
 * Test of the group permission setting in Beta4.2
 *
 * @since Beta-4.2.0
 *
 */
@Test(groups = "ticket:1434")
public class SetGroupPermissionsTest extends PermissionsTest {

    @Test
    public void testOnGroupCreationPermissionsAreSet() throws Exception {
        String name = "privateGroup-" + uuid();
        ExperimenterGroup privateGroup = new ExperimenterGroup();
        privateGroup.setName(name);
        privateGroup.setLdap(false);
        privateGroup.getDetails().setPermissions(Permissions.USER_PRIVATE);
        long gid = iAdmin.createGroup(privateGroup);
        privateGroup = iAdmin.getGroup(gid);
        Permissions perms = privateGroup.getDetails().getPermissions();
        assertTrue(perms + "", Permissions.USER_PRIVATE.identical(perms));
    }

    @Test
    public void testGroupsCanBeMadeShared() throws Exception {
        setup(Permissions.USER_PRIVATE);
        fixture.make_leader();

        Image image = fixture.saveImage();
        iAdmin.changePermissions(fixture.group(), Permissions.GROUP_PRIVATE);
        assertShared(fixture.group());
        assertShared(image);
    }

    @Test
    public void testGroupsCanBeMadeSharedViaUpdateGroup() throws Exception {
        setup(Permissions.USER_PRIVATE);
        Image image = fixture.saveImage();
        ExperimenterGroup group = fixture.group();
        group.getDetails().setPermissions(Permissions.COLLAB_READONLY);
        loginRootKeepGroup();
        iAdmin.updateGroup(group);
        assertShared(fixture.group());
        assertShared(image);
    }

    @Test
    public void testGroupsCanBeMadeSharedWriteable() throws Exception {
        setup(Permissions.PRIVATE);
        fixture.make_leader();

        Image image = fixture.saveImage();

        iAdmin.changePermissions(fixture.group(), Permissions.COLLAB_READLINK);
        assertSharedAndWritable(fixture.group());
        assertSharedAndWritable(image);
    }

    @Test
    public void testGroupsCanBeMadePublic() throws Exception {
        setup(Permissions.USER_PRIVATE);
        fixture.make_leader();

        Image image = fixture.saveImage();
        assertPrivate(image);
        assertPrivate(fixture.group());
        iAdmin.changePermissions(fixture.group(), Permissions.PUBLIC);
        assertPublic(fixture.group());
        assertPublic(image);
    }

    @Test
    public void testGroupsCanBeMadePrivateAgain() throws Exception {
        setup(Permissions.USER_PRIVATE);
        fixture.make_leader();

        Image image = fixture.saveImage();
        iAdmin.changePermissions(fixture.group(), Permissions.PUBLIC);
        assertPublic(image);
        assertPublic(fixture.group());
        iAdmin.changePermissions(fixture.group(), Permissions.USER_PRIVATE);
        fail("NYI");
        assertPrivate(image);
        assertPrivate(fixture.group());
    }

    @Test
    public void testEventLogExistsOfGroupChange() throws Exception {
        fail();
    }


    /**
     * A newly created group should have private permissions. If we try to just
     * chmod the group it should fail with a security violation since various
     * things have to happen (e.g. changing all objects)
     */
    @Test
    public void testBackdoorChangingOfGroupPermissionsDisallowed() throws Exception {

        setup(Permissions.USER_PRIVATE);
        assertPrivate(fixture.group());

        try {
            loginRoot();
            ExperimenterGroup g = fixture.group();
            g.getDetails().setPermissions(Permissions.PUBLIC);
            g = iUpdate.saveAndReturnObject(g);
            fail("ticket:1434");
        } catch (SecurityViolation sv) {
            // good
        }

        // This is the supported way of changing your group's permissions
        iAdmin.changePermissions(fixture.group(), Permissions.PUBLIC);
        assertPublic(fixture.group());
    }

    @Test
    public void testLoweringPermissionsDoesntBreakConsistency() throws Exception {
        fail();
    }

    @Test
    public void testCopiedFromPython() throws Exception {
        
        loginRoot();
        
        String uuid = iAdmin.getEventContext().getCurrentSessionUuid();
        ExperimenterGroup new_gr1 = new ExperimenterGroup();
        new_gr1.setName("group1_"+uuid);
        new_gr1.setLdap(false);
        Permissions p = Permissions.COLLAB_READLINK;
        new_gr1.getDetails().setPermissions(p);
        long g1_id = iAdmin.createGroup(new_gr1);
            
        //  update name of group1
        ExperimenterGroup gr1 = iAdmin.getGroup(g1_id);
        assertEquals("rwrw--", gr1.getDetails().getPermissions().toString());
        String new_name = "changed_name_group1_" + uuid;
        gr1.setName(new_name);
        iAdmin.updateGroup(gr1);
        ExperimenterGroup gr1_u = iAdmin.getGroup(g1_id);
        assertEquals(new_name, gr1_u.getName());
    }
}
