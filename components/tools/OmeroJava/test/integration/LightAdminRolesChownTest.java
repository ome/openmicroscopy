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
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import omero.api.IAdminPrx;
import omero.gateway.util.Requests;
import omero.model.Dataset;
import omero.model.DatasetImageLink;
import omero.model.Experimenter;
import omero.model.ExperimenterGroup;
import omero.model.ExperimenterI;
import omero.model.IObject;
import omero.model.Image;
import omero.model.OriginalFile;
import omero.model.Project;
import omero.model.ProjectDatasetLink;
import omero.model.enums.AdminPrivilegeChown;
import omero.model.enums.AdminPrivilegeSudo;
import omero.sys.EventContext;

import org.testng.Assert;
import org.testng.annotations.Test;


/**
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since 5.4
 */
public class LightAdminRolesChownTest extends RolesTests {

    /**
     * Test that light admin can, having the <tt>Chown</tt> privilege,
     * transfer the data between two users (normalUser and anotherUser).
     * Test also that light admin, if sudoed, cannot transfer ownership,
     * because light admin sudoes as a non-admin non-group-owner user.
     * In case of private group the transfer of an Image severs the link between the Dataset and Image.
     * For this unlinking, only the Chown permissions are sufficient, no other permissions are necessary.
     * <tt>Chown</tt> privilege is sufficient also
     * to transfer ownership of annotations (tag and file attachment are tested here),
     * but just in case of private and read-only groups, which is in line with the
     * general behavior of the <tt>Chown</tt> command.
     * @param isSudoing if to test a success of workflows where Sudoed in
     * @param permChown if to test a user who has the <tt>Chown</tt> privilege
     * @param groupPermissions to test the effect of group permission level
     * @throws Exception unexpected
     * @see <a href="https://downloads.openmicroscopy.org/resources/experimental/tests/graph-permissions/0.1/testChown.pptx">graphical explanation</a>
     */
    @Test(dataProvider = "isSudoing and Chown privileges cases")
    public void testChown(boolean isSudoing, boolean permChown, String groupPermissions)
            throws Exception {
        /* Define the conditions for the chown of the image is passing.*/
        final boolean chownImagePassing = permChown && !isSudoing;
        /* Chown of the annotations on the image is passing when
         * chownImagePassing is true in higher permissions groups (read-annotate and read-write)
         * only.*/
        final boolean annotationsChownExpectSuccess = chownImagePassing &&
                (groupPermissions.equals("rw----") || groupPermissions.equals("rwr---"));

        final IObject[] normalUser = users.get(groupPermissions);
        final long user_id = normalUser[1].getId().getValue();
        final long user_group_id = normalUser[0].getId().getValue();

        final IObject[] otherUser = others.get(groupPermissions);
        final long other_id = otherUser[1].getId().getValue();
        /* Create a Dataset as the normalUser and import into it */
        loginUser(((Experimenter) normalUser[1]).getOmeName().getValue());
        Dataset dat = mmFactory.simpleDataset();
        Dataset sentDat = (Dataset) iUpdate.saveAndReturnObject(dat);
        List<IObject> originalFileAndImage = importImageWithOriginalFile(sentDat);
        OriginalFile originalFile = (OriginalFile) originalFileAndImage.get(0);
        Image image = (Image) originalFileAndImage.get(1);

        /* Annotate the imported image with Tag and file attachment.*/
        List<IObject> annotOriginalFileAnnotationTagAndLinks = annotateImageWithTagAndFile(image);

        /* Set up the basic permissions for this test.*/
        List<String> permissions = new ArrayList<String>();
        permissions.add(AdminPrivilegeSudo.value);
        if (permChown) permissions.add(AdminPrivilegeChown.value);
        final EventContext lightAdmin;
        lightAdmin = loginNewAdmin(true, permissions);
        sudo((Experimenter) normalUser[1]);

        /* Take care of workflows which do not use sudo.*/
        if (!isSudoing) {
            loginUser(lightAdmin);
        }
        /* Check that the value of canChown boolean matches chownPassingWhenNotSudoing
         * boolean in each case.*/
        Assert.assertEquals(getCurrentPermissions(image).canChown(), chownImagePassing);
        /* Get into correct group context and check all cases.*/
        client.getImplicitContext().put(omero.constants.GROUP.value, Long.toString(user_group_id));
        /* lightAdmin tries to chown the image.*/
        doChange(client, factory, Requests.chown().target(image).toUser(other_id).build(), chownImagePassing);
        /* ChecK the results of the chown when lightAdmin is sudoed,
         * which should fail in any case.*/
        if (isSudoing) {
            assertOwnedBy(image, user_id);
            assertOwnedBy(originalFile, user_id);
            assertOwnedBy(annotOriginalFileAnnotationTagAndLinks, user_id);
        /* Check the chown was successful for both the image and the annotations
         * when the permissions for chowning both
         * the image as well as the annotations on it are sufficient.*/
        } else if (chownImagePassing && annotationsChownExpectSuccess) {
            assertOwnedBy(image, other_id);
            assertOwnedBy(originalFile, other_id);
            /* Annotations will be chowned because
             * groupPermissions are private or read-only (captured in boolean
             * annotationsChownExpectSuccess).*/
            assertOwnedBy(annotOriginalFileAnnotationTagAndLinks, other_id);
        /* Check the chown was successful for the image but not the annotations
         * in case the annotationsChownExpectSuccess is false, i.e. in read-only and private group.*/
        } else if (chownImagePassing && !annotationsChownExpectSuccess){
            assertOwnedBy(image, other_id);
            assertOwnedBy(originalFile, other_id);
            assertOwnedBy(annotOriginalFileAnnotationTagAndLinks, user_id);
        } else {
        /* In the remaining case, the chown will fail, as the chownPassingWhenNotSudoing
         * is false because permChown was not given. All objects belong to normalUser.*/
            assertOwnedBy(image, user_id);
            assertOwnedBy(originalFile, user_id);
            assertOwnedBy(annotOriginalFileAnnotationTagAndLinks, user_id);
        }
        /* In any case, the image must be in the right group.*/
        assertInGroup(image, user_group_id);
    }


