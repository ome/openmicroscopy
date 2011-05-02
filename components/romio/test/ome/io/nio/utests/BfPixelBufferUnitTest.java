/*
 *   $Id$
 *
 *   Copyright 2011 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.io.nio.utests;

import static org.testng.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import ome.io.nio.OriginalFileMetadataProvider;
import ome.io.nio.PixelBuffer;
import ome.io.nio.PixelsService;
import ome.model.core.Pixels;
import ome.model.enums.PixelsType;

import org.apache.commons.io.FileUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Tests the logic for creating {@link BfPixelBuffer} instances.
 * @since 4.3
 */
public class BfPixelBufferUnitTest {

    private String root;

    private ome.model.core.Pixels pixels;

    private PixelBuffer pixelBuffer;

    private OriginalFileMetadataProvider provider;

    private PixelsService service;

    // Sizes don't really matter here
    private static final int sizeX = 512;

    private static final int sizeY = 512;

    private static final int sizeZ = 4;

    private static final int sizeC = 2;

    private static final int sizeT = 6;

    @BeforeClass
    private void setup() {
        root = PathUtil.getInstance().getTemporaryDataFilePath();
        provider = new TestingOriginalFileMetadataProvider();
        pixels = new Pixels();

        String pixelType = "uint16";
        pixels.setId(1L);
        pixels.setSizeX(sizeX);
        pixels.setSizeY(sizeY);
        pixels.setSizeZ(sizeZ);
        pixels.setSizeC(sizeC);
        pixels.setSizeT(sizeT);
        PixelsType type = new PixelsType();
        type.setValue(pixelType);
        pixels.setPixelsType(type);
    }

    @AfterClass
    public void tearDown() throws IOException {
        FileUtils.deleteDirectory(new File(root));
    }

    @Test
    public void testBfPixelBufferCreation() {
        pixels.setMethodology(PixelsService.METADATA_ONLY); // FIXME - use constant from elsewhere?
        service = new PixelsService(root);
        pixelBuffer = service.getPixelBuffer(pixels, provider, true);
        assertEquals(pixelBuffer.getClass().getName(),"ome.io.bioformats.BfPixelBuffer");
    }

    @Test
    public void testRomioPixelBufferCreationFromNullMethodology() {
        pixels.setMethodology(null);
        service = new PixelsService(root);
        pixelBuffer = service.getPixelBuffer(pixels, provider, true);
        assertEquals(pixelBuffer.getClass().getName(),"ome.io.nio.RomioPixelBuffer");
    }

    @Test
    public void testRomioPixelBufferCreationFromOtherString() {
        pixels.setMethodology("SOME_OTHER_TEXT");
        service = new PixelsService(root);
        pixelBuffer = service.getPixelBuffer(pixels, provider, true);
        assertEquals(pixelBuffer.getClass().getName(),"ome.io.nio.RomioPixelBuffer");
    }

}
