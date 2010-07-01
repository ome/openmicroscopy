/*
 *   $Id$
 *
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package integration;

import static omero.rtypes.*;
import omero.api.IRoiPrx;
import omero.api.RoiOptions;
import omero.api.RoiResult;
import omero.model.ImageI;
import omero.model.RectI;
import omero.model.RoiI;
import omero.model.Shape;

import org.testng.annotations.Test;

/**
 *
 */
@Test(groups = { "client", "integration", "blitz" })
public class RoisTest extends AbstractTest {

    @Test(groups = "ticket:1679")
    public void testTicket1679() throws Exception {

        IRoiPrx roiService = factory.getRoiService();
        RoiResult serverReturn;

        ImageI image = (ImageI) iUpdate.saveAndReturnObject(simpleImage(0));
        RoiI roi = new RoiI();
        RectI rect = new RectI();
        roi.setImage(image);

        RoiI serverROI;

        serverROI = (RoiI) iUpdate.saveAndReturnObject(roi);
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
        Shape shape = serverROI.getShape(0);
        serverROI.removeShape(shape);
        serverROI = (RoiI) iUpdate.saveAndReturnObject(serverROI);
        serverReturn = roiService.findByImage(image.getId().getValue(),
                new RoiOptions());
    }

}
