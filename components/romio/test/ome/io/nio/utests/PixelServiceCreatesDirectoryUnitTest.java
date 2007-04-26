/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.io.nio.utests;

import org.testng.annotations.*;
import ome.io.nio.PixelBuffer;
import ome.io.nio.PixelsService;
import ome.model.core.Pixels;
import ome.model.enums.PixelsType;
import java.util.Properties;
import junit.framework.TestCase;

public class PixelServiceCreatesDirectoryUnitTest extends TestCase {
    private Pixels pixels;

    private PixelBuffer pixelBuffer;

    private PixelsService service;
	
	private static final String ROOT = PathUtil.getInstance().getDataFilePath();
    
	@Override
    @Configuration(beforeTestMethod = true)
    protected void setUp() {
        pixels = new Pixels();
        pixels.setId(1234567890123L);
        pixels.setSizeX(256);
        pixels.setSizeY(256);
        pixels.setSizeZ(64);
        pixels.setSizeC(3);
        pixels.setSizeT(50);

        PixelsType type = new PixelsType();
        type.setValue("uint16");
        pixels.setPixelsType(type);
        service = new PixelsService(ROOT);
    }

    @Test
    public void testLargeId() throws Exception {
        pixelBuffer = service.createPixelBuffer(pixels);
    }

}
