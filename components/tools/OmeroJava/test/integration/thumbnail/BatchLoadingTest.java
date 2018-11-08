/*
 * Copyright (C) 2018 University of Dundee & Open Microscopy Environment.
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package integration.thumbnail;

import com.google.common.collect.ImmutableList;

import integration.AbstractServerTest;
import integration.ModelMockFactory;
import ome.formats.OMEROMetadataStoreClient;
import omero.api.ThumbnailStorePrx;
import omero.model.Pixels;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class BatchLoadingTest extends AbstractServerTest {

    /**
     * Reference to the importer store.
     */
    private OMEROMetadataStoreClient importer;

    /**
     * Set up a new user in a new group and set the local {@code importer} field.
     *
     * @throws Exception unexpected
     */
    @BeforeMethod
    protected void setUpNewUserWithImporter() throws Exception {
        newUserAndGroup("rwr-r-");
        importer = new OMEROMetadataStoreClient();
        importer.initialize(factory);
    }

    /**
     * Tests thumbnailService methods: getThumbnailSet(rint, rint, list<long>)
     * and getThumbnailByLongestSideSet(rint, list<long>)
     *
     * @throws Exception Thrown if an error occurred.
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
                List<Pixels> pxls = importFile(importer, f, format);
                pixelsIds.add(pxls.get(0).getId().getValue());
            }
        } catch (Throwable e) {
            throw new Exception("cannot import image", e);
        }
        f.deleteOnExit();

        int sizeX = 48;
        int sizeY = 48;
        Map<Long, byte[]> thmbs = svc.getThumbnailSet(omero.rtypes.rint(sizeX),
                omero.rtypes.rint(sizeY), pixelsIds);
        Map<Long, byte[]> lsThmbs = svc.getThumbnailByLongestSideSet(
                omero.rtypes.rint(sizeX), pixelsIds);
        Iterator<byte[]> it = thmbs.values().iterator();
        int tnCount = 0;
        while (it.hasNext()) {
            Utils.checkSize(it.next(), sizeX, sizeY);
            tnCount++;
        }
        Assert.assertEquals(thumbNailCount, tnCount);

        it = lsThmbs.values().iterator();
        tnCount = 0;
        while (it.hasNext()) {
            Utils.checkSize(it.next(), sizeX, sizeY);
            tnCount++;
        }
        Assert.assertEquals(thumbNailCount, tnCount);
        svc.close();
    }


    /**
     * Test that thumbnails can be retrieved from multiple groups at once.
     *
     * @throws Throwable unexpected
     */
    @Test
    public void testGetThumbnailsMultipleGroups() throws Throwable {
        final byte[] thumbnail;
        final long pixelsIdα, pixelsIdβ;
        ThumbnailStorePrx svc = null;

        /* create a fake image file */
        final File file = File.createTempFile(getClass().getSimpleName(), ".fake");
        file.deleteOnExit();

        try {
            /* import the image as one user in one group and get its thumbnail */
            pixelsIdα = importFile(importer, file, "fake").get(0).getId().getValue();
            svc = factory.createThumbnailStore();
            svc.setPixelsId(pixelsIdα);
            thumbnail = svc.getThumbnailByLongestSide(null);
        } finally {
            if (svc != null) {
                {
                    svc.close();
                    svc = null;
                }
            }
        }

        /* import the image as another user in another group */
        setUpNewUserWithImporter();
        pixelsIdβ = importFile(importer, file, "fake").get(0).getId().getValue();

        final Map<Long, byte[]> thumbnails;

        try {
            /* use all-groups context to fetch both thumbnails at once */
            final List<Long> pixelsIdsαβ = ImmutableList.of(pixelsIdα, pixelsIdβ);
            svc = factory.createThumbnailStore();
            thumbnails = svc.getThumbnailByLongestSideSet(null, pixelsIdsαβ, ALL_GROUPS_CONTEXT);
        } finally {
            if (svc != null) {
                {
                    svc.close();
                    svc = null;
                }
            }
        }

        /* check that the thumbnails are as expected */
        Utils.checkSize(thumbnail, 48, 48);
        Assert.assertEquals(thumbnails.get(pixelsIdα), thumbnail);
        Assert.assertEquals(thumbnails.get(pixelsIdβ), thumbnail);
    }
}
