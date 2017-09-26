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

import omero.RLong;
import omero.SecurityViolation;
import omero.api.IRenderingSettingsPrx;
import omero.gateway.util.Requests;
import omero.model.Experimenter;
import omero.model.IObject;
import omero.model.Image;
import omero.model.ImageI;
import omero.model.Pixels;
import omero.model.RectangleI;
import omero.model.RenderingDef;
import omero.model.Roi;
import omero.model.RoiI;
import omero.model.enums.AdminPrivilegeChown;
import omero.model.enums.AdminPrivilegeDeleteOwned;
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
public class LightAdminRolesROITest extends RolesTests {

    /**
     * Light admin (lightAdmin) tries to put ROI and Rendering Settings on an
     * image of normalUser.
     * lightAdmin tries then to transfer the ownership of the ROI and Rendering settings
     * to normalUser.
     * lightAdmin does not use Sudo in this test.
     * @param permWriteOwned if to test a user who has the <tt>WriteOwned</tt> privilege
     * @param permChown if to test a user who has the <tt>Chown</tt> privilege
     * @param groupPermissions to test the effect of group permission level
     * @throws Exception unexpected
     * @see <a href="https://downloads.openmicroscopy.org/resources/experimental/tests/graph-permissions/0.1/testROIAndRenderingSettingsNoSudo.pptx">graphical explanation</a>
     */
    @Test(dataProvider = "WriteOwned and Chown privileges cases")
    public void testROIAndRenderingSettingsNoSudo(boolean permWriteOwned, boolean permChown,
            String groupPermissions) throws Exception {
        /* Creation of rendering settings on others' images is permitted with WriteOwned permissions
         * in all group types except private.*/
        boolean isExpectSuccessCreateROIRndSettings = permWriteOwned && !groupPermissions.equals("rw----");
        /* The only necessary additional permission for the whole workflow (creation & chown)
         * to succeed is permChown.*/
        boolean isExpectSuccessCreateAndChown = isExpectSuccessCreateROIRndSettings && permChown;
        final IObject[] normalUser = users.get(groupPermissions);
        final long user_group_id = normalUser[0].getId().getValue();
        final long user_id = normalUser[1].getId().getValue();
        /* Set up the light admin's permissions for this test.*/
        List<String> permissions = new ArrayList<String>();
        if (permChown) permissions.add(AdminPrivilegeChown.value);
        if (permWriteOwned) permissions.add(AdminPrivilegeWriteOwned.value);

        /* normalUser creates an image with pixels in normalUser's group.*/
        loginUser(((Experimenter) normalUser[1]).getOmeName().getValue());
        Image image = mmFactory.createImage();
        Image sentImage = (Image) iUpdate.saveAndReturnObject(image);
        Pixels pixelsOfImage = sentImage.getPrimaryPixels();

        /* lightAdmin logs in.*/
        final EventContext lightAdmin;
        lightAdmin = loginNewAdmin(true, permissions);
        client.getImplicitContext().put(omero.constants.GROUP.value, Long.toString(user_group_id));

        /* lightAdmin tries to set ROI on normalUser's image.*/
        Roi roi = new RoiI();
        roi.addShape(new RectangleI());
        roi.setImage((Image) sentImage.proxy());
        try {
            roi = (Roi) iUpdate.saveAndReturnObject(roi);
            /* Check the value of canAnnotate on the sentImage.
             * The value must be true as the ROI can be saved.*/
            Assert.assertTrue(getCurrentPermissions(sentImage).canAnnotate());
            Assert.assertTrue(isExpectSuccessCreateROIRndSettings);
        } catch (SecurityViolation sv) {
            /* Check the value of canAnnotate on the sentImage.
             * The value must be false as the ROI cannot be saved.*/
            Assert.assertFalse(getCurrentPermissions(sentImage).canAnnotate());
            Assert.assertFalse(isExpectSuccessCreateROIRndSettings);
        }

        /* lightAdmin tries to set rendering settings on normalUser's image
         * using setOriginalSettingsInSet method.*/
        IRenderingSettingsPrx prx = factory.getRenderingSettingsService();
        try {
            prx.setOriginalSettingsInSet(Pixels.class.getName(),
                    Arrays.asList(pixelsOfImage.getId().getValue()));
            /* Check the value of canAnnotate on the sentImage.
             * The value must be true as the Rnd settings can be saved.*/
            Assert.assertTrue(getCurrentPermissions(sentImage).canAnnotate());
            Assert.assertTrue(isExpectSuccessCreateROIRndSettings);
        } catch (SecurityViolation sv) {
            /* Check the value of canAnnotate on the sentImage.
             * The value must be false as the Rnd settings cannot be saved.*/
            Assert.assertFalse(getCurrentPermissions(sentImage).canAnnotate());
            Assert.assertFalse(isExpectSuccessCreateROIRndSettings, sv.toString());
        }
        /* Retrieve the image corresponding to the ROI and Rnd settings
         * (if they could be set) and check the ROI and rendering settings
         * belong to lightAdmin, whereas the image belongs to normalUser.*/
        RenderingDef rDef = (RenderingDef) iQuery.findByQuery("FROM RenderingDef WHERE pixels.id = :id",
                new ParametersI().addId(pixelsOfImage.getId()));
        if (isExpectSuccessCreateROIRndSettings) {
            long imageId = ((RLong) iQuery.projection(
                    "SELECT rdef.pixels.image.id FROM RenderingDef rdef WHERE rdef.id = :id",
                    new ParametersI().addId(rDef.getId())).get(0).get(0)).getValue();
            assertOwnedBy(roi, lightAdmin);
            assertOwnedBy(rDef, lightAdmin);
            assertOwnedBy((new ImageI(imageId, false)), user_id);
        } else {
            /* ROI and Rnd settings (rDef) must be null as they could not be set.*/
            roi = (Roi) iQuery.findByQuery("FROM Roi WHERE image.id = :id",
                    new ParametersI().addId(sentImage.getId()));
            Assert.assertNull(roi);
            Assert.assertNull(rDef);
        }
        /* lightAdmin tries to chown the ROI and the rendering settings (rDef) to normalUser.
         * Only attempt the canChown check and the chown if the ROI and rendering settings exist.*/
        if (isExpectSuccessCreateROIRndSettings) {
            /* Check the value of canChown on the ROI and rendering settings (rDef) matches
             * the boolean isExpectSuccessCreateAndChownRndSettings.*/
            Assert.assertEquals(getCurrentPermissions(roi).canChown(), isExpectSuccessCreateAndChown);
            Assert.assertEquals(getCurrentPermissions(rDef).canChown(), isExpectSuccessCreateAndChown);
            /* Note that in read-only group, the chown of ROI would fail, see
             * https://trello.com/c/7o4q2Tkt/745-fix-graphs-for-mixed-ownership-read-only.
             * The workaround used here is to chown both the image and the ROI.*/
            doChange(client, factory, Requests.chown().target(roi, sentImage).toUser(user_id).build(), isExpectSuccessCreateAndChown);
            doChange(client, factory, Requests.chown().target(rDef).toUser(user_id).build(), isExpectSuccessCreateAndChown);
            /* Retrieve the image corresponding to the ROI and Rnd settings.*/
            long imageId = ((RLong) iQuery.projection(
                    "SELECT rdef.pixels.image.id FROM RenderingDef rdef WHERE rdef.id = :id",
                    new ParametersI().addId(rDef.getId())).get(0).get(0)).getValue();
            if (isExpectSuccessCreateAndChown) {
                /* First case: Workflow succeeded for creation and chown, all belongs to normalUser.*/
                assertOwnedBy(roi, user_id);
                assertOwnedBy(rDef, user_id);
                assertOwnedBy((new ImageI (imageId, false)), user_id);
            } else {
                /* Second case: Creation succeeded, but the chown failed.*/
                assertOwnedBy(roi, lightAdmin);
                assertOwnedBy(rDef, lightAdmin);
                assertOwnedBy((new ImageI(imageId, false)), user_id);
            }
        } else {
            /* Third case: Creation did not succeed, and chown was not attempted.*/
            Assert.assertNull(roi);
            Assert.assertNull(rDef);
        }
    }


