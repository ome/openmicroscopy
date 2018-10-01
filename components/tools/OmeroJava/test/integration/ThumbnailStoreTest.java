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
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import ome.formats.OMEROMetadataStoreClient;
import ome.formats.importer.ImportConfig;
import omero.ServerError;
import omero.api.IRenderingSettingsPrx;
import omero.api.RenderingEnginePrx;
import omero.api.ThumbnailStorePrx;
import omero.model.Pixels;
import omero.model.RenderingDef;
import omero.sys.EventContext;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;


/**
 * Collections of tests for the <code>ThumbnailStore</code> service.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 * href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp; <a
 * href="mailto:donald@lifesci.dundee.ac.uk"
 * >donald@lifesci.dundee.ac.uk</a>
 * @version 3.0 <small> (<b>Internal version:</b> $Revision: $Date: $) </small>
 * @since 3.0-Beta4
 */
public class ThumbnailStoreTest extends AbstractServerTest {

    /**
     * Tests utilising single thumbnail loading APIs
     */
    public static class SingleThumbnail extends AbstractServerTest {

        /**
         * Reference to the importer store.
         */
        private OMEROMetadataStoreClient importer;

        private EventContext owner;
        private File importedFile;
        private Pixels pixels;
        private ThumbnailStorePrx svc;

        @BeforeMethod
        public void setUpNewUserWithImporter() throws Throwable {
            owner = newUserAndGroup("rwr-r-");
            importer = new OMEROMetadataStoreClient();
            importer.initialize(factory);

            String format = ModelMockFactory.FORMATS[0];
            importedFile = createImageFile(format);
            pixels = importFile(importer, importedFile, format).get(0);

            svc = factory.createThumbnailStore();
            setThumbnailStoreToPixels(svc, pixels.getId().getValue());
        }

        @AfterMethod
        public void cleanup() throws ServerError {
            svc.close();
        }

        /**
         * Test to retrieve the newly created image. Tests thumbnailService methods:
         * getThumbnail(rint, rint) and getThumbnailByLongestSide(rint)
         *
         * @throws Exception Thrown if an error occurred.
         */
        @Test
        public void testGetThumbnail() throws Exception {
            getThumbnail(svc);
        }

        @Test
        public void testGetThumbnailByLongestSide() throws Exception {
            final int sizeX = 48;
            byte[] lsValues = svc.getThumbnailByLongestSide(omero.rtypes
                    .rint(sizeX));
            Assert.assertNotNull(lsValues);
            Assert.assertTrue(lsValues.length > 0);
        }

        @Test
        public void testGetThumbnailWithoutDefault() throws Exception {
            getThumbnailWithoutDefault(svc);
        }

        /**
         * Test to retrieve the thumbnails for images. Load the thumbnails, reset
         * the rendering settings then reload the rendering settings again. Tests
         * thumbnailService methods: getThumbnail(rint, rint) and
         * getThumbnailByLongestSide(rint)
         *
         * @throws Exception Thrown if an error occurred.
         */
        @Test
        public void testGetThumbnailAfterReset() throws Exception {
            getThumbnail(svc);

            // Reset rendering settings for pixels object
            resetRenderingSettingsForPixelsObject(pixels);

            // Call getThumbnail
            getThumbnail(svc);
        }

        @Test
        public void testGetThumbnailWithoutDefaultAfterReset() throws Exception {
            getThumbnailWithoutDefault(svc);

            // Reset rendering settings for pixels object
            resetRenderingSettingsForPixelsObject(pixels);

            getThumbnailWithoutDefault(svc);
        }

        @Test
        private void testGetThumbnailAsOtherUser() throws Exception {
            // Close current thumbnail store
            svc.close();

            // Create new user in group and login as that user
            EventContext newUser = newUserInGroup();
            loginUser(newUser);

            // Create a new thumbnail store
            svc = factory.createThumbnailStore();

            // Set pixels id to the new thumbnail store
            setThumbnailStoreToPixels(svc, pixels.getId().getValue());

            // Get the thumbnail
            getThumbnail(svc);
        }

