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
import java.util.List;

import omero.gateway.util.Requests;
import omero.model.Dataset;
import omero.model.Experimenter;
import omero.model.IObject;
import omero.model.Image;
import omero.model.OriginalFile;
import omero.model.enums.AdminPrivilegeChgrp;
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
public class LightAdminRolesChgrpTest extends RolesTests {


    /**
     * Test that light admin can
     * chgrp on behalf of another user in cases where the user (owner of the data)
     * is a member of both groups. The chgrp action succeeds if <tt>Sudo</tt> privilege
     * is used. If lightAdmin does not sudo, then the <tt>Chgrp</tt> privilege is necessary.
     * Also tests the ability of the <tt>Chgrp</tt> privilege and chgrp command
     * to sever necessary links for performing the chgrp. This is achieved by
     * having the image which is getting moved into a different group in a dataset
     * in the original group (the chgrp has to sever the DatasetImageLink to perform
     * the move (chgrp)). <tt>Chgrp</tt> privilege is sufficient also
     * to move annotations on the moved objects (tag and file attachment are tested here).
     * @param isSudoing if to test a success of workflows where Sudoed in
     * @param permChgrp if to test a user who has the <tt>Chgrp</tt> privilege
     * @param groupPermissions to test the effect of group permission level
     * @throws Exception unexpected
     * @see <a href="https://downloads.openmicroscopy.org/resources/experimental/tests/graph-permissions/0.1/testChgrp.pptx">graphical explanation</a>
     */
    @Test(dataProvider = "isSudoing and Chgrp privileges cases")
    public void testChgrp(boolean isSudoing, boolean permChgrp, String groupPermissions)
            throws Exception {
        /* Set up a user and three groups, the user being a member of
         * two of the groups.*/
        final IObject[] normalUser = users.get(groupPermissions);
        final long user_id = normalUser[1].getId().getValue();
        final long user_group_id = normalUser[0].getId().getValue();
        /* Group where the user is a member.*/
        final long normalUsersOtherGroupId = newGroupAddUser(groupPermissions, user_id).getId().getValue();
        /* If normalUser (data owner) is member of target group,
         * Chgrp action passes when lightAdmin is
         * Sudoed as the normalUser (data owner) or when Chgrp permission is given to lightAdmin.
         * A successful chgrp action will also move all annotations on the moved image,
         * which are unique on the image.*/
        boolean isExpectSuccessInMemberGroup = permChgrp || isSudoing;
        /* Create a Dataset as normalUser and import into it.*/
        loginUser(((Experimenter) normalUser[1]).getOmeName().getValue());
        Dataset dat = mmFactory.simpleDataset();
        Dataset sentDat = (Dataset) iUpdate.saveAndReturnObject(dat);
        List<IObject> originalFileAndImage = importImageWithOriginalFile(sentDat);
        OriginalFile originalFile = (OriginalFile) originalFileAndImage.get(0);
        Image image = (Image) originalFileAndImage.get(1);

        /* Annotate the imported image with Tag and file attachment.*/
        List<IObject> annotOriginalFileAnnotationTagAndLinks = annotateImageWithTagAndFile(image);

        /* Set up the light admin's permissions for this test.*/
        List<String> permissions = new ArrayList<String>();
        permissions.add(AdminPrivilegeSudo.value);
        if (permChgrp) permissions.add(AdminPrivilegeChgrp.value);
        final EventContext lightAdmin;
        lightAdmin = loginNewAdmin(true, permissions);
        sudo((Experimenter) normalUser[1]);

        /* Take care of workflows which do not use sudo.*/
        if (!isSudoing) {
            loginUser(lightAdmin);
        }
        /* In order to find the image in whatever group, get to all groups context.*/
        mergeIntoContext(client.getImplicitContext(), ALL_GROUPS_CONTEXT);
        /* lightAdmin tries to move the image into another group of the normalUser
         * which should succeed if sudoing and also in case
         * the light admin has Chgrp permissions
         * (i.e. isExpectSuccessInMemberGroup is true). Also check that
         * the canChgrp boolean matches the isExpectSuccessInMemberGroup boolean value */
        Assert.assertEquals(getCurrentPermissions(image).canChgrp(), isExpectSuccessInMemberGroup);
        doChange(client, factory, Requests.chgrp().target(image).toGroup(normalUsersOtherGroupId).build(), isExpectSuccessInMemberGroup);
        if (isExpectSuccessInMemberGroup) {
            assertInGroup(image, normalUsersOtherGroupId);
            assertInGroup(originalFile, normalUsersOtherGroupId);
            /* Annotations on the image changed the group with the image.*/
            assertInGroup(annotOriginalFileAnnotationTagAndLinks, normalUsersOtherGroupId);
        } else {
            assertInGroup(image, user_group_id);
            assertInGroup(originalFile, user_group_id);
            /* The annotations were not moved.*/
            assertInGroup(annotOriginalFileAnnotationTagAndLinks, user_group_id);
        }
        /* In any case, the image should still belong to normalUser.*/
        assertOwnedBy(image, user_id);
    }

