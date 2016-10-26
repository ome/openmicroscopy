/*
 * Copyright (C) 2016 University of Dundee & Open Microscopy Environment.
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package integration;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import omero.SecurityViolation;
import omero.ServerError;
import omero.gateway.util.Requests;
import omero.model.AdminPrivilege;
import omero.model.Experimenter;
import omero.model.ExperimenterGroup;
import omero.model.ExperimenterGroupI;
import omero.model.ExperimenterI;
import omero.model.Folder;
import omero.model.GroupExperimenterMapI;
import omero.model.IObject;
import omero.model.NamedValue;
import omero.model.Session;
import omero.model.enums.AdminPrivilegeChgrp;
import omero.model.enums.AdminPrivilegeChown;
import omero.model.enums.AdminPrivilegeModifyUser;
import omero.model.enums.AdminPrivilegeSudo;
import omero.model.enums.AdminPrivilegeWriteOwned;
import omero.sys.EventContext;
import omero.sys.Principal;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

/**
 * Tests the effectiveness of light administrator privileges.
 * @author m.t.b.carroll@dundee.ac.uk
 * @since 5.3.0
 */
public class LightAdminPrivilegesTest extends AbstractServerTest {

    private ImmutableSet<AdminPrivilege> allPrivileges = null;
    private ExperimenterGroup systemGroup = null;

    @BeforeClass
    public void populateAllPrivileges() throws ServerError {
        final ImmutableSet.Builder<AdminPrivilege> privileges = ImmutableSet.builder();
        for (final IObject privilege : factory.getTypesService().allEnumerations("AdminPrivilege")) {
            privileges.add((AdminPrivilege) privilege);
        }
        allPrivileges = privileges.build();
    }

    @BeforeClass
    public void populateSystemGroup() throws ServerError {
        systemGroup = (ExperimenterGroup) iQuery.findByString("ExperimenterGroup", "name", SYSTEM_GROUP);
    }

    /**
     * Create a light administrator, possibly without a specific privilege, and log in as them.
     * @param isAdmin if the user should be a member of the <tt>system</tt> group
     * @param restriction the privilege that the user should not have, or {@code null} if they should have all privileges
     * @return the new user's context
     * @throws Exception if the light administrator could not be created
     */
    private EventContext loginNewAdmin(boolean isAdmin, String restriction) throws Exception {
        final EventContext ctx = isAdmin ? newUserInGroup(systemGroup, false) : newUserAndGroup("rw----");
        if (restriction != null) {
            final List<AdminPrivilege> privileges = new ArrayList<>(allPrivileges);
            final Iterator<AdminPrivilege> privilegeIterator = privileges.iterator();
            while (!privilegeIterator.next().getValue().getValue().equals(restriction));
            privilegeIterator.remove();
            root.getSession().getAdminService().setAdminPrivileges(new ExperimenterI(ctx.userId, false), privileges);
            /* avoid old session as privileges are briefly cached */
            loginUser(ctx);
        }
        return ctx;
    }

    /**
     * Sudo to the given user.
     * @param target a user
     * @return context for a session owned by the given user
     * @throws Exception if the sudo could not be performed
     */
    private EventContext sudo(Experimenter target) throws Exception {
        if (!target.isLoaded()) {
            target = iAdmin.getExperimenter(target.getId().getValue());
        }
        final Principal principal = new Principal();
        principal.name = target.getOmeName().getValue();
        final Session session = factory.getSessionService().createSessionWithTimeout(principal, 100 * 1000);
        final omero.client client = newOmeroClient();
        final String sessionUUID = session.getUuid().getValue();
        client.createSession(sessionUUID, sessionUUID);
        return init(client);
    }

    /**
     * Create a light administrator, possibly without a specific privilege, and log in as them, possibly sudo'ing afterward.
     * @param isAdmin if the user should be a member of the <tt>system</tt> group
     * @param isSudo if the user should then sudo to be a member of the <tt>system</tt> group with all privileges
     * @param restriction the privilege that the user should not have, or {@code null} if they should have all privileges
     * @return the new user's context (may be a sudo session)
     * @throws Exception if the light administrator actor could not be set up as specified
     */
    private EventContext loginNewActor(boolean isAdmin, boolean isSudo, String restriction) throws Exception {
        final EventContext adminContext = loginNewAdmin(isAdmin, restriction);
        if (isSudo) {
            final EventContext fullAdminContext = loginNewAdmin(true, null);
            loginUser(adminContext);
            try {
                final EventContext sudoContext = sudo(new ExperimenterI(fullAdminContext.userId, false));
                Assert.assertTrue(isAdmin, "normal users cannot sudo");
                return sudoContext;
            } catch (SecurityViolation sv) {
                Assert.assertFalse(isAdmin, "admins can sudo");
                throw sv;
            }
        } else {
            return adminContext;
        }
    }

