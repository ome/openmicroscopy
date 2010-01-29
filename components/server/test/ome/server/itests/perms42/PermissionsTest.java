/*
 *   $Id$
 *
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.server.itests.perms42;

import ome.conditions.SecurityViolation;
import ome.model.containers.Dataset;
import ome.model.core.Image;
import ome.model.internal.Permissions;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.server.itests.AbstractManagedContextTest;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Test of the re-enabled group permissions in Beta4.2
 *
 * @since Beta-4.2.0
 *
 */
@Test(groups = "ticket:1434")
public class PermissionsTest extends AbstractManagedContextTest {

    class Fixture {
        Experimenter user;
        String groupName;
        ExperimenterGroup group;
        Image image;
        {
            user = loginNewUser();
            groupName = uuid();
            group = new ExperimenterGroup(groupName);
            iAdmin.createGroup(group);
            group = iAdmin.lookupGroup(groupName);
            iAdmin.addGroups(user, group);
            image = new_Image("ticket:1434");
        }

        Image saveImage() {
            return iUpdate.saveAndReturnObject(image);
        }

        void log_in() {
            login(user.getOmeName(), groupName, "Test");
        }
    }

    Fixture fixture;

    @BeforeMethod
    void setupFixture() {
        fixture = new Fixture();
    }

    @AfterMethod
    void teardownFixture() {
        fixture = null;
    }

    //
    // Controlling group permission settings
    //

    @Test
    public void testOnGroupCreationPermissionsAreSet() throws Exception {
        fail();
    }

    @Test
    public void testGroupsCanBeMadeShared() throws Exception {
        fail();
    }

    @Test
    public void testGroupsCanBeMadePublic() throws Exception {
        fail();
    }

    //
    // Configurable default permissions removed in favor of using group
    //

    @Test
    public void testObjectCreatedWithGroupPermissions() throws Exception {
        fixture.log_in();
        Image image = fixture.saveImage();
        Permissions groupPermissions = fixture.group.getDetails()
                .getPermissions();
        Permissions imagePermissions = image.getDetails().getPermissions();
        assertTrue(groupPermissions.identical(imagePermissions));

    }

    @Test
    public void testObjectCreatedWithGroupPermissionsMinusUmask()
            throws Exception {
        fail();
    }

    //
    // Guarantee consistent graphs on read
    //

    //
    // Guarantee consistent graphs on write
    //

    @Test
    public void testUserInTwoGroupsCantMixWithLink() throws Exception {

        // Create an image as root
        Image i = fixture.saveImage();

        // Create an image as fixture user
        fixture.log_in();
        Dataset d = new Dataset("ticket:1434");
        d.linkImage(i);
        try {
            d = iUpdate.saveAndReturnObject(d);
            fail("Mixed group should not allowed!");
        } catch (SecurityViolation sv) {
            // good
        }

    }

    //
    //
    //

    @Test
    public void testUserInDiffGroupCantSeeObjects() throws Exception {
        fail();
    }

    @Test
    public void testUserCantReadOwnFromAnotherContext() throws Exception {
        fail();
    }

    @Test
    public void testPICanStillDoAnythingInGroup() throws Exception {
        fail();
    }

    @Test
    public void testAdminCantChangePermsOfGroupByAccident() throws Exception {
        fail();
    }

    @Test
    public void testOwnerCanCallChgrpOnCoherentGraph() throws Exception {
        fail();
    }

    @Test
    public void testOwnerCantCallChgrpForAnotherOwner() throws Exception {
        fail();
    }

    @Test
    public void testChgrpLeavesADeletionRecordWhereNecessary() throws Exception {
        fail();
    }

    @Test
    public void testChgrpCanAlternativelyStoreByValue() throws Exception {
        fail();
    }

    @Test
    public void testTriggersPreventMixingGraphs() throws Exception {
        fail();
    }

    @Test
    public void testWorldPermissionsAreKeptCoherent() throws Exception {
        fail();
    }

    @Test
    public void testUserCanSafelyStoreInfoInAPrivateGroup() throws Exception {
        fail();
    }

    @Test
    public void testPICanMoveAGroupToShared() throws Exception {
        fail();
    }

    @Test
    public void testUserWillBeInformedIfGroupIsShared() throws Exception {
        fail();
    }

}
