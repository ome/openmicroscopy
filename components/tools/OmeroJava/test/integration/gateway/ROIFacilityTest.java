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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import junit.framework.Assert;
import omero.api.IPixelsPrx;
import omero.cmd.CmdCallbackI;
import omero.gateway.SecurityContext;
import omero.gateway.exception.DSAccessException;
import omero.gateway.exception.DSOutOfServiceException;
import omero.gateway.facility.BrowseFacility;
import omero.gateway.facility.ROIFacility;
import omero.gateway.model.FolderData;
import omero.gateway.model.ImageData;
import omero.gateway.model.ROIData;
import omero.gateway.model.RectangleData;
import omero.gateway.util.Pojos;
import omero.model.Folder;
import omero.model.IObject;
import omero.model.PixelsType;
import omero.model.Roi;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 * @since 5.1
 */

public class ROIFacilityTest extends GatewayTest {

    private ROIFacility roifac;
    private BrowseFacility browse;
    private Collection<ROIData> rois;

    private ImageData img;
    private FolderData folder;
    private ROIData folderImageROI, folderROI, imageROI, orphanedROI;

    private FolderData addRemoveFolder;
    private Collection<ROIData> addRemoveFolderROIs;

    @Override
    @BeforeClass(alwaysRun = true)
    protected void setUp() throws Exception {
        super.setUp();
        roifac = gw.getFacility(ROIFacility.class);
        browse = gw.getFacility(BrowseFacility.class);
        initData();
    }

    // The tests should run in the order they appear here, hence
    // the dependsOnMethods annotations on successive test methods

    @Test
    public void testSaveROIs() throws Exception {
        rois = new ArrayList<ROIData>();
        rois.add(createRectangleROI(0, 0, 10, 10));
        rois.add(createRectangleROI(11, 11, 10, 10));
        rois = roifac.saveROIs(rootCtx, img.getId(), rois);

        Assert.assertEquals(rois.size(), 2);
        for (ROIData roi : rois) {
            Assert.assertTrue("ROI doesn't have an ID!", roi.getId() >= 0);
        }
        
        for (ROIData r : rois) {
           CmdCallbackI cb = datamanagerFacility.delete(rootCtx, r.asIObject());
           cb.block(10000);
        }
    }
    
    @Test(dependsOnMethods = { "testSaveROIs" })
    public void testGetROIFolders() throws DSOutOfServiceException,
            DSAccessException {
        Collection<FolderData> folders = roifac.getROIFolders(rootCtx,
                img.getId());
        Assert.assertEquals(1, folders.size());
        Assert.assertEquals(folder.getId(), folders.iterator().next().getId());
    }
    
    @Test(dependsOnMethods = { "testGetROIFolders" })
    public void testLoadROIs() throws Exception {
        
        // all rois of the image
        Collection<ROIData> rois = roifac.loadROIs(rootCtx, img.getId(), null);
        Assert.assertEquals(2, rois.size());
        Iterator<ROIData> it = rois.iterator();
        while (it.hasNext()) {
            ROIData roi = it.next();
            if (roi.getId() == folderImageROI.getId()
                    || roi.getId() == imageROI.getId())
                it.remove();
        }
        Assert.assertTrue(rois.isEmpty());

        // image rois not in folder
        rois = roifac.loadROIs(rootCtx, img.getId(), Collections.EMPTY_LIST);
        Assert.assertEquals(1, rois.size());
        it = rois.iterator();
        while (it.hasNext()) {
            ROIData roi = it.next();
            if (roi.getId() == imageROI.getId())
                it.remove();
        }
        Assert.assertTrue(rois.isEmpty());

        // image rois within folder
        rois = roifac.loadROIs(rootCtx, img.getId(),
                Collections.singleton(folder.getId()));
        Assert.assertEquals(1, rois.size());
        it = rois.iterator();
        while (it.hasNext()) {
            ROIData roi = it.next();
            if (roi.getId() == folderImageROI.getId())
                it.remove();
        }
        Assert.assertTrue(rois.isEmpty());

        // all rois of folder
        rois = roifac.loadROIs(rootCtx, null,
                Collections.singleton(folder.getId()));
        Assert.assertEquals(2, rois.size());
        it = rois.iterator();
        while (it.hasNext()) {
            ROIData roi = it.next();
            if (roi.getId() == folderROI.getId()
                    || roi.getId() == folderImageROI.getId())
                it.remove();
        }
        Assert.assertTrue(rois.isEmpty());

        // all rois of folder without image
        rois = roifac.loadROIs(rootCtx, -1L,
                Collections.singleton(folder.getId()));
        Assert.assertEquals(1, rois.size());
        it = rois.iterator();
        while (it.hasNext()) {
            ROIData roi = it.next();
            if (roi.getId() == folderROI.getId())
                it.remove();
        }
        Assert.assertTrue(rois.isEmpty());

        // rois without folder and without image
        rois = roifac.loadROIs(rootCtx, -1L, Collections.EMPTY_LIST);
        Assert.assertEquals(1, rois.size());
        it = rois.iterator();
        while (it.hasNext()) {
            ROIData roi = it.next();
            if (roi.getId() == orphanedROI.getId())
                it.remove();
        }
        Assert.assertTrue(rois.isEmpty());

        // all rois
        rois = roifac.loadROIs(rootCtx, null, null);
        Assert.assertEquals(4, rois.size());
        it = rois.iterator();
        while (it.hasNext()) {
            ROIData roi = it.next();
            if (roi.getId() == orphanedROI.getId()
                    || roi.getId() == folderROI.getId()
                    || roi.getId() == folderImageROI.getId()
                    || roi.getId() == imageROI.getId())
                it.remove();
        }
        Assert.assertTrue(rois.isEmpty());
    }

