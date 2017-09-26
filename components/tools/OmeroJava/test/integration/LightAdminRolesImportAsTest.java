/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2016 University of Dundee & Open Microscopy Environment.
 *  All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package integration;

import java.util.ArrayList;
import java.util.List;

import omero.ServerError;
import omero.gateway.util.Requests;
import omero.model.Dataset;
import omero.model.DatasetImageLink;
import omero.model.ExperimenterI;
import omero.model.IObject;
import omero.model.Image;
import omero.model.OriginalFile;
import omero.model.Project;
import omero.model.ProjectDatasetLink;
import omero.model.enums.AdminPrivilegeChgrp;
import omero.model.enums.AdminPrivilegeChown;
import omero.model.enums.AdminPrivilegeSudo;
import omero.model.enums.AdminPrivilegeWriteFile;
import omero.model.enums.AdminPrivilegeWriteManagedRepo;
import omero.model.enums.AdminPrivilegeWriteOwned;
import omero.sys.EventContext;
import omero.sys.ParametersI;

import org.testng.Assert;
import org.testng.annotations.Test;


/**
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since 5.4
 */
public class LightAdminRolesImportAsTest extends RolesTests {


    /**
     * Light admin is trying to "import for others" without using Sudo in following manner.
     * lightAdmin imports into group of the normalUser (future owner of the data).
     * lightAdmin then transfers the ownership of the imported data to normalUser.
     * For this test, combinations of <tt>WriteOwned</tt>, <tt>WriteFile</tt>,
     * <tt>WriteManagedRepo</tt> and <tt>Chown</tt> privileges will be explored
     * for lightAdmin. For this workflow the creation and targeting of a Dataset
     * is tested too.
     * @param permWriteOwned if to test a user who has the <tt>WriteOwned</tt> privilege
     * @param permWriteFile if to test a user who has the <tt>WriteFile</tt> privilege
     * @param permWriteManagedRepo if to test a user who has the <tt>WriteManagedRepo</tt> privilege
     * @param permChown if to test a user who has the <tt>Chown</tt> privilege
     * @param groupPermissions to test the effect of group permission level
     * @throws Exception unexpected
     * @see <a href="https://downloads.openmicroscopy.org/resources/experimental/tests/graph-permissions/0.1/testImporterAsNoSudoChownOnlyWorkflow.pptx">graphical explanation</a>
     */
    @Test(dataProvider = "WriteOwned, WriteFile, WriteManagedRepo and Chown privileges cases")
    public void testImporterAsNoSudoChownOnlyWorkflow(boolean permWriteOwned, boolean permWriteFile,
            boolean permWriteManagedRepo, boolean permChown, String groupPermissions) throws Exception {
        /* Define case in which the import not using sudo and importing into a group
         * the light admin is not a member of is expected to succeed.*/
        boolean importNotYourGroupExpectSuccess = permWriteOwned && permWriteFile && permWriteManagedRepo;
        /* Define case in which the creation of Dataset belonging to lightAdmin
         * in a group where lightAdmin is not member is expected to succeed.*/
        boolean createDatasetExpectSuccess = permWriteOwned;
        /* Define case in which the whole workflow is possible (as lightAdmin create
         * Dataset, import into it, then chown the Dataset with the imported
         * image to normalUser).*/
        boolean createDatasetImportNotYourGroupAndChownExpectSuccess =
                permChown && permWriteManagedRepo && permWriteOwned && permWriteFile;
        final EventContext normalUser = newUserAndGroup(groupPermissions);
        /* Set up the light admin's permissions for this test.*/
        List<String> permissions = new ArrayList<String>();
        if (permChown) permissions.add(AdminPrivilegeChown.value);
        if (permWriteOwned) permissions.add(AdminPrivilegeWriteOwned.value);
        if (permWriteFile) permissions.add(AdminPrivilegeWriteFile.value);
        if (permWriteManagedRepo) permissions.add(AdminPrivilegeWriteManagedRepo.value);
        final EventContext lightAdmin;
        lightAdmin = loginNewAdmin(true, permissions);
        /* lightAdmin creates Dataset in the normalUser's group
         * (lightAdmin is not member of that group).*/
        client.getImplicitContext().put("omero.group", Long.toString(normalUser.groupId));
        Dataset dat = mmFactory.simpleDataset();
        Dataset sentDat = null;
        /* Creation of Dataset success is governed by
         * createDatasetExpectSuccess boolean (defined above).*/
        try {
            sentDat = (Dataset) iUpdate.saveAndReturnObject(dat);
            Assert.assertTrue(createDatasetExpectSuccess);
        } catch (ServerError se) {
            Assert.assertFalse(createDatasetExpectSuccess, se.toString());
        }
        /* As lightAdmin, import an Image into the created Dataset.*/
        OriginalFile originalFile = null;
        Image image = null;
        /* Import success is governed by importNotYourGroupExpectSuccess boolean (defined above).*/
        try {
            List<IObject> originalFileAndImage = importImageWithOriginalFile(sentDat);
            originalFile = (OriginalFile) originalFileAndImage.get(0);
            image = (Image) originalFileAndImage.get(1);
            Assert.assertTrue(importNotYourGroupExpectSuccess);
        } catch (ServerError se) {
            Assert.assertFalse(importNotYourGroupExpectSuccess, se.toString());
        }
        /* Check the ownership and group of the original file and the image.*/
        if (importNotYourGroupExpectSuccess) {
            assertOwnedBy(originalFile, lightAdmin);
            assertInGroup(originalFile, normalUser.groupId);
            assertOwnedBy(image, lightAdmin);
            assertInGroup(image, normalUser.groupId);
        /* In case the import was not successful, Image does not exist.
         * Further testing is not interesting in such case.*/
        } else {
            Assert.assertNull(originalFile, "if import failed, the originalFile should be null");
            Assert.assertNull(image, "if import failed, the image should be null");
            return;
        }
        /* Check that the canChown value on the Dataset matches the boolean
         * createDatasetImportNotYourGroupAndChownExpectSuccess.*/
        Assert.assertEquals(getCurrentPermissions(sentDat).canChown(),
                createDatasetImportNotYourGroupAndChownExpectSuccess);
        /* lightAdmin tries to change the ownership of the Dataset to normalUser.*/
        doChange(client, factory, Requests.chown().target(sentDat).toUser(normalUser.userId).build(),
                createDatasetImportNotYourGroupAndChownExpectSuccess);
        final DatasetImageLink link = (DatasetImageLink) iQuery.findByQuery(
                "FROM DatasetImageLink WHERE parent.id = :id",
                new ParametersI().addId(sentDat.getId()));
        /* Check that image, dataset and link are in the normalUser's group
         * and belong to normalUser in case the workflow succeeded.*/
        if (createDatasetImportNotYourGroupAndChownExpectSuccess) {
            assertOwnedBy(image, normalUser);
            assertInGroup(image, normalUser.groupId);
            assertOwnedBy(sentDat, normalUser);
            assertInGroup(sentDat, normalUser.groupId);
            assertOwnedBy(link, normalUser);
            assertInGroup(link, normalUser.groupId);
            assertOwnedBy(originalFile, normalUser);
            assertInGroup(originalFile, normalUser.groupId);
        /* Check that the image, dataset and link still belong
         * to lightAdmin as the chown failed, but are in the group of normalUser.*/
        } else {
            assertOwnedBy(image, lightAdmin);
            assertInGroup(image, normalUser.groupId);
            assertOwnedBy(sentDat, lightAdmin);
            assertInGroup(sentDat, normalUser.groupId);
            assertOwnedBy(link, lightAdmin);
            assertInGroup(link, normalUser.groupId);
        }
    }