    /**
     * Test that users may modify other users only if they are a member of the <tt>system</tt> group and
     * have the <tt>ModifyUser</tt> privilege. Attempts creation of new user via {@link AbstractServerTest#iUpdate}.
     * @param isAdmin if to test a member of the <tt>system</tt> group
     * @param isRestricted if to test a user who does <em>not</em> have the <tt>ModifyUser</tt> privilege
     * @param isSudo if to test attempt to subvert privilege by sudo to an unrestricted member of the <tt>system</tt> group
     * @throws Exception unexpected
     */
    @Test(dataProvider = "light administrator privilege test cases")
    public void testModifyUserPrivilegeCreationViaUpdate(boolean isAdmin, boolean isRestricted, boolean isSudo) throws Exception {
        final boolean isExpectSuccess = isAdmin && !isRestricted;
        final long newGroupId = newUserAndGroup("rw----").groupId;
        loginNewActor(isAdmin, isSudo, isRestricted ? AdminPrivilegeModifyUser.value : null);
        final Experimenter newUser = new ExperimenterI();
        newUser.setOmeName(omero.rtypes.rstring(UUID.randomUUID().toString()));
        newUser.setFirstName(omero.rtypes.rstring("August"));
        newUser.setLastName(omero.rtypes.rstring("Köhler"));
        newUser.setLdap(omero.rtypes.rbool(false));
        GroupExperimenterMapI gem = new GroupExperimenterMapI();
        gem.setParent(new ExperimenterGroupI(newGroupId, false));
        gem.setChild(newUser);
        gem.setOwner(omero.rtypes.rbool(false));
        newUser.addGroupExperimenterMap(gem);
        gem  = new GroupExperimenterMapI();
        gem.setParent(new ExperimenterGroupI(iAdmin.getSecurityRoles().userGroupId, false));
        gem.setChild(newUser);
        gem.setOwner(omero.rtypes.rbool(false));
        newUser.addGroupExperimenterMap(gem);
        try {
            iUpdate.saveObject(newUser);
            Assert.assertTrue(isExpectSuccess);
        } catch (ServerError se) {
            Assert.assertFalse(isExpectSuccess);
        }
    }

    /**
     * Test that users may modify other users only if they are a member of the <tt>system</tt> group and
     * have the <tt>ModifyUser</tt> privilege. Attempts creation of new user via {@link AbstractServerTest#iAdmin}.
     * @param isAdmin if to test a member of the <tt>system</tt> group
     * @param isRestricted if to test a user who does <em>not</em> have the <tt>ModifyUser</tt> privilege
     * @param isSudo if to test attempt to subvert privilege by sudo to an unrestricted member of the <tt>system</tt> group
     * @throws Exception unexpected
     */
    @Test(dataProvider = "light administrator privilege test cases")
    public void testModifyUserPrivilegeCreationViaAdmin(boolean isAdmin, boolean isRestricted, boolean isSudo) throws Exception {
        final boolean isExpectSuccess = isAdmin && !isRestricted;
        final long newGroupId = newUserAndGroup("rw----").groupId;
        loginNewActor(isAdmin, isSudo, isRestricted ? AdminPrivilegeModifyUser.value : null);
        final Experimenter newUser = new ExperimenterI();
        newUser.setOmeName(omero.rtypes.rstring(UUID.randomUUID().toString()));
        newUser.setFirstName(omero.rtypes.rstring("August"));
        newUser.setLastName(omero.rtypes.rstring("Köhler"));
        newUser.setLdap(omero.rtypes.rbool(false));
        try {
            final long userGroupId = iAdmin.getSecurityRoles().userGroupId;
            final List<ExperimenterGroup> groups = ImmutableList.<ExperimenterGroup>of(new ExperimenterGroupI(userGroupId, false));
            iAdmin.createExperimenter(newUser, new ExperimenterGroupI(newGroupId, false), groups);
            Assert.assertTrue(isExpectSuccess);
        } catch (ServerError se) {
            Assert.assertFalse(isExpectSuccess);
        }
    }

