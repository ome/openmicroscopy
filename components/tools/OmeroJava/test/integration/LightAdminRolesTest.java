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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import omero.RLong;
import omero.RString;
import omero.RType;
import omero.SecurityViolation;
import omero.ServerError;
import omero.api.ServiceFactoryPrx;
import omero.cmd.Chown2;
import omero.gateway.util.Requests;
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
import omero.model.Folder;
import omero.model.IObject;
import omero.model.Image;
import omero.model.OriginalFile;
import omero.model.Project;
import omero.model.ProjectDatasetLink;
import omero.model.ProjectDatasetLinkI;
import omero.model.ProjectI;
import omero.model.Session;
import omero.model.enums.AdminPrivilegeChgrp;
import omero.model.enums.AdminPrivilegeChown;
import omero.model.enums.AdminPrivilegeDeleteOwned;
import omero.model.enums.AdminPrivilegeSudo;
import omero.model.enums.AdminPrivilegeWriteFile;
import omero.model.enums.AdminPrivilegeWriteManagedRepo;
import omero.model.enums.AdminPrivilegeWriteOwned;
import omero.sys.EventContext;
import omero.sys.ParametersI;
import omero.sys.Principal;
import omero.util.TempFileManager;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

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
     * @throws Exception unexpected
     */
    @Test(dataProvider = "combined privileges cases")
    public void testImporterAsSudoCreateImport(boolean isAdmin, boolean isSudoing, boolean permChgrp,
            boolean permWriteOwned, boolean permWriteFile, String groupPermissions) throws Exception {
        final EventContext normalUser = newUserAndGroup(groupPermissions);
        final boolean isExpectSuccessCreate = (isAdmin && permWriteOwned) || (isAdmin && isSudoing);
        /* set up the light admin's permissions for this test */
        ArrayList <String> permissions = new ArrayList <String>();
        permissions.add(AdminPrivilegeSudo.value);
        if (permChgrp) permissions.add(AdminPrivilegeChgrp.value);;
        if (permWriteOwned) permissions.add(AdminPrivilegeWriteOwned.value);
        if (permWriteFile) permissions.add(AdminPrivilegeWriteFile.value);
        final EventContext lightAdmin;
        lightAdmin = loginNewAdmin(isAdmin, permissions);
        if (isSudoing) {
            try {
                sudo(new ExperimenterI(normalUser.userId, false));
                    if (!isAdmin) {
                        Assert.fail("Sudo-permitted non-administrators cannot sudo.");
                    }
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
            long projId = sentProj.getId().getValue();
            final Project retrievedProject = (Project) iQuery.get("Project", projId);
            Assert.assertEquals(retrievedProject.getDetails().getOwner().getId().getValue(), normalUser.userId);
            long datId = sentDat.getId().getValue();
            final Dataset retrievedDataset = (Dataset) iQuery.get("Dataset", datId);
            Assert.assertEquals(retrievedDataset.getDetails().getOwner().getId().getValue(), normalUser.userId);
        } else {
            Assert.assertNull(sentProj);
            Assert.assertNull(sentDat);
        }
        /* finish the test if light admin is not sudoing, the firther part
        of the test deals with the imports. Imports when not sudoing workflows are covered in
        other tests in this class */
        if (!isSudoing) return;

        /* check that after sudo, the light admin is able to ImportAs and target
         * the import into the just created Dataset.
         * Check thus that the light admin can import and write the original file
         * on behalf of the normalUser and into the group of normalUser */
        final RString imageName = omero.rtypes.rstring(fakeImageFile.getName());
        if (!isAdmin) return;/* exit the test in case light admin is not an admin,
        too complicated and uninteresting case */
        final List<List<RType>> result = iQuery.projection(
                "SELECT id FROM OriginalFile WHERE name = :name ORDER BY id DESC LIMIT 1",
                new ParametersI().add("name", imageName));
        final long previousId = result.isEmpty() ? -1 : ((RLong) result.get(0).get(0)).getValue();
        List<String> path = Collections.singletonList(fakeImageFile.getPath());
        importFileset(path, path.size(), sentDat);
        final OriginalFile remoteFile = (OriginalFile) iQuery.findByQuery(
                "FROM OriginalFile o WHERE o.id > :id AND o.name = :name",
                new ParametersI().addId(previousId).add("name", imageName));
        Assert.assertEquals(remoteFile.getDetails().getOwner().getId().getValue(), normalUser.userId);
        Assert.assertEquals(remoteFile.getDetails().getGroup().getId().getValue(), normalUser.groupId);


        /* check that the light admin when sudoed, can link the created Dataset
         * to the created Project, check the ownership of the links
         * is of the simple user */

        ProjectDatasetLink link = linkProjectDataset(sentProj, sentDat);

        /* Now check the ownership of image and links
         * between image and Dataset and Dataset and Project */
        final IObject image = iQuery.findByQuery(
                "FROM Image WHERE fileset IN "
                + "(SELECT fileset FROM FilesetEntry WHERE originalFile.id = :id)",
                new ParametersI().addId(remoteFile.getId()));
        final IObject imageDatasetLink = iQuery.findByQuery(
                "FROM DatasetImageLink WHERE child.id = :id",
                new ParametersI().addId(image.getId()));
        final IObject retrievedProjectDatasetLink = iQuery.findByQuery(
                "FROM ProjectDatasetLink WHERE id = :id",
                new ParametersI().addId(link.getId()));
        Assert.assertEquals(image.getDetails().getOwner().getId().getValue(), normalUser.userId);
        Assert.assertEquals(imageDatasetLink.getDetails().getOwner().getId().getValue(), normalUser.userId);
        Assert.assertEquals(retrievedProjectDatasetLink.getDetails().getOwner().getId().getValue(), normalUser.userId);
    }

    /**
     * Test whether an ImporterAs can delete image, Project and Dataset
     * and their respective links belonging to another
     * user. Behaviors of the system are explored when ImporterAs
     * is and is not using <tt>Sudo</tt> privilege
     * for this action.
     * @throws Exception unexpected
     */
   @Test(dataProvider = "combined privileges cases")
   public void testImporterDelete(boolean isAdmin, boolean isSudoing, boolean permChgrp,
           boolean permWriteOwned, boolean permWriteFile, String groupPermissions) throws Exception {
       final EventContext normalUser = newUserAndGroup(groupPermissions);
       /* set up the light admin's permissions for this test */
       ArrayList <String> permissions = new ArrayList <String>();
       permissions.add(AdminPrivilegeSudo.value);
       if (permChgrp) permissions.add(AdminPrivilegeChgrp.value);;
       if (permWriteOwned) permissions.add(AdminPrivilegeWriteOwned.value);
       if (permWriteFile) permissions.add(AdminPrivilegeWriteFile.value);
       final EventContext lightAdmin;
       lightAdmin = loginNewAdmin(isAdmin, permissions);
       try {
           sudo(new ExperimenterI(normalUser.userId, false));
               if (!isAdmin) {
                   Assert.fail("Sudo-permitted non-administrators cannot sudo.");
               }
           }catch (SecurityViolation sv) {
               /* sudo expected to fail if the user is not in system group */
           }
       /* create a Dataset and Project being sudoed as normalUser */
       if (!isAdmin) return;
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
           Assert.assertTrue(isAdmin);
       } catch (ServerError se) {
           Assert.assertFalse(isAdmin);
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
           Assert.assertTrue(isAdmin);
       } catch (ServerError se) {
           Assert.assertFalse(isAdmin);
       }
       OriginalFile remoteFile = (OriginalFile) iQuery.findByQuery(
               "FROM OriginalFile o WHERE o.id > :id AND o.name = :name",
               new ParametersI().addId(previousId).add("name", imageName));
       if (isAdmin) {
           Assert.assertEquals(remoteFile.getDetails().getOwner().getId().getValue(), normalUser.userId);
           Assert.assertEquals(remoteFile.getDetails().getGroup().getId().getValue(), normalUser.groupId);
       }
       /* link the Project and the Dataset */
       ProjectDatasetLink link = linkProjectDataset(sentProj, sentDat);
       Image image = (Image) iQuery.findByQuery(
               "FROM Image WHERE fileset IN "
               + "(SELECT fileset FROM FilesetEntry WHERE originalFile.id = :id)",
               new ParametersI().addId(remoteFile.getId()));
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
        * created on behalf of the user */
       if ((!isSudoing && permWriteOwned && permWriteFile) || isSudoing ) {
           try {
               doChange(Requests.delete().target(datasetImageLink).build());
               doChange(Requests.delete().target(projectDatasetLink).build());
               doChange(Requests.delete().target(image).build());
               doChange(Requests.delete().target(sentDat).build());
               doChange(Requests.delete().target(sentProj).build());
           } catch (ServerError se) {
           /* not expected */
           }
       } else if (!isSudoing && permWriteOwned && !permWriteFile) {
           try {
               doChange(Requests.delete().target(datasetImageLink).build());
               doChange(Requests.delete().target(projectDatasetLink).build());
               doChange(Requests.delete().target(image).build());
               doChange(Requests.delete().target(sentDat).build());
               doChange(Requests.delete().target(sentProj).build());
           } catch (ServerError se) {
           /* not expected */
           }
       }
       /* Check one of the objects for non-existence after deletion. First, logging
        * in as root, retrieve all the objects to check them later*/
       logRootIntoGroup(normalUser.groupId);
       OriginalFile retrievedRemoteFile = (OriginalFile) iQuery.findByQuery(
               "FROM OriginalFile WHERE id = :id",
               new ParametersI().addId(remoteFile.getId()));
       Image retrievedImage = (Image) iQuery.findByQuery(
               "FROM Image WHERE fileset IN "
               + "(SELECT fileset FROM FilesetEntry WHERE originalFile.id = :id)",
               new ParametersI().addId(remoteFile.getId()));
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
       if ((!isSudoing && permWriteOwned && permWriteFile) || isSudoing ) {
           /* successful delete expected */
           Assert.assertNull(retrievedRemoteFile, "original file should be deleted");
           Assert.assertNull(retrievedImage, "image should be deleted");
           Assert.assertNull(retrievedDat, "dataset should be deleted");
           Assert.assertNull(retrievedProj, "project should be deleted");
           Assert.assertNull(retrievedDatasetImageLink, "Dat-Image link should be deleted");
           Assert.assertNull(retrievedProjectDatasetLink, "Proj-Dat link should be deleted");
       } else if (!isSudoing && permWriteOwned && !permWriteFile){
           /* When WriteFile permission is missing, general expectation is that only OMERO objects
            * would be successfully deleted, except image, because image has under itself original file,
            * for deletion of which WriteFile is necessary. Unfortunately, in actual fact, slightly non-standard
            * functionality in the back end (considering the original file orphaned after image
            * was deleted first) allows the deletion of the image and original file also in this case.*/
           Assert.assertNull(retrievedRemoteFile, "original file deleted - this is surprising, because WriteFile is false");
           Assert.assertNull(retrievedImage, "image deleted - this is surprising, because WriteFile is false");
           Assert.assertNull(retrievedDat, "dataset should be deleted");
           Assert.assertNull(retrievedProj, "project should be deleted");
           Assert.assertNull(retrievedDatasetImageLink, "Dat-Image link should be deleted");
           Assert.assertNull(retrievedProjectDatasetLink, "Proj-Dat link should be deleted");
       } else {
           /* no deletion should have been successful without permWriteOwned
            * and permWriteFile permissions */
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
     * @throws Exception unexpected
     */
    @Test(dataProvider = "combined privileges cases")
    public void testLightAdminEdit(boolean isAdmin, boolean isSudoing, boolean permChgrp,
            boolean permWriteOwned, boolean permWriteFile, String groupPermissions) throws Exception {
        final boolean isExpectSuccess = (isAdmin && isSudoing) || (isAdmin && permWriteOwned);
        final EventContext normalUser = newUserAndGroup(groupPermissions);
        /* set up the light admin's permissions for this test */
        ArrayList <String> permissions = new ArrayList <String>();
        permissions.add(AdminPrivilegeSudo.value);
        if (permChgrp) permissions.add(AdminPrivilegeChgrp.value);;
        if (permWriteOwned) permissions.add(AdminPrivilegeWriteOwned.value);
        if (permWriteFile) permissions.add(AdminPrivilegeWriteFile.value);
        final EventContext lightAdmin;
        lightAdmin = loginNewAdmin(isAdmin, permissions);
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
                if (!isAdmin) {
                    Assert.fail("Sudo-permitted non-administrators cannot sudo.");
                }
            } catch (SecurityViolation sv) {
                /* sudo expected to fail if the user is not in system group */
            }
        }
        if (!isAdmin) return; /* if the light admin is not an admin, exit the test, not interesting case */
        /* try to rename the Project as the light admin, either sudoed as normalUser or not */
        final String changedName = "ChangedNameOfLightAdmin";
        client.getImplicitContext().put("omero.group", Long.toString(normalUser.groupId));
        long id = sentProj.getId().getValue();
        final Project retrievedUnrenamedProject = (Project) iQuery.get("Project", id);
        retrievedUnrenamedProject.setName(omero.rtypes.rstring(changedName));
        if (isExpectSuccess) {/* in case no WriteOwned permission is give to light admin, and he/she is
        not sudoing, following line would throw a Security violation */
            sentProj = (Project) iUpdate.saveAndReturnObject(retrievedUnrenamedProject);
        }
        String savedChangedName = sentProj.getName().getValue().toString();
        logRootIntoGroup(normalUser.groupId);
        final Project retrievedRenamedProject = (Project) iQuery.get("Project", id);
        final String retrievedName = retrievedRenamedProject.getName().getValue().toString();
        Assert.assertEquals(retrievedRenamedProject.getDetails().getOwner().getId().getValue(), normalUser.userId);
        /* check that the name was changed and saved or original name is retained as appropriate */
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
     * Also test that ImporterAs can, having the <tt>Chgrp</tt>
     * privilege move another user's data into another group whether the
     * owner of the data is member of target group or not.
     * @throws Exception unexpected
     */
    @Test(dataProvider = "combined privileges cases")
    public void testImporterAsSudoChgrp(boolean isAdmin, boolean isSudoing, boolean permChgrp,
            boolean permWriteOwned, boolean permWriteFile, String groupPermissions) throws Exception {
        /* define case where the Sudo is not being used post-import
         * to perform the chgrp action. Such cases are all expected to fail
         * except the light admin has Chgrp permission. WriteOwned and WriteFile
         * are not important for the Chgrp success in such situation. Note that sudoing
         * cannot lead to a successful Chgrp in case the owner of the data is not a member
         * of the target group.*/
        boolean chgrpNoSudoExpectSuccessAnyGroup = (isAdmin && !isSudoing && permChgrp);
        /* Define successfull case when data are moved into group which the owner
         * of the data is a member of.*/
        boolean isExpectSuccessInMemberGroup = chgrpNoSudoExpectSuccessAnyGroup || isSudoing;
        final EventContext normalUser = newUserAndGroup(groupPermissions);
        final long anotherGroupId = newUserAndGroup(groupPermissions).groupId;
        final long normalUsersOtherGroupId = newGroupAddUser(groupPermissions, normalUser.userId, false).getId().getValue();
        /* set up the light admin's permissions for this test */
        ArrayList <String> permissions = new ArrayList <String>();
        permissions.add(AdminPrivilegeSudo.value);
        if (permChgrp) permissions.add(AdminPrivilegeChgrp.value);;
        if (permWriteOwned) permissions.add(AdminPrivilegeWriteOwned.value);
        if (permWriteFile) permissions.add(AdminPrivilegeWriteFile.value);
        final EventContext lightAdmin;
        lightAdmin = loginNewAdmin(isAdmin, permissions);
        try {
            sudo(new ExperimenterI(normalUser.userId, false));
                if (!isAdmin) {
                    Assert.fail("Sudo-permitted non-administrators cannot sudo.");
                }
            }catch (SecurityViolation sv) {
                /* sudo expected to fail if the user is not in system group */
            }
        /* import an image for the normalUser into the normalUser's default group */
        if (!isAdmin) return;
        client.getImplicitContext().put("omero.group", Long.toString(normalUser.groupId));
        final RString imageName = omero.rtypes.rstring(fakeImageFile.getName());
        final List<List<RType>> result = iQuery.projection(
                "SELECT id FROM OriginalFile WHERE name = :name ORDER BY id DESC LIMIT 1",
                new ParametersI().add("name", imageName));
        final long previousId = result.isEmpty() ? -1 : ((RLong) result.get(0).get(0)).getValue();
        try {
            List<String> path = Collections.singletonList(fakeImageFile.getPath());
            importFileset(path);
            Assert.assertTrue(isAdmin);
        } catch (ServerError se) {
            Assert.assertFalse(isAdmin);
        }
        OriginalFile remoteFile = (OriginalFile) iQuery.findByQuery(
                "FROM OriginalFile o WHERE o.id > :id AND o.name = :name",
                new ParametersI().addId(previousId).add("name", imageName));
        if (isAdmin) {
            Assert.assertEquals(remoteFile.getDetails().getOwner().getId().getValue(), normalUser.userId);
            Assert.assertEquals(remoteFile.getDetails().getGroup().getId().getValue(), normalUser.groupId);
        }
        Image image = (Image) iQuery.findByQuery(
                "FROM Image WHERE fileset IN "
                + "(SELECT fileset FROM FilesetEntry WHERE originalFile.id = :id)",
                new ParametersI().addId(remoteFile.getId()));
        /* take care of post-import workflows which do not use sudo */
        if (!isSudoing) {
            loginUser(lightAdmin);
        }
        /* remember in which group the image was before chgrp was attempted */
        long imageGroupId = image.getDetails().getGroup().getId().getValue();
        /*in order to find the image in whatever group, get context with group
         * set to -1 (=all groups) */
        client.getImplicitContext().put("omero.group", Long.toString(-1));
        /* try to move the image into another group of the normalUser
         * which should succeed if sudoing and also in case
         * the light admin has Chgrp permissions
         * (i.e. isExpectSuccess is true) */
        doChange(client, factory, Requests.chgrp().target(image).toGroup(normalUsersOtherGroupId).build(), isExpectSuccessInMemberGroup);
        image = (Image) iQuery.get("Image", image.getId().getValue());
        remoteFile = (OriginalFile) iQuery.get("OriginalFile", remoteFile.getId().getValue());
        /* note in which group the image now is now */
        imageGroupId = image.getDetails().getGroup().getId().getValue();
        if (isExpectSuccessInMemberGroup) {
            Assert.assertEquals(imageGroupId, normalUsersOtherGroupId);
            Assert.assertEquals(remoteFile.getDetails().getGroup().getId().getValue(), normalUsersOtherGroupId);
        } else {
            Assert.assertEquals(imageGroupId, normalUser.groupId);
            Assert.assertEquals(remoteFile.getDetails().getGroup().getId().getValue(), normalUser.groupId);
        }
        /* in any case, the image should still belong to normalUser */
        Assert.assertEquals(image.getDetails().getOwner().getId().getValue(), normalUser.userId);

        /* try to move into another group the normalUser
         * is not a member of, which should fail in all cases
         * except the light admin has Chgrp permission and is not sudoing
         * (i.e. chgrpNoSudoExpectSuccessAnyGroup is true) */
        doChange(client, factory, Requests.chgrp().target(image).toGroup(anotherGroupId).build(),
                chgrpNoSudoExpectSuccessAnyGroup);
        image = (Image) iQuery.get("Image", image.getId().getValue());
        remoteFile = (OriginalFile) iQuery.get("OriginalFile", remoteFile.getId().getValue());
        if(chgrpNoSudoExpectSuccessAnyGroup) {
            /* check that the image moved to another group */
            Assert.assertEquals(image.getDetails().getGroup().getId().getValue(), anotherGroupId);
            Assert.assertEquals(remoteFile.getDetails().getGroup().getId().getValue(), anotherGroupId);
        } else {
            /* check that the image is still in its original group
             * (stored in the imageGroupId variable) */
            Assert.assertEquals(image.getDetails().getGroup().getId().getValue(), imageGroupId);
            Assert.assertEquals(remoteFile.getDetails().getGroup().getId().getValue(), imageGroupId);
        }
        /* in any case, the image should still belong to normalUser */
        Assert.assertEquals(image.getDetails().getOwner().getId().getValue(), normalUser.userId);
    }

    /**
     * Test that an ImporterAs cannot
     * chown on behalf of another user in any combination of <tt>Sudo</tt> privilege
     * with having or not having also the <tt>Chown</tt>. <tt>WriteOwned</tt> and
     * <tt>WriteFile</tt> privileges except for having all three of them (in which case
     * the chown action wiill succeed)
     * @throws Exception unexpected
     */
    @Test(dataProvider = "combined privileges cases")
    public void testImporterAsSudoChown(boolean isAdmin, boolean isSudoing, boolean permChown,
            boolean permWriteOwned, boolean permWriteFile, boolean permDeleteOwned, String groupPermissions) throws Exception {
        final EventContext normalUser = newUserAndGroup(groupPermissions);
        final boolean chownPassing = isAdmin && permChown && permWriteOwned && permWriteFile && permDeleteOwned;
        final long anotherUserId = newUserAndGroup(groupPermissions).userId;
        /* set up the basic permissions for this test */
        ArrayList <String> permissions = new ArrayList <String>();
        permissions.add(AdminPrivilegeSudo.value);
        if (permChown) permissions.add(AdminPrivilegeChown.value);;
        if (permWriteOwned) permissions.add(AdminPrivilegeWriteOwned.value);
        if (permWriteFile) permissions.add(AdminPrivilegeWriteFile.value);
        if (permDeleteOwned) permissions.add(AdminPrivilegeDeleteOwned.value);
        final EventContext lightAdmin;
        lightAdmin = loginNewAdmin(isAdmin, permissions);
        try {
            sudo(new ExperimenterI(normalUser.userId, false));
                if (!isAdmin) {
                    Assert.fail("Sudo-permitted non-administrators cannot sudo.");
                }
            }catch (SecurityViolation sv) {
                /* sudo expected to fail if the user is not in system group */
        }

        /* import an image for the normalUser into the normalUser's default group */
        if (!isAdmin) return;
        client.getImplicitContext().put("omero.group", Long.toString(normalUser.groupId));
        final RString imageName = omero.rtypes.rstring(fakeImageFile.getName());
        final List<List<RType>> result = iQuery.projection(
                "SELECT id FROM OriginalFile WHERE name = :name ORDER BY id DESC LIMIT 1",
                new ParametersI().add("name", imageName));
        final long previousId = result.isEmpty() ? -1 : ((RLong) result.get(0).get(0)).getValue();
        try {
            List<String> path = Collections.singletonList(fakeImageFile.getPath());
            importFileset(path);
            Assert.assertTrue(isAdmin);
        } catch (ServerError se) {
                Assert.assertFalse(isAdmin);
        }
        OriginalFile remoteFile = (OriginalFile) iQuery.findByQuery(
                "FROM OriginalFile o WHERE o.id > :id AND o.name = :name",
                new ParametersI().addId(previousId).add("name", imageName));
        if (isAdmin) {
            Assert.assertEquals(remoteFile.getDetails().getOwner().getId().getValue(), normalUser.userId);
            Assert.assertEquals(remoteFile.getDetails().getGroup().getId().getValue(), normalUser.groupId);
        }
        Image image = (Image) iQuery.findByQuery(
                "FROM Image WHERE fileset IN "
                + "(SELECT fileset FROM FilesetEntry WHERE originalFile.id = :id)",
                new ParametersI().addId(remoteFile.getId()));
        /* stop sudoing for some test cases by logging in as light admin */
        if (!isSudoing) {
            loginUser(lightAdmin);
        }
        /* try to chown the image of the normalUser just being sudoed,
         * which should fail in both cases you have a chown permissions or
         * not */
        client.getImplicitContext().put("omero.group", Long.toString(normalUser.groupId));
        if (isSudoing) {
            doChange(client, factory, Requests.chown().target(image).toUser(anotherUserId).build(), false);
            image = (Image) iQuery.get("Image", image.getId().getValue());
            remoteFile = (OriginalFile) iQuery.findByQuery(
                    "FROM OriginalFile o WHERE o.id > :id AND o.name = :name",
                    new ParametersI().addId(previousId).add("name", imageName));
            Assert.assertEquals(image.getDetails().getOwner().getId().getValue(), normalUser.userId);
            Assert.assertEquals(image.getDetails().getGroup().getId().getValue(), normalUser.groupId);
            Assert.assertEquals(remoteFile.getDetails().getOwner().getId().getValue(), normalUser.userId);
        } else {
            /* when trying to chown the image NOT being sudoed,
             * this should fail in case you have not all of Chown & WriteOwned & WriteFile
             * permissions, collated in "chownPassing" boolean */
            doChange(client, factory, Requests.chown().target(image).toUser(anotherUserId).build(), chownPassing);
            image = (Image) iQuery.get("Image", image.getId().getValue());
            remoteFile = (OriginalFile) iQuery.findByQuery(
                    "FROM OriginalFile o WHERE o.id > :id AND o.name = :name",
                    new ParametersI().addId(previousId).add("name", imageName));
            if (chownPassing) {
                Assert.assertEquals(image.getDetails().getOwner().getId().getValue(), anotherUserId);
                Assert.assertEquals(remoteFile.getDetails().getOwner().getId().getValue(), anotherUserId);
            } else {
                Assert.assertEquals(image.getDetails().getOwner().getId().getValue(), normalUser.userId);
                Assert.assertEquals(remoteFile.getDetails().getOwner().getId().getValue(), normalUser.userId);
            }
            /* in any case, the image must be in the right group */
            Assert.assertEquals(image.getDetails().getGroup().getId().getValue(), normalUser.groupId);
        }
    }

    /**
     * Test that an ImporterAs workflow without using Sudo.
     * The data will be imported into a group the user/(future owner of the data)
     * is a member of, then just chowned to the user.
     * This workflow is possible only if PR#4957 dealing with
     * admins importing data into groups they are not member of will get
     * merged. For this test, combinations of  <tt>Chown</tt>, <tt>Chgrp</tt>,. <tt>WriteOwned</tt>
     * and <tt>WriteFile</tt> privileges will be explored for the light admin.
     * For this workflow the creation and targeting of a Dataset
     * is tested too.
     * @throws Exception unexpected
     */

    @Test(dataProvider = "combined privileges cases")
    public void testImporterAsNoSudoChownOnlyWorkflow(boolean isAdmin, boolean permChgrp, boolean permChown,
            boolean permWriteOwned, boolean permWriteFile, boolean permWriteManagedRepo, String groupPermissions) throws Exception {
        /* define case where the import without any sudo importing into a group
         * the light admin is not a member of is expected to succeed
         */
        boolean importNotYourGroupExpectSuccess = (isAdmin && permWriteOwned && permWriteFile && permWriteManagedRepo);
        /* importing into the group of the normalUser directly
         * will succeed if the import will succeed and the subsequent Chown is possible
         */
        boolean importNotYourGroupAndChownExpectSuccess =
                (isAdmin && permWriteOwned && permWriteFile && permChown && permWriteManagedRepo);
        final EventContext normalUser = newUserAndGroup(groupPermissions);
        /* set up the light admin's permissions for this test */
        ArrayList <String> permissions = new ArrayList <String>();
        if (permChown) permissions.add(AdminPrivilegeChown.value);;
        if (permChgrp) permissions.add(AdminPrivilegeChgrp.value);;
        if (permWriteOwned) permissions.add(AdminPrivilegeWriteOwned.value);
        if (permWriteFile) permissions.add(AdminPrivilegeWriteFile.value);
        if (permWriteManagedRepo) permissions.add(AdminPrivilegeWriteManagedRepo.value);
        final EventContext lightAdmin;
        lightAdmin = loginNewAdmin(isAdmin, permissions);
        if (!isAdmin) return;
        /* First create a Dataset in the normalUser's group (you are not 
         * a member of this goup) */
        client.getImplicitContext().put("omero.group", Long.toString(normalUser.groupId));
        Dataset dat = mmFactory.simpleDataset();
        Dataset sentDat = new DatasetI();
        sentDat = null;
        if (importNotYourGroupExpectSuccess) {/* you are allowed to create the dataset only
        with sufficient permissions, which are the same as permissions for importing */
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
        final OriginalFile remoteFile = (OriginalFile) iQuery.findByQuery(
                "FROM OriginalFile o WHERE o.id > :id AND o.name = :name",
                new ParametersI().addId(previousId).add("name", imageName));
        if (importNotYourGroupExpectSuccess) {
            Assert.assertEquals(remoteFile.getDetails().getOwner().getId().getValue(), lightAdmin.userId);
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
            Assert.assertEquals(image.getDetails().getOwner().getId().getValue(), lightAdmin.userId);
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
        if (importNotYourGroupAndChownExpectSuccess) {
            doChange(client, factory, Requests.chown().target(sentDat).toUser(normalUser.userId).build(), true);
            image = (Image) iQuery.get("Image", image.getId().getValue());
            dat = (Dataset) iQuery.get("Dataset", sentDat.getId().getValue());
            DatasetImageLink link = (DatasetImageLink) iQuery.findByQuery(
                    "FROM DatasetImageLink WHERE parent.id  = :id",
                    new ParametersI().addId(sentDat.getId()));
            /* image, dataset and link are in the normalUser's group and belong to normalUser */
            Assert.assertEquals(image.getDetails().getOwner().getId().getValue(), normalUser.userId);
            Assert.assertEquals(image.getDetails().getGroup().getId().getValue(), normalUser.groupId);
            Assert.assertEquals(dat.getDetails().getOwner().getId().getValue(), normalUser.userId);
            Assert.assertEquals(dat.getDetails().getGroup().getId().getValue(), normalUser.groupId);
            Assert.assertEquals(link.getDetails().getOwner().getId().getValue(), normalUser.userId);
            Assert.assertEquals(link.getDetails().getGroup().getId().getValue(), normalUser.groupId);
        } else {
            doChange(client, factory, Requests.chown().target(sentDat).toUser(normalUser.userId).build(), false);
            image = (Image) iQuery.get("Image", image.getId().getValue());
            dat = (Dataset) iQuery.get("Dataset", sentDat.getId().getValue());
            DatasetImageLink link = (DatasetImageLink) iQuery.findByQuery(
                    "FROM DatasetImageLink WHERE parent.id  = :id",
                    new ParametersI().addId(sentDat.getId()));
            /* check that the image, dataset and link still belongs
             * to the light admin as the chown failed, but are in the group of normalUser */
            Assert.assertEquals(image.getDetails().getOwner().getId().getValue(), lightAdmin.userId);
            Assert.assertEquals(image.getDetails().getGroup().getId().getValue(), normalUser.groupId);
            Assert.assertEquals(dat.getDetails().getOwner().getId().getValue(), lightAdmin.userId);
            Assert.assertEquals(dat.getDetails().getGroup().getId().getValue(), normalUser.groupId);
            Assert.assertEquals(link.getDetails().getOwner().getId().getValue(), lightAdmin.userId);
            Assert.assertEquals(link.getDetails().getGroup().getId().getValue(), normalUser.groupId);
        }
    }

    /** Additonal test of ImporterAs without using Sudo.
     * The workflow deals with the eventuality of pre-existing container
     * in the target group and linking of the image or dataset to this container
     * (dataset or project). The image import has been tested in other tests,
     * here the image and/or dataset will be created and saved instead and just
     * the linking to a container will be tested. Only when the light admin has
     * WriteOwned privilege is the linking possible.
     * @throws Exception unexpected
     */
    @Test(dataProvider = "combined privileges cases")
    public void testImporterAsNoSudoLinkInTargetGroup(boolean isAdmin, boolean permChgrp, boolean permChown,
            boolean permWriteOwned, boolean permWriteFile, String groupPermissions) throws Exception {
        /* linking should be always permitted as long as light admin is in System Group
         * and has WriteOwned permissions. Exception is Private group, where linking will
         * always fail.*/
        boolean isExpectSuccess = isAdmin && permWriteOwned;
        final EventContext normalUser = newUserAndGroup(groupPermissions);
        /* set up the light admin's permissions for this test */
        ArrayList <String> permissions = new ArrayList <String>();
        if (permChown) permissions.add(AdminPrivilegeChown.value);;
        if (permChgrp) permissions.add(AdminPrivilegeChgrp.value);;
        if (permWriteOwned) permissions.add(AdminPrivilegeWriteOwned.value);
        if (permWriteFile) permissions.add(AdminPrivilegeWriteFile.value);
        final EventContext lightAdmin;
        lightAdmin = loginNewAdmin(isAdmin, permissions);
        /* create an image, dataset and project as normalUser in a group of the normalUser */
        if (!isAdmin) return;
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
        if (!isExpectSuccess) return; /* further testing not necessary in case links could not be created */
        if (groupPermissions == "rw----") return; /* in private group linking not possible */
        DatasetImageLink linkOfDatasetImage = linkDatasetImage(sentDat, sentImage);
        ProjectDatasetLink linkOfProjectDataset = linkProjectDataset(sentProj, sentDat);
        /* after successful linkage, transfer the ownership
         * of both links to the normalUser. For that the light admin
         * needs additonally the Chown permission. Note that the links
         * have to be transferred step by step, as the Chown feature
         * of whole hierarchy does not transfer links owned by non-owners
         * of the P/D?I objects. */
        Chown2 chown = Requests.chown().target(linkOfDatasetImage).toUser(normalUser.userId).build();
        doChange(client, factory, chown, permChown);
        chown = Requests.chown().target(linkOfProjectDataset).toUser(normalUser.userId).build();
        doChange(client, factory, chown, permChown);

        /* now retrieve and check that the links, image, dataset and project
         * are owned by normalUser */
        Image retrievedImage = (Image) iQuery.get("Image", sentImage.getId().getValue());
        Dataset retrievedDataset = (Dataset) iQuery.get("Dataset", sentDat.getId().getValue());
        Project retrievedProject = (Project) iQuery.get("Project", sentProj.getId().getValue());
        DatasetImageLink retrievedDatasetImageLink = (DatasetImageLink) iQuery.findByQuery(
                "FROM DatasetImageLink WHERE parent.id  = :id",
                new ParametersI().addId(sentDat.getId()));
        ProjectDatasetLink retrievedProjectDatasetLink = (ProjectDatasetLink) iQuery.findByQuery(
                "FROM ProjectDatasetLink WHERE parent.id  = :id",
                new ParametersI().addId(sentProj.getId()));
        Assert.assertEquals(retrievedImage.getDetails().getOwner().getId().getValue(), normalUser.userId);
        Assert.assertEquals(retrievedDataset.getDetails().getOwner().getId().getValue(), normalUser.userId);
        Assert.assertEquals(retrievedProject.getDetails().getOwner().getId().getValue(), normalUser.userId);
        if (permChown) {
            Assert.assertEquals(retrievedDatasetImageLink.getDetails().getOwner().getId().getValue(), normalUser.userId);
            Assert.assertEquals(retrievedProjectDatasetLink.getDetails().getOwner().getId().getValue(), normalUser.userId);
        } else {
            Assert.assertEquals(retrievedDatasetImageLink.getDetails().getOwner().getId().getValue(), lightAdmin.userId);
            Assert.assertEquals(retrievedProjectDatasetLink.getDetails().getOwner().getId().getValue(), lightAdmin.userId);
        }
    }

        /** Test a workflow of ImporterAs without using Sudo.
         * The data will be imported to the group
         * of the light admin (where the user is not a member)
         * and chgrp-ed and chowned into the correct group/user afterwards.
         * For this test, combinations of  <tt>Chown</tt>, <tt>Chgrp</tt>,. <tt>WriteOwned</tt> and
         * and <tt>WriteFile</tt> privileges will be explored for the light admin.
         * For this workflow the creation and targeting of a Dataset
         * is tested too.
         * @throws Exception unexpected
         */
        @Test(dataProvider = "combined privileges cases")
        public void testImporterAsNoSudoChgrpChownWorkflow(boolean isAdmin, boolean permChgrp, boolean permChown,
                boolean permWriteOwned, boolean permWriteFile, String groupPermissions) throws Exception {
            /* the second workflow with importing into the group of the light admin and
             * subsequent moving the data into the group of normalUser and chowning
             * them to the normal user will succeed if Chgrp and Chown is possible,
             * which needs permChgrp, permChown, but not WriteFile and WriteOwned,
             * because the light admin is the owner of the data which he is chowning
             */
            boolean importYourGroupAndChgrpAndChownExpectSuccess =
                    (isAdmin && permChgrp && permChown);
            final EventContext normalUser = newUserAndGroup(groupPermissions);
            /* set up the light admin's permissions for this test */
            ArrayList <String> permissions = new ArrayList <String>();
            if (permChown) permissions.add(AdminPrivilegeChown.value);;
            if (permChgrp) permissions.add(AdminPrivilegeChgrp.value);;
            if (permWriteOwned) permissions.add(AdminPrivilegeWriteOwned.value);
            if (permWriteFile) permissions.add(AdminPrivilegeWriteFile.value);
            final EventContext lightAdmin;
            lightAdmin = loginNewAdmin(isAdmin, permissions);
        /* Workflow2: import an image as lightAdmin into a group you are a member of */
        if (!isAdmin) return;
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
        Assert.assertEquals(remoteFile.getDetails().getOwner().getId().getValue(), lightAdmin.userId);
        Assert.assertEquals(remoteFile.getDetails().getGroup().getId().getValue(), lightAdmin.groupId);
        /* check that also the image corresponding to the original file is in the right group */
        Image image = null;
        if (!(remoteFile == null)) {
            image = (Image) iQuery.findByQuery("FROM Image WHERE fileset IN "
                    + "(SELECT fileset FROM FilesetEntry WHERE originalFile.id = :id)",
                    new ParametersI().addId(remoteFile.getId()));
        }
        Assert.assertEquals(image.getDetails().getOwner().getId().getValue(), lightAdmin.userId);
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
        if (isAdmin && permChgrp) {
            doChange(client, factory, Requests.chgrp().target(sentDat).toGroup(normalUser.groupId).build(), true);

        } else {
            doChange(client, factory, Requests.chgrp().target(sentDat).toGroup(normalUser.groupId).build(), false);
        }
        /* retrieve again the image, dataset and link */
        image = (Image) iQuery.get("Image", image.getId().getValue());
        dat = (Dataset) iQuery.get("Dataset", sentDat.getId().getValue());
        DatasetImageLink link = (DatasetImageLink) iQuery.findByQuery(
                "FROM DatasetImageLink WHERE parent.id  = :id",
                new ParametersI().addId(sentDat.getId()));
        /* note in which group the image now is now */
        imageGroupId = image.getDetails().getGroup().getId().getValue();
        /* check that the image, dataset, and their link was moved too if the permissions
         * were sufficient */
        if (isAdmin && permChgrp) {
            Assert.assertEquals(imageGroupId, normalUser.groupId);
            Assert.assertEquals(dat.getDetails().getGroup().getId().getValue(), normalUser.groupId);
            Assert.assertEquals(link.getDetails().getGroup().getId().getValue(), normalUser.groupId);
        /* check that the image, dataset and their link were not moved if
         * the permissions were not sufficient
         */
        } else {
            Assert.assertEquals(imageGroupId, lightAdmin.groupId);
            Assert.assertEquals(dat.getDetails().getGroup().getId().getValue(), lightAdmin.groupId);
            Assert.assertEquals(link.getDetails().getGroup().getId().getValue(), lightAdmin.groupId);
        }
        /* now, having moved the dataset, image and link in the group of normalUser,
         * try to change the ownership of the dataset to the normalUser */
        /* Chowning the dataset should fail in case you have not both of
         * isAdmin & Chown permissions which are
         * captured in the boolean importYourGroupAndChgrpAndChownExpectSuccess.
         * Additionally, in this boolean is permChgrp, which was necessary for the
         * previous step of moving the data into normalUser's group.
         * A successful chowning of the dataset will chown the linked image
         * and the link too.*/
        if (importYourGroupAndChgrpAndChownExpectSuccess) {/* whole workflow2 succeeded */
            doChange(client, factory, Requests.chown().target(dat).toUser(normalUser.userId).build(), true);
            image = (Image) iQuery.get("Image", image.getId().getValue());
            dat = (Dataset) iQuery.get("Dataset", sentDat.getId().getValue());
            link = (DatasetImageLink) iQuery.findByQuery(
                    "FROM DatasetImageLink WHERE parent.id  = :id",
                    new ParametersI().addId(sentDat.getId()));
            /* image, dataset and link are in the normalUser's group and belong to normalUser */
            Assert.assertEquals(image.getDetails().getOwner().getId().getValue(), normalUser.userId);
            Assert.assertEquals(image.getDetails().getGroup().getId().getValue(), normalUser.groupId);
            Assert.assertEquals(dat.getDetails().getOwner().getId().getValue(), normalUser.userId);
            Assert.assertEquals(dat.getDetails().getGroup().getId().getValue(), normalUser.groupId);
            Assert.assertEquals(link.getDetails().getOwner().getId().getValue(), normalUser.userId);
            Assert.assertEquals(link.getDetails().getGroup().getId().getValue(), normalUser.groupId);
        } else if (permChown) {
            /* even if the workflow2 as a whole failed, the chown might be successful */
            doChange(client, factory, Requests.chown().target(dat).toUser(normalUser.userId).build(), true);
            image = (Image) iQuery.get("Image", image.getId().getValue());
            dat = (Dataset) iQuery.get("Dataset", sentDat.getId().getValue());
            link = (DatasetImageLink) iQuery.findByQuery(
                    "FROM DatasetImageLink WHERE parent.id  = :id",
                    new ParametersI().addId(sentDat.getId()));
            /* the image, dataset and link belong to the normalUser, but is in the light admin's group */
            Assert.assertEquals(image.getDetails().getOwner().getId().getValue(), normalUser.userId);
            Assert.assertEquals(image.getDetails().getGroup().getId().getValue(), lightAdmin.groupId);
            Assert.assertEquals(dat.getDetails().getOwner().getId().getValue(), normalUser.userId);
            Assert.assertEquals(dat.getDetails().getGroup().getId().getValue(), lightAdmin.groupId);
            Assert.assertEquals(link.getDetails().getOwner().getId().getValue(), normalUser.userId);
            Assert.assertEquals(link.getDetails().getGroup().getId().getValue(), lightAdmin.groupId);
        } else if (permChgrp) {
            /* as workflow2 as a whole failed, in case the chgrp was successful,
             * the chown must be failing */
            doChange(client, factory, Requests.chown().target(dat).toUser(normalUser.userId).build(), false);
            image = (Image) iQuery.get("Image", image.getId().getValue());
            dat = (Dataset) iQuery.get("Dataset", sentDat.getId().getValue());
            link = (DatasetImageLink) iQuery.findByQuery(
                    "FROM DatasetImageLink WHERE parent.id  = :id",
                    new ParametersI().addId(sentDat.getId()));
            /* the image, dataset and link are in normalUser's group but still belong to light admin */
            Assert.assertEquals(image.getDetails().getOwner().getId().getValue(), lightAdmin.userId);
            Assert.assertEquals(image.getDetails().getGroup().getId().getValue(), normalUser.groupId);
            Assert.assertEquals(dat.getDetails().getOwner().getId().getValue(), lightAdmin.userId);
            Assert.assertEquals(dat.getDetails().getGroup().getId().getValue(), normalUser.groupId);
            Assert.assertEquals(link.getDetails().getOwner().getId().getValue(), lightAdmin.userId);
            Assert.assertEquals(link.getDetails().getGroup().getId().getValue(), normalUser.groupId);
        } else {
            /* the remaining option when the previous chgrp as well as this chown fail */
            doChange(client, factory, Requests.chown().target(dat).toUser(normalUser.userId).build(), false);
            image = (Image) iQuery.get("Image", image.getId().getValue());
            dat = (Dataset) iQuery.get("Dataset", sentDat.getId().getValue());
            link = (DatasetImageLink) iQuery.findByQuery(
                    "FROM DatasetImageLink WHERE parent.id  = :id",
                    new ParametersI().addId(sentDat.getId()));
            /* the image, dataset and link are in light admin's group and belong to light admin */
            Assert.assertEquals(image.getDetails().getOwner().getId().getValue(), lightAdmin.userId);
            Assert.assertEquals(image.getDetails().getGroup().getId().getValue(), lightAdmin.groupId);
            Assert.assertEquals(dat.getDetails().getOwner().getId().getValue(), lightAdmin.userId);
            Assert.assertEquals(dat.getDetails().getGroup().getId().getValue(), lightAdmin.groupId);
            Assert.assertEquals(link.getDetails().getOwner().getId().getValue(), lightAdmin.userId);
            Assert.assertEquals(link.getDetails().getGroup().getId().getValue(), lightAdmin.groupId);
        }
    }

    /** Test of DataOrganizer.
     * The workflow deals with the possibility of having to transfer all the data
     * to another user using the Chown privilege and using the targetUser
     * option of the Chown2 command which transfers all the data owned by one
     * user to another user. The data are in 2 groups, of which the original data owner
     * is a member of, the recipient of the data is just a member of one of the groups.
     * @throws Exception unexpected
     */
    @Test(dataProvider = "combined privileges cases")
    public void testDataOrganizerChownAll(boolean isAdmin, boolean permChgrp, boolean permChown,
            boolean permWriteOwned, boolean permWriteFile, boolean permDeleteOwned, String groupPermissions) throws Exception {
        final boolean isExpectSuccess = isAdmin && permChown && permWriteOwned && permWriteFile && permDeleteOwned;
        /* chown is passing in this test with isAdmin, permChown and permWriteOwned only,
         * the permWriteFile is not necessary, but note that this is just because we have
         * images with no original files linked to them. If there would be original files, then
         * chown would work only with permWriteFile, just as in testImporterAsSudoChown.*/
        final boolean chownPassing = isAdmin && permChown && permWriteOwned && permDeleteOwned;
        if (!isExpectSuccess) return;
        final EventContext normalUser = newUserAndGroup(groupPermissions);
        ExperimenterGroup otherGroup = newGroupAddUser(groupPermissions, normalUser.userId, false);
        final EventContext recipient = newUserInGroup(otherGroup, false);
        /* set up the light admin's permissions for this test */
        ArrayList <String> permissions = new ArrayList <String>();
        if (permChown) permissions.add(AdminPrivilegeChown.value);;
        if (permChgrp) permissions.add(AdminPrivilegeChgrp.value);;
        if (permWriteOwned) permissions.add(AdminPrivilegeWriteOwned.value);
        if (permWriteFile) permissions.add(AdminPrivilegeWriteFile.value);
        if (permDeleteOwned) permissions.add(AdminPrivilegeDeleteOwned.value);
        final EventContext lightAdmin;
        lightAdmin = loginNewAdmin(isAdmin, permissions);
        /* create two sets of P/D/I hierarchy as normalUser in the default
         * group of the normalUser */
        if (!isAdmin) return;
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
        if (!isExpectSuccess) {
            return;
        }
        /* check the transfer of all the data in the first group was successful */
        client.getImplicitContext().put("omero.group", Long.toString(-1));
        Project retrievedProject1 = (Project) iQuery.get("Project", sentProj1.getId().getValue());
        Dataset retrievedDataset1 = (Dataset) iQuery.get("Dataset", sentDat1.getId().getValue());
        Image retrievedImage1 = (Image) iQuery.get("Image", sentImage1.getId().getValue());
        DatasetImageLink retrievedDatasetImageLink1 = (DatasetImageLink) iQuery.findByQuery(
                "FROM DatasetImageLink WHERE parent.id  = :id",
                new ParametersI().addId(sentDat1.getId()));
        ProjectDatasetLink retrievedProjectDatasetLink1 = (ProjectDatasetLink) iQuery.findByQuery(
                "FROM ProjectDatasetLink WHERE child.id  = :id",
                new ParametersI().addId(sentDat1.getId()));
        Assert.assertEquals(retrievedProject1.getDetails().getOwner().getId().getValue(), recipient.userId);
        Assert.assertEquals(retrievedDataset1.getDetails().getOwner().getId().getValue(), recipient.userId);
        Assert.assertEquals(retrievedImage1.getDetails().getOwner().getId().getValue(), recipient.userId);
        Assert.assertEquals(retrievedDatasetImageLink1.getDetails().getOwner().getId().getValue(), recipient.userId);
        Assert.assertEquals(retrievedProjectDatasetLink1.getDetails().getOwner().getId().getValue(), recipient.userId);
        Project retrievedProject2 = (Project) iQuery.get("Project", sentProj2.getId().getValue());
        Dataset retrievedDataset2 = (Dataset) iQuery.get("Dataset", sentDat2.getId().getValue());
        Image retrievedImage2 = (Image) iQuery.get("Image", sentImage2.getId().getValue());
        DatasetImageLink retrievedDatasetImageLink2 = (DatasetImageLink) iQuery.findByQuery(
                "FROM DatasetImageLink WHERE parent.id  = :id",
                new ParametersI().addId(sentDat2.getId()));
        ProjectDatasetLink retrievedProjectDatasetLink2 = (ProjectDatasetLink) iQuery.findByQuery(
                "FROM ProjectDatasetLink WHERE child.id  = :id",
                new ParametersI().addId(sentDat2.getId()));
        Assert.assertEquals(retrievedProject2.getDetails().getOwner().getId().getValue(), recipient.userId);
        Assert.assertEquals(retrievedDataset2.getDetails().getOwner().getId().getValue(), recipient.userId);
        Assert.assertEquals(retrievedImage2.getDetails().getOwner().getId().getValue(), recipient.userId);
        Assert.assertEquals(retrievedDatasetImageLink2.getDetails().getOwner().getId().getValue(), recipient.userId);
        Assert.assertEquals(retrievedProjectDatasetLink2.getDetails().getOwner().getId().getValue(), recipient.userId);
        /* check ownership of the objects in otherGroup */
        Project retrievedProjectOtherGroup1 = (Project) iQuery.get("Project", sentProj1OtherGroup.getId().getValue());
        Dataset retrievedDatasetOtherGroup1 = (Dataset) iQuery.get("Dataset", sentDat1OtherGroup.getId().getValue());
        Image retrievedImageOtherGroup1 = (Image) iQuery.get("Image", sentImage1OtherGroup.getId().getValue());
        DatasetImageLink retrievedDatasetImageLinkOtherGroup1 = (DatasetImageLink) iQuery.findByQuery(
                "FROM DatasetImageLink WHERE parent.id  = :id",
                new ParametersI().addId(sentDat1OtherGroup.getId()));
        ProjectDatasetLink retrievedProjectDatasetLinkOtherGroup1 = (ProjectDatasetLink) iQuery.findByQuery(
                "FROM ProjectDatasetLink WHERE child.id  = :id",
                new ParametersI().addId(sentDat1OtherGroup.getId()));
        Assert.assertEquals(retrievedProjectOtherGroup1.getDetails().getOwner().getId().getValue(), recipient.userId);
        Assert.assertEquals(retrievedDatasetOtherGroup1.getDetails().getOwner().getId().getValue(), recipient.userId);
        Assert.assertEquals(retrievedImageOtherGroup1.getDetails().getOwner().getId().getValue(), recipient.userId);
        Assert.assertEquals(retrievedDatasetImageLinkOtherGroup1.getDetails().getOwner().getId().getValue(), recipient.userId);
        Assert.assertEquals(retrievedProjectDatasetLinkOtherGroup1.getDetails().getOwner().getId().getValue(), recipient.userId);
        Project retrievedProjectOtherGroup2 = (Project) iQuery.get("Project", sentProj2OtherGroup.getId().getValue());
        Dataset retrievedDatasetOtherGroup2 = (Dataset) iQuery.get("Dataset", sentDat2OtherGroup.getId().getValue());
        Image retrievedImageOtherGroup2 = (Image) iQuery.get("Image", sentImage2OtherGroup.getId().getValue());
        DatasetImageLink retrievedDatasetImageLinkOtherGroup2 = (DatasetImageLink) iQuery.findByQuery(
                "FROM DatasetImageLink WHERE parent.id  = :id",
                new ParametersI().addId(sentDat2OtherGroup.getId()));
        ProjectDatasetLink retrievedProjectDatasetLinkOtherGroup2 = (ProjectDatasetLink) iQuery.findByQuery(
                "FROM ProjectDatasetLink WHERE child.id  = :id",
                new ParametersI().addId(sentDat2OtherGroup.getId()));
        Assert.assertEquals(retrievedProjectOtherGroup2.getDetails().getOwner().getId().getValue(), recipient.userId);
        Assert.assertEquals(retrievedDatasetOtherGroup2.getDetails().getOwner().getId().getValue(), recipient.userId);
        Assert.assertEquals(retrievedImageOtherGroup2.getDetails().getOwner().getId().getValue(), recipient.userId);
        Assert.assertEquals(retrievedDatasetImageLinkOtherGroup2.getDetails().getOwner().getId().getValue(), recipient.userId);
        Assert.assertEquals(retrievedProjectDatasetLinkOtherGroup2.getDetails().getOwner().getId().getValue(), recipient.userId);
    }

    /**
     * @return test cases for isAdmin (member of system group) all group permissions cases
     */
    @DataProvider(name = "isAdmin and groupPerms cases")
    public Object[][] provideAdminPrivilegeCases() {
        int index = 0;
        final int IS_ADMIN = index++;
        final int GROUP_PERMS = index++;

        final boolean[] booleanCases = new boolean[]{false, true};
        final String[] permsCases = new String[]{"rw----", "rwr---", "rwra--", "rwrw--"};
        final List<Object[]> testCases = new ArrayList<Object[]>();

        for (final boolean isAdmin : booleanCases) {
            for (final String groupPerms : permsCases) {
                final Object[] testCase = new Object[index];
                testCase[IS_ADMIN] = isAdmin;
                testCase[GROUP_PERMS] = groupPerms;
                // DEBUG  if (isAdmin == false && isRestricted == true && isSudo == false)
                testCases.add(testCase);
            }
        }

        return testCases.toArray(new Object[testCases.size()][]);
    }

    /**
     * @return test cases for adding the privileges combined with isAdmin cases
     */
    @DataProvider(name = "combined privileges cases")
    public Object[][] provideCombinedPrivilegesCases() {
        int index = 0;
        final int IS_ADMIN = index++;
        final int IS_SUDOING = index++;
        final int PERM_ADDITIONAL = index++;
        final int PERM_ADDITIONAL2 = index++;
        final int PERM_ADDITIONAL3 = index++;
        final int PERM_ADDITIONAL4 = index++;
        final int GROUP_PERMS = index++;

        final boolean[] booleanCases = new boolean[]{false, true};
        final String[] permsCases = new String[]{"rw----", "rwr---", "rwra--", "rwrw--"};
        final List<Object[]> testCases = new ArrayList<Object[]>();

        for (final boolean isAdmin : booleanCases) {
            for (final boolean isSudoing : booleanCases) {
                for (final boolean permAdditional : booleanCases) {
                    for (final boolean permAdditional2 : booleanCases) {
                        for (final boolean permAdditional3 : booleanCases) {
                            for (final boolean permAdditional4 : booleanCases) {
                                for (final String groupPerms : permsCases) {
                                    final Object[] testCase = new Object[index];
                                    testCase[IS_ADMIN] = isAdmin;
                                    testCase[IS_SUDOING] = isSudoing;
                                    testCase[PERM_ADDITIONAL] = permAdditional;
                                    testCase[PERM_ADDITIONAL2] = permAdditional2;
                                    testCase[PERM_ADDITIONAL3] = permAdditional3;
                                    testCase[PERM_ADDITIONAL4] = permAdditional4;
                                    testCase[GROUP_PERMS] = groupPerms;
                                    // DEBUG  if (isAdmin == false && isRestricted == true && isSudo == false)
                                    testCases.add(testCase);
                                }
                            }
                        }
                    }
                }
            }
        }
        return testCases.toArray(new Object[testCases.size()][]);
    }
}
