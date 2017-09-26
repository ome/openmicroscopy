/*
 * Copyright (C) 2017 University of Dundee & Open Microscopy Environment.
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

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import ome.model.enums.DetectorType;
import ome.model.enums.EventType;
import omero.RString;
import omero.ServerError;
import omero.api.IScriptPrx;
import omero.api.ITypesPrx;
import omero.api.IUpdatePrx;
import omero.api.RawFileStorePrx;
import omero.api.SearchPrx;
import omero.model.AdminPrivilege;
import omero.model.AdminPrivilegeI;
import omero.model.ContrastMethod;
import omero.model.ContrastMethodI;
import omero.model.Experimenter;
import omero.model.ExperimenterGroup;
import omero.model.ExperimenterGroupI;
import omero.model.ExperimenterI;
import omero.model.IObject;
import omero.model.Image;
import omero.model.ImageI;
import omero.model.NamedValue;
import omero.model.OriginalFile;
import omero.model.PermissionsI;
import omero.model.Project;
import omero.model.enums.AdminPrivilegeChgrp;
import omero.model.enums.AdminPrivilegeChown;
import omero.model.enums.AdminPrivilegeDeleteFile;
import omero.model.enums.AdminPrivilegeDeleteManagedRepo;
import omero.model.enums.AdminPrivilegeDeleteOwned;
import omero.model.enums.AdminPrivilegeDeleteScriptRepo;
import omero.model.enums.AdminPrivilegeModifyGroup;
import omero.model.enums.AdminPrivilegeModifyGroupMembership;
import omero.model.enums.AdminPrivilegeModifyUser;
import omero.model.enums.AdminPrivilegeSudo;
import omero.model.enums.AdminPrivilegeWriteFile;
import omero.model.enums.AdminPrivilegeWriteManagedRepo;
import omero.model.enums.AdminPrivilegeWriteOwned;
import omero.model.enums.AdminPrivilegeWriteScriptRepo;
import omero.sys.EventContext;
import omero.sys.ParametersI;

import org.apache.commons.io.FileUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableList;

/**
 * Tests the concrete workflows of the light admins
 * @author p.walczysko@dundee.ac.uk
 * @since 5.4.0
 */
public class LightAdminRolesTest extends RolesTests {

    @Override
    @AfterClass
    public void tearDown() throws Exception {
        final ITypesPrx svc = root.getSession().getTypesService();
        svc.resetEnumerations(ContrastMethod.class.getName());
        svc.resetEnumerations(DetectorType.class.getName());
        super.tearDown();
    }


   

