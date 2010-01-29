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
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.server.itests.AbstractManagedContextTest;

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
        ExperimenterGroup group1;
        {
            user = loginNewUser();
            groupName = uuid();
            group1 = new ExperimenterGroup(groupName);
            group1 = new ExperimenterGroup(iAdmin.createGroup(group1), false);
            iAdmin.addGroups(user, group1);
        }
    }

    @Test
    public void testUserInTwoGroupsCantMixWithLink() throws Exception {
        Fixture fixture = new Fixture();
        Image i = new_Image("ticket:1434");
        i = iUpdate.saveAndReturnObject(i);

        login(fixture.user.getOmeName(), fixture.groupName, "Test");
        Dataset d = new Dataset("ticket:1434");
        d.linkImage(i);
        try {
            d = iUpdate.saveAndReturnObject(d);
            fail("Mixed group should not allowed!");
        } catch (SecurityViolation sv) {
            // good
        }

    }

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
