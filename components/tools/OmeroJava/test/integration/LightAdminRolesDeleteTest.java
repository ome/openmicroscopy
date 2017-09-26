/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2017 University of Dundee & Open Microscopy Environment.
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
import java.util.Collections;
import java.util.List;

import omero.gateway.util.Requests;
import omero.model.Dataset;
import omero.model.ExperimenterGroup;
import omero.model.ExperimenterGroupI;
import omero.model.Experimenter;
import omero.model.IObject;
import omero.model.Image;
import omero.model.OriginalFile;
import omero.model.Project;
import omero.model.ProjectDatasetLink;
import omero.model.enums.AdminPrivilegeDeleteOwned;
import omero.model.enums.AdminPrivilegeSudo;
import omero.sys.EventContext;
import omero.sys.ParametersI;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableList;


/**
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since 5.4
 */
public class LightAdminRolesDeleteTest extends RolesTests {

   
    /**
     * Test whether a light admin (lightAdmin) can delete image, Project and Dataset
     * and their respective links belonging to another
     * user (normalUser).
     * Note that for this test, lightAdmin is member of normalUser's group.
     * lightAdmin's privileges regarding deletion of others' data are not elevated by
     * membership in the group over the privileges of normal member of group (otherUser).
     * @param isAdmin if to test a success of workflows when light admin
     * @param permDeleteOwned if to test a user who has the <tt>DeleteOwned</tt> privilege
     * @param groupPermissions to test the effect of group permission level
     * @throws Exception unexpected
     */
    @Test(dataProvider = "isAdmin and Delete cases")
    public void testDeleteGroupMemberNoSudo(boolean isAdmin, boolean permDeleteOwned,
            String groupPermissions) throws Exception {
        /* Only DeleteOwned permission is needed for deletion of links, Dataset
         * and image (with original file) when isAdmin. When not isAdmin, only in
         * read-write group deletion of others data is possible.*/
        boolean deletePassing = (permDeleteOwned && isAdmin) || groupPermissions.equals("rwrw--");
        final IObject[] normalUser = users.get(groupPermissions);
        final long user_id = normalUser[1].getId().getValue();
        final long user_group_id = normalUser[0].getId().getValue();

        final IObject[] otherUser = others.get(groupPermissions);
        final long other_id = otherUser[1].getId().getValue();
        ExperimenterGroup normalUsergroup = new ExperimenterGroupI(user_group_id, false);
        /* Set up the light admin's permissions for this test.*/
        List<String> permissions = new ArrayList<String>();
        if (permDeleteOwned) permissions.add(AdminPrivilegeDeleteOwned.value);
        final EventContext lightAdmin = loginNewAdmin(true, permissions);
        /* root adds lightAdmin to normalUser's group.*/
        logRootIntoGroup(user_group_id);
        normalUsergroup = addUsers(normalUsergroup, ImmutableList.of(lightAdmin.userId, other_id), false);
        /* normalUser creates a Dataset and Project.*/
        loginUser(((Experimenter) normalUser[1]).getOmeName().getValue());
        Project sentProj = (Project) iUpdate.saveAndReturnObject(mmFactory.simpleProject());
        Dataset sentDat = (Dataset) iUpdate.saveAndReturnObject(mmFactory.simpleDataset());
        /* normalUser imports an image
         * and targets it into the created Dataset.*/
        List<IObject> originalFileAndImage = importImageWithOriginalFile(sentDat);
        OriginalFile originalFile = (OriginalFile) originalFileAndImage.get(0);
        Image image = (Image) originalFileAndImage.get(1);
        assertOwnedBy(image, user_id);
        /* normalUser links the Project and the Dataset.*/
        ProjectDatasetLink projectDatasetLink = linkParentToChild(sentProj, sentDat);
        IObject datasetImageLink = iQuery.findByQuery(
                "FROM DatasetImageLink WHERE child.id = :id",
                new ParametersI().addId(image.getId()));
        /* Post-import workflows are done either by lightAdmin or by otherUser.*/
        if (isAdmin) {
            loginUser(lightAdmin);
        } else {
            loginUser(((Experimenter) otherUser[1]).getOmeName().getValue());
        }
        client.getImplicitContext().put(omero.constants.GROUP.value, Long.toString(user_group_id));
        /* Check that lightAdmin or otherUser can delete the objects
         * of normalUser only if lightAdmin has sufficient permissions or it is read-write group.
         * Note that deletion of the Project
         * would delete the whole hierarchy, which was successfully tested
         * during writing of this test. The order of the below delete() commands
         * is intentional, as the ability to delete the links and Project/Dataset/Image separately is
         * tested in this way.
         * Also check that the canDelete boolean on the object retrieved by the lightAdmin
         * or otherUser matches the deletePassing boolean.*/
        Assert.assertEquals(getCurrentPermissions(datasetImageLink).canDelete(), deletePassing);
        doChange(client, factory, Requests.delete().target(datasetImageLink).build(), deletePassing);
        Assert.assertEquals(getCurrentPermissions(projectDatasetLink).canDelete(), deletePassing);
        doChange(client, factory, Requests.delete().target(projectDatasetLink).build(), deletePassing);
        Assert.assertEquals(getCurrentPermissions(image).canDelete(), deletePassing);
        doChange(client, factory, Requests.delete().target(image).build(), deletePassing);
        Assert.assertEquals(getCurrentPermissions(sentDat).canDelete(), deletePassing);
        doChange(client, factory, Requests.delete().target(sentDat).build(), deletePassing);
        Assert.assertEquals(getCurrentPermissions(sentProj).canDelete(), deletePassing);
        doChange(client, factory, Requests.delete().target(sentProj).build(), deletePassing);

        /* Check the existence/non-existence of the objects as appropriate.*/
        logRootIntoGroup(user_group_id);
        if (deletePassing) {
            assertDoesNotExist(originalFile);
            assertDoesNotExist(image);
            assertDoesNotExist(sentDat);
            assertDoesNotExist(sentProj);
            assertDoesNotExist(datasetImageLink);
            assertDoesNotExist(projectDatasetLink);
        } else {
            assertExists(originalFile);
            assertExists(image);
            assertExists(sentDat);
            assertExists(sentProj);
            assertExists(datasetImageLink);
            assertExists(projectDatasetLink);
        }
    }