        @Test
        private void testGetThumbnailWithoutDefaultAsOtherUser() throws Exception {
            // Close current thumbnail store
            svc.close();

            // Create new user in group and login as that user
            EventContext newUser = newUserInGroup();
            loginUser(newUser);

            // Create a new thumbnail store
            svc = factory.createThumbnailStore();

            // Set pixels id to the new thumbnail store
            setThumbnailStoreToPixels(svc, pixels.getId().getValue());

            // Get the thumbnail
            getThumbnailWithoutDefault(svc);
        }

        private void resetRenderingSettingsForPixelsObject(Pixels pixels) throws ServerError {
            // Reset the rendering settings.
            IRenderingSettingsPrx proxy = factory.getRenderingSettingsService();
            RenderingDef settings = proxy.getRenderingSettings(pixels.getId().getValue());
            proxy.resetDefaults(settings, pixels);
        }

        private File createImageFile(String format) throws Throwable {
            File f = File.createTempFile("testImportGraphicsImages" + format, "."
                    + format);
            mmFactory.createImageFile(f, format);
            return f;
        }
    }


    /**
     * Tests utilising single thumbnail loading APIs by users other than the
     * owner of the image.
     */
    public static class SingleThumbnailMultiUser extends AbstractServerTest {

        @BeforeMethod
        protected void setUpNewUserWithImporter() throws Throwable {
            createImporter();
        }

        @Test(dataProvider = "permissions")
        public void testGetThumbnail(String permissions) throws Throwable {
            // Create two users in same group
            EventContext user1 = newUserAndGroup(permissions);
            loginUser(user1);

            ImportConfig config = new ImportConfig();
            config.doThumbnails.set(false); // skip thumbnails

            // Import image without thumbnails
            Pixels pixels = importFile(config);

            // View image as user 1
            ThumbnailStorePrx svc = factory.createThumbnailStore();
            setThumbnailStoreToPixels(svc, pixels.getId().getValue());
            getThumbnail(svc);
            svc.close();

            // Create new user in group and login as that user
            EventContext user2 = newUserInGroup();
            loginUser(user2);

            // Try to load image
            svc = factory.createThumbnailStore();
            try {
                setThumbnailStoreToPixels(svc, pixels.getId().getValue());
                getThumbnail(svc);
            } catch (omero.ResourceError e) {
                // With permission rw----, the image is private to user 1.
                // Expect this to fail for user 2.
                if (!permissions.equalsIgnoreCase("rw----")) {
                    throw e;
                }
            }
        }

        @Test(dataProvider = "permissions")
        public void testGetThumbnailWithoutDefault(String permissions) throws Throwable {
            // Create two users in same group
            EventContext user1 = newUserAndGroup(permissions);
            loginUser(user1);

            // Obtain image
            ImportConfig config = new ImportConfig();
            config.doThumbnails.set(false); // skip thumbnails

            // Import image without thumbnails
            Pixels pixels = importFile(config);

            // View image as user 1
            ThumbnailStorePrx svc = factory.createThumbnailStore();
            setThumbnailStoreToPixels(svc, pixels.getId().getValue());
            getThumbnailWithoutDefault(svc);
            svc.close();

            // Create new user in group and login as that user
            EventContext user2 = newUserInGroup();
            loginUser(user2);

            // Try to load image
            svc = factory.createThumbnailStore();
            try {
                setThumbnailStoreToPixels(svc, pixels.getId().getValue());
                getThumbnailWithoutDefault(svc);
            } catch (omero.ResourceError e) {
                // With permission rw----, the image is private to user 1.
                // Expect this to fail for user 2.
                if (!permissions.equalsIgnoreCase("rw----")) {
                    throw e;
                }
            }
        }