    /**
     * Tests that light admin can, having the <tt>Chgrp</tt>
     * privilege move another user's data into another group where the
     * owner of the data is not member.
     * <tt>Sudo</tt> privilege and being sudoed should not be sufficient,
     * and also, when being sudoed, the cancellation of the <tt>Chgrp</tt> privilege
     * by being sudoed as normalUser causes that the move of the image fails.
     * Also tests the ability of the <tt>Chgrp</tt> privilege and chgrp command
     * to sever necessary links for performing the chgrp. This is achieved by
     * having the image which is getting moved into a different group in a dataset
     * in the original group (the chgrp has to sever the DatasetImageLink to perform
     * the move (chgrp)). <tt>Chgrp</tt> privilege is sufficient also
     * to move annotations (tag and file attachment are tested here).
     * @param isSudoing if to test a success of workflows where Sudoed in
     * @param permChgrp if to test a user who has the <tt>Chgrp</tt> privilege
     * @param groupPermissions to test the effect of group permission level
     * @throws Exception unexpected
     * @see <a href="https://downloads.openmicroscopy.org/resources/experimental/tests/graph-permissions/0.1/testChgrpNonMember.pptx">graphical explanation</a>
     */
    @Test(dataProvider = "isSudoing and Chgrp privileges cases")
    public void testChgrpNonMember(boolean isSudoing, boolean permChgrp, String groupPermissions)
            throws Exception {
        /* Set up a user (normalUser) and two groups, the normalUser being a member of
         * only one of the groups.*/
        final IObject[] normalUser = users.get(groupPermissions);
        final long user_id = normalUser[1].getId().getValue();
        final long user_group_id = normalUser[0].getId().getValue();
        /* group where the normalUser is not member */
        final long anotherGroupId = newUserAndGroup(groupPermissions).groupId;
        /* When normalUser (data owner) is not member of the target group,
         * Chgrp action passes only when lightAdmin has Chgrp permission
         * and lightAdmin is not Sudoing. This permission situation should be also valid
         * for all annotations on the image which are unique on the image (not used
         * anywhere else).*/
        boolean chgrpNonMemberExpectSuccess = !isSudoing && permChgrp;
        /* Define cases where canChgrp on the image is expected to be true.
         * As the canChgrp boolean cannot "know" in advance to which group the
         * move is intended, it must show "true" in every case in which SOME
         * chgrp might be successful.*/
        final boolean canChgrpExpectedTrue = permChgrp || isSudoing;
        /* Create a Dataset as the normalUser and import into it.*/
        loginUser(((Experimenter) normalUser[1]).getOmeName().getValue());
        Dataset dat = mmFactory.simpleDataset();
        Dataset sentDat = (Dataset) iUpdate.saveAndReturnObject(dat);
        List<IObject> originalFileAndImage = importImageWithOriginalFile(sentDat);
        OriginalFile originalFile = (OriginalFile) originalFileAndImage.get(0);
        Image image = (Image) originalFileAndImage.get(1);

        /* Annotate the imported image with Tag and file attachment.*/
        List<IObject> annotOriginalFileAnnotationTagAndLinks = annotateImageWithTagAndFile(image);

        /* Set up the light admin's permissions for this test.*/
        List<String> permissions = new ArrayList<String>();
        permissions.add(AdminPrivilegeSudo.value);
        if (permChgrp) permissions.add(AdminPrivilegeChgrp.value);
        final EventContext lightAdmin;
        lightAdmin = loginNewAdmin(true, permissions);
        sudo((Experimenter) normalUser[1]);

        /* Take care of workflows which do not use sudo.*/
        if (!isSudoing) {
            loginUser(lightAdmin);
        }
        /* In order to find the image in whatever group, get all groups context.*/
        mergeIntoContext(client.getImplicitContext(), ALL_GROUPS_CONTEXT);

        /* Try to move the image into anotherGroup the normalUser
         * is not a member of, which should fail in all cases
         * except the lightAdmin has Chgrp permission and is not sudoing
         * (i.e. chgrpNoSudoExpectSuccessAnyGroup is true). Also check the
         * the canChgrp boolean.*/
        Assert.assertEquals(getCurrentPermissions(image).canChgrp(), canChgrpExpectedTrue);
        doChange(client, factory, Requests.chgrp().target(image).toGroup(anotherGroupId).build(),
                chgrpNonMemberExpectSuccess);
        if (chgrpNonMemberExpectSuccess) {
            /* Check that the image and its original file moved to another group.*/
            assertInGroup(image, anotherGroupId);
            assertInGroup(originalFile, anotherGroupId);
            /* check the annotations on the image changed the group as expected */
            assertInGroup(annotOriginalFileAnnotationTagAndLinks, anotherGroupId);
        } else {
            /* Check that the image is still in its original group (normalUser's group).*/
            assertInGroup(image, user_group_id);
            assertInGroup(originalFile, user_group_id);
            /* The annotations stayed with the image in the normalUser's group.*/
            assertInGroup(annotOriginalFileAnnotationTagAndLinks, user_group_id);
        }
        /* In any case, the image should still belong to normalUser.*/
        assertOwnedBy(image, user_id);
    }

}
