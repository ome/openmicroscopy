/*
 * ome.io.nio.itests.PlaneIOUnitTest
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.io.nio.itests;

import java.io.File;
import java.io.IOException;

import ome.io.nio.DimensionsOutOfBoundsException;
import ome.io.nio.PixelBuffer;
import ome.util.PixelData;
import ome.io.nio.PixelsService;
import ome.model.core.Pixels;
import ome.server.itests.AbstractManagedContextTest;

import org.apache.commons.codec.binary.Hex;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author callan
 * 
 */
public class PixbufCreationUnitTest extends AbstractManagedContextTest {
    Pixels pixels;

    PixbufIOFixture baseFixture;
    String ROOT;

    PixelBuffer pixbuf;

    @Test
    public void testValidPixbuf() throws IOException {
        String validSHA1 = "11875aa5c6e7c09d433b1b0c793761001f8f34e7";

        byte[] md = pixbuf.calculateMessageDigest();
        assertEquals(validSHA1, Hex.encodeHexString(md));
    }

    @Test
    public void testNullPlanes() throws IOException,
            DimensionsOutOfBoundsException {
        for (int t = 0; t < pixels.getSizeT(); t++) {
            for (int c = 0; c < pixels.getSizeC(); c++) {
                for (int z = 0; z < pixels.getSizeZ(); z++) {
                    PixelData pd = pixbuf.getPlane(z, c, t);
                    assertNull(pd);
                }
            }
        }
    }

    @BeforeClass
    protected void setup() throws Exception {

        ROOT = getOmeroDataDir();

        // Create set up the base fixture which sets up the database for us
        baseFixture = new PixbufIOFixture(this.iContainer, this.iUpdate);
        pixels = baseFixture.setUp();

        // "Our" fixture which creates the planes needed for this test case.
        PixelsService service = new PixelsService(ROOT);
        pixbuf = service.createPixelBuffer(pixels);

    }

    @AfterClass
    protected void tearDown() throws Exception {
        // Tear down the resources create in this fixture
        String path = pixbuf.getPath();
        File f = new File(path);
        f.delete();

        // Tear down the resources created as part of the base fixture
        baseFixture.tearDown();
    }
}
