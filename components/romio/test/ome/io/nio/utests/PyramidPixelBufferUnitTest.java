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

import ome.io.nio.OriginalFileMetadataProvider;
import ome.io.nio.PixelBuffer;
import ome.io.nio.PixelsService;
import ome.io.nio.Utils;
import ome.io.nio.TileLoopIteration;
import ome.model.core.Pixels;
import ome.model.enums.PixelsType;
import ome.util.PixelData;

import org.apache.commons.io.FileUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
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

    // We're using 1000 rather than the evenly divisible 1024 below to expose
    // uneven tile sizes to the test case.
    private static final int sizeX = 1000;

    private static final int sizeY = 1010;

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
            public boolean isRequirePyramid(Pixels pixels) {
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
        short tileCount = (short) Utils.forEachTile(new TileLoopIteration() {
            public void run(int z, int c, int t, int x, int y, int tileWidth,
                            int tileHeight, int tileCount) {
                byte[] tile = new byte[tileWidth * tileHeight * bytesPerPixel];
                ByteBuffer.wrap(tile).asShortBuffer().put(0, (short) tileCount);
                hashDigests.add(ome.util.Utils.bytesToHex(
                        ome.util.Utils.calculateMessageDigest(tile)));
                try
                {
                    pixelBuffer.setTile(
                            tile, z, c, t, x, y, tileWidth, tileHeight);
                }
                catch (IOException e)
                {
                    throw new RuntimeException(e);
                }
                tileCount++;
            }
        }, pixelBuffer, tileWidth, tileHeight);
        assertEquals(tileCount, 768);
        pixelBuffer.close();
    }

    @Test(dependsOnMethods={"testPyramidWriteTiles"}, enabled=false)
    public void testPyramidReadTiles() throws Exception {
        pixelBuffer.setResolutionLevel(pixelBuffer.getResolutionLevels() - 1);
        short tileCount = (short) Utils.forEachTile(new TileLoopIteration() {
            public void run(int z, int c, int t, int x, int y, int tileWidth,
                            int tileHeight, int tileCount) {
                try
                {
                    final PixelData tile = pixelBuffer.getTile(z, c, t, x, y,
                            tileWidth, tileHeight);
                    String readDigest = ome.util.Utils.bytesToHex(
                            ome.util.Utils.calculateMessageDigest(
                                    tile.getData()));
                    String writtenDigest = hashDigests.get(tileCount);
                    if (!writtenDigest.equals(readDigest))
                    {
                        fail(String.format(
                                "Hash digest mismatch z:%d c:%d t:%d " +
                                "x:%d y:%d -- %s != %s",
                                z, c, t, x, y,
                                writtenDigest, readDigest));
                    }
                }
                catch (IOException e)
                {
                    throw new RuntimeException(e);
                }
            }
        }, pixelBuffer, tileWidth, tileHeight);
        assertEquals(tileCount, 768);
    }

    @Test(dependsOnMethods={"testPyramidWriteTiles"}, enabled=true)
    public void testGetPixelBufferResolutionLevels() {
        assertEquals(pixelBuffer.getResolutionLevels(), 6);
    }

    @Test(dependsOnMethods={"testPyramidWriteTiles"}, enabled=true)
    public void testGetPixelBufferResolutionLevel() {
        assertEquals(pixelBuffer.getResolutionLevel(), 5);
    }

    @Test(dependsOnMethods={"testPyramidWriteTiles"}, enabled=true)
    public void testSetPixelBufferResolutionLevel() {
        pixelBuffer.setResolutionLevel(0);
        assertEquals(pixelBuffer.getResolutionLevel(), 0);
    }

    @Test(dependsOnMethods={"testPyramidWriteTiles"}, enabled=true)
    public void testSetPixelBufferResolutionLevelChangeOfDimensions() {
        pixelBuffer.setResolutionLevel(pixelBuffer.getResolutionLevels() - 2);
        assertEquals(pixelBuffer.getSizeX(), sizeX / 2);
        assertEquals(pixelBuffer.getSizeY(), sizeY / 2);
    }

    @Test(dependsOnMethods={"testPyramidWriteTiles"}, enabled=true)
    public void testPyramidReadTilesFirstResolutionLevel() throws Exception {
        pixelBuffer.setResolutionLevel(pixelBuffer.getResolutionLevels() - 2);
        short tileCount = (short) Utils.forEachTile(new TileLoopIteration() {
            public void run(int z, int c, int t, int x, int y, int tileWidth,
                            int tileHeight, int tileCount) {
                try
                {
                    final PixelData tile = pixelBuffer.getTile(z, c, t, x, y,
                            tileWidth, tileHeight);
                    assertEquals(tile.size(), tileWidth * tileHeight);
                }
                catch (IOException e)
                {
                    throw new RuntimeException(e);
                }
            }
        }, pixelBuffer, tileWidth, tileHeight);
        assertEquals(tileCount, 192);
    }

}