    /**
     * Light administrator (lightAdmin) tries to create new Project and Dataset and set normalUser as
     * the owner of the Project and Dataset in a group where lightAdmin is not member (normalUser's group).
     * lightAdmin tries the creation above both sudoing and not sudoing as normalUser
     * and both having and not having <tt>WriteOwned</tt> privilege.
     * The test finishes for test cases in which lightAdmin does not sudo after the creation attempt above.
     * For cases in which lightAdmin does Sudo as normalUser:
     * lightAdmin tries to import data on behalf of normalUser into the just created Dataset.
     * lightAdmin, still sudoed as normalUser, tries then to link the Dataset to the Project.
     * Non-sudoing workflows for import and linking are too complicated to be included in this test.
     * Those two actions are covered by separate tests ({@link #testImporterAsNoSudoChownOnlyWorkflow testImporterAsNoSudoChownOnly},
     * {@link #testImporterAsNoSudoChgrpChownWorkflow testImporterAsNoSudoChgrpChown} and {@link #testLinkNoSudo testLinkNoSudo}).
     * @param isSudoing if to test a success of workflows where Sudoed in
     * @param permWriteOwned if to test a user who has the <tt>WriteOwned</tt> privilege
     * @param groupPermissions to test the effect of group permission level
     * @throws Exception unexpected
     * @see <a href="https://downloads.openmicroscopy.org/resources/experimental/tests/graph-permissions/0.1/testImporterAsSudoCreateImport.pptx">graphical explanation</a>
     */
    @Test(dataProvider = "isSudoing and WriteOwned privileges cases")
    public void testImporterAsSudoCreateImport(boolean isSudoing, boolean permWriteOwned,
            String groupPermissions) throws Exception {
        final EventContext normalUser = newUserAndGroup(groupPermissions);
        /* Only WriteOwned permission is needed for creation of objects when not sudoing.
         * When sudoing, no other permission is needed.*/
        final boolean isExpectSuccessCreate = permWriteOwned || isSudoing;
        /* Set up the light admin's permissions for this test.*/
        List<String> permissions = new ArrayList<String>();
        permissions.add(AdminPrivilegeSudo.value);
        if (permWriteOwned) permissions.add(AdminPrivilegeWriteOwned.value);
        loginNewAdmin(true, permissions);
        /* lightAdmin possibly sudoes on behalf of normalUser, depending on test case.*/
        if (isSudoing) sudo(new ExperimenterI(normalUser.userId, false));

        /* lightAdmin tries to create Project and Dataset on behalf of the normalUser
         * in normalUser's group.*/
        client.getImplicitContext().put("omero.group", Long.toString(normalUser.groupId));
        Project proj = mmFactory.simpleProject();
        Dataset dat = mmFactory.simpleDataset();
        Project sentProj = null;
        Dataset sentDat = null;
        /* lightAdmin sets the normalUser as the owner of the newly created Project/Dataset but only
         * in cases in which lightAdmin is not sudoing (if sudoing, the created Project/Dataset
         * already belong to normalUser).*/
        if (!isSudoing) {
            proj.getDetails().setOwner(new ExperimenterI(normalUser.userId, false));
            dat.getDetails().setOwner(new ExperimenterI(normalUser.userId, false));
        }
        /* Check lightAdmin can or cannot save the created Project and Dataset as appropriate
         * (see definition of isExpectSuccessCreate above).*/
        try {
            sentProj = (Project) iUpdate.saveAndReturnObject(proj);
            sentDat = (Dataset) iUpdate.saveAndReturnObject(dat);
            Assert.assertTrue(isExpectSuccessCreate);
        } catch (ServerError se) {
            Assert.assertFalse(isExpectSuccessCreate);
        }
        /* Check the owner of the Project and Dataset (P/D) is the normalUser in all cases
         * the P/D were created, in all other cases P/D should be null.*/
        if (isExpectSuccessCreate) {
            assertOwnedBy(sentProj, normalUser);
            assertOwnedBy(sentDat, normalUser);
        } else {
            Assert.assertNull(sentProj);
            Assert.assertNull(sentDat);
        }
        /* Finish the test if lightAdmin is not sudoing.
         * Further tests of this test method deal with import and linking
         * for normalUser by lightAdmin who sudoes on behalf of normalUser.
         * Imports and linking for others while NOT using Sudo
         * are covered in other test methods in this class.*/
        if (!isSudoing) return;

        /* Check that after sudo, lightAdmin can import and write the originalFile
         * of the imported image on behalf of the normalUser into the created Dataset.*/
        List<IObject> originalFileAndImage = importImageWithOriginalFile(sentDat);
        OriginalFile originalFile = (OriginalFile) originalFileAndImage.get(0);
        Image image = (Image) originalFileAndImage.get(1);

        /* Check the canLink() boolean for both Dataset and Project.
         * lightAdmin is always sudoing in this part of the test.
         * Thus canLink() is always true, because lightAdmin is sudoed
         * on behalf of the owner of the objects (normalUser).*/
        Assert.assertTrue(getCurrentPermissions(sentProj).canLink());
        Assert.assertTrue(getCurrentPermissions(sentDat).canLink());
        /* Check that being sudoed, lightAdmin can link the Project and Dataset of normalUser.*/
        ProjectDatasetLink projectDatasetLink = linkParentToChild(sentProj, sentDat);

        /* Check the owner of the image, its originalFile, imageDatasetLink and projectDatasetLink
         * is normalUser and the image and its originalFile are in normalUser's group.*/
        final IObject imageDatasetLink = iQuery.findByQuery(
                "FROM DatasetImageLink WHERE child.id = :id",
                new ParametersI().addId(image.getId().getValue()));
        assertInGroup(originalFile, normalUser.groupId);
        assertOwnedBy(originalFile, normalUser);
        assertInGroup(image, normalUser.groupId);
        assertOwnedBy(image, normalUser);
        assertOwnedBy(imageDatasetLink, normalUser);
        assertOwnedBy(projectDatasetLink, normalUser);
    }

