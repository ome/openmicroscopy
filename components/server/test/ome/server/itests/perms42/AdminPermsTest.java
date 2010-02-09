/*
 *   $Id$
 *
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.server.itests.perms42;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import ome.conditions.SecurityViolation;
import ome.model.internal.Permissions;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.model.meta.GroupExperimenterMap;

import org.testng.annotations.Test;


/**
 * Tests the new functionality added to IAdmin as a part of #1434
 *
 * @since Beta-4.2.0
 *
 */
@Test(groups = "ticket:1434")
public class AdminPermsTest extends PermissionsTest {


    // METHODS:
    // --------------------
    // updateExperimenter
    // updateExperimenterWithPassword
    // updateGroup
    // createUser
    // createExperimenter
    // createExperimenterWithPassword
    // removeGroups
    // setGroupOwner
    // unsetGroupOwner
    // addGroupOwners
    // removeGroupOwners


    @Test
    public void testDeleteExperimenter() throws Exception {
        setup(Permissions.PRIVATE);
        Experimenter e2 = loginNewUserInOtherUsersGroup(fixture.user);
        fixture.log_in();
        try {
            iAdmin.deleteExperimenter(e2);
            fail("secvio");
        } catch (SecurityViolation sv) {
            // good;
        }
        fixture.make_leader();
        iAdmin.deleteExperimenter(e2);
    }

    @Test
    public void testDeleteGroup() throws Exception {
        setup(Permissions.PRIVATE);
        try {
            iAdmin.deleteGroup(fixture.group());
            fail("secvio");
        } catch (SecurityViolation sv) {
            // good;
        }
        fixture.make_leader();
        iAdmin.deleteGroup(fixture.group());
    }

    @Test
    public void testChangeUserPassword() throws Exception {
        setup(Permissions.PRIVATE);
        Experimenter member = loginNewUserInOtherUsersGroup(fixture.user);
        iAdmin.changePassword("UserChangesPassword");
        fixture.log_in();
        try {
            iAdmin.changeUserPassword(member.getOmeName(), "PIChangesPass");
            fail("secvio");
        } catch (SecurityViolation sv) {
            // good;
        }
        fixture.make_leader();
        iAdmin.changeUserPassword(member.getOmeName(), "PIChangesPass");
    }

    @Test
    public void testAddRemoveGroupOwners() throws Exception {
        setup(Permissions.PRIVATE);

        assertMembers(fixture.group(), fixture.user.getId());
        assertLeaders(fixture.group());
        loginRoot();
        iAdmin.addGroupOwners(fixture.group(), fixture.user);
        assertMembers(fixture.group(), fixture.user.getId());
        assertLeaders(fixture.group(), fixture.user.getId());
        
        // Now add another user
        Experimenter e2 = loginNewUser();
        loginRoot();
        iAdmin.addGroupOwners(fixture.group(), e2);
        assertMembers(fixture.group(), fixture.user.getId(), e2.getId());
        assertLeaders(fixture.group(), fixture.user.getId(), e2.getId());
        
        // Now remove that new user
        iAdmin.removeGroupOwners(fixture.group(), e2);
        assertMembers(fixture.group(), fixture.user.getId(), e2.getId());
        assertLeaders(fixture.group(), fixture.user.getId());
    }

    @Test
    public void testCreateUserMakeOwnerAndRemoveAsOwner() throws Exception {
        setup(Permissions.PRIVATE);

        assertMembers(fixture.group(), fixture.user.getId());
        assertLeaders(fixture.group());
        loginRoot();
        iAdmin.addGroupOwners(fixture.group(), fixture.user);
        assertMembers(fixture.group(), fixture.user.getId());
        assertLeaders(fixture.group(), fixture.user.getId());

        // Now add another user
        Experimenter e2 = new Experimenter();
        e2.setOmeName(uuid());
        e2.setFirstName(uuid());
        e2.setLastName(uuid());
        fixture.log_in();
        long uid = iAdmin.createExperimenter(e2, fixture.group());
        e2.setId(uid);

        assertMembers(fixture.group(), fixture.user.getId(), e2.getId());
        iAdmin.addGroupOwners(fixture.group(), e2);
        assertLeaders(fixture.group(), fixture.user.getId(), e2.getId());

        // Now remove that new user
        iAdmin.removeGroupOwners(fixture.group(), e2);
        assertMembers(fixture.group(), fixture.user.getId(), e2.getId());
        assertLeaders(fixture.group(), fixture.user.getId());
    }

    // Helpers
    // =========================================================================

    protected void assertMembers(ExperimenterGroup group, Long...members) {
        Set<Long> toCheck = new HashSet<Long>(Arrays.asList(members));
        Set<Long> thatHas = new HashSet<Long>();
        for (GroupExperimenterMap map : group.unmodifiableGroupExperimenterMap()) {
            thatHas.add(map.child().getId());
        }
        assertEqualSets(thatHas, toCheck);
    }
    
    protected void assertLeaders(ExperimenterGroup group, Long...members) {
        Set<Long> toCheck = new HashSet<Long>(Arrays.asList(members));
        Set<Long> thatHas = new HashSet<Long>();
        for (GroupExperimenterMap map : group.unmodifiableGroupExperimenterMap()) {
            if (map.getOwner()) {
                thatHas.add(map.child().getId());
            }
        }
        assertEqualSets(thatHas, toCheck);
    }

    protected void assertEqualSets(Set<Long> thatHas, Set<Long> toCheck) {
        Set<Long> missing = new HashSet<Long>();
        missing.addAll(toCheck);
        missing.removeAll(thatHas);
        
        Set<Long> extra = new HashSet<Long>();
        extra.addAll(thatHas);
        extra.removeAll(toCheck);
        
        assertTrue(String.format("Missing:%s Extra: %s", missing, extra),
                missing.size() ==  0 && extra.size() == 0);
    }

}