    /**
     * Test that a light admin can edit the name of a project
     * on behalf of another user either using <tt>Sudo</tt> privilege
     * or not using it, but having <tt>WriteOwned</tt> privilege instead.
     * @param isSudoing if to test a success of workflows where Sudoed in
     * @param permWriteOwned if to test a user who has the <tt>WriteOwned</tt> privilege
     * @param groupPermissions to test the effect of group permission level
     * @throws Exception unexpected
     * @see <a href="https://downloads.openmicroscopy.org/resources/experimental/tests/graph-permissions/0.1/testEdit.pptx">graphical explanation</a>
     */
    @Test(dataProvider = "isSudoing and WriteOwned privileges cases")
    public void testEdit(boolean isSudoing, boolean permWriteOwned,
            String groupPermissions) throws Exception {
        final boolean isExpectSuccess = isSudoing || permWriteOwned;
        final EventContext normalUser = newUserAndGroup(groupPermissions);
        /* Set up the light admin's permissions for this test.*/
        List<String> permissions = new ArrayList<String>();
        permissions.add(AdminPrivilegeSudo.value);
        if (permWriteOwned) permissions.add(AdminPrivilegeWriteOwned.value);
        final EventContext lightAdmin;
        lightAdmin = loginNewAdmin(true, permissions);
        /* Create Project as normalUser.*/
        loginUser(normalUser);
        Project proj = mmFactory.simpleProject();
        final String originalName = "OriginalNameOfNormalUser";
        proj.setName(omero.rtypes.rstring(originalName));
        Project sentProj = (Project) iUpdate.saveAndReturnObject(proj);
        String savedOriginalName = sentProj.getName().getValue().toString();
        loginUser(lightAdmin);
        /* As lightAdmin, sudo as the normalUser if this should be the case.*/
        if (isSudoing) sudo(new ExperimenterI(normalUser.userId, false));
        /* Check that the canEdit flag on the created project is as expected.*/
        Assert.assertEquals(getCurrentPermissions(sentProj).canEdit(), isExpectSuccess);
        /* Try to rename the Project.*/
        final String changedName = "ChangedNameOfLightAdmin";
        client.getImplicitContext().put("omero.group", Long.toString(normalUser.groupId));
        long id = sentProj.getId().getValue();
        final Project retrievedUnrenamedProject = (Project) iQuery.get("Project", id);
        retrievedUnrenamedProject.setName(omero.rtypes.rstring(changedName));
        /* Check that lightAdmin can edit the Project of normalUser only when
         * lightAdmin is equipped with sufficient permissions, captured in boolean isExpectSuccess.*/
        try {
            sentProj = (Project) iUpdate.saveAndReturnObject(retrievedUnrenamedProject);
            Assert.assertTrue(isExpectSuccess);
        } catch (ServerError se) {
            Assert.assertFalse(isExpectSuccess, se.toString());
        }
        String savedChangedName = sentProj.getName().getValue().toString();
        logRootIntoGroup(normalUser.groupId);
        final String retrievedName = ((RString) iQuery.projection(
                "SELECT name FROM Project p WHERE p.id = :id",
                new ParametersI().addId(sentProj.getId())).get(0).get(0)).getValue();
        /* Check that the Project still belongs to normalUser and the name of the Project
         * was changed and saved or original name is retained as appropriate.*/
        assertOwnedBy(sentProj, normalUser);
        if (isExpectSuccess) {
            Assert.assertEquals(savedChangedName, retrievedName);
            Assert.assertEquals(savedChangedName, changedName);
        } else {
            Assert.assertEquals(savedOriginalName, retrievedName);
            Assert.assertEquals(savedOriginalName, originalName);
        }
    }


 


    
    /**
     * Light admin (lightAdmin) tries to modify group membership.
     * lightAdmin will succeed if they have <tt>ModifyGroupMembership</tt> privilege.
     * To modify the group membership, lightAdmin attempts to add
     * an existing user to an existing group.
     * @param isPrivileged if to test a user who has the <tt>ModifyGroupMembership</tt> privilege
     * @param groupPermissions to test the effect of group permission level
     * @throws Exception unexpected
     */
    @Test(dataProvider = "isPrivileged cases")
    public void testModifyGroupMembershipAddUser(boolean isPrivileged, String groupPermissions) throws Exception {
        /* isPrivileged translates in this test into ModifyGroupMembership permission, see below.*/
        boolean isExpectSuccessAddUserToGroup = isPrivileged;
        final EventContext normalUser = newUserAndGroup(groupPermissions);
        /* One extra group is needed to add the existing normalUser to.*/
        final EventContext otherUser = newUserAndGroup(groupPermissions);
        List<String> permissions = new ArrayList<String>();
        if (isPrivileged) permissions.add(AdminPrivilegeModifyGroupMembership.value);
        loginNewAdmin(true, permissions);
        final Experimenter user = new ExperimenterI(normalUser.userId, false);
        final ExperimenterGroup group = new ExperimenterGroupI(otherUser.groupId, false);
        try {
            iAdmin.addGroups(user, Collections.singletonList(group));
            Assert.assertTrue(isExpectSuccessAddUserToGroup);
        } catch (ServerError se) {
            Assert.assertFalse(isExpectSuccessAddUserToGroup);
        }
    }