    /**
     * Light admin (lightAdmin) imports data for others (normalUser) without using Sudo.
     * lightAdmin first creates a Dataset and imports an Image into it in lightAdmin's group
     * (normalUser is not member of lightAdmin's group).
     * Then, lightAdmin tries to move the Dataset into normalUser's group.
     * Then, lightAdmin tries to chown the Dataset to normalUser.
     * For this test, combinations of <tt>Chown</tt>, <tt>Chgrp</tt>,
     * privileges of lightAdmin are explored.
     * @param permChgrp if to test a user who has the <tt>Chgrp</tt> privilege
     * @param permChown if to test a user who has the <tt>Chown</tt> privilege
     * @param groupPermissions to test the effect of group permission level
     * @throws Exception unexpected
     * @see <a href="https://downloads.openmicroscopy.org/resources/experimental/tests/graph-permissions/0.1/testImporterAsNoSudoChgrpChownWorkflow.pptx">graphical explanation</a>
     */
    @Test(dataProvider = "Chgrp and Chown privileges cases")
    public void testImporterAsNoSudoChgrpChownWorkflow(boolean permChgrp, boolean permChown,
            String groupPermissions) throws Exception {
        /* Importing into the group of the lightAdmin and
         * subsequent moving the data into the group of normalUser and chowning
         * them to the normalUser succeeds if Chgrp and Chown is possible,
         * which needs permChgrp, permChown, but not WriteFile and WriteOwned,*/
        boolean importYourGroupAndChgrpAndChownExpectSuccess = permChgrp && permChown;
        final EventContext normalUser = newUserAndGroup(groupPermissions);
        /* Set up the light admin's permissions for this test.*/
        List<String> permissions = new ArrayList<String>();
        if (permChown) permissions.add(AdminPrivilegeChown.value);
        if (permChgrp) permissions.add(AdminPrivilegeChgrp.value);
        final EventContext lightAdmin;
        lightAdmin = loginNewAdmin(true, permissions);
        /* lightAdmin creates a Dataset in lightAdmin's group and imports
         * an image into it.*/
        client.getImplicitContext().put("omero.group", Long.toString(lightAdmin.groupId));
        Dataset dat = mmFactory.simpleDataset();
        Dataset sentDat = (Dataset) iUpdate.saveAndReturnObject(dat);
        /* Import an Image into the created Dataset.*/
        List<IObject> originalFileAndImage = importImageWithOriginalFile(sentDat);
        OriginalFile originalFile = (OriginalFile) originalFileAndImage.get(0);
        Image image = (Image) originalFileAndImage.get(1);
        /* Check that originalFile and the image
         * corresponding to the originalFile are in the right group.*/
        assertOwnedBy(originalFile, lightAdmin);
        assertInGroup(originalFile, lightAdmin.groupId);
        assertOwnedBy(image, lightAdmin);
        assertInGroup(image, lightAdmin.groupId);

        /* In order to find the image in whatever group, get all groups context.*/
        mergeIntoContext(client.getImplicitContext(), ALL_GROUPS_CONTEXT);
        /* Check that the value of canChgrp on the dataset is true.
         * Note that although the move into normalUser's group might fail,
         * lightAdmin could be moving the dataset into some group where they are member,
         * and thus the canChgrp must be "true".*/
        Assert.assertTrue(getCurrentPermissions(sentDat).canChgrp());
        /* lightAdmin tries to move the dataset (and with it the linked image)
         * from lightAdmin's group to normalUser's group,
         * which should succeed in case the light admin has Chgrp permissions.*/
        doChange(client, factory, Requests.chgrp().target(sentDat).toGroup(normalUser.groupId).build(), permChgrp);
        /* Check the group of the moved objects.*/
        final DatasetImageLink datasetImageLink = (DatasetImageLink) iQuery.findByQuery(
                "FROM DatasetImageLink WHERE parent.id = :id",
                new ParametersI().addId(sentDat.getId()));
        if (permChgrp) {
            assertInGroup(originalFile, normalUser.groupId);
            assertInGroup(image, normalUser.groupId);
            assertInGroup(sentDat, normalUser.groupId);
            assertInGroup(datasetImageLink, normalUser.groupId);
        } else {
            assertInGroup(originalFile, lightAdmin.groupId);
            assertInGroup(image, lightAdmin.groupId);
            assertInGroup(sentDat, lightAdmin.groupId);
            assertInGroup(datasetImageLink, lightAdmin.groupId);
        }
        /* Check that the canChown boolean on Dataset is matching permChown boolean.*/
        Assert.assertEquals(getCurrentPermissions(sentDat).canChown(), permChown);
        /* lightAdmin tries to transfer the ownership of Dataset to normalUser.
         * Chowning the Dataset succeeds if lightAdmin has Chown privilege.
         * Successful chowning of the dataset transfers the ownership of the linked image
         * and the link too.*/
        doChange(client, factory, Requests.chown().target(sentDat).toUser(normalUser.userId).build(), permChown);
        /* Boolean importYourGroupAndChgrpAndChownExpectSuccess
         * captures permChown and permChgrp. Check the objects ownership and groups.*/
        if (importYourGroupAndChgrpAndChownExpectSuccess) {
            /* First case: The whole "import for others" workflow succeeds.
             * Image, Dataset and link are in normalUser's group and belong to normalUser.*/
            assertOwnedBy(originalFile, normalUser);
            assertInGroup(originalFile, normalUser.groupId);
            assertOwnedBy(image, normalUser);
            assertInGroup(image, normalUser.groupId);
            assertOwnedBy(sentDat, normalUser);
            assertInGroup(sentDat, normalUser.groupId);
            assertOwnedBy(datasetImageLink, normalUser);
            assertInGroup(datasetImageLink, normalUser.groupId);
        } else if (permChown) {
            /* Second case: Chown succeeds, but Chgrp fails.
             * Image, Dataset and link belong to the normalUser, but are in lightAdmin's group */
            assertOwnedBy(originalFile, normalUser);
            assertInGroup(originalFile, lightAdmin.groupId);
            assertOwnedBy(image, normalUser);
            assertInGroup(image, lightAdmin.groupId);
            assertOwnedBy(sentDat, normalUser);
            assertInGroup(sentDat, lightAdmin.groupId);
            assertOwnedBy(datasetImageLink, normalUser);
            assertInGroup(datasetImageLink, lightAdmin.groupId);
        } else if (permChgrp) {
            /* Third case: Chgrp succeeds, but Chown fails.
             * Image, Dataset and link are in normalUser's group but belong to lightAdmin.*/
            assertOwnedBy(originalFile, lightAdmin);
            assertInGroup(originalFile, normalUser.groupId);
            assertOwnedBy(image, lightAdmin);
            assertInGroup(image, normalUser.groupId);
            assertOwnedBy(sentDat, lightAdmin);
            assertInGroup(sentDat, normalUser.groupId);
            assertOwnedBy(datasetImageLink, lightAdmin);
            assertInGroup(datasetImageLink, normalUser.groupId);
        } else {
            /* Fourth case: Ghgrp and Chown both fail.
             * Image, Dataset and link are in lightAdmin's group and belong to lightAdmin.*/
            assertOwnedBy(originalFile, lightAdmin);
            assertInGroup(originalFile, lightAdmin.groupId);
            assertOwnedBy(image, lightAdmin);
            assertInGroup(image, lightAdmin.groupId);
            assertOwnedBy(sentDat, lightAdmin);
            assertInGroup(sentDat, lightAdmin.groupId);
            assertOwnedBy(datasetImageLink, lightAdmin);
            assertInGroup(datasetImageLink, lightAdmin.groupId);
        }
    }
}
