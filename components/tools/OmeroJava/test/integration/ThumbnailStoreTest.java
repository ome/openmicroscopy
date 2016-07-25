/*
 * integration.ThumbnailStoreTest
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2013 University of Dundee. All rights reserved.
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
package integration;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import ome.formats.OMEROMetadataStoreClient;
import omero.api.IRenderingSettingsPrx;
import omero.api.ThumbnailStorePrx;
import omero.model.Pixels;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Collections of tests for the <code>ThumbnailStore</code> service.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:donald@lifesci.dundee.ac.uk"
 *         >donald@lifesci.dundee.ac.uk</a>
 * @version 3.0 <small> (<b>Internal version:</b> $Revision: $Date: $) </small>
 * @since 3.0-Beta4
 */
public class ThumbnailStoreTest extends AbstractServerTest {

    /** Reference to the importer store. */
    private OMEROMetadataStoreClient importer;

    /**
     * Overridden to initialize the list.
     *
     * @see AbstractServerTest#setUp()
     */
    @Override
    @BeforeClass
    protected void setUp() throws Exception {
        super.setUp();
        importer = new OMEROMetadataStoreClient();
        importer.initialize(factory);
    }

    /**
     * Test to retrieve the newly created image. Tests thumbnailService methods:
     * getThumbnail(rint, rint) and getThumbnailByLongestSide(rint)
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testGetThumbnail() throws Exception {
        ThumbnailStorePrx svc = factory.createThumbnailStore();
        // first import an image already tested see ImporterTest
        String format = ModelMockFactory.FORMATS[0];
        File f = File.createTempFile("testImportGraphicsImages" + format, "."
                + format);
        mmFactory.createImageFile(f, format);
        List<Pixels> pixels = null;
        try {
            pixels = importFile(importer, f, format, false);
        } catch (Throwable e) {
            throw new Exception("cannot import image", e);
        }
        f.delete();
        Pixels p = pixels.get(0);
        long pixelsID = p.getId().getValue();
        if (!(svc.setPixelsId(pixelsID))) {
            svc.resetDefaults();
            svc.setPixelsId(pixelsID);
        }
        int sizeX = 48;
        int sizeY = 48;
        byte[] values = svc.getThumbnail(omero.rtypes.rint(sizeX),
                omero.rtypes.rint(sizeY));
        Assert.assertNotNull(values);
        Assert.assertTrue(values.length > 0);

        byte[] lsValues = svc.getThumbnailByLongestSide(omero.rtypes
                .rint(sizeX));
        Assert.assertNotNull(lsValues);
        Assert.assertTrue(lsValues.length > 0);
        svc.close();
    }

    /**
     * Tests thumbnailService methods: getThumbnailSet(rint, rint, list<long>)
     * and getThumbnailByLongestSideSet(rint, list<long>)
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testGetThumbnailSet() throws Exception {
        ThumbnailStorePrx svc = factory.createThumbnailStore();
        // first import an image already tested see ImporterTest
        String format = ModelMockFactory.FORMATS[0];
        File f = File.createTempFile("testImportGraphicsImages" + format, "."
                + format);
        mmFactory.createImageFile(f, format);
        List<Long> pixelsIds = new ArrayList<Long>();
        int thumbNailCount = 20;
        try {
            for (int i = 0; i < thumbNailCount; i++) {
                List<Pixels> pxls = importFile(importer, f, format, false);
                pixelsIds.add(pxls.get(0).getId().getValue());
            }
        } catch (Throwable e) {
            throw new Exception("cannot import image", e);
        }
        f.delete();

        int sizeX = 48;
        int sizeY = 48;
        Map<Long, byte[]> thmbs = svc.getThumbnailSet(omero.rtypes.rint(sizeX),
                omero.rtypes.rint(sizeY), pixelsIds);
        Map<Long, byte[]> lsThmbs = svc.getThumbnailByLongestSideSet(
                omero.rtypes.rint(sizeX), pixelsIds);
        Iterator<byte[]> it = thmbs.values().iterator();
        byte[] t = null;
        int tnCount = 0;
        while (it.hasNext()) {
            t = it.next();
            Assert.assertNotNull(t);
            Assert.assertTrue(t.length > 0);
            tnCount++;
        }
        Assert.assertEquals(thumbNailCount, tnCount);

        it = lsThmbs.values().iterator();
        tnCount = 0;
        while (it.hasNext()) {
            t = it.next();
            Assert.assertNotNull(t);
            Assert.assertTrue(t.length > 0);
            tnCount++;
        }
        Assert.assertEquals(thumbNailCount, tnCount);
        svc.close();
    }

    /**
     * Test to retrieve the thumbnails for images. Load the thumbnails, reset
     * the rendering settings then reload the rendering settings again. Tests
     * thumbnailService methods: getThumbnail(rint, rint) and
     * getThumbnailByLongestSide(rint)
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testGetThumbnailAfterReset() throws Exception {
        ThumbnailStorePrx svc = factory.createThumbnailStore();
        // first import an image already tested see ImporterTest
        String format = ModelMockFactory.FORMATS[0];
        List<Pixels> all = new ArrayList<Pixels>();
        List<Pixels> pixels = null;
        int n = 2;
        File f;
        for (int i = 0; i < n; i++) {
            f = File.createTempFile("testImportGraphicsImages_" + i + format,
                    "." + format);
            mmFactory.createImageFile(f, format);
            try {
                pixels = importFile(importer, f, format, false);
                all.addAll(pixels);
            } catch (Throwable e) {
                throw new Exception("cannot import image", e);
            }
            f.delete();
        }
        Iterator<Pixels> i = all.iterator();
        long id;
        int sizeX = 96;
        int sizeY = 96;
        byte[] values;
        List<Long> ids = new ArrayList<Long>(pixels.size());
        while (i.hasNext()) {
            id = i.next().getId().getValue();
            ids.add(id);
            if (!(svc.setPixelsId(id))) {
                svc.resetDefaults();
                svc.setPixelsId(id);
            }
            values = svc.getThumbnail(omero.rtypes.rint(sizeX),
                    omero.rtypes.rint(sizeY));
            Assert.assertNotNull(values);
            Assert.assertTrue(values.length > 0);
        }
        // Reset the rendering settings.
        IRenderingSettingsPrx proxy = factory.getRenderingSettingsService();
        proxy.resetDefaultsInSet(Pixels.class.getName(), ids);
        i = all.iterator();
        // Retrieve the thumbnails. They need to be created again.
        while (i.hasNext()) {
            id = i.next().getId().getValue();
            ids.add(id);
            if (!(svc.setPixelsId(id))) {
                svc.resetDefaults();
                svc.setPixelsId(id);
            }
            values = svc.getThumbnail(omero.rtypes.rint(sizeX),
                    omero.rtypes.rint(sizeY));
            Assert.assertNotNull(values);
            Assert.assertTrue(values.length > 0);
        }
        svc.close();
    }
}