    /**
     * Light admin (lightAdmin) tries to modify group membership.
     * lightAdmin will succeed if they have <tt>ModifyGroupMembership</tt> privilege.
     * To modify the group membership, lightAdmin attempts to remove
     * an existing user from an existing group.
     * @param isPrivileged if to test a user who has the <tt>ModifyGroupMembership</tt> privilege
     * @param groupPermissions to test the effect of group permission level
     * @throws Exception unexpected
     */
    @Test(dataProvider = "isPrivileged cases")
    public void testModifyGroupMembershipRemoveUser(boolean isPrivileged,
            String groupPermissions) throws Exception {
        /* isPrivileged translates in this test into ModifyGroupMembership permission, see below.*/
        boolean isExpectSuccessRemoveUserFromGroup = isPrivileged;
        final EventContext normalUser = newUserAndGroup(groupPermissions);
        /* One extra group is needed from which normalUser removal will be attempted.*/
        final ExperimenterGroup otherGroup = newGroupAddUser("rwr-r-", normalUser.userId);
        List<String> permissions = new ArrayList<String>();
        if (isPrivileged) permissions.add(AdminPrivilegeModifyGroupMembership.value);
        loginNewAdmin(true, permissions);
        final Experimenter user = new ExperimenterI(normalUser.userId, false);
        try {
            iAdmin.removeGroups(user, Collections.singletonList(otherGroup));
            Assert.assertTrue(isExpectSuccessRemoveUserFromGroup);
        } catch (ServerError se) {
            Assert.assertFalse(isExpectSuccessRemoveUserFromGroup);
        }
    }

    /**
     * Light admin (lightAdmin) tries to make a user an owner of a group.
     * lightAdmin will succeed if they have the <tt>ModifyGroupMembership</tt> privilege.
     * @param isPrivileged if to test a user who has the <tt>ModifyGroupMembership</tt> privilege
     * @param groupPermissions to test the effect of group permission level
     * @throws Exception unexpected
     */
    @Test(dataProvider = "isPrivileged cases")
    public void testModifyGroupMembershipMakeOwner(boolean isPrivileged, String groupPermissions) throws Exception {
        /* isPrivileged translates in this test into ModifyGroupMembership permission, see below.*/
        boolean isExpectSuccessMakeOwnerOfGroup= isPrivileged;
        final EventContext normalUser = newUserAndGroup(groupPermissions);
        List<String> permissions = new ArrayList<String>();
        if (isPrivileged) permissions.add(AdminPrivilegeModifyGroupMembership.value);
        loginNewAdmin(true, permissions);
        final Experimenter user = new ExperimenterI(normalUser.userId, false);
        final ExperimenterGroup group = new ExperimenterGroupI(normalUser.groupId, false);
        try {
            iAdmin.setGroupOwner(group, user);
            Assert.assertTrue(isExpectSuccessMakeOwnerOfGroup);
        } catch (ServerError se) {
            Assert.assertFalse(isExpectSuccessMakeOwnerOfGroup);
        }
    }

    /**
     * Light admin (lightAdmin) tries to unset a user from being an owner of a group.
     * lightAdmin will succeed if they have the <tt>ModifyGroupMembership</tt> privilege.
     * @param isPrivileged if to test a user who has the <tt>ModifyGroupMembership</tt> privilege
     * @param groupPermissions to test the effect of group permission level
     * @throws Exception unexpected
     */
    @Test(dataProvider = "isPrivileged cases")
    public void testModifyGroupMembershipUnsetOwner(boolean isPrivileged,
            String groupPermissions) throws Exception {
        /* isPrivileged translates in this test into ModifyGroupMembership permission, see below.*/
        boolean isExpectSuccessUnsetOwnerOfGroup= isPrivileged;
        /* Set up the normalUser and make him an Owner by passing "true" in the
         * newUserAndGroup method argument.*/
        final EventContext normalUser = newUserAndGroup(groupPermissions, true);
        List<String> permissions = new ArrayList<String>();
        if (isPrivileged) permissions.add(AdminPrivilegeModifyGroupMembership.value);
        loginNewAdmin(true, permissions);
        final Experimenter user = new ExperimenterI(normalUser.userId, false);
        final ExperimenterGroup group = new ExperimenterGroupI(normalUser.groupId, false);
        try {
            iAdmin.unsetGroupOwner(group, user);
            Assert.assertTrue(isExpectSuccessUnsetOwnerOfGroup);
        } catch (ServerError se) {
            Assert.assertFalse(isExpectSuccessUnsetOwnerOfGroup);
        }
        /* Check that normalUser was unset as the owner of group when appropriate.*/
        if (isExpectSuccessUnsetOwnerOfGroup) {
            Assert.assertTrue(iAdmin.getLeaderOfGroupIds(user).isEmpty());
        } else {
            Assert.assertEquals((long) iAdmin.getLeaderOfGroupIds(user).get(0), group.getId().getValue());
        }
    }

