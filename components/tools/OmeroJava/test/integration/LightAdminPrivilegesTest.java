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

import java.io.File;
import java.io.IOException;
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
import omero.gateway.util.Requests;
import omero.gateway.util.Requests.Delete2Builder;
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
import omero.model.enums.AdminPrivilegeDeleteOwned;
import omero.model.enums.AdminPrivilegeDeleteScriptRepo;
import omero.model.enums.AdminPrivilegeModifyGroup;
import omero.model.enums.AdminPrivilegeModifyGroupMembership;
import omero.model.enums.AdminPrivilegeModifyUser;
import omero.model.enums.AdminPrivilegeReadSession;
import omero.model.enums.AdminPrivilegeSudo;
import omero.model.enums.AdminPrivilegeWriteFile;
import omero.model.enums.AdminPrivilegeWriteOwned;
import omero.model.enums.AdminPrivilegeWriteScriptRepo;
import omero.model.enums.ChecksumAlgorithmMurmur3128;
import omero.model.enums.ChecksumAlgorithmSHA1160;
import omero.sys.EventContext;
import omero.sys.ParametersI;
import omero.sys.Principal;
import omero.util.TempFileManager;

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
 * @since 5.3.0
 */
public class LightAdminPrivilegesTest extends AbstractServerImportTest {

    private static final TempFileManager TEMPORARY_FILE_MANAGER = new TempFileManager(
            "test-" + LightAdminPrivilegesTest.class.getSimpleName());

    private ImmutableSet<AdminPrivilege> allPrivileges = null;