    @Test(dependsOnMethods = { "testLoadROIs" })
    public void testAddROIsToFolder() throws Exception {
        addRemoveFolder = createFolder(rootCtx);

        addRemoveFolderROIs = new ArrayList<ROIData>();
        ROIData r = createRectangleROI(0, 0, 10, 10);
        r = roifac.saveROIs(rootCtx, img.getId(), Collections.singleton(r))
                .iterator().next();
        addRemoveFolderROIs.add(r);
        r = createRectangleROI(15, 15, 10, 10);
        r = roifac.saveROIs(rootCtx, img.getId(), Collections.singleton(r))
                .iterator().next();
        addRemoveFolderROIs.add(r);

        roifac.addRoisToFolders(rootCtx, img.getId(), addRemoveFolderROIs,
                Collections.singletonList(addRemoveFolder));

        addRemoveFolder = browse
                .getFolders(rootCtx,
                        Collections.singletonList(addRemoveFolder.getId()))
                .iterator().next();
        Assert.assertEquals(2, addRemoveFolder.copyROILinks().size());

        Collection<ROIData> loadedRois = roifac.loadROIs(rootCtx, img.getId(),
                Collections.singleton(addRemoveFolder.getId()));
        Assert.assertEquals(addRemoveFolderROIs.size(), loadedRois.size());
        Iterator<ROIData> it = loadedRois.iterator();
        Collection<Long> ids = Pojos.extractIds(addRemoveFolderROIs);
        while (it.hasNext()) {
            ROIData n = it.next();
            if (ids.contains(n.getId()))
                it.remove();
        }
        Assert.assertTrue(loadedRois.isEmpty());
    }
    

    @Test(dependsOnMethods = { "testAddROIsToFolder" })
    public void testRemoveROIsFromFolder() throws Exception {
        addRemoveFolder = browse
                .getFolders(rootCtx,
                        Collections.singletonList(addRemoveFolder.getId()))
                .iterator().next();
        Assert.assertEquals(addRemoveFolderROIs.size(), addRemoveFolder.copyROILinks().size());

        roifac.removeRoisFromFolders(rootCtx, img.getId(), addRemoveFolderROIs,
                Collections.singletonList(addRemoveFolder));

        addRemoveFolder = browse
                .getFolders(rootCtx,
                        Collections.singletonList(addRemoveFolder.getId()))
                .iterator().next();
        Assert.assertTrue(addRemoveFolder.copyROILinks().isEmpty());

        Collection<Long> ids = Pojos.extractIds(addRemoveFolderROIs);
        Collection<ROIData> loadedRois = roifac.loadROIs(rootCtx, ids);
        Assert.assertEquals(addRemoveFolderROIs.size(), loadedRois.size());
        Iterator<ROIData> it = loadedRois.iterator();
        while (it.hasNext()) {
            ROIData n = it.next();
            if (ids.contains(n.getId()))
                it.remove();
        }
        Assert.assertTrue(loadedRois.isEmpty());
    }

    private void clean() throws Exception {
        Collection<ROIData> rois = roifac.loadROIs(rootCtx, null, null);
        for (ROIData r : rois) {
            datamanagerFacility.delete(rootCtx, r.asIObject());
        }

    }

    private void initData() throws Exception {
        clean();

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

        folderImageROI = createRectangleROI(5, 5, 10, 10);
        folderROI = createRectangleROI(15, 15, 10, 10);
        imageROI = createRectangleROI(25, 25, 10, 10);
        orphanedROI = createRectangleROI(35, 35, 10, 10);

        folderImageROI = roifac
                .saveROIs(rootCtx, imgId, Collections.singleton(folderImageROI))
                .iterator().next();
        imageROI = roifac
                .saveROIs(rootCtx, imgId, Collections.singleton(imageROI))
                .iterator().next();
        folderROI = roifac
                .saveROIs(rootCtx, -1, Collections.singleton(folderROI))
                .iterator().next();
        orphanedROI = roifac
                .saveROIs(rootCtx, -1, Collections.singleton(orphanedROI))
                .iterator().next();

        Collection<ROIData> folderROIs = new ArrayList<ROIData>();
        folderROIs.add(folderImageROI);
        folderROIs.add(folderROI);
        folder = createRoiFolder(rootCtx, folderROIs);
    }

    private ROIData createRectangleROI(int x, int y, int w, int h) {
        ROIData roiData = new ROIData();
        RectangleData rectangle = new RectangleData(x, y, w, h);
        roiData.addShapeData(rectangle);
        return roiData;
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