    /**
     * Light admin (lightAdmin) tries to create a new user.
     * lightAdmin will succeed if they have the <tt>ModifyUser</tt> privilege.
     * @param isPrivileged if to test a user who has the <tt>ModifyUser</tt> privilege
     * @param groupPermissions to test the effect of group permission level
     * @throws Exception unexpected
     */
    @Test(dataProvider = "isPrivileged cases")
    public void testModifyUserCreate(boolean isPrivileged,
            String groupPermissions) throws Exception {
        /* isPrivileged translates in this test into ModifyUser permission, see below.*/
        boolean isExpectSuccessCreateUser= isPrivileged;
        final long newGroupId = newUserAndGroup(groupPermissions).groupId;
        List<String> permissions = new ArrayList<String>();
        if (isPrivileged) permissions.add(AdminPrivilegeModifyUser.value);
        loginNewAdmin(true, permissions);
        final Experimenter newUser = new ExperimenterI();
        newUser.setOmeName(omero.rtypes.rstring(UUID.randomUUID().toString()));
        newUser.setFirstName(omero.rtypes.rstring("August"));
        newUser.setLastName(omero.rtypes.rstring("Köhler"));
        newUser.setLdap(omero.rtypes.rbool(false));
        try {
            final long userGroupId = iAdmin.getSecurityRoles().userGroupId;
            final List<ExperimenterGroup> groups = ImmutableList.<ExperimenterGroup>of(new ExperimenterGroupI(userGroupId, false));
            iAdmin.createExperimenter(newUser, new ExperimenterGroupI(newGroupId, false), groups);
            Assert.assertTrue(isExpectSuccessCreateUser);
        } catch (ServerError se) {
            Assert.assertFalse(isExpectSuccessCreateUser);
        }
    }

    /**
     * Light admin (lightAdmin) tries to create a new user which is also light admin (createdAdmin).
     * createdAdmin has the same privileges as the creating lightAdmin.
     * lightAdmin will succeed if they have the <tt>ModifyUser</tt> privilege.
     * Four types of lightAdmin privileges are tested, matching the types defined in the user doc.
     * @param permModifyUser if to test a user who has the <tt>ModifyUser</tt> privilege
     * @param lightAdminType to test 4 light admin permission combinations matching the user doc
     * @throws Exception unexpected
     */
    @Test(dataProvider = "createLightAdmin cases")
    public void testModifyUserCreateLight(boolean permModifyUser, String lightAdminType) throws Exception {
        /* isPrivileged translates in this test into ModifyUser permission, see below.*/
        boolean isExpectSuccessCreateLightAdmin= permModifyUser;
        List<String> permissions = new ArrayList<String>();
        if (permModifyUser) permissions.add(AdminPrivilegeModifyUser.value);
        /* Define the permission types for the four types of lightAdmin. The
         * "DataViewer" lightAdminType does not have any permissions, and thus
         * it is not listed in the if/else branching below. "Organizer" should
         * normally have "ModifyUser" permission, but this is an object of testing,
         * and so is not given in the else if block below.*/
        if (lightAdminType.equals("Importer")) {
            permissions.add(AdminPrivilegeSudo.value);
        } else if (lightAdminType.equals("Analyst")) {
            permissions.add(AdminPrivilegeChown.value);
            permissions.add(AdminPrivilegeWriteManagedRepo.value);
            permissions.add(AdminPrivilegeWriteFile.value);
            permissions.add(AdminPrivilegeWriteOwned.value);
            permissions.add(AdminPrivilegeWriteScriptRepo.value);
            permissions.add(AdminPrivilegeDeleteScriptRepo.value);
        } else if (lightAdminType.equals("Organizer")) {
            permissions.add(AdminPrivilegeChgrp.value);
            permissions.add(AdminPrivilegeChown.value);
            permissions.add(AdminPrivilegeModifyGroup.value);
            permissions.add(AdminPrivilegeModifyGroupMembership.value);
            permissions.add(AdminPrivilegeDeleteOwned.value);
            permissions.add(AdminPrivilegeDeleteManagedRepo.value);
            permissions.add(AdminPrivilegeDeleteFile.value);
            permissions.add(AdminPrivilegeWriteManagedRepo.value);
            permissions.add(AdminPrivilegeWriteFile.value);
            permissions.add(AdminPrivilegeWriteOwned.value);
        }
        loginNewAdmin(true, permissions);
        /* lightAdmin declares and defines the createdAdmin they are
         * attempting to create (createdAdmin). Permissions will be the same for lightAdmin
         * and createdAdmin.*/
        Experimenter createdAdmin = new ExperimenterI();
        createdAdmin.setOmeName(omero.rtypes.rstring(UUID.randomUUID().toString()));
        createdAdmin.setFirstName(omero.rtypes.rstring("August"));
        createdAdmin.setLastName(omero.rtypes.rstring("Köhler"));
        createdAdmin.setLdap(omero.rtypes.rbool(false));
        final List<AdminPrivilege> privileges = new ArrayList<>();
        for (final String permission : permissions) {
            final AdminPrivilege privilege = new AdminPrivilegeI();
            privilege.setValue(omero.rtypes.rstring(permission));
            privileges.add(privilege);
        }
        /* lightAdmin succeeds only if they have right permissions.*/
        try {
            iAdmin.createRestrictedSystemUser(createdAdmin, privileges);
            Assert.assertTrue(isExpectSuccessCreateLightAdmin);
        } catch (ServerError se) {
            Assert.assertFalse(isExpectSuccessCreateLightAdmin);
        }
    }

