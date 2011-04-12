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
import ome.util.Utils;

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

    private List<String> hashDigests = new ArrayList<String>();

    private static final int sizeX = 1024;

    private static final int sizeY = 1024;

    private static final int sizeZ = 4;

    private static final int sizeC = 2;

    private static final int sizeT = 6;

    private static final int tileWidth = 256;

    private static final int tileHeight = 256;

    private int bytesPerPixel;

    @BeforeClass
    private void setup() {
        root = PathUtil.getInstance().getTemporaryDataFilePath();
        provider = new TestingOriginalFileMetadataProvider();
        pixels = new Pixels();

        String pixelType = "uint16";
        bytesPerPixel = FormatTools.getBytesPerPixel(pixelType);
        pixels.setId(1L);
        pixels.setSizeX(sizeX);
        pixels.setSizeY(sizeY);
        pixels.setSizeZ(sizeZ);
        pixels.setSizeC(sizeC);
        pixels.setSizeT(sizeT);

        PixelsType type = new PixelsType();
        type.setValue(pixelType);
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

    @Test(dependsOnMethods={"testTruePyramidCreation"}, enabled=true)
    public void testPyramidWriteTiles() throws Exception {
        byte[] tile = new byte[tileWidth * tileHeight * bytesPerPixel];
        int x, y;
        short tileCount = 0;
        for (int t = 0; t < sizeT; t++)
        {
            for (int c = 0; c < sizeC; c++)
            {
                for (int z = 0; z < sizeZ; z++)
                {
                    for (int tileOffsetY = 0;
                    tileOffsetY < sizeY / tileHeight;
                    tileOffsetY++)
                    {
                        for (int tileOffsetX = 0;
                             tileOffsetX < sizeX / tileWidth;
                             tileOffsetX++)
                        {
                            x = tileOffsetX * tileWidth;
                            y = tileOffsetY * tileHeight;
                            ByteBuffer.wrap(tile).asShortBuffer().put(0, tileCount);
                            hashDigests.add(Utils.bytesToHex(
                                    Utils.calculateMessageDigest(tile)));
                            pixelBuffer.setTile(
                                    tile, z, c, t, x, y, tileWidth, tileHeight);
                            tileCount++;
                        }
                    }
                }
            }
        }
        assertEquals(768, tileCount);
        pixelBuffer.close();
    }

    @Test(dependsOnMethods={"testPyramidWriteTiles"}, enabled=true)
    public void testPyramidReadTiles() throws Exception {
        PixelData tile;
        int x, y;
        short tileCount = 0;

        for (int t = 0; t < sizeT; t++)
        {
            for (int c = 0; c < sizeC; c++)
            {
                for (int z = 0; z < sizeZ; z++)
                {
                    for (int tileOffsetY = 0;
                    tileOffsetY < sizeY / tileHeight;
                    tileOffsetY++)
                    {
                        for (int tileOffsetX = 0;
                             tileOffsetX < sizeX / tileWidth;
                             tileOffsetX++)
                        {
                            x = tileOffsetX * tileWidth;
                            y = tileOffsetY * tileHeight;
                            int rasterizedT = FormatTools.getIndex(
                                    "XYZCT", sizeZ, sizeC, sizeT,
                                    sizeZ * sizeC * sizeT, z, c, t);
                            tile = pixelBuffer.getTile(0, 0, rasterizedT, x, y,
                                                       tileWidth, tileHeight);
                            String readDigest = Utils.bytesToHex(
                                    Utils.calculateMessageDigest(
                                            tile.getData()));
                            String writtenDigest = hashDigests.get(tileCount);
                            if (!writtenDigest.equals(readDigest))
                            {
                                fail(String.format(
                                        "Hash digest mismatch z:%d c:%d t:%d " +
                                        "x:%d: y:%d -- %s != %s",
                                        z, c, t, x, y,
                                        writtenDigest, readDigest));
                            }
                            tileCount++;
                        }
                    }
                }
            }
        }
        assertEquals(768, tileCount);
    }

}
