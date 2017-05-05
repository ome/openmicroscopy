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

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import omero.RLong;
import omero.RString;
import omero.RType;
import omero.SecurityViolation;
import omero.ServerError;
import omero.api.IRenderingSettingsPrx;
import omero.api.IScriptPrx;
import omero.api.RawFileStorePrx;
import omero.api.ServiceFactoryPrx;
import omero.cmd.Chown2;
import omero.gateway.util.Requests;
import omero.gateway.util.Requests.Delete2Builder;
import omero.model.AdminPrivilege;
import omero.model.AdminPrivilegeI;
import omero.model.Dataset;
import omero.model.DatasetI;
import omero.model.DatasetImageLink;
import omero.model.DatasetImageLinkI;
import omero.model.Experimenter;
import omero.model.ExperimenterGroup;
import omero.model.ExperimenterGroupI;
import omero.model.ExperimenterI;
import omero.model.FileAnnotation;
import omero.model.FileAnnotationI;
import omero.model.IObject;
import omero.model.Image;
import omero.model.ImageAnnotationLink;
import omero.model.ImageAnnotationLinkI;
import omero.model.ImageI;
import omero.model.NamedValue;
import omero.model.OriginalFile;
import omero.model.OriginalFileI;
import omero.model.PermissionsI;
import omero.model.Pixels;
import omero.model.Project;
import omero.model.ProjectDatasetLink;
import omero.model.ProjectDatasetLinkI;
import omero.model.ProjectI;
import omero.model.RectangleI;
import omero.model.RenderingDef;
import omero.model.RenderingDefI;
import omero.model.Roi;
import omero.model.RoiI;
import omero.model.Session;
import omero.model.enums.AdminPrivilegeChgrp;
import omero.model.enums.AdminPrivilegeChown;
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
 * Tests the concrete workflows of the light admins
 * @author p.walczysko@dundee.ac.uk
 * @since 5.3.0
 */
public class LightAdminRolesTest extends AbstractServerImportTest {

    private static final TempFileManager TEMPORARY_FILE_MANAGER = new TempFileManager(
            "test-" + LightAdminRolesTest.class.getSimpleName());

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
     * Assert that the given object is owned by the given owner.
     * @param object a model object
     * @param expectedOwner a user's event context
     * @throws ServerError unexpected
     */
    private void assertOwnedBy(IObject object, EventContext expectedOwner) throws ServerError {
        assertOwnedBy(Collections.singleton(object), expectedOwner);
    }

    /**
     * Assert that the given objects are owned by the given owner.
     * @param objects some model objects
     * @param expectedOwner a user's event context
     * @throws ServerError unexpected
     */
    private void assertOwnedBy(Collection<? extends IObject> objects, EventContext expectedOwner) throws ServerError {
        if (objects.isEmpty()) {
            throw new IllegalArgumentException("must assert about some objects");
        }
        for (final IObject object : objects) {
            final String objectName = object.getClass().getName() + '[' + object.getId().getValue() + ']';
            final String query = "SELECT details.owner.id FROM " + object.getClass().getSuperclass().getSimpleName() +
                    " WHERE id = " + object.getId().getValue();
            final List<List<RType>> results = iQuery.projection(query, null);
            final long actualOwnerId = ((RLong) results.get(0).get(0)).getValue();
            Assert.assertEquals(actualOwnerId, expectedOwner.userId, objectName);
        }
    }

    private ProjectDatasetLink linkProjectDataset(Project project, Dataset dataset) throws ServerError {
        if (project.isLoaded() && project.getId() != null) {
            project = (Project) project.proxy();
        }
        if (dataset.isLoaded() && dataset.getId() != null) {
            dataset = (Dataset) dataset.proxy();
        }

        final ProjectDatasetLink link = new ProjectDatasetLinkI();
        link.setParent(project);
        link.setChild(dataset);
        return (ProjectDatasetLink) iUpdate.saveAndReturnObject(link);
    }

    private DatasetImageLink linkDatasetImage(Dataset dataset, Image image) throws ServerError {
        if (dataset.isLoaded() && dataset.getId() != null) {
            dataset = (Dataset) dataset.proxy();
        }
        if (image.isLoaded() && image.getId() != null) {
            image = (Image) image.proxy();
        }

        final DatasetImageLink link = new DatasetImageLinkI();
        link.setParent(dataset);
        link.setChild(image);
        return (DatasetImageLink) iUpdate.saveAndReturnObject(link);
    }

    /**
     * Assert that the given object is owned by the given owner.
     * @param object a model object
     * @param expectedOwner a user's event context
     * @throws ServerError unexpected
     */
    private EventContext loginNewAdmin(boolean isAdmin, String permission) throws Exception {
        final EventContext ctx = loginNewAdmin(isAdmin, Arrays.asList(permission));
        return ctx;
    }

