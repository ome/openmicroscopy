/*
 *   $Id$
 *
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.server.itests.perms42;

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

    protected class Fixture {
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

    protected Fixture fixture;

    @BeforeMethod
    protected void setupFixture() {
        fixture = new Fixture();
    }

    @AfterMethod
    protected void teardownFixture() {
        fixture = null;
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
