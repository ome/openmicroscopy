/*
 * Copyright (C) 2016-2017 University of Dundee & Open Microscopy Environment.
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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import ome.services.blitz.repo.path.FsFile;
import ome.services.scripts.ScriptRepoHelper;
import omero.RLong;
import omero.RString;
import omero.RType;
import omero.SecurityViolation;
import omero.ServerError;
import omero.api.IScriptPrx;
import omero.api.ISessionPrx;
import omero.api.RawFileStorePrx;
import omero.cmd.CmdCallbackI;
import omero.cmd.CurrentSessionsRequest;
import omero.cmd.CurrentSessionsResponse;
import omero.cmd.HandlePrx;
import omero.cmd.graphs.ChildOption;
import omero.gateway.util.Requests;
import omero.gateway.util.Requests.Delete2Builder;
import omero.grid.ImportLocation;
import omero.grid.ManagedRepositoryPrx;
import omero.grid.ManagedRepositoryPrxHelper;
import omero.grid.RepositoryMap;
import omero.grid.RepositoryPrx;
import omero.grid.SharedResourcesPrx;
import omero.model.AdminPrivilege;
import omero.model.ChecksumAlgorithm;
import omero.model.ChecksumAlgorithmI;
import omero.model.Experimenter;
import omero.model.ExperimenterGroup;
import omero.model.ExperimenterGroupI;
import omero.model.ExperimenterI;
import omero.model.Fileset;
import omero.model.Folder;
import omero.model.GroupExperimenterMap;
import omero.model.GroupExperimenterMapI;
import omero.model.IObject;
import omero.model.NamedValue;
import omero.model.OriginalFile;
import omero.model.OriginalFileI;
import omero.model.Session;
import omero.model.enums.AdminPrivilegeChgrp;
import omero.model.enums.AdminPrivilegeChown;
import omero.model.enums.AdminPrivilegeDeleteFile;
import omero.model.enums.AdminPrivilegeDeleteManagedRepo;
import omero.model.enums.AdminPrivilegeDeleteOwned;
import omero.model.enums.AdminPrivilegeDeleteScriptRepo;
import omero.model.enums.AdminPrivilegeModifyGroup;
import omero.model.enums.AdminPrivilegeModifyGroupMembership;
import omero.model.enums.AdminPrivilegeModifyUser;
import omero.model.enums.AdminPrivilegeReadSession;
import omero.model.enums.AdminPrivilegeSudo;
import omero.model.enums.AdminPrivilegeWriteFile;
import omero.model.enums.AdminPrivilegeWriteManagedRepo;
import omero.model.enums.AdminPrivilegeWriteOwned;
import omero.model.enums.AdminPrivilegeWriteScriptRepo;
import omero.model.enums.ChecksumAlgorithmMurmur3128;
import omero.model.enums.ChecksumAlgorithmSHA1160;
import omero.sys.EventContext;
import omero.sys.ParametersI;

import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

/**
 * Tests the effectiveness of light administrator privileges.
 * @author m.t.b.carroll@dundee.ac.uk
 * @since 5.4.0
 */
public class LightAdminPrivilegesTest extends RolesTests {

    private ImmutableSet<AdminPrivilege> allPrivileges = null;

