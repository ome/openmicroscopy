/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2012 University of Dundee & Open Microscopy Environment.
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
package integration.chgrp;

import integration.AbstractServerTest;
import integration.DeleteServiceTest;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ome.xml.model.OME;
import omero.ServerError;
import omero.cmd.Chgrp2;
import omero.model.Dataset;
import omero.model.ExperimenterGroup;
import omero.model.Image;
import omero.model.Instrument;
import omero.model.Laser;
import omero.model.LightSource;
import omero.model.Pixels;
import omero.sys.ParametersI;

import org.apache.commons.lang.RandomStringUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableMap;

import static org.testng.AssertJUnit.*;
import ome.specification.XMLMockObjects;
import ome.specification.XMLWriter;

/**
 *
 *
 * @author Scott Littlewood, <a
 *         href="mailto:sylittlewood@dundee.ac.uk">sylittlewood@dundee.ac.uk</a>
 * @since Beta4.4
 */
public class HierarchyMoveImageWithAcquisitionDataTest extends
        AbstractServerTest {

    /**
     * Overridden to delete the files.
     *
     * @see AbstractServerTest#tearDown()
     */
    @Override
    @AfterClass
    public void tearDown() throws Exception {
    }

    private Pixels createImageWithAcquisitionData() throws Exception {
        File f = File.createTempFile(RandomStringUtils.random(100, false, true),
                ".ome.xml");
        f.deleteOnExit();
        XMLMockObjects xml = new XMLMockObjects();
        XMLWriter writer = new XMLWriter();
        OME ome = xml.createImageWithAcquisitionData();
        writer.writeFile(f, ome, true);

        try {
            List<Pixels> pixels = importFile(f, "ome.xml");
            return pixels.get(0);
        } catch (Throwable e) {
            throw new Exception("cannot import image", e);
        }
    }

    /**
     * Helper method to load the Image.
     *
     * @param p
     * @return
     * @throws ServerError
     */
    protected Image getImageWithId(long imageId) throws ServerError {
        ParametersI queryParameters = new ParametersI();
        queryParameters.addId(imageId);
        String queryForImage = "select img from Image as img where img.id = :id";
        return (Image) iQuery.findByQuery(queryForImage, queryParameters);
    }

    /**
     * Test moving data as the data owner from a private to a private group
     *
     * @throws Exception
     */
    @Test(groups = "broken")
    public void moveImageRWtoRW() throws Exception {
        moveImageBetweenPermissionGroups("rw----", "rw----");
    }

    /**
     * Performs the changing of group for an image with an ROI owned by the same
     * user
     *
     * @param sourceGroupPermissions
     * @param targetGroupPermissions
     * @throws Exception
     */
    private void moveImageBetweenPermissionGroups(
            String sourceGroupPermissions, String targetGroupPermissions)
            throws Exception {

        XMLMockObjects mockObjects = new XMLMockObjects();

        long userId = iAdmin.getEventContext().userId;

        ExperimenterGroup sourceGroup = createGroupWithMember(userId,
                sourceGroupPermissions);

        ExperimenterGroup targetGroup = createGroupWithMember(userId,
                targetGroupPermissions);

        long targetGroupId = targetGroup.getId().getValue();

        // force a refresh of the user's group membership
        iAdmin.getEventContext();

        Pixels pixels = createImageWithAcquisitionData();
        Image sourceImage = pixels.getImage();

        Image savedImage = (Image) iUpdate.saveAndReturnObject(sourceImage);
        long originalImageId = savedImage.getId().getValue();
        List<Long> imageIds = new ArrayList<Long>(1);
        imageIds.add(originalImageId);

        // make sure we are in the source group
        loginUser(sourceGroup);

        // Perform the move operation.
        final Chgrp2 dc = new Chgrp2();
        dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                Dataset.class.getSimpleName(),
                Collections.singletonList(originalImageId));
        dc.groupId = targetGroupId;
        callback(true, client, dc);

        // check if the image have been moved.
        Image returnedSourceImage = getImageWithId(originalImageId);
        assertNull(returnedSourceImage);

        // Move the user into the target group!
        loginUser(targetGroup);

        // get the acquisition data

        // check it's correct
        ome.xml.model.Laser xmlLaser = (ome.xml.model.Laser) mockObjects
                .createLightSource(ome.xml.model.Laser.class.getName(), 0);

        Image returnedTargetImage = getImageWithId(originalImageId);
        assertNotNull(returnedTargetImage);

        long instrumentId = returnedTargetImage.getInstrument().getId()
                .getValue();

        Instrument instrument = factory.getMetadataService().loadInstrument(
                instrumentId);

        List<LightSource> lights = instrument.copyLightSource();

        for (LightSource lightSource : lights) {
            if (lightSource instanceof Laser)
                validateLaser((Laser) lightSource, xmlLaser);
        }
    }

    /**
     * Creates a new group for the user with the permissions detailed
     *
     * @param userId
     * @param permissions
     * @return
     * @throws Exception
     */
    private ExperimenterGroup createGroupWithMember(long userId,
            String permissions) throws Exception {
        return newGroupAddUser(permissions, userId);
    }

    /**
     * Validates if the inserted object corresponds to the XML object.
     *
     * @param laser
     *            The laser to check.
     * @param xml
     *            The XML version.
     */
    private void validateLaser(Laser laser, ome.xml.model.Laser xml) {
        assertEquals(laser.getManufacturer().getValue(), xml.getManufacturer());
        assertEquals(laser.getModel().getValue(), xml.getModel());
        assertEquals(laser.getSerialNumber().getValue(), xml.getSerialNumber());
        assertEquals(laser.getLotNumber().getValue(), xml.getLotNumber());
        assertEquals(laser.getPower().getValue(), xml.getPower());
        assertTrue(laser.getType().getValue().getValue()
                .equals(XMLMockObjects.LASER_TYPE.getValue()));
    }
}
