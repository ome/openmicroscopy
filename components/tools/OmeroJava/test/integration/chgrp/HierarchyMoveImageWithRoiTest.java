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

import static omero.rtypes.rdouble;
import static omero.rtypes.rint;
import integration.AbstractServerTest;
import integration.DeleteServiceTest;

import java.util.ArrayList;
import java.util.List;

import omero.ServerError;
import omero.cmd.Chgrp;
import omero.model.ExperimenterGroup;
import omero.model.IObject;
import omero.model.Image;
import omero.model.Rect;
import omero.model.RectI;
import omero.model.Roi;
import omero.model.RoiI;
import omero.model.Shape;
import omero.sys.EventContext;
import omero.sys.ParametersI;

import org.testng.annotations.Test;

/**
 * 
 * 
 * @author Scott Littlewood, <a
 *         href="mailto:sylittlewood@dundee.ac.uk">sylittlewood@dundee.ac.uk</a>
 * @since Beta4.4
 */
public class HierarchyMoveImageWithRoiTest extends AbstractServerTest {

    // given previous events
    // when executing a command
    // expect event stack

    // user1 created
    // private group1 created
    // read write group2 created
    // user1 in group1, group2
    // user1 adds imageA to group1
    // user1 adds roi to imageA

    // user1 executes command: move image from group1 to group2

    @Test
    public void moveImageWithROIFromPrivateGroupToReadWriteGroupAsMember()
            throws Exception {
        EventContext targetGroupEventContext = newUserAndGroup("rwrw--");

        System.out.println(String.format("Target Group: %s",
                targetGroupEventContext.groupId));
        disconnect();

        // make a user have a private group and be owner
        ExperimenterGroup privateGroup = newGroupAddUser("rw----",
                targetGroupEventContext.userId, true);

        System.out.println(String.format("Private Group: %s", privateGroup
                .getId().getValue()));

        // add an image to this group with an ROI by this user
        Image image = mmFactory.createImageWithRoi();
        Image returnedImage = (Image) iUpdate.saveAndReturnObject(image);

        // move the image and roi to the read-write shared group

        // Create commands to move and create the link in target
        Chgrp changeGroup = new Chgrp(DeleteServiceTest.REF_IMAGE,
                returnedImage.getId().getValue(), null,
                targetGroupEventContext.groupId);

        // move the image to the shared rwrw group
        doChange(changeGroup);

        disconnect();

        // the image should no longer be in this group
        ParametersI param = new ParametersI();
        param.addId(returnedImage.getId().getValue());
        String sql = "select i from Image where i.id = :id";

        assertNull(iQuery.findByQuery(sql, param));
    }

    private EventContext createPrivateGroup() throws Exception {
        String privateGroupPermissions = "rw----";
        return newUserAndGroup(privateGroupPermissions);
    }

    @Test
    public void moveImageRWtoRWRW() throws Exception {
        EventContext privateGroupContext = createPrivateGroup();
        ExperimenterGroup readWriteGroup = createReadWriteGroupWithUser(privateGroupContext.userId);
        iAdmin.getEventContext();

        Image image = createSimpleImage();

        Roi serverROI = createSimpleRoiFor(image);

        List<Long> shapeIds = new ArrayList<Long>();

        for (int i = 0; i < serverROI.sizeOfShapes(); i++) {
            Shape shape = serverROI.getShape(i);
            shapeIds.add(shape.getId().getValue());
        }

        // Move the image.
        doChange(new Chgrp(DeleteServiceTest.REF_IMAGE, image.getId()
                .getValue(), null, readWriteGroup.getId().getValue()));

        // check if the objects have been delete.

        ParametersI param = new ParametersI();
        param.addId(serverROI.getId().getValue());
        String sql = "select d from Roi as d where d.id = :id";
        assertNull(iQuery.findByQuery(sql, param));

        // shapes
        param = new ParametersI();
        param.addIds(shapeIds);
        sql = "select d from Shape as d where d.id in (:ids)";
        List<IObject> results = iQuery.findAllByQuery(sql, param);

        assertEquals(0, results.size());

        // Check that the data moved
        loginUser(readWriteGroup);
        param = new ParametersI();
        param.addId(serverROI.getId().getValue());
        sql = "select d from Roi as d where d.id = :id";

        assertNotNull(iQuery.findByQuery(sql, param));

        // shapes
        param = new ParametersI();
        param.addIds(shapeIds);
        sql = "select d from Shape as d where d.id in (:ids)";
        results = iQuery.findAllByQuery(sql, param);

        assertTrue(results.size() > 0);
    }

    private ExperimenterGroup createReadWriteGroupWithUser(long userId)
            throws Exception {
        ExperimenterGroup readWriteGroup = newGroupAddUser("rwrw--", userId);
        return readWriteGroup;
    }

    private Roi createSimpleRoiFor(Image image) throws ServerError {
        Roi roi = new RoiI();
        roi.setImage(image);
        Rect rect;
        Roi serverROI = (Roi) iUpdate.saveAndReturnObject(roi);
        for (int i = 0; i < 3; i++) {
            rect = new RectI();
            rect.setX(rdouble(10));
            rect.setY(rdouble(10));
            rect.setWidth(rdouble(10));
            rect.setHeight(rdouble(10));
            rect.setTheZ(rint(i));
            rect.setTheT(rint(0));
            serverROI.addShape(rect);
        }
        serverROI = (RoiI) iUpdate.saveAndReturnObject(serverROI);
        return serverROI;
    }

    private Image createSimpleImage() throws ServerError {
        Image image = (Image) iUpdate.saveAndReturnObject(mmFactory
                .simpleImage(0));
        return image;
    }
}