    /**
     * Create a light administrator, with a specific privilege, and log in as them.
     * All the other privileges will be set to False.
     * @param isAdmin if the user should be a member of the <tt>system</tt> group
     * @param restriction the privilege that the user should not have, or {@code null} if they should have all privileges
     * @return the new user's context
     * @throws Exception if the light administrator could not be created
     */
    private EventContext loginNewAdmin(boolean isAdmin, List <String> permissions) throws Exception {
        final EventContext ctx = isAdmin ? newUserInGroup(iAdmin.lookupGroup(roles.systemGroupName), false) : newUserAndGroup("rwr-r-");
        final ServiceFactoryPrx rootSession = root.getSession();
        Experimenter user = new ExperimenterI(ctx.userId, false);
        user = (Experimenter) rootSession.getQueryService().get("Experimenter", ctx.userId);
        final List<AdminPrivilege> privileges = new ArrayList<>();
        rootSession.getAdminService().setAdminPrivileges(user, privileges);
        for (final String permission : permissions) {
            final AdminPrivilege privilege = new AdminPrivilegeI();
            privilege.setValue(omero.rtypes.rstring(permission));
            privileges.add(privilege);
        }
        rootSession.getAdminService().setAdminPrivileges(user, privileges);
        /* avoid old session as privileges are briefly cached */
        loginUser(ctx);
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
     * Test that an ImporterAs can create new Project and Dataset
     * and import data on behalf of another user solely with <tt>Sudo</tt> privilege
     * into this Dataset. Further link the Dataset to the Project, check
     * that the link belongs to the user (not to the ImporterAs).
     * All workflows are tested here both when light admin is sudoing
     * and when he/she is not sudoing, except for Link and Import (both tested
     * only when sudoing, as the non-sudoing workflows are too complicated
     * for those two actions and thus covered by separate tests.
     * @param isAdmin if to test a member of the <tt>system</tt> group
     * @param isSudoing if to test a success of workflows where Sudoed in
     * @param permWriteOwned if to test a user who has the <tt>WriteOwned</tt> privilege
     * @param groupPermissions if to test the effect of group permission level
     * @throws Exception unexpected
     */
    @Test(dataProvider = "isSudoing and WriteOwned privileges cases")
    public void testImporterAsSudoCreateImport(boolean isSudoing, boolean permWriteOwned,
            String groupPermissions) throws Exception {
        final EventContext normalUser = newUserAndGroup(groupPermissions);
        final boolean isExpectSuccessCreate = permWriteOwned || isSudoing;
        /* set up the light admin's permissions for this test */
        List<String> permissions = new ArrayList<String>();
        permissions.add(AdminPrivilegeSudo.value);
        if (permWriteOwned) permissions.add(AdminPrivilegeWriteOwned.value);
        final EventContext lightAdmin;
        lightAdmin = loginNewAdmin(true, permissions);
        if (isSudoing) {
            try {
                sudo(new ExperimenterI(normalUser.userId, false));
                }catch (SecurityViolation sv) {
                    /* sudo expected to fail if the user is not in system group */
                }
        }

        /* First, check that the light admin (=importer As)
         * can create Project and Dataset on behalf of the normalUser
         * in the group of the normalUser in anticipation of importing
         * data for the normalUser in the next step into these containers */
        client.getImplicitContext().put("omero.group", Long.toString(normalUser.groupId));
        Project proj = mmFactory.simpleProject();
        Dataset dat = mmFactory.simpleDataset();
        Project sentProj = new ProjectI();
        Dataset sentDat = new DatasetI();
        sentDat = null;
        sentProj = null;
        /* set the normalUser as the owner of the newly created P/D but do this only
         * when the light admin is not sudoing (if sudoing, this step is not necessary
         * because the created P/D already belongs to the normalUser) */
        if (!isSudoing) {
            proj.getDetails().setOwner(new ExperimenterI(normalUser.userId, false));
            dat.getDetails().setOwner(new ExperimenterI(normalUser.userId, false));
        }
        try {
            sentProj = (Project) iUpdate.saveAndReturnObject(proj);
            sentDat = (Dataset) iUpdate.saveAndReturnObject(dat);
            Assert.assertTrue(isExpectSuccessCreate);
        } catch (ServerError se) {
            Assert.assertFalse(isExpectSuccessCreate);
        }
        /* Check the owner of the project and dataset is the normalUser in case
         * these were created */
        if (isExpectSuccessCreate) {
            assertOwnedBy(sentProj, normalUser);
            assertOwnedBy(sentDat, normalUser);
        } else {
            Assert.assertNull(sentProj);
            Assert.assertNull(sentDat);
        }
        /* finish the test if light admin is not sudoing, the further part
        of the test deals with the imports. Imports when not sudoing workflows are covered in
        other tests in this class */
        if (!isSudoing) return;

        /* check that after sudo, the light admin is able to ImportAs and target
         * the import into the just created Dataset.
         * Check thus that the light admin can import and write the original file
         * on behalf of the normalUser and into the group of normalUser */
        final RString imageName = omero.rtypes.rstring(fakeImageFile.getName());
        final List<List<RType>> result = iQuery.projection(
                "SELECT id FROM OriginalFile WHERE name = :name ORDER BY id DESC LIMIT 1",
                new ParametersI().add("name", imageName));
        final long previousId = result.isEmpty() ? -1 : ((RLong) result.get(0).get(0)).getValue();
        List<String> path = Collections.singletonList(fakeImageFile.getPath());
        importFileset(path, path.size(), sentDat);
        final OriginalFile remoteFile = (OriginalFile) iQuery.findByQuery(
                "FROM OriginalFile o WHERE o.id > :id AND o.name = :name",
                new ParametersI().addId(previousId).add("name", imageName));
        assertOwnedBy(remoteFile, normalUser);
        Assert.assertEquals(remoteFile.getDetails().getGroup().getId().getValue(), normalUser.groupId);

        /* check that the light admin when sudoed, can link the created Dataset
         * to the created Project, check the ownership of the links
         * is of the simple user */

        ProjectDatasetLink projectDatasetLink = linkProjectDataset(sentProj, sentDat);

        /* Now check the ownership of image and links
         * between image and Dataset and Dataset and Project */
        final long imageId = ((RLong) iQuery.projection(
                "SELECT id FROM Image WHERE fileset IN "
                + "(SELECT fileset FROM FilesetEntry WHERE originalFile.id = :id)",
                new ParametersI().addId(remoteFile.getId())).get(0).get(0)).getValue();
        final IObject imageDatasetLink = iQuery.findByQuery(
                "FROM DatasetImageLink WHERE child.id = :id",
                new ParametersI().addId(imageId));
        assertOwnedBy(new ImageI(imageId, false), normalUser);
        assertOwnedBy(imageDatasetLink, normalUser);
        assertOwnedBy(projectDatasetLink, normalUser);
    }

    /**
     * Test whether a light admin can delete image, Project and Dataset
     * and their respective links belonging to another
     * user. Behaviors of the system are explored when light admin
     * is and is not using <tt>Sudo</tt> privilege
     * for this action.
     * @param isAdmin if to test a member of the <tt>system</tt> group
     * @param isSudoing if to test a success of workflows where Sudoed in
     * @param permDeleteOwned if to test a user who has the <tt>DeleteOwned</tt> privilege
     * @param groupPermissions if to test the effect of group permission level
     * @throws Exception unexpected
     */
   @Test(dataProvider = "isSudoing and Delete privileges cases")
   public void testDelete(boolean isSudoing, boolean permDeleteOwned,
           String groupPermissions) throws Exception {
       /* only DeleteOwned permission is truly needed for deletion of links, dataset
        * and image (with original file) when not sudoing */
       boolean deletePassing = permDeleteOwned || isSudoing;
       final EventContext normalUser = newUserAndGroup(groupPermissions);
       /* set up the light admin's permissions for this test */
       List<String> permissions = new ArrayList<String>();
       permissions.add(AdminPrivilegeSudo.value);
       if (permDeleteOwned) permissions.add(AdminPrivilegeDeleteOwned.value);
       final EventContext lightAdmin;
       lightAdmin = loginNewAdmin(true, permissions);
       try {
           sudo(new ExperimenterI(normalUser.userId, false));
           }catch (SecurityViolation sv) {
           }
       /* create a Dataset and Project being sudoed as normalUser */
       client.getImplicitContext().put("omero.group", Long.toString(normalUser.groupId));
       Project proj = mmFactory.simpleProject();
       Dataset dat = mmFactory.simpleDataset();
       Project sentProj = new ProjectI();
       sentProj = null;
       Dataset sentDat = new DatasetI();
       sentDat = null;
       try {
           sentProj = (Project) iUpdate.saveAndReturnObject(proj);
           sentDat = (Dataset) iUpdate.saveAndReturnObject(dat);
       } catch (ServerError se) {
       }
       /* import an image for the normalUser into the normalUser's default group 
        * and target it into the created Dataset*/
       final RString imageName = omero.rtypes.rstring(fakeImageFile.getName());
       final List<List<RType>> result = iQuery.projection(
               "SELECT id FROM OriginalFile WHERE name = :name ORDER BY id DESC LIMIT 1",
               new ParametersI().add("name", imageName));
       final long previousId = result.isEmpty() ? -1 : ((RLong) result.get(0).get(0)).getValue();
       try {
           List<String> path = Collections.singletonList(fakeImageFile.getPath());
           importFileset(path, path.size(), sentDat);
       } catch (ServerError se) {
       }
       final List<RType> resultAfterImport = iQuery.projection(
               "SELECT id, details.group.id FROM OriginalFile o WHERE o.id > :id AND o.name = :name",
               new ParametersI().addId(previousId).add("name", imageName)).get(0);
       final long remoteFileId = ((RLong) resultAfterImport.get(0)).getValue();
       final long remoteFileGroupId = ((RLong) resultAfterImport.get(1)).getValue();
       assertOwnedBy((new OriginalFileI(remoteFileId, false)), normalUser);
       Assert.assertEquals(remoteFileGroupId, normalUser.groupId);
       /* link the Project and the Dataset */
       ProjectDatasetLink link = linkProjectDataset(sentProj, sentDat);
       Image image = (Image) iQuery.findByQuery(
               "FROM Image WHERE fileset IN "
               + "(SELECT fileset FROM FilesetEntry WHERE originalFile.id = :id)",
               new ParametersI().addId(remoteFileId));
       IObject datasetImageLink = iQuery.findByQuery(
               "FROM DatasetImageLink WHERE child.id = :id",
               new ParametersI().addId(image.getId()));
       IObject projectDatasetLink = iQuery.findByQuery(
               "FROM ProjectDatasetLink WHERE id = :id",
               new ParametersI().addId(link.getId()));
       /* take care of post-import workflows which do not use sudo */
       if (!isSudoing) {
           loginUser(lightAdmin);
       }
       /* Now check that the ImporterAs can delete the objects
        * created on behalf of the user. Note that deletion of the Project
        * would delete the whole hierarchy, which was successfully tested
        * during writing of this test.*/
       if (deletePassing) {
           doChange(Requests.delete().target(datasetImageLink).build());
           doChange(Requests.delete().target(projectDatasetLink).build());
           doChange(Requests.delete().target(image).build());
           doChange(Requests.delete().target(sentDat).build());
           doChange(Requests.delete().target(sentProj).build());
       }
       /* Check one of the objects for non-existence after deletion. First, logging
        * in as root, retrieve all the objects to check them later*/
       logRootIntoGroup(normalUser.groupId);
       OriginalFile retrievedRemoteFile = (OriginalFile) iQuery.findByQuery(
               "FROM OriginalFile WHERE id = :id",
               new ParametersI().addId(remoteFileId));
       Image retrievedImage = (Image) iQuery.findByQuery(
               "FROM Image WHERE fileset IN "
               + "(SELECT fileset FROM FilesetEntry WHERE originalFile.id = :id)",
               new ParametersI().addId(remoteFileId));
       Dataset retrievedDat = (Dataset) iQuery.findByQuery(
               "FROM Dataset WHERE id = :id",
               new ParametersI().addId(sentDat.getId()));
       Project retrievedProj = (Project) iQuery.findByQuery(
               "FROM Project WHERE id = :id",
               new ParametersI().addId(sentProj.getId()));
       DatasetImageLink retrievedDatasetImageLink = (DatasetImageLink) iQuery.findByQuery(
               "FROM DatasetImageLink WHERE child.id = :id",
               new ParametersI().addId(image.getId()));
       ProjectDatasetLink retrievedProjectDatasetLink = (ProjectDatasetLink) iQuery.findByQuery(
               "FROM ProjectDatasetLink WHERE child.id = :id",
               new ParametersI().addId(sentDat.getId()));
       /* now check the existence/non-existence of the objects as appropriate */
       if (deletePassing) {
           /* successful delete expected */
           Assert.assertNull(retrievedRemoteFile, "original file should be deleted");
           Assert.assertNull(retrievedImage, "image should be deleted");
           Assert.assertNull(retrievedDat, "dataset should be deleted");
           Assert.assertNull(retrievedProj, "project should be deleted");
           Assert.assertNull(retrievedDatasetImageLink, "Dat-Image link should be deleted");
           Assert.assertNull(retrievedProjectDatasetLink, "Proj-Dat link should be deleted");
       } else {
           /* no deletion should have been successful without permDeleteOwned */
           Assert.assertNotNull(retrievedRemoteFile, "original file not deleted");
           Assert.assertNotNull(retrievedImage, "image not deleted");
           Assert.assertNotNull(retrievedDat, "dataset not deleted");
           Assert.assertNotNull(retrievedProj, "project not deleted");
           Assert.assertNotNull(retrievedDatasetImageLink, "Dat-Image link not deleted");
           Assert.assertNotNull(retrievedProjectDatasetLink, "Proj-Dat link not deleted");
       }
   }

    /**
     * Test that a light admin can
     * edit the name of a project
     * on behalf of another user solely with <tt>Sudo</tt> privilege
     * or without it, using permWriteOwned privilege
     * @param isAdmin if to test a member of the <tt>system</tt> group
     * @param isSudoing if to test a success of workflows where Sudoed in
     * @param permWriteOwned if to test a user who has the <tt>WriteOwned</tt> privilege
     * @param groupPermissions if to test the effect of group permission level
     * @throws Exception unexpected
     */
    @Test(dataProvider = "isSudoing and WriteOwned privileges cases")
    public void testEdit(boolean isSudoing, boolean permWriteOwned,
            String groupPermissions) throws Exception {
        final boolean isExpectSuccess = isSudoing || permWriteOwned;
        final EventContext normalUser = newUserAndGroup(groupPermissions);
        /* set up the light admin's permissions for this test */
        List<String> permissions = new ArrayList<String>();
        permissions.add(AdminPrivilegeSudo.value);
        if (permWriteOwned) permissions.add(AdminPrivilegeWriteOwned.value);
        final EventContext lightAdmin;
        lightAdmin = loginNewAdmin(true, permissions);
        /* set up the project as the normalUser */
        loginUser(normalUser);
        Project proj = mmFactory.simpleProject();
        final String originalName = "OriginalNameOfNormalUser";
        proj.setName(omero.rtypes.rstring(originalName));
        Project sentProj = (Project) iUpdate.saveAndReturnObject(proj);
        String savedOriginalName = sentProj.getName().getValue().toString();
        loginUser(lightAdmin);
        /* being the light admin, sudo as the normalUser if this should be the case */
        if (isSudoing) {
            try {
                sudo(new ExperimenterI(normalUser.userId, false));
            } catch (SecurityViolation sv) {
            }
        }
        /* try to rename the Project as the light admin, either sudoed as normalUser or not */
        final String changedName = "ChangedNameOfLightAdmin";
        client.getImplicitContext().put("omero.group", Long.toString(normalUser.groupId));
        long id = sentProj.getId().getValue();
        final Project retrievedUnrenamedProject = (Project) iQuery.get("Project", id);
        retrievedUnrenamedProject.setName(omero.rtypes.rstring(changedName));
        if (isExpectSuccess) {/* in case no WriteOwned permission is given to light admin, and he/she is
        not sudoing, following line would throw a Security violation */
            sentProj = (Project) iUpdate.saveAndReturnObject(retrievedUnrenamedProject);
        }
        String savedChangedName = sentProj.getName().getValue().toString();
        logRootIntoGroup(normalUser.groupId);
        final String retrievedName = ((RString) iQuery.projection(
                "SELECT name FROM Project p WHERE p.id = :id",
                new ParametersI().addId(sentProj.getId())).get(0).get(0)).getValue();
        /* check that the Project still belongs to normalUser and the name of the Project
         * was changed and saved or original name is retained as appropriate */
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
     * Test that an ImporterAs can
     * chgrp on behalf of another user solely with <tt>Sudo</tt> privilege
     * only when this user is a member of both original and target groups
     * Also test that light admin can, having the <tt>Chgrp</tt>
     * privilege move another user's data into another group whether the
     * owner of the data is member of target group or not.
     * Also tests the ability of the <tt>Chgrp</tt> privilege and chgrp command
     * to sever necessary links for performing the chgrp. This is achieved by
     * having the image which is getting moved into a different group in a dataset
     * in the original group (the chgrp has to sever the DatasetImageLink to perform
     * the move (chgrp).
     * @param isAdmin if to test a member of the <tt>system</tt> group
     * @param isSudoing if to test a success of workflows where Sudoed in
     * @param permChgrp if to test a user who has the <tt>Chgrp</tt> privilege
     * @param groupPermissions if to test the effect of group permission level
     * @throws Exception unexpected
     */
    @Test(dataProvider = "isSudoing and Chgrp privileges cases")
    public void testChgrp(boolean isSudoing, boolean permChgrp,
            String groupPermissions) throws Exception {
        /* Set up a user and three groups, the user being a member of
         * two of the groups.
         */
        final EventContext normalUser = newUserAndGroup(groupPermissions);
        /* Group where the user is a member */
        final long normalUsersOtherGroupId = newGroupAddUser(groupPermissions, normalUser.userId, false).getId().getValue();
        /* Group where the user is not member */
        final long anotherGroupId = newUserAndGroup(groupPermissions).groupId;
        /* Define cases:
         * When data owner is not member of the target group,
         * Chgrp action passes only when light admin has Chgrp permission
         * and is not Sudoing (Sudoing can be thought of as destroyer of the
         * privileges, because it forces the permissions of the Sudoed-as user
         * onto the light admin).*/
        boolean chgrpNoSudoExpectSuccessAnyGroup = !isSudoing && permChgrp;
        /* When data owner is member of the target group,
         * Chgrp action passes also when light admin is
         * Sudoed as the data owner */
        boolean isExpectSuccessInMemberGroup = chgrpNoSudoExpectSuccessAnyGroup || isSudoing;
        /* create a Dataset as the normalUser and import into it */
        loginUser(normalUser);
        Dataset dat = mmFactory.simpleDataset();
        Dataset sentDat = (Dataset) iUpdate.saveAndReturnObject(dat);
        final RString imageName = omero.rtypes.rstring(fakeImageFile.getName());
        final List<List<RType>> result = iQuery.projection(
                "SELECT id FROM OriginalFile WHERE name = :name ORDER BY id DESC LIMIT 1",
                new ParametersI().add("name", imageName));
        final long previousId = result.isEmpty() ? -1 : ((RLong) result.get(0).get(0)).getValue();
        List<String> path = Collections.singletonList(fakeImageFile.getPath());
        importFileset(path, path.size(), sentDat);
        final List<RType> resultAfterImport = iQuery.projection(
                "SELECT id, details.group.id FROM OriginalFile o WHERE o.id > :id AND o.name = :name",
                new ParametersI().addId(previousId).add("name", imageName)).get(0);
        final long remoteFileId = ((RLong) resultAfterImport.get(0)).getValue();
        long remoteFileGroupId = ((RLong) resultAfterImport.get(1)).getValue();
        assertOwnedBy((new OriginalFileI(remoteFileId, false)), normalUser);
        Assert.assertEquals(remoteFileGroupId, normalUser.groupId);
        Image image = (Image) iQuery.findByQuery(
                "FROM Image WHERE fileset IN "
                + "(SELECT fileset FROM FilesetEntry WHERE originalFile.id = :id)",
                new ParametersI().addId(remoteFileId));
        /* set up the light admin's permissions for this test */
        List<String> permissions = new ArrayList<String>();
        permissions.add(AdminPrivilegeSudo.value);
        if (permChgrp) permissions.add(AdminPrivilegeChgrp.value);
        final EventContext lightAdmin;
        lightAdmin = loginNewAdmin(true, permissions);
        try {
            sudo(new ExperimenterI(normalUser.userId, false));
            }catch (SecurityViolation sv) {
            }
        /* take care of workflows which do not use sudo */
        if (!isSudoing) {
            loginUser(lightAdmin);
        }
        /*in order to find the image in whatever group, get context with group
         * set to -1 (=all groups) */
        client.getImplicitContext().put("omero.group", Long.toString(-1));
        /* try to move the image into another group of the normalUser
         * which should succeed if sudoing and also in case
         * the light admin has Chgrp permissions
         * (i.e. isExpectSuccessInMemberGroup is true) */
        doChange(client, factory, Requests.chgrp().target(image).toGroup(normalUsersOtherGroupId).build(), isExpectSuccessInMemberGroup);
        /* note in which group the image and the original file are now */
        long afterFirstChgrpImageGroupId = ((RLong) iQuery.projection(
                "SELECT details.group.id FROM Image i WHERE i.id = :id",
                new ParametersI().addId(image.getId())).get(0).get(0)).getValue();
        remoteFileGroupId = ((RLong) iQuery.projection(
                "SELECT details.group.id FROM OriginalFile o WHERE o.id = :id",
                new ParametersI().addId(remoteFileId)).get(0).get(0)).getValue();
        if (isExpectSuccessInMemberGroup) {
            Assert.assertEquals(afterFirstChgrpImageGroupId, normalUsersOtherGroupId);
            Assert.assertEquals(remoteFileGroupId, normalUsersOtherGroupId);
        } else {
            Assert.assertEquals(afterFirstChgrpImageGroupId, normalUser.groupId);
            Assert.assertEquals(remoteFileGroupId, normalUser.groupId);
        }
        /* in any case, the image should still belong to normalUser */
        assertOwnedBy(image, normalUser);

        /* try to move into another group the normalUser
         * is not a member of, which should fail in all cases
         * except the light admin has Chgrp permission and is not sudoing
         * (i.e. chgrpNoSudoExpectSuccessAnyGroup is true) */
        doChange(client, factory, Requests.chgrp().target(image).toGroup(anotherGroupId).build(),
                chgrpNoSudoExpectSuccessAnyGroup);
        long afterSecondChgrpImageGroupId = ((RLong) iQuery.projection(
                "SELECT details.group.id FROM Image i WHERE i.id = :id",
                new ParametersI().addId(image.getId())).get(0).get(0)).getValue();
        remoteFileGroupId = ((RLong) iQuery.projection(
                "SELECT details.group.id FROM OriginalFile o WHERE o.id = :id",
                new ParametersI().addId(remoteFileId)).get(0).get(0)).getValue();
        if(chgrpNoSudoExpectSuccessAnyGroup) {
            /* check that the image moved to another group */
            Assert.assertEquals(afterSecondChgrpImageGroupId, anotherGroupId);
            Assert.assertEquals(afterSecondChgrpImageGroupId, anotherGroupId);
        } else {
            /* check that the image, after this second Chgrp attempt,
             * is still in its original group
             * (stored in the afterFirstChgrpImageGroupId variable) */
            Assert.assertEquals(afterSecondChgrpImageGroupId, afterFirstChgrpImageGroupId);
            Assert.assertEquals(afterSecondChgrpImageGroupId, afterFirstChgrpImageGroupId);
        }
        /* in any case, the image should still belong to normalUser */
        assertOwnedBy(image, normalUser);
    }

    /**
     * Test that an ImporterAs cannot
     * chown on behalf of another user if sudoed in as that user.
     * Chown will be successful only when not sudoed and having
     * the <tt>Chown</tt> privilege.
     * Test is in case of private group severing the link between the Dataset and Image.
     * For this, only the Chown permissions are sufficient, no other permissions are necessary.
     * @param isAdmin if to test a member of the <tt>system</tt> group
     * @param isSudoing if to test a success of workflows where Sudoed in
     * @param permChown if to test a user who has the <tt>Chown</tt> privilege
     * @param groupPermissions if to test the effect of group permission level
     * @throws Exception unexpected
     */
    @Test(dataProvider = "isSudoing and Chown privileges cases")
    public void testChown(boolean isSudoing, boolean permChown,
            String groupPermissions) throws Exception {
        /* define the conditions for the chown passing (when not sudoing) */
        final boolean chownPassingWhenNotSudoing = permChown;
        final EventContext normalUser = newUserAndGroup(groupPermissions);
        final EventContext anotherUser = newUserAndGroup(groupPermissions);
        /* create a Dataset as the normalUser and import into it */
        loginUser(normalUser);
        Dataset dat = mmFactory.simpleDataset();
        Dataset sentDat = (Dataset) iUpdate.saveAndReturnObject(dat);
        final RString imageName = omero.rtypes.rstring(fakeImageFile.getName());
        final List<List<RType>> result = iQuery.projection(
                "SELECT id FROM OriginalFile WHERE name = :name ORDER BY id DESC LIMIT 1",
                new ParametersI().add("name", imageName));
        final long previousId = result.isEmpty() ? -1 : ((RLong) result.get(0).get(0)).getValue();
        List<String> path = Collections.singletonList(fakeImageFile.getPath());
        importFileset(path, path.size(), sentDat);
        final List<RType> resultAfterImport = iQuery.projection(
                "SELECT id, details.group.id FROM OriginalFile o WHERE o.id > :id AND o.name = :name",
                new ParametersI().addId(previousId).add("name", imageName)).get(0);
        final long remoteFileId = ((RLong) resultAfterImport.get(0)).getValue();
        long remoteFileGroupId = ((RLong) resultAfterImport.get(1)).getValue();
        assertOwnedBy((new OriginalFileI(remoteFileId, false)), normalUser);
        Assert.assertEquals(remoteFileGroupId, normalUser.groupId);
        Image image = (Image) iQuery.findByQuery(
                "FROM Image WHERE fileset IN "
                + "(SELECT fileset FROM FilesetEntry WHERE originalFile.id = :id)",
                new ParametersI().addId(remoteFileId));
        /* set up the basic permissions for this test */
        List<String> permissions = new ArrayList<String>();
        permissions.add(AdminPrivilegeSudo.value);
        if (permChown) permissions.add(AdminPrivilegeChown.value);

        final EventContext lightAdmin;
        lightAdmin = loginNewAdmin(true, permissions);
        try {
            sudo(new ExperimenterI(normalUser.userId, false));
            }catch (SecurityViolation sv) {
        }
        /* take care of workflows which do not use sudo */
        if (!isSudoing) {
            loginUser(lightAdmin);
        }
        /* light admin tries to chown the image of the normalUser whilst sudoed,
         * which should fail whether they have a Chown permissions or not */
        client.getImplicitContext().put("omero.group", Long.toString(normalUser.groupId));
        if (isSudoing) {
            doChange(client, factory, Requests.chown().target(image).toUser(anotherUser.userId).build(), false);
            long imageGroupId = ((RLong) iQuery.projection(
                    "SELECT details.group.id FROM Image i WHERE i.id = :id",
                    new ParametersI().addId(image.getId())).get(0).get(0)).getValue();
            Assert.assertEquals(imageGroupId, normalUser.groupId);
            assertOwnedBy(image, normalUser);
            assertOwnedBy((new OriginalFileI(remoteFileId, false)), normalUser);
        } else {
            /* chowning the image NOT being sudoed,
             * should pass only in case you have Chown
             * privilege, captured in "chownPassingWhenNotSudoing" boolean */
            doChange(client, factory, Requests.chown().target(image).toUser(anotherUser.userId).build(), chownPassingWhenNotSudoing);
            if (chownPassingWhenNotSudoing) {
                assertOwnedBy(image, anotherUser);
                assertOwnedBy((new OriginalFileI(remoteFileId, false)), anotherUser);
            } else {
                assertOwnedBy(image, normalUser);
                assertOwnedBy((new OriginalFileI(remoteFileId, false)), normalUser);
            }
            /* in any case, the image must be in the right group */
            long imageGroupId = ((RLong) iQuery.projection(
                    "SELECT details.group.id FROM Image i WHERE i.id = :id",
                    new ParametersI().addId(image.getId())).get(0).get(0)).getValue();
            Assert.assertEquals(imageGroupId, normalUser.groupId);
        }
    }

    /**
     * Test that an ImporterAs workflow without using Sudo.
     * The data will be imported into a group the user/(future owner of the data)
     * is a member of, then just chowned to the user.
     * This workflow is possible only if PR#4957 dealing with
     * admins importing data into groups they are not member of will get
     * merged. For this test, combinations of  <tt>Chown</tt>, <tt>WriteOwned</tt>,
     *  <tt>WriteFile</tt> and <tt>WriteManagedRepo</tt> privileges will be explored
     * for the light admin. For this workflow the creation and targeting of a Dataset
     * is tested too.
     * @param isAdmin if to test a member of the <tt>system</tt> group
     * @param permWriteOwned if to test a user who has the <tt>WriteOwned</tt> privilege
     * @param permWriteManagedRepo if to test a user who has the <tt>WriteManagedRepo</tt> privilege
     * @param permWriteFile if to test a user who has the <tt>WriteFile</tt> privilege
     * @param permChown if to test a user who has the <tt>Chown</tt> privilege
     * @param groupPermissions if to test the effect of group permission level
     * @throws Exception unexpected
     */

    @Test(dataProvider = "WriteOwned, WriteFile, WriteManagedRepo and Chown privileges cases")
    public void testImporterAsNoSudoChownOnlyWorkflow(boolean permWriteOwned, boolean permWriteFile,
            boolean permWriteManagedRepo, boolean permChown, String groupPermissions) throws Exception {
        /* define case where the import without any sudo importing into a group
         * the light admin is not a member of is expected to succeed
         */
        boolean importNotYourGroupExpectSuccess = permWriteOwned && permWriteFile && permWriteManagedRepo;
        /* define case where the creation of a dataset belonging to light admin
         * in the group where light admin is not a member
         * without any sudo is expected to succeed */
        boolean createDatasetExpectSuccess = permWriteOwned;
        /* define case where the whole workflow is possible (i.e. create
         * dataset, import into it, then chown the dataset with the imported
         * image to the user)
         */
        boolean createDatasetImportNotYourGroupAndChownExpectSuccess =
                permChown && permWriteManagedRepo && permWriteOwned && permWriteFile;
        final EventContext normalUser = newUserAndGroup(groupPermissions);
        /* set up the light admin's permissions for this test */
        List<String> permissions = new ArrayList<String>();
        if (permChown) permissions.add(AdminPrivilegeChown.value);
        if (permWriteOwned) permissions.add(AdminPrivilegeWriteOwned.value);
        if (permWriteFile) permissions.add(AdminPrivilegeWriteFile.value);
        if (permWriteManagedRepo) permissions.add(AdminPrivilegeWriteManagedRepo.value);
        final EventContext lightAdmin;
        lightAdmin = loginNewAdmin(true, permissions);
        /* First create a Dataset in the normalUser's group (you are not 
         * a member of this goup) */
        client.getImplicitContext().put("omero.group", Long.toString(normalUser.groupId));
        Dataset dat = mmFactory.simpleDataset();
        Dataset sentDat = new DatasetI();
        sentDat = null;
        if (createDatasetExpectSuccess) {/* you are allowed to create the dataset only
        with sufficient permissions, which are captured in createDatasetExpectSuccess.*/
            sentDat = (Dataset) iUpdate.saveAndReturnObject(dat);
        }
        /* import an image into the created Dataset */
        final RString imageName = omero.rtypes.rstring(fakeImageFile.getName());
        List<List<RType>> result = iQuery.projection(
                "SELECT id FROM OriginalFile WHERE name = :name ORDER BY id DESC LIMIT 1",
                new ParametersI().add("name", imageName));
        final long previousId = result.isEmpty() ? -1 : ((RLong) result.get(0).get(0)).getValue();
        try { /* only succeeds if permissions are sufficient or more */
            List<String> path = Collections.singletonList(fakeImageFile.getPath());
            importFileset(path, path.size(), sentDat);
            Assert.assertTrue(importNotYourGroupExpectSuccess);
        } catch (ServerError se) { /* fails if permissions are insufficient */
            Assert.assertFalse(importNotYourGroupExpectSuccess);
        }
        OriginalFile remoteFile = (OriginalFile) iQuery.findByQuery(
                "FROM OriginalFile o WHERE o.id > :id AND o.name = :name",
                new ParametersI().addId(previousId).add("name", imageName));
        if (importNotYourGroupExpectSuccess) {
            assertOwnedBy(remoteFile, lightAdmin);
            Assert.assertEquals(remoteFile.getDetails().getGroup().getId().getValue(), normalUser.groupId);
        } else Assert.assertNull(remoteFile, "if import failed, the remoteFile should be null");
        /* check that also the image corresponding to the original file is in the right group */
        Image image = null;
        if (!(remoteFile == null)) {
            image = (Image) iQuery.findByQuery("FROM Image WHERE fileset IN "
                    + "(SELECT fileset FROM FilesetEntry WHERE originalFile.id = :id)",
                    new ParametersI().addId(remoteFile.getId()));
        }
        if (importNotYourGroupExpectSuccess) {
            assertOwnedBy(image, lightAdmin);
            Assert.assertEquals(image.getDetails().getGroup().getId().getValue(), normalUser.groupId);
        } else {
            Assert.assertNull(image, "if import failed, the image should be null");
            /* jump out of the test, the second part of the test is interesting only
             * if the image exists
             */
            return;
        }
        /* now, having the image linked to the dataset in the group of normalUser already,
         * try to change the ownership of the dataset to the normalUser */
        /* Chowning the dataset should fail in case you have not all of
         * isAdmin & Chown & WriteOwned & WriteFile permissions which are
         * captured in the boolean importNotYourGroupAndChownExpectSuccess */
        if (createDatasetImportNotYourGroupAndChownExpectSuccess) {
            doChange(client, factory, Requests.chown().target(sentDat).toUser(normalUser.userId).build(), true);
            final long remoteFileGroupId = ((RLong) iQuery.projection(
                    "SELECT details.group.id FROM OriginalFile o WHERE o.id = :id",
                    new ParametersI().addId(remoteFile.getId())).get(0).get(0)).getValue();
            final long imageGroupId = ((RLong) iQuery.projection(
                    "SELECT details.group.id FROM Image i WHERE i.id = :id",
                    new ParametersI().addId(image.getId())).get(0).get(0)).getValue();
            final long datasetGroupId = ((RLong) iQuery.projection(
                    "SELECT details.group.id FROM Dataset d WHERE d.id = :id",
                    new ParametersI().addId(sentDat.getId())).get(0).get(0)).getValue();
            final List<RType> resultForLink = iQuery.projection(
                    "SELECT id, details.group.id FROM DatasetImageLink WHERE parent.id  = :id",
                    new ParametersI().addId(sentDat.getId())).get(0);
            final long linkId = ((RLong) resultForLink.get(0)).getValue();
            final long linkGroupId = ((RLong) resultForLink.get(1)).getValue();
            /* image, dataset and link are in the normalUser's group and belong to normalUser */
            assertOwnedBy(image, normalUser);
            Assert.assertEquals(imageGroupId, normalUser.groupId);
            assertOwnedBy(sentDat, normalUser);
            Assert.assertEquals(datasetGroupId, normalUser.groupId);
            assertOwnedBy((new DatasetImageLinkI(linkId, false)), normalUser);
            Assert.assertEquals(linkGroupId, normalUser.groupId);
            assertOwnedBy(remoteFile, normalUser);
            Assert.assertEquals(remoteFileGroupId, normalUser.groupId);
        } else {
            doChange(client, factory, Requests.chown().target(sentDat).toUser(normalUser.userId).build(), false);
            final long remoteFileGroupId = ((RLong) iQuery.projection(
                    "SELECT details.group.id FROM OriginalFile o WHERE o.id = :id",
                    new ParametersI().addId(remoteFile.getId())).get(0).get(0)).getValue();
            final long imageGroupId = ((RLong) iQuery.projection(
                    "SELECT details.group.id FROM Image i WHERE i.id = :id",
                    new ParametersI().addId(image.getId())).get(0).get(0)).getValue();
            final long datasetGroupId = ((RLong) iQuery.projection(
                    "SELECT details.group.id FROM Dataset d WHERE d.id = :id",
                    new ParametersI().addId(sentDat.getId())).get(0).get(0)).getValue();
            final List<RType> resultForLink = iQuery.projection(
                    "SELECT id, details.group.id FROM DatasetImageLink WHERE parent.id  = :id",
                    new ParametersI().addId(sentDat.getId())).get(0);
            final long linkId = ((RLong) resultForLink.get(0)).getValue();
            final long linkGroupId = ((RLong) resultForLink.get(1)).getValue();
            /* check that the image, dataset and link still belongs
             * to the light admin as the chown failed, but are in the group of normalUser */
            assertOwnedBy(image, lightAdmin);
            Assert.assertEquals(imageGroupId, normalUser.groupId);
            assertOwnedBy(sentDat, lightAdmin);
            Assert.assertEquals(datasetGroupId, normalUser.groupId);
            assertOwnedBy((new DatasetImageLinkI(linkId, false)), lightAdmin);
            Assert.assertEquals(linkGroupId, normalUser.groupId);
        }
    }

    /** Additonal test of light amdin without using Sudo.
     * The workflow deals with the eventuality of pre-existing container
     * in the target group and linking of the image or dataset to this container
     * (dataset or project). The image import has been tested in other tests,
     * here the image and/or dataset will be created and saved instead and just
     * the linking to a container will be tested. Only when the light admin has
     * WriteOwned privilege is the linking possible.
     * @param isAdmin if to test a member of the <tt>system</tt> group
     * @param permChown if to test a user who has the <tt>Chown</tt> privilege
     * @param permWriteOwned if to test a user who has the <tt>WriteOwned</tt> privilege
     * @param groupPermissions if to test the effect of group permission level
     * @throws Exception unexpected
     */
    @Test(dataProvider = "WriteOwned and Chown privileges cases")
    public void testLinkNoSudo(boolean permWriteOwned, boolean permChown,
            String groupPermissions) throws Exception {
        /* linking should be always permitted as long as light admin is in System Group
         * and has WriteOwned permissions. Exception is Private group, where linking will
         * always fail.*/
        boolean isExpectLinkingSuccess = permWriteOwned && !(groupPermissions == "rw----");
        boolean isExpectSuccessLinkAndChown = isExpectLinkingSuccess && permChown;
        final EventContext normalUser = newUserAndGroup(groupPermissions);
        /* set up the light admin's permissions for this test */
        List<String> permissions = new ArrayList<String>();
        if (permChown) permissions.add(AdminPrivilegeChown.value);
        if (permWriteOwned) permissions.add(AdminPrivilegeWriteOwned.value);
        final EventContext lightAdmin;
        lightAdmin = loginNewAdmin(true, permissions);
        /* create an image, dataset and project as normalUser in a group of the normalUser */
        loginUser(normalUser);
        client.getImplicitContext().put("omero.group", Long.toString(normalUser.groupId));
        Image image = mmFactory.createImage();
        Image sentImage = (Image) iUpdate.saveAndReturnObject(image);
        Dataset dat = mmFactory.simpleDataset();
        Dataset sentDat = (Dataset) iUpdate.saveAndReturnObject(dat);
        Project proj = mmFactory.simpleProject();
        Project sentProj = (Project) iUpdate.saveAndReturnObject(proj);
        /* now login as light admin and create links between the image and dataset
         * and the dataset and the project
         */
        loginUser(lightAdmin);
        client.getImplicitContext().put("omero.group", Long.toString(normalUser.groupId));
        DatasetImageLink linkOfDatasetImage = new DatasetImageLinkI();
        ProjectDatasetLink linkOfProjectDataset = new ProjectDatasetLinkI();
        if (isExpectLinkingSuccess) {
            linkOfDatasetImage = linkDatasetImage(sentDat, sentImage);
            linkOfProjectDataset = linkProjectDataset(sentProj, sentDat);
        } else {
            return; /*links could not be created, finish the test */
        }

        /* after successful linkage, transfer the ownership
         * of both links to the normalUser. For that the light admin
         * needs additonally the Chown permission. Note that the links
         * have to be transferred step by step, as the Chown feature
         * of whole hierarchy does not transfer links owned by non-owners
         * of the P/D?I objects. */
        Chown2 chown = Requests.chown().target(linkOfDatasetImage).toUser(normalUser.userId).build();
        doChange(client, factory, chown, isExpectSuccessLinkAndChown);
        chown = Requests.chown().target(linkOfProjectDataset).toUser(normalUser.userId).build();
        doChange(client, factory, chown, isExpectSuccessLinkAndChown);

        /* now retrieve and check that the links, image, dataset and project
         * are owned by normalUser */
        final long linkDatasetImageId = ((RLong) iQuery.projection(
                "SELECT id FROM DatasetImageLink WHERE parent.id  = :id",
                new ParametersI().addId(sentDat.getId())).get(0).get(0)).getValue();
        final long linkProjectDatasetId = ((RLong) iQuery.projection(
                "SELECT id FROM ProjectDatasetLink WHERE parent.id  = :id",
                new ParametersI().addId(sentProj.getId())).get(0).get(0)).getValue();
        assertOwnedBy(sentImage, normalUser);
        assertOwnedBy(sentDat, normalUser);
        assertOwnedBy(sentProj, normalUser);
        if (isExpectSuccessLinkAndChown) {
            assertOwnedBy((new DatasetImageLinkI (linkDatasetImageId, false)), normalUser);
            assertOwnedBy((new ProjectDatasetLinkI (linkProjectDatasetId, false)), normalUser);
        } else {
            assertOwnedBy((new DatasetImageLinkI (linkDatasetImageId, false)), lightAdmin);
            assertOwnedBy((new ProjectDatasetLinkI (linkProjectDatasetId, false)), lightAdmin);
        }
    }

        /** Test a workflow of ImporterAs without using Sudo.
         * The data will be imported to the group
         * of the light admin (where the user is not a member)
         * and chgrp-ed and chowned into the correct group/user afterwards.
         * For this test, combinations of  <tt>Chown</tt>, <tt>Chgrp</tt>,
         * privileges is explored for the light admin.
         * For this workflow the creation and targeting of a Dataset
         * is tested too.
         * @param isAdmin if to test a member of the <tt>system</tt> group
         * @param permChgrp if to test a user who has the <tt>Chgrp</tt> privilege
         * @param permChown if to test a user who has the <tt>Chown</tt> privilege
         * @param groupPermissions if to test the effect of group permission level
         * @throws Exception unexpected
         */
        @Test(dataProvider = "Chgrp and Chown privileges cases")
        public void testImporterAsNoSudoChgrpChownWorkflow(boolean permChgrp, boolean permChown,
                String groupPermissions) throws Exception {
        /* importing into the group of the light admin and
         * subsequent moving the data into the group of normalUser and chowning
         * them to the normal user will succeed if Chgrp and Chown is possible,
         * which needs permChgrp, permChown, but not WriteFile and WriteOwned,
         */
        boolean importYourGroupAndChgrpAndChownExpectSuccess = permChgrp && permChown;
        final EventContext normalUser = newUserAndGroup(groupPermissions);
        /* set up the light admin's permissions for this test */
        List<String> permissions = new ArrayList<String>();
        if (permChown) permissions.add(AdminPrivilegeChown.value);
        if (permChgrp) permissions.add(AdminPrivilegeChgrp.value);
        final EventContext lightAdmin;
        lightAdmin = loginNewAdmin(true, permissions);
        /* Workflow2: import an image as lightAdmin into a group you are a member of */
        /* First create a Dataset in your (light admin's) group */
        client.getImplicitContext().put("omero.group", Long.toString(lightAdmin.groupId));
        Dataset dat = mmFactory.simpleDataset();
        Dataset sentDat = new DatasetI();
        sentDat = null;
        sentDat = (Dataset) iUpdate.saveAndReturnObject(dat);
        /* import an image into the created Dataset */
        final RString imageName = omero.rtypes.rstring(fakeImageFile.getName());
        List<List<RType>> result = iQuery.projection(
                "SELECT id FROM OriginalFile WHERE name = :name ORDER BY id DESC LIMIT 1",
                new ParametersI().add("name", imageName));
        final long previousId = result.isEmpty() ? -1 : ((RLong) result.get(0).get(0)).getValue();
        try { /* expected */
            List<String> path = Collections.singletonList(fakeImageFile.getPath());
            importFileset(path, path.size(), sentDat);
        } catch (ServerError se) { /* not expected */}
        OriginalFile remoteFile = (OriginalFile) iQuery.findByQuery(
                "FROM OriginalFile o WHERE o.id > :id AND o.name = :name",
                new ParametersI().addId(previousId).add("name", imageName));
        assertOwnedBy(remoteFile, lightAdmin);
        Assert.assertEquals(remoteFile.getDetails().getGroup().getId().getValue(), lightAdmin.groupId);
        /* check that also the image corresponding to the original file is in the right group */
        Image image = null;
        image = (Image) iQuery.findByQuery("FROM Image WHERE fileset IN "
                + "(SELECT fileset FROM FilesetEntry WHERE originalFile.id = :id)",
                new ParametersI().addId(remoteFile.getId()));
        assertOwnedBy(image, lightAdmin);
        Assert.assertEquals(image.getDetails().getGroup().getId().getValue(), lightAdmin.groupId);

        /* now try to move the dataset into the group of the user */
        long imageGroupId = image.getDetails().getGroup().getId().getValue();
        /*in order to find the image in whatever group, get context with group
         * set to -1 (=all groups)
         */
        client.getImplicitContext().put("omero.group", Long.toString(-1));
        /* try to move the dataset (and with it the linked image)
         * from light admin's default group
         * into the default group of the normalUser
         * which should succeed in case the light admin has Chgrp permissions
         */
        if (permChgrp) {
            doChange(client, factory, Requests.chgrp().target(sentDat).toGroup(normalUser.groupId).build(), true);

        } else {
            doChange(client, factory, Requests.chgrp().target(sentDat).toGroup(normalUser.groupId).build(), false);
        }
        /* retrieve again the image, dataset and link */
        long datasetGroupId =((RLong) iQuery.projection(
                "SELECT details.group.id FROM Dataset d WHERE d.id = :id",
                new ParametersI().addId(sentDat.getId())).get(0).get(0)).getValue();
        long datasetImageLinkGroupId = ((RLong) iQuery.projection(
                "SELECT details.group.id FROM DatasetImageLink WHERE parent.id = :id",
                new ParametersI().addId(sentDat.getId())).get(0).get(0)).getValue();
        long remoteFileGroupId = ((RLong) iQuery.projection(
                "SELECT details.group.id FROM OriginalFile o WHERE o.id = :id",
                new ParametersI().addId(remoteFile.getId())).get(0).get(0)).getValue();
        /* note in which group the image now is now */
        imageGroupId = ((RLong) iQuery.projection(
                "SELECT details.group.id FROM Image i WHERE i.id = :id",
                new ParametersI().addId(image.getId())).get(0).get(0)).getValue();
        /* check that the image, dataset, and their link was moved too if the permissions
         * were sufficient */
        if (permChgrp) {
            Assert.assertEquals(remoteFileGroupId, normalUser.groupId);
            Assert.assertEquals(imageGroupId, normalUser.groupId);
            Assert.assertEquals(datasetGroupId, normalUser.groupId);
            Assert.assertEquals(datasetImageLinkGroupId, normalUser.groupId);
        /* check that the image, dataset and their link were not moved if
         * the permissions were not sufficient
         */
        } else {
            Assert.assertEquals(remoteFileGroupId, lightAdmin.groupId);
            Assert.assertEquals(imageGroupId, lightAdmin.groupId);
            Assert.assertEquals(datasetGroupId, lightAdmin.groupId);
            Assert.assertEquals(datasetImageLinkGroupId, lightAdmin.groupId);
        }
        /* now, having moved the dataset, image, original file and link in the group of normalUser,
         * try to change the ownership of the dataset to the normalUser */
        /* Chowning the dataset should fail in case you have not both of
         * isAdmin & Chown permissions which are
         * captured in the boolean importYourGroupAndChgrpAndChownExpectSuccess.
         * Additionally, in this boolean is permChgrp, which was necessary for the
         * previous step of moving the data into normalUser's group.
         * A successful chowning of the dataset will chown the linked image
         * and the link too.*/
        if (importYourGroupAndChgrpAndChownExpectSuccess) {/* whole workflow2 succeeded */
            doChange(client, factory, Requests.chown().target(sentDat).toUser(normalUser.userId).build(), true);
            remoteFileGroupId = ((RLong) iQuery.projection(
                    "SELECT details.group.id FROM OriginalFile o WHERE o.id = :id",
                    new ParametersI().addId(remoteFile.getId())).get(0).get(0)).getValue();
            imageGroupId = ((RLong) iQuery.projection(
                    "SELECT details.group.id FROM Image i WHERE i.id = :id",
                    new ParametersI().addId(image.getId())).get(0).get(0)).getValue();
            datasetGroupId = ((RLong) iQuery.projection(
                    "SELECT details.group.id FROM Dataset d WHERE d.id = :id",
                    new ParametersI().addId(sentDat.getId())).get(0).get(0)).getValue();
            long datasetImageLinkId = ((RLong) iQuery.projection(
                    "SELECT id FROM DatasetImageLink WHERE parent.id = :id",
                    new ParametersI().addId(sentDat.getId())).get(0).get(0)).getValue();
            datasetImageLinkGroupId = ((RLong) iQuery.projection(
                    "SELECT details.group.id FROM DatasetImageLink WHERE parent.id = :id",
                    new ParametersI().addId(sentDat.getId())).get(0).get(0)).getValue();
            /* image, dataset and link are in the normalUser's group and belong to normalUser */
            assertOwnedBy(remoteFile, normalUser);
            Assert.assertEquals(remoteFileGroupId, normalUser.groupId);
            assertOwnedBy(image, normalUser);
            Assert.assertEquals(imageGroupId, normalUser.groupId);
            assertOwnedBy(sentDat, normalUser);
            Assert.assertEquals(datasetGroupId, normalUser.groupId);
            assertOwnedBy((new DatasetImageLinkI (datasetImageLinkId, false)), normalUser);
            Assert.assertEquals(datasetImageLinkGroupId, normalUser.groupId);
        } else if (permChown) {
            /* even if the workflow2 as a whole failed, the chown might be successful */
            doChange(client, factory, Requests.chown().target(sentDat).toUser(normalUser.userId).build(), true);
            remoteFileGroupId = ((RLong) iQuery.projection(
                    "SELECT details.group.id FROM OriginalFile o WHERE o.id = :id",
                    new ParametersI().addId(remoteFile.getId())).get(0).get(0)).getValue();
            imageGroupId = ((RLong) iQuery.projection(
                    "SELECT details.group.id FROM Image i WHERE i.id = :id",
                    new ParametersI().addId(image.getId())).get(0).get(0)).getValue();
            datasetGroupId = ((RLong) iQuery.projection(
                    "SELECT details.group.id FROM Dataset d WHERE d.id = :id",
                    new ParametersI().addId(sentDat.getId())).get(0).get(0)).getValue();
            long datasetImageLinkId = ((RLong) iQuery.projection(
                    "SELECT id FROM DatasetImageLink WHERE parent.id = :id",
                    new ParametersI().addId(sentDat.getId())).get(0).get(0)).getValue();
            datasetImageLinkGroupId = ((RLong) iQuery.projection(
                    "SELECT details.group.id FROM DatasetImageLink WHERE parent.id = :id",
                    new ParametersI().addId(sentDat.getId())).get(0).get(0)).getValue();
            /* the image, dataset and link belong to the normalUser, but is in the light admin's group */
            assertOwnedBy(remoteFile, normalUser);
            Assert.assertEquals(remoteFileGroupId, lightAdmin.groupId);
            assertOwnedBy(image, normalUser);
            Assert.assertEquals(imageGroupId, lightAdmin.groupId);
            assertOwnedBy(sentDat, normalUser);
            Assert.assertEquals(datasetGroupId, lightAdmin.groupId);
            assertOwnedBy((new DatasetImageLinkI (datasetImageLinkId, false)), normalUser);
            Assert.assertEquals(datasetImageLinkGroupId, lightAdmin.groupId);
        } else if (permChgrp) {
            /* as workflow2 as a whole failed, in case the chgrp was successful,
             * the chown must be failing */
            doChange(client, factory, Requests.chown().target(sentDat).toUser(normalUser.userId).build(), false);
            remoteFileGroupId = ((RLong) iQuery.projection(
                    "SELECT details.group.id FROM OriginalFile o WHERE o.id = :id",
                    new ParametersI().addId(remoteFile.getId())).get(0).get(0)).getValue();
            imageGroupId = ((RLong) iQuery.projection(
                    "SELECT details.group.id FROM Image i WHERE i.id = :id",
                    new ParametersI().addId(image.getId())).get(0).get(0)).getValue();
            datasetGroupId = ((RLong) iQuery.projection(
                    "SELECT details.group.id FROM Dataset d WHERE d.id = :id",
                    new ParametersI().addId(sentDat.getId())).get(0).get(0)).getValue();
            long datasetImageLinkId = ((RLong) iQuery.projection(
                    "SELECT id FROM DatasetImageLink WHERE parent.id = :id",
                    new ParametersI().addId(sentDat.getId())).get(0).get(0)).getValue();
            datasetImageLinkGroupId = ((RLong) iQuery.projection(
                    "SELECT details.group.id FROM DatasetImageLink WHERE parent.id = :id",
                    new ParametersI().addId(sentDat.getId())).get(0).get(0)).getValue();
            /* the image, dataset and link are in normalUser's group but still belong to light admin */
            assertOwnedBy(remoteFile, lightAdmin);
            Assert.assertEquals(remoteFileGroupId, normalUser.groupId);
            assertOwnedBy(image, lightAdmin);
            Assert.assertEquals(imageGroupId, normalUser.groupId);
            assertOwnedBy(sentDat, lightAdmin);
            Assert.assertEquals(datasetGroupId, normalUser.groupId);
            assertOwnedBy((new DatasetImageLinkI (datasetImageLinkId, false)), lightAdmin);
            Assert.assertEquals(datasetImageLinkGroupId, normalUser.groupId);
        } else {
            /* the remaining option when the previous chgrp as well as this chown fail */
            doChange(client, factory, Requests.chown().target(sentDat).toUser(normalUser.userId).build(), false);
            remoteFileGroupId = ((RLong) iQuery.projection(
                    "SELECT details.group.id FROM OriginalFile o WHERE o.id = :id",
                    new ParametersI().addId(remoteFile.getId())).get(0).get(0)).getValue();
            imageGroupId = ((RLong) iQuery.projection(
                    "SELECT details.group.id FROM Image i WHERE i.id = :id",
                    new ParametersI().addId(image.getId())).get(0).get(0)).getValue();
            datasetGroupId = ((RLong) iQuery.projection(
                    "SELECT details.group.id FROM Dataset d WHERE d.id = :id",
                    new ParametersI().addId(sentDat.getId())).get(0).get(0)).getValue();
            long datasetImageLinkId = ((RLong) iQuery.projection(
                    "SELECT id FROM DatasetImageLink WHERE parent.id = :id",
                    new ParametersI().addId(sentDat.getId())).get(0).get(0)).getValue();
            datasetImageLinkGroupId = ((RLong) iQuery.projection(
                    "SELECT details.group.id FROM DatasetImageLink WHERE parent.id = :id",
                    new ParametersI().addId(sentDat.getId())).get(0).get(0)).getValue();
            /* the image, dataset and link are in light admin's group and belong to light admin */
            assertOwnedBy(remoteFile, lightAdmin);
            Assert.assertEquals(remoteFileGroupId, lightAdmin.groupId);
            assertOwnedBy(image, lightAdmin);
            Assert.assertEquals(imageGroupId, lightAdmin.groupId);
            assertOwnedBy(sentDat, lightAdmin);
            Assert.assertEquals(datasetGroupId, lightAdmin.groupId);
            assertOwnedBy((new DatasetImageLinkI (datasetImageLinkId, false)), lightAdmin);
            Assert.assertEquals(datasetImageLinkGroupId, lightAdmin.groupId);
        }
    }

    /** Test of DataOrganizer.
     * The workflow deals with the possibility of having to transfer all the data
     * to another user using the Chown privilege and using the targetUser
     * option of the Chown2 command which transfers all the data owned by one
     * user to another user. The data are in 2 groups, of which the original data owner
     * is a member of, the recipient of the data is just a member of one of the groups.
     * @param isAdmin if to test a member of the <tt>system</tt> group
     * @param isPrivileged if to test a user who has the <tt>Chown</tt> privilege
     * @param groupPermissions if to test the effect of group permission level
     * @throws Exception unexpected
     */
    @Test(dataProvider = "isPrivileged cases")
    public void testChownAllBelongingToUser(boolean isPrivileged, String groupPermissions) throws Exception {
        /* chown is passing in this test with isAdmin and permChown only.*/
        final boolean chownPassing = isPrivileged;
        final EventContext normalUser = newUserAndGroup(groupPermissions);
        ExperimenterGroup otherGroup = newGroupAddUser(groupPermissions, normalUser.userId, false);
        final EventContext recipient = newUserInGroup(otherGroup, false);
        /* set up the light admin's permissions for this test */
        List<String> permissions = new ArrayList<String>();
        if (isPrivileged) permissions.add(AdminPrivilegeChown.value);
        final EventContext lightAdmin;
        lightAdmin = loginNewAdmin(true, permissions);
        /* create two sets of P/D/I hierarchy as normalUser in the default
         * group of the normalUser */
        loginUser(normalUser); /* comment out this line in order to let the light admin own the hierarchy */
        client.getImplicitContext().put("omero.group", Long.toString(normalUser.groupId));
        Image image1 = mmFactory.createImage();
        Image image2 = mmFactory.createImage();
        Image sentImage1 = (Image) iUpdate.saveAndReturnObject(image1);
        Image sentImage2 = (Image) iUpdate.saveAndReturnObject(image2);
        Dataset dat1 = mmFactory.simpleDataset();
        Dataset dat2 = mmFactory.simpleDataset();
        Dataset sentDat1 = (Dataset) iUpdate.saveAndReturnObject(dat1);
        Dataset sentDat2 = (Dataset) iUpdate.saveAndReturnObject(dat2);
        Project proj1 = mmFactory.simpleProject();
        Project proj2 = mmFactory.simpleProject();
        Project sentProj1 = (Project) iUpdate.saveAndReturnObject(proj1);
        Project sentProj2 = (Project) iUpdate.saveAndReturnObject(proj2);
        DatasetImageLink linkOfDatasetImage1 = linkDatasetImage(sentDat1, sentImage1);
        DatasetImageLink linkOfDatasetImage2 = linkDatasetImage(sentDat2, sentImage2);
        ProjectDatasetLink linkOfProjectDataset1 = linkProjectDataset(sentProj1, sentDat1);
        ProjectDatasetLink linkOfProjectDataset2 = linkProjectDataset(sentProj2, sentDat2);

        /* now also create this hierarchy in the other group as the normalUser */

        client.getImplicitContext().put("omero.group", Long.toString(otherGroup.getId().getValue()));
        Image image1OtherGroup = mmFactory.createImage();
        Image image2OtherGroup = mmFactory.createImage();
        Image sentImage1OtherGroup = (Image) iUpdate.saveAndReturnObject(image1OtherGroup);
        Image sentImage2OtherGroup = (Image) iUpdate.saveAndReturnObject(image2OtherGroup);
        Dataset dat1OtherGroup = mmFactory.simpleDataset();
        Dataset dat2OtherGroup = mmFactory.simpleDataset();
        Dataset sentDat1OtherGroup = (Dataset) iUpdate.saveAndReturnObject(dat1OtherGroup);
        Dataset sentDat2OtherGroup = (Dataset) iUpdate.saveAndReturnObject(dat2OtherGroup);
        Project proj1OtherGroup = mmFactory.simpleProject();
        Project proj2OtherGroup = mmFactory.simpleProject();
        Project sentProj1OtherGroup = (Project) iUpdate.saveAndReturnObject(proj1OtherGroup);
        Project sentProj2OtherGroup = (Project) iUpdate.saveAndReturnObject(proj2OtherGroup);
        DatasetImageLink linkOfDatasetImage1OtherGroup = linkDatasetImage(sentDat1OtherGroup, sentImage1OtherGroup);
        DatasetImageLink linkOfDatasetImage2OtherGroup = linkDatasetImage(sentDat2OtherGroup, sentImage2OtherGroup);
        ProjectDatasetLink linkOfProjectDataset1OtherGroup = linkProjectDataset(sentProj1OtherGroup, sentDat1OtherGroup);
        ProjectDatasetLink linkOfProjectDataset2OtherGroup = linkProjectDataset(sentProj2OtherGroup, sentDat2OtherGroup);
        /* now transfer all the data of normalUser to recipient */
        loginUser(lightAdmin);
        client.getImplicitContext().put("omero.group", Long.toString(-1));
        /* transfer can proceed only if chownPassing boolean is true */
        doChange(client, factory, Requests.chown().targetUsers(normalUser.userId).toUser(recipient.userId).build(), chownPassing);
        if (!chownPassing) {
            return;
        }
        /* check the transfer of all the data in the first group was successful */
        /* check ownership of the first hierarchy set*/
        client.getImplicitContext().put("omero.group", Long.toString(-1));
        assertOwnedBy(sentProj1, recipient);
        assertOwnedBy(sentDat1, recipient);
        assertOwnedBy(sentImage1, recipient);
        assertOwnedBy(linkOfDatasetImage1, recipient);
        assertOwnedBy(linkOfProjectDataset1, recipient);
        /*check ownership of the second hierarchy set*/
        assertOwnedBy(sentProj2, recipient);
        assertOwnedBy(sentDat2, recipient);
        assertOwnedBy(sentImage2, recipient);
        assertOwnedBy(linkOfDatasetImage2, recipient);
        assertOwnedBy(linkOfProjectDataset2, recipient);
        /* check ownership of the objects in otherGroup */
        /* check ownership of the first hierarchy in the other group */
        assertOwnedBy(sentProj1OtherGroup, recipient);
        assertOwnedBy(sentDat1OtherGroup, recipient);
        assertOwnedBy(sentImage1OtherGroup, recipient);
        assertOwnedBy(linkOfDatasetImage1OtherGroup, recipient);
        assertOwnedBy(linkOfProjectDataset1OtherGroup, recipient);
        /* check ownership of the second hierarchy in the other group */
        assertOwnedBy(sentProj2OtherGroup, recipient);
        assertOwnedBy(sentDat2OtherGroup, recipient);
        assertOwnedBy(sentImage1OtherGroup, recipient);
        assertOwnedBy(linkOfDatasetImage2OtherGroup, recipient);
        assertOwnedBy(linkOfProjectDataset2OtherGroup, recipient);
    }

    /** Test of light admin without using Sudo.
     * The workflow deals with the eventuality of putting ROI and Rendering Settings on an
     * image of the user and then transferring the ownership of the ROI and settings
     * to the user.
     * @param isAdmin if to test a member of the <tt>system</tt> group
     * @param permChown if to test a user who has the <tt>Chown</tt> privilege
     * @param permWriteOwned if to test a user who has the <tt>WriteOwned</tt> privilege
     * @param groupPermissions if to test the effect of group permission level
     * @throws Exception unexpected
     */
    @Test(dataProvider = "WriteOwned and Chown privileges cases")
    public void testROIAndRenderingSettingsNoSudo(boolean permWriteOwned, boolean permChown,
            String groupPermissions) throws Exception {
        /* creation of rendering settings should be always permitted as long as light admin is in System Group
         * and has WriteOwned permissions. Exception is Private group, where it will
         * always fail.*/
        boolean isExpectSuccessCreateROIRndSettings = permWriteOwned && !(groupPermissions == "rw----") ;
        /* When attempting to chown ROI without the image the ROI is on in read-only and private groups,
         * the server says that this is not allowed. Unintended behaviour, the bug was filed.
         * The boolean isExpectSuccessCreateAndChownROI had to be adjusted accordingly for this test to pass.
         * Note that the private groups were already excluded in the boolean isExpectSuccessCreateROIRndSettings */
        boolean isExpectSuccessCreateAndChownROI = isExpectSuccessCreateROIRndSettings && permChown && !(groupPermissions == "rwr---");
        boolean isExpectSuccessCreateAndChownRndSettings = isExpectSuccessCreateROIRndSettings && permChown;
        final EventContext normalUser = newUserAndGroup(groupPermissions);
        /* set up the light admin's permissions for this test */
        List<String> permissions = new ArrayList<String>();
        if (permChown) permissions.add(AdminPrivilegeChown.value);
        if (permWriteOwned) permissions.add(AdminPrivilegeWriteOwned.value);

        /* create an image with pixels as normalUser in a group of the normalUser */
        loginUser(normalUser);
        Image image = mmFactory.createImage();
        Image sentImage = (Image) iUpdate.saveAndReturnObject(image);
        Pixels pixelsOfImage = sentImage.getPrimaryPixels();

        /* login as light admin */
        final EventContext lightAdmin;
        lightAdmin = loginNewAdmin(true, permissions);
        client.getImplicitContext().put("omero.group", Long.toString(normalUser.groupId));

        /* set the ROI as light admin on the image of the user */
        Roi roi = new RoiI();
        roi.addShape(new RectangleI());
        roi.setImage((Image) sentImage.proxy());
        try {
            roi = (Roi) iUpdate.saveAndReturnObject(roi);
            Assert.assertTrue(isExpectSuccessCreateROIRndSettings);
        } catch (SecurityViolation sv) {
            /* will not work in private group or when the permissions are unsufficient */
            Assert.assertFalse(isExpectSuccessCreateROIRndSettings);
        }

        /* set rendering settings as light admin using setOriginalSettingsInSet method */
        IRenderingSettingsPrx prx = factory.getRenderingSettingsService();
        try {
            prx.setOriginalSettingsInSet(Pixels.class.getName(),
                    Arrays.asList(pixelsOfImage.getId().getValue()));
            Assert.assertTrue(isExpectSuccessCreateROIRndSettings);
        } catch (SecurityViolation sv) {
            /* will not work in private group or when the permissions are unsufficient */
            Assert.assertFalse(isExpectSuccessCreateROIRndSettings);
        }
        /* retrieve the image corresponding to the roi and Rnd settings
         * and check the roi and rendering settings belong to light admin,
         * whereas the image belongs to normalUser */
        RenderingDef rDef = (RenderingDef) iQuery.findByQuery("FROM RenderingDef WHERE pixels.id = :id",
                new ParametersI().addId(pixelsOfImage.getId()));
        if (isExpectSuccessCreateROIRndSettings) {
            /* retrieving the image here via rDef, in order to
             * be sure this is the image on which
             * the rendering definitions are attached */
            long imageId = ((RLong) iQuery.projection(
                    "SELECT rdef.pixels.image.id FROM RenderingDef rdef WHERE rdef.id = :id",
                    new ParametersI().addId(rDef.getId())).get(0).get(0)).getValue();
            assertOwnedBy(roi, lightAdmin);
            assertOwnedBy(rDef, lightAdmin);
            assertOwnedBy((new ImageI (imageId, false)), normalUser);
        } else {/* as the permissions were not sufficient
                 * no rendering settings were created and no roi saved */
            roi = (Roi) iQuery.findByQuery("FROM Roi WHERE image.id = :id",
                    new ParametersI().addId(sentImage.getId()));
            Assert.assertNull(roi);
            Assert.assertNull(rDef);
        }
        /* after this, as light admin try to chown the ROI and the rendering settings to normalUser */
        if (isExpectSuccessCreateROIRndSettings) {/* only attempt the chown if the ROI and rendering settings exist
             and also in case of ROIs cannot chown in read-only group. See definition of boolean
             isExpectSuccessCreateAndChownROI */
            doChange(client, factory, Requests.chown().target(roi).toUser(normalUser.userId).build(), isExpectSuccessCreateAndChownROI);
            doChange(client, factory, Requests.chown().target(rDef).toUser(normalUser.userId).build(), isExpectSuccessCreateAndChownRndSettings);
            /* retrieving the image here via rDef, in order to
             * be sure this is the image on which
             * the rendering definitions are attached */
            long imageId = ((RLong) iQuery.projection(
                    "SELECT rdef.pixels.image.id FROM RenderingDef rdef WHERE rdef.id = :id",
                    new ParametersI().addId(rDef.getId())).get(0).get(0)).getValue();
            if (isExpectSuccessCreateAndChownROI) {/* whole workflow succeeded for ROI, all belongs to normalUser */
                assertOwnedBy(roi, normalUser);
                assertOwnedBy((new ImageI (imageId, false)), normalUser);
            } else {/* the creation of ROI succeeded, but the chown failed */
                assertOwnedBy(roi, lightAdmin);
                assertOwnedBy((new ImageI (imageId, false)), normalUser);
            }
            if (isExpectSuccessCreateAndChownRndSettings) {/* whole workflow succeeded for Rnd settings, all belongs to normalUser */
                assertOwnedBy(rDef, normalUser);
                assertOwnedBy((new ImageI (imageId, false)), normalUser);
            } else {/* the creation of the Rnd settings succeeded, but the chown failed */
                assertOwnedBy(rDef, lightAdmin);
                assertOwnedBy((new ImageI (imageId, false)), normalUser);
            }
        } else {/* neither ROI nor rendering settings were not created, and chown was not attempted */
            Assert.assertNull(roi);
            Assert.assertNull(rDef);
        }
    }

    /** Test of light admin without using Sudo.
     * The workflow deals with the eventuality of uploading a File Attachment
     * and linking it to an image of the user and then transferring
     * the ownership of the attachment and link
     * to the user.
     * @param isAdmin if to test a member of the <tt>system</tt> group
     * @param permChown if to test a user who has the <tt>Chown</tt> privilege
     * @param permWriteOwned if to test a user who has the <tt>WriteOwned</tt> privilege
     * @param permWriteFile if to test a user who has the <tt>WriteFile</tt> privilege
     * @param groupPermissions if to test the effect of group permission level
     * @throws Exception unexpected
     */
    @Test(dataProvider = "fileAttachment privileges cases")
    public void testFileAttachmentNoSudo(boolean permChown, boolean permWriteOwned,
            boolean permWriteFile, String groupPermissions) throws Exception {
        /* upload/creation of File Attachment should be always permitted as long as light admin is in System Group
         * and has WriteOwned and WriteFile permissions. */
        boolean isExpectSuccessCreateFileAttachment = permWriteOwned && permWriteFile;
        boolean isExpectSuccessLinkFileAttachemnt = isExpectSuccessCreateFileAttachment && !(groupPermissions == "rw----");
        boolean isExpectSuccessCreateFileAttAndChown = isExpectSuccessCreateFileAttachment && permChown;
        boolean isExpectSuccessCreateLinkAndChown = isExpectSuccessLinkFileAttachemnt && permChown;
        final EventContext normalUser = newUserAndGroup(groupPermissions);
        /* set up the light admin's permissions for this test */
        List<String> permissions = new ArrayList<String>();
        if (permChown) permissions.add(AdminPrivilegeChown.value);
        if (permWriteOwned) permissions.add(AdminPrivilegeWriteOwned.value);
        if (permWriteFile) permissions.add(AdminPrivilegeWriteFile.value);

        /* create an image with pixels as normalUser in a group of the normalUser */
        loginUser(normalUser);
        Image image = mmFactory.createImage();
        Image sentImage = (Image) iUpdate.saveAndReturnObject(image);
        /* login as light admin */
        final EventContext lightAdmin;
        lightAdmin = loginNewAdmin(true, permissions);
        client.getImplicitContext().put("omero.group", Long.toString(normalUser.groupId));
        /* create a file attachment as light admin */
        final OriginalFile originalFile = mmFactory.createOriginalFile();
        FileAnnotation fileAnnotation = new FileAnnotationI();
        fileAnnotation.setFile(originalFile);
        try {
            fileAnnotation = (FileAnnotation) iUpdate.saveAndReturnObject(fileAnnotation);
            Assert.assertTrue(isExpectSuccessCreateFileAttachment);
        } catch (SecurityViolation sv) {
            Assert.assertFalse(isExpectSuccessCreateFileAttachment);
            return; /* finish the test in case we have no FileAttachment */
        }
        /* link the file attachment to the image of the user as light admin
         * This will not work in private group. See definition of the boolean
         * isExpectSuccessLinkFileAttachemnt */
        ImageAnnotationLink link = new ImageAnnotationLinkI();
        link.setParent(sentImage);
        link.setChild(fileAnnotation);
        try {
            link = (ImageAnnotationLink) iUpdate.saveAndReturnObject(link);
            Assert.assertTrue(isExpectSuccessLinkFileAttachemnt);
        } catch (SecurityViolation sv) {
            Assert.assertFalse(isExpectSuccessLinkFileAttachemnt);
            return; /* finish the test in case we have no Link */
        }

        /* transfer the ownership of the attachment and the link to the user */
        /* The attachment was certainly created. In cases in which it was not created,
         * the test was terminated (see above). */
        doChange(client, factory, Requests.chown().target(fileAnnotation).toUser(normalUser.userId).build(), isExpectSuccessCreateFileAttAndChown);
        fileAnnotation = (FileAnnotation) iQuery.findByQuery("FROM FileAnnotation WHERE id = :id",
                new ParametersI().addId(fileAnnotation.getId()));
        if (isExpectSuccessCreateFileAttAndChown) {/* file ann creation and chowning succeeded */
            assertOwnedBy(fileAnnotation, normalUser);
        } else {/* the creation of file annotation succeeded, but the chown failed */
            assertOwnedBy(fileAnnotation, lightAdmin);
        }
        /* The link was certainly created. In cases where the creation was not successful,
         * the test was terminated (see above).*/
        doChange(client, factory, Requests.chown().target(link).toUser(normalUser.userId).build(), isExpectSuccessCreateLinkAndChown);
        if (isExpectSuccessCreateLinkAndChown) {/* if the link could
        be both created and chowned, this means also the attachment was created and chowned
        and thus the whole workflow succeeded (see declaration of
        isExpectSuccessCreateLinkAndChown boolean).*/
            link = (ImageAnnotationLink) iQuery.findByQuery("FROM ImageAnnotationLink l JOIN FETCH"
                    + " l.child JOIN FETCH l.parent WHERE l.child.id = :id",
                    new ParametersI().addId(fileAnnotation.getId()));
            assertOwnedBy(link, normalUser);
            assertOwnedBy(fileAnnotation, normalUser);
        } else {/* link was created but could not be chowned */
            link = (ImageAnnotationLink) iQuery.findByQuery("FROM ImageAnnotationLink l JOIN FETCH"
                    + " l.child JOIN FETCH l.parent WHERE l.child.id = :id",
                    new ParametersI().addId(fileAnnotation.getId()));
            assertOwnedBy(link, lightAdmin);
        }
    }

    /** Test of light admin without using Sudo.
     * The workflow tries to upload an official script.
     * The only permission light admin needs for this is WriteScriptRepo
     * @param isAdmin if to test a member of the <tt>system</tt> group
     * @param isPrivileged if to test a user who has the <tt>WriteScriptRepo</tt> privilege
     * @param groupPermissions if to test the effect of group permission level
     * @throws Exception unexpected
     */
    @Test(dataProvider = "isPrivileged cases")
    public void testOfficialSciptUploadNoSudo(boolean isPrivileged, String groupPermissions) throws Exception {
        /* upload/creation of File Attachment should be always permitted as long as light admin is in System Group
         * and has WriteOwned and WriteFile permissions. */
        boolean isExpectSuccessUploadOfficialScript = isPrivileged;
        final EventContext normalUser = newUserAndGroup(groupPermissions);
        /* set up the light admin's permissions for this test */
        List<String> permissions = new ArrayList<String>();
        if (isPrivileged) permissions.add(AdminPrivilegeWriteScriptRepo.value);
        final EventContext lightAdmin;
        lightAdmin = loginNewAdmin(true, permissions);
        client.getImplicitContext().put("omero.group", Long.toString(normalUser.groupId));
        IScriptPrx iScript = factory.getScriptService();
        /* fetch a script from the server */
        OriginalFile scriptFile = iScript.getScriptsByMimetype(ScriptServiceTest.PYTHON_MIMETYPE).get(0);
        RawFileStorePrx rfs = factory.createRawFileStore();
        rfs.setFileId(scriptFile.getId().getValue());
        final String actualScript = new String(rfs.read(0, (int) rfs.size()), StandardCharsets.UTF_8);
        rfs.close();
        /* try uploading the script as a new script in the normal user's group */
        iScript = factory.getScriptService();
        final String testScriptName = "Test_" + getClass().getName() + '_' + UUID.randomUUID() + ".py";
        long testScriptId = -1;
        try {
            testScriptId = iScript.uploadOfficialScript(testScriptName, actualScript);
            Assert.assertTrue(isExpectSuccessUploadOfficialScript);
        } catch (ServerError se) {
            Assert.assertFalse(isExpectSuccessUploadOfficialScript);
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
     * Test that users may delete official scripts only if they are a member of the <tt>system</tt> group and
     * have the <tt>DeleteScriptRepo</tt> privilege.
     * @param isAdmin if to test a member of the <tt>system</tt> group
     * @param isPrivileged if to test a user who has the <tt>DeleteScriptRepo</tt> privilege
     * @param groupPermissions if to test the effect of group permission level
     * @throws Exception unexpected
     */
    @Test(dataProvider = "isPrivileged cases")
    public void testOfficialScriptDeleteNoSudo(boolean isPrivileged, String groupPermissions) throws Exception {
        if (groupPermissions.equals("rwrw--")) {
            throw new SkipException("does not work in read-write group");
        }
        boolean isExpectSuccessDeleteOfficialScript = isPrivileged;
        final EventContext normalUser = newUserAndGroup(groupPermissions);
        List<String> permissions = new ArrayList<String>();
        if (isPrivileged) permissions.add(AdminPrivilegeDeleteScriptRepo.value);
        final EventContext lightAdmin;
        lightAdmin = loginNewAdmin(true, permissions);
        client.getImplicitContext().put("omero.group", Long.toString(normalUser.groupId));
        IScriptPrx iScript = factory.getScriptService();
        /* fetch a script from the server */
        final OriginalFile scriptFile = iScript.getScriptsByMimetype(ScriptServiceTest.PYTHON_MIMETYPE).get(0);
        RawFileStorePrx rfs = factory.createRawFileStore();
        rfs.setFileId(scriptFile.getId().getValue());
        final String actualScript = new String(rfs.read(0, (int) rfs.size()), StandardCharsets.UTF_8);
        rfs.close();
        /* upload the script as a new script (as another admin with appropriate permission */
        loginNewAdmin(true, AdminPrivilegeWriteScriptRepo.value);
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
        /* try deleting the script as the light admin established at the beginning of the test */
        loginUser(lightAdmin);
        client.getImplicitContext().put("omero.group", Long.toString(normalUser.groupId));
        iScript = factory.getScriptService();
        try {
            iScript.deleteScript(testScriptId);
            Assert.assertTrue(isExpectSuccessDeleteOfficialScript);
        } catch (ServerError se) {
            Assert.assertFalse(isExpectSuccessDeleteOfficialScript);
        }
        /* check if the script was deleted or left intact */
        loginUser(normalUser);
        if (isExpectSuccessDeleteOfficialScript) {
            assertDoesNotExist(testScript);
        } else {
            assertExists(testScript);
        }
        rfs = factory.createRawFileStore();
        try {
            rfs.setFileId(testScriptId);
            final String currentScript = new String(rfs.read(0, (int) rfs.size()), StandardCharsets.UTF_8);
            Assert.assertEquals(currentScript, actualScript);
            Assert.assertFalse(isExpectSuccessDeleteOfficialScript);
        } catch (Ice.LocalException | ServerError se) {
            /* can catch only ServerError once RawFileStoreTest.testBadFileId is fixed */
            Assert.assertTrue(isExpectSuccessDeleteOfficialScript);
        } finally {
            rfs.close();
        }
    }

    /**
     * Test that light admin can modify group membership when he/she has
     * only the <tt>ModifyGroupMembership</tt> privilege.
     * The addition of a user is being attempted here.
     * @param isAdmin if to test a member of the <tt>system</tt> group
     * @param isPrivileged if to test a user who has the <tt>ModifyGroupMembership</tt> privilege
     * @param groupPermissions if to test the effect of group permission level
     * @throws Exception unexpected
     */
    @Test(dataProvider = "isPrivileged cases")
    public void testModifyGroupMembershipAddUser(boolean isPrivileged, String groupPermissions) throws Exception {
        /* the permModifyGroupMembership should be a sufficient permission to perform
         * the user addition into a group */
        boolean isExpectSuccessAddUserToGroup = isPrivileged;
        final EventContext normalUser = newUserAndGroup(groupPermissions);
        /* one extra group is needed to add the existing normalUser to */
        final EventContext otherUser = newUserAndGroup(groupPermissions);
        List<String> permissions = new ArrayList<String>();
        if (isPrivileged) permissions.add(AdminPrivilegeModifyGroupMembership.value);
        final EventContext lightAdmin;
        lightAdmin = loginNewAdmin(true, permissions);
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
     * Test that light admin can modify group membership when he/she has
     * only the <tt>ModifyGroupMembership</tt> privilege.
     * The removal of a user is being attempted here.
     * @param isAdmin if to test a member of the <tt>system</tt> group
     * @param isPrivileged if to test a user who has the <tt>ModifyGroupMembership</tt> privilege
     * @param groupPermissions if to test the effect of group permission level
     * @throws Exception unexpected
     */
    @Test(dataProvider = "isPrivileged cases")
    public void testModifyGroupMembershipRemoveUser(boolean isPrivileged,
            String groupPermissions) throws Exception {
        /* the permModifyGroupMembership should be a sufficient permission to perform
         * the user removal from a group */
        boolean isExpectSuccessRemoveUserFromGroup = isPrivileged;
        final EventContext normalUser = newUserAndGroup(groupPermissions);
        /* one extra group is needed which the normalUser is also a member of */
        final ExperimenterGroup otherGroup = newGroupAddUser("rwr-r-", normalUser.userId);
        List<String> permissions = new ArrayList<String>();
        if (isPrivileged) permissions.add(AdminPrivilegeModifyGroupMembership.value);
        final EventContext lightAdmin;
        lightAdmin = loginNewAdmin(true, permissions);
        final Experimenter user = new ExperimenterI(normalUser.userId, false);
        try {
            iAdmin.removeGroups(user, Collections.singletonList(otherGroup));
            Assert.assertTrue(isExpectSuccessRemoveUserFromGroup);
        } catch (ServerError se) {
            Assert.assertFalse(isExpectSuccessRemoveUserFromGroup);
        }
    }

    /**
     * Test that light admin can make a user an owner of a group
     * when the light admin has only the <tt>ModifyGroupMembership</tt> privilege.
     * @param isAdmin if to test a member of the <tt>system</tt> group
     * @param isPrivileged if to test a user who has the <tt>ModifyGroupMembership</tt> privilege
     * @param groupPermissions if to test the effect of group permission level
     * @throws Exception unexpected
     */
    @Test(dataProvider = "isPrivileged cases")
    public void testModifyGroupMembershipMakeOwner(boolean isPrivileged, String groupPermissions) throws Exception {
        /* the permModifyGroupMembership should be a sufficient permission to perform
         * the setting of a new group owner */
        boolean isExpectSuccessMakeOwnerOfGroup= isPrivileged;
        final EventContext normalUser = newUserAndGroup(groupPermissions);
        List<String> permissions = new ArrayList<String>();
        if (isPrivileged) permissions.add(AdminPrivilegeModifyGroupMembership.value);
        final EventContext lightAdmin;
        lightAdmin = loginNewAdmin(true, permissions);
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
     * Test that light admin can unset a user to be an owner of a group
     * when the light admin has only the <tt>ModifyGroupMembership</tt> privilege.
     * @param isAdmin if to test a member of the <tt>system</tt> group
     * @param isPrivileged if to test a user who has the <tt>ModifyGroupMembership</tt> privilege
     * @param groupPermissions if to test the effect of group permission level
     * @throws Exception unexpected
     */
    @Test(dataProvider = "isPrivileged cases")
    public void testModifyGroupMembershipUnsetOwner(boolean isPrivileged,
            String groupPermissions) throws Exception {
        /* the permModifyGroupMembership should be a sufficient permission to perform
         * the unsetting of a new group owner */
        boolean isExpectSuccessUnsetOwnerOfGroup= isPrivileged;
        /* set up the normalUser and make him an Owner by passing "true" in the
         * newUserAndGroup method argument */
        final EventContext normalUser = newUserAndGroup(groupPermissions, true);
        List<String> permissions = new ArrayList<String>();
        if (isPrivileged) permissions.add(AdminPrivilegeModifyGroupMembership.value);
        final EventContext lightAdmin;
        lightAdmin = loginNewAdmin(true, permissions);
        final Experimenter user = new ExperimenterI(normalUser.userId, false);
        final ExperimenterGroup group = new ExperimenterGroupI(normalUser.groupId, false);
        try {
            iAdmin.unsetGroupOwner(group, user);
            Assert.assertTrue(isExpectSuccessUnsetOwnerOfGroup);
        } catch (ServerError se) {
            Assert.assertFalse(isExpectSuccessUnsetOwnerOfGroup);
        }
        /* check that the normalUser was unset as the owner of group when appropriate */
        if (isExpectSuccessUnsetOwnerOfGroup) {
            Assert.assertTrue(iAdmin.getLeaderOfGroupIds(user).isEmpty());
        } else {
            Assert.assertEquals((long) iAdmin.getLeaderOfGroupIds(user).get(0), group.getId().getValue());
        }
    }

    /**
     * Test that light admin can create a new user
     * when the light admin has only the <tt>ModifyUser</tt> privilege.
     * @param isAdmin if to test a member of the <tt>system</tt> group
     * @param isPrivileged if to test a user who has the <tt>ModifyUser</tt> privilege
     * @param groupPermissions if to test the effect of group permission level
     * @throws Exception unexpected
     */
    @Test(dataProvider = "isPrivileged cases")
    public void testModifyUserCreate(boolean isPrivileged,
            String groupPermissions) throws Exception {
        /* the permModifyUser should be a sufficient permission to perform
         * the creation of a new user */
        boolean isExpectSuccessCreateUser= isPrivileged;
        final long newGroupId = newUserAndGroup(groupPermissions).groupId;
        List<String> permissions = new ArrayList<String>();
        if (isPrivileged) permissions.add(AdminPrivilegeModifyUser.value);
        final EventContext lightAdmin;
        lightAdmin = loginNewAdmin(true, permissions);
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
     * Test that light admin can edit an existing user
     * when the light admin has only the <tt>ModifyUser</tt> privilege.
     * @param isAdmin if to test a member of the <tt>system</tt> group
     * @param isPrivileged if to test a user who has the <tt>ModifyUser</tt> privilege
     * @param groupPermissions if to test the effect of group permission level
     * @throws Exception unexpected
     */
    @Test(dataProvider = "isPrivileged cases")
    public void testModifyUserEdit(boolean isPrivileged,
            String groupPermissions) throws Exception {
        /* the permModifyUser should be a sufficient permission to perform
         * the editing of a user */
        boolean isExpectSuccessEditUser= isPrivileged;
        final long newUserId = newUserAndGroup(groupPermissions).userId;
        List<String> permissions = new ArrayList<String>();
        if (isPrivileged) permissions.add(AdminPrivilegeModifyUser.value);
        final EventContext lightAdmin;
        lightAdmin = loginNewAdmin(true, permissions);
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
     * Test that light admin can create a new group
     * when the light admin has only the <tt>ModifyGroup</tt> privilege.
     * @param isAdmin if to test a member of the <tt>system</tt> group
     * @param isPrivileged if to test a user who has the <tt>ModifyGroup</tt> privilege
     * @param groupPermissions if to test the effect of group permission level
     * @throws Exception unexpected
     */
    @Test(dataProvider = "isPrivileged cases")
    public void testModifyGroupCreate(boolean isPrivileged,
            String groupPermissions) throws Exception {
        /* the permModifyGroup should be a sufficient permission to perform
         * a group creation */
        boolean isExpectSuccessCreateGroup = isPrivileged;
        final ExperimenterGroup newGroup = new ExperimenterGroupI();
        newGroup.setLdap(omero.rtypes.rbool(false));
        newGroup.setName(omero.rtypes.rstring(UUID.randomUUID().toString()));
        newGroup.getDetails().setPermissions(new PermissionsI(groupPermissions));
        /* set up the permissions for the light admin */
        List<String> permissions = new ArrayList<String>();
        if (isPrivileged) permissions.add(AdminPrivilegeModifyGroup.value);
        final EventContext lightAdmin;
        lightAdmin = loginNewAdmin(true, permissions);
        try {
            iAdmin.createGroup(newGroup);
            Assert.assertTrue(isExpectSuccessCreateGroup);
        } catch (ServerError se) {
            Assert.assertFalse(isExpectSuccessCreateGroup);
        }
    }

    /**
     * Test that light admin can edit an existing group
     * when the light admin has only the <tt>ModifyGroup</tt> privilege.
     * @param isAdmin if to test a member of the <tt>system</tt> group
     * @param isPrivileged if to test a user who has the <tt>ModifyGroup</tt> privilege
     * @param groupPermissions if to test the effect of group permission level
     * @throws Exception unexpected
     */
    @Test(dataProvider = "isPrivileged cases")
    public void testModifyGroupEdit(boolean isPrivileged,
            String groupPermissions) throws Exception {
        /* the permModifyGroup should be a sufficient permission to perform
         * group editing */
        boolean isExpectSuccessEditGroup = isPrivileged;
        /* set up the new group as Read-Write as the downgrade (edit) to all group
         * types by the light admin will be tested later in the test */
        final long newGroupId = newUserAndGroup("rwrw--").groupId;
        /* set up the permissions for the light admin */
        List<String> permissions = new ArrayList<String>();
        if (isPrivileged) permissions.add(AdminPrivilegeModifyGroup.value);
        final EventContext lightAdmin;
        lightAdmin = loginNewAdmin(true, permissions);
        /* light admin will downgrade the group to all possible permission levels and
         * also will edit the ldap settings */
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
     * @return test cases for File Attachment workflow testFileAttachmentNoSudo
     */
    @DataProvider(name = "fileAttachment privileges cases")
    public Object[][] provideFileAttachmentPrivilegesCases() {
        int index = 0;
        final int PERM_CHOWN = index++;
        final int PERM_WRITEOWNED = index++;
        final int PERM_WRITEFILE = index++;
        final int GROUP_PERMS = index++;

        final boolean[] booleanCases = new boolean[]{false, true};
        final String[] permsCases = new String[]{"rw----", "rwr---", "rwra--", "rwrw--"};
        final List<Object[]> testCases = new ArrayList<Object[]>();

            for (final boolean permChown : booleanCases) {
                for (final boolean permWriteOwned : booleanCases) {
                    for (final boolean permWriteFile : booleanCases) {
                        for (final String groupPerms : permsCases) {
                            final Object[] testCase = new Object[index];
                            testCase[PERM_CHOWN] = permChown;
                            testCase[PERM_WRITEOWNED] = permWriteOwned;
                            testCase[PERM_WRITEFILE] = permWriteFile;
                            testCase[GROUP_PERMS] = groupPerms;
                            //DEBUG if (isAdmin == true && permAdditional == true && permAdditional2 == true && permAdditional3 == true)
                            testCases.add(testCase);
                        }
                    }
                }
            }
        return testCases.toArray(new Object[testCases.size()][]);
    }

    /**
     * @return test cases for testCreateLinkImportSudo and testEdit
     */
    @DataProvider(name = "isSudoing and WriteOwned privileges cases")
    public Object[][] provideIsSudoingAndWriteOwned() {
        int index = 0;
        final int IS_SUDOING = index++;
        final int PERM_WRITEOWNED = index++;
        final int GROUP_PERMS = index++;

        final boolean[] booleanCases = new boolean[]{false, true};
        final String[] permsCases = new String[]{"rw----", "rwr---", "rwra--", "rwrw--"};
        final List<Object[]> testCases = new ArrayList<Object[]>();

            for (final boolean isSudoing : booleanCases) {
                for (final boolean permWriteOwned : booleanCases) {
                    for (final String groupPerms : permsCases) {
                        final Object[] testCase = new Object[index];
                        if (isSudoing && permWriteOwned)
                            /* not an interesting case */
                            continue;
                        testCase[IS_SUDOING] = isSudoing;
                        testCase[PERM_WRITEOWNED] = permWriteOwned;
                        testCase[GROUP_PERMS] = groupPerms;
                        // DEBUG  if (isAdmin == false && isRestricted == true && isSudo == false)
                        testCases.add(testCase);
                    }
                }
            }
        return testCases.toArray(new Object[testCases.size()][]);
    }

    /**
     * @return test cases for testDelete
     */
    @DataProvider(name = "isSudoing and Delete privileges cases")
    public Object[][] provideIsSudoingAndDeleteOwned() {
        int index = 0;
        final int IS_SUDOING = index++;
        final int PERM_DELETEOWNED = index++;
        final int GROUP_PERMS = index++;

        final boolean[] booleanCases = new boolean[]{false, true};
        final String[] permsCases = new String[]{"rw----", "rwr---", "rwra--", "rwrw--"};
        final List<Object[]> testCases = new ArrayList<Object[]>();

            for (final boolean isSudoing : booleanCases) {
                for (final boolean permDeleteOwned : booleanCases) {
                    for (final String groupPerms : permsCases) {
                        final Object[] testCase = new Object[index];
                        if (isSudoing && permDeleteOwned)
                            /* not an interesting case */
                            continue;
                        testCase[IS_SUDOING] = isSudoing;
                        testCase[PERM_DELETEOWNED] = permDeleteOwned;
                        testCase[GROUP_PERMS] = groupPerms;
                        // DEBUG  if (isAdmin == false && isRestricted == true && isSudo == false)
                        testCases.add(testCase);
                    }
                }
            }
        return testCases.toArray(new Object[testCases.size()][]);
    }

    /**
     * @return isSudoing and Chgrp test cases for testChgrp
     */
    @DataProvider(name = "isSudoing and Chgrp privileges cases")
    public Object[][] provideIsSudoingAndChgrpOwned() {
        int index = 0;
        final int IS_SUDOING = index++;
        final int PERM_CHGRP = index++;
        final int GROUP_PERMS = index++;

        final boolean[] booleanCases = new boolean[]{false, true};
        final String[] permsCases = new String[]{"rw----", "rwr---", "rwra--", "rwrw--"};
        final List<Object[]> testCases = new ArrayList<Object[]>();

            for (final boolean isSudoing : booleanCases) {
                for (final boolean permChgrp : booleanCases) {
                    for (final String groupPerms : permsCases) {
                        final Object[] testCase = new Object[index];
                        /* no test cases are excluded here, because isSudoing
                         * is in a sense acting to annule Chgrp permission
                         * which is tested in the testChgrp and is an interesting case.*/
                        testCase[IS_SUDOING] = isSudoing;
                        testCase[PERM_CHGRP] = permChgrp;
                        testCase[GROUP_PERMS] = groupPerms;
                        // DEBUG  if (isAdmin == false && isRestricted == true && isSudo == false)
                        testCases.add(testCase);
                    }
                }
            }
        return testCases.toArray(new Object[testCases.size()][]);
    }

    /**
     * @return isSudoing and Chgrp test cases for testChown
     */
    @DataProvider(name = "isSudoing and Chown privileges cases")
    public Object[][] provideIsSudoingAndChown() {
        int index = 0;
        final int IS_SUDOING = index++;
        final int PERM_CHOWN = index++;
        final int GROUP_PERMS = index++;

        final boolean[] booleanCases = new boolean[]{false, true};
        final String[] permsCases = new String[]{"rw----", "rwr---", "rwra--", "rwrw--"};
        final List<Object[]> testCases = new ArrayList<Object[]>();

            for (final boolean isSudoing : booleanCases) {
                for (final boolean permChown : booleanCases) {
                    for (final String groupPerms : permsCases) {
                        final Object[] testCase = new Object[index];
                        /* no test cases are excluded here, because isSudoing
                         * is in a sense acting to annule Chown permission
                         * which is tested in the testChown and is an interesting case.*/
                        testCase[IS_SUDOING] = isSudoing;
                        testCase[PERM_CHOWN] = permChown;
                        testCase[GROUP_PERMS] = groupPerms;
                        // DEBUG  if (isAdmin == false && isRestricted == true && isSudo == false)
                        testCases.add(testCase);
                    }
                }
            }
        return testCases.toArray(new Object[testCases.size()][]);
    }

    /**
     * @return provide WriteOwned, WriteFile, WriteManagedRepo and Chown cases
     * for testImporterAsNoSudoChownOnly
     */
    @DataProvider(name = "WriteOwned, WriteFile, WriteManagedRepo and Chown privileges cases")
    public Object[][] provideWriteOwnedWriteFileWriteManagedRepoAndChown() {
        int index = 0;
        final int PERM_WRITEOWNED = index++;
        final int PERM_WRITEFILE = index++;
        final int PERM_WRITEMANAGEDREPO = index++;
        final int PERM_CHOWN = index++;
        final int GROUP_PERMS = index++;

        final boolean[] booleanCases = new boolean[]{false, true};
        final String[] permsCases = new String[]{"rw----", "rwr---", "rwra--", "rwrw--"};
        final List<Object[]> testCases = new ArrayList<Object[]>();

            for (final boolean permWriteOwned : booleanCases) {
                for (final boolean permWriteFile : booleanCases) {
                    for (final boolean permWriteManagedRepo : booleanCases) {
                        for (final boolean permChown : booleanCases) {
                            for (final String groupPerms : permsCases) {
                                final Object[] testCase = new Object[index];
                                if (!permWriteOwned && !permWriteFile)
                                    /* not an interesting case */
                                    continue;
                                if (!permWriteOwned && !permWriteManagedRepo)
                                    /* not an interesting case */
                                    continue;
                                if (!permWriteOwned && !permWriteFile && !permWriteManagedRepo)
                                    /* not an interesting case */
                                    continue;
                                testCase[PERM_WRITEOWNED] = permWriteOwned;
                                testCase[PERM_WRITEFILE] = permWriteFile;
                                testCase[PERM_WRITEMANAGEDREPO] = permWriteManagedRepo;
                                testCase[PERM_CHOWN] = permChown;
                                testCase[GROUP_PERMS] = groupPerms;
                                // DEBUG  if (isAdmin == false && isRestricted == true && isSudo == false)
                                testCases.add(testCase);
                            }
                        }
                    }
                }
            }
        return testCases.toArray(new Object[testCases.size()][]);
    }

    /**
     * @return WriteOwned and Chown test cases for
     * testLinkNoSudo and testROIAndRenderingSettingsNoSudo
     */
    @DataProvider(name = "WriteOwned and Chown privileges cases")
    public Object[][] provideWriteOwnedAndChown() {
        int index = 0;
        final int PERM_WRITEOWNED = index++;
        final int PERM_CHOWN = index++;
        final int GROUP_PERMS = index++;

        final boolean[] booleanCases = new boolean[]{false, true};
        final String[] permsCases = new String[]{"rw----", "rwr---", "rwra--", "rwrw--"};
        final List<Object[]> testCases = new ArrayList<Object[]>();

            for (final boolean permWriteOwned : booleanCases) {
                for (final boolean permChown : booleanCases) {
                    for (final String groupPerms : permsCases) {
                        final Object[] testCase = new Object[index];
                        if (!permWriteOwned && !permChown)
                            /* not an interesting case */
                            continue;
                        testCase[PERM_WRITEOWNED] = permWriteOwned;
                        testCase[PERM_CHOWN] = permChown;
                        testCase[GROUP_PERMS] = groupPerms;
                        // DEBUG  if (isAdmin == false && isRestricted == true && isSudo == false)
                        testCases.add(testCase);
                    }
                }
            }
        return testCases.toArray(new Object[testCases.size()][]);
    }

    /**
     * @return Chgrp and Chown test cases for testImporterAsNoSudoChgrpChown
     */
    @DataProvider(name = "Chgrp and Chown privileges cases")
    public Object[][] provideChgrpAndChown() {
        int index = 0;
        final int PERM_CHGRP = index++;
        final int PERM_CHOWN = index++;
        final int GROUP_PERMS = index++;

        final boolean[] booleanCases = new boolean[]{false, true};
        final String[] permsCases = new String[]{"rw----", "rwr---", "rwra--", "rwrw--"};
        final List<Object[]> testCases = new ArrayList<Object[]>();

            for (final boolean permChgrp : booleanCases) {
                for (final boolean permChown : booleanCases) {
                    for (final String groupPerms : permsCases) {
                        final Object[] testCase = new Object[index];
                        /* No test cases are excluded here, because Chgrp
                         * and Chown are two separate steps which can work
                         * independently on each other and both are tested
                         * in the test.*/
                        testCase[PERM_CHGRP] = permChgrp;
                        testCase[PERM_CHOWN] = permChown;
                        testCase[GROUP_PERMS] = groupPerms;
                        // DEBUG  if (isAdmin == false && isRestricted == true && isSudo == false)
                        testCases.add(testCase);
                    }
                }
            }
        return testCases.toArray(new Object[testCases.size()][]);
    }

    /**
     * @return isPrivileged test cases. The isPrivileged parameter translates into one
     * tested privilege in particular tests (for example in testScriptUpload isPrivileged
     * concerns WriteScriptRepo privilege specifically)
     */
    @DataProvider(name = "isPrivileged cases")
    public Object[][] provideIsPrivilegesCases() {
        int index = 0;
        final int IS_PRIVILEGED = index++;
        final int GROUP_PERMS = index++;

        final boolean[] booleanCases = new boolean[]{false, true};
        final String[] permsCases = new String[]{"rw----", "rwr---", "rwra--", "rwrw--"};
        final List<Object[]> testCases = new ArrayList<Object[]>();

            for (final boolean isPrivileged : booleanCases) {
                for (final String groupPerms : permsCases) {
                    final Object[] testCase = new Object[index];
                    testCase[IS_PRIVILEGED] = isPrivileged;
                    testCase[GROUP_PERMS] = groupPerms;
                    // DEBUG  if (isAdmin == false && isRestricted == true && isSudo == false)
                    testCases.add(testCase);
                }
            }
        return testCases.toArray(new Object[testCases.size()][]);
    }
}