    /**
     * Test that users may modify other users only if they are a member of the <tt>system</tt> group and
     * have the <tt>ModifyUser</tt> privilege. Attempts change of existing user via {@link AbstractServerTest#iUpdate}.
     * @param isAdmin if to test a member of the <tt>system</tt> group
     * @param isRestricted if to test a user who does <em>not</em> have the <tt>ModifyUser</tt> privilege
     * @param isSudo if to test attempt to subvert privilege by sudo to an unrestricted member of the <tt>system</tt> group
     * @throws Exception unexpected
     */
    @Test(dataProvider = "light administrator privilege test cases")
    public void testModifyUserPrivilegeEditingViaUpdate(boolean isAdmin, boolean isRestricted, boolean isSudo) throws Exception {
        final boolean isExpectSuccess = isAdmin && !isRestricted;
        final long newUserId = newUserAndGroup("rw----").userId;
        loginNewActor(isAdmin, isSudo, isRestricted ? AdminPrivilegeModifyUser.value : null);
        final Experimenter newUser = (Experimenter) iQuery.get("Experimenter", newUserId);
        newUser.setConfig(ImmutableList.of(new NamedValue("color", "green")));
        try {
            iUpdate.saveObject(newUser);
            Assert.assertTrue(isExpectSuccess);
        } catch (ServerError se) {
            Assert.assertFalse(isExpectSuccess);
        }
    }

    /**
     * Test that users may modify other users only if they are a member of the <tt>system</tt> group and
     * have the <tt>ModifyUser</tt> privilege. Attempts change of existing user via {@link AbstractServerTest#iAdmin}.
     * @param isAdmin if to test a member of the <tt>system</tt> group
     * @param isRestricted if to test a user who does <em>not</em> have the <tt>ModifyUser</tt> privilege
     * @param isSudo if to test attempt to subvert privilege by sudo to an unrestricted member of the <tt>system</tt> group
     * @throws Exception unexpected
     */
    @Test(dataProvider = "light administrator privilege test cases")
    public void testModifyUserPrivilegeEditingViaAdmin(boolean isAdmin, boolean isRestricted, boolean isSudo) throws Exception {
        final boolean isExpectSuccess = isAdmin && !isRestricted;
        final long newUserId = newUserAndGroup("rw----").userId;
        loginNewActor(isAdmin, isSudo, isRestricted ? AdminPrivilegeModifyUser.value : null);
        final Experimenter newUser = (Experimenter) iQuery.get("Experimenter", newUserId);
        newUser.setConfig(ImmutableList.of(new NamedValue("color", "green")));
        try {
            iAdmin.updateExperimenter(newUser);
            Assert.assertTrue(isExpectSuccess);
        } catch (ServerError se) {
            Assert.assertFalse(isExpectSuccess);
        }
    }

    /**
     * Test that a light administrator cannot sudo without the <tt>Sudo</tt> privilege.
     * @throws Exception unexpected
     */
    @Test
    public void testSudoPrivilege() throws Exception {
        final EventContext normalUser = newUserAndGroup("rw----");
        loginNewAdmin(true, AdminPrivilegeSudo.value);
        try {
            sudo(new ExperimenterI(normalUser.userId, false));
            Assert.fail("Sudo-restricted administrators cannot sudo.");
        } catch (SecurityViolation sv) {
            /* expected */
        }
    }

    /**
     * Test that users may write other users' data only if they are a member of the <tt>system</tt> group and
     * have the <tt>WriteOwned</tt> privilege. Attempts creation of another user's data.
     * @param isAdmin if to test a member of the <tt>system</tt> group
     * @param isRestricted if to test a user who does <em>not</em> have the <tt>ModifyUser</tt> privilege
     * @param isSudo if to test attempt to subvert privilege by sudo to an unrestricted member of the <tt>system</tt> group
     * @throws Exception unexpected
     */
    @Test(dataProvider = "light administrator privilege test cases")
    public void testWriteOwnedPrivilegeCreation(boolean isAdmin, boolean isRestricted, boolean isSudo) throws Exception {
        final boolean isExpectSuccess = isAdmin && !isRestricted;
        final EventContext normalUser = newUserAndGroup("rw----");
        loginNewActor(isAdmin, isSudo, isRestricted ? AdminPrivilegeWriteOwned.value : null);
        Folder folder = mmFactory.simpleFolder();
        folder.getDetails().setOwner(new ExperimenterI(normalUser.userId, false));
        try {
            final ImmutableMap<String, String> groupContext = ImmutableMap.of("omero.group", Long.toString(normalUser.groupId));
            folder = (Folder) iUpdate.saveAndReturnObject(folder, groupContext);
            Assert.assertEquals(folder.getDetails().getOwner().getId().getValue(), normalUser.userId);
            Assert.assertEquals(folder.getDetails().getGroup().getId().getValue(), normalUser.groupId);
            Assert.assertTrue(isExpectSuccess);
        } catch (ServerError se) {
            Assert.assertFalse(isExpectSuccess);
        }
    }

