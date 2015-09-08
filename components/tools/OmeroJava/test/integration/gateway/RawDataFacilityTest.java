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
import java.util.List;
import java.util.Random;
import java.util.UUID;

import omero.api.IPixelsPrx;
import omero.api.RawPixelsStorePrx;
import omero.gateway.exception.DataSourceException;
import omero.gateway.rnd.Plane2D;
import omero.model.IObject;
import omero.model.PixelsType;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import omero.gateway.model.DatasetData;
import omero.gateway.model.ImageData;
import omero.gateway.model.ProjectData;


/**
 *
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 * @since 5.1
 */

public class RawDataFacilityTest extends GatewayTest {

    private long imgId;
    private byte[] rawData;
    
    @Override
    @BeforeClass(alwaysRun = true)
    protected void setUp() throws Exception {
        super.setUp();
        initData();
    }

    @Test
    public void testGetPlane() throws DataSourceException {
        ImageData img = browseFacility.getImage(rootCtx, imgId);
        Plane2D plane = rawdataFacility.getPlane(rootCtx, img.getDefaultPixels(), 0, 0, 0);
        byte[] planeData = new byte[100*100];
        for(int i=0; i<10000; i++)
            planeData[i] = plane.getRawValue(i);
        
        Assert.assertEquals(planeData, rawData);
    }
    
    @Test
    public void testGetTile() throws DataSourceException {
        ImageData img = browseFacility.getImage(rootCtx, imgId);
        int x = 0, y=0, w=img.getDefaultPixels().getSizeX(), h=1;
        
        // get the first pixel row of the image as "tile"
        Plane2D plane = rawdataFacility.getTile(rootCtx, img.getDefaultPixels(), 0, 0, 0, x, y, w, h);
        byte[] planeData = new byte[w];
        for(int i=0; i<w; i++)
            planeData[i] = plane.getRawValue(i);
        
        byte[] rawDataPart = new byte[w];
        System.arraycopy(rawData, 0, rawDataPart, 0, w);
        
        Assert.assertEquals(planeData, rawDataPart);
    }
    
    private void initData() throws Exception {
        ProjectData p = createProject(rootCtx);
        DatasetData d = createDataset(rootCtx, p);

        String name = UUID.randomUUID().toString();
        IPixelsPrx svc = gw.getPixelsService(rootCtx);
        List<IObject> types = svc
                .getAllEnumerations(PixelsType.class.getName());
        List<Integer> channels = new ArrayList<Integer>();
        for (int i = 0; i < 3; i++) {
            channels.add(i);
        }
        imgId = svc.createImage(100, 100, 1, 1, channels,
                (PixelsType) types.get(1), name, "").getValue();

        List<Long> ids = new ArrayList<Long>(1);
        ids.add(imgId);
        ImageData img = browseFacility.getImages(rootCtx, ids).iterator()
                .next();

        List<ImageData> l = new ArrayList<ImageData>(1);
        l.add(img);
        datamanagerFacility.addImagesToDataset(rootCtx, l, d);

        ids.clear();
        ids.add(d.getId());
        d = browseFacility.getDatasets(rootCtx, ids).iterator().next();

        RawPixelsStorePrx store = gw.createPixelsStore(rootCtx);
        store.setPixelsId(img.getDefaultPixels().getId(), false);
        Random rand = new Random();
        rawData = new byte[100 * 100];
        rand.nextBytes(rawData);
        store.setPlane(rawData, 0, 0, 0);
        gw.closeService(rootCtx, store);
    }
}