    /**
     * Light admin (lightAdmin) tries to edit an existing user.
     * lightAdmin will succeed if they have the <tt>ModifyUser</tt> privilege.
     * @param isPrivileged if to test a user who has the <tt>ModifyUser</tt> privilege
     * @param groupPermissions to test the effect of group permission level
     * @throws Exception unexpected
     */
    @Test(dataProvider = "isPrivileged cases")
    public void testModifyUserEdit(boolean isPrivileged,
            String groupPermissions) throws Exception {
        /* isPrivileged translates in this test into ModifyUser permission, see below.*/
        boolean isExpectSuccessEditUser= isPrivileged;
        final long newUserId = newUserAndGroup(groupPermissions).userId;
        List<String> permissions = new ArrayList<String>();
        if (isPrivileged) permissions.add(AdminPrivilegeModifyUser.value);
        loginNewAdmin(true, permissions);
        final Experimenter newUser = (Experimenter) iQuery.get("Experimenter", newUserId);
        newUser.setConfig(ImmutableList.of(new NamedValue("color", "green")));
        try {
            iAdmin.updateExperimenter(newUser);
            Assert.assertTrue(isExpectSuccessEditUser);
        } catch (ServerError se) {
            Assert.assertFalse(isExpectSuccessEditUser);
        }
    }

    /**
     * Light admin (lightAdmin) tries to create a new group.
     * lightAmin will succeed if they have the <tt>ModifyGroup</tt> privilege.
     * @param isPrivileged if to test a user who has the <tt>ModifyGroup</tt> privilege
     * @param groupPermissions to test the effect of group permission level
     * @throws Exception unexpected
     */
    @Test(dataProvider = "isPrivileged cases")
    public void testModifyGroupCreate(boolean isPrivileged,
            String groupPermissions) throws Exception {
        /* isPrivileged translates in this test into ModifyGroup permission, see below.*/
        boolean isExpectSuccessCreateGroup = isPrivileged;
        final ExperimenterGroup newGroup = new ExperimenterGroupI();
        newGroup.setLdap(omero.rtypes.rbool(false));
        newGroup.setName(omero.rtypes.rstring(UUID.randomUUID().toString()));
        newGroup.getDetails().setPermissions(new PermissionsI(groupPermissions));
        /* Set up the permissions for lightAdmin.*/
        List<String> permissions = new ArrayList<String>();
        if (isPrivileged) permissions.add(AdminPrivilegeModifyGroup.value);
        loginNewAdmin(true, permissions);
        try {
            iAdmin.createGroup(newGroup);
            Assert.assertTrue(isExpectSuccessCreateGroup);
        } catch (ServerError se) {
            Assert.assertFalse(isExpectSuccessCreateGroup);
        }
    }