    /**
     * Test that users may write other users' data only if they are a member of the <tt>system</tt> group and
     * have the <tt>WriteOwned</tt> privilege. Attempts changing of another user's existing data.
     * @param isAdmin if to test a member of the <tt>system</tt> group
     * @param isRestricted if to test a user who does <em>not</em> have the <tt>ModifyUser</tt> privilege
     * @param isSudo if to test attempt to subvert privilege by sudo to an unrestricted member of the <tt>system</tt> group
     * @throws Exception unexpected
     */
    @Test(dataProvider = "light administrator privilege test cases")
    public void testWriteOwnedPrivilegeEditing(boolean isAdmin, boolean isRestricted, boolean isSudo) throws Exception {
        final boolean isExpectSuccess = isAdmin && !isRestricted;
        final EventContext normalUser = newUserAndGroup("rw----");
        Folder folder = (Folder) iUpdate.saveAndReturnObject(mmFactory.simpleFolder());
        loginNewActor(isAdmin, isSudo, isRestricted ? AdminPrivilegeWriteOwned.value : null);
        final ImmutableMap<String, String> groupContext = ImmutableMap.of("omero.group", Long.toString(normalUser.groupId));
        try {
            folder = (Folder) iQuery.get("Folder", folder.getId().getValue(), groupContext);
            Assert.assertTrue(isAdmin, "normal users cannot read data from others' groups");
        } catch (SecurityViolation sv) {
            Assert.assertFalse(isAdmin, "admins can read data from others' groups");
            /* cannot now make the attempt */
            return;
        }
        folder.setName(omero.rtypes.rstring(getClass().getName()));
        try {
            folder = (Folder) iUpdate.saveAndReturnObject(folder, groupContext);
            Assert.assertEquals(folder.getName().getValue(), getClass().getName());
            Assert.assertTrue(isExpectSuccess);
        } catch (ServerError se) {
            Assert.assertFalse(isExpectSuccess);
        }
    }

    /**
     * Test that users may write other users' data only if they are a member of the <tt>system</tt> group and
     * have the <tt>WriteOwned</tt> privilege. Attempts deletion of another user's data.
     * @param isAdmin if to test a member of the <tt>system</tt> group
     * @param isRestricted if to test a user who does <em>not</em> have the <tt>ModifyUser</tt> privilege
     * @param isSudo if to test attempt to subvert privilege by sudo to an unrestricted member of the <tt>system</tt> group
     * @throws Exception unexpected
     */
    @Test(dataProvider = "light administrator privilege test cases")
    public void testWriteOwnedPrivilegeDeletion(boolean isAdmin, boolean isRestricted, boolean isSudo) throws Exception {
        final boolean isExpectSuccess = isAdmin && !isRestricted;
        newUserAndGroup("rw----");
        Folder folder = (Folder) iUpdate.saveAndReturnObject(mmFactory.simpleFolder());
        loginNewActor(isAdmin, isSudo, isRestricted ? AdminPrivilegeWriteOwned.value : null);
        doChange(client, factory, Requests.delete().target(folder).build(), isExpectSuccess);
    }

