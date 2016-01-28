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
import java.util.Arrays;
import java.util.List;

import omero.ServerError;
import omero.cmd.Chgrp2;
import omero.gateway.util.Requests;
import omero.model.ExperimenterGroup;
import omero.model.IObject;
import omero.model.Image;
import omero.model.Rectangle;
import omero.model.RectangleI;
import omero.model.Roi;
import omero.model.RoiI;
import omero.model.Shape;
import omero.sys.EventContext;
import omero.sys.ParametersI;

import org.testng.annotations.Test;

import static org.testng.AssertJUnit.*;

/**
 * @author Scott Littlewood, <a
 *         href="mailto:sylittlewood@dundee.ac.uk">sylittlewood@dundee.ac.uk</a>
 * @since Beta4.4
 */
public class HierarchyMoveImageWithRoiFromOtherUserTest extends
        AbstractServerTest {

    /**
     * Performs the changing of group for an image with an ROI owned by the same
     * user
     *
     * @param sourceGroupPermissions
     * @param targetGroupPermissions
     * @throws Exception
     */
    private void moveImageBetweenPermissionGroups(
            String sourceGroupPermissions, String targetGroupPermissions,
            boolean roiOwnerInTargetGroup) throws Exception {

        // image owner
        EventContext imageOwnerContext = newUserAndGroup(
                sourceGroupPermissions, false);

        Image image = createSimpleImage();
        long originalImageId = image.getId().getValue();

        // change to another user in source group
        EventContext roiUserContext = newUserInGroup();

        // get the image from the roi user's perspective
        Image roiUserImage = getImageWithId(image.getId().getValue());

        // create the roi as the roi user
        Roi serverROI = createSimpleRoiFor(roiUserImage);
        long originalRoiId = serverROI.getId().getValue();

        List<Long> shapeIds = new ArrayList<Long>();

        for (int i = 0; i < serverROI.sizeOfShapes(); i++) {
            Shape shape = serverROI.getShape(i);
            shapeIds.add(shape.getId().getValue());
        }

        disconnect();

        // create the target group
        ExperimenterGroup targetGroup = newGroupAddUser(targetGroupPermissions,
                imageOwnerContext.userId, false);

        if (roiOwnerInTargetGroup) {
            addUserToGroup(roiUserContext.userId, targetGroup);
        }

        // switch back to the original user
        loginUser(imageOwnerContext);

        // Perform the move operation as original user
        final Chgrp2 dc = Requests.chgrp("Image", originalImageId, targetGroup.getId().getValue());
        callback(true, client, dc);

        // check the roi has been moved to target group
        Roi originalRoi = getRoiWithId(originalRoiId);
        assertNull(originalRoi);

        // check the shapes have moved to target group
        List<IObject> orginalShapes = getShapesWithIds(shapeIds);
        assertEquals(0, orginalShapes.size());

        disconnect();

        // Move the user into the target group!
        loginUser(targetGroup);

        EventContext targetGroupContext = iAdmin.getEventContext();

        assertFalse(imageOwnerContext.groupId == targetGroupContext.groupId);
        assertEquals(imageOwnerContext.userId, targetGroupContext.userId);

        // check that the image has moved
        Image movedImage = getImageWithId(originalImageId);
        assertNotNull(movedImage);

        // Check that the ROI has moved
        Roi movedRoi = getRoiWithId(originalRoiId);
        assertNotNull(movedRoi);

        List<IObject> movedShapes = getShapesWithIds(shapeIds);
        assertEquals(shapeIds.size(), movedShapes.size());

        // check who the owner of roi is
        long movedRoiOwnerId = movedRoi.getDetails().getOwner().getId()
                .getValue();
        assertEquals(roiUserContext.userId, movedRoiOwnerId);
    }

    /**
     * Test moving data as the data owner from a read-write to a read-write
     * group
     *
     * @throws Exception
     */
    @Test
    public void moveImageRWRWtoRWRW() throws Exception {
        moveImageBetweenPermissionGroups("rwrw--", "rwrw--", true);
    }

    /**
     * Test moving data as the data owner from a read-write to a read-write
     * group where the Roi User is not in the target group
     *
     * @throws Exception
     */
    @Test
    public void moveImageRWRWtoRWRW_RoiUserNotInTargetGroup() throws Exception {
        moveImageBetweenPermissionGroups("rwrw--", "rwrw--", false);
    }

    /**
     * Test moving data as the data owner from a read-annotate to a read-only
     * group
     *
     * @throws Exception
     */
    @Test
    public void moveImageRWRAtoRWR() throws Exception {
        moveImageBetweenPermissionGroups("rwra--", "rwr---", true);
    }

    /**
     * Test moving data as the data owner from a read-annotate to a read-only
     * group where Roi user is not in the target group group
     *
     * @throws Exception
     */
    @Test
    public void moveImageRWRAtoRWR_RoiUserNotInTargetGroup() throws Exception {
        moveImageBetweenPermissionGroups("rwra--", "rwr---", false);
    }

    /**
     * Test moving data as the data owner from a read-annotate to a
     * read-annotate group
     *
     * @throws Exception
     */
    @Test
    public void moveImageRWRAtoRWRA() throws Exception {
        moveImageBetweenPermissionGroups("rwra--", "rwra--", true);
    }

    /**
     * Test moving data as the data owner from a read-annotate to a
     * read-annotate group where Roi user is not in the target group group
     *
     * @throws Exception
     */
    @Test
    public void moveImageRWRAtoRWRA_RoiUserNotInTargetGroup() throws Exception {
        moveImageBetweenPermissionGroups("rwra--", "rwra--", false);
    }

    /**
     * Test moving data as the data owner from a read-annotate to a read-write
     * group
     *
     * @throws Exception
     */
    @Test
    public void moveImageRWRAtoRWRW() throws Exception {
        moveImageBetweenPermissionGroups("rwra--", "rwrw--", true);
    }

    /**
     * Test moving data as the data owner from a read-annotate to a read-write
     * group where Roi user is not in the target group group
     *
     * @throws Exception
     */
    @Test
    public void moveImageRWRAtoRWRW_RoiUserNotInTargetGroup() throws Exception {
        moveImageBetweenPermissionGroups("rwra--", "rwrw--", false);
    }

    /**
     * Test moving data as the data owner from a read-write to a read-only group
     *
     * @throws Exception
     */
    @Test
    public void moveImageRWRWtoRWR() throws Exception {
        moveImageBetweenPermissionGroups("rwrw--", "rwr---", true);
    }

    /**
     * Test moving data as the data owner from a read-write to a read-only group
     * where Roi user is not in the target group group
     *
     * @throws Exception
     */
    @Test
    public void moveImageRWRWtoRWR_RoiUserNotInTargetGroup() throws Exception {
        moveImageBetweenPermissionGroups("rwrw--", "rwr---", false);
    }

    /**
     * Test moving data as the data owner from a read-write to a read-annotate
     * group
     *
     * @throws Exception
     */
    @Test
    public void moveImageRWRWtoRWRA() throws Exception {
        moveImageBetweenPermissionGroups("rwrw--", "rwra--", true);
    }

    /**
     * Test moving data as the data owner from a read-write to a read-annotate
     * group where Roi user is not in the target group group
     *
     * @throws Exception
     */
    @Test
    public void moveImageRWRWtoRWRA_RoiUserNotInTargetGroup() throws Exception {
        moveImageBetweenPermissionGroups("rwrw--", "rwra--", false);
    }

    /**
     * Adds the user to the group, no context switching
     *
     * @param userId
     * @param targetGroup
     * @throws Exception
     */
    private void addUserToGroup(long userId, ExperimenterGroup targetGroup)
            throws Exception {
        addUsers(targetGroup, Arrays.asList(userId), false);
    }

    /**
     * Queries the server for the Image with the id provided under the current
     * user/group security context
     *
     * @param imageId
     * @return
     * @throws ServerError
     */
    private Image getImageWithId(long imageId) throws ServerError {
        ParametersI queryParameters = new ParametersI();
        queryParameters.addId(imageId);
        String queryForImage = "select d from Image as d where d.id = :id";
        return (Image) iQuery.findByQuery(queryForImage, queryParameters);
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
            Rectangle rect = new RectangleI();
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