        /**
         * Test scenario outlined on:
         * https://trello.com/c/itoDPkxB/24-read-only-settings-and-thumbnails-generation
         * <p>
         * 1. User 1 import image and skip thumbnail generation (don't view it)
         * 2. User 2 view the image (create rendering settings)
         * 3. User 1 view the image and change the rendering settings
         * 4. User 2 view load their thumbnail and compare to user 1's thumbnail
         *
         * @throws Throwable
         */
        @Test(dataProvider = "permissions")
        public void testGetThumbnailWithRenderingSettingsChange(String permissions) throws Throwable {
            // Skip this test for rw---- group
            if (permissions.equalsIgnoreCase("rw----")) {
                return;
            }

            EventContext user1 = newUserAndGroup(permissions);
            loginUser(user1);

            ImportConfig config = new ImportConfig();
            config.doThumbnails.set(false); // skip thumbnails

            // Import image without thumbnails
            Pixels pixels = importFile(config);
            final long pixelsId = pixels.getId().getValue();

            // Create new user in group and login as that user
            EventContext user2 = newUserInGroup();
            loginUser(user2);

            // Generate rendering settings for user 2
            RenderingEnginePrx re = factory.createRenderingEngine();
            re.lookupPixels(pixelsId);
            if (!re.lookupRenderingDef(pixelsId)) {
                re.resetDefaultSettings(true);
                re.lookupRenderingDef(pixelsId);
            }
            // re.load();
            re.close();

            // Load thumbnail as user 2 to create thumbnail on disk
            ThumbnailStorePrx svc = factory.createThumbnailStore();
            setThumbnailStoreToPixels(svc, pixelsId);
            getThumbnailWithoutDefault(svc);
            svc.close();

            // Switch to user 1
            loginUser(user1);

            // Load and change to trigger rendering settings and thumbnail creation for user 1
            svc = factory.createThumbnailStore();
            setThumbnailStoreToPixels(svc, pixels.getId().getValue());
            getThumbnailWithoutDefault(svc);

            // Get rendering settings for pixels object as user 1
            re = factory.createRenderingEngine();
            re.lookupPixels(pixelsId);
            if (!re.lookupRenderingDef(pixelsId)) {
                re.resetDefaultSettings(true);
                re.lookupRenderingDef(pixelsId);
            }
            re.load();
            re.setActive(0, false);
            re.saveCurrentSettings();
            re.close();

            // Get thumbnail for user 1
            byte[] user1Thumbnail = getThumbnailWithoutDefault(svc);
            svc.close();

            // Login as user 2 and get their version of the thumbnail
            loginUser(user2);
            svc = factory.createThumbnailStore();
            setThumbnailStoreToPixels(svc, pixels.getId().getValue());
            byte[] user2Thumbnail = getThumbnailWithoutDefault(svc);
            svc.close();

            // Quick check
            Assert.assertEquals(user1Thumbnail.length, user2Thumbnail.length);
            Assert.assertTrue(Arrays.equals(user1Thumbnail, user2Thumbnail));
        }

        @DataProvider(name = "permissions")
        public Object[][] providePermissions() {
            return new Object[][]{{"rw----"}, {"rwr---"}, {"rwra--"}, {"rwrw--"}};
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

    public static class BatchOfThumbnails extends AbstractServerTest {
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
            Assert.assertTrue(thumbnail.length > 0);
            Assert.assertEquals(thumbnails.get(pixelsIdα), thumbnail);
            Assert.assertEquals(thumbnails.get(pixelsIdβ), thumbnail);
        }
    }

    private static byte[] getThumbnail(ThumbnailStorePrx svc) throws ServerError {
        final int sizeX = 96;
        final int sizeY = 96;

        // Get thumbnail
        byte[] values = svc.getThumbnail(
                omero.rtypes.rint(sizeX),
                omero.rtypes.rint(sizeY));
        Assert.assertNotNull(values);
        Assert.assertTrue(values.length > 0);

        // Return the bytes
        return values;
    }

    private static byte[] getThumbnailWithoutDefault(ThumbnailStorePrx svc) throws ServerError {
        final int sizeX = 96;
        final int sizeY = 96;

        // Get thumbnail
        byte[] values = svc.getThumbnailWithoutDefault(
                omero.rtypes.rint(sizeX),
                omero.rtypes.rint(sizeY));
        Assert.assertNotNull(values);

        // Return the bytes
        return values;
    }

    private static void setThumbnailStoreToPixels(ThumbnailStorePrx svc, long pixelsId) throws ServerError {
        if (!svc.setPixelsId(pixelsId)) {
            svc.resetDefaults();
            svc.setPixelsId(pixelsId);
        }
    }
}