    /**
     * Test that users may move others' data only if they are a member of the <tt>system</tt> group and
     * have the <tt>Chgrp</tt> privilege. Attempts moving data via {@link omero.cmd.Chgrp2}.
     * @param isAdmin if to test a member of the <tt>system</tt> group
     * @param isRestricted if to test a user who does <em>not</em> have the <tt>Chgrp</tt> privilege
     * @param isSudo if to test attempt to subvert privilege by sudo to an unrestricted member of the <tt>system</tt> group
     * @throws Exception unexpected
     */
    @Test(dataProvider = "light administrator privilege test cases")
    public void testChgrpPrivilegeViaRequest(boolean isAdmin, boolean isRestricted, boolean isSudo) throws Exception {
        final boolean isExpectSuccess = isAdmin && !isRestricted;
        final EventContext normalUser = newUserAndGroup("rwr---");
        final long otherGroupId = newGroupAddUser("rwr---", normalUser.userId).getId().getValue();
        Folder folder = (Folder) iUpdate.saveAndReturnObject(mmFactory.simpleFolder());
        Assert.assertEquals(folder.getDetails().getGroup().getId().getValue(), normalUser.groupId);
        loginNewActor(isAdmin, isSudo, isRestricted ? AdminPrivilegeChgrp.value : null);
        doChange(client, factory, Requests.chgrp().target(folder).toGroup(otherGroupId).build(), isExpectSuccess);
        if (isExpectSuccess) {
            final ImmutableMap<String, String> groupContext = ImmutableMap.of("omero.group", Long.toString(otherGroupId));
            folder = (Folder) iQuery.get("Folder", folder.getId().getValue(), groupContext);
            Assert.assertEquals(folder.getDetails().getOwner().getId().getValue(), normalUser.userId);
            Assert.assertEquals(folder.getDetails().getGroup().getId().getValue(), otherGroupId);
        }
    }

    /**
     * Test that users may move others' data only if they are a member of the <tt>system</tt> group and
     * have the <tt>Chgrp</tt> privilege. Attempts moving data via {@link AbstractServerTest#iUpdate}.
     * @param isAdmin if to test a member of the <tt>system</tt> group
     * @param isRestricted if to test a user who does <em>not</em> have the <tt>Chgrp</tt> privilege
     * @param isSudo if to test attempt to subvert privilege by sudo to an unrestricted member of the <tt>system</tt> group
     * @throws Exception unexpected
     */
    @Test(dataProvider = "light administrator privilege test cases")
    public void testChgrpPrivilegeViaUpdate(boolean isAdmin, boolean isRestricted, boolean isSudo) throws Exception {
        final boolean isExpectSuccess = isAdmin && !isRestricted;
        final EventContext normalUser = newUserAndGroup("rwr---");
        final ExperimenterGroup otherGroup = newGroupAddUser("rwr---", normalUser.userId);
        Folder folder = (Folder) iUpdate.saveAndReturnObject(mmFactory.simpleFolder());
        Assert.assertEquals(folder.getDetails().getGroup().getId().getValue(), normalUser.groupId);
        loginNewActor(isAdmin, isSudo, isRestricted ? AdminPrivilegeChgrp.value : null);
        final ImmutableMap<String, String> groupContext = ImmutableMap.of("omero.group", Long.toString(normalUser.groupId));
        try {
            folder = (Folder) iQuery.get("Folder", folder.getId().getValue(), groupContext);
            Assert.assertTrue(isAdmin, "normal users cannot read data from others' groups");
        } catch (SecurityViolation sv) {
            Assert.assertFalse(isAdmin, "admins can read data from others' groups");
            /* cannot now make the attempt */
            return;
        }
        folder.getDetails().setGroup(otherGroup);
        try {
            folder = (Folder) iUpdate.saveAndReturnObject(folder, groupContext);
            Assert.assertEquals(folder.getDetails().getOwner().getId().getValue(), normalUser.userId);
            Assert.assertEquals(folder.getDetails().getGroup().getId().getValue(), otherGroup.getId().getValue());
            Assert.assertTrue(isExpectSuccess);
        } catch (ServerError se) {
            Assert.assertFalse(isExpectSuccess);
        }
    }

    /**
     * Test that users may give others' data only if they are a member of the <tt>system</tt> group and
     * have the <tt>Chown</tt> privilege. Attempts giving data via {@link omero.cmd.Chown2}.
     * @param isAdmin if to test a member of the <tt>system</tt> group
     * @param isRestricted if to test a user who does <em>not</em> have the <tt>Chown</tt> privilege
     * @param isSudo if to test attempt to subvert privilege by sudo to an unrestricted member of the <tt>system</tt> group
     * @throws Exception unexpected
     */
    @Test(dataProvider = "light administrator privilege test cases")
    public void testChownPrivilegeViaRequest(boolean isAdmin, boolean isRestricted, boolean isSudo) throws Exception {
        final boolean isExpectSuccess = isAdmin && !isRestricted;
        final EventContext normalUser = newUserAndGroup("rwr---");
        Folder folder = (Folder) iUpdate.saveAndReturnObject(mmFactory.simpleFolder());
        Assert.assertEquals(folder.getDetails().getOwner().getId().getValue(), normalUser.userId);
        final EventContext otherUser = newUserInGroup(normalUser);
        loginNewActor(isAdmin, isSudo, isRestricted ? AdminPrivilegeChown.value : null);
        doChange(client, factory, Requests.chown().target(folder).toUser(otherUser.userId).build(), isExpectSuccess);
        if (isExpectSuccess) {
            final ImmutableMap<String, String> groupContext = ImmutableMap.of("omero.group", Long.toString(normalUser.groupId));
            folder = (Folder) iQuery.get("Folder", folder.getId().getValue(), groupContext);
            Assert.assertEquals(folder.getDetails().getOwner().getId().getValue(), otherUser.userId);
            Assert.assertEquals(folder.getDetails().getGroup().getId().getValue(), normalUser.groupId);
        }
    }

