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
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import loci.formats.FormatTools;

import ome.conditions.MissingPyramidException;
import ome.io.messages.MissingPyramidMessage;
import ome.io.nio.OriginalFileMetadataProvider;
import ome.io.nio.PixelBuffer;
import ome.io.nio.PixelsService;
import ome.model.core.Pixels;
import ome.model.enums.PixelsType;
import ome.util.PixelData;

import org.apache.commons.io.FileUtils;
import org.jmock.Mock;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
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

    @BeforeClass
    private void setup() {
        root = PathUtil.getInstance().getTemporaryDataFilePath();
        provider = new TestingOriginalFileMetadataProvider();
        pixels = new Pixels();

        pixels.setId(1L);
        pixels.setSizeX(1024);
        pixels.setSizeY(1024);
        pixels.setSizeZ(4);
        pixels.setSizeC(2);
        pixels.setSizeT(6);

        PixelsType type = new PixelsType();
        type.setValue("uint16");
        pixels.setPixelsType(type);

        service = new PixelsService(root) {
            protected boolean isRequirePyramid(Pixels pixels) {
              return true;
            }
        };
    }

    @AfterClass
    public void tearDown() throws IOException {
        FileUtils.deleteDirectory(new File(root));
    }

    @Test
    public void testTruePyramidCreation() {
        pixelBuffer = service.getPixelBuffer(pixels, null, null, true);
    }

    @Test(dependsOnMethods={"testTruePyramidCreation"})
    public void testPyramidWriteTiles() throws Exception {
        byte[] tile = new byte[256 * 256 * 2];
        int tileWidth = 256, tileHeight = 256, x, y;
        short tileCount = 0;
        for (int t = 0; t < pixels.getSizeT(); t++)
        {
            for (int c = 0; c < pixels.getSizeC(); c++)
            {
                for (int z = 0; z < pixels.getSizeZ(); z++)
                {
                    for (int tileOffsetX = 0;
                         tileOffsetX < pixels.getSizeX() / tileWidth;
                         tileOffsetX++)
                    {
                        for (int tileOffsetY = 0;
                             tileOffsetY < pixels.getSizeY() / tileHeight;
                             tileOffsetY++)
                        {
                            x = tileOffsetX * tileWidth;
                            y = tileOffsetY * tileHeight;
                            ByteBuffer.wrap(tile).asShortBuffer().put(0, tileCount);
                            pixelBuffer.setTile(
                                    tile, z, c, t, x, y, tileWidth, tileHeight);
                            tileCount++;
                        }
                    }
                }
            }
        }
        assertEquals(768, tileCount);
        //pixelBuffer.close();
    }

    @Test(dependsOnMethods={"testPyramidWriteTiles"})
    public void testPyramidReadTiles() throws Exception {
        PixelData tile;
        int tileWidth = 256, tileHeight = 256, x, y;
        short tileCount = 0;
        int sizeX = pixels.getSizeX();
        int sizeY = pixels.getSizeY();
        int sizeZ = pixels.getSizeZ();
        int sizeC = pixels.getSizeC();
        int sizeT = pixels.getSizeT();
        for (int t = 0; t < sizeT; t++)
        {
            for (int c = 0; c < sizeC; c++)
            {
                for (int z = 0; z < sizeZ; z++)
                {
                    for (int tileOffsetX = 0;
                         tileOffsetX < sizeX / tileWidth;
                         tileOffsetX++)
                    {
                        for (int tileOffsetY = 0;
                             tileOffsetY < sizeY / tileHeight;
                             tileOffsetY++)
                        {
                            x = tileOffsetX * tileWidth;
                            y = tileOffsetY * tileHeight;
                            int rasterizedT = FormatTools.getIndex(
                                    "XYZCT", sizeZ, sizeC, sizeT,
                                    sizeZ * sizeC * sizeT, z, c, t);
                            tile = pixelBuffer.getTile(0, 0, rasterizedT, x, y,
                                                       tileWidth, tileHeight);
                            tileCount++;
                        }
                    }
                }
            }
        }
        assertEquals(768, tileCount);
    }

}
