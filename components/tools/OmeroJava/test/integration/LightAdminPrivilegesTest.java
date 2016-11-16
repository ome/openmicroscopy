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

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import omero.SecurityViolation;
import omero.ServerError;
import omero.api.IScriptPrx;
import omero.api.RawFileStorePrx;
import omero.cmd.CmdCallbackI;
import omero.cmd.HandlePrx;
import omero.gateway.util.Requests;
import omero.gateway.util.Requests.Delete2Builder;
import omero.grid.RepositoryMap;
import omero.grid.RepositoryPrx;
import omero.model.AdminPrivilege;
import omero.model.Experimenter;
import omero.model.ExperimenterGroup;
import omero.model.ExperimenterGroupI;
import omero.model.ExperimenterI;
import omero.model.Folder;
import omero.model.GroupExperimenterMapI;
import omero.model.IObject;
import omero.model.NamedValue;
import omero.model.OriginalFile;
import omero.model.OriginalFileI;
import omero.model.Session;
import omero.model.enums.AdminPrivilegeChgrp;
import omero.model.enums.AdminPrivilegeChown;
import omero.model.enums.AdminPrivilegeModifyUser;
import omero.model.enums.AdminPrivilegeSudo;
import omero.model.enums.AdminPrivilegeWriteFile;
import omero.model.enums.AdminPrivilegeWriteOwned;
import omero.sys.EventContext;
import omero.sys.ParametersI;
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

    /**
     * Populate the set of available light administrator privileges.
     * @throws ServerError unexpected
     */
    @BeforeClass
    public void populateAllPrivileges() throws ServerError {
        final ImmutableSet.Builder<AdminPrivilege> privileges = ImmutableSet.builder();
        for (final IObject privilege : factory.getTypesService().allEnumerations("AdminPrivilege")) {
            privileges.add((AdminPrivilege) privilege);
        }
        allPrivileges = privileges.build();
    }

    /**
     * Create a light administrator, possibly without a specific privilege, and log in as them.
     * @param isAdmin if the user should be a member of the <tt>system</tt> group
     * @param restriction the privilege that the user should not have, or {@code null} if they should have all privileges
     * @return the new user's context
     * @throws Exception if the light administrator could not be created
     */
    private EventContext loginNewAdmin(boolean isAdmin, String restriction) throws Exception {
        final EventContext ctx = isAdmin ? newUserInGroup(iAdmin.lookupGroup(SYSTEM_GROUP), false) : newUserAndGroup("rw----");
        if (!(isAdmin && restriction == null)) {
            final List<AdminPrivilege> privileges = new ArrayList<>(allPrivileges);
            if (restriction != null) {
                final Iterator<AdminPrivilege> privilegeIterator = privileges.iterator();
                while (!privilegeIterator.next().getValue().getValue().equals(restriction));
                privilegeIterator.remove();
            }
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
     * Identifies expected repositories and provides their name for looking them up from the database.
     * @author m.t.b.carroll@dundee.ac.uk
     * @since 5.3.0
     */
    private static enum Repository {
        MANAGED("ManagedRepository"), SCRIPT("scripts");

        /* corresponds to OriginalFile.name */
        final String name;

        private Repository(String name) {
            this.name = name;
        }
    }

    /**
     * Get a proxy for the given repository. It is assumed that such a repository exists.
     * @param repository a repository
     * @return a proxy for the repository
     * @throws ServerError unexpected
     */
    private RepositoryPrx getRepository(Repository repository) throws ServerError {
        final RepositoryMap repositories = factory.sharedResources().repositories();
        int index;
        for (index = 0; !repository.name.equals(repositories.descriptions.get(index).getName().getValue()); index++);
        return repositories.proxies.get(index);
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
     * Test that users may write other users' files only if they are a member of the <tt>system</tt> group and
     * have the <tt>WriteFile</tt> privilege.
     * Attempts creation of a file in another user's directory via {@link RepositoryPrx#register(String, omero.RString)}.
     * @param isAdmin if to test a member of the <tt>system</tt> group
     * @param isRestricted if to test a user who does <em>not</em> have the <tt>WriteFile</tt> privilege
     * @param isSudo if to test attempt to subvert privilege by sudo to an unrestricted member of the <tt>system</tt> group
     * @throws Exception unexpected
     */
    @Test(dataProvider = "light administrator privilege test cases")
    public void testWriteFilePrivilegeCreationViaRepoRegister(boolean isAdmin, boolean isRestricted, boolean isSudo)
            throws Exception {
        final boolean isExpectSuccess = isAdmin && !isRestricted;
        final EventContext normalUser = newUserAndGroup("rwr---");
        RepositoryPrx repo = getRepository(Repository.SCRIPT);
        final String userDirectory = "/Test_" + getClass().getName() + "_" + UUID.randomUUID();
        repo.makeDir(userDirectory, false);
        loginNewActor(isAdmin, isSudo, isRestricted ? AdminPrivilegeWriteFile.value : null);
        repo = getRepository(Repository.SCRIPT);
        final HashMap<String, String> context = new HashMap<>(client.getImplicitContext().getContext());
        context.put("omero.group", Long.toString(normalUser.groupId));
        final String filename = userDirectory + '/' + UUID.randomUUID();
        try {
            repo.register(filename, null, context);
            Assert.assertTrue(isExpectSuccess);
        } catch (ServerError se) {
            Assert.assertFalse(isExpectSuccess);
        }
    }

    /**
     * Test that users may write other users' files only if they are a member of the <tt>system</tt> group and
     * have the <tt>WriteFile</tt> privilege.
     * Attempts creation of a directory in another user's directory via {@link RepositoryPrx#makeDir(String, boolean)}.
     * @param isAdmin if to test a member of the <tt>system</tt> group
     * @param isRestricted if to test a user who does <em>not</em> have the <tt>WriteFile</tt> privilege
     * @param isSudo if to test attempt to subvert privilege by sudo to an unrestricted member of the <tt>system</tt> group
     * @throws Exception unexpected
     */
    @Test(dataProvider = "light administrator privilege test cases")
    public void testWriteFilePrivilegeCreationViaRepoMakeDir(boolean isAdmin, boolean isRestricted, boolean isSudo)
            throws Exception {
        final boolean isExpectSuccess = isAdmin && !isRestricted;
        final EventContext normalUser = newUserAndGroup("rwr---");
        RepositoryPrx repo = getRepository(Repository.SCRIPT);
        final String userDirectory = "/Test_" + getClass().getName() + "_" + UUID.randomUUID();
        repo.makeDir(userDirectory, false);
        loginNewActor(isAdmin, isSudo, isRestricted ? AdminPrivilegeWriteFile.value : null);
        repo = getRepository(Repository.SCRIPT);
        final HashMap<String, String> context = new HashMap<>(client.getImplicitContext().getContext());
        context.put("omero.group", Long.toString(normalUser.groupId));
        final String filename = userDirectory + '/' + UUID.randomUUID();
        try {
            repo.makeDir(filename, false, context);
            Assert.assertTrue(isExpectSuccess);
        } catch (ServerError se) {
            Assert.assertFalse(isExpectSuccess);
        }
    }

    /**
     * Test that users may write other users' files only if they are a member of the <tt>system</tt> group and
     * have the <tt>WriteFile</tt> privilege.
     * Attempts creation of a file in another user's group via {@link omero.api.IScriptPrx#uploadScript(String, String)}.
     * @param isAdmin if to test a member of the <tt>system</tt> group
     * @param isRestricted if to test a user who does <em>not</em> have the <tt>WriteFile</tt> privilege
     * @param isSudo if to test attempt to subvert privilege by sudo to an unrestricted member of the <tt>system</tt> group
     * @throws Exception unexpected
     */
    @Test(dataProvider = "light administrator privilege test cases")
    public void testWriteFilePrivilegeCreationViaScripts(boolean isAdmin, boolean isRestricted, boolean isSudo) throws Exception {
        final boolean isExpectSuccess = isAdmin && !isRestricted;
        final EventContext normalUser = newUserAndGroup("rwr---");
        IScriptPrx iScript = factory.getScriptService();
        /* fetch a script from the server */
        OriginalFile scriptFile = iScript.getScriptsByMimetype(ScriptServiceTest.PYTHON_MIMETYPE).get(0);
        RawFileStorePrx rfs = factory.createRawFileStore();
        rfs.setFileId(scriptFile.getId().getValue());
        final String actualScript = new String(rfs.read(0, (int) rfs.size()), StandardCharsets.UTF_8);
        rfs.close();
        /* try uploading the script as a new script in the normal user's group */
        final ImmutableMap<String, String> groupContext = ImmutableMap.of("omero.group", Long.toString(normalUser.groupId));
        loginNewActor(isAdmin, isSudo, isRestricted ? AdminPrivilegeWriteFile.value : null);
        iScript = factory.getScriptService(groupContext);
        final String testScriptName = "Test_" + getClass().getName() + "_" + UUID.randomUUID() + ".py";
        long testScriptId = -1;
        try {
            testScriptId = iScript.uploadScript(testScriptName, actualScript, groupContext);
            Assert.assertTrue(isExpectSuccess);
        } catch (ServerError se) {
            Assert.assertFalse(isExpectSuccess);
            /* upload failed so finish here */
            return;
        }
        /* check that the new script exists */
        loginUser(normalUser);
        scriptFile = (OriginalFile) iQuery.get("OriginalFile", testScriptId);
        Assert.assertEquals(scriptFile.getDetails().getGroup().getId().getValue(), normalUser.groupId);
        /* check if the script is correctly uploaded */
        rfs = factory.createRawFileStore();
        rfs.setFileId(testScriptId);
        final String currentScript = new String(rfs.read(0, (int) rfs.size()), StandardCharsets.UTF_8);
        rfs.close();
        Assert.assertEquals(currentScript, actualScript);
    }

    /**
     * Test that users may write other users' files only if they are a member of the <tt>system</tt> group and
     * have the <tt>WriteFile</tt> privilege.
     * Attempts creation of another user's file via {@link omero.api.IUpdatePrx#saveAndReturnObject(IObject, java.util.Map)}.
     * @param isAdmin if to test a member of the <tt>system</tt> group
     * @param isRestricted if to test a user who does <em>not</em> have the <tt>WriteFile</tt> privilege
     * @param isSudo if to test attempt to subvert privilege by sudo to an unrestricted member of the <tt>system</tt> group
     * @throws Exception unexpected
     */
    @Test(dataProvider = "light administrator privilege test cases")
    public void testWriteFilePrivilegeCreationViaUpdate(boolean isAdmin, boolean isRestricted, boolean isSudo) throws Exception {
        final boolean isExpectSuccess = isAdmin && !isRestricted;
        final EventContext normalUser = newUserAndGroup("rw----");
        loginNewActor(isAdmin, isSudo, isRestricted ? AdminPrivilegeWriteFile.value : null);
        OriginalFile file = mmFactory.createOriginalFile();
        file.getDetails().setOwner(new ExperimenterI(normalUser.userId, false));
        try {
            final ImmutableMap<String, String> groupContext = ImmutableMap.of("omero.group", Long.toString(normalUser.groupId));
            file = (OriginalFile) iUpdate.saveAndReturnObject(file, groupContext);
            Assert.assertEquals(file.getDetails().getOwner().getId().getValue(), normalUser.userId);
            Assert.assertEquals(file.getDetails().getGroup().getId().getValue(), normalUser.groupId);
            Assert.assertTrue(isExpectSuccess);
        } catch (ServerError se) {
            Assert.assertFalse(isExpectSuccess);
        }
    }

    /**
     * Test that users may write other users' files only if they are a member of the <tt>system</tt> group and
     * have the <tt>WriteFile</tt> privilege.
     * Attempts writing file via {@link RawFileStorePrx#write(byte[], long, int, java.util.Map)}.
     * @param isAdmin if to test a member of the <tt>system</tt> group
     * @param isRestricted if to test a user who does <em>not</em> have the <tt>WriteFile</tt> privilege
     * @param isSudo if to test attempt to subvert privilege by sudo to an unrestricted member of the <tt>system</tt> group
     * @throws Exception unexpected
     */
    @Test(dataProvider = "light administrator privilege test cases")
    public void testWriteFilePrivilegeEditingViaRaw(boolean isAdmin, boolean isRestricted, boolean isSudo) throws Exception {
        final boolean isExpectSuccess = isAdmin && !isRestricted;
        final EventContext normalUser = newUserAndGroup("rw----");
        /* write a random file */
        final long fileId = iUpdate.saveAndReturnObject(mmFactory.createOriginalFile()).getId().getValue();
        final byte[] fileContentOriginal = new byte[64];
        new Random().nextBytes(fileContentOriginal);
        RawFileStorePrx rfs = factory.createRawFileStore();
        rfs.setFileId(fileId);
        rfs.write(fileContentOriginal, 0, fileContentOriginal.length);
        rfs.close();
        byte[] fileContentCurrent;
        rfs = factory.createRawFileStore();
        rfs.setFileId(fileId);
        fileContentCurrent = rfs.read(0, (int) rfs.size());
        rfs.close();
        Assert.assertEquals(fileContentCurrent, fileContentOriginal);
        /* try to overwrite with a blank file */
        loginNewActor(isAdmin, isSudo, isRestricted ? AdminPrivilegeWriteFile.value : null);
        final ImmutableMap<String, String> groupContext = ImmutableMap.of("omero.group", Long.toString(normalUser.groupId));
        final byte[] fileContentBlank = new byte[fileContentOriginal.length];
        try {
            rfs = factory.createRawFileStore(groupContext);
            rfs.setFileId(fileId, groupContext);
            rfs.write(fileContentBlank, 0, fileContentBlank.length, groupContext);
            Assert.assertTrue(isExpectSuccess);
        } catch (ServerError se) {
            Assert.assertFalse(isExpectSuccess);
        } finally {
            try {
                rfs.close(groupContext);
            } catch (ServerError se) {
                /* cannot try to close */
            }
        }
        /* check the resulting file content */
        loginUser(normalUser);
        rfs = factory.createRawFileStore();
        rfs.setFileId(fileId);
        fileContentCurrent = rfs.read(0, (int) rfs.size());
        rfs.close();
        Assert.assertEquals(fileContentCurrent, isExpectSuccess ? fileContentBlank : fileContentOriginal);
    }

    /**
     * Test that users may write other users' files only if they are a member of the <tt>system</tt> group and
     * have the <tt>WriteFile</tt> privilege.
     * Attempts writing file via {@link RepositoryPrx#file(String, String, java.util.Map)}
     * and {@link RawFileStorePrx#write(byte[], long, int, java.util.Map)}.
     * @param isAdmin if to test a member of the <tt>system</tt> group
     * @param isRestricted if to test a user who does <em>not</em> have the <tt>WriteFile</tt> privilege
     * @param isSudo if to test attempt to subvert privilege by sudo to an unrestricted member of the <tt>system</tt> group
     * @throws Exception unexpected
     */
    @Test(dataProvider = "light administrator privilege test cases")
    public void testWriteFilePrivilegeEditingViaRepoFile(boolean isAdmin, boolean isRestricted, boolean isSudo) throws Exception {
        final boolean isExpectSuccess = isAdmin && !isRestricted;
        final EventContext normalUser = newUserAndGroup("rwr---");
        /* fetch a script from the server */
        final List<OriginalFile> scripts = factory.getScriptService().getScriptsByMimetype(ScriptServiceTest.PYTHON_MIMETYPE);
        RawFileStorePrx rfs = factory.createRawFileStore();
        rfs.setFileId(scripts.get(0).getId().getValue());
        final byte[] fileContentOriginal = rfs.read(0, (int) rfs.size());
        rfs.close();
        /* upload the script as a new script */
        final String testScriptName = "Test_" + getClass().getName() + "_" + UUID.randomUUID() + ".py";
        RepositoryPrx repo = getRepository(Repository.SCRIPT);
        final OriginalFile testScript = repo.register(testScriptName, omero.rtypes.rstring(ScriptServiceTest.PYTHON_MIMETYPE));
        final long testScriptId = testScript.getId().getValue();
        rfs = repo.file(testScriptName, "rw");
        rfs.write(fileContentOriginal, 0, fileContentOriginal.length);
        rfs.close();
        /* check that script is readable */
        byte[] fileContentCurrent;
        rfs = factory.createRawFileStore();
        rfs.setFileId(testScriptId);
        fileContentCurrent = rfs.read(0, (int) rfs.size());
        rfs.close();
        Assert.assertEquals(fileContentCurrent, fileContentOriginal);
        /* try to edit the script */
        loginNewActor(isAdmin, isSudo, isRestricted ? AdminPrivilegeWriteFile.value : null);
        final HashMap<String, String> context = new HashMap<>(client.getImplicitContext().getContext());
        context.put("omero.group", Long.toString(normalUser.groupId));
        repo = getRepository(Repository.SCRIPT);
        final byte[] fileContentBlank = new byte[fileContentOriginal.length];
        try {
            rfs = repo.file(testScriptName, "rw", context);
            rfs.write(fileContentBlank, 0, fileContentBlank.length, context);
            Assert.assertTrue(isExpectSuccess);
        } catch (ServerError se) {
            Assert.assertFalse(isExpectSuccess);
        } finally {
            try {
                rfs.close(context);
            } catch (Ice.CommunicatorDestroyedException cde) {
                /* cannot try to close */
            }
        }
        /* check the content of the script */
        loginUser(normalUser);
        rfs = factory.createRawFileStore();
        rfs.setFileId(testScriptId);
        fileContentCurrent = rfs.read(0, (int) rfs.size());
        rfs.close();
        Assert.assertEquals(fileContentCurrent, isExpectSuccess ? fileContentBlank : fileContentOriginal);
        /* avoid the problem captured by ScriptServiceTest.testGetScriptsFiltersUnreadable */
        doChange(Requests.delete().target(testScript).build());
    }

    /**
     * Test that users may write other users' files only if they are a member of the <tt>system</tt> group and
     * have the <tt>WriteFile</tt> privilege.
     * Attempts changing an existing file via {@link omero.api.IScriptPrx#editScript(OriginalFile, String, java.util.Map)}.
     * @param isAdmin if to test a member of the <tt>system</tt> group
     * @param isRestricted if to test a user who does <em>not</em> have the <tt>WriteFile</tt> privilege
     * @param isSudo if to test attempt to subvert privilege by sudo to an unrestricted member of the <tt>system</tt> group
     * @throws Exception unexpected
     */
    @Test(dataProvider = "light administrator privilege test cases")
    public void testWriteFilePrivilegeEditingViaScripts(boolean isAdmin, boolean isRestricted, boolean isSudo) throws Exception {
        final boolean isExpectSuccess = isAdmin && !isRestricted;
        final EventContext normalUser = newUserAndGroup("rw----");
        IScriptPrx iScript = factory.getScriptService();
        final List<OriginalFile> scripts = iScript.getScriptsByMimetype(ScriptServiceTest.PYTHON_MIMETYPE);
        /* fetch a script from the server */
        RawFileStorePrx rfs = factory.createRawFileStore();
        rfs.setFileId(scripts.get(0).getId().getValue());
        final String originalScript = new String(rfs.read(0, (int) rfs.size()), StandardCharsets.UTF_8);
        rfs.close();
        /* upload the script as a new script */
        final String testScriptName = "Test_" + getClass().getName() + "_" + UUID.randomUUID() + ".py";
        final long testScriptId = iScript.uploadScript(testScriptName, originalScript);
        /* try replacing the content of the normal user's script */
        loginNewActor(isAdmin, isSudo, isRestricted ? AdminPrivilegeWriteFile.value : null);
        iScript = factory.getScriptService();
        final ImmutableMap<String, String> groupContext = ImmutableMap.of("omero.group", Long.toString(normalUser.groupId));
        final String newScript = originalScript + "\n# this script is a copy of another";
        try {
            iScript.editScript(new OriginalFileI(testScriptId, false), newScript, groupContext);
            Assert.assertTrue(isExpectSuccess);
        } catch (ServerError se) {
            Assert.assertFalse(isExpectSuccess);
        }
        /* check the permissions on the script */
        loginUser(normalUser);
        OriginalFile scriptFile = (OriginalFile) iQuery.get("OriginalFile", testScriptId);
        Assert.assertEquals(scriptFile.getDetails().getOwner().getId().getValue(), normalUser.userId);
        Assert.assertEquals(scriptFile.getDetails().getGroup().getId().getValue(), normalUser.groupId);
        /* check the content of the script */
        rfs = factory.createRawFileStore();
        rfs.setFileId(testScriptId);
        final String currentScript = new String(rfs.read(0, (int) rfs.size()), StandardCharsets.UTF_8);
        rfs.close();
        Assert.assertEquals(currentScript, isExpectSuccess ? newScript : originalScript);
    }

    /**
     * Test that users may write other users' files only if they are a member of the <tt>system</tt> group and
     * have the <tt>WriteFile</tt> privilege.
     * Attempts changing an existing file via {@link omero.api.IUpdatePrx#saveAndReturnObject(IObject, java.util.Map)}.
     * @param isAdmin if to test a member of the <tt>system</tt> group
     * @param isRestricted if to test a user who does <em>not</em> have the <tt>WriteFile</tt> privilege
     * @param isSudo if to test attempt to subvert privilege by sudo to an unrestricted member of the <tt>system</tt> group
     * @throws Exception unexpected
     */
    @Test(dataProvider = "light administrator privilege test cases")
    public void testWriteFilePrivilegeEditingViaUpdate(boolean isAdmin, boolean isRestricted, boolean isSudo) throws Exception {
        final boolean isExpectSuccess = isAdmin && !isRestricted;
        final EventContext normalUser = newUserAndGroup("rw----");
        OriginalFile file = (OriginalFile) iUpdate.saveAndReturnObject(mmFactory.createOriginalFile());
        loginNewActor(isAdmin, isSudo, isRestricted ? AdminPrivilegeWriteFile.value : null);
        final ImmutableMap<String, String> groupContext = ImmutableMap.of("omero.group", Long.toString(normalUser.groupId));
        try {
            file = (OriginalFile) iQuery.get("OriginalFile", file.getId().getValue(), groupContext);
            Assert.assertTrue(isAdmin, "normal users cannot read data from others' groups");
        } catch (SecurityViolation sv) {
            Assert.assertFalse(isAdmin, "admins can read data from others' groups");
            /* cannot now make the attempt */
            return;
        }
        file.setName(omero.rtypes.rstring(getClass().getName()));
        try {
            file = (OriginalFile) iUpdate.saveAndReturnObject(file, groupContext);
            Assert.assertEquals(file.getName().getValue(), getClass().getName());
            Assert.assertTrue(isExpectSuccess);
        } catch (ServerError se) {
            Assert.assertFalse(isExpectSuccess);
        }
    }

    /**
     * Test that users may write other users' files only if they are a member of the <tt>system</tt> group and
     * have the <tt>WriteFile</tt> privilege.
     * Attempts deletion of another user's file via {@link RepositoryPrx#deletePaths(String[], boolean, boolean, java.util.Map)}.
     * @param isAdmin if to test a member of the <tt>system</tt> group
     * @param isRestricted if to test a user who does <em>not</em> have the <tt>WriteFile</tt> privilege
     * @param isSudo if to test attempt to subvert privilege by sudo to an unrestricted member of the <tt>system</tt> group
     * @throws Exception unexpected
     */
    @Test(dataProvider = "light administrator privilege test cases")
    public void testWriteFilePrivilegeDeletionViaRepo(boolean isAdmin, boolean isRestricted, boolean isSudo) throws Exception {
        final boolean isExpectSuccess = isAdmin && !isRestricted;
        final EventContext normalUser = newUserAndGroup("rwr---");
        /* fetch a script from the server */
        final List<OriginalFile> scripts = factory.getScriptService().getScriptsByMimetype(ScriptServiceTest.PYTHON_MIMETYPE);
        RawFileStorePrx rfs = factory.createRawFileStore();
        rfs.setFileId(scripts.get(0).getId().getValue());
        final byte[] fileContentOriginal = rfs.read(0, (int) rfs.size());
        rfs.close();
        /* upload the script as a new script */
        final String testScriptName = "Test_" + getClass().getName() + "_" + UUID.randomUUID() + ".py";
        RepositoryPrx repo = getRepository(Repository.SCRIPT);
        final OriginalFile testScript = repo.register(testScriptName, omero.rtypes.rstring(ScriptServiceTest.PYTHON_MIMETYPE));
        final long testScriptId = testScript.getId().getValue();
        rfs = repo.file(testScriptName, "rw");
        rfs.write(fileContentOriginal, 0, fileContentOriginal.length);
        rfs.close();
        /* check that script is readable */
        byte[] fileContentCurrent;
        rfs = factory.createRawFileStore();
        rfs.setFileId(testScriptId);
        fileContentCurrent = rfs.read(0, (int) rfs.size());
        rfs.close();
        Assert.assertEquals(fileContentCurrent, fileContentOriginal);
        /* try to delete the script */
        loginNewActor(isAdmin, isSudo, isRestricted ? AdminPrivilegeWriteFile.value : null);
        final HashMap<String, String> context = new HashMap<>(client.getImplicitContext().getContext());
        context.put("omero.group", Long.toString(normalUser.groupId));
        repo = getRepository(Repository.SCRIPT);
        try {
            final HandlePrx handle = repo.deletePaths(new String[] {testScriptName}, false, false, context);
            final CmdCallbackI callback = new CmdCallbackI(client, handle);
            callback.loop(20, scalingFactor);
            assertCmd(callback, isExpectSuccess);
        } catch (Ice.LocalException ue) {
            Assert.assertFalse(isExpectSuccess);
        }
        /* check the content of the script */
        loginUser(normalUser);
        rfs = factory.createRawFileStore();
        try {
            rfs.setFileId(testScriptId);
            fileContentCurrent = rfs.read(0, (int) rfs.size());
            Assert.assertEquals(fileContentCurrent, fileContentOriginal);
            Assert.assertFalse(isExpectSuccess);
        } catch (Ice.LocalException | ServerError se) {
            /* can catch only ServerError once RawFileStoreTest.testBadFileId is fixed */
            Assert.assertTrue(isExpectSuccess);
        } finally {
            rfs.close();
        }
        if (!isExpectSuccess) {
            /* avoid the problem captured by ScriptServiceTest.testGetScriptsFiltersUnreadable */
            doChange(Requests.delete().target(testScript).build());
        }
    }

    /**
     * Test that users may write other users' files only if they are a member of the <tt>system</tt> group and
     * have the <tt>WriteFile</tt> privilege.
     * Attempts deletion of another user's file via {@link omero.cmd.Delete2}.
     * @param isAdmin if to test a member of the <tt>system</tt> group
     * @param isRestricted if to test a user who does <em>not</em> have the <tt>WriteFile</tt> privilege
     * @param isSudo if to test attempt to subvert privilege by sudo to an unrestricted member of the <tt>system</tt> group
     * @throws Exception unexpected
     */
    @Test(dataProvider = "light administrator privilege test cases")
    public void testWriteFilePrivilegeDeletionViaRequest(boolean isAdmin, boolean isRestricted, boolean isSudo) throws Exception {
        final boolean isExpectSuccess = isAdmin && !isRestricted;
        newUserAndGroup("rw----");
        final OriginalFile file = (OriginalFile) iUpdate.saveAndReturnObject(mmFactory.createOriginalFile());
        loginNewActor(isAdmin, isSudo, isRestricted ? AdminPrivilegeWriteFile.value : null);
        doChange(client, factory, Requests.delete().target(file).build(), isExpectSuccess);
    }

    /**
     * Test that users may write other users' files only if they are a member of the <tt>system</tt> group and
     * have the <tt>WriteFile</tt> privilege.
     * Attempts deletion of another user's file via {@link IScriptPrx#deleteScript(long, java.util.Map)}.
     * @param isAdmin if to test a member of the <tt>system</tt> group
     * @param isRestricted if to test a user who does <em>not</em> have the <tt>WriteFile</tt> privilege
     * @param isSudo if to test attempt to subvert privilege by sudo to an unrestricted member of the <tt>system</tt> group
     * @throws Exception unexpected
     */
    @Test(dataProvider = "light administrator privilege test cases")
    public void testWriteFilePrivilegeDeletionViaScripts(boolean isAdmin, boolean isRestricted, boolean isSudo) throws Exception {
        final boolean isExpectSuccess = isAdmin && !isRestricted;
        final EventContext normalUser = newUserAndGroup("rw----");
        IScriptPrx iScript = factory.getScriptService();
        /* fetch a script from the server */
        final OriginalFile scriptFile = iScript.getScriptsByMimetype(ScriptServiceTest.PYTHON_MIMETYPE).get(0);
        RawFileStorePrx rfs = factory.createRawFileStore();
        rfs.setFileId(scriptFile.getId().getValue());
        final String actualScript = new String(rfs.read(0, (int) rfs.size()), StandardCharsets.UTF_8);
        rfs.close();
        /* upload the script as a new script */
        final String testScriptName = "Test_" + getClass().getName() + "_" + UUID.randomUUID() + ".py";
        final long testScriptId = iScript.uploadScript(testScriptName, actualScript);
        /* delete any jobs associated with the script */
        final Delete2Builder delete = Requests.delete().option(Requests.option().excludeType("OriginalFile").build());
        for (final IObject scriptJob : iQuery.findAllByQuery(
                "SELECT DISTINCT link.parent FROM JobOriginalFileLink link WHERE link.child.id = :id",
                new ParametersI().addId(testScriptId))) {
            delete.target(scriptJob);
        }
        doChange(delete.build());
        /* check that the new script exists */
        final OriginalFile testScript = new OriginalFileI(testScriptId, false);
        assertExists(testScript);
        /* try deleting the script */
        final ImmutableMap<String, String> groupContext = ImmutableMap.of("omero.group", Long.toString(normalUser.groupId));
        loginNewActor(isAdmin, isSudo, isRestricted ? AdminPrivilegeWriteFile.value : null);
        iScript = factory.getScriptService(groupContext);
        try {
            iScript.deleteScript(testScriptId, groupContext);
            Assert.assertTrue(isExpectSuccess);
        } catch (ServerError se) {
            Assert.assertFalse(isExpectSuccess);
        }
        /* check if the script was deleted or left intact */
        loginUser(normalUser);
        if (isExpectSuccess) {
            assertDoesNotExist(testScript);
        } else {
            assertExists(testScript);
        }
        rfs = factory.createRawFileStore();
        try {
            rfs.setFileId(testScriptId);
            final String currentScript = new String(rfs.read(0, (int) rfs.size()), StandardCharsets.UTF_8);
            Assert.assertEquals(currentScript, actualScript);
            Assert.assertFalse(isExpectSuccess);
        } catch (Ice.LocalException | ServerError se) {
            /* can catch only ServerError once RawFileStoreTest.testBadFileId is fixed */
            Assert.assertTrue(isExpectSuccess);
        } finally {
            rfs.close();
        }
    }

    /**
     * Test that users may write other users' data only if they are a member of the <tt>system</tt> group and
     * have the <tt>WriteOwned</tt> privilege. Attempts creation of another user's data.
     * @param isAdmin if to test a member of the <tt>system</tt> group
     * @param isRestricted if to test a user who does <em>not</em> have the <tt>WriteOwned</tt> privilege
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
     * @param isRestricted if to test a user who does <em>not</em> have the <tt>WriteOwned</tt> privilege
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
     * @param isRestricted if to test a user who does <em>not</em> have the <tt>WriteOwned</tt> privilege
     * @param isSudo if to test attempt to subvert privilege by sudo to an unrestricted member of the <tt>system</tt> group
     * @throws Exception unexpected
     */
    @Test(dataProvider = "light administrator privilege test cases")
    public void testWriteOwnedPrivilegeDeletion(boolean isAdmin, boolean isRestricted, boolean isSudo) throws Exception {
        final boolean isExpectSuccess = isAdmin && !isRestricted;
        newUserAndGroup("rw----");
        final Folder folder = (Folder) iUpdate.saveAndReturnObject(mmFactory.simpleFolder());
        loginNewActor(isAdmin, isSudo, isRestricted ? AdminPrivilegeWriteOwned.value : null);
        doChange(client, factory, Requests.delete().target(folder).build(), isExpectSuccess);
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
                    if (!isAdmin && isRestricted) {
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
