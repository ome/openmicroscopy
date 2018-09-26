/*
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2006-2017 University of Dundee. All rights reserved.
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
import ome.formats.importer.ImportConfig;
import omero.ServerError;
import omero.api.IRenderingSettingsPrx;
import omero.api.ThumbnailStorePrx;
import omero.model.Pixels;

import omero.model.RenderingDef;
import omero.sys.EventContext;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableList;

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
     * Set up a new user in a new group and set the local {@code importer} field.
     * @throws Exception unexpected
     */
    @BeforeMethod
    protected void setUpNewUserWithImporter() throws Exception {
        newUserAndGroup("rwr-r-");
        importer = new OMEROMetadataStoreClient();
        importer.initialize(factory);
    }

    public static class SingleThumbnail extends AbstractServerTest {

        /** Reference to the importer store. */
        private OMEROMetadataStoreClient importer;

        private EventContext owner;
        private File importedFile;
        private Pixels pixels;

        @BeforeMethod
        protected void setUpNewUserWithImporter() throws Throwable {
            owner = newUserAndGroup("rwr-r-");
            importer = new OMEROMetadataStoreClient();
            importer.initialize(factory);

            String format = ModelMockFactory.FORMATS[0];
            importedFile = createImageFile(format);
            pixels = importFile(importer, importedFile, format).get(0);
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
            final int sizeX = 48;
            final int sizeY = 48;

            ThumbnailStorePrx svc = getThumbnailStoreForPixels();
            byte[] values = svc.getThumbnail(omero.rtypes.rint(sizeX),
                    omero.rtypes.rint(sizeY));
            Assert.assertNotNull(values);
            Assert.assertTrue(values.length > 0);
            svc.close();
        }

        @Test
        public void testGetThumbnailByLongestSide() throws Exception {
            final int sizeX = 48;

            ThumbnailStorePrx svc = getThumbnailStoreForPixels();
            byte[] lsValues = svc.getThumbnailByLongestSide(omero.rtypes
                    .rint(sizeX));
            Assert.assertNotNull(lsValues);
            Assert.assertTrue(lsValues.length > 0);
            svc.close();
        }

        @Test
        public void testGetThumbnailWithoutDefault() throws Exception {
            final int sizeX = 48;
            final int sizeY = 48;

            ThumbnailStorePrx svc = getThumbnailStoreForPixels();
            byte[] values = svc.getThumbnailWithoutDefault(
                    omero.rtypes.rint(sizeX),
                    omero.rtypes.rint(sizeY));
            Assert.assertNotNull(values);
            Assert.assertTrue(values.length > 0);
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
            final int sizeX = 96;
            final int sizeY = 96;

            ThumbnailStorePrx svc = getThumbnailStoreForPixels();
            byte[] values = svc.getThumbnail(omero.rtypes.rint(sizeX),
                    omero.rtypes.rint(sizeY));
            Assert.assertNotNull(values);
            Assert.assertTrue(values.length > 0);

            final long id = pixels.getId().getValue();

            // Reset rendering settings for pixels object
            resetRenderingSettingsForPixelsObject(id);

            // Set pixel object to thumbnail store
            if (!svc.setPixelsId(id)) {
                svc.resetDefaults();
                svc.setPixelsId(id);
            }

            // Call getThumbnail
            values = svc.getThumbnail(omero.rtypes.rint(sizeX),
                    omero.rtypes.rint(sizeY));
            Assert.assertNotNull(values);
            Assert.assertTrue(values.length > 0);
            svc.close();
        }

        @Test
        public void testGetThumbnailWithoutDefaultAfterReset() throws Exception {
            final int sizeX = 96;
            final int sizeY = 96;

            ThumbnailStorePrx svc = getThumbnailStoreForPixels();
            byte[] values = svc.getThumbnailWithoutDefault(omero.rtypes.rint(sizeX),
                    omero.rtypes.rint(sizeY));
            Assert.assertNotNull(values);
            Assert.assertTrue(values.length > 0);

            final long id = pixels.getId().getValue();

            // Reset rendering settings for pixels object
            resetRenderingSettingsForPixelsObject(id);

            // Set pixel object to thumbnail store
            if (!svc.setPixelsId(id)) {
                svc.resetDefaults();
                svc.setPixelsId(id);
            }

            // Call getThumbnail
            values = svc.getThumbnailWithoutDefault(omero.rtypes.rint(sizeX),
                    omero.rtypes.rint(sizeY));
            Assert.assertNotNull(values);
            Assert.assertTrue(values.length > 0);
            svc.close();
        }

        @Test
        private void testGetThumbnailAsOtherUser() throws Exception {
            final int sizeX = 96;
            final int sizeY = 96;

            // Create new user in group and login as that user
            EventContext newUser = newUserInGroup();
            loginUser(newUser);

            // Get thumbnail
            ThumbnailStorePrx svc = getThumbnailStoreForPixels();
            byte[] values = svc.getThumbnail(
                    omero.rtypes.rint(sizeX),
                    omero.rtypes.rint(sizeY));
            Assert.assertNotNull(values);
            Assert.assertTrue(values.length > 0);
            svc.close();
        }

        @Test
        private void testGetThumbnailWithoutDefaultAsOtherUser() throws Exception {
            final int sizeX = 96;
            final int sizeY = 96;

            // Create new user in group and login as that user
            EventContext newUser = newUserInGroup();
            loginUser(newUser);

            // Get thumbnail
            ThumbnailStorePrx svc = getThumbnailStoreForPixels();
            byte[] values = svc.getThumbnailWithoutDefault(
                    omero.rtypes.rint(sizeX),
                    omero.rtypes.rint(sizeY));
            Assert.assertNotNull(values);
            Assert.assertTrue(values.length > 0);
            svc.close();
        }

        private void resetRenderingSettingsForPixelsObject(long pixelId) throws ServerError {
            // Reset the rendering settings.
            IRenderingSettingsPrx proxy = factory.getRenderingSettingsService();
            RenderingDef settings = proxy.getRenderingSettings(pixelId);
            proxy.resetDefaults(settings, pixels);
        }

        private ThumbnailStorePrx getThumbnailStoreForPixels() throws ServerError {
            ThumbnailStorePrx svc = factory.createThumbnailStore();
            long pixelsID = pixels.getId().getValue();
            if (!svc.setPixelsId(pixelsID)) {
                svc.resetDefaults();
                svc.setPixelsId(pixelsID);
            }
            return svc;
        }

        private File createImageFile(String format) throws Throwable {
            File f = File.createTempFile("testImportGraphicsImages" + format, "."
                    + format);
            mmFactory.createImageFile(f, format);
            return f;
        }
    }


    public static class SingleThumbnailWithImportOptions extends AbstractServerTest {

        /** Reference to the importer store. */
        private EventContext owner;
        private File importedFile;
        private Pixels pixels;

        @BeforeMethod
        protected void setUpNewUserWithImporter() throws Throwable {
            owner = newUserAndGroup("rwr-r-");
            createImporter();
        }

        @Test
        private void testGetThumbnailWithoutRenderingSettingsAsOtherUser() throws Throwable {
            ImportConfig config = new ImportConfig();
            config.doThumbnails.set(false); // skip thumbnails

            // Import image without thumbnails
            pixels = importFile(config);

            // Create new user in group and login as that user
            EventContext newUser = newUserInGroup();
            loginUser(newUser);

            final int sizeX = 96;
            final int sizeY = 96;

            // Try to load image
            ThumbnailStorePrx svc = getThumbnailStoreForPixels(pixels);
            byte[] values = svc.getThumbnailWithoutDefault(omero.rtypes.rint(sizeX),
                    omero.rtypes.rint(sizeY));
            Assert.assertNotNull(values);
            Assert.assertTrue(values.length > 0);
            svc.close();
        }

        private void resetRenderingSettingsForPixelsObject(long pixelId) throws ServerError {
            // Reset the rendering settings.
            IRenderingSettingsPrx proxy = factory.getRenderingSettingsService();
            RenderingDef settings = proxy.getRenderingSettings(pixelId);
            proxy.resetDefaults(settings, pixels);
        }

        private ThumbnailStorePrx getThumbnailStoreForPixels(Pixels pixels) throws ServerError {
            ThumbnailStorePrx svc = factory.createThumbnailStore();
            long pixelsID = pixels.getId().getValue();
            if (!svc.setPixelsId(pixelsID)) {
                svc.resetDefaults();
                svc.setPixelsId(pixelsID);
            }
            return svc;
        }

        private Pixels importFile(ImportConfig config) throws Throwable {
            String format = ModelMockFactory.FORMATS[0];
            File file = createImageFile(format);
            return importFile(config, file, format).get(0);
        }

        private File createImageFile(String format) throws Throwable {
            File f = File.createTempFile("testImportGraphicsImages" + format, "."
                    + format);
            mmFactory.createImageFile(f, format);
            return f;
        }
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
                List<Pixels> pxls = importFile(importer, f, format);
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
     * Test that thumbnails can be retrieved from multiple groups at once.
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
        Assert.assertTrue(thumbnail.length > 0);
        Assert.assertEquals(thumbnails.get(pixelsIdα), thumbnail);
        Assert.assertEquals(thumbnails.get(pixelsIdβ), thumbnail);
    }
}
