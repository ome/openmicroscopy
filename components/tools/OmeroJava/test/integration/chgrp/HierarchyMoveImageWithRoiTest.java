/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2015 University of Dundee & Open Microscopy Environment.
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

import static omero.rtypes.rdouble;
import static omero.rtypes.rint;
import integration.AbstractServerTest;

import java.util.ArrayList;
import java.util.List;

import omero.ServerError;
import omero.cmd.Chgrp2;
import omero.gateway.util.Requests;
import omero.model.ExperimenterGroup;
import omero.model.IObject;
import omero.model.Image;
import omero.model.Rect;
import omero.model.RectI;
import omero.model.Roi;
import omero.model.RoiI;
import omero.model.Shape;
import omero.sys.ParametersI;

import org.testng.annotations.Test;

import static org.testng.AssertJUnit.*;

/**
 * @author Scott Littlewood, <a
 *         href="mailto:sylittlewood@dundee.ac.uk">sylittlewood@dundee.ac.uk</a>
 * @since Beta4.4
 */
public class HierarchyMoveImageWithRoiTest extends AbstractServerTest {

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

        long userId = iAdmin.getEventContext().userId;

        ExperimenterGroup sourceGroup = newGroupAddUser(sourceGroupPermissions,
                userId);

        ExperimenterGroup targetGroup = newGroupAddUser(targetGroupPermissions,
                        userId);
        long rwGroupId = targetGroup.getId().getValue();

        // force a refresh of the user's group membership
        iAdmin.getEventContext();

        Image image = createSimpleImage();
        long originalImageId = image.getId().getValue();

        Roi serverROI = createSimpleRoiFor(image);
        long originalRoiId = serverROI.getId().getValue();

        List<Long> shapeIds = new ArrayList<Long>();

        for (int i = 0; i < serverROI.sizeOfShapes(); i++) {
            Shape shape = serverROI.getShape(i);
            shapeIds.add(shape.getId().getValue());
        }

        // make sure we are in the source group
        loginUser(sourceGroup);

        // Perform the move operation.
        final Chgrp2 dc = Requests.chgrp("Image", originalImageId, rwGroupId);
        callback(true, client, dc);

        // check if the objects have been moved.
        Roi originalRoi = getRoiWithId(originalRoiId);
        assertNull(originalRoi);

        // check the shapes have been moved
        List<IObject> orginalShapes = getShapesWithIds(shapeIds);
        assertEquals(0, orginalShapes.size());

        // Move the user into the RW group!
        loginUser(targetGroup);

        // Check that the ROI has moved
        Roi movedRoi = getRoiWithId(originalRoiId);
        assertNotNull(movedRoi);