    private File fakeImageFile = null;

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
     * Create a fake image file for use in import tests.
     * @throws IOException unexpected
     */
    @BeforeClass
    public void createFakeImageFile() throws IOException {
        final File temporaryDirectory = TEMPORARY_FILE_MANAGER.createPath("images", null, true);
        fakeImageFile = new File(temporaryDirectory, "image.fake");
        fakeImageFile.createNewFile();
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
     * Sudo to the given user.
     * @param targetName the name of a user
     * @return context for a session owned by the given user
     * @throws Exception if the sudo could not be performed
     */
    private EventContext sudo(String targetName) throws Exception {
        final Principal principal = new Principal();
        principal.name = targetName;
        final Session session = factory.getSessionService().createSessionWithTimeout(principal, 100 * 1000);
        final omero.client client = newOmeroClient();
        final String sessionUUID = session.getUuid().getValue();
        client.createSession(sessionUUID, sessionUUID);
        return init(client);
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
     * @since 5.3.0
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
        client.getImplicitContext().put("omero.group", Long.toString(otherGroupId));
        doChange(client, factory, Requests.chgrp().target(folder).toGroup(otherGroupId).build(), isExpectSuccess);
        if (isExpectSuccess) {
            folder = (Folder) iQuery.get("Folder", folder.getId().getValue());
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
        final EventContext normalUser = newUserAndGroup("rwr-r-");
        final ExperimenterGroup otherGroup = newGroupAddUser("rwr-r-", normalUser.userId);
        Folder folder = (Folder) iUpdate.saveAndReturnObject(mmFactory.simpleFolder());
        Assert.assertEquals(folder.getDetails().getGroup().getId().getValue(), normalUser.groupId);
        loginNewActor(isAdmin, isSudo ? loginNewAdmin(true, null).userName : null, isRestricted ? AdminPrivilegeChgrp.value : null);
        client.getImplicitContext().put("omero.group", Long.toString(normalUser.groupId));
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
        client.getImplicitContext().put("omero.group", Long.toString(normalUser.groupId));
        doChange(client, factory, Requests.chown().target(folder).toUser(otherUser.userId).build(), isExpectSuccess);
        if (isExpectSuccess) {
            folder = (Folder) iQuery.get("Folder", folder.getId().getValue());
            Assert.assertEquals(folder.getDetails().getOwner().getId().getValue(), otherUser.userId);
            Assert.assertEquals(folder.getDetails().getGroup().getId().getValue(), normalUser.groupId);
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
        client.getImplicitContext().put("omero.group", Long.toString(normalUser.groupId));
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

    /**
     * Test that users may write other users' files only if they are a member of the <tt>system</tt> group and
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
        /* fetch a script from the server */
        final List<OriginalFile> scripts = factory.getScriptService().getScriptsByMimetype(ScriptServiceTest.PYTHON_MIMETYPE);
        RawFileStorePrx rfs = factory.createRawFileStore();
        rfs.setFileId(scripts.get(0).getId().getValue());
        final byte[] fileContentOriginal = rfs.read(0, (int) rfs.size());
        rfs.close();
        /* upload the script as a new script */
        final String testScriptName = "Test_" + getClass().getName() + '_' + UUID.randomUUID() + ".py";
        RepositoryPrx repo = getRepository(Repository.OTHER);
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
        loginNewActor(isAdmin, isSudo ? loginNewAdmin(true, null).userName : null,
                isRestricted ? AdminPrivilegeDeleteFile.value : null);
        client.getImplicitContext().put("omero.group", Long.toString(normalUser.groupId));
        repo = getRepository(Repository.OTHER);
        try {
            final HandlePrx handle = repo.deletePaths(new String[] {testScriptName}, false, false);
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
        client.getImplicitContext().put("omero.group", Long.toString(normalUser.groupId));
        doChange(client, factory, Requests.delete().target(file).build(), isExpectSuccess);
    }

    /**
     * Test that users may write other users' files only if they are a member of the <tt>system</tt> group and
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
        IScriptPrx iScript = factory.getScriptService();
        /* fetch a script from the server */
        final OriginalFile scriptFile = iScript.getScriptsByMimetype(ScriptServiceTest.PYTHON_MIMETYPE).get(0);
        RawFileStorePrx rfs = factory.createRawFileStore();
        rfs.setFileId(scriptFile.getId().getValue());
        final String actualScript = new String(rfs.read(0, (int) rfs.size()), StandardCharsets.UTF_8);
        rfs.close();
        /* upload the script as a new script */
        final String testScriptName = "Test_" + getClass().getName() + '_' + UUID.randomUUID() + ".py";
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
        loginNewActor(isAdmin, isSudo ? loginNewAdmin(true, null).userName : null,
                isRestricted ? AdminPrivilegeDeleteFile.value : null);
        client.getImplicitContext().put("omero.group", Long.toString(normalUser.groupId));
        iScript = factory.getScriptService();
        try {
            iScript.deleteScript(testScriptId);
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
        client.getImplicitContext().put("omero.group", Long.toString(normalUser.groupId));
        doChange(client, factory, Requests.delete().target(folder).build(), isExpectSuccess);
    }

    /**
     * Test that users may write official scripts only if they are a member of the <tt>system</tt> group and
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
        /* fetch a script from the server */
        final List<OriginalFile> scripts = factory.getScriptService().getScriptsByMimetype(ScriptServiceTest.PYTHON_MIMETYPE);
        RawFileStorePrx rfs = factory.createRawFileStore();
        rfs.setFileId(scripts.get(0).getId().getValue());
        final byte[] fileContentOriginal = rfs.read(0, (int) rfs.size());
        rfs.close();
        /* upload the script as a new script */
        final String testScriptName = "Test_" + getClass().getName() + '_' + UUID.randomUUID() + ".py";
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
        loginNewActor(isAdmin, isSudo ? loginNewAdmin(true, null).userName : null,
                isRestricted ? AdminPrivilegeDeleteScriptRepo.value : null);
        client.getImplicitContext().put("omero.group", Long.toString(normalUser.groupId));
        repo = getRepository(Repository.SCRIPT);
        try {
            final HandlePrx handle = repo.deletePaths(new String[] {testScriptName}, false, false);
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
     * Test that users may write official scripts only if they are a member of the <tt>system</tt> group and
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
        final OriginalFile file = (OriginalFile) iUpdate.saveAndReturnObject(mmFactory.createOriginalFile());
        loginNewActor(isAdmin, isSudo ? loginNewAdmin(true, null).userName : null,
                isRestricted ? AdminPrivilegeDeleteScriptRepo.value : null);
        client.getImplicitContext().put("omero.group", Long.toString(normalUser.groupId));
        doChange(client, factory, Requests.delete().target(file).build(), isExpectSuccess);
    }

    /**
     * Test that users may write official scripts only if they are a member of the <tt>system</tt> group and
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
        IScriptPrx iScript = factory.getScriptService();
        /* fetch a script from the server */
        final OriginalFile scriptFile = iScript.getScriptsByMimetype(ScriptServiceTest.PYTHON_MIMETYPE).get(0);
        RawFileStorePrx rfs = factory.createRawFileStore();
        rfs.setFileId(scriptFile.getId().getValue());
        final String actualScript = new String(rfs.read(0, (int) rfs.size()), StandardCharsets.UTF_8);
        rfs.close();
        /* upload the script as a new script */
        loginNewAdmin(true, null);
        iScript = factory.getScriptService();
        final String testScriptName = "Test_" + getClass().getName() + '_' + UUID.randomUUID() + ".py";
        final long testScriptId = iScript.uploadOfficialScript(testScriptName, actualScript);
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
        client.getImplicitContext().put("omero.group", Long.toString(normalUser.groupId));
        iScript = factory.getScriptService();
        try {
            iScript.deleteScript(testScriptId);
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
        if (isExpectSuccess) {
            Assert.assertTrue(sessionUuids.contains(normalUser.sessionUuid));
        } else {
            Assert.assertFalse(sessionUuids.contains(normalUser.sessionUuid));
        }
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
        if (isExpectSuccess) {
            Assert.assertTrue(sessionUuids.contains(normalUser.sessionUuid));
        } else {
            Assert.assertFalse(sessionUuids.contains(normalUser.sessionUuid));
        }
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
     * have the <tt>WriteFile</tt> privilege. Attempts to write files via the import process.
     * @param isAdmin if to test a member of the <tt>system</tt> group
     * @param isRestricted if to test a user who does <em>not</em> have the <tt>WriteFile</tt> privilege
     * @param isSudo if to test attempt to subvert privilege by sudo to an unrestricted member of the <tt>system</tt> group
     * @throws Exception unexpected
     */
    @Test(dataProvider = "light administrator privilege test cases")
    public void testWriteFilePrivilegeCreationViaRepoImport(boolean isAdmin, boolean isRestricted, boolean isSudo)
            throws Exception {
        final boolean isExpectSuccess = isAdmin && !isRestricted;
        final EventContext normalUser = newUserAndGroup("rwr-r-");
        loginNewActor(isAdmin, isSudo ? loginNewAdmin(true, null).userName : null,
                isRestricted ? AdminPrivilegeWriteFile.value : null);
        client.getImplicitContext().put("omero.group", Long.toString(normalUser.groupId));
        final RString imageName = omero.rtypes.rstring(fakeImageFile.getName());
        final List<List<RType>> result = iQuery.projection(
                "SELECT id FROM OriginalFile WHERE name = :name ORDER BY id DESC LIMIT 1",
                new ParametersI().add("name", imageName));
        final long previousId = result.isEmpty() ? -1 : ((RLong) result.get(0).get(0)).getValue();
        try {
            importFileset(Collections.singletonList(fakeImageFile.getPath()));
            Assert.assertTrue(isExpectSuccess);
        } catch (ServerError se) {
            Assert.assertFalse(isExpectSuccess);
        }
        final OriginalFile remoteFile = (OriginalFile) iQuery.findByQuery(
                "FROM OriginalFile o WHERE o.id > :id AND o.name = :name",
                new ParametersI().addId(previousId).add("name", imageName));
        if (isExpectSuccess) {
            Assert.assertEquals(remoteFile.getDetails().getGroup().getId().getValue(), normalUser.groupId);
        } else {
            Assert.assertNull(remoteFile);
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
        client.getImplicitContext().put("omero.group", Long.toString(normalUser.groupId));
        repo = getRepository(Repository.OTHER);
        final String filename = userDirectory + '/' + UUID.randomUUID();
        try {
            repo.makeDir(filename, false);
            Assert.assertTrue(isExpectSuccess);
        } catch (ServerError se) {
            Assert.assertFalse(isExpectSuccess);
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
        client.getImplicitContext().put("omero.group", Long.toString(normalUser.groupId));
        repo = getRepository(Repository.OTHER);
        final String filename = userDirectory + '/' + UUID.randomUUID();
        try {
            factory.sharedResources().newTable(repo.root().getId().getValue(), filename).close();
            Assert.assertTrue(isExpectSuccess);
        } catch (Ice.LocalException se) {
            Assert.assertFalse(isExpectSuccess);
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
        client.getImplicitContext().put("omero.group", Long.toString(normalUser.groupId));
        repo = getRepository(Repository.OTHER);
        final String filename = userDirectory + '/' + UUID.randomUUID();
        try {
            repo.register(filename, null);
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
        final EventContext normalUser = newUserAndGroup("rwr-r-");
        IScriptPrx iScript = factory.getScriptService();
        /* fetch a script from the server */
        OriginalFile scriptFile = iScript.getScriptsByMimetype(ScriptServiceTest.PYTHON_MIMETYPE).get(0);
        RawFileStorePrx rfs = factory.createRawFileStore();
        rfs.setFileId(scriptFile.getId().getValue());
        final String actualScript = new String(rfs.read(0, (int) rfs.size()), StandardCharsets.UTF_8);
        rfs.close();
        /* try uploading the script as a new script in the normal user's group */
        loginNewActor(isAdmin, isSudo ? loginNewAdmin(true, null).userName : null,
                isRestricted ? AdminPrivilegeWriteFile.value : null);
        client.getImplicitContext().put("omero.group", Long.toString(normalUser.groupId));
        iScript = factory.getScriptService();
        final String testScriptName = "Test_" + getClass().getName() + '_' + UUID.randomUUID() + ".py";
        long testScriptId = -1;
        try {
            testScriptId = iScript.uploadScript(testScriptName, actualScript);
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
        client.getImplicitContext().put("omero.group", Long.toString(normalUser.groupId));
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
        loginNewActor(isAdmin, isSudo ? loginNewAdmin(true, null).userName : null,
                isRestricted ? AdminPrivilegeWriteFile.value : null);
        client.getImplicitContext().put("omero.group", Long.toString(normalUser.groupId));
        final byte[] fileContentBlank = new byte[fileContentOriginal.length];
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
     * Attempts changing the file's checksum algorithm via
     * {@link ManagedRepositoryPrx#setChecksumAlgorithm(ChecksumAlgorithm, List)}.
     * @param isAdmin if to test a member of the <tt>system</tt> group
     * @param isRestricted if to test a user who does <em>not</em> have the <tt>WriteFile</tt> privilege
     * @param isSudo if to test attempt to subvert privilege by sudo to an unrestricted member of the <tt>system</tt> group
     * @throws Exception unexpected
     */
    @Test(dataProvider = "light administrator privilege test cases")
    public void testWriteFilePrivilegeEditingViaRepoChecksum(boolean isAdmin, boolean isRestricted, boolean isSudo)
            throws Exception {
        final boolean isExpectSuccess = isAdmin && !isRestricted;
        final EventContext normalUser = newUserAndGroup("rwr-r-");
        /* import a fake image and determine its hash and hasher */
        final List<String> imageFilenames = Collections.singletonList(fakeImageFile.getPath());
        final String repoPath = importFileset(imageFilenames).sharedPath + FsFile.separatorChar;
        List<RType> results = iQuery.projection(
                "SELECT id, hasher.value, hash FROM OriginalFile WHERE name = :name AND path = :path",
                new ParametersI().add("name", omero.rtypes.rstring(fakeImageFile.getName()))
                                 .add("path", omero.rtypes.rstring(repoPath))).get(0);
        final long imageFileId = ((RLong) results.get(0)).getValue();
        final String hasherOriginal = ((RString) results.get(1)).getValue();
        final String hashOriginal = ((RString) results.get(2)).getValue();
        /* try to change the image's hasher */
        loginNewActor(isAdmin, isSudo ? loginNewAdmin(true, null).userName : null,
                isRestricted ? AdminPrivilegeWriteFile.value : null);
        client.getImplicitContext().put("omero.group", Long.toString(normalUser.groupId));
        final ManagedRepositoryPrx repo = ManagedRepositoryPrxHelper.checkedCast(getRepository(Repository.MANAGED));
        final String hasherChanged;
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
        /* fetch a script from the server */
        final List<OriginalFile> scripts = factory.getScriptService().getScriptsByMimetype(ScriptServiceTest.PYTHON_MIMETYPE);
        RawFileStorePrx rfs = factory.createRawFileStore();
        rfs.setFileId(scripts.get(0).getId().getValue());
        final byte[] fileContentOriginal = rfs.read(0, (int) rfs.size());
        rfs.close();
        /* upload the script as a new script */
        final String testScriptName = "Test_" + getClass().getName() + '_' + UUID.randomUUID() + ".py";
        RepositoryPrx repo = getRepository(Repository.OTHER);
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
        loginNewActor(isAdmin, isSudo ? loginNewAdmin(true, null).userName : null,
                isRestricted ? AdminPrivilegeWriteFile.value : null);
        client.getImplicitContext().put("omero.group", Long.toString(normalUser.groupId));
        repo = getRepository(Repository.OTHER);
        final byte[] fileContentBlank = new byte[fileContentOriginal.length];
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
        IScriptPrx iScript = factory.getScriptService();
        final List<OriginalFile> scripts = iScript.getScriptsByMimetype(ScriptServiceTest.PYTHON_MIMETYPE);
        /* fetch a script from the server */
        RawFileStorePrx rfs = factory.createRawFileStore();
        rfs.setFileId(scripts.get(0).getId().getValue());
        final String originalScript = new String(rfs.read(0, (int) rfs.size()), StandardCharsets.UTF_8);
        rfs.close();
        /* upload the script as a new script */
        final String testScriptName = "Test_" + getClass().getName() + '_' + UUID.randomUUID() + ".py";
        final long testScriptId = iScript.uploadScript(testScriptName, originalScript);
        /* try replacing the content of the normal user's script */
        loginNewActor(isAdmin, isSudo ? loginNewAdmin(true, null).userName : null,
                isRestricted ? AdminPrivilegeWriteFile.value : null);
        client.getImplicitContext().put("omero.group", Long.toString(normalUser.groupId));
        iScript = factory.getScriptService();
        final String newScript = originalScript + "\n# this script is a copy of another";
        try {
            iScript.editScript(new OriginalFileI(testScriptId, false), newScript);
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
        client.getImplicitContext().put("omero.group", Long.toString(normalUser.groupId));
        file = (OriginalFile) iQuery.get("OriginalFile", file.getId().getValue());
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
        client.getImplicitContext().put("omero.group", Long.toString(normalUser.groupId));
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
        client.getImplicitContext().put("omero.group", Long.toString(normalUser.groupId));
        folder = (Folder) iQuery.get("Folder", folder.getId().getValue());
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
        client.getImplicitContext().put("omero.group", Long.toString(normalUser.groupId));
        repo = getRepository(Repository.SCRIPT);
        final String filename = userDirectory + '/' + UUID.randomUUID();
        try {
            repo.makeDir(filename, false);
            Assert.assertTrue(isExpectSuccess);
        } catch (ServerError se) {
            Assert.assertFalse(isExpectSuccess);
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
        client.getImplicitContext().put("omero.group", Long.toString(normalUser.groupId));
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
        client.getImplicitContext().put("omero.group", Long.toString(normalUser.groupId));
        repo = getRepository(Repository.SCRIPT);
        final String filename = userDirectory + '/' + UUID.randomUUID();
        try {
            repo.register(filename, null);
            Assert.assertTrue(isExpectSuccess);
        } catch (ServerError se) {
            Assert.assertFalse(isExpectSuccess);
        }
    }

    /**
     * Test that users may write official scripts only if they are a member of the <tt>system</tt> group and
     * have the <tt>WriteScriptRepo</tt> privilege.
     * Attempts creation of a file in another user's group via {@link omero.api.IScriptPrx#uploadOfficialScript(String, String)}.
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
        IScriptPrx iScript = factory.getScriptService();
        /* fetch a script from the server */
        OriginalFile scriptFile = iScript.getScriptsByMimetype(ScriptServiceTest.PYTHON_MIMETYPE).get(0);
        RawFileStorePrx rfs = factory.createRawFileStore();
        rfs.setFileId(scriptFile.getId().getValue());
        final String actualScript = new String(rfs.read(0, (int) rfs.size()), StandardCharsets.UTF_8);
        rfs.close();
        /* try uploading the script as a new script in the normal user's group */
        loginNewActor(isAdmin, isSudo ? loginNewAdmin(true, null).userName : null,
                isRestricted ? AdminPrivilegeWriteScriptRepo.value : null);
        client.getImplicitContext().put("omero.group", Long.toString(normalUser.groupId));
        iScript = factory.getScriptService();
        final String testScriptName = "Test_" + getClass().getName() + '_' + UUID.randomUUID() + ".py";
        long testScriptId = -1;
        try {
            testScriptId = iScript.uploadOfficialScript(testScriptName, actualScript);
            Assert.assertTrue(isExpectSuccess);
        } catch (ServerError se) {
            Assert.assertFalse(isExpectSuccess);
            /* upload failed so finish here */
            return;
        }
        /* check that the new script exists in the "user" group */
        loginUser(normalUser);
        scriptFile = (OriginalFile) iQuery.get("OriginalFile", testScriptId);
        Assert.assertEquals(scriptFile.getDetails().getOwner().getId().getValue(), roles.rootId);
        Assert.assertEquals(scriptFile.getDetails().getGroup().getId().getValue(), roles.userGroupId);
        /* check if the script is correctly uploaded */
        rfs = factory.createRawFileStore();
        rfs.setFileId(testScriptId);
        final String currentScript = new String(rfs.read(0, (int) rfs.size()), StandardCharsets.UTF_8);
        rfs.close();
        Assert.assertEquals(currentScript, actualScript);
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
        /* fetch a script from the server */
        final List<OriginalFile> scripts = factory.getScriptService().getScriptsByMimetype(ScriptServiceTest.PYTHON_MIMETYPE);
        RawFileStorePrx rfs = factory.createRawFileStore();
        rfs.setFileId(scripts.get(0).getId().getValue());
        final byte[] fileContentOriginal = rfs.read(0, (int) rfs.size());
        rfs.close();
        /* upload the script as a new script */
        final String testScriptName = "Test_" + getClass().getName() + '_' + UUID.randomUUID() + ".py";
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
        loginNewActor(isAdmin, isSudo ? loginNewAdmin(true, null).userName : null,
                isRestricted ? AdminPrivilegeWriteScriptRepo.value : null);
        client.getImplicitContext().put("omero.group", Long.toString(normalUser.groupId));
        repo = getRepository(Repository.SCRIPT);
        final byte[] fileContentBlank = new byte[fileContentOriginal.length];
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
        IScriptPrx iScript = factory.getScriptService();
        final List<OriginalFile> scripts = iScript.getScriptsByMimetype(ScriptServiceTest.PYTHON_MIMETYPE);
        /* fetch a script from the server */
        RawFileStorePrx rfs = factory.createRawFileStore();
        rfs.setFileId(scripts.get(0).getId().getValue());
        final String originalScript = new String(rfs.read(0, (int) rfs.size()), StandardCharsets.UTF_8);
        rfs.close();
        /* upload the script as a new script */
        loginNewAdmin(true, null);
        iScript = factory.getScriptService();
        final String testScriptName = "Test_" + getClass().getName() + '_' + UUID.randomUUID() + ".py";
        final long testScriptId = iScript.uploadOfficialScript(testScriptName, originalScript);
        /* try replacing the content of the normal user's script */
        loginNewActor(isAdmin, isSudo ? loginNewAdmin(true, null).userName : null,
                isRestricted ? AdminPrivilegeWriteScriptRepo.value : null);
        client.getImplicitContext().put("omero.group", Long.toString(normalUser.groupId));
        iScript = factory.getScriptService();
        final String newScript = originalScript + "\n# this script is a copy of another";
        try {
            iScript.editScript(new OriginalFileI(testScriptId, false), newScript);
            Assert.assertTrue(isExpectSuccess);
        } catch (ServerError se) {
            Assert.assertFalse(isExpectSuccess);
        }
        /* check the permissions on the script */
        loginUser(normalUser);
        OriginalFile scriptFile = (OriginalFile) iQuery.get("OriginalFile", testScriptId);
        Assert.assertEquals(scriptFile.getDetails().getOwner().getId().getValue(), roles.rootId);
        Assert.assertEquals(scriptFile.getDetails().getGroup().getId().getValue(), roles.userGroupId);
        /* check the content of the script */
        rfs = factory.createRawFileStore();
        rfs.setFileId(testScriptId);
        final String currentScript = new String(rfs.read(0, (int) rfs.size()), StandardCharsets.UTF_8);
        rfs.close();
        Assert.assertEquals(currentScript, isExpectSuccess ? newScript : originalScript);
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
        client.getImplicitContext().put("omero.group", Long.toString(normalUser.groupId));
        file = (OriginalFile) iQuery.get("OriginalFile", file.getId().getValue());
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
}
