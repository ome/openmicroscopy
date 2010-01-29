/*
 *   $Id$
 *
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
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
        privateGroup.getDetails().setPermissions(Permissions.USER_PRIVATE);
        long gid = iAdmin.createGroup(privateGroup);
        privateGroup = iAdmin.getGroup(gid);
        Permissions perms = privateGroup.getDetails().getPermissions();
        assertTrue(perms + "", Permissions.USER_PRIVATE.identical(perms));
    }

    @Test
    public void testGroupsCanBeMadeShared() throws Exception {
        setup(Permissions.USER_PRIVATE);
        Image image = fixture.saveImage();
        iAdmin.changePermissions(fixture.group(), Permissions.GROUP_PRIVATE);
        checkShared(fixture.group());
        checkShared(image);
    }

    @Test
    public void testGroupsCanBeMadePublic() throws Exception {
        setup(Permissions.USER_PRIVATE);
        Image image = fixture.saveImage();
        checkPrivate(image);
        checkPrivate(fixture.group());
        iAdmin.changePermissions(fixture.group(), Permissions.PUBLIC);
        checkPublic(fixture.group());
        checkPublic(image);
        fail("This should be made disallowed, because what context would one use?");
    }

    @Test
    public void testGroupsCanBeMadePrivateAgain() throws Exception {
        setup(Permissions.USER_PRIVATE);
        Image image = fixture.saveImage();
        iAdmin.changePermissions(fixture.group(), Permissions.PUBLIC);
        checkPublic(image);
        checkPublic(fixture.group());
        iAdmin.changePermissions(fixture.group(), Permissions.USER_PRIVATE);
        fail("NYI");
        checkPrivate(image);
        checkPrivate(fixture.group());

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
        checkPrivate(fixture.group());

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
        checkPublic(fixture.group());
    }

    @Test
    public void testLoweringPermissionsDoesntBreakConsistency() throws Exception {
        fail();
    }

}
