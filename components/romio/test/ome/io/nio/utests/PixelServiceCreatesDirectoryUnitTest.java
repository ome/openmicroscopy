/*
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.io.nio.utests;



import java.io.File;
import java.io.IOException;

import ome.io.nio.PixelBuffer;
import ome.io.nio.PixelsService;
import ome.model.core.Pixels;
import ome.model.enums.PixelsType;

import org.apache.commons.io.FileUtils;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class PixelServiceCreatesDirectoryUnitTest {
    private Pixels pixels;

    private PixelsService service;

    private static final String ROOT = 
        PathUtil.getInstance().getTemporaryDataFilePath();

    @AfterClass
    public void tearDown() throws IOException {
        FileUtils.deleteDirectory(new File(ROOT));
    }

    @BeforeMethod
    public void setUp() {
        pixels = new Pixels();
        pixels.setId(1234567890123L);
        pixels.setSizeX(8);
        pixels.setSizeY(8);
        pixels.setSizeZ(1);
        pixels.setSizeC(1);
        pixels.setSizeT(1);

        PixelsType type = new PixelsType();
        type.setValue("uint16");
        pixels.setPixelsType(type);
        service = new PixelsService(ROOT);
    }

    @Test
    public void testLargeId() throws Exception {
        PixelBuffer pixelBuffer = service.createPixelBuffer(pixels);
        AssertJUnit.assertNotNull(pixelBuffer);
    }

}