    /**
     * light admin (lightAdmin) being also a group owner of one group
     * (ownedGroup) tries to delete Dataset of other user (normalUser).
     * lightAdmin also tries to delete data of yet one other user (otherUser)
     * in a group which they do not own (notOwnedGroup).
     * lightAdmin succeeds only in the ownGroup, in the notOwnedGroup they
     * succeed only with DeleteOwned privilege.
     * @param isPrivileged if to test a user who has the <tt>DeleteOwned</tt> privilege
     * @param groupPermissions to test the effect of group permission level
     * @throws Exception unexpected
    */
    @Test(dataProvider = "isPrivileged cases")
    public void testDeleteGroupOwner(boolean isPrivileged,
            String groupPermissions) throws Exception {
        /* DeleteOwned privilege is necessary for deletion in group which is
         * not owned. For deletion in group which is owned, no privilege is necessary.*/
        boolean deletePassingNotOwnedGroup = isPrivileged;
        boolean deletePassingOwnedGroup = true;
        final IObject[] normalUser = users.get(groupPermissions);
        final long user_group_id = normalUser[0].getId().getValue();

        final IObject[] otherUser = others.get(groupPermissions);
        final long other_group_id = otherUser[0].getId().getValue();
        /* Set up the light admin's permissions for this test */
        List<String> permissions = new ArrayList<String>();
        permissions.add(AdminPrivilegeSudo.value);
        if (isPrivileged) permissions.add(AdminPrivilegeDeleteOwned.value);
        final EventContext lightAdmin = loginNewAdmin(true, permissions);
        ExperimenterGroup ownedGroup = new ExperimenterGroupI(user_group_id, false);
        ExperimenterGroup notOwnedGroup = new ExperimenterGroupI(other_group_id, false);
        /* root adds lightAdmin to normalUser's group as owner.*/
        logRootIntoGroup(user_group_id);
        ownedGroup = addUsers(ownedGroup, Collections.singletonList(lightAdmin.userId), true);
        /* normalUser creates a Dataset in ownGroup.*/
        loginUser(((Experimenter) normalUser[1]).getOmeName().getValue());
        final Dataset sentDataset = (Dataset) iUpdate.saveAndReturnObject(mmFactory.simpleDataset());
        /* otherUser creates a Dataset in notOwnGroup.*/
        loginUser(((Experimenter) otherUser[1]).getOmeName().getValue());
        final Dataset sentOtherDataset = (Dataset) iUpdate.saveAndReturnObject(mmFactory.simpleDataset());
        /* Check that the Datasets are in their groups as expected.*/
        assertInGroup(sentDataset, ownedGroup);
        assertInGroup(sentOtherDataset, notOwnedGroup);
        /* Check that lightAdmin can delete the Datasets only when permissions allow that.
         * Also check that the canDelete boolean
         * on the object retrieved by the lightAdmin matches the deletePassing
         * boolean.*/
        loginUser(lightAdmin);
        client.getImplicitContext().put(omero.constants.GROUP.value, Long.toString(user_group_id));
        Assert.assertEquals(getCurrentPermissions(sentDataset).canDelete(), deletePassingOwnedGroup);
        doChange(client, factory, Requests.delete().target(sentDataset).build(), deletePassingOwnedGroup);
        client.getImplicitContext().put(omero.constants.GROUP.value, Long.toString(other_group_id));
        Assert.assertEquals(getCurrentPermissions(sentOtherDataset).canDelete(), deletePassingNotOwnedGroup);
        doChange(client, factory, Requests.delete().target(sentOtherDataset).build(), deletePassingNotOwnedGroup);
        /* Check the existence/non-existence of the objects as appropriate.*/
        assertDoesNotExist(sentDataset);
        if (deletePassingNotOwnedGroup) {
            assertDoesNotExist(sentOtherDataset);
        } else {
            assertExists(sentOtherDataset);
        }
    }