    /**
     * Test that users may give others' data only if they are a member of the <tt>system</tt> group and
     * have the <tt>Chown</tt> privilege. Attempts giving data via {@link AbstractServerTest#iUpdate}.
     * @param isAdmin if to test a member of the <tt>system</tt> group
     * @param isRestricted if to test a user who does <em>not</em> have the <tt>Chown</tt> privilege
     * @param isSudo if to test attempt to subvert privilege by sudo to an unrestricted member of the <tt>system</tt> group
     * @throws Exception unexpected
     */
    @Test(dataProvider = "light administrator privilege test cases")
    public void testChownPrivilegeViaUpdate(boolean isAdmin, boolean isRestricted, boolean isSudo) throws Exception {
        final boolean isExpectSuccess = isAdmin && !isRestricted;
        final EventContext normalUser = newUserAndGroup("rwr---");
        Folder folder = (Folder) iUpdate.saveAndReturnObject(mmFactory.simpleFolder());
        Assert.assertEquals(folder.getDetails().getOwner().getId().getValue(), normalUser.userId);
        final EventContext otherUser = newUserInGroup(normalUser);
        loginNewActor(isAdmin, isSudo, isRestricted ? AdminPrivilegeChown.value : null);
        final ImmutableMap<String, String> groupContext = ImmutableMap.of("omero.group", Long.toString(normalUser.groupId));
        try {
            folder = (Folder) iQuery.get("Folder", folder.getId().getValue(), groupContext);
            Assert.assertTrue(isAdmin, "normal users cannot read data from others' groups");
        } catch (SecurityViolation sv) {
            Assert.assertFalse(isAdmin, "admins can read data from others' groups");
            /* cannot now make the attempt */
            return;
        }
        folder.getDetails().setOwner(new ExperimenterI(otherUser.userId, false));
        try {
            folder = (Folder) iUpdate.saveAndReturnObject(folder, groupContext);
            Assert.assertEquals(folder.getDetails().getOwner().getId().getValue(), otherUser.userId);
            Assert.assertEquals(folder.getDetails().getGroup().getId().getValue(), normalUser.groupId);
            Assert.assertTrue(isExpectSuccess);
        } catch (ServerError se) {
            Assert.assertFalse(isExpectSuccess);
        }
    }

    /**
     * @return a variety of test cases for light administrator privileges
     */
    @DataProvider(name = "light administrator privilege test cases")
    public Object[][] provideAdminPrivilegeCases() {
        int index = 0;
        final int IS_ADMIN = index++;
        final int IS_RESTRICTED = index++;
        final int IS_SUDO = index++;

        final boolean[] booleanCases = new boolean[]{false, true};

        final List<Object[]> testCases = new ArrayList<Object[]>();

        for (final boolean isAdmin : booleanCases) {
            for (final boolean isRestricted : booleanCases) {
                for (final boolean isSudo : booleanCases) {
                    if (!isAdmin && isSudo) {
                        /* not interesting */
                        continue;
                    }
                    if (isSudo && !isRestricted) {
                        /* not interesting */
                        continue;
                    }
                    final Object[] testCase = new Object[index];
                    testCase[IS_ADMIN] = isAdmin;
                    testCase[IS_RESTRICTED] = isRestricted;
                    testCase[IS_SUDO] = isSudo;
                    // DEBUG  if (isAdmin == false && isRestricted == true && isSudo == false)
                    testCases.add(testCase);
                }
            }
        }

        return testCases.toArray(new Object[testCases.size()][]);
    }
}