    /**
     * Light admin (lightAdmin) tries to transfer the ownership of all the data of a user (normalUser)
     * to another user. The data are in 2 groups, of which the original data owner (normalUser)
     * is member, the recipient of the data is member of just one of the groups. Chown privilege
     * is sufficient for lightAdmin to perform the workflow.
     * @param isPrivileged if to test a user who has the <tt>Chown</tt> privilege
     * @param groupPermissions to test the effect of group permission level
     * @throws Exception unexpected
     * @see <a href="https://downloads.openmicroscopy.org/resources/experimental/tests/graph-permissions/0.1/testChownAllBelongingToUserLightAdmin.pptx">graphical explanation</a>
     */
    @Test(dataProvider = "isPrivileged cases")
    public void testChownAllBelongingToUser(boolean isPrivileged, String groupPermissions) throws Exception {
        /* Chown privilege is sufficient for the workflow.*/
        final boolean chownPassing = isPrivileged;
        final IObject[] normalUser = users.get(groupPermissions);
        final long user_id = normalUser[1].getId().getValue();
        final long user_group_id = normalUser[0].getId().getValue();
        ExperimenterGroup anotherGroup = newGroupAddUser(groupPermissions,  user_id, false);
        //Add a new user to the group
        IAdminPrx rootAdmin = root.getSession().getAdminService();
        String uuid = UUID.randomUUID().toString();
        Experimenter recipient = new ExperimenterI();
        recipient.setOmeName(omero.rtypes.rstring(uuid));
        recipient.setFirstName(omero.rtypes.rstring("integration"));
        recipient.setLastName(omero.rtypes.rstring("tester"));
        recipient.setLdap(omero.rtypes.rbool(false));
        long recipient_id = newUserInGroupWithPassword(recipient, anotherGroup, uuid);
        recipient = rootAdmin.getExperimenter(recipient_id);
        rootAdmin.addGroups(recipient, Arrays.asList(anotherGroup));

        /* normalUser creates two sets of Project/Dataset/Image hierarchy in their default group.*/
        loginUser(((Experimenter) normalUser[1]).getOmeName().getValue());
        client.getImplicitContext().put(omero.constants.GROUP.value, Long.toString(user_group_id));
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
        DatasetImageLink linkOfDatasetImage1 = linkParentToChild(sentDat1, sentImage1);
        DatasetImageLink linkOfDatasetImage2 = linkParentToChild(sentDat2, sentImage2);
        ProjectDatasetLink linkOfProjectDataset1 = linkParentToChild(sentProj1, sentDat1);
        ProjectDatasetLink linkOfProjectDataset2 = linkParentToChild(sentProj2, sentDat2);

        /* normalUser creates two sets of Project/Dataset?Image hierarchy in the other group (anotherGroup).*/
        client.getImplicitContext().put(omero.constants.GROUP.value, Long.toString(anotherGroup.getId().getValue()));
        Image image1AnotherGroup = mmFactory.createImage();
        Image image2AnotherGroup = mmFactory.createImage();
        Image sentImage1AnootherGroup = (Image) iUpdate.saveAndReturnObject(image1AnotherGroup);
        Image sentImage2AnotherGroup = (Image) iUpdate.saveAndReturnObject(image2AnotherGroup);
        Dataset dat1AnotherGroup = mmFactory.simpleDataset();
        Dataset dat2AnotherGroup = mmFactory.simpleDataset();
        Dataset sentDat1AnotherGroup = (Dataset) iUpdate.saveAndReturnObject(dat1AnotherGroup);
        Dataset sentDat2AnotherGroup = (Dataset) iUpdate.saveAndReturnObject(dat2AnotherGroup);
        Project proj1AnotherGroup = mmFactory.simpleProject();
        Project proj2AnotherGroup = mmFactory.simpleProject();
        Project sentProj1AnootherGroup = (Project) iUpdate.saveAndReturnObject(proj1AnotherGroup);
        Project sentProj2AnotherGroup = (Project) iUpdate.saveAndReturnObject(proj2AnotherGroup);
        DatasetImageLink linkOfDatasetImage1AnotherGroup = linkParentToChild(sentDat1AnotherGroup, sentImage1AnootherGroup);
        DatasetImageLink linkOfDatasetImage2AnotherGroup = linkParentToChild(sentDat2AnotherGroup, sentImage2AnotherGroup);
        ProjectDatasetLink linkOfProjectDataset1AnotherGroup = linkParentToChild(sentProj1AnootherGroup, sentDat1AnotherGroup);
        ProjectDatasetLink linkOfProjectDataset2AnotherGroup = linkParentToChild(sentProj2AnotherGroup, sentDat2AnotherGroup);
        /* lightAdmin tries to transfers all normalUser's data to recipient.*/
        /* Set up the light admin's permissions for this test.*/
        List<String> permissions = new ArrayList<String>();
        if (isPrivileged) permissions.add(AdminPrivilegeChown.value);
        loginNewAdmin(true, permissions);
        /* In order to be able to operate in both groups, get all groups context.*/
        mergeIntoContext(client.getImplicitContext(), ALL_GROUPS_CONTEXT);
        /* Check on one selected object only (sentProj1AnotherGroup) the value
         * of canChown. The value must match the chownPassing boolean.*/
        Assert.assertEquals(getCurrentPermissions(sentProj1AnootherGroup).canChown(), chownPassing);
        /* Check that transfer proceeds only if chownPassing boolean is true.*/
        doChange(client, factory, Requests.chown().targetUsers(user_id).toUser(recipient_id).build(), chownPassing);
        if (!chownPassing) {
            /* Finish the test if no transfer of data could proceed.*/
            return;
        }
        /* Check the transfer of all the data in normalUser's group was successful,
         * first checking ownership of the first hierarchy set.*/
        assertOwnedBy(sentProj1, recipient_id);
        assertOwnedBy(sentDat1, recipient_id);
        assertOwnedBy(sentImage1, recipient_id);
        assertOwnedBy(linkOfDatasetImage1, recipient_id);
        assertOwnedBy(linkOfProjectDataset1, recipient_id);
        /* Check ownership of the second hierarchy set.*/
        assertOwnedBy(sentProj2, recipient_id);
        assertOwnedBy(sentDat2, recipient_id);
        assertOwnedBy(sentImage2, recipient_id);
        assertOwnedBy(linkOfDatasetImage2, recipient_id);
        assertOwnedBy(linkOfProjectDataset2, recipient_id);
        /* Check ownership of the objects in anotherGroup,
         * first checking ownership of the first hierarchy.*/
        assertOwnedBy(sentProj1AnootherGroup, recipient_id);
        assertOwnedBy(sentDat1AnotherGroup, recipient_id);
        assertOwnedBy(sentImage1AnootherGroup, recipient_id);
        assertOwnedBy(linkOfDatasetImage1AnotherGroup, recipient_id);
        assertOwnedBy(linkOfProjectDataset1AnotherGroup, recipient_id);
        /* Check ownership of the second hierarchy set in anotherGroup.*/
        assertOwnedBy(sentProj2AnotherGroup, recipient_id);
        assertOwnedBy(sentDat2AnotherGroup, recipient_id);
        assertOwnedBy(sentImage1AnootherGroup, recipient_id);
        assertOwnedBy(linkOfDatasetImage2AnotherGroup, recipient_id);
        assertOwnedBy(linkOfProjectDataset2AnotherGroup, recipient_id);
    }

}