    /**
     * Test whether a light admin can delete image, Project and Dataset
     * and their respective links belonging to another
     * user. Behaviors of the system are explored when lightAdmin
     * is and is not using <tt>Sudo</tt> privilege
     * for this action.
     * @param isSudoing if to test a success of workflows where Sudoed in
     * @param permDeleteOwned if to test a user who has the <tt>DeleteOwned</tt> privilege
     * @param groupPermissions to test the effect of group permission level
     * @throws Exception unexpected
     * @see <a href="https://downloads.openmicroscopy.org/resources/experimental/tests/graph-permissions/0.1/testDelete.pptx">graphical explanation</a>
     */
    @Test(dataProvider = "isSudoing and Delete privileges cases")
    public void testDelete(boolean isSudoing, boolean permDeleteOwned,
            String groupPermissions) throws Exception {
        /* Only DeleteOwned permission is needed for deletion of links, Dataset
         * and image (with original file) when not sudoing. When sudoing, no other
         * permission is needed.*/
        boolean deletePassing = permDeleteOwned || isSudoing;
        final IObject[] normalUser = users.get(groupPermissions);
        final long user_id = normalUser[1].getId().getValue();
        final long user_group_id = normalUser[0].getId().getValue();

        /* Set up the light admin's permissions for this test */
        List<String> permissions = new ArrayList<String>();
        permissions.add(AdminPrivilegeSudo.value);
        if (permDeleteOwned) permissions.add(AdminPrivilegeDeleteOwned.value);
        final EventContext lightAdmin;
        lightAdmin = loginNewAdmin(true, permissions);
        sudo((Experimenter) normalUser[1]);
        /* Create a Dataset and Project being sudoed as normalUser.*/
        client.getImplicitContext().put(omero.constants.GROUP.value, Long.toString(user_group_id));
        Project sentProj = (Project) iUpdate.saveAndReturnObject(mmFactory.simpleProject());
        Dataset sentDat = (Dataset) iUpdate.saveAndReturnObject(mmFactory.simpleDataset());
        /* Import an image for the normalUser into the normalUser's default group
         * and target it into the created Dataset.*/
        List<IObject> originalFileAndImage = importImageWithOriginalFile(sentDat);
        OriginalFile originalFile = (OriginalFile) originalFileAndImage.get(0);
        Image image = (Image) originalFileAndImage.get(1);
        assertOwnedBy(image, user_id);
        /* Link the Project and the Dataset.*/
        ProjectDatasetLink projectDatasetLink = linkParentToChild(sentProj, sentDat);
        IObject datasetImageLink = iQuery.findByQuery(
                "FROM DatasetImageLink WHERE child.id = :id",
                new ParametersI().addId(image.getId()));
        /* Take care of post-import workflows which do not use sudo.*/
        if (!isSudoing) {
            loginUser(lightAdmin);
            client.getImplicitContext().put(omero.constants.GROUP.value, Long.toString(user_group_id));
        }
        /* Check that lightAdmin can delete the objects
         * created on behalf of normalUser only if lightAdmin has sufficient permissions.
         * Note that deletion of the Project
         * would delete the whole hierarchy, which was successfully tested
         * during writing of this test. The order of the below delete() commands
         * is intentional, as the ability to delete the links and Project/Dataset/Image separately is
         * tested in this way.
         * Also check that the canDelete boolean on the object retrieved
         * by the lightAdmin matches the deletePassing boolean.*/
        Assert.assertEquals(getCurrentPermissions(datasetImageLink).canDelete(), deletePassing);
        doChange(client, factory, Requests.delete().target(datasetImageLink).build(), deletePassing);
        Assert.assertEquals(getCurrentPermissions(projectDatasetLink).canDelete(), deletePassing);
        doChange(client, factory, Requests.delete().target(projectDatasetLink).build(), deletePassing);
        Assert.assertEquals(getCurrentPermissions(image).canDelete(), deletePassing);
        doChange(client, factory, Requests.delete().target(image).build(), deletePassing);
        Assert.assertEquals(getCurrentPermissions(sentDat).canDelete(), deletePassing);
        doChange(client, factory, Requests.delete().target(sentDat).build(), deletePassing);
        Assert.assertEquals(getCurrentPermissions(sentProj).canDelete(), deletePassing);
        doChange(client, factory, Requests.delete().target(sentProj).build(), deletePassing);

        /* Check the existence/non-existence of the objects as appropriate.*/
        if (deletePassing) {
            assertDoesNotExist(originalFile);
            assertDoesNotExist(image);
            assertDoesNotExist(sentDat);
            assertDoesNotExist(sentProj);
            assertDoesNotExist(datasetImageLink);
            assertDoesNotExist(projectDatasetLink);
        } else {
            assertExists(originalFile);
            assertExists(image);
            assertExists(sentDat);
            assertExists(sentProj);
            assertExists(datasetImageLink);
            assertExists(projectDatasetLink);
        }
    }

}
