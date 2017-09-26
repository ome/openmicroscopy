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

import omero.RLong;
import omero.ServerError;
import omero.cmd.Chown2;
import omero.gateway.util.Requests;
import omero.model.Dataset;
import omero.model.DatasetImageLink;
import omero.model.DatasetImageLinkI;
import omero.model.ExperimenterGroup;
import omero.model.ExperimenterGroupI;
import omero.model.Image;
import omero.model.Project;
import omero.model.ProjectDatasetLink;
import omero.model.ProjectDatasetLinkI;
import omero.model.enums.AdminPrivilegeChown;
import omero.model.enums.AdminPrivilegeWriteOwned;
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
public class LightAdminRolesLinkUserTest extends RolesTests {


    /**
     * lightAdmin tries to link their object to a pre-existing container (Dataset or Project)
     * in the target group (of normalUser).
     * Note that in this test lightAdmin is a member of normalUser's group.
     * normalUser creates and saves the Dataset and Project,
     * then lightAdmin or otherUser creates an image and dataset
     * and they try to link these objects to the containers (Dataset or Project)
     * of normalUser. lightAdmin succeeds if they have sufficient privileges.
     * Neither partially working with own data, nor being
     * a member of the group elevates lightAdmin's privileges over the
     * privileges of a normal member of group (otherUser) working with their own data.
     * @param permWriteOwned if to test a user who has the <tt>WriteOwned</tt> privilege
     * @param isAdmin if to test a lightAdmin
     * @param groupPermissions to test the effect of group permission level
     * @throws Exception unexpected
     */
    @Test(dataProvider = "WriteOwned and isAdmin cases")
    public void testLinkMemberOfGroupNoSudo(boolean permWriteOwned, boolean isAdmin,
            String groupPermissions) throws Exception {
        /* WriteOwned permission is necessary and sufficient for lightAdmin to link
         * others objects to their objects. Exceptions are Private group, where such linking will
         * fail in all cases and Read-Write group where linking will succeed even
         * for otherUser (otherUser and lightAdmin are both members of the group).*/
        boolean isExpectLinkingSuccessAdmin =
                (permWriteOwned && !groupPermissions.equals("rw----") || groupPermissions.equals("rwrw--"));
        boolean isExpectLinkingSuccessUser = groupPermissions.equals("rwrw--");
        final boolean isExpectLinkingSuccess = isAdmin ? isExpectLinkingSuccessAdmin : isExpectLinkingSuccessUser;
        final EventContext normalUser = newUserAndGroup(groupPermissions);
        final EventContext otherUser = newUserAndGroup(groupPermissions);
        ExperimenterGroup normalUsergroup = new ExperimenterGroupI(normalUser.groupId, false);
        /* Set up the light admin's permissions for this test.*/
        List<String> permissions = new ArrayList<String>();
        if (permWriteOwned) permissions.add(AdminPrivilegeWriteOwned.value);
        final EventContext lightAdmin = loginNewAdmin(true, permissions);
        /* root adds lightAdmin to normalUser's group.*/
        logRootIntoGroup(normalUser);
        normalUsergroup = addUsers(normalUsergroup, ImmutableList.of(lightAdmin.userId, otherUser.userId), false);
        /* Create Dataset and Project as normalUser in normalUser's group.*/
        loginUser(normalUser);
        Dataset dat = mmFactory.simpleDataset();
        Dataset sentDat = (Dataset) iUpdate.saveAndReturnObject(dat);
        Project proj = mmFactory.simpleProject();
        Project sentProj = (Project) iUpdate.saveAndReturnObject(proj);
        /* Create Image and Dataset as lightAdmin or otherUser in normalUser's group.*/
        if (isAdmin) {
            loginUser(lightAdmin);
        } else {
            loginUser(otherUser);
        }
        client.getImplicitContext().put(omero.constants.GROUP.value, Long.toString(normalUser.groupId));
        Image ownImage = mmFactory.createImage();
        Image sentOwnImage = (Image) iUpdate.saveAndReturnObject(ownImage);
        Dataset ownDat = mmFactory.simpleDataset();
        Dataset sentOwnDat = (Dataset) iUpdate.saveAndReturnObject(ownDat);
        /* lightAdmin or otherUser checks that the canLink value on all the objects to be linked
         * is true (for own image) and for other people's objects (sentProj, sentDat) the canLink
         * are matching the expected behavior (see booleans isExpectLinkingSuccess... definitions).*/
        Assert.assertTrue(getCurrentPermissions(sentOwnImage).canLink());
        Assert.assertEquals(getCurrentPermissions(sentProj).canLink(), isExpectLinkingSuccess);
        /* lightAdmin or otherUser try to create links between their own image and normalUser's Dataset
         * and between their own Dataset and normalUser's Project.*/
        try {
            linkParentToChild(sentDat, sentOwnImage);
            linkParentToChild(sentProj, sentOwnDat);
            Assert.assertTrue(isExpectLinkingSuccess);
        } catch (ServerError se) {
            Assert.assertFalse(isExpectLinkingSuccess, se.toString());
        }
    }

