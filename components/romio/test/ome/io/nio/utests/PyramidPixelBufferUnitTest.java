/*
 *   $Id$
 *
 *   Copyright 2011 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.io.nio.utests;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ome.conditions.MissingPyramidException;
import ome.io.messages.MissingPyramidMessage;
import ome.io.nio.OriginalFileMetadataProvider;
import ome.io.nio.PixelBuffer;
import ome.io.nio.PixelsService;
import ome.model.core.Pixels;
import ome.model.enums.PixelsType;

import org.apache.commons.io.FileUtils;
import org.jmock.Mock;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Tests the logic for creating {@link BfPyramidPixelBuffer} instances.
 * @since 4.3
 */
public class PyramidPixelBufferUnitTest {

    private String root;

    private ome.model.core.Pixels pixels;

    private PixelBuffer pixelBuffer;

    private OriginalFileMetadataProvider provider;

    private PixelsService service;

    @BeforeMethod
    private void setup() {
        root = PathUtil.getInstance().getTemporaryDataFilePath();
        provider = new TestingOriginalFileMetadataProvider();
        pixels = new Pixels();

        pixels.setId(1L);
        pixels.setSizeX(1024);
        pixels.setSizeY(1024);
        pixels.setSizeZ(1);
        pixels.setSizeC(1);
        pixels.setSizeT(1);

        PixelsType type = new PixelsType();
        type.setValue("uint16");
        pixels.setPixelsType(type);

        service = new PixelsService(root) {
            protected boolean isRequirePyramid(Pixels pixels) {
              return true;
            }
        };
    }

    @AfterMethod
    public void tearDown() throws IOException {
        FileUtils.deleteDirectory(new File(root));
    }

    @Test
    public void testTruePyramidCreation() {
        PixelBuffer pb = service.getPixelBuffer(pixels, null, null, true);

    }


}