    /**
     * Light admin (lightAdmin) tries to edit an existing group.
     * lightAdmin will succeed if they have the <tt>ModifyGroup</tt> privilege.
     * @param isPrivileged if to test a user who has the <tt>ModifyGroup</tt> privilege
     * @param groupPermissions to test the effect of group permission level
     * @throws Exception unexpected
     */
    @Test(dataProvider = "isPrivileged cases")
    public void testModifyGroupEdit(boolean isPrivileged,
            String groupPermissions) throws Exception {
        /* isPrivileged translates in this test into ModifyGroup permission, see below.*/
        boolean isExpectSuccessEditGroup = isPrivileged;
        /* Set up the new group as Read-Write as part of the edit test will be a downgrade
         * of that group to all group types by the lightAdmin.*/
        final long newGroupId = newUserAndGroup("rwrw--").groupId;
        /* Set up the permissions for the lightAdmin.*/
        List<String> permissions = new ArrayList<String>();
        if (isPrivileged) permissions.add(AdminPrivilegeModifyGroup.value);
        loginNewAdmin(true, permissions);
        /* lightAdmin tries to downgrade the group to all possible permission levels and
         * also tries to edit the LDAP settings.*/
        final ExperimenterGroup newGroup = (ExperimenterGroup) iQuery.get("ExperimenterGroup", newGroupId);
        newGroup.getDetails().setPermissions(new PermissionsI(groupPermissions));
        newGroup.setLdap(omero.rtypes.rbool(true));
        try {
            iAdmin.updateGroup(newGroup);
            Assert.assertTrue(isExpectSuccessEditGroup);
        } catch (ServerError se) {
            Assert.assertFalse(isExpectSuccessEditGroup);
        }
    }

  
    /**
     * Tests if an enumeration not used can be deleted.
     * This should pass but it can be restored using the method
     * <code>resetEnumerations</code>.
     * @throws Exception
     */
    @Test
    public void testDeleteEnumerationsbyRestrictedSystemUser() throws Exception {
        //root create an enumeration first
        ContrastMethod ho = new ContrastMethodI();
        ho.setValue(omero.rtypes.rstring("testDeleteEnumerationsbyRestrictedSystemUser"));
        final ITypesPrx ts = root.getSession().getTypesService();
        List<IObject> types = ts.allEnumerations(ContrastMethod.class.getName());
        int n = types.size();
        ho = (ContrastMethod) ts.createEnumeration(ho);
        types = ts.allEnumerations(ContrastMethod.class.getName());
        Assert.assertEquals(types.size(), (n+1));
        logNewAdminWithoutPrivileges();
        //try to delete an enum type. The type is not important
        final ITypesPrx types_svc = factory.getTypesService();
        types = types_svc.allEnumerations(ContrastMethod.class.getName());
        int m = types.size();
        Assert.assertEquals(m, (n+1));
        Iterator<IObject> i = types.iterator();
        while (i.hasNext()) {
            IObject o = i.next();
            if (o.getId().getValue() == ho.getId().getValue()) {
                types_svc.deleteEnumeration(ho);
                break;
            }
        }
        types = types_svc.allEnumerations(ContrastMethod.class.getName());
        Assert.assertEquals(types.size(), n);
    }

    /**
     * Tests if an enumeration already used can be deleted.
     * An exception should be thrown.
     * @throws Exception
     */
    @Test(expectedExceptions = omero.ValidationException.class)
    public void testDeleteUsedEnumerationsbyRestrictedSystemUser() throws Exception {
        logNewAdminWithoutPrivileges();
        //try to delete an enum type. The type is not important
        final ITypesPrx types_svc = factory.getTypesService();
        List<IObject> types = types_svc.allEnumerations(EventType.class.getName());
        Iterator<IObject> i = types.iterator();
        while (i.hasNext()) {
            types_svc.deleteEnumeration(i.next());
        }
    }

    /**
     * Tests if deleted enumeration can be reset
     * @throws Exception
     */
    @Test
    public void testResetEnumerationsbyRestrictedSystemUser() throws Exception {
        logNewAdminWithoutPrivileges();
        final ITypesPrx types_svc = factory.getTypesService();
        List<IObject> types = types_svc.allEnumerations(DetectorType.class.getName());
        int n = types.size();
        Iterator<IObject> i = types.iterator();
        int count = 0;
        while (i.hasNext()) {
            try {
                types_svc.deleteEnumeration(i.next());
                count++;
            } catch (Exception e) {
                //Cannot delete the enumeration since it is used
            }
        }
        //not all enum are used so we should have deleted at least one
        Assert.assertTrue(count > 0);
        types = types_svc.allEnumerations(DetectorType.class.getName());
        Assert.assertEquals(types.size(), (n-count));
        //reset the deleted enumerations
        types_svc.resetEnumerations(DetectorType.class.getName());
        types = types_svc.allEnumerations(DetectorType.class.getName());
        //We should be back to the original list. Other enum might have been
        //added by other tests.
        Assert.assertTrue(types.size() >= n);
    }