    /**
     * Light admin (lightAdmin) tries to delete ROI (belonging to normalUser)
     * The ROI is on image of normalUser.
     * lightAdmin does not use Sudo in this test.
     * @param isPrivileged if to test a user who has the <tt>DeleteOwned</tt> privilege
     * @param groupPermissions to test the effect of group permission level
     * @throws Exception unexpected
     */
    @Test(dataProvider = "isPrivileged cases")
    public void testROIDelete(boolean isPrivileged, String groupPermissions) throws Exception {
        boolean isExpectSuccessDeleteROI = isPrivileged;
        final IObject[] normalUser = users.get(groupPermissions);
        final long user_group_id = normalUser[0].getId().getValue();
        final long user_id = normalUser[1].getId().getValue();
        /* Set up the light admin's permissions for this test.*/
        List<String> permissions = new ArrayList<String>();
        if (isPrivileged) permissions.add(AdminPrivilegeDeleteOwned.value);

        /* normalUser creates an image with pixels and ROI in normalUser's group.*/
        loginUser(((Experimenter) normalUser[1]).getOmeName().getValue());
        Image image = mmFactory.createImage();
        Image sentImage = (Image) iUpdate.saveAndReturnObject(image);
        sentImage.getPrimaryPixels();
        Roi roi = new RoiI();
        roi.addShape(new RectangleI());
        roi.setImage((Image) sentImage.proxy());
        roi = (Roi) iUpdate.saveAndReturnObject(roi);
        assertOwnedBy(sentImage, user_id);
        assertOwnedBy(roi, user_id);
        /* lightAdmin logs in and tries to delete the ROI.*/
        loginNewAdmin(true, permissions);
        client.getImplicitContext().put(omero.constants.GROUP.value, Long.toString(user_group_id));
        doChange(client, factory, Requests.delete().target(roi).build(), isExpectSuccessDeleteROI);
        /* Check the ROI was deleted, whereas the image exists.*/
        if (isExpectSuccessDeleteROI) {
            assertDoesNotExist(roi);
        } else {
            assertExists(roi);
        }
        assertExists(sentImage);
    }

}