    /**
     * Populate the set of available light administrator privileges.
     * @throws ServerError unexpected
     */
    @BeforeClass
    public void populateAllPrivileges() throws ServerError {
        final ImmutableSet.Builder<AdminPrivilege> privileges = ImmutableSet.builder();
        for (final IObject privilege : root.getSession().getTypesService().allEnumerations("AdminPrivilege")) {
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
        final EventContext ctx;
        if (isAdmin) {
            ctx = newUserInGroup(iAdmin.lookupGroup(roles.systemGroupName), false);
        } else {
            ctx = newUserAndGroup("rwr-r-");
        }
        if (!(isAdmin && restriction == null)) {
            final List<AdminPrivilege> privileges = new ArrayList<>(allPrivileges);
            if (restriction != null) {
                final Iterator<AdminPrivilege> privilegeIterator = privileges.iterator();
                while (!privilegeIterator.next().getValue().getValue().equals(restriction));
                privilegeIterator.remove();
            }
            root.getSession().getAdminService().setAdminPrivileges(new ExperimenterI(ctx.userId, false), privileges);
        }
        loginUser(ctx);
        return iAdmin.getEventContext();
    }

    /**
     * Create a light administrator, possibly without a specific privilege, and log in as them, possibly sudo'ing afterward.
     * @param isAdmin if the user should be a member of the <tt>system</tt> group
     * @param sudoTo the name of the user to whom the new user should then sudo or {@code null} for no sudo
     * @param restriction the privilege that the user should not have, or {@code null} if they should have all privileges
     * @return the new user's context (may be a sudo session)
     * @throws Exception if the light administrator actor could not be set up as specified
     */
    private EventContext loginNewActor(boolean isAdmin, String sudoTo, String restriction) throws Exception {
        final EventContext adminContext = loginNewAdmin(isAdmin, restriction);
        if (sudoTo != null) {
            try {
                final EventContext sudoContext = sudo(sudoTo);
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
     * Identifies the expected kinds of repository.
     * @author m.t.b.carroll@dundee.ac.uk
     * @since 5.4.0
     */
    private static enum Repository {
        MANAGED, SCRIPT, OTHER;
    }

    /**
     * Get a proxy for an instance of the given kind of repository.
     * @param repository a kind of repository
     * @return a proxy for the repository
     * @throws ServerError if the repositories could not be queried
     */
    private RepositoryPrx getRepository(Repository repository) throws ServerError {
        final RepositoryMap repositories = factory.sharedResources().repositories();
        RepositoryPrx latestProxy = null;
        long latestRepoId = 0;
        for (int index = repositories.descriptions.size() - 1; index >= 0; index--) {
            final boolean isManaged = ManagedRepositoryPrxHelper.checkedCast(repositories.proxies.get(index)) != null;
            final boolean isScript = ScriptRepoHelper.SCRIPT_REPO.equals(repositories.descriptions.get(index).getHash().getValue());
            if (repository == Repository.MANAGED && isManaged ||
                repository == Repository.SCRIPT && isScript ||
                repository == Repository.OTHER && !(isManaged || isScript)) {
                final RepositoryPrx currentProxy = repositories.proxies.get(index);
                final long currentRepoId = repositories.descriptions.get(index).getId().getValue();
                if (latestProxy == null || latestRepoId < currentRepoId) {
                    latestProxy = currentProxy;
                    latestRepoId = currentRepoId;
                }
            }
        }
        Assert.assertNotNull(latestProxy);
        return latestProxy;
    }

    /* -=-=- TEST NECESSITY OF LIGHT ADMINISTRATOR PRIVILEGES -=-=- */

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
        final EventContext normalUser = newUserAndGroup("rwr-r-");
        final long otherGroupId = newGroupAddUser("rwr-r-", normalUser.userId).getId().getValue();
        Folder folder = (Folder) iUpdate.saveAndReturnObject(mmFactory.simpleFolder());
        Assert.assertEquals(folder.getDetails().getGroup().getId().getValue(), normalUser.groupId);
        loginNewActor(isAdmin, isSudo ? loginNewAdmin(true, null).userName : null, isRestricted ? AdminPrivilegeChgrp.value : null);
        try (final AutoCloseable igc = new ImplicitGroupContext(otherGroupId)) {
            Assert.assertEquals(getCurrentPermissions(folder).canChgrp(), isExpectSuccess);
            doChange(client, factory, Requests.chgrp().target(folder).toGroup(otherGroupId).build(), isExpectSuccess);
            if (isExpectSuccess) {
                folder = (Folder) iQuery.get("Folder", folder.getId().getValue());
                Assert.assertEquals(folder.getDetails().getOwner().getId().getValue(), normalUser.userId);
                Assert.assertEquals(folder.getDetails().getGroup().getId().getValue(), otherGroupId);
            }
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
        final EventContext normalUser = newUserAndGroup("rwr-r-");
        final ExperimenterGroup otherGroup = newGroupAddUser("rwr-r-", normalUser.userId);
        Folder folder = (Folder) iUpdate.saveAndReturnObject(mmFactory.simpleFolder());
        Assert.assertEquals(folder.getDetails().getGroup().getId().getValue(), normalUser.groupId);
        loginNewActor(isAdmin, isSudo ? loginNewAdmin(true, null).userName : null, isRestricted ? AdminPrivilegeChgrp.value : null);
        try (final AutoCloseable igc = new ImplicitGroupContext(normalUser.groupId)) {
            Assert.assertEquals(getCurrentPermissions(folder).canChgrp(), isExpectSuccess);
            folder = (Folder) iQuery.get("Folder", folder.getId().getValue());
            folder.getDetails().setGroup(otherGroup);
            try {
                folder = (Folder) iUpdate.saveAndReturnObject(folder);
                Assert.assertEquals(folder.getDetails().getOwner().getId().getValue(), normalUser.userId);
                Assert.assertEquals(folder.getDetails().getGroup().getId().getValue(), otherGroup.getId().getValue());
                Assert.assertTrue(isExpectSuccess);
            } catch (ServerError se) {
                Assert.assertFalse(isExpectSuccess);
            }
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
        final EventContext normalUser = newUserAndGroup("rwr-r-");
        Folder folder = (Folder) iUpdate.saveAndReturnObject(mmFactory.simpleFolder());
        Assert.assertEquals(folder.getDetails().getOwner().getId().getValue(), normalUser.userId);
        final EventContext otherUser = newUserInGroup(normalUser);
        loginNewActor(isAdmin, isSudo ? loginNewAdmin(true, null).userName : null, isRestricted ? AdminPrivilegeChown.value : null);
        try (final AutoCloseable igc = new ImplicitGroupContext(normalUser.groupId)) {
            Assert.assertEquals(getCurrentPermissions(folder).canChown(), isExpectSuccess);
            doChange(client, factory, Requests.chown().target(folder).toUser(otherUser.userId).build(), isExpectSuccess);
            if (isExpectSuccess) {
                folder = (Folder) iQuery.get("Folder", folder.getId().getValue());
                Assert.assertEquals(folder.getDetails().getOwner().getId().getValue(), otherUser.userId);
                Assert.assertEquals(folder.getDetails().getGroup().getId().getValue(), normalUser.groupId);
            }
        }
    }

    /**
     * Test that users may give others' data only if they are a member of the <tt>system</tt> group and
     * have the <tt>Chown</tt> privilege. Attempts giving data via {@link omero.api.IUpdatePrx}.
     * @param isAdmin if to test a member of the <tt>system</tt> group
     * @param isRestricted if to test a user who does <em>not</em> have the <tt>Chown</tt> privilege
     * @param isSudo if to test attempt to subvert privilege by sudo to an unrestricted member of the <tt>system</tt> group
     * @throws Exception unexpected
     */
    @Test(dataProvider = "light administrator privilege test cases")
    public void testChownPrivilegeViaUpdate(boolean isAdmin, boolean isRestricted, boolean isSudo) throws Exception {
        final boolean isExpectSuccess = isAdmin && !isRestricted;
        final EventContext normalUser = newUserAndGroup("rwr-r-");
        Folder folder = (Folder) iUpdate.saveAndReturnObject(mmFactory.simpleFolder());
        Assert.assertEquals(folder.getDetails().getOwner().getId().getValue(), normalUser.userId);
        final EventContext otherUser = newUserInGroup(normalUser);
        loginNewActor(isAdmin, isSudo ? loginNewAdmin(true, null).userName : null, isRestricted ? AdminPrivilegeChown.value : null);
        try (final AutoCloseable igc = new ImplicitGroupContext(normalUser.groupId)) {
            Assert.assertEquals(getCurrentPermissions(folder).canChown(), isExpectSuccess);
            folder = (Folder) iQuery.get("Folder", folder.getId().getValue());
            folder.getDetails().setOwner(new ExperimenterI(otherUser.userId, false));
            try {
                folder = (Folder) iUpdate.saveAndReturnObject(folder);
                Assert.assertEquals(folder.getDetails().getOwner().getId().getValue(), otherUser.userId);
                Assert.assertEquals(folder.getDetails().getGroup().getId().getValue(), normalUser.groupId);
                Assert.assertTrue(isExpectSuccess);
            } catch (ServerError se) {
                Assert.assertFalse(isExpectSuccess);
            }
        }
    }

    /**
     * Test that users may delete other users' files only if they are a member of the <tt>system</tt> group and
     * have the <tt>DeleteFile</tt> privilege.
     * Attempts deletion of another user's file via {@link RepositoryPrx#deletePaths(String[], boolean, boolean)}.
     * @param isAdmin if to test a member of the <tt>system</tt> group
     * @param isRestricted if to test a user who does <em>not</em> have the <tt>DeleteFile</tt> privilege
     * @param isSudo if to test attempt to subvert privilege by sudo to an unrestricted member of the <tt>system</tt> group
     * @throws Exception unexpected
     */
    @Test(dataProvider = "light administrator privilege test cases")
    public void testDeleteFilePrivilegeDeletionViaRepo(boolean isAdmin, boolean isRestricted, boolean isSudo) throws Exception {
        final boolean isExpectSuccess = isAdmin && !isRestricted;
        final EventContext normalUser = newUserAndGroup("rwr-r-");
        /* upload the test script as a new script */
        final String testScriptName = "Test_" + getClass().getName() + '_' + UUID.randomUUID() + ".py";
        RepositoryPrx repo = getRepository(Repository.OTHER);
        final OriginalFile testScript = repo.register(testScriptName, omero.rtypes.rstring(ScriptServiceTest.PYTHON_MIMETYPE));
        final long testScriptId = testScript.getId().getValue();
        final byte[] fileContentOriginal = getPythonScript().getBytes(StandardCharsets.UTF_8);
        RawFileStorePrx rfs = repo.file(testScriptName, "rw");
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
        loginNewActor(isAdmin, isSudo ? loginNewAdmin(true, null).userName : null,
                isRestricted ? AdminPrivilegeDeleteFile.value : null);
        try (final AutoCloseable igc = new ImplicitGroupContext(normalUser.groupId)) {
            Assert.assertEquals(getCurrentPermissions(testScript).canDelete(), isExpectSuccess);
            repo = getRepository(Repository.OTHER);
            try {
                final HandlePrx handle = repo.deletePaths(new String[] {testScriptName}, false, false);
                final CmdCallbackI callback = new CmdCallbackI(client, handle);
                callback.loop(20, scalingFactor);
                assertCmd(callback, isExpectSuccess);
            } catch (Ice.LocalException ue) {
                Assert.assertFalse(isExpectSuccess);
            }
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
     * Test that users may delete other users' files only if they are a member of the <tt>system</tt> group and
     * have the <tt>DeleteFile</tt> privilege.
     * Attempts deletion of another user's file via {@link omero.cmd.Delete2}.
     * @param isAdmin if to test a member of the <tt>system</tt> group
     * @param isRestricted if to test a user who does <em>not</em> have the <tt>DeleteFile</tt> privilege
     * @param isSudo if to test attempt to subvert privilege by sudo to an unrestricted member of the <tt>system</tt> group
     * @throws Exception unexpected
     */
    @Test(dataProvider = "light administrator privilege test cases")
    public void testDeleteFilePrivilegeDeletionViaRequest(boolean isAdmin, boolean isRestricted, boolean isSudo) throws Exception {
        final boolean isExpectSuccess = isAdmin && !isRestricted;
        final EventContext normalUser = newUserAndGroup("rwr-r-");
        final OriginalFile file = (OriginalFile) iUpdate.saveAndReturnObject(mmFactory.createOriginalFile());
        loginNewActor(isAdmin, isSudo ? loginNewAdmin(true, null).userName : null,
                isRestricted ? AdminPrivilegeDeleteFile.value : null);
        try (final AutoCloseable igc = new ImplicitGroupContext(normalUser.groupId)) {
            Assert.assertEquals(getCurrentPermissions(file).canDelete(), isExpectSuccess);
            doChange(client, factory, Requests.delete().target(file).build(), isExpectSuccess);
        }
    }

    /**
     * Test that users may delete other users' files only if they are a member of the <tt>system</tt> group and
     * have the <tt>DeleteFile</tt> privilege.
     * Attempts deletion of another user's file via {@link IScriptPrx#deleteScript(long)}.
     * @param isAdmin if to test a member of the <tt>system</tt> group
     * @param isRestricted if to test a user who does <em>not</em> have the <tt>DeleteFile</tt> privilege
     * @param isSudo if to test attempt to subvert privilege by sudo to an unrestricted member of the <tt>system</tt> group
     * @throws Exception unexpected
     */
    @Test(dataProvider = "light administrator privilege test cases")
    public void testDeleteFilePrivilegeDeletionViaScripts(boolean isAdmin, boolean isRestricted, boolean isSudo) throws Exception {
        final boolean isExpectSuccess = isAdmin && !isRestricted;
        final EventContext normalUser = newUserAndGroup("rwr-r-");
        /* upload the test script as a new script */
        IScriptPrx iScript = factory.getScriptService();
        final String testScriptName = "Test_" + getClass().getName() + '_' + UUID.randomUUID() + ".py";
        final long testScriptId = iScript.uploadScript(testScriptName, getPythonScript());
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
        loginNewActor(isAdmin, isSudo ? loginNewAdmin(true, null).userName : null,
                isRestricted ? AdminPrivilegeDeleteFile.value : null);
        try (final AutoCloseable igc = new ImplicitGroupContext(normalUser.groupId)) {
            Assert.assertEquals(getCurrentPermissions(testScript).canDelete(), isExpectSuccess);
            iScript = factory.getScriptService();
            try {
                iScript.deleteScript(testScriptId);
                Assert.assertTrue(isExpectSuccess);
            } catch (ServerError se) {
                Assert.assertFalse(isExpectSuccess);
            }
        }
        /* check if the script was deleted or left intact */
        loginUser(normalUser);
        if (isExpectSuccess) {
            assertDoesNotExist(testScript);
        } else {
            assertExists(testScript);
        }
        final RawFileStorePrx rfs = factory.createRawFileStore();
        try {
            rfs.setFileId(testScriptId);
            final String currentScript = new String(rfs.read(0, (int) rfs.size()), StandardCharsets.UTF_8);
            Assert.assertEquals(currentScript, getPythonScript());
            Assert.assertFalse(isExpectSuccess);
        } catch (Ice.LocalException | ServerError se) {
            /* can catch only ServerError once RawFileStoreTest.testBadFileId is fixed */
            Assert.assertTrue(isExpectSuccess);
        } finally {
            rfs.close();
        }
    }

    /**
     * Test that users may delete other users' files only if they are a member of the <tt>system</tt> group and
     * have the <tt>DeleteManagedRepo</tt> privilege.
     * Attempts deletion of another user's file via {@link RepositoryPrx#deletePaths(String[], boolean, boolean)}.
     * @param isAdmin if to test a member of the <tt>system</tt> group
     * @param isRestricted if to test a user who does <em>not</em> have the <tt>DeleteManagedRepo</tt> privilege
     * @param isSudo if to test attempt to subvert privilege by sudo to an unrestricted member of the <tt>system</tt> group
     * @throws Exception unexpected
     */
    @Test(dataProvider = "light administrator privilege test cases")
    public void testDeleteManagedRepoPrivilegeDeletionViaRepo(boolean isAdmin, boolean isRestricted, boolean isSudo)
            throws Exception {
        /* import a fake image file as a normal user */
        final boolean isExpectSuccess = isAdmin && !isRestricted;
        final EventContext normalUser = newUserAndGroup("rwr-r-");
        final ImportLocation importLocation = importFileset(Collections.singletonList(fakeImageFile.getPath()));
        final RString imagePath = omero.rtypes.rstring(importLocation.sharedPath + FsFile.separatorChar);
        final RString imageName = omero.rtypes.rstring(fakeImageFile.getName());
        final OriginalFile remoteFile = (OriginalFile) iQuery.findByQuery(
                "FROM OriginalFile o WHERE o.path = :path AND o.name = :name AND o.details.group.id = :group_id",
                new ParametersI().add("path", imagePath).add("name", imageName).addLong("group_id", normalUser.groupId));
        /* delete the model objects related to the file */
        final Fileset fileset = (Fileset) iQuery.findByQuery(
                "SELECT fe.fileset FROM FilesetEntry fe WHERE fe.originalFile.id = :id",
                new ParametersI().addId(remoteFile.getId()));
        final ChildOption excludeFiles = Requests.option().excludeType(OriginalFile.class.getSimpleName()).build();
        doChange(Requests.delete().target(fileset).option(excludeFiles).build());
        /* try to delete the file */
        loginNewActor(isAdmin, isSudo ? loginNewAdmin(true, null).userName : null,
                isRestricted ? AdminPrivilegeDeleteManagedRepo.value : null);
        try (final AutoCloseable igc = new ImplicitGroupContext(normalUser.groupId)) {
            Assert.assertEquals(getCurrentPermissions(remoteFile).canDelete(), isExpectSuccess);
            final RepositoryPrx repo = getRepository(Repository.MANAGED);
            try {
                final String remoteFilename = remoteFile.getPath().getValue() + remoteFile.getName().getValue();
                final HandlePrx handle = repo.deletePaths(new String[] {remoteFilename}, false, false);
                final CmdCallbackI callback = new CmdCallbackI(client, handle);
                callback.loop(20, scalingFactor);
                assertCmd(callback, isExpectSuccess);
            } catch (Ice.LocalException ue) {
                Assert.assertFalse(isExpectSuccess);
            }
        }
        /* check the existence of the file */
        loginUser(normalUser);
        try {
            iQuery.get("OriginalFile", remoteFile.getId().getValue());
            Assert.assertFalse(isExpectSuccess);
        } catch (ServerError se) {
            Assert.assertTrue(isExpectSuccess);
        }
    }

    /**
     * Test that users may delete other users' files only if they are a member of the <tt>system</tt> group and
     * have the <tt>DeleteManagedRepo</tt> privilege.
     * Attempts deletion of another user's file via {@link omero.cmd.Delete2}.
     * @param isAdmin if to test a member of the <tt>system</tt> group
     * @param isRestricted if to test a user who does <em>not</em> have the <tt>DeleteManagedRepo</tt> privilege
     * @param isSudo if to test attempt to subvert privilege by sudo to an unrestricted member of the <tt>system</tt> group
     * @throws Exception unexpected
     */
    @Test(dataProvider = "light administrator privilege test cases")
    public void testDeleteManagedRepoPrivilegeDeletionViaRequest(boolean isAdmin, boolean isRestricted, boolean isSudo)
            throws Exception {
        /* import a fake image file as a normal user */
        final boolean isExpectSuccess = isAdmin && !isRestricted;
        final EventContext normalUser = newUserAndGroup("rwr-r-");
        final ImportLocation importLocation = importFileset(Collections.singletonList(fakeImageFile.getPath()));
        final RString imagePath = omero.rtypes.rstring(importLocation.sharedPath + FsFile.separatorChar);
        final RString imageName = omero.rtypes.rstring(fakeImageFile.getName());
        final OriginalFile remoteFile = (OriginalFile) iQuery.findByQuery(
                "FROM OriginalFile o WHERE o.path = :path AND o.name = :name AND o.details.group.id = :group_id",
                new ParametersI().add("path", imagePath).add("name", imageName).addLong("group_id", normalUser.groupId));
        /* delete the model objects related to the file */
        final Fileset fileset = (Fileset) iQuery.findByQuery(
                "SELECT fe.fileset FROM FilesetEntry fe WHERE fe.originalFile.id = :id",
                new ParametersI().addId(remoteFile.getId()));
        final ChildOption excludeFiles = Requests.option().excludeType(OriginalFile.class.getSimpleName()).build();
        doChange(Requests.delete().target(fileset).option(excludeFiles).build());
        /* try to delete the file */
        loginNewActor(isAdmin, isSudo ? loginNewAdmin(true, null).userName : null,
                isRestricted ? AdminPrivilegeDeleteManagedRepo.value : null);
        try (final AutoCloseable igc = new ImplicitGroupContext(normalUser.groupId)) {
            Assert.assertEquals(getCurrentPermissions(remoteFile).canDelete(), isExpectSuccess);
            doChange(client, factory, Requests.delete().target(remoteFile).build(), isExpectSuccess);
        }
    }

    /**
     * Test that users may delete other users' data only if they are a member of the <tt>system</tt> group and
     * have the <tt>DeleteOwned</tt> privilege. Attempts deletion of another user's data.
     * @param isAdmin if to test a member of the <tt>system</tt> group
     * @param isRestricted if to test a user who does <em>not</em> have the <tt>DeleteOwned</tt> privilege
     * @param isSudo if to test attempt to subvert privilege by sudo to an unrestricted member of the <tt>system</tt> group
     * @throws Exception unexpected
     */
    @Test(dataProvider = "light administrator privilege test cases")
    public void testDeleteOwnedPrivilegeDeletion(boolean isAdmin, boolean isRestricted, boolean isSudo) throws Exception {
        final boolean isExpectSuccess = isAdmin && !isRestricted;
        final EventContext normalUser = newUserAndGroup("rwr-r-");
        final Folder folder = (Folder) iUpdate.saveAndReturnObject(mmFactory.simpleFolder());
        loginNewActor(isAdmin, isSudo ? loginNewAdmin(true, null).userName : null,
                isRestricted ? AdminPrivilegeDeleteOwned.value : null);
        try (final AutoCloseable igc = new ImplicitGroupContext(normalUser.groupId)) {
            Assert.assertEquals(getCurrentPermissions(folder).canDelete(), isExpectSuccess);
            doChange(client, factory, Requests.delete().target(folder).build(), isExpectSuccess);
        }
    }

    /**
     * Test that users may delete official scripts only if they are a member of the <tt>system</tt> group and
     * have the <tt>DeleteScriptRepo</tt> privilege.
     * Attempts deletion of another user's file via {@link RepositoryPrx#deletePaths(String[], boolean, boolean)}.
     * @param isAdmin if to test a member of the <tt>system</tt> group
     * @param isRestricted if to test a user who does <em>not</em> have the <tt>DeleteScriptRepo</tt> privilege
     * @param isSudo if to test attempt to subvert privilege by sudo to an unrestricted member of the <tt>system</tt> group
     * @throws Exception unexpected
     */
    @Test(dataProvider = "light administrator privilege test cases")
    public void testDeleteScriptRepoPrivilegeDeletionViaRepo(boolean isAdmin, boolean isRestricted, boolean isSudo)
            throws Exception {
        final boolean isExpectSuccess = isAdmin && !isRestricted;
        final EventContext normalUser = newUserAndGroup("rwr-r-");
        /* upload the test script as a new script */
        final String testScriptName = "Test_" + getClass().getName() + '_' + UUID.randomUUID() + ".py";
        RepositoryPrx repo = getRepository(Repository.SCRIPT);
        final OriginalFile testScript = repo.register(testScriptName, omero.rtypes.rstring(ScriptServiceTest.PYTHON_MIMETYPE));
        final long testScriptId = testScript.getId().getValue();
        final byte[] fileContentOriginal = getPythonScript().getBytes(StandardCharsets.UTF_8);
        RawFileStorePrx rfs = repo.file(testScriptName, "rw");
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
        loginNewActor(isAdmin, isSudo ? loginNewAdmin(true, null).userName : null,
                isRestricted ? AdminPrivilegeDeleteScriptRepo.value : null);
        try (final AutoCloseable igc = new ImplicitGroupContext(normalUser.groupId)) {
            Assert.assertEquals(getCurrentPermissions(testScript).canDelete(), isExpectSuccess);
            repo = getRepository(Repository.SCRIPT);
            try {
                final HandlePrx handle = repo.deletePaths(new String[] {testScriptName}, false, false);
                final CmdCallbackI callback = new CmdCallbackI(client, handle);
                callback.loop(20, scalingFactor);
                assertCmd(callback, isExpectSuccess);
            } catch (Ice.LocalException ue) {
                Assert.assertFalse(isExpectSuccess);
            }
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
     * Test that users may delete official scripts only if they are a member of the <tt>system</tt> group and
     * have the <tt>DeleteScriptRepo</tt> privilege.
     * Attempts deletion of another user's file via {@link omero.cmd.Delete2}.
     * @param isAdmin if to test a member of the <tt>system</tt> group
     * @param isRestricted if to test a user who does <em>not</em> have the <tt>DeleteScriptRepo</tt> privilege
     * @param isSudo if to test attempt to subvert privilege by sudo to an unrestricted member of the <tt>system</tt> group
     * @throws Exception unexpected
     */
    @Test(dataProvider = "light administrator privilege test cases")
    public void testDeleteScriptRepoPrivilegeDeletionViaRequest(boolean isAdmin, boolean isRestricted, boolean isSudo)
            throws Exception {
        final boolean isExpectSuccess = isAdmin && !isRestricted;
        final EventContext normalUser = newUserAndGroup("rwr-r-");
        /* upload the test script as a new script */
        final String testScriptName = "Test_" + getClass().getName() + '_' + UUID.randomUUID() + ".py";
        RepositoryPrx repo = getRepository(Repository.SCRIPT);
        final OriginalFile testScript = repo.register(testScriptName, omero.rtypes.rstring(ScriptServiceTest.PYTHON_MIMETYPE));
        final long testScriptId = testScript.getId().getValue();
        final byte[] fileContentOriginal = getPythonScript().getBytes(StandardCharsets.UTF_8);
        RawFileStorePrx rfs = repo.file(testScriptName, "rw");
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
        loginNewActor(isAdmin, isSudo ? loginNewAdmin(true, null).userName : null,
                isRestricted ? AdminPrivilegeDeleteScriptRepo.value : null);
        try (final AutoCloseable igc = new ImplicitGroupContext(normalUser.groupId)) {
            Assert.assertEquals(getCurrentPermissions(testScript).canDelete(), isExpectSuccess);
            doChange(client, factory, Requests.delete().target(testScript).build(), isExpectSuccess);
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
     * Test that users may delete official scripts only if they are a member of the <tt>system</tt> group and
     * have the <tt>DeleteScriptRepo</tt> privilege.
     * Attempts deletion of another user's file via {@link IScriptPrx#deleteScript(long)}.
     * @param isAdmin if to test a member of the <tt>system</tt> group
     * @param isRestricted if to test a user who does <em>not</em> have the <tt>DeleteScriptRepo</tt> privilege
     * @param isSudo if to test attempt to subvert privilege by sudo to an unrestricted member of the <tt>system</tt> group
     * @throws Exception unexpected
     */
    @Test(dataProvider = "light administrator privilege test cases")
    public void testDeleteScriptRepoPrivilegeDeletionViaScripts(boolean isAdmin, boolean isRestricted, boolean isSudo)
            throws Exception {
        final boolean isExpectSuccess = isAdmin && !isRestricted;
        final EventContext normalUser = newUserAndGroup("rwr-r-");
        loginNewAdmin(true, null);
        /* upload the test script as a new script */
        IScriptPrx iScript = factory.getScriptService();
        final String testScriptName = "Test_" + getClass().getName() + '_' + UUID.randomUUID() + ".py";
        final long testScriptId = iScript.uploadOfficialScript(testScriptName, getPythonScript());
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
        loginNewActor(isAdmin, isSudo ? loginNewAdmin(true, null).userName : null,
                isRestricted ? AdminPrivilegeDeleteScriptRepo.value : null);
        try (final AutoCloseable igc = new ImplicitGroupContext(normalUser.groupId)) {
            Assert.assertEquals(getCurrentPermissions(testScript).canDelete(), isExpectSuccess);
            iScript = factory.getScriptService();
            try {
                iScript.deleteScript(testScriptId);
                Assert.assertTrue(isExpectSuccess);
            } catch (ServerError se) {
                Assert.assertFalse(isExpectSuccess);
            }
        }
        /* check if the script was deleted or left intact */
        loginUser(normalUser);
        if (isExpectSuccess) {
            assertDoesNotExist(testScript);
        } else {
            assertExists(testScript);
        }
        final RawFileStorePrx rfs = factory.createRawFileStore();
        try {
            rfs.setFileId(testScriptId);
            final String currentScript = new String(rfs.read(0, (int) rfs.size()), StandardCharsets.UTF_8);
            Assert.assertEquals(currentScript, getPythonScript());
            Assert.assertFalse(isExpectSuccess);
        } catch (Ice.LocalException | ServerError se) {
            /* can catch only ServerError once RawFileStoreTest.testBadFileId is fixed */
            Assert.assertTrue(isExpectSuccess);
        } finally {
            rfs.close();
        }
    }

    /**
     * Test that users may modify groups only if they are a member of the <tt>system</tt> group and
     * have the <tt>ModifyGroup</tt> privilege. Attempts creation of new group via {@link omero.api.IAdminPrx}.
     * @param isAdmin if to test a member of the <tt>system</tt> group
     * @param isRestricted if to test a user who does <em>not</em> have the <tt>ModifyGroup</tt> privilege
     * @param isSudo if to test attempt to subvert privilege by sudo to an unrestricted member of the <tt>system</tt> group
     * @throws Exception unexpected
     */
    @Test(dataProvider = "light administrator privilege test cases")
    public void testModifyGroupPrivilegeCreationViaAdmin(boolean isAdmin, boolean isRestricted, boolean isSudo) throws Exception {
        final boolean isExpectSuccess = isAdmin && !isRestricted;
        loginNewActor(isAdmin, isSudo ? loginNewAdmin(true, null).userName : null,
                isRestricted ? AdminPrivilegeModifyGroup.value : null);
        final ExperimenterGroup newGroup = new ExperimenterGroupI();
        newGroup.setLdap(omero.rtypes.rbool(false));
        newGroup.setName(omero.rtypes.rstring(UUID.randomUUID().toString()));
        try {
            iAdmin.createGroup(newGroup);
            Assert.assertTrue(isExpectSuccess);
        } catch (ServerError se) {
            Assert.assertFalse(isExpectSuccess);
        }
    }

    /**
     * Test that users may modify groups only if they are a member of the <tt>system</tt> group and
     * have the <tt>ModifyGroup</tt> privilege. Attempts creation of new group via {@link omero.api.IUpdatePrx}.
     * @param isAdmin if to test a member of the <tt>system</tt> group
     * @param isRestricted if to test a user who does <em>not</em> have the <tt>ModifyGroup</tt> privilege
     * @param isSudo if to test attempt to subvert privilege by sudo to an unrestricted member of the <tt>system</tt> group
     * @throws Exception unexpected
     */
    @Test(dataProvider = "light administrator privilege test cases")
    public void testModifyGroupPrivilegeCreationViaUpdate(boolean isAdmin, boolean isRestricted, boolean isSudo) throws Exception {
        final boolean isExpectSuccess = isAdmin && !isRestricted;
        loginNewActor(isAdmin, isSudo ? loginNewAdmin(true, null).userName : null,
                isRestricted ? AdminPrivilegeModifyGroup.value : null);
        final ExperimenterGroup newGroup = new ExperimenterGroupI();
        newGroup.setLdap(omero.rtypes.rbool(false));
        newGroup.setName(omero.rtypes.rstring(UUID.randomUUID().toString()));
        try {
            iUpdate.saveObject(newGroup);
            Assert.assertTrue(isExpectSuccess);
        } catch (ServerError se) {
            Assert.assertFalse(isExpectSuccess);
        }
    }

    /**
     * Test that users may modify groups only if they are a member of the <tt>system</tt> group and
     * have the <tt>ModifyGroup</tt> privilege.
     * Attempts change of existing group via {@link omero.api.IAdminPrx#updateGroup(ExperimenterGroup)}.
     * @param isAdmin if to test a member of the <tt>system</tt> group
     * @param isRestricted if to test a user who does <em>not</em> have the <tt>ModifyGroup</tt> privilege
     * @param isSudo if to test attempt to subvert privilege by sudo to an unrestricted member of the <tt>system</tt> group
     * @throws Exception unexpected
     */
    @Test(dataProvider = "light administrator privilege test cases")
    public void testModifyGroupPrivilegeEditingViaAdmin(boolean isAdmin, boolean isRestricted, boolean isSudo)
            throws Exception {
        final boolean isExpectSuccess = isAdmin && !isRestricted;
        final long newGroupId = newUserAndGroup("rwr-r-").groupId;
        loginNewActor(isAdmin, isSudo ? loginNewAdmin(true, null).userName : null,
                isRestricted ? AdminPrivilegeModifyGroup.value : null);
        final ExperimenterGroup newGroup = (ExperimenterGroup) iQuery.get("ExperimenterGroup", newGroupId);
        newGroup.setLdap(omero.rtypes.rbool(true));
        try {
            iAdmin.updateGroup(newGroup);
            Assert.assertTrue(isExpectSuccess);
        } catch (ServerError se) {
            Assert.assertFalse(isExpectSuccess);
        }
    }

    /**
     * Test that users may modify groups only if they are a member of the <tt>system</tt> group and
     * have the <tt>ModifyGroup</tt> privilege. Attempts change of existing group via {@link omero.api.IUpdatePrx}.
     * @param isAdmin if to test a member of the <tt>system</tt> group
     * @param isRestricted if to test a user who does <em>not</em> have the <tt>ModifyGroup</tt> privilege
     * @param isSudo if to test attempt to subvert privilege by sudo to an unrestricted member of the <tt>system</tt> group
     * @throws Exception unexpected
     */
    @Test(dataProvider = "light administrator privilege test cases")
    public void testModifyGroupPrivilegeEditingViaUpdate(boolean isAdmin, boolean isRestricted, boolean isSudo) throws Exception {
        final boolean isExpectSuccess = isAdmin && !isRestricted;
        final long newGroupId = newUserAndGroup("rwr-r-").groupId;
        loginNewActor(isAdmin, isSudo ? loginNewAdmin(true, null).userName : null,
                isRestricted ? AdminPrivilegeModifyGroup.value : null);
        final ExperimenterGroup newGroup = (ExperimenterGroup) iQuery.get("ExperimenterGroup", newGroupId);
        Assert.assertEquals(getCurrentPermissions(newGroup).canEdit(), isExpectSuccess);
        newGroup.setLdap(omero.rtypes.rbool(true));
        try {
            iUpdate.saveObject(newGroup);
            Assert.assertTrue(isExpectSuccess);
        } catch (ServerError se) {
            Assert.assertFalse(isExpectSuccess);
        }
    }

    /**
     * Test that users may modify group membership only if they are a member of the <tt>system</tt> group and
     * have the <tt>ModifyGroupMembership</tt> privilege.
     * Attempts change of existing group membership via {@link omero.api.IAdminPrx#addGroups(Experimenter, List)}.
     * @param isAdmin if to test a member of the <tt>system</tt> group
     * @param isRestricted if to test a user who does <em>not</em> have the <tt>ModifyGroupMembership</tt> privilege
     * @param isSudo if to test attempt to subvert privilege by sudo to an unrestricted member of the <tt>system</tt> group
     * @throws Exception unexpected
     */
    @Test(dataProvider = "light administrator privilege test cases")
    public void testModifyGroupMembershipPrivilegeCreationViaAdmin(boolean isAdmin, boolean isRestricted, boolean isSudo)
            throws Exception {
        final boolean isExpectSuccess = isAdmin && !isRestricted;
        final EventContext normalUser = newUserAndGroup("rwr-r-");
        final EventContext otherUser = newUserAndGroup("rwr-r-");
        loginNewActor(isAdmin, isSudo ? loginNewAdmin(true, null).userName : null,
                isRestricted ? AdminPrivilegeModifyGroupMembership.value : null);
        try {
            final Experimenter user = new ExperimenterI(normalUser.userId, false);
            final ExperimenterGroup group = new ExperimenterGroupI(otherUser.groupId, false);
            iAdmin.addGroups(user, Collections.singletonList(group));
            Assert.assertTrue(isExpectSuccess);
        } catch (ServerError se) {
            Assert.assertFalse(isExpectSuccess);
        }
    }

    /**
     * Test that users may modify group membership only if they are a member of the <tt>system</tt> group and
     * have the <tt>ModifyGroupMembership</tt> privilege.
     * Attempts change of existing group membership via {@link omero.api.IAdminPrx#setGroupOwner(ExperimenterGroup, Experimenter)}.
     * @param isAdmin if to test a member of the <tt>system</tt> group
     * @param isRestricted if to test a user who does <em>not</em> have the <tt>ModifyGroupMembership</tt> privilege
     * @param isSudo if to test attempt to subvert privilege by sudo to an unrestricted member of the <tt>system</tt> group
     * @throws Exception unexpected
     */
    @Test(dataProvider = "light administrator privilege test cases")
    public void testModifyGroupMembershipPrivilegeEditingViaAdminSetOwner(boolean isAdmin, boolean isRestricted, boolean isSudo)
            throws Exception {
        final boolean isExpectSuccess = isAdmin && !isRestricted;
        final EventContext normalUser = newUserAndGroup("rwr-r-");
        loginNewActor(isAdmin, isSudo ? loginNewAdmin(true, null).userName : null,
                isRestricted ? AdminPrivilegeModifyGroupMembership.value : null);
        try {
            final Experimenter user = new ExperimenterI(normalUser.userId, false);
            final ExperimenterGroup group = new ExperimenterGroupI(normalUser.groupId, false);
            iAdmin.setGroupOwner(group, user);
            Assert.assertTrue(isExpectSuccess);
        } catch (ServerError se) {
            Assert.assertFalse(isExpectSuccess);
        }
    }

    /**
     * Test that users may modify group membership only if they are a member of the <tt>system</tt> group and
     * have the <tt>ModifyGroupMembership</tt> privilege.
     * Attempts change of existing group membership via
     * {@link omero.api.IAdminPrx#unsetGroupOwner(ExperimenterGroup, Experimenter)}.
     * @param isAdmin if to test a member of the <tt>system</tt> group
     * @param isRestricted if to test a user who does <em>not</em> have the <tt>ModifyGroupMembership</tt> privilege
     * @param isSudo if to test attempt to subvert privilege by sudo to an unrestricted member of the <tt>system</tt> group
     * @throws Exception unexpected
     */
    @Test(dataProvider = "light administrator privilege test cases")
    public void testModifyGroupMembershipPrivilegeEditingViaAdminUnsetOwner(boolean isAdmin, boolean isRestricted, boolean isSudo)
            throws Exception {
        final boolean isExpectSuccess = isAdmin && !isRestricted;
        final EventContext normalUser = newUserAndGroup("rwr-r-", true);
        loginNewActor(isAdmin, isSudo ? loginNewAdmin(true, null).userName : null,
                isRestricted ? AdminPrivilegeModifyGroupMembership.value : null);
        try {
            final Experimenter user = new ExperimenterI(normalUser.userId, false);
            final ExperimenterGroup group = new ExperimenterGroupI(normalUser.groupId, false);
            iAdmin.unsetGroupOwner(group, user);
            Assert.assertTrue(isExpectSuccess);
        } catch (ServerError se) {
            Assert.assertFalse(isExpectSuccess);
        }
    }

    /**
     * Test that users may modify group membership only if they are a member of the <tt>system</tt> group and
     * have the <tt>ModifyGroupMembership</tt> privilege.
     * Attempts change of existing group membership via {@link omero.api.IUpdatePrx}.
     * @param isAdmin if to test a member of the <tt>system</tt> group
     * @param isRestricted if to test a user who does <em>not</em> have the <tt>ModifyGroupMembership</tt> privilege
     * @param isSudo if to test attempt to subvert privilege by sudo to an unrestricted member of the <tt>system</tt> group
     * @throws Exception unexpected
     */
    @Test(dataProvider = "light administrator privilege test cases")
    public void testModifyGroupMembershipPrivilegeEditingViaUpdateGroup(boolean isAdmin, boolean isRestricted, boolean isSudo)
            throws Exception {
        final boolean isExpectSuccess = isAdmin && !isRestricted;
        final EventContext normalUser = newUserAndGroup("rwr-r-");
        final long newGroupId = newUserAndGroup("rwr-r-").groupId;
        loginNewActor(isAdmin, isSudo ? loginNewAdmin(true, null).userName : null,
                isRestricted ? AdminPrivilegeModifyGroupMembership.value : null);
        final GroupExperimenterMap link = (GroupExperimenterMap) iQuery.findByQuery(
                "FROM GroupExperimenterMap WHERE parent.id = :group_id AND child.id = :user_id",
                new ParametersI().addLong("group_id", normalUser.groupId).addLong("user_id", normalUser.userId));
        Assert.assertEquals(getCurrentPermissions(link).canEdit(), isExpectSuccess);
        try {
            link.setParent(new ExperimenterGroupI(newGroupId, false));
            iUpdate.saveObject(link);
            Assert.assertTrue(isExpectSuccess);
        } catch (ServerError se) {
            Assert.assertFalse(isExpectSuccess);
        }
    }

    /**
     * Test that users may modify group membership only if they are a member of the <tt>system</tt> group and
     * have the <tt>ModifyGroupMembership</tt> privilege.
     * Attempts change of existing group membership via {@link omero.api.IUpdatePrx}.
     * @param isAdmin if to test a member of the <tt>system</tt> group
     * @param isRestricted if to test a user who does <em>not</em> have the <tt>ModifyGroupMembership</tt> privilege
     * @param isSudo if to test attempt to subvert privilege by sudo to an unrestricted member of the <tt>system</tt> group
     * @throws Exception unexpected
     */
    @Test(dataProvider = "light administrator privilege test cases")
    public void testModifyGroupMembershipPrivilegeEditingViaUpdateOwner(boolean isAdmin, boolean isRestricted, boolean isSudo)
            throws Exception {
        final boolean isExpectSuccess = isAdmin && !isRestricted;
        final EventContext normalUser = newUserAndGroup("rwr-r-");
        loginNewActor(isAdmin, isSudo ? loginNewAdmin(true, null).userName : null,
                isRestricted ? AdminPrivilegeModifyGroupMembership.value : null);
        final GroupExperimenterMap link = (GroupExperimenterMap) iQuery.findByQuery(
                "FROM GroupExperimenterMap WHERE parent.id = :group_id AND child.id = :user_id",
                new ParametersI().addLong("group_id", normalUser.groupId).addLong("user_id", normalUser.userId));
        Assert.assertEquals(getCurrentPermissions(link).canEdit(), isExpectSuccess);
        try {
            link.setOwner(omero.rtypes.rbool(true));
            iUpdate.saveObject(link);
            Assert.assertTrue(isExpectSuccess);
        } catch (ServerError se) {
            Assert.assertFalse(isExpectSuccess);
        }
    }

    /**
     * Test that users may modify group membership only if they are a member of the <tt>system</tt> group and
     * have the <tt>ModifyGroupMembership</tt> privilege.
     * Attempts change of existing group membership via {@link omero.api.IAdminPrx#removeGroups(Experimenter, List)}.
     * @param isAdmin if to test a member of the <tt>system</tt> group
     * @param isRestricted if to test a user who does <em>not</em> have the <tt>ModifyGroupMembership</tt> privilege
     * @param isSudo if to test attempt to subvert privilege by sudo to an unrestricted member of the <tt>system</tt> group
     * @throws Exception unexpected
     */
    @Test(dataProvider = "light administrator privilege test cases")
    public void testModifyGroupMembershipPrivilegeDeletionViaAdmin(boolean isAdmin, boolean isRestricted, boolean isSudo)
            throws Exception {
        final boolean isExpectSuccess = isAdmin && !isRestricted;
        final EventContext normalUser = newUserAndGroup("rwr-r-");
        final ExperimenterGroup otherGroup = newGroupAddUser("rwr-r-", normalUser.userId);
        loginNewActor(isAdmin, isSudo ? loginNewAdmin(true, null).userName : null,
                isRestricted ? AdminPrivilegeModifyGroupMembership.value : null);
        try {
            final Experimenter user = new ExperimenterI(normalUser.userId, false);
            iAdmin.removeGroups(user, Collections.singletonList(otherGroup));
            Assert.assertTrue(isExpectSuccess);
        } catch (ServerError se) {
            Assert.assertFalse(isExpectSuccess);
        }
    }

    /**
     * Test that users may modify group membership only if they are a member of the <tt>system</tt> group and
     * have the <tt>ModifyGroupMembership</tt> privilege.
     * Attempts change of existing group membership via {@link omero.cmd.Delete2}.
     * @param isAdmin if to test a member of the <tt>system</tt> group
     * @param isRestricted if to test a user who does <em>not</em> have the <tt>ModifyGroupMembership</tt> privilege
     * @param isSudo if to test attempt to subvert privilege by sudo to an unrestricted member of the <tt>system</tt> group
     * @throws Exception unexpected
     */
    @Test(dataProvider = "light administrator privilege test cases")
    public void testModifyGroupMembershipPrivilegeDeletionViaRequest(boolean isAdmin, boolean isRestricted, boolean isSudo)
            throws Exception {
        final boolean isExpectSuccess = isAdmin && !isRestricted;
        final EventContext normalUser = newUserAndGroup("rwr-r-");
        final ExperimenterGroup otherGroup = newGroupAddUser("rwr-r-", normalUser.userId);
        loginNewActor(isAdmin, isSudo ? loginNewAdmin(true, null).userName : null,
                isRestricted ? AdminPrivilegeModifyGroupMembership.value : null);
        final GroupExperimenterMap link = (GroupExperimenterMap) iQuery.findByQuery(
                "FROM GroupExperimenterMap WHERE parent.id = :group_id AND child.id = :user_id",
                new ParametersI().addLong("group_id", otherGroup.getId()).addLong("user_id", normalUser.userId));
        Assert.assertEquals(getCurrentPermissions(link).canDelete(), isExpectSuccess);
        doChange(client, factory, Requests.delete().target(link).build(), isExpectSuccess);
    }

    /**
     * Test that users may modify other users only if they are a member of the <tt>system</tt> group and
     * have the <tt>ModifyUser</tt> privilege. Attempts creation of new user via {@link omero.api.IAdminPrx}.
     * @param isAdmin if to test a member of the <tt>system</tt> group
     * @param isRestricted if to test a user who does <em>not</em> have the <tt>ModifyUser</tt> privilege
     * @param isSudo if to test attempt to subvert privilege by sudo to an unrestricted member of the <tt>system</tt> group
     * @throws Exception unexpected
     */
    @Test(dataProvider = "light administrator privilege test cases")
    public void testModifyUserPrivilegeCreationViaAdmin(boolean isAdmin, boolean isRestricted, boolean isSudo) throws Exception {
        final boolean isExpectSuccess = isAdmin && !isRestricted;
        final long newGroupId = newUserAndGroup("rwr-r-").groupId;
        loginNewActor(isAdmin, isSudo ? loginNewAdmin(true, null).userName : null,
                isRestricted ? AdminPrivilegeModifyUser.value : null);
        final Experimenter newUser = createExperimenterI(UUID.randomUUID().toString(), getClass().getSimpleName(), "Test");
        try {
            final ExperimenterGroup userGroup = new ExperimenterGroupI(roles.userGroupId, false);
            iAdmin.createExperimenter(newUser, new ExperimenterGroupI(newGroupId, false), Collections.singletonList(userGroup));
            Assert.assertTrue(isExpectSuccess);
        } catch (ServerError se) {
            Assert.assertFalse(isExpectSuccess);
        }
    }

    /**
     * Test that users may modify other users only if they are a member of the <tt>system</tt> group and
     * have the <tt>ModifyUser</tt> privilege. Attempts creation of new user via {@link omero.api.IUpdatePrx}.
     * @param isAdmin if to test a member of the <tt>system</tt> group
     * @param isRestricted if to test a user who does <em>not</em> have the <tt>ModifyUser</tt> privilege
     * @param isSudo if to test attempt to subvert privilege by sudo to an unrestricted member of the <tt>system</tt> group
     * @throws Exception unexpected
     */
    @Test(dataProvider = "light administrator privilege test cases")
    public void testModifyUserPrivilegeCreationViaUpdate(boolean isAdmin, boolean isRestricted, boolean isSudo) throws Exception {
        final boolean isExpectSuccess = isAdmin && !isRestricted;
        final long newGroupId = newUserAndGroup("rwr-r-").groupId;
        loginNewActor(isAdmin, isSudo ? loginNewAdmin(true, null).userName : null,
                isRestricted ? AdminPrivilegeModifyUser.value : null);
        final Experimenter newUser = createExperimenterI(UUID.randomUUID().toString(), getClass().getSimpleName(), "Test");
        GroupExperimenterMapI link = new GroupExperimenterMapI();
        link.setParent(new ExperimenterGroupI(newGroupId, false));
        link.setChild(newUser);
        link.setOwner(omero.rtypes.rbool(false));
        newUser.addGroupExperimenterMap(link);
        link  = new GroupExperimenterMapI();
        link.setParent(new ExperimenterGroupI(roles.userGroupId, false));
        link.setChild(newUser);
        link.setOwner(omero.rtypes.rbool(false));
        newUser.addGroupExperimenterMap(link);
        try {
            iUpdate.saveObject(newUser);
            Assert.assertTrue(isExpectSuccess);
        } catch (ServerError se) {
            Assert.assertFalse(isExpectSuccess);
        }
    }

    /**
     * Test that users may modify other users only if they are a member of the <tt>system</tt> group and
     * have the <tt>ModifyUser</tt> privilege.
     * Attempts change of existing user via {@link omero.api.IAdminPrx#setDefaultGroup(Experimenter, ExperimenterGroup)}.
     * @param isAdmin if to test a member of the <tt>system</tt> group
     * @param isRestricted if to test a user who does <em>not</em> have the <tt>ModifyUser</tt> privilege
     * @param isSudo if to test attempt to subvert privilege by sudo to an unrestricted member of the <tt>system</tt> group
     * @throws Exception unexpected
     */
    @Test(dataProvider = "light administrator privilege test cases")
    public void testModifyUserPrivilegeEditingViaAdminSetGroup(boolean isAdmin, boolean isRestricted, boolean isSudo)
            throws Exception {
        final boolean isExpectSuccess = isAdmin && !isRestricted;
        final long newUserId = newUserAndGroup("rwr-r-").userId;
        final ExperimenterGroup otherGroup = newGroupAddUser("rwr-r-", newUserId);
        loginNewActor(isAdmin, isSudo ? loginNewAdmin(true, null).userName : null,
                isRestricted ? AdminPrivilegeModifyUser.value : null);
        try {
            iAdmin.setDefaultGroup(new ExperimenterI(newUserId, false), otherGroup);
            Assert.assertTrue(isExpectSuccess);
        } catch (ServerError se) {
            Assert.assertFalse(isExpectSuccess);
        }
    }

    /**
     * Test that users may modify other users only if they are a member of the <tt>system</tt> group and
     * have the <tt>ModifyUser</tt> privilege.
     * Attempts change of existing user via {@link omero.api.IAdminPrx#updateExperimenter(Experimenter)}.
     * @param isAdmin if to test a member of the <tt>system</tt> group
     * @param isRestricted if to test a user who does <em>not</em> have the <tt>ModifyUser</tt> privilege
     * @param isSudo if to test attempt to subvert privilege by sudo to an unrestricted member of the <tt>system</tt> group
     * @throws Exception unexpected
     */
    @Test(dataProvider = "light administrator privilege test cases")
    public void testModifyUserPrivilegeEditingViaAdminUpdate(boolean isAdmin, boolean isRestricted, boolean isSudo)
            throws Exception {
        final boolean isExpectSuccess = isAdmin && !isRestricted;
        final long newUserId = newUserAndGroup("rwr-r-").userId;
        loginNewActor(isAdmin, isSudo ? loginNewAdmin(true, null).userName : null,
                isRestricted ? AdminPrivilegeModifyUser.value : null);
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
     * Test that users may modify other users only if they are a member of the <tt>system</tt> group and
     * have the <tt>ModifyUser</tt> privilege. Attempts change of existing user via {@link omero.api.IUpdatePrx}.
     * @param isAdmin if to test a member of the <tt>system</tt> group
     * @param isRestricted if to test a user who does <em>not</em> have the <tt>ModifyUser</tt> privilege
     * @param isSudo if to test attempt to subvert privilege by sudo to an unrestricted member of the <tt>system</tt> group
     * @throws Exception unexpected
     */
    @Test(dataProvider = "light administrator privilege test cases")
    public void testModifyUserPrivilegeEditingViaUpdate(boolean isAdmin, boolean isRestricted, boolean isSudo) throws Exception {
        final boolean isExpectSuccess = isAdmin && !isRestricted;
        final long newUserId = newUserAndGroup("rwr-r-").userId;
        loginNewActor(isAdmin, isSudo ? loginNewAdmin(true, null).userName : null,
                isRestricted ? AdminPrivilegeModifyUser.value : null);
        final Experimenter newUser = (Experimenter) iQuery.get("Experimenter", newUserId);
        Assert.assertEquals(getCurrentPermissions(newUser).canEdit(), isExpectSuccess);
        newUser.setConfig(ImmutableList.of(new NamedValue("color", "green")));
        try {
            iUpdate.saveObject(newUser);
            Assert.assertTrue(isExpectSuccess);
        } catch (ServerError se) {
            Assert.assertFalse(isExpectSuccess);
        }
    }

    /**
     * Test that users read others' sessions only if they are a member of the <tt>system</tt> group and
     * have the <tt>ReadSession</tt> privilege. Attempts reading via {@link omero.api.IQueryPrx#find(String, long)}
     * and {@link omero.api.IQueryPrx#projection(String, omero.sys.Parameters)}.
     * @param isAdmin if to test a member of the <tt>system</tt> group
     * @param isRestricted if to test a user who does <em>not</em> have the <tt>ReadSession</tt> privilege
     * @param isSudo if to test attempt to subvert privilege by sudo to an unrestricted member of the <tt>system</tt> group
     * @throws Exception unexpected
     */
    @Test(dataProvider = "light administrator privilege test cases")
    public void testReadSessionPrivilegeViaIQueryFind(boolean isAdmin, boolean isRestricted, boolean isSudo) throws Exception {
        final boolean isExpectSuccess = isAdmin && !isRestricted;
        final EventContext normalUser = newUserAndGroup("rwr-r-");
        final EventContext actor = loginNewActor(isAdmin, isSudo ? normalUser.userName : null,
                isRestricted ? AdminPrivilegeReadSession.value : null);
        Session session = (Session) iQuery.find("Session", actor.sessionId);
        Assert.assertEquals(session.getUuid().getValue(), actor.sessionUuid);
        try {
            session = (Session) iQuery.find("Session", normalUser.sessionId);
            Assert.assertEquals(session.getUuid().getValue(), normalUser.sessionUuid);
            Assert.assertTrue(isExpectSuccess);
        } catch (SecurityViolation sv) {
            Assert.assertFalse(isExpectSuccess);
        }
    }

    /**
     * Test that users read others' sessions only if they are a member of the <tt>system</tt> group and
     * have the <tt>ReadSession</tt> privilege.
     * Attempts reading via {@link omero.api.IQueryPrx#projection(String, omero.sys.Parameters)} in query by {@code id}.
     * @param isAdmin if to test a member of the <tt>system</tt> group
     * @param isRestricted if to test a user who does <em>not</em> have the <tt>ReadSession</tt> privilege
     * @param isSudo if to test attempt to subvert privilege by sudo to an unrestricted member of the <tt>system</tt> group
     * @throws Exception unexpected
     */
    @Test(dataProvider = "light administrator privilege test cases")
    public void testReadSessionPrivilegeViaIQueryProjectionById(boolean isAdmin, boolean isRestricted, boolean isSudo)
            throws Exception {
        final boolean isExpectSuccess = isAdmin && !isRestricted;
        final EventContext normalUser = newUserAndGroup("rwr-r-");
        final EventContext actor = loginNewActor(isAdmin, isSudo ? normalUser.userName : null,
                isRestricted ? AdminPrivilegeReadSession.value : null);
        final String hql = "SELECT uuid FROM Session WHERE id = :id";
        List<List<RType>> result = iQuery.projection(hql, new ParametersI().addId(actor.sessionId));
        Assert.assertEquals(((RString) result.get(0).get(0)).getValue(), actor.sessionUuid);
        result = iQuery.projection(hql, new ParametersI().addId(normalUser.sessionId));
        if (isExpectSuccess) {
            Assert.assertEquals(((RString) result.get(0).get(0)).getValue(), normalUser.sessionUuid);
        } else {
            Assert.assertTrue(result.isEmpty());
        }
    }

    /**
     * Test that users read others' sessions only if they are a member of the <tt>system</tt> group and
     * have the <tt>ReadSession</tt> privilege.
     * Attempts reading via {@link omero.api.IQueryPrx#projection(String, omero.sys.Parameters)} in query by {@code uuid}.
     * @param isAdmin if to test a member of the <tt>system</tt> group
     * @param isRestricted if to test a user who does <em>not</em> have the <tt>ReadSession</tt> privilege
     * @param isSudo if to test attempt to subvert privilege by sudo to an unrestricted member of the <tt>system</tt> group
     * @throws Exception unexpected
     */
    @Test(dataProvider = "light administrator privilege test cases")
    public void testReadSessionPrivilegeViaIQueryProjectionByUuid(boolean isAdmin, boolean isRestricted, boolean isSudo)
            throws Exception {
        final boolean isExpectSuccess = isAdmin && !isRestricted;
        final EventContext normalUser = newUserAndGroup("rwr-r-");
        final EventContext actor = loginNewActor(isAdmin, isSudo ? normalUser.userName : null,
                isRestricted ? AdminPrivilegeReadSession.value : null);
        final String hql = "SELECT id FROM Session WHERE uuid = :uuid";
        List<List<RType>> result = iQuery.projection(hql, new ParametersI().add("uuid", omero.rtypes.rstring(actor.sessionUuid)));
        Assert.assertEquals(((RLong) result.get(0).get(0)).getValue(), actor.sessionId);
        result = iQuery.projection(hql, new ParametersI().add("uuid", omero.rtypes.rstring(normalUser.sessionUuid)));
        if (isExpectSuccess) {
            Assert.assertEquals(((RLong) result.get(0).get(0)).getValue(), normalUser.sessionId);
        } else {
            Assert.assertTrue(result.isEmpty());
        }
    }

    /**
     * Test that users read others' sessions only if they are a member of the <tt>system</tt> group and
     * have the <tt>ReadSession</tt> privilege. Attempts reading via {@link ISessionPrx#getMyOpenSessions()}.
     * @param isAdmin if to test a member of the <tt>system</tt> group
     * @param isRestricted if to test a user who does <em>not</em> have the <tt>ReadSession</tt> privilege
     * @throws Exception unexpected
     */
    @Test(dataProvider = "light administrator privilege test cases (without sudo)")
    public void testReadSessionPrivilegeViaISession(boolean isAdmin, boolean isRestricted) throws Exception {
        final boolean isExpectSuccess = isAdmin && !isRestricted;
        final EventContext normalUser = newUserAndGroup("rwr-r-");
        /* prevent the normal user's session from being closed yet */
        final omero.client normalUserClient = client;
        client = null;
        /* attempt to sudo to the normal user */
        final EventContext actor = loginNewAdmin(isAdmin, isRestricted ? AdminPrivilegeReadSession.value : null);
        final EventContext actorAsNormalUser;
        try {
            actorAsNormalUser = sudo(normalUser.userName);
            Assert.assertTrue(isAdmin, "normal users cannot sudo");
        } catch (SecurityViolation sv) {
            Assert.assertFalse(isAdmin, "admins can sudo");
            /* cannot proceed with the attempt */
            normalUserClient.__del__();
            return;
        }
        /* test current user's open sessions */
        final ISessionPrx iSession = factory.getSessionService();
        final Set<String> sessionUuids = new HashSet<>();
        for (final Session session : iSession.getMyOpenSessions()) {
            sessionUuids.add(session.getUuid().getValue());
        }
        Assert.assertFalse(sessionUuids.contains(actor.sessionUuid));
        Assert.assertTrue(sessionUuids.contains(actorAsNormalUser.sessionUuid));
        /* can never succeed because privileges are not preserved through sudo */
        Assert.assertFalse(sessionUuids.contains(normalUser.sessionUuid));
        normalUserClient.__del__();
    }

    /**
     * Test that users read others' sessions only if they are a member of the <tt>system</tt> group and
     * have the <tt>ReadSession</tt> privilege. Attempts reading via {@link CurrentSessionsRequest}.
     * @param isAdmin if to test a member of the <tt>system</tt> group
     * @param isRestricted if to test a user who does <em>not</em> have the <tt>ReadSession</tt> privilege
     * @throws Exception unexpected
     */
    @Test(dataProvider = "light administrator privilege test cases (without sudo)")
    public void testReadSessionPrivilegeViaRequest(boolean isAdmin, boolean isRestricted) throws Exception {
        final boolean isExpectSuccess = isAdmin && !isRestricted;
        final EventContext normalUser = newUserAndGroup("rwr-r-");
        /* prevent the normal user's session from being closed yet */
        final omero.client normalUserClient = client;
        client = null;
        /* attempt to sudo to the normal user */
        EventContext actor = loginNewAdmin(isAdmin, isRestricted ? AdminPrivilegeReadSession.value : null);
        final EventContext actorAsNormalUser;
        try {
            actorAsNormalUser = sudo(normalUser.userName);
            Assert.assertTrue(isAdmin, "normal users cannot sudo");
        } catch (SecurityViolation sv) {
            Assert.assertFalse(isAdmin, "admins can sudo");
            /* cannot proceed with the attempt */
            normalUserClient.__del__();
            return;
        }
        /* test open sessions */
        final CurrentSessionsResponse response = (CurrentSessionsResponse) doChange(new CurrentSessionsRequest());
        final Set<String> sessionUuids = new HashSet<>();
        for (final Session session : response.sessions) {
            if (session != null) {
                sessionUuids.add(session.getUuid().getValue());
            }
        }
        Assert.assertFalse(sessionUuids.contains(actor.sessionUuid));
        Assert.assertTrue(sessionUuids.contains(actorAsNormalUser.sessionUuid));
        /* can never succeed because privileges are not preserved through sudo */
        Assert.assertFalse(sessionUuids.contains(normalUser.sessionUuid));
        normalUserClient.__del__();
    }

    /**
     * Test that a light administrator cannot sudo without the <tt>Sudo</tt> privilege.
     * @throws Exception unexpected
     */
    @Test
    public void testSudoPrivilege() throws Exception {
        final EventContext normalUser = newUserAndGroup("rwr-r-");
        loginNewAdmin(true, AdminPrivilegeSudo.value);
        try {
            sudo(normalUser.userName);
            Assert.fail("Sudo-restricted administrators cannot sudo.");
        } catch (SecurityViolation sv) {
            /* expected */
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
        final EventContext normalUser = newUserAndGroup("rwr-r-");
        RepositoryPrx repo = getRepository(Repository.OTHER);
        final String userDirectory = "/Test_" + getClass().getName() + '_' + UUID.randomUUID();
        repo.makeDir(userDirectory, false);
        loginNewActor(isAdmin, isSudo ? loginNewAdmin(true, null).userName : null,
                isRestricted ? AdminPrivilegeWriteFile.value : null);
        try (final AutoCloseable igc = new ImplicitGroupContext(normalUser.groupId)) {
            repo = getRepository(Repository.OTHER);
            final String filename = userDirectory + '/' + UUID.randomUUID();
            try {
                repo.makeDir(filename, false);
                Assert.assertTrue(isExpectSuccess);
            } catch (ServerError se) {
                Assert.assertFalse(isExpectSuccess);
            }
        }
    }

    /**
     * Test that users may write other users' files only if they are a member of the <tt>system</tt> group and
     * have the <tt>WriteFile</tt> privilege.
     * Attempts creation of a table in another user's directory via {@link SharedResourcesPrx#newTable(long, String)}.
     * @param isAdmin if to test a member of the <tt>system</tt> group
     * @param isRestricted if to test a user who does <em>not</em> have the <tt>WriteFile</tt> privilege
     * @param isSudo if to test attempt to subvert privilege by sudo to an unrestricted member of the <tt>system</tt> group
     * @throws Exception unexpected
     */
    @Test(dataProvider = "light administrator privilege test cases")
    public void testWriteFilePrivilegeCreationViaRepoNewTable(boolean isAdmin, boolean isRestricted, boolean isSudo)
            throws Exception {
        final boolean isExpectSuccess = isAdmin && !isRestricted;
        final EventContext normalUser = newUserAndGroup("rwr-r-");
        if (!factory.sharedResources().areTablesEnabled()) {
            throw new SkipException("tables are not enabled");
        }
        RepositoryPrx repo = getRepository(Repository.OTHER);
        final String userDirectory = "/Test_" + getClass().getName() + '_' + UUID.randomUUID();
        repo.makeDir(userDirectory, false);
        loginNewActor(isAdmin, isSudo ? loginNewAdmin(true, null).userName : null,
                isRestricted ? AdminPrivilegeWriteFile.value : null);
        try (final AutoCloseable igc = new ImplicitGroupContext(normalUser.groupId)) {
            repo = getRepository(Repository.OTHER);
            final String filename = userDirectory + '/' + UUID.randomUUID();
            try {
                factory.sharedResources().newTable(repo.root().getId().getValue(), filename).close();
                Assert.assertTrue(isExpectSuccess);
            } catch (Ice.LocalException se) {
                Assert.assertFalse(isExpectSuccess);
            }
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
        final EventContext normalUser = newUserAndGroup("rwr-r-");
        RepositoryPrx repo = getRepository(Repository.OTHER);
        final String userDirectory = "/Test_" + getClass().getName() + '_' + UUID.randomUUID();
        repo.makeDir(userDirectory, false);
        loginNewActor(isAdmin, isSudo ? loginNewAdmin(true, null).userName : null,
                isRestricted ? AdminPrivilegeWriteFile.value : null);
        try (final AutoCloseable igc = new ImplicitGroupContext(normalUser.groupId)) {
            repo = getRepository(Repository.OTHER);
            final String filename = userDirectory + '/' + UUID.randomUUID();
            try {
                repo.register(filename, null);
                Assert.assertTrue(isExpectSuccess);
            } catch (ServerError se) {
                Assert.assertFalse(isExpectSuccess);
            }
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
        final EventContext normalUser = newUserAndGroup("rwr-r-");
        loginNewActor(isAdmin, isSudo ? loginNewAdmin(true, null).userName : null,
                isRestricted ? AdminPrivilegeWriteFile.value : null);
        /* try uploading the test script as a new script in the normal user's group */
        final long testScriptId;
        try (final AutoCloseable igc = new ImplicitGroupContext(normalUser.groupId)) {
            final IScriptPrx iScript = factory.getScriptService();
            final String testScriptName = "Test_" + getClass().getName() + '_' + UUID.randomUUID() + ".py";
            try {
                testScriptId = iScript.uploadScript(testScriptName, getPythonScript());
                Assert.assertTrue(isExpectSuccess);
            } catch (ServerError se) {
                Assert.assertFalse(isExpectSuccess);
                /* upload failed so finish here */
                return;
            }
        }
        /* check that the new script exists */
        loginUser(normalUser);
        final OriginalFile scriptFile = (OriginalFile) iQuery.get("OriginalFile", testScriptId);
        Assert.assertEquals(scriptFile.getDetails().getGroup().getId().getValue(), normalUser.groupId);
        /* check if the script is correctly uploaded */
        final RawFileStorePrx rfs = factory.createRawFileStore();
        rfs.setFileId(testScriptId);
        final String currentScript = new String(rfs.read(0, (int) rfs.size()), StandardCharsets.UTF_8);
        rfs.close();
        Assert.assertEquals(currentScript, getPythonScript());
    }

    /**
     * Test that users may write other users' files only if they are a member of the <tt>system</tt> group and
     * have the <tt>WriteFile</tt> privilege.
     * Attempts creation of another user's file via {@link omero.api.IUpdatePrx#saveAndReturnObject(IObject)}.
     * @param isAdmin if to test a member of the <tt>system</tt> group
     * @param isRestricted if to test a user who does <em>not</em> have the <tt>WriteFile</tt> privilege
     * @param isSudo if to test attempt to subvert privilege by sudo to an unrestricted member of the <tt>system</tt> group
     * @throws Exception unexpected
     */
    @Test(dataProvider = "light administrator privilege test cases")
    public void testWriteFilePrivilegeCreationViaUpdate(boolean isAdmin, boolean isRestricted, boolean isSudo) throws Exception {
        final boolean isExpectSuccess = isAdmin && !isRestricted;
        final EventContext normalUser = newUserAndGroup("rwr-r-");
        loginNewActor(isAdmin, isSudo ? loginNewAdmin(true, null).userName : null,
                isRestricted ? AdminPrivilegeWriteFile.value : null);
        try (final AutoCloseable igc = new ImplicitGroupContext(normalUser.groupId)) {
            OriginalFile file = mmFactory.createOriginalFile();
            file.getDetails().setOwner(new ExperimenterI(normalUser.userId, false));
            try {
                file = (OriginalFile) iUpdate.saveAndReturnObject(file);
                Assert.assertEquals(file.getDetails().getOwner().getId().getValue(), normalUser.userId);
                Assert.assertEquals(file.getDetails().getGroup().getId().getValue(), normalUser.groupId);
                Assert.assertTrue(isExpectSuccess);
            } catch (ServerError se) {
                Assert.assertFalse(isExpectSuccess);
            }
        }
    }

    /**
     * Test that users may write other users' files only if they are a member of the <tt>system</tt> group and
     * have the <tt>WriteFile</tt> privilege.
     * Attempts writing file via {@link RawFileStorePrx#write(byte[], long, int)}.
     * @param isAdmin if to test a member of the <tt>system</tt> group
     * @param isRestricted if to test a user who does <em>not</em> have the <tt>WriteFile</tt> privilege
     * @param isSudo if to test attempt to subvert privilege by sudo to an unrestricted member of the <tt>system</tt> group
     * @throws Exception unexpected
     */
    @Test(dataProvider = "light administrator privilege test cases")
    public void testWriteFilePrivilegeEditingViaRaw(boolean isAdmin, boolean isRestricted, boolean isSudo) throws Exception {
        final boolean isExpectSuccess = isAdmin && !isRestricted;
        final EventContext normalUser = newUserAndGroup("rwr-r-");
        /* write a random file */
        final OriginalFile file = (OriginalFile) iUpdate.saveAndReturnObject(mmFactory.createOriginalFile());
        final long fileId = file.getId().getValue();
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
        loginNewActor(isAdmin, isSudo ? loginNewAdmin(true, null).userName : null,
                isRestricted ? AdminPrivilegeWriteFile.value : null);
        final byte[] fileContentBlank;
        try (final AutoCloseable igc = new ImplicitGroupContext(normalUser.groupId)) {
            Assert.assertEquals(getCurrentPermissions(file).canEdit(), isExpectSuccess);
            fileContentBlank = new byte[fileContentOriginal.length];
            try {
                rfs = factory.createRawFileStore();
                rfs.setFileId(fileId);
                rfs.write(fileContentBlank, 0, fileContentBlank.length);
                Assert.assertTrue(isExpectSuccess);
            } catch (ServerError se) {
                Assert.assertFalse(isExpectSuccess);
            } finally {
                try {
                    rfs.close();
                } catch (ServerError se) {
                    /* cannot try to close */
                }
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
     * Attempts writing file via {@link RepositoryPrx#file(String, String)}
     * and {@link RawFileStorePrx#write(byte[], long, int)}.
     * @param isAdmin if to test a member of the <tt>system</tt> group
     * @param isRestricted if to test a user who does <em>not</em> have the <tt>WriteFile</tt> privilege
     * @param isSudo if to test attempt to subvert privilege by sudo to an unrestricted member of the <tt>system</tt> group
     * @throws Exception unexpected
     */
    @Test(dataProvider = "light administrator privilege test cases")
    public void testWriteFilePrivilegeEditingViaRepoFile(boolean isAdmin, boolean isRestricted, boolean isSudo) throws Exception {
        final boolean isExpectSuccess = isAdmin && !isRestricted;
        final EventContext normalUser = newUserAndGroup("rwr-r-");
        /* upload the test script as a new script */
        final String testScriptName = "Test_" + getClass().getName() + '_' + UUID.randomUUID() + ".py";
        RepositoryPrx repo = getRepository(Repository.OTHER);
        final OriginalFile testScript = repo.register(testScriptName, omero.rtypes.rstring(ScriptServiceTest.PYTHON_MIMETYPE));
        final long testScriptId = testScript.getId().getValue();
        final byte[] fileContentOriginal = getPythonScript().getBytes(StandardCharsets.UTF_8);
        RawFileStorePrx rfs = repo.file(testScriptName, "rw");
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
        loginNewActor(isAdmin, isSudo ? loginNewAdmin(true, null).userName : null,
                isRestricted ? AdminPrivilegeWriteFile.value : null);
        final byte[] fileContentBlank;
        try (final AutoCloseable igc = new ImplicitGroupContext(normalUser.groupId)) {
            Assert.assertEquals(getCurrentPermissions(testScript).canEdit(), isExpectSuccess);
            repo = getRepository(Repository.OTHER);
            fileContentBlank = new byte[fileContentOriginal.length];
            try {
                rfs = repo.file(testScriptName, "rw");
                rfs.write(fileContentBlank, 0, fileContentBlank.length);
                Assert.assertTrue(isExpectSuccess);
            } catch (ServerError se) {
                Assert.assertFalse(isExpectSuccess);
            } finally {
                try {
                    rfs.close();
                } catch (Ice.CommunicatorDestroyedException cde) {
                    /* cannot try to close */
                }
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
     * Attempts changing an existing file via {@link omero.api.IScriptPrx#editScript(OriginalFile, String)}.
     * @param isAdmin if to test a member of the <tt>system</tt> group
     * @param isRestricted if to test a user who does <em>not</em> have the <tt>WriteFile</tt> privilege
     * @param isSudo if to test attempt to subvert privilege by sudo to an unrestricted member of the <tt>system</tt> group
     * @throws Exception unexpected
     */
    @Test(dataProvider = "light administrator privilege test cases")
    public void testWriteFilePrivilegeEditingViaScripts(boolean isAdmin, boolean isRestricted, boolean isSudo) throws Exception {
        final boolean isExpectSuccess = isAdmin && !isRestricted;
        final EventContext normalUser = newUserAndGroup("rwr-r-");
        /* upload the test script as a new script */
        IScriptPrx iScript = factory.getScriptService();
        final String testScriptName = "Test_" + getClass().getName() + '_' + UUID.randomUUID() + ".py";
        final long testScriptId = iScript.uploadScript(testScriptName, getPythonScript());
        OriginalFile testScript = new OriginalFileI(testScriptId, false);
        /* try replacing the content of the normal user's script */
        loginNewActor(isAdmin, isSudo ? loginNewAdmin(true, null).userName : null,
                isRestricted ? AdminPrivilegeWriteFile.value : null);
        final String newScript;
        try (final AutoCloseable igc = new ImplicitGroupContext(normalUser.groupId)) {
            Assert.assertEquals(getCurrentPermissions(testScript).canEdit(), isExpectSuccess);
            iScript = factory.getScriptService();
            newScript = getPythonScript() + "\n# this script is a copy of another";
            try {
                iScript.editScript(new OriginalFileI(testScriptId, false), newScript);
                Assert.assertTrue(isExpectSuccess);
            } catch (ServerError se) {
                Assert.assertFalse(isExpectSuccess);
            }
        }
        /* check the permissions on the script */
        loginUser(normalUser);
        testScript = (OriginalFile) iQuery.get("OriginalFile", testScriptId);
        Assert.assertEquals(testScript.getDetails().getOwner().getId().getValue(), normalUser.userId);
        Assert.assertEquals(testScript.getDetails().getGroup().getId().getValue(), normalUser.groupId);
        /* check the content of the script */
        final RawFileStorePrx rfs = factory.createRawFileStore();
        rfs.setFileId(testScriptId);
        final String currentScript = new String(rfs.read(0, (int) rfs.size()), StandardCharsets.UTF_8);
        rfs.close();
        Assert.assertEquals(currentScript, isExpectSuccess ? newScript : getPythonScript());
    }

    /**
     * Test that users may write other users' files only if they are a member of the <tt>system</tt> group and
     * have the <tt>WriteFile</tt> privilege.
     * Attempts changing an existing file via {@link omero.api.IUpdatePrx#saveAndReturnObject(IObject)}.
     * @param isAdmin if to test a member of the <tt>system</tt> group
     * @param isRestricted if to test a user who does <em>not</em> have the <tt>WriteFile</tt> privilege
     * @param isSudo if to test attempt to subvert privilege by sudo to an unrestricted member of the <tt>system</tt> group
     * @throws Exception unexpected
     */
    @Test(dataProvider = "light administrator privilege test cases")
    public void testWriteFilePrivilegeEditingViaUpdate(boolean isAdmin, boolean isRestricted, boolean isSudo) throws Exception {
        final boolean isExpectSuccess = isAdmin && !isRestricted;
        final EventContext normalUser = newUserAndGroup("rwr-r-");
        OriginalFile file = (OriginalFile) iUpdate.saveAndReturnObject(mmFactory.createOriginalFile());
        loginNewActor(isAdmin, isSudo ? loginNewAdmin(true, null).userName : null,
                isRestricted ? AdminPrivilegeWriteFile.value : null);
        try (final AutoCloseable igc = new ImplicitGroupContext(normalUser.groupId)) {
            file = (OriginalFile) iQuery.get("OriginalFile", file.getId().getValue());
            Assert.assertEquals(getCurrentPermissions(file).canEdit(), isExpectSuccess);
            final String newFilename = "Test_" + getClass().getName() + '_' + UUID.randomUUID();
            file.setName(omero.rtypes.rstring(newFilename));
            try {
                file = (OriginalFile) iUpdate.saveAndReturnObject(file);
                Assert.assertEquals(file.getName().getValue(), newFilename);
                Assert.assertTrue(isExpectSuccess);
            } catch (ServerError se) {
                Assert.assertFalse(isExpectSuccess);
            }
        }
    }

    /**
     * Test that users may write other users' files only if they are a member of the <tt>system</tt> group and
     * have the <tt>WriteManagedRepo</tt> privilege. Attempts to write files via the import process.
     * @param isAdmin if to test a member of the <tt>system</tt> group
     * @param isRestricted if to test a user who does <em>not</em> have the <tt>WriteManagedRepo</tt> privilege
     * @param isSudo if to test attempt to subvert privilege by sudo to an unrestricted member of the <tt>system</tt> group
     * @throws Exception unexpected
     */
    @Test(dataProvider = "light administrator privilege test cases")
    public void testWriteManagedRepoPrivilegeCreationViaRepoImport(boolean isAdmin, boolean isRestricted, boolean isSudo)
            throws Exception {
        final boolean isExpectSuccess = isAdmin && !isRestricted;
        final EventContext normalUser = newUserAndGroup("rwr-r-");
        loginNewActor(isAdmin, isSudo ? loginNewAdmin(true, null).userName : null,
                isRestricted ? AdminPrivilegeWriteManagedRepo.value : null);
        try (final AutoCloseable igc = new ImplicitGroupContext(normalUser.groupId)) {
            final ImportLocation importLocation;
            try {
                importLocation = importFileset(Collections.singletonList(fakeImageFile.getPath()));
                Assert.assertTrue(isExpectSuccess);
            } catch (ServerError se) {
                Assert.assertFalse(isExpectSuccess);
                /* no file to check */
                return;
            }
            final RString imagePath = omero.rtypes.rstring(importLocation.sharedPath + FsFile.separatorChar);
            final RString imageName = omero.rtypes.rstring(fakeImageFile.getName());
            final OriginalFile remoteFile = (OriginalFile) iQuery.findByQuery(
                    "FROM OriginalFile o WHERE o.path = :path AND o.name = :name AND o.details.group.id = :group_id",
                    new ParametersI().add("path", imagePath).add("name", imageName).addLong("group_id", normalUser.groupId));
            Assert.assertNotNull(remoteFile);
        }
    }

    /**
     * Test that users may write other users' files only if they are a member of the <tt>system</tt> group and
     * have the <tt>WriteManagedRepo</tt> privilege.
     * Attempts changing the file's checksum algorithm via
     * {@link ManagedRepositoryPrx#setChecksumAlgorithm(ChecksumAlgorithm, List)}.
     * @param isAdmin if to test a member of the <tt>system</tt> group
     * @param isRestricted if to test a user who does <em>not</em> have the <tt>WriteManagedRepo</tt> privilege
     * @param isSudo if to test attempt to subvert privilege by sudo to an unrestricted member of the <tt>system</tt> group
     * @throws Exception unexpected
     */
    @Test(dataProvider = "light administrator privilege test cases")
    public void testWriteManagedRepoPrivilegeEditingViaRepoChecksum(boolean isAdmin, boolean isRestricted, boolean isSudo)
            throws Exception {
        final boolean isExpectSuccess = isAdmin && !isRestricted;
        final EventContext normalUser = newUserAndGroup("rwr-r-");
        /* import a fake image and determine its hash and hasher */
        final List<String> imageFilenames = Collections.singletonList(fakeImageFile.getPath());
        final String repoPath = importFileset(imageFilenames).sharedPath + FsFile.separatorChar;
        List<RType> results = iQuery.projection(
                "SELECT id, hasher.value, hash FROM OriginalFile " +
                "WHERE name = :name AND path = :path AND details.group.id = :group_id",
                new ParametersI().add("name", omero.rtypes.rstring(fakeImageFile.getName()))
                                 .add("path", omero.rtypes.rstring(repoPath))
                                 .add("group_id", omero.rtypes.rlong(normalUser.groupId))).get(0);
        final long imageFileId = ((RLong) results.get(0)).getValue();
        final String hasherOriginal = ((RString) results.get(1)).getValue();
        final String hashOriginal = ((RString) results.get(2)).getValue();
        /* try to change the image's hasher */
        loginNewActor(isAdmin, isSudo ? loginNewAdmin(true, null).userName : null,
                isRestricted ? AdminPrivilegeWriteManagedRepo.value : null);
        final String hasherChanged;
        try (final AutoCloseable igc = new ImplicitGroupContext(normalUser.groupId)) {
            Assert.assertEquals(getCurrentPermissions(new OriginalFileI(imageFileId, false)).canEdit(), isExpectSuccess);
            final ManagedRepositoryPrx repo = ManagedRepositoryPrxHelper.checkedCast(getRepository(Repository.MANAGED));
            if (ChecksumAlgorithmSHA1160.value.equals(hasherOriginal)) {
                hasherChanged = ChecksumAlgorithmMurmur3128.value;
            } else {
                hasherChanged = ChecksumAlgorithmSHA1160.value;
            }
            try {
                final ChecksumAlgorithm hasherAlgorithm = new ChecksumAlgorithmI();
                hasherAlgorithm.setValue(omero.rtypes.rstring(hasherChanged));
                repo.setChecksumAlgorithm(hasherAlgorithm, Collections.singletonList(imageFileId));
                Assert.assertTrue(isExpectSuccess);
            } catch (Ice.LocalException | ServerError se) {
                Assert.assertFalse(isExpectSuccess);
            }
        }
        /* check the effect on the image's hash and hasher */
        loginUser(normalUser);
        results = iQuery.projection(
                "SELECT hasher.value, hash FROM OriginalFile WHERE id = :id",
                new ParametersI().addId(imageFileId)).get(0);
        final String hasherNew = ((RString) results.get(0)).getValue();
        final String hashNew = ((RString) results.get(1)).getValue();
        if (isExpectSuccess) {
            Assert.assertEquals(hasherNew, hasherChanged);
            Assert.assertNotEquals(hashNew, hashOriginal);
        } else {
            Assert.assertEquals(hasherNew, hasherOriginal);
            Assert.assertEquals(hashNew, hashOriginal);
        }
    }

    /**
     * Test that users may write other users' files only if they are a member of the <tt>system</tt> group and
     * have the <tt>WriteManagedRepo</tt> privilege.
     * Attempts writing file via {@link RepositoryPrx#file(String, String)}
     * and {@link RawFileStorePrx#write(byte[], long, int)}.
     * @param isAdmin if to test a member of the <tt>system</tt> group
     * @param isRestricted if to test a user who does <em>not</em> have the <tt>WriteManagedRepo</tt> privilege
     * @param isSudo if to test attempt to subvert privilege by sudo to an unrestricted member of the <tt>system</tt> group
     * @throws Exception unexpected
     */
    @Test(dataProvider = "light administrator privilege test cases")
    public void testWriteManagedRepoPrivilegeEditingViaRepoFile(boolean isAdmin, boolean isRestricted, boolean isSudo)
            throws Exception {
        /* import a fake image file as a normal user */
        final boolean isExpectSuccess = isAdmin && !isRestricted;
        final EventContext normalUser = newUserAndGroup("rwr-r-");
        final ImportLocation importLocation = importFileset(Collections.singletonList(fakeImageFile.getPath()));
        final RString imagePath = omero.rtypes.rstring(importLocation.sharedPath + FsFile.separatorChar);
        final RString imageName = omero.rtypes.rstring(fakeImageFile.getName());
        OriginalFile remoteFile = (OriginalFile) iQuery.findByQuery(
                "FROM OriginalFile o WHERE o.path = :path AND o.name = :name AND o.details.group.id = :group_id",
                new ParametersI().add("path", imagePath).add("name", imageName).addLong("group_id", normalUser.groupId));
        /* try to edit the file */
        loginNewActor(isAdmin, isSudo ? loginNewAdmin(true, null).userName : null,
                isRestricted ? AdminPrivilegeWriteManagedRepo.value : null);
        final byte[] fileContentBlank;
        try (final AutoCloseable igc = new ImplicitGroupContext(normalUser.groupId)) {
            Assert.assertEquals(getCurrentPermissions(remoteFile).canEdit(), isExpectSuccess);
            final RepositoryPrx repo = getRepository(Repository.MANAGED);
            fileContentBlank = new byte[(int) (fakeImageFile.length() + 16)];
            RawFileStorePrx rfs = null;
            try {
                rfs = repo.file(remoteFile.getPath().getValue() + remoteFile.getName().getValue(), "rw");
                rfs.write(fileContentBlank, 0, fileContentBlank.length);
                Assert.assertTrue(isExpectSuccess);
            } catch (ServerError se) {
                Assert.assertFalse(isExpectSuccess);
            } finally {
                try {
                    if (rfs != null) {
                        rfs.close();
                    }
                } catch (Ice.CommunicatorDestroyedException cde) {
                    /* cannot try to close */
                }
            }
        }
        /* check the resulting file size */
        loginUser(normalUser);
        remoteFile = (OriginalFile) iQuery.get("OriginalFile", remoteFile.getId().getValue());
        Assert.assertEquals(remoteFile.getSize().getValue(), isExpectSuccess ? fileContentBlank.length : fakeImageFile.length());
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
        final EventContext normalUser = newUserAndGroup("rwr-r-");
        loginNewActor(isAdmin, isSudo ? loginNewAdmin(true, null).userName : null,
                isRestricted ? AdminPrivilegeWriteOwned.value : null);
        try (final AutoCloseable igc = new ImplicitGroupContext(normalUser.groupId)) {
            Folder folder = mmFactory.simpleFolder();
            folder.getDetails().setOwner(new ExperimenterI(normalUser.userId, false));
            try {
                folder = (Folder) iUpdate.saveAndReturnObject(folder);
                Assert.assertEquals(folder.getDetails().getOwner().getId().getValue(), normalUser.userId);
                Assert.assertEquals(folder.getDetails().getGroup().getId().getValue(), normalUser.groupId);
                Assert.assertTrue(isExpectSuccess);
            } catch (ServerError se) {
                Assert.assertFalse(isExpectSuccess);
            }
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
        final EventContext normalUser = newUserAndGroup("rwr-r-");
        Folder folder = (Folder) iUpdate.saveAndReturnObject(mmFactory.simpleFolder());
        loginNewActor(isAdmin, isSudo ? loginNewAdmin(true, null).userName : null,
                isRestricted ? AdminPrivilegeWriteOwned.value : null);
        try (final AutoCloseable igc = new ImplicitGroupContext(normalUser.groupId)) {
            folder = (Folder) iQuery.get("Folder", folder.getId().getValue());
            Assert.assertEquals(getCurrentPermissions(folder).canEdit(), isExpectSuccess);
            final String newFolderName = "Test_" + getClass().getName() + '_' + UUID.randomUUID();
            folder.setName(omero.rtypes.rstring(newFolderName));
            try {
                folder = (Folder) iUpdate.saveAndReturnObject(folder);
                Assert.assertEquals(folder.getName().getValue(), newFolderName);
                Assert.assertTrue(isExpectSuccess);
            } catch (ServerError se) {
                Assert.assertFalse(isExpectSuccess);
            }
        }
    }

    /**
     * Test that users may write official scripts only if they are a member of the <tt>system</tt> group and
     * have the <tt>WriteScriptRepo</tt> privilege.
     * Attempts creation of a directory in another user's directory via {@link RepositoryPrx#makeDir(String, boolean)}.
     * @param isAdmin if to test a member of the <tt>system</tt> group
     * @param isRestricted if to test a user who does <em>not</em> have the <tt>WriteScriptRepo</tt> privilege
     * @param isSudo if to test attempt to subvert privilege by sudo to an unrestricted member of the <tt>system</tt> group
     * @throws Exception unexpected
     */
    @Test(dataProvider = "light administrator privilege test cases")
    public void testWriteScriptRepoPrivilegeCreationViaRepoMakeDir(boolean isAdmin, boolean isRestricted, boolean isSudo)
            throws Exception {
        final boolean isExpectSuccess = isAdmin && !isRestricted;
        final EventContext normalUser = newUserAndGroup("rwr-r-");
        RepositoryPrx repo = getRepository(Repository.SCRIPT);
        final String userDirectory = "/Test_" + getClass().getName() + '_' + UUID.randomUUID();
        repo.makeDir(userDirectory, false);
        loginNewActor(isAdmin, isSudo ? loginNewAdmin(true, null).userName : null,
                isRestricted ? AdminPrivilegeWriteScriptRepo.value : null);
        try (final AutoCloseable igc = new ImplicitGroupContext(normalUser.groupId)) {
            repo = getRepository(Repository.SCRIPT);
            final String filename = userDirectory + '/' + UUID.randomUUID();
            try {
                repo.makeDir(filename, false);
                Assert.assertTrue(isExpectSuccess);
            } catch (ServerError se) {
                Assert.assertFalse(isExpectSuccess);
            }
        }
    }

    /**
     * Test that users may write official scripts only if they are a member of the <tt>system</tt> group and
     * have the <tt>WriteScriptRepo</tt> privilege.
     * Attempts creation of a table in another user's directory via {@link SharedResourcesPrx#newTable(long, String)}.
     * @param isAdmin if to test a member of the <tt>system</tt> group
     * @param isRestricted if to test a user who does <em>not</em> have the <tt>WriteScriptRepo</tt> privilege
     * @param isSudo if to test attempt to subvert privilege by sudo to an unrestricted member of the <tt>system</tt> group
     * @throws Exception unexpected
     */
    @Test(dataProvider = "light administrator privilege test cases", groups = "broken")
    public void testWriteScriptRepoPrivilegeCreationViaRepoNewTable(boolean isAdmin, boolean isRestricted, boolean isSudo)
            throws Exception {
        final boolean isExpectSuccess = isAdmin && !isRestricted;
        final EventContext normalUser = newUserAndGroup("rwr-r-");
        if (!factory.sharedResources().areTablesEnabled()) {
            throw new SkipException("tables are not enabled");
        }
        RepositoryPrx repo = getRepository(Repository.SCRIPT);
        final String userDirectory = "/Test_" + getClass().getName() + '_' + UUID.randomUUID();
        repo.makeDir(userDirectory, false);
        loginNewActor(isAdmin, isSudo ? loginNewAdmin(true, null).userName : null,
                isRestricted ? AdminPrivilegeWriteScriptRepo.value : null);
        try (final AutoCloseable igc = new ImplicitGroupContext(normalUser.groupId)) {
            repo = getRepository(Repository.SCRIPT);
            final String filename = userDirectory + '/' + UUID.randomUUID();
            try {
                // TODO: test is broken because SharedResources.newTable ignores its repo ID argument
                factory.sharedResources().newTable(repo.root().getId().getValue(), filename).close();
                Assert.assertTrue(isExpectSuccess);
            } catch (Ice.LocalException se) {
                Assert.assertFalse(isExpectSuccess);
            }
        }
    }

    /**
     * Test that users may write official scripts only if they are a member of the <tt>system</tt> group and
     * have the <tt>WriteScriptRepo</tt> privilege.
     * Attempts creation of a file in another user's directory via {@link RepositoryPrx#register(String, omero.RString)}.
     * @param isAdmin if to test a member of the <tt>system</tt> group
     * @param isRestricted if to test a user who does <em>not</em> have the <tt>WriteScriptRepo</tt> privilege
     * @param isSudo if to test attempt to subvert privilege by sudo to an unrestricted member of the <tt>system</tt> group
     * @throws Exception unexpected
     */
    @Test(dataProvider = "light administrator privilege test cases")
    public void testWriteScriptRepoPrivilegeCreationViaRepoRegister(boolean isAdmin, boolean isRestricted, boolean isSudo)
            throws Exception {
        final boolean isExpectSuccess = isAdmin && !isRestricted;
        final EventContext normalUser = newUserAndGroup("rwr-r-");
        RepositoryPrx repo = getRepository(Repository.SCRIPT);
        final String userDirectory = "/Test_" + getClass().getName() + '_' + UUID.randomUUID();
        repo.makeDir(userDirectory, false);
        loginNewActor(isAdmin, isSudo ? loginNewAdmin(true, null).userName : null,
                isRestricted ? AdminPrivilegeWriteScriptRepo.value : null);
        try (final AutoCloseable igc = new ImplicitGroupContext(normalUser.groupId)) {
            repo = getRepository(Repository.SCRIPT);
            final String filename = userDirectory + '/' + UUID.randomUUID();
            try {
                repo.register(filename, null);
                Assert.assertTrue(isExpectSuccess);
            } catch (ServerError se) {
                Assert.assertFalse(isExpectSuccess);
            }
        }
    }

    /**
     * Test that users may write official scripts only if they are a member of the <tt>system</tt> group and
     * have the <tt>WriteScriptRepo</tt> privilege.
     * Attempts creation of a file in the <q>user</q> group via {@link omero.api.IScriptPrx#uploadOfficialScript(String, String)}.
     * @param isAdmin if to test a member of the <tt>system</tt> group
     * @param isRestricted if to test a user who does <em>not</em> have the <tt>WriteScriptRepo</tt> privilege
     * @param isSudo if to test attempt to subvert privilege by sudo to an unrestricted member of the <tt>system</tt> group
     * @throws Exception unexpected
     */
    @Test(dataProvider = "light administrator privilege test cases")
    public void testWriteScriptRepoPrivilegeCreationViaScripts(boolean isAdmin, boolean isRestricted, boolean isSudo)
            throws Exception {
        final boolean isExpectSuccess = isAdmin && !isRestricted;
        final EventContext normalUser = newUserAndGroup("rwr-r-");
        loginNewActor(isAdmin, isSudo ? loginNewAdmin(true, null).userName : null,
                isRestricted ? AdminPrivilegeWriteScriptRepo.value : null);
        /* try uploading the test script as a new script */
        final IScriptPrx iScript = factory.getScriptService();
        final String testScriptName = "Test_" + getClass().getName() + '_' + UUID.randomUUID() + ".py";
        long testScriptId = -1;
        try {
            testScriptId = iScript.uploadOfficialScript(testScriptName, getPythonScript());
            Assert.assertTrue(isExpectSuccess);
        } catch (ServerError se) {
            Assert.assertFalse(isExpectSuccess);
            /* upload failed so finish here */
            return;
        }
        /* check that the new script exists in the "user" group */
        loginUser(normalUser);
        final OriginalFile scriptFile = (OriginalFile) iQuery.get("OriginalFile", testScriptId);
        Assert.assertEquals(scriptFile.getDetails().getOwner().getId().getValue(), roles.rootId);
        Assert.assertEquals(scriptFile.getDetails().getGroup().getId().getValue(), roles.userGroupId);
        /* check if the script is correctly uploaded */
        final RawFileStorePrx rfs = factory.createRawFileStore();
        rfs.setFileId(testScriptId);
        final String currentScript = new String(rfs.read(0, (int) rfs.size()), StandardCharsets.UTF_8);
        rfs.close();
        Assert.assertEquals(currentScript, getPythonScript());
    }

    /**
     * Test that users may write official scripts only if they are a member of the <tt>system</tt> group and
     * have the <tt>WriteScriptRepo</tt> privilege.
     * Attempts writing file via {@link RepositoryPrx#file(String, String)}
     * and {@link RawFileStorePrx#write(byte[], long, int)}.
     * @param isAdmin if to test a member of the <tt>system</tt> group
     * @param isRestricted if to test a user who does <em>not</em> have the <tt>WriteScriptRepo</tt> privilege
     * @param isSudo if to test attempt to subvert privilege by sudo to an unrestricted member of the <tt>system</tt> group
     * @throws Exception unexpected
     */
    @Test(dataProvider = "light administrator privilege test cases")
    public void testWriteScriptRepoPrivilegeEditingViaRepoFile(boolean isAdmin, boolean isRestricted, boolean isSudo)
            throws Exception {
        final boolean isExpectSuccess = isAdmin && !isRestricted;
        final EventContext normalUser = newUserAndGroup("rwr-r-");
        /* upload the test script as a new script */
        final String testScriptName = "Test_" + getClass().getName() + '_' + UUID.randomUUID() + ".py";
        RepositoryPrx repo = getRepository(Repository.SCRIPT);
        final OriginalFile testScript = repo.register(testScriptName, omero.rtypes.rstring(ScriptServiceTest.PYTHON_MIMETYPE));
        final long testScriptId = testScript.getId().getValue();
        final byte[] fileContentOriginal = getPythonScript().getBytes(StandardCharsets.UTF_8);
        RawFileStorePrx rfs = repo.file(testScriptName, "rw");
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
        loginNewActor(isAdmin, isSudo ? loginNewAdmin(true, null).userName : null,
                isRestricted ? AdminPrivilegeWriteScriptRepo.value : null);
        final byte[] fileContentBlank;
        try (final AutoCloseable igc = new ImplicitGroupContext(normalUser.groupId)) {
            Assert.assertEquals(getCurrentPermissions(testScript).canEdit(), isExpectSuccess);
            repo = getRepository(Repository.SCRIPT);
            fileContentBlank = new byte[fileContentOriginal.length];
            try {
                rfs = repo.file(testScriptName, "rw");
                rfs.write(fileContentBlank, 0, fileContentBlank.length);
                Assert.assertTrue(isExpectSuccess);
            } catch (ServerError se) {
                Assert.assertFalse(isExpectSuccess);
            } finally {
                try {
                    rfs.close();
                } catch (Ice.CommunicatorDestroyedException cde) {
                    /* cannot try to close */
                }
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
     * Test that users may write official scripts only if they are a member of the <tt>system</tt> group and
     * have the <tt>WriteScriptRepo</tt> privilege.
     * Attempts changing an existing file via {@link omero.api.IScriptPrx#editScript(OriginalFile, String)}.
     * @param isAdmin if to test a member of the <tt>system</tt> group
     * @param isRestricted if to test a user who does <em>not</em> have the <tt>WriteScriptRepo</tt> privilege
     * @param isSudo if to test attempt to subvert privilege by sudo to an unrestricted member of the <tt>system</tt> group
     * @throws Exception unexpected
     */
    @Test(dataProvider = "light administrator privilege test cases")
    public void testWriteScriptRepoPrivilegeEditingViaScripts(boolean isAdmin, boolean isRestricted, boolean isSudo)
            throws Exception {
        final boolean isExpectSuccess = isAdmin && !isRestricted;
        final EventContext normalUser = newUserAndGroup("rwr-r-");
        loginNewAdmin(true, null);
        /* upload the test script as a new script */
        IScriptPrx iScript = factory.getScriptService();
        final String testScriptName = "Test_" + getClass().getName() + '_' + UUID.randomUUID() + ".py";
        final long testScriptId = iScript.uploadOfficialScript(testScriptName, getPythonScript());
        /* try replacing the content of the normal user's script */
        loginNewActor(isAdmin, isSudo ? loginNewAdmin(true, null).userName : null,
                isRestricted ? AdminPrivilegeWriteScriptRepo.value : null);
        OriginalFile testScript;
        final String newScript;
        try (final AutoCloseable igc = new ImplicitGroupContext(normalUser.groupId)) {
            testScript = new OriginalFileI(testScriptId, false);
            Assert.assertEquals(getCurrentPermissions(testScript).canEdit(), isExpectSuccess);
            iScript = factory.getScriptService();
            newScript = getPythonScript() + "\n# this script is a copy of another";
            try {
                iScript.editScript(testScript, newScript);
                Assert.assertTrue(isExpectSuccess);
            } catch (ServerError se) {
                Assert.assertFalse(isExpectSuccess);
            }
        }
        /* check the permissions on the script */
        loginUser(normalUser);
        testScript = (OriginalFile) iQuery.get("OriginalFile", testScriptId);
        Assert.assertEquals(testScript.getDetails().getOwner().getId().getValue(), roles.rootId);
        Assert.assertEquals(testScript.getDetails().getGroup().getId().getValue(), roles.userGroupId);
        /* check the content of the script */
        final RawFileStorePrx rfs = factory.createRawFileStore();
        rfs.setFileId(testScriptId);
        final String currentScript = new String(rfs.read(0, (int) rfs.size()), StandardCharsets.UTF_8);
        rfs.close();
        Assert.assertEquals(currentScript, isExpectSuccess ? newScript : getPythonScript());
    }

    /**
     * Test that users may write official scripts only if they are a member of the <tt>system</tt> group and
     * have the <tt>WriteScriptRepo</tt> privilege.
     * Attempts changing an existing file via {@link omero.api.IUpdatePrx#saveAndReturnObject(IObject)}.
     * @param isAdmin if to test a member of the <tt>system</tt> group
     * @param isRestricted if to test a user who does <em>not</em> have the <tt>WriteScriptRepo</tt> privilege
     * @param isSudo if to test attempt to subvert privilege by sudo to an unrestricted member of the <tt>system</tt> group
     * @throws Exception unexpected
     */
    @Test(dataProvider = "light administrator privilege test cases")
    public void testWriteScriptRepoPrivilegeEditingViaUpdate(boolean isAdmin, boolean isRestricted, boolean isSudo)
            throws Exception {
        final boolean isExpectSuccess = isAdmin && !isRestricted;
        final EventContext normalUser = newUserAndGroup("rwr-r-");
        RepositoryPrx repo = getRepository(Repository.SCRIPT);
        final String filename = "/" + UUID.randomUUID();
        OriginalFile file = repo.register(filename, null);
        loginNewActor(isAdmin, isSudo ? loginNewAdmin(true, null).userName : null,
                isRestricted ? AdminPrivilegeWriteScriptRepo.value : null);
        try (final AutoCloseable igc = new ImplicitGroupContext(normalUser.groupId)) {
            file = (OriginalFile) iQuery.get("OriginalFile", file.getId().getValue());
            Assert.assertEquals(getCurrentPermissions(file).canEdit(), isExpectSuccess);
            final String newFilename = "Test_" + getClass().getName() + '_' + UUID.randomUUID();
            file.setName(omero.rtypes.rstring(newFilename));
            try {
                file = (OriginalFile) iUpdate.saveAndReturnObject(file);
                Assert.assertEquals(file.getName().getValue(), newFilename);
                Assert.assertTrue(isExpectSuccess);
            } catch (ServerError se) {
                Assert.assertFalse(isExpectSuccess);
            }
        }
    }

    /**
     * Test that {@link omero.api.IAdminPrx#getCurrentAdminPrivileges()} returns the expected list of privileges.
     * @param isAdmin if to test a member of the <tt>system</tt> group
     * @param isRestricted if to test a user who does <em>not</em> have the <tt>ReadSession</tt> privilege
     * @param isSudo if to test attempt to subvert privilege by sudo to an unrestricted member of the <tt>system</tt> group
     * @throws Exception unexpected
     */
    @Test(dataProvider = "light administrator privilege test cases")
    public void testGetCurrentPrivileges(boolean isAdmin, boolean isRestricted, boolean isSudo) throws Exception {
        loginNewActor(isAdmin, isSudo ? loginNewAdmin(true, null).userName : null,
                isRestricted ? AdminPrivilegeReadSession.value : null);
        final Set<String> privileges = new HashSet<>();
        for (final AdminPrivilege privilege : iAdmin.getCurrentAdminPrivileges()) {
            privileges.add(privilege.getValue().getValue());
        }
        if (isAdmin) {
            Assert.assertFalse(privileges.isEmpty());
            if (isRestricted) {
                Assert.assertFalse(privileges.contains(AdminPrivilegeReadSession.value));
            } else {
                Assert.assertTrue(privileges.contains(AdminPrivilegeReadSession.value));
            }
        } else {
            Assert.assertTrue(privileges.isEmpty());
        }
    }

    /**
     * Test that {@link omero.api.IAdminPrx#getEventContext()} has the expected sudo status and list of privileges.
     * @param isAdmin if to test a member of the <tt>system</tt> group
     * @param isRestricted if to test a user who does <em>not</em> have the <tt>ReadSession</tt> privilege
     * @param isSudo if to test attempt to subvert privilege by sudo to an unrestricted member of the <tt>system</tt> group
     * @throws Exception unexpected
     */
    @Test(dataProvider = "light administrator privilege test cases")
    public void testGetEventContext(boolean isAdmin, boolean isRestricted, boolean isSudo) throws Exception {
        loginNewActor(isAdmin, isSudo ? loginNewAdmin(true, null).userName : null,
                isRestricted ? AdminPrivilegeReadSession.value : null);
        final EventContext eventContext = iAdmin.getEventContext();
        if (isSudo) {
            Assert.assertNotNull(eventContext.sudoerId);
            Assert.assertNotNull(eventContext.sudoerName);
        } else {
            Assert.assertNull(eventContext.sudoerId);
            Assert.assertNull(eventContext.sudoerName);
        }
        final List<String> privileges =  eventContext.adminPrivileges;
        if (isAdmin) {
            Assert.assertFalse(privileges.isEmpty());
            if (isRestricted) {
                Assert.assertFalse(privileges.contains(AdminPrivilegeReadSession.value));
            } else {
                Assert.assertTrue(privileges.contains(AdminPrivilegeReadSession.value));
            }
        } else {
            Assert.assertTrue(privileges.isEmpty());
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

    /**
     * @return a variety of test cases for light administrator privileges without an option for trying sudo
     */
    @DataProvider(name = "light administrator privilege test cases (without sudo)")
    public Object[][] provideAdminPrivilegeCasesNoSudo() {
        final List<Object[]> testCases = new ArrayList<Object[]>();
        for (final Object[] testCase : provideAdminPrivilegeCases()) {
            if (Boolean.FALSE.equals(testCase[2]) /* isSudo */) {
                testCases.add(Arrays.copyOfRange(testCase, 0, 2));
            }
        }
        return testCases.toArray(new Object[testCases.size()][]);
    }

    /* -=-=- TEST PERMISSIONS SURROUNDING USER AND GROUP MANAGEMENT FOR NON-ADMINISTRATORS -=-=- */

    /**
     * Test that normal users may create users only in groups that they own.
     * Attempts creation via {@link omero.api.IAdminPrx}.
     * @param isGroupOwner if to test a user who is a group owner
     * @param isSameGroup if to test creating a user in the normal user's own group
     * @throws Exception unexpected
     */
    @Test(dataProvider = "at least group owner or same group test cases")
    public void testNormalUserCreatesUserViaAdmin(boolean isGroupOwner, boolean isSameGroup) throws Exception {
        final boolean isExpectSuccess = isGroupOwner && isSameGroup;
        final EventContext actor = newUserAndGroup("rwr---", isGroupOwner);
        final Experimenter newUser = createExperimenterI(UUID.randomUUID().toString(), getClass().getSimpleName(), "Test");
        final String groupName = isSameGroup ? actor.groupName : newGroupAddUser("rwr---", actor.userId).getName().getValue();
        try {
            iAdmin.createUser(newUser, groupName);
            Assert.assertTrue(isExpectSuccess);
        } catch (SecurityViolation sv) {
            Assert.assertFalse(isExpectSuccess);
        }
    }

    /**
     * Test that normal users may create users only in groups that they own.
     * Attempts creation via {@link omero.api.IUpdatePrx}.
     * In fact, normal users <em>cannot</em> use {@link omero.api.IUpdatePrx} to create users.
     * @param isGroupOwner if to test a user who is a group owner
     * @param isSameGroup if to test creating a user in the normal user's own group
     * @throws Exception unexpected
     */
    @Test(dataProvider = "both group owner and same group test case")
    public void testNormalUserCreatesUserViaUpdate(boolean isGroupOwner, boolean isSameGroup) throws Exception {
        final boolean isExpectSuccess = false;  // not isGroupOwner && isSameGroup
        final EventContext actor = newUserAndGroup("rwr---", isGroupOwner);
        final Experimenter newUser = createExperimenterI(UUID.randomUUID().toString(), getClass().getSimpleName(), "Test");
        final ExperimenterGroup group;
        if (isSameGroup) {
            group = new ExperimenterGroupI(actor.groupId, false);
        } else {
            group = (ExperimenterGroup) newGroupAddUser("rwr---", actor.userId).proxy();
        }
        GroupExperimenterMapI link = new GroupExperimenterMapI();
        link.setParent(group);
        link.setChild(newUser);
        link.setOwner(omero.rtypes.rbool(false));
        newUser.addGroupExperimenterMap(link);
        link  = new GroupExperimenterMapI();
        link.setParent(new ExperimenterGroupI(roles.userGroupId, false));
        link.setChild(newUser);
        link.setOwner(omero.rtypes.rbool(false));
        newUser.addGroupExperimenterMap(link);
        try {
            /* a normal user never may use this method to create users */
            iUpdate.saveObject(newUser);
            Assert.assertTrue(isExpectSuccess);
            throw new SecurityViolation();
        } catch (SecurityViolation sv) {
            Assert.assertFalse(isExpectSuccess);
        }
    }

    /**
     * Test that normal users may add and remove members of groups that they own.
     * Attempts adding or removing member via {@link omero.api.IAdminPrx}.
     * @param isGroupOwner if to test a user who is a group owner
     * @param isSameGroup if to test adding or removing a user in the normal user's own group
     * @param isAddsLink if to test adding, otherwise removing
     * @throws Exception unexpected
     */
    @Test(dataProvider = "at least group owner or same group with adds link test cases")
    public void testNormalUserAdjustsMembershipViaAdmin(boolean isGroupOwner, boolean isSameGroup, boolean isAddsLink)
            throws Exception {
        final boolean isExpectSuccess = isGroupOwner && isSameGroup;
        final EventContext target = newUserAndGroup("rwr---", false);
        final EventContext actor = newUserAndGroup("rwr---", isGroupOwner);
        final ExperimenterGroup groupForAdjustment;
        if (isSameGroup) {
            groupForAdjustment = new ExperimenterGroupI(actor.groupId, false);
        } else {
            groupForAdjustment = new ExperimenterGroupI(newUserAndGroup("rwr---", false).groupId, false);
        }
        if (!isAddsLink) {
            addUsers(groupForAdjustment, Collections.singletonList(target.userId), false);
        }
        final Experimenter targetUser = new ExperimenterI(target.userId, false);
        try {
            if (isAddsLink) {
                iAdmin.addGroups(targetUser, Collections.singletonList(groupForAdjustment));
            } else {
                iAdmin.removeGroups(targetUser, Collections.singletonList(groupForAdjustment));
            }
            Assert.assertTrue(isExpectSuccess);
        } catch (SecurityViolation sv) {
            Assert.assertFalse(isExpectSuccess);
        }
    }

    /**
     * Test that normal users may add members of groups that they own.
     * Attempts adding member via {@link omero.api.IUpdatePrx}.
     * In fact, normal users <em>cannot</em> use {@link omero.api.IUpdatePrx} to add group members.
     * @param isGroupOwner if to test a user who is a group owner
     * @param isSameGroup if to test adding a user in the normal user's own group
     * @throws Exception unexpected
     */
    @Test(dataProvider = "both group owner and same group test case")
    public void testNormalUserAdjustsMembershipViaUpdate(boolean isGroupOwner, boolean isSameGroup) throws Exception {
        final boolean isExpectSuccess = false;  // not isGroupOwner && isSameGroup
        final EventContext target = newUserAndGroup("rwr---", false);
        final EventContext actor = newUserAndGroup("rwr---", isGroupOwner);
        final ExperimenterGroup groupForAdjustment;
        if (isSameGroup) {
            groupForAdjustment = new ExperimenterGroupI(actor.groupId, false);
        } else {
            groupForAdjustment = new ExperimenterGroupI(newUserAndGroup("rwr---", false).groupId, false);
        }
        final GroupExperimenterMap link = (GroupExperimenterMap) iQuery.findByQuery(
                "FROM GroupExperimenterMap WHERE parent.id = :group_id AND child.id = :user_id",
                new ParametersI().addLong("group_id", target.groupId).addLong("user_id", target.userId));
        link.setParent(groupForAdjustment);
        try {
            /* a normal user never may use this method to create users */
            iUpdate.saveObject(link);
            Assert.assertTrue(isExpectSuccess);
            throw new SecurityViolation();
        } catch (SecurityViolation sv) {
            Assert.assertFalse(isExpectSuccess);
        }
    }

    /**
     * Test that normal users may remove members of groups that they own.
     * Attempts removing member via {@link omero.cmd.Delete2}.
     * In fact, normal users <em>cannot</em> use {@link omero.cmd.Delete2} to remove group members.
     * @param isGroupOwner if to test a user who is a group owner
     * @param isSameGroup if to test removing a user in the normal user's own group
     * @throws Exception unexpected
     */
    @Test(dataProvider = "both group owner and same group test case")
    public void testNormalUserDeletesMembershipViaRequest(boolean isGroupOwner, boolean isSameGroup) throws Exception {
        final boolean isExpectSuccess = false;  // not isGroupOwner && isSameGroup
        final EventContext target = newUserAndGroup("rwr---", false);
        final EventContext actor = newUserAndGroup("rwr---", isGroupOwner);
        final ExperimenterGroup groupForAdjustment;
        if (isSameGroup) {
            groupForAdjustment = new ExperimenterGroupI(actor.groupId, false);
        } else {
            groupForAdjustment = new ExperimenterGroupI(newUserAndGroup("rwr---", false).groupId, false);
        }
        addUsers(groupForAdjustment, Collections.singletonList(target.userId), false);
        final GroupExperimenterMap link = (GroupExperimenterMap) iQuery.findByQuery(
                "FROM GroupExperimenterMap WHERE parent.id = :group_id AND child.id = :user_id",
                new ParametersI().addLong("group_id", groupForAdjustment.getId()).addLong("user_id", target.userId));
        /* a normal user never may use this method to remove group memberships */
        doChange(client, factory, Requests.delete().target(link).build(), isExpectSuccess);
    }

    /**
     * @return test cases with <q>group owner</q> or <q>same group</q> set
     */
    @DataProvider(name = "at least group owner or same group test cases")
    public Object[][] provideGroupOwnerOrSameGroupTestCases() {
        int index = 0;
        final int IS_GROUP_OWNER = index++;
        final int IS_SAME_GROUP = index++;

        final boolean[] booleanCases = new boolean[]{false, true};

        final List<Object[]> testCases = new ArrayList<Object[]>();

        for (final boolean isGroupOwner : booleanCases) {
            for (final boolean isSameGroup : booleanCases) {
                if (!(isGroupOwner || isSameGroup)) {
                    /* not interesting */
                    continue;
                }
                final Object[] testCase = new Object[index];
                testCase[IS_GROUP_OWNER] = isGroupOwner;
                testCase[IS_SAME_GROUP] = isSameGroup;
                // DEBUG  if (isGroupOwner == false && isSameGroup == true)
                testCases.add(testCase);
            }
        }

        return testCases.toArray(new Object[testCases.size()][]);
    }

    /**
     * @return test cases with <q>group owner</q> or <q>same group</q> set and also <q>adds link</q>
     */
    @DataProvider(name = "at least group owner or same group with adds link test cases")
    public Object[][] provideGroupOwnerOrSameGroupWithAddsLinkTestCases() {
        int index = 0;
        final int IS_GROUP_OWNER = index++;
        final int IS_SAME_GROUP = index++;
        final int IS_ADDS_LINK = index++;

        final boolean[] booleanCases = new boolean[]{false, true};

        final List<Object[]> testCases = new ArrayList<Object[]>();

        for (final boolean isGroupOwner : booleanCases) {
            for (final boolean isSameGroup : booleanCases) {
                for (final boolean isAddsLink : booleanCases) {
                    if (!(isGroupOwner || isSameGroup)) {
                        /* not interesting */
                        continue;
                    }
                    final Object[] testCase = new Object[index];
                    testCase[IS_GROUP_OWNER] = isGroupOwner;
                    testCase[IS_SAME_GROUP] = isSameGroup;
                    testCase[IS_ADDS_LINK] = isAddsLink;
                    // DEBUG  if (isGroupOwner == false && isSameGroup == true && isAddsLink == true)
                    testCases.add(testCase);
                }
            }
        }

        return testCases.toArray(new Object[testCases.size()][]);
    }

    /**
     * @return test case with <q>group owner</q> and <q>same group</q> set
     */
    @DataProvider(name = "both group owner and same group test case")
    public Object[][] provideGroupOwnerAndSameGroupTestCases() {
        int index = 0;
        final int IS_GROUP_OWNER = index++;
        final int IS_SAME_GROUP = index++;

        final boolean[] booleanCases = new boolean[]{false, true};

        final List<Object[]> testCases = new ArrayList<Object[]>();

        for (final boolean isGroupOwner : booleanCases) {
            for (final boolean isSameGroup : booleanCases) {
                if (!(isGroupOwner && isSameGroup)) {
                    /* not interesting */
                    continue;
                }
                final Object[] testCase = new Object[index];
                testCase[IS_GROUP_OWNER] = isGroupOwner;
                testCase[IS_SAME_GROUP] = isSameGroup;
                // DEBUG  if (isGroupOwner == true && isSameGroup == true)
                testCases.add(testCase);
            }
        }

        return testCases.toArray(new Object[testCases.size()][]);
    }

    /* -=-=- TEST PERMISSIONS SURROUNDING USER AND GROUP MANAGEMENT FOR ADMINISTRATORS -=-=- */

    /**
     * Test that administrators may create users only with no more privileges than they themselves have.
     * Attempts creating user via {@link omero.api.IAdminPrx}.
     * @param isHasSudo if the administrator has the <tt>Sudo</tt> privilege
     * @param isGrantsSudo if the created user should be given the <tt>Sudo</tt> privilege
     * @param isCreatesAdmin if the created user should be an administrator
     * @throws Exception unexpected
     */
    @Test(dataProvider = "privilege elevation test cases")
    public void testAdminCreatesUserViaAdmin(boolean isHasSudo, boolean isGrantsSudo, boolean isCreatesAdmin) throws Exception {
        final boolean isExpectSuccess = isHasSudo || !isGrantsSudo || !isCreatesAdmin;
        final String groupName = isCreatesAdmin ? roles.systemGroupName : newUserAndGroup("rwr---", false).groupName;
        loginNewAdmin(true, isHasSudo ? null : AdminPrivilegeSudo.value);
        final Experimenter newUser = createExperimenterI(UUID.randomUUID().toString(), getClass().getSimpleName(), "Test");
        final String sudoConfigName = AdminPrivilege.class.getSimpleName() + ':' + AdminPrivilegeSudo.value;
        if (!isGrantsSudo) {
            newUser.setConfig(Collections.singletonList(new NamedValue(sudoConfigName, Boolean.toString(false))));
        }
        try {
            final long newUserId = iAdmin.createUser(newUser, groupName);
            Assert.assertTrue(isExpectSuccess);
            boolean newUserHasSudo = false;
            for (final AdminPrivilege privilege : iAdmin.getAdminPrivileges(new ExperimenterI(newUserId, false))) {
                if (AdminPrivilegeSudo.value.equals(privilege.getValue().getValue())) {
                    newUserHasSudo = true;
                    break;
                }
            }
            Assert.assertEquals(newUserHasSudo, isGrantsSudo && isCreatesAdmin);
        } catch (SecurityViolation sv) {
            Assert.assertFalse(isExpectSuccess);
        }
    }

    /**
     * Test that administrators may create users only with no more privileges than they themselves have.
     * Attempts creating user via {@link omero.api.IUpdatePrx}.
     * @param isHasSudo if the administrator has the <tt>Sudo</tt> privilege
     * @param isGrantsSudo if the created user should be given the <tt>Sudo</tt> privilege
     * @param isCreatesAdmin if the created user should be an administrator
     * @throws Exception unexpected
     */
    @Test(dataProvider = "privilege elevation test cases")
    public void testAdminCreatesUserViaUpdate(boolean isHasSudo, boolean isGrantsSudo, boolean isCreatesAdmin) throws Exception {
        final boolean isExpectSuccess = isHasSudo || !isGrantsSudo || !isCreatesAdmin;
        final long groupId = isCreatesAdmin ? roles.systemGroupId : newUserAndGroup("rwr---", false).groupId;
        loginNewAdmin(true, isHasSudo ? null : AdminPrivilegeSudo.value);
        final Experimenter newUser = createExperimenterI(UUID.randomUUID().toString(), getClass().getSimpleName(), "Test");
        final String sudoConfigName = AdminPrivilege.class.getSimpleName() + ':' + AdminPrivilegeSudo.value;
        if (!isGrantsSudo) {
            newUser.setConfig(Collections.singletonList(new NamedValue(sudoConfigName, Boolean.toString(false))));
        }
        GroupExperimenterMapI link = new GroupExperimenterMapI();
        link.setParent(new ExperimenterGroupI(groupId, false));
        link.setChild(newUser);
        link.setOwner(omero.rtypes.rbool(false));
        newUser.addGroupExperimenterMap(link);
        link  = new GroupExperimenterMapI();
        link.setParent(new ExperimenterGroupI(roles.userGroupId, false));
        link.setChild(newUser);
        link.setOwner(omero.rtypes.rbool(false));
        newUser.addGroupExperimenterMap(link);
        try {
            final long newUserId = iUpdate.saveAndReturnObject(newUser).getId().getValue();
            Assert.assertTrue(isExpectSuccess);
            boolean newUserHasSudo = false;
            for (final AdminPrivilege privilege : iAdmin.getAdminPrivileges(new ExperimenterI(newUserId, false))) {
                if (AdminPrivilegeSudo.value.equals(privilege.getValue().getValue())) {
                    newUserHasSudo = true;
                    break;
                }
            }
            Assert.assertEquals(newUserHasSudo, isGrantsSudo && isCreatesAdmin);
        } catch (ServerError se) {
            Assert.assertFalse(isExpectSuccess);
        }
    }

    /**
     * Test that administrators may remove members of groups.
     * Attempts removing member via {@link omero.cmd.Delete2}.
     * @param isHasSudo if the administrator has the <tt>Sudo</tt> privilege
     * @param isTargetSudo if the user to be removed has the <tt>Sudo</tt> privilege
     * @param isGroupSystem if to remove the user from the <tt>system</tt> group, otherwise another
     * @throws Exception unexpected
     */
    @Test(dataProvider = "privilege elevation expecting always pass test cases")
    public void testAdminDeletesMembershipViaRequest(boolean isHasSudo, boolean isTargetSudo, boolean isGroupSystem)
            throws Exception {
        final boolean isExpectSuccess = true;
        final ExperimenterGroup otherGroup = new ExperimenterGroupI(newUserAndGroup("rwr---", false).groupId, false);
        final ExperimenterGroup groupForAdjustment;
        if (isGroupSystem) {
            groupForAdjustment = new ExperimenterGroupI(roles.systemGroupId, false);
        } else {
            groupForAdjustment = otherGroup;
        }
        final EventContext target = loginNewAdmin(true, isTargetSudo ? null : AdminPrivilegeSudo.value);
        addUsers(otherGroup, Collections.singletonList(target.userId), false);
        loginNewAdmin(true, isHasSudo ? null : AdminPrivilegeSudo.value);
        addUsers(groupForAdjustment, Collections.singletonList(target.userId), false);
        final GroupExperimenterMap link = (GroupExperimenterMap) iQuery.findByQuery(
                "FROM GroupExperimenterMap WHERE parent.id = :group_id AND child.id = :user_id",
                new ParametersI().addLong("group_id", groupForAdjustment.getId()).addLong("user_id", target.userId));
        doChange(client, factory, Requests.delete().target(link).build(), isExpectSuccess);
    }

    /**
     * Test that administrators may give users only privileges that they themselves have.
     * Attempts editing privileges via {@link omero.api.IAdminPrx}.
     * @param isHasSudo if the administrator has the <tt>Sudo</tt> privilege
     * @param isGrantsSudo if the target user should be given the <tt>Sudo</tt> privilege
     * @param isEditsAdmin if the target user should be an administrator
     * @throws Exception unexpected
     */
    @Test(dataProvider = "privilege elevation test cases")
    public void testAdminEditsAdminPrivilegesViaAdmin(boolean isHasSudo, boolean isGrantsSudo, boolean isEditsAdmin)
            throws Exception {
        final boolean isExpectSuccess = isHasSudo || !isGrantsSudo || !isEditsAdmin;
        final EventContext target = loginNewAdmin(isEditsAdmin, AdminPrivilegeSudo.value);
        final Experimenter targetUser = new ExperimenterI(target.userId, false);
        loginNewAdmin(true, isHasSudo ? null : AdminPrivilegeSudo.value);
        final List<AdminPrivilege> privileges = iAdmin.getAdminPrivileges(targetUser);
        for (final AdminPrivilege privilege : allPrivileges) {
            if (isGrantsSudo && AdminPrivilegeSudo.value.equals(privilege.getValue().getValue())) {
                privileges.add(privilege);
            } else if (AdminPrivilegeWriteFile.value.equals(privilege.getValue().getValue())) {
                privileges.remove(privilege);
            }
        }
        try {
            iAdmin.setAdminPrivileges(targetUser, privileges);
            Assert.assertTrue(isExpectSuccess);
        } catch (ServerError sv) {
            Assert.assertFalse(isExpectSuccess);
        }
    }

    /**
     * Test that administrators may give users only privileges that they themselves have.
     * Attempts editing privileges via {@link omero.api.IUpdatePrx}.
     * @param isHasSudo if the administrator has the <tt>Sudo</tt> privilege
     * @param isGrantsSudo if the target user should be given the <tt>Sudo</tt> privilege
     * @param isEditsAdmin if the target user should be an administrator
     * @throws Exception unexpected
     */
    @Test(dataProvider = "privilege elevation test cases")
    public void testAdminEditsAdminPrivilegesViaUpdate(boolean isHasSudo, boolean isGrantsSudo, boolean isEditsAdmin)
            throws Exception {
        final boolean isExpectSuccess = isHasSudo || !isGrantsSudo || !isEditsAdmin;
        final EventContext actor = loginNewAdmin(true, isHasSudo ? null : AdminPrivilegeSudo.value);
        Experimenter newUser = createExperimenterI(UUID.randomUUID().toString(), getClass().getSimpleName(), "Test");
        final long newUserId;
        if (isEditsAdmin) {
            newUserId = iAdmin.createRestrictedSystemUser(newUser, Collections.<AdminPrivilege>emptyList());
        } else {
            final String groupName = newUserAndGroup("rwr---", false).groupName;
            loginUser(actor);
            newUserId = iAdmin.createUser(newUser, groupName);
        }
        newUser = (Experimenter) iQuery.get("Experimenter", newUserId);
        final String sudoConfigName = AdminPrivilege.class.getSimpleName() + ':' + AdminPrivilegeSudo.value;
        if (isGrantsSudo) {
            newUser.setConfig(Collections.singletonList(new NamedValue(sudoConfigName, Boolean.toString(true))));
        }
        try {
            iUpdate.saveObject(newUser);
            Assert.assertTrue(isExpectSuccess);
            boolean newUserHasSudo = false;
            for (final AdminPrivilege privilege : iAdmin.getAdminPrivileges(new ExperimenterI(newUserId, false))) {
                if (AdminPrivilegeSudo.value.equals(privilege.getValue().getValue())) {
                    newUserHasSudo = true;
                    break;
                }
            }
            Assert.assertEquals(newUserHasSudo, isGrantsSudo && isEditsAdmin);
        } catch (ServerError se) {
            Assert.assertFalse(isExpectSuccess);
        }
    }

    /**
     * Test that administrators may give users only privileges that they themselves have.
     * Attempts adjusting group membership via {@link omero.api.IAdminPrx}.
     * @param isHasSudo if the administrator has the <tt>Sudo</tt> privilege
     * @param isTargetSudo if the target user should have the <tt>Sudo</tt> privilege
     * @param isGroupSystem if to test editing membership of the <tt>system</tt> group
     * @param isAddsLink if to test adding, otherwise removing
     * @throws Exception unexpected
     */
    @Test(dataProvider = "privilege elevation test cases with adds link")
    public void testAdminEditsMembershipViaAdmin(boolean isHasSudo, boolean isTargetSudo, boolean isGroupSystem, boolean isAddsLink)
            throws Exception {
        final boolean isExpectSuccess = isHasSudo || !isTargetSudo || !isGroupSystem || !isAddsLink;
        final ExperimenterGroup otherGroup = new ExperimenterGroupI(newUserAndGroup("rwr---", false).groupId, false);
        final ExperimenterGroup groupForAdjustment;
        if (isGroupSystem) {
            groupForAdjustment = new ExperimenterGroupI(roles.systemGroupId, false);
        } else {
            groupForAdjustment = otherGroup;
        }
        final EventContext target = loginNewAdmin(false, isTargetSudo ? null : AdminPrivilegeSudo.value);
        if (!isAddsLink) {
            addUsers(otherGroup, Collections.singletonList(target.userId), false);
        }
        loginNewAdmin(true, isHasSudo ? null : AdminPrivilegeSudo.value);
        final Experimenter targetUser = new ExperimenterI(target.userId, false);
        try {
            if (isAddsLink) {
                iAdmin.addGroups(targetUser, Collections.singletonList(groupForAdjustment));
            } else {
                iAdmin.removeGroups(targetUser, Collections.singletonList(groupForAdjustment));
            }
            Assert.assertTrue(isExpectSuccess);
        } catch (SecurityViolation sv) {
            Assert.assertFalse(isExpectSuccess);
        }
    }

    /**
     * Test that administrators may give users only privileges that they themselves have.
     * Attempts adjusting group membership via {@link omero.api.IUpdatePrx}.
     * @param isHasSudo if the administrator has the <tt>Sudo</tt> privilege
     * @param isTargetSudo if the target user should have the <tt>Sudo</tt> privilege
     * @param isGroupSystem if to test editing membership of the <tt>system</tt> group
     * @throws Exception unexpected
     */
    @Test(dataProvider = "privilege elevation test cases")
    public void testAdminEditsMembershipViaUpdate(boolean isHasSudo, boolean isTargetSudo, boolean isGroupSystem) throws Exception {
        final boolean isExpectSuccess = isHasSudo || !isTargetSudo || !isGroupSystem;
        final ExperimenterGroup otherGroup = new ExperimenterGroupI(newUserAndGroup("rwr---", false).groupId, false);
        final ExperimenterGroup groupForAdjustment;
        if (isGroupSystem) {
            groupForAdjustment = new ExperimenterGroupI(roles.systemGroupId, false);
        } else {
            groupForAdjustment = otherGroup;
        }
        final EventContext target = newUserAndGroup("rwr---", false);
        final List<AdminPrivilege> privileges = new ArrayList<>();
        if (isTargetSudo) {
            for (final AdminPrivilege privilege : allPrivileges) {
                if (AdminPrivilegeSudo.value.equals(privilege.getValue().getValue())) {
                    privileges.add(privilege);
                    break;
                }
            }
        }
        root.getSession().getAdminService().setAdminPrivileges(new ExperimenterI(target.userId, false), privileges);
        loginNewAdmin(true, isHasSudo ? null : AdminPrivilegeSudo.value);
        final GroupExperimenterMap link = (GroupExperimenterMap) iQuery.findByQuery(
                "FROM GroupExperimenterMap WHERE parent.id = :group_id AND child.id = :user_id",
                new ParametersI().addLong("group_id", target.groupId).addLong("user_id", target.userId));
        link.setParent(groupForAdjustment);
        try {
            iUpdate.saveObject(link);
            Assert.assertTrue(isExpectSuccess);
        } catch (ServerError se) {
            Assert.assertFalse(isExpectSuccess);
        }
    }

    /**
     * @return test cases that explore privilege elevation risk
     */
    @DataProvider(name = "privilege elevation test cases")
    public Object[][] providePrivilegeElevationTestCases() {
        int index = 0;
        final int IS_ACTOR_SUDO = index++;
        final int IS_TARGET_SUDO = index++;
        final int IS_TARGET_SYSTEM = index++;

        final boolean[] booleanCases = new boolean[]{false, true};

        final List<Object[]> testCases = new ArrayList<Object[]>();

        for (final boolean isActorSudo : booleanCases) {
            for (final boolean isTargetSudo : booleanCases) {
                for (final boolean isTargetSystem : booleanCases) {
                    if ((isActorSudo || !isTargetSudo) && !isTargetSystem) {
                        /* not interesting */
                        continue;
                    }
                    final Object[] testCase = new Object[index];
                    testCase[IS_ACTOR_SUDO] = isActorSudo;
                    testCase[IS_TARGET_SUDO] = isTargetSudo;
                    testCase[IS_TARGET_SYSTEM] = isTargetSystem;
                    // DEBUG  if (isActorSudo == false && isTargetSudo == false && isTargetSystem == true)
                    testCases.add(testCase);
                }
            }
        }

        return testCases.toArray(new Object[testCases.size()][]);
    }

    /**
     * @return test cases that explore privilege elevation risk also with <q>adds link</q>
     */
    @DataProvider(name = "privilege elevation test cases with adds link")
    public Object[][] providePrivilegeElevationWithAddsLinkTestCases() {
        int index = 0;
        final int IS_ACTOR_SUDO = index++;
        final int IS_TARGET_SUDO = index++;
        final int IS_TARGET_SYSTEM = index++;
        final int IS_ADDS_LINK = index++;

        final boolean[] booleanCases = new boolean[]{false, true};

        final List<Object[]> testCases = new ArrayList<Object[]>();

        for (final boolean isActorSudo : booleanCases) {
            for (final boolean isTargetSudo : booleanCases) {
                for (final boolean isTargetSystem : booleanCases) {
                    for (final boolean isAddsLink : booleanCases) {
                        if ((isActorSudo || !isTargetSudo) && !isTargetSystem) {
                            /* not interesting */
                            continue;
                        }
                        final Object[] testCase = new Object[index];
                        testCase[IS_ACTOR_SUDO] = isActorSudo;
                        testCase[IS_TARGET_SUDO] = isTargetSudo;
                        testCase[IS_TARGET_SYSTEM] = isTargetSystem;
                        testCase[IS_ADDS_LINK] = isAddsLink;
                        // DEBUG  if (isActorSudo == false && isTargetSudo == false && isTargetSystem == true && isAddsLink == true)
                        testCases.add(testCase);
                    }
                }
            }
        }

        return testCases.toArray(new Object[testCases.size()][]);
    }

    /**
     * @return test cases that explore privilege elevation risk for safe operations
     */
    @DataProvider(name = "privilege elevation expecting always pass test cases")
    public Object[][] providePrivilegeElevationAlwaysPassTestCases() {
        int index = 0;
        final int IS_ACTOR_SUDO = index++;
        final int IS_TARGET_SUDO = index++;
        final int IS_TARGET_SYSTEM = index++;

        final boolean[] booleanCases = new boolean[]{false, true};

        final List<Object[]> testCases = new ArrayList<Object[]>();

        for (final boolean isActorSudo : booleanCases) {
            for (final boolean isTargetSudo : booleanCases) {
                for (final boolean isTargetSystem : booleanCases) {
                    if (isActorSudo || !isTargetSudo) {
                        /* not interesting */
                        continue;
                    }
                    final Object[] testCase = new Object[index];
                    testCase[IS_ACTOR_SUDO] = isActorSudo;
                    testCase[IS_TARGET_SUDO] = isTargetSudo;
                    testCase[IS_TARGET_SYSTEM] = isTargetSystem;
                    // DEBUG  if (isActorSudo == false && isTargetSudo == true && isTargetSystem == true)
                    testCases.add(testCase);
                }
            }
        }

        return testCases.toArray(new Object[testCases.size()][]);
    }
}
