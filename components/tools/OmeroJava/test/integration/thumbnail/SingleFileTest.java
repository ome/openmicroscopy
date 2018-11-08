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

import integration.AbstractServerTest;
import integration.ModelMockFactory;
import ome.formats.OMEROMetadataStoreClient;
import omero.ServerError;
import omero.api.IRenderingSettingsPrx;
import omero.api.ThumbnailStorePrx;
import omero.model.Pixels;
import omero.model.RenderingDef;
import omero.sys.EventContext;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;

import javax.imageio.ImageIO;

/**
 * Tests utilizing single thumbnail loading APIs
 */
public class SingleFileTest extends AbstractServerTest {

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
        loginUser(owner);

        importer = new OMEROMetadataStoreClient();
        importer.initialize(factory);

        String format = ModelMockFactory.FORMATS[0];
        importedFile = createImageFile(format);
        pixels = importFile(importer, importedFile, format).get(0);

        svc = factory.createThumbnailStore();
        Utils.setThumbnailStoreToPixels(svc, pixels.getId().getValue());
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
        Utils.getThumbnail(svc);
    }

    @Test
    public void testGetThumbnailByLongestSide() throws Exception {
        final int sizeX = 48;
        byte[] lsValues = svc.getThumbnailByLongestSide(omero.rtypes
                .rint(sizeX));
        Assert.assertNotNull(lsValues);
        Assert.assertTrue(lsValues.length > 0);
        // Check width and height
        
        try(InputStream in = new ByteArrayInputStream(lsValues)) {
            BufferedImage buf = ImageIO.read(in);
            Assert.assertEquals(sizeX, buf.getWidth());
            Assert.assertEquals(sizeX, buf.getHeight());
        } catch (Exception e) {
            throw new RuntimeException("Cannot convert byte array", e);
        }
    }

    @Test
    public void testGetThumbnailWithoutDefault() throws Exception {
        Utils.getThumbnailWithoutDefault(svc);
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
        Utils.getThumbnail(svc);

        // Reset rendering settings for pixels object
        resetRenderingSettingsForPixelsObject(pixels);

        // Call getThumbnail
        Utils.getThumbnail(svc);
    }

    @Test
    public void testGetThumbnailWithoutDefaultAfterReset() throws Exception {
        Utils.getThumbnailWithoutDefault(svc);

        // Reset rendering settings for pixels object
        resetRenderingSettingsForPixelsObject(pixels);

        Utils.getThumbnailWithoutDefault(svc);
    }

    @Test
    public void testGetThumbnailAsOtherUser() throws Exception {
        // Close current thumbnail store
        svc.close();

        // Create new user in group and login as that user
        EventContext newUser = newUserInGroup();
        loginUser(newUser);

        // Create a new thumbnail store
        svc = factory.createThumbnailStore();

        // Set pixels id to the new thumbnail store
        Utils.setThumbnailStoreToPixels(svc, pixels.getId().getValue());

        // Get the thumbnail
        Utils.getThumbnail(svc);
    }

    @Test
    public void testGetThumbnailWithoutDefaultAsOtherUser() throws Exception {
        // Close current thumbnail store
        svc.close();

        // Create new user in group and login as that user
        EventContext newUser = newUserInGroup();
        loginUser(newUser);

        // Create a new thumbnail store
        svc = factory.createThumbnailStore();

        // Set pixels id to the new thumbnail store
        Utils.setThumbnailStoreToPixels(svc, pixels.getId().getValue());

        // Get the thumbnail
        Utils.getThumbnailWithoutDefault(svc);
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