    /**
     * lightAdmin tries to link an object to a pre-existing container (Dataset or Project)
     * in the target group (of normalUser where lightAdmin is not member).
     * lightAdmin tries to link image or Dataset to Dataset or Project.
     * The image import (by lightAdmin for others) has been tested in other tests.
     * Here, normalUser creates and saves the image, Dataset and Project,
     * then lightAdmin tries to link these objects.
     * lightAdmin will succeed if they have WriteOwned privilege.
     * @param permWriteOwned if to test a user who has the <tt>WriteOwned</tt> privilege
     * @param permChown if to test a user who has the <tt>Chown</tt> privilege
     * @param groupPermissions to test the effect of group permission level
     * @throws Exception unexpected
     * @see <a href="https://downloads.openmicroscopy.org/resources/experimental/tests/graph-permissions/0.1/testLinkNoSudo.pptx">graphical explanation</a>
     */
    @Test(dataProvider = "WriteOwned and Chown privileges cases")
    public void testLinkNoSudo(boolean permWriteOwned, boolean permChown,
            String groupPermissions) throws Exception {
        /* WriteOwned permission is necessary and sufficient for lightAdmin to link
         * others objects. Exception is Private group, where such linking will
         * fail in all cases.*/
        boolean isExpectLinkingSuccess = permWriteOwned && !groupPermissions.equals("rw----");
        boolean isExpectSuccessLinkAndChown = isExpectLinkingSuccess && permChown;
        final EventContext normalUser = newUserAndGroup(groupPermissions);
        /* Set up the light admin's permissions for this test.*/
        List<String> permissions = new ArrayList<String>();
        if (permChown) permissions.add(AdminPrivilegeChown.value);
        if (permWriteOwned) permissions.add(AdminPrivilegeWriteOwned.value);
        final EventContext lightAdmin;
        lightAdmin = loginNewAdmin(true, permissions);
        /* Create an image, Dataset and Project as normalUser in normalUser's group.*/
        loginUser(normalUser);
        client.getImplicitContext().put(omero.constants.GROUP.value, Long.toString(normalUser.groupId));
        Image image = mmFactory.createImage();
        Image sentImage = (Image) iUpdate.saveAndReturnObject(image);
        Dataset dat = mmFactory.simpleDataset();
        Dataset sentDat = (Dataset) iUpdate.saveAndReturnObject(dat);
        Project proj = mmFactory.simpleProject();
        Project sentProj = (Project) iUpdate.saveAndReturnObject(proj);
        loginUser(lightAdmin);
        client.getImplicitContext().put(omero.constants.GROUP.value, Long.toString(normalUser.groupId));
        /* lightAdmin checks that the canLink value on all the objects to be linked
         * matches the isExpectLinkingSuccess boolean.*/
        Assert.assertEquals(getCurrentPermissions(sentImage).canLink(), isExpectLinkingSuccess);
        Assert.assertEquals(getCurrentPermissions(sentDat).canLink(), isExpectLinkingSuccess);
        Assert.assertEquals(getCurrentPermissions(sentProj).canLink(), isExpectLinkingSuccess);
        /* lightAdmin tries to create links between the image and Dataset
         * and between Dataset and Project.
         * If links could not be created, finish the test.*/
        DatasetImageLink linkOfDatasetImage = new DatasetImageLinkI();
        ProjectDatasetLink linkOfProjectDataset = new ProjectDatasetLinkI();
        try {
            linkOfDatasetImage = linkParentToChild(sentDat, sentImage);
            linkOfProjectDataset = linkParentToChild(sentProj, sentDat);
            Assert.assertTrue(isExpectLinkingSuccess);
        } catch (ServerError se) {
            Assert.assertFalse(isExpectLinkingSuccess, se.toString());
            return;
        }

        /* Check that the value of canChown boolean on the links is matching
         * the isExpectSuccessLinkAndChown boolean.*/
        Assert.assertEquals(getCurrentPermissions(linkOfDatasetImage).canChown(), isExpectSuccessLinkAndChown);
        Assert.assertEquals(getCurrentPermissions(linkOfProjectDataset).canChown(), isExpectSuccessLinkAndChown);

        /* lightAdmin transfers the ownership of both links to normalUser.
         * The success of the whole linking and chowning
         * operation is captured in boolean isExpectSuccessLinkAndChown. Note that the
         * ownership of the links must be transferred explicitly, as the Chown feature
         * on the Project would not transfer ownership links owned by non-owners
         * of the Project/Dataset/Image objects (chown on mixed ownership hierarchy does not chown objects
         * owned by other users).*/
        Chown2 chown = Requests.chown().target(linkOfDatasetImage).toUser(normalUser.userId).build();
        doChange(client, factory, chown, isExpectSuccessLinkAndChown);
        chown = Requests.chown().target(linkOfProjectDataset).toUser(normalUser.userId).build();
        doChange(client, factory, chown, isExpectSuccessLinkAndChown);

        /* Check the ownership of the links, Image, Dataset and Project.*/
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
            assertOwnedBy((new DatasetImageLinkI(linkDatasetImageId, false)), normalUser);
            assertOwnedBy((new ProjectDatasetLinkI(linkProjectDatasetId, false)), normalUser);
        } else {
            assertOwnedBy((new DatasetImageLinkI(linkDatasetImageId, false)), lightAdmin);
            assertOwnedBy((new ProjectDatasetLinkI(linkProjectDatasetId, false)), lightAdmin);
        }
    }

}
