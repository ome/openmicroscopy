/*
 *   $Id$
 *
 *   Copyright 2010-2014 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.server.itests.perms42;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ome.conditions.SecurityViolation;
import ome.model.enums.Format;
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

    @Test
    public void testUpdateSelf() {
        setup(Permissions.PRIVATE);
        fixture.user.setEmail(uuid());
        iAdmin.updateSelf(fixture.user);
    }

    @Test
    public void testUpdateExperimenter() {
        setup(Permissions.PRIVATE);
        Experimenter other = loginNewUserInOtherUsersGroup(fixture.user);
        fixture.log_in();

        try {
            other.setEmail(uuid());
            iAdmin.updateExperimenter(other);
            fail("sec-vio");
        } catch (SecurityViolation sv) {
            // goood
        }

        try {
            other.setEmail(uuid());
            iAdmin.updateExperimenterWithPassword(other, uuid());
            fail("sec-vio");
        } catch (SecurityViolation sv) {
            // goood
        }

        fixture.make_leader();

        other.setEmail(uuid());
        iAdmin.updateExperimenter(other);

        other.setEmail(uuid());
        iAdmin.updateExperimenterWithPassword(other, uuid());

    }

    @Test
    public void testUpdateGroup() {
        setup(Permissions.PRIVATE);
        fixture.make_leader();
        ExperimenterGroup g = fixture.group();
        g.setName(uuid());
        g.setLdap(false);
        // g.getDetails().setPermissions(Permissions.SHARED);
        iAdmin.updateGroup(g);
    }

    @Test
    public void testCreateUser() {

        // Non-member group to be used as dummy
        loginRoot();
        ExperimenterGroup g2 = newGroup();

        setup(Permissions.PRIVATE);

        Experimenter e = uuidUser();
        try {
            iAdmin.createUser(e, g2.getName());
            fail("not in my group");
        } catch (SecurityViolation sv) {
            // good;
        }
        try {
            iAdmin.createUser(e, fixture.groupName);
            fail("my group, i'm not leader");
        } catch (SecurityViolation sv) {
            // good;
        }

        fixture.make_leader();

        try {
            iAdmin.createUser(e, g2.getName());
            fail("still not in my group even thought i'm leader");
        } catch (SecurityViolation sv) {
            // good;
        }

        iAdmin.createUser(e, fixture.groupName);

    }

    @Test
    public void testCreateUserAsOwner() {

        loginRoot();
        ExperimenterGroup g2 = newGroup();

        setup(Permissions.PRIVATE);

        Experimenter e = uuidUser();

        try {
            iAdmin.createUser(e, g2.getName());
            fail("not in my group");
        } catch (SecurityViolation sv) {
            // good;
        }
        try {
            iAdmin.createUser(e, fixture.groupName);
            fail("my group, i'm not leader");
        } catch (SecurityViolation sv) {
            // good;
        }

        fixture.make_leader();

        try {
            iAdmin.createUser(e, g2.getName());
            fail("still not in my group even thought i'm leader");
        } catch (SecurityViolation sv) {
            // good;
        }

        iAdmin.createUser(e, fixture.groupName);

    }


    @Test
    public void testCreateExperimenterWithPassword() {

        // Non-member group to be used as dummy
        loginRoot();
        ExperimenterGroup g2 = newGroup();

        setup(Permissions.PRIVATE);

        Experimenter e = uuidUser();
        try {
            iAdmin.createExperimenterWithPassword(e, "pass", g2);
            fail("not in my group");
        } catch (SecurityViolation sv) {
            // good;
        }
        try {
            iAdmin.createExperimenterWithPassword(e, "pass", fixture.group());
            fail("my group, i'm not leader");
        } catch (SecurityViolation sv) {
            // good;
        }

        fixture.make_leader();

        try {
            iAdmin.createExperimenterWithPassword(e, "pass", g2);
            fail("still not in my group even thought i'm leader");
        } catch (SecurityViolation sv) {
            // good;
        }

        iAdmin.createExperimenterWithPassword(e, "pass", fixture.group()); // Yes.

    }

    @Test
    public void testCreateExperimenter() {

        // Non-member group to be used as dummy
        loginRoot();
        ExperimenterGroup g2 = newGroup();

        setup(Permissions.PRIVATE);

        Experimenter e = uuidUser();
        try {
            iAdmin.createExperimenter(e, g2);
            fail("not in my group");
        } catch (SecurityViolation sv) {
            // good;
        }
        try {
            iAdmin.createExperimenter(e, fixture.group());
            fail("my group, i'm not leader");
        } catch (SecurityViolation sv) {
            // good;
        }

        fixture.make_leader();

        try {
            iAdmin.createExperimenter(e, g2);
            fail("still not in my group even thought i'm leader");
        } catch (SecurityViolation sv) {
            // good;
        }

        iAdmin.createExperimenter(e, fixture.group()); // Yes.

    }


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
    public void testCreateUserMakeOwnerAndRemoveAsOwnerWithAddRemove() throws Exception {
        setup(Permissions.PRIVATE);

        assertMembers(fixture.group(), fixture.user.getId());
        assertLeaders(fixture.group());
        try { // Try to add self
            iAdmin.addGroupOwners(fixture.group(), fixture.user);
            fail("sec-vio");
        } catch (SecurityViolation sv) {
            // good
        }
        loginRoot();
        iAdmin.addGroupOwners(fixture.group(), fixture.user);
        assertMembers(fixture.group(), fixture.user.getId());
        assertLeaders(fixture.group(), fixture.user.getId());

        Experimenter e2 = uuidUser();
        fixture.log_in();
        long uid = iAdmin.createExperimenter(e2, fixture.group());
        e2.setId(uid);

        assertMembers(fixture.group(), fixture.user.getId(), e2.getId());
        iAdmin.addGroupOwners(fixture.group(), e2);
        assertLeaders(fixture.group(), fixture.user.getId(), e2.getId());

        // 2. Now remove that new user
        iAdmin.removeGroupOwners(fixture.group(), e2);
        assertMembers(fixture.group(), fixture.user.getId(), e2.getId());
        assertLeaders(fixture.group(), fixture.user.getId());

        // Finally, the one owner removes his/herself (valid)
        iAdmin.removeGroupOwners(fixture.group(), fixture.user);
    }

    @Test
    public void testCreateUserMakeOwnerAndRemoveAsOwnerWithSetUnset() throws Exception {
        setup(Permissions.PRIVATE);

        assertMembers(fixture.group(), fixture.user.getId());
        assertLeaders(fixture.group());
        try { // Try to add self
            iAdmin.setGroupOwner(fixture.group(), fixture.user);
            fail("sec-vio");
        } catch (SecurityViolation sv) {
            // good
        }
        loginRoot();
        iAdmin.setGroupOwner(fixture.group(), fixture.user);
        assertMembers(fixture.group(), fixture.user.getId());
        assertLeaders(fixture.group(), fixture.user.getId());

        Experimenter e2 = uuidUser();
        fixture.log_in();
        long uid = iAdmin.createExperimenter(e2, fixture.group());
        e2.setId(uid);

        assertMembers(fixture.group(), fixture.user.getId(), e2.getId());
        iAdmin.setGroupOwner(fixture.group(), e2);
        assertLeaders(fixture.group(), fixture.user.getId(), e2.getId());

        // 2. Now remove that new user
        iAdmin.unsetGroupOwner(fixture.group(), e2);
        assertMembers(fixture.group(), fixture.user.getId(), e2.getId());
        assertLeaders(fixture.group(), fixture.user.getId());

        // Finally, the one owner removes his/herself (valid)
        iAdmin.unsetGroupOwner(fixture.group(), fixture.user);
    }

    @Test(groups = "ticket:1811")
    public void testTicket1811() throws Exception {
        setup(Permissions.PRIVATE);
        assertPi(false);
        fixture.make_leader();
        assertPi(true);
        loginRoot();
        iAdmin.addGroups(fixture.user, fixture.group());
        assertPi(true);
    }

    @Test(groups = "ticket:1822")
    public void testTicket1822() throws Exception {

        loginRoot();
        setup(Permissions.PRIVATE);
        fixture.make_leader();

        ExperimenterGroup group1 = fixture.group();
        Experimenter user1 = fixture.user;
        Experimenter user2 = loginNewUserInOtherUsersGroup(user1);

        assertNotOwner(group1, user2);

        fixture.log_in();
        iAdmin.setGroupOwner(group1, user2);

        assertOwner(group1, user2);

    }

    /**
     * Create a system type while logged into a non-system group.
     */
    @Test(groups = "ticket:1779")
    public void testTicket1779() {

        // Create a group with different permissions from root
        loginRoot();
        Permissions sysPerms = iAdmin.getEventContext().getCurrentGroupPermissions();
        ExperimenterGroup g = newGroup(Permissions.COLLAB_READLINK);
        assertFalse(sysPerms.identical(g.getDetails().getPermissions()));

        // Now login to that group and check permissions
        loginUser("root", g.getName());
        Permissions grpPerms = iAdmin.getEventContext().getCurrentGroupPermissions();
        assertFalse(sysPerms.identical(grpPerms));
        assertTrue(Permissions.COLLAB_READLINK.identical(grpPerms));

        // Now create a group via IUpdate and see what permissions it gets.
        ExperimenterGroup g2 = new ExperimenterGroup();
        g2.setName(uuid());
        g2.setLdap(false);
        g2 = iUpdate.saveAndReturnObject(g2);
        assertTrue(grpPerms.identical(g.getDetails().getPermissions()));
        //
        // assertTrue(sysPerms.identical(g.getDetails().getPermissions()));
        //
        // This is the question: should the group have sys or grp permissions?
        // System types don't have a group, otherwise it would make sense for
        // them to have "grp". However, system types are always readable, so
        // the only critical object at the moment is group as shown here.
        // If the current permissions of the group is not what you want for the
        // system type, then manually set it as an admin!
    }

    private void assertNotOwner(ExperimenterGroup group, Experimenter user) {
        List<Long> leaderOf = iAdmin.getLeaderOfGroupIds(user);
        assertFalse(leaderOf.contains(group.getId()));
    }

    private void assertOwner(ExperimenterGroup group, Experimenter user) {
        List<Long> leaderOf = iAdmin.getLeaderOfGroupIds(user);
        assertTrue(leaderOf.contains(group.getId()));
    }

    private void assertPi(boolean isPi) {
        assertEquals(isPi, iAdmin.getLeaderOfGroupIds(fixture.user)
                .contains(fixture.group().getId()));
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


    private Experimenter uuidUser() {
        Experimenter e = new Experimenter();
        e.setOmeName(uuid());
        e.setFirstName(uuid());
        e.setLastName(uuid());
        e.setLdap(false);
        return e;
    }

    private ExperimenterGroup newGroup() {
        return newGroup(null);
    }

    private ExperimenterGroup newGroup(Permissions p) {
        ExperimenterGroup g2 = new ExperimenterGroup();
        g2.setName(uuid());
        g2.setLdap(false);
        g2.getDetails().setPermissions(p);
        g2 = iAdmin.getGroup(iAdmin.createGroup(g2));
        return g2;
    }

}