        List<IObject> movedShapes = getShapesWithIds(shapeIds);
        assertEquals(shapeIds.size(), movedShapes.size());
    }

    /**
     * Test moving data as the data owner from a private to a private group
     *
     * @throws Exception
     */
    @Test
    public void moveImageRWtoRW() throws Exception {
        moveImageBetweenPermissionGroups("rw----", "rw----");
    }

    /**
     * Test moving data as the data owner from a private to a read-only group
     *
     * @throws Exception
     */
    @Test
    public void moveImageRWtoRWR() throws Exception {
        moveImageBetweenPermissionGroups("rw----", "rwr---");
    }

    /**
     * Test moving data as the data owner from a private to a read-annotate
     * group
     *
     * @throws Exception
     */
    @Test
    public void moveImageRWtoRWRA() throws Exception {
        moveImageBetweenPermissionGroups("rw----", "rwra--");
    }

    /**
     * Test moving data as the data owner from a private to a read-write group
     *
     * @throws Exception
     */
    @Test
    public void moveImageRWtoRWRW() throws Exception {
        moveImageBetweenPermissionGroups("rw----", "rwrw--");
    }

    /**
     * Test moving data as the data owner from a read-only to a private group
     *
     * @throws Exception
     */
    @Test
    public void moveImageRWRtoRW() throws Exception {
        moveImageBetweenPermissionGroups("rwr---", "rw----");
    }

    /**
     * Test moving data as the data owner from a read-only to a read-only group
     *
     * @throws Exception
     */
    @Test
    public void moveImageRWRtoRWR() throws Exception {
        moveImageBetweenPermissionGroups("rwr---", "rwr---");
    }

    /**
     * Test moving data as the data owner from a read-only to a read-annotate
     * group
     *
     * @throws Exception
     */
    @Test
    public void moveImageRWRtoRWRA() throws Exception {
        moveImageBetweenPermissionGroups("rwr---", "rwra--");
    }

    /**
     * Test moving data as the data owner from a read-only to a read-write group
     *
     * @throws Exception
     */
    @Test
    public void moveImageRWRtoRWRW() throws Exception {
        moveImageBetweenPermissionGroups("rwr---", "rwrw--");
    }

    /**
     * Test moving data as the data owner from a read-annotate to a private
     * group
     *
     * @throws Exception
     */
    @Test
    public void moveImageRWRAtoRW() throws Exception {
        moveImageBetweenPermissionGroups("rwra--", "rw----");
    }

    /**
     * Test moving data as the data owner from a read-annotate to a read-only
     * group
     *
     * @throws Exception
     */
    @Test
    public void moveImageRWRAtoRWR() throws Exception {
        moveImageBetweenPermissionGroups("rwra--", "rwr---");
    }

    /**
     * Test moving data as the data owner from a read-annotate to a
     * read-annotate group
     *
     * @throws Exception
     */
    @Test
    public void moveImageRWRAtoRWRA() throws Exception {
        moveImageBetweenPermissionGroups("rwra--", "rwra--");
    }

    /**
     * Test moving data as the data owner from a read-annotate to a read-write
     * group
     *
     * @throws Exception
     */
    @Test
    public void moveImageRWRAtoRWRW() throws Exception {
        moveImageBetweenPermissionGroups("rwra--", "rwrw--");
    }

    /**
     * Test moving data as the data owner from a read-write to a private group
     *
     * @throws Exception
     */
    @Test
    public void moveImageRWRWtoRW() throws Exception {
        moveImageBetweenPermissionGroups("rwrw--", "rw----");
    }

    /**
     * Test moving data as the data owner from a read-write to a read-only group
     *
     * @throws Exception
     */
    @Test
    public void moveImageRWRWtoRWR() throws Exception {
        moveImageBetweenPermissionGroups("rwrw--", "rwr---");
    }

    /**
     * Test moving data as the data owner from a read-write to a read-annotate
     * group
     *
     * @throws Exception
     */
    @Test
    public void moveImageRWRWtoRWRA() throws Exception {
        moveImageBetweenPermissionGroups("rwrw--", "rwra--");
    }

    /**
     * Test moving data as the data owner from a read-write to a read-write
     * group
     *
     * @throws Exception
     */
    @Test
    public void moveImageRWRWtoRWRW() throws Exception {
        moveImageBetweenPermissionGroups("rwrw--", "rwrw--");
    }

    /**
     * Queries the server for the ROI with the id provided under the current
     * user/group security context
     *
     * @param roiId
     * @return
     * @throws ServerError
     */
    private Roi getRoiWithId(long roiId) throws ServerError {
        ParametersI queryParameters = new ParametersI();
        queryParameters.addId(roiId);
        String queryForROI = "select d from Roi as d where d.id = :id";
        return (Roi) iQuery.findByQuery(queryForROI, queryParameters);
    }

    /**
     * Queries the server for all the shapes with matching ids under the current
     * user/group security context
     *
     * @param shapeIds
     * @return
     * @throws ServerError
     */
    private List<IObject> getShapesWithIds(List<Long> shapeIds)
            throws ServerError {
        ParametersI queryParameters = new ParametersI();
        queryParameters.addIds(shapeIds);
        String queryForShapes = "select d from Shape as d where d.id in (:ids)";
        return iQuery.findAllByQuery(queryForShapes, queryParameters);
    }

    /**
     * Creates and returns a server created ROI on an image under the current
     * user/group security context
     *
     * @param image
     * @return
     * @throws ServerError
     */
    private Roi createSimpleRoiFor(Image image) throws ServerError {
        Roi roi = new RoiI();
        roi.setImage(image);

        for (int i = 0; i < 3; i++) {
            Rect rect = new RectI();
            rect.setX(rdouble(10));
            rect.setY(rdouble(20));
            rect.setWidth(rdouble(40));
            rect.setHeight(rdouble(80));
            rect.setTheZ(rint(i));
            rect.setTheT(rint(0));
            roi.addShape(rect);
        }

        return (RoiI) iUpdate.saveAndReturnObject(roi);
    }

    /**
     * Creates and returns an image on the server under the current user/group
     * security context
     *
     * @return
     * @throws ServerError
     */
    private Image createSimpleImage() throws ServerError {
        Image simpleImage = mmFactory.simpleImage();
        return (Image) iUpdate.saveAndReturnObject(simpleImage);
    }
}
