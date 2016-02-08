/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2015-2016 University of Dundee. All rights reserved.
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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import junit.framework.Assert;
import omero.api.IPixelsPrx;
import omero.gateway.SecurityContext;
import omero.gateway.exception.DSAccessException;
import omero.gateway.exception.DSOutOfServiceException;
import omero.gateway.facility.BrowseFacility;
import omero.gateway.facility.ROIFacility;
import omero.gateway.model.ROIResult;
import omero.model.Folder;
import omero.model.IObject;
import omero.model.PixelsType;
import omero.model.Roi;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import omero.gateway.model.FolderData;
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
    
    private ImageData folderImg;
    private FolderData folder;
    private Set<Long> folderRoiIds;

    @Override
    @BeforeClass(alwaysRun = true)
    protected void setUp() throws Exception {
        super.setUp();
        roifac = gw.getFacility(ROIFacility.class);
        initData();
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
        roiData.addShapeData(rectangle);
        return roiData;
    }

    @Test(dependsOnMethods = { "testSaveROIs" })
    public void testLoadROIs() throws DSOutOfServiceException,
            DSAccessException {
        List<ROIResult> roiResults = roifac.loadROIs(rootCtx, img.getId());
        List<ROIData> myRois = new ArrayList<ROIData>();
        for (ROIResult r : roiResults) {
            myRois.addAll(r.getROIs());
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

    @Test
    public void testGetROIFolders() throws DSOutOfServiceException,
            DSAccessException {
        Collection<FolderData> folders = roifac.getROIFolders(rootCtx,
                folderImg.getId());
        Assert.assertEquals(1, folders.size());
        Assert.assertEquals(folder.getId(), folders.iterator().next().getId());
    }

    @Test(dependsOnMethods = { "testGetROIFolders" })
    public void testLoadRoisForFolder() throws DSOutOfServiceException,
            DSAccessException {
        Collection<FolderData> folders = roifac.getROIFolders(rootCtx,
                folderImg.getId());
        FolderData folder = folders.iterator().next();

        Collection<ROIResult> roiResults = roifac.loadROIsForFolder(rootCtx,
                folderImg.getId(), folder.getId());

        Collection<ROIData> rois = new ArrayList<ROIData>();
        for (ROIResult r : roiResults)
            for (ROIData rd : r.getROIs())
                rois.add(rd);
        Assert.assertEquals(2, rois.size());

        Iterator<ROIData> it = rois.iterator();
        while (it.hasNext()) {
            ROIData r = it.next();
            if (folderRoiIds.contains(r.getId()))
                it.remove();
        }
        Assert.assertTrue(rois.isEmpty());
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
        
        long folderImgId = svc.createImage(100, 100, 1, 1, channels,
                (PixelsType) types.get(1), name, "").getValue();
        folderImg = gw.getFacility(BrowseFacility.class).getImage(rootCtx,
                folderImgId);
        ROIData folderRoi1 = createRectangleROI(5, 5, 10, 10);
        ROIData folderRoi2 = createRectangleROI(10, 10, 10, 10);
        Collection<ROIData> rois = new ArrayList<ROIData>(2);
        rois.add(folderRoi1);
        rois.add(folderRoi2);
        rois = roifac.saveROIs(rootCtx, folderImg.getId(), rois);
        folderRoiIds = new HashSet<Long>();
        for (ROIData d : rois)
            folderRoiIds.add(d.getId());
        folder = createRoiFolder(rootCtx, rois);
    }
    
    private FolderData createRoiFolder(SecurityContext ctx,
            Collection<ROIData> rois) throws DSOutOfServiceException,
            DSAccessException {
        FolderData folder = new FolderData();
        folder.setName(UUID.randomUUID().toString());
        Folder f = folder.asFolder();
        for (ROIData roi : rois)
            f.linkRoi((Roi) roi.asIObject());
        return (FolderData) datamanagerFacility
                .saveAndReturnObject(ctx, folder);
    }
}
