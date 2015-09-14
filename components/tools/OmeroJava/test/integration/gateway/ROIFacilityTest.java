/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2015 University of Dundee. All rights reserved.
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
package integration.gateway;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import junit.framework.Assert;
import omero.api.IPixelsPrx;
import omero.gateway.exception.DSAccessException;
import omero.gateway.exception.DSOutOfServiceException;
import omero.gateway.facility.BrowseFacility;
import omero.gateway.facility.ROIFacility;
import omero.gateway.model.ROIResult;
import omero.model.IObject;
import omero.model.PixelsType;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import omero.gateway.model.ImageData;
import omero.gateway.model.ROIData;
import omero.gateway.model.RectangleData;

/**
 *
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 * @since 5.1
 */

public class ROIFacilityTest extends GatewayTest {

    private ImageData img;
    private ROIFacility roifac;
    private Collection<ROIData> rois;

    @Override
    @BeforeClass(alwaysRun = true)
    protected void setUp() throws Exception {
        super.setUp();
        initData();
        roifac = gw.getFacility(ROIFacility.class);
    }

    @Test
    public void testSaveROIs() throws DSOutOfServiceException,
            DSAccessException {
        rois = new ArrayList<ROIData>();
        rois.add(createRectangleROI(0, 0, 10, 10));
        rois.add(createRectangleROI(11, 11, 10, 10));
        rois = roifac.saveROIs(rootCtx, img.getId(), rois);

        Assert.assertEquals(rois.size(), 2);
        for (ROIData roi : rois) {
            Assert.assertTrue("ROI doesn't have an ID!", roi.getId() >= 0);
        }
    }

    private ROIData createRectangleROI(int x, int y, int w, int h) {
        ROIData roiData = new ROIData();
        RectangleData rectangle = new RectangleData(x, y, w, h);
        rectangle.setVisible(true);
        roiData.addShapeData(rectangle);
        return roiData;
    }

    @Test(dependsOnMethods = { "testSaveROIs" })
    public void testLoadROIs() throws DSOutOfServiceException,
            DSAccessException {
        List<ROIResult> roiResults = roifac.loadROIs(rootCtx, img.getId());
        List<ROIData> myRois = new ArrayList<ROIData>();
        for (ROIResult r : roiResults) {
            Collection c = r.getROIs();
            for (Object o : c)
                myRois.add((ROIData) o);
        }

        Assert.assertEquals(rois.size(), myRois.size());

        Iterator<ROIData> it = myRois.iterator();
        while (it.hasNext()) {
            ROIData r = it.next();
            for (ROIData r2 : rois) {
                if (r2.getId() == r.getId())
                    it.remove();
            }
        }

        Assert.assertTrue("Loaded ROIs don't match saved ROIs!",
                myRois.isEmpty());
    }

    private void initData() throws Exception {
        String name = UUID.randomUUID().toString();
        IPixelsPrx svc = gw.getPixelsService(rootCtx);
        List<IObject> types = svc
                .getAllEnumerations(PixelsType.class.getName());
        List<Integer> channels = new ArrayList<Integer>();
        for (int i = 0; i < 3; i++) {
            channels.add(i);
        }
        long imgId = svc.createImage(100, 100, 1, 1, channels,
                (PixelsType) types.get(1), name, "").getValue();

        img = gw.getFacility(BrowseFacility.class).getImage(rootCtx, imgId);

    }
}
