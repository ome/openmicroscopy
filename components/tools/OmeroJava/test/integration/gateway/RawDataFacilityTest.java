/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2015-2021 University of Dundee. All rights reserved.
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

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Executors;

import ome.formats.OMEROMetadataStoreClient;
import ome.formats.importer.IObservable;
import ome.formats.importer.IObserver;
import ome.formats.importer.ImportCandidates;
import ome.formats.importer.ImportConfig;
import ome.formats.importer.ImportEvent;
import ome.formats.importer.ImportLibrary;
import ome.formats.importer.OMEROWrapper;
import omero.api.IPixelsPrx;
import omero.api.RawPixelsStorePrx;
import omero.api.ResolutionDescription;
import omero.gateway.exception.DSAccessException;
import omero.gateway.exception.DSOutOfServiceException;
import omero.gateway.exception.DataSourceException;
import omero.gateway.model.PixelsData;
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
    public void testGetPlane() throws DataSourceException, DSOutOfServiceException, DSAccessException {
        ImageData img = browseFacility.getImage(rootCtx, imgId);
        Plane2D plane = rawdataFacility.getPlane(rootCtx, img.getDefaultPixels(), 0, 0, 0);
        byte[] planeData = new byte[100*100];
        for(int i=0; i<10000; i++)
            planeData[i] = plane.getRawValue(i);
        
        Assert.assertEquals(planeData, rawData);
    }
    
    @Test
    public void testGetPixelValues() throws DataSourceException,
            DSOutOfServiceException, DSAccessException {
        ImageData img = browseFacility.getImage(rootCtx, imgId);
        Plane2D plane = rawdataFacility.getPlane(rootCtx,
                img.getDefaultPixels(), 0, 0, 0);
        double[][] pixelData = new double[100][100];
        double[][] expPixelData = new double[100][100];
        for (int i = 0; i < 10000; i++) {
            int x = i % 100;
            int y = i / 100;
            pixelData[x][y] = plane.getPixelValue(x, y);
            expPixelData[x][y] = (double) Byte.toUnsignedInt(plane
                    .getRawValue(y * 100 + x));
        }
        Assert.assertTrue(Arrays.deepEquals(pixelData, expPixelData));
        Assert.assertTrue(Arrays.deepEquals(plane.getPixelValues(), expPixelData));
    }
    
    @Test
    public void testGetTile() throws DataSourceException, DSOutOfServiceException, DSAccessException {
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

    @Test
    public void testGetTileResolutions() throws Throwable {
        PixelsData pix = importImageFile();
        Assert.assertTrue(pix.getId() > -1);
        List<ResolutionDescription> res = rawdataFacility.getResolutionDescriptions(rootCtx,pix);
        Assert.assertEquals(res.size(), 5);
        int size = 6000;
        for (int i=0; i<res.size(); i++) {
            ResolutionDescription des = res.get(i);
            Assert.assertEquals(des.sizeX, size);
            rawdataFacility.getTile(rootCtx, pix, 0,0, 0, 0, 0, 100, 100, i);
            size /= 2;
        }
    }
    
    @Test
    public void testGetHistogram() throws DataSourceException,
            DSOutOfServiceException, DSAccessException {
        ImageData img = browseFacility.getImage(rootCtx, imgId);

        int[] exp = new int[256];
        for (byte b : rawData) {
            int bin = ((int) b) & 0xFF;
            exp[bin]++;
        }

        Map<Integer, int[]> histo = rawdataFacility.getHistogram(rootCtx,
                img.getDefaultPixels(), new int[] { 0 }, 0, 0);
        int[] data = histo.entrySet().iterator().next().getValue();
        Assert.assertEquals(data.length, 256);

        for (int i = 0; i < 256; i++) {
            Assert.assertEquals(data[i], exp[i]);
        }
    }
    
    private void initData() throws Exception {
        ProjectData p = createProject(rootCtx);
        DatasetData d = createDataset(rootCtx, p);

        String name = UUID.randomUUID().toString();
        IPixelsPrx svc = gw.getPixelsService(rootCtx);
        List<IObject> types = gw.getTypesService(rootCtx)
                .allEnumerations(PixelsType.class.getName());
        PixelsType type = (PixelsType) types.get(2); // unit8
        List<Integer> channels = new ArrayList<Integer>();
        for (int i = 0; i < 3; i++) {
            channels.add(i);
        }
        imgId = svc.createImage(100, 100, 1, 1, channels,
                type, name, "").getValue();

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
        for (int i = 0; i < rawData.length; i++) {
            int r = rand.nextInt(256);
            rawData[i] = (byte) r;
        }
        store.setPlane(rawData, 0, 0, 0);
        gw.closeService(rootCtx, store);
    }

    private PixelsData importImageFile()
            throws Throwable
    {
        OMEROMetadataStoreClient importer = gw.getImportStore(rootCtx);
        ImportConfig config = new ImportConfig();
        config.doThumbnails.set(true);
        ImportLibrary library = new ImportLibrary(importer, new OMEROWrapper(
                config));

        File f = File.createTempFile("testImage",
                "&sizeX=6000&sizeY=6000&sizeZ=1&sizeT=1&resolutions=5.fake");
        f.deleteOnExit();
        OMEROWrapper reader = new OMEROWrapper(config);
        String[] paths = new String[1];
        paths[0] = f.getAbsolutePath();
        IObserver o = new IObserver() {
            public void update(IObservable importLibrary, ImportEvent event) {}
        };
        ImportCandidates candidates = new ImportCandidates(reader, paths, o);
        return new PixelsData(library.importImage(candidates.getContainers().get(0),
                Executors.newSingleThreadExecutor(), 0).iterator().next());
    }
}