    /**
     * Tests if an enumeration can be updated.
     * @throws Exception
     */
    @Test(expectedExceptions = omero.ValidationException.class)
    public void testUpdateEnumerationsbyRestrictedSystemUser() throws Exception {
        logNewAdminWithoutPrivileges();
        //try to update an enum type. The type is not important
        final ITypesPrx types_svc = factory.getTypesService();
        List<IObject> types = types_svc.allEnumerations(ContrastMethod.class.getName());
        Iterator<IObject> i = types.iterator();
        while (i.hasNext()) {
            ContrastMethod o = (ContrastMethod) i.next();
            o.setValue(omero.rtypes.rstring("foo"));
            types_svc.updateEnumeration(o);
        }
    }

    /**
     * Tests that any member in the system group can index the data.
     * @throws Exception
     */
    @Test
    public void testIndexObjectbyRestrictedSystemUser() throws Exception {
        logNewAdminWithoutPrivileges();
        final IUpdatePrx service = factory.getUpdateService();
        Image image = new ImageI();
        image.setName(omero.rtypes.rstring("Image with A - 11 reagent"));
        image = (Image) service.saveAndReturnObject(image);
        service.indexObject(image);
        SearchPrx prx = factory.createSearchService();
        prx.onlyType(Image.class.getName());
        prx.byFullText("\"A \\- 11\"");
        boolean found = false;
        while (prx.hasNext()) {
            for (IObject obj : prx.results()) {
                if (image.getClass().isAssignableFrom(obj.getClass())) {
                    if (obj.getId().equals(image.getId())) {
                        found = true;
                    }
                }
            }
        }
        Assert.assertTrue(found);
    }

    /**
     * Tests if a script can be modified using the upload method from omero.client.
     * @throws Exception
     */
    @Test(expectedExceptions = omero.SecurityViolation.class)
    public void testModifyScriptUsingUploadFromClientbyRestrictedSystemUser() throws Exception {
        logNewAdminWithoutPrivileges();
        IScriptPrx iScript = factory.getScriptService();
        /* lightAdmin fetches a script from the server.*/
        OriginalFile scriptFile = iScript.getScriptsByMimetype(ScriptServiceTest.PYTHON_MIMETYPE).get(0);
        String actualScript;
        RawFileStorePrx rfs = null;
        try {
            rfs = factory.createRawFileStore();
            rfs.setFileId(scriptFile.getId().getValue());
            actualScript = new String(rfs.read(0, (int) rfs.size()), StandardCharsets.UTF_8);
        } finally {
            if (rfs != null) rfs.close();
        }
        /* lightAdmin tries uploading the script as a new script in normalUser's group.*/
        iScript = factory.getScriptService();
        final String testScriptName = "Test_" + getClass().getName() + '_' + UUID.randomUUID() + ".py";
        File file = new File(testScriptName);
        file.deleteOnExit();
        FileUtils.writeStringToFile(file, actualScript);
        client.upload(file, scriptFile);
    }

    /**
     * Tests if a file owned by another user can be uploaded using the upload
     * method from omero.client.
     * @throws Exception
     */
    @Test(expectedExceptions = omero.SecurityViolation.class)
    public void testUploadFromClientbyRestrictedSystemUser() throws Exception {
        newUserAndGroup("rwrw--");
        OriginalFile of = (OriginalFile) iUpdate.saveAndReturnObject(mmFactory
                .createOriginalFile());
        long ofId = of.getId().getValue();
        RawFileStorePrx rfPrx = factory.createRawFileStore();
        try {
            rfPrx.setFileId(ofId);
            rfPrx.write(new byte[] { 1, 2, 3, 4 }, 0, 4);
            of = rfPrx.save();
        } finally {
            rfPrx.close();
        }
        logNewAdminWithoutPrivileges();
        final String testScriptName = "Test_" + getClass().getName() + '_' + UUID.randomUUID() + ".py";
        File file = new File(testScriptName);
        file.deleteOnExit();
        FileUtils.writeStringToFile(file, "test");
        client.upload(file, of);
    }

}
