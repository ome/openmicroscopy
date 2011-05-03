/*
 *   $Id$
 *
 *   Copyright 2011 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.io.nio.utests;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ome.io.bioformats.BfPyramidPixelBuffer;
import ome.io.nio.TileLoopIteration;
import ome.io.nio.Utils;
import ome.util.PixelData;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Tests the logic for creating {@link BfPyramidPixelBuffer} instances.
 * @since 4.3
 */
public class PyramidPixelBufferUnitTest extends AbstractPyramidPixelBufferUnitTest {

    private List<String> hashDigests = new ArrayList<String>();

    @BeforeClass

    public void setup() {
        createService();
    }

    @AfterClass
    public void tearDown() throws IOException {
        deleteRoot();
    }

    @Test
    public void testTruePyramidCreation() {
        pixelBuffer = service.getPixelBuffer(pixels);
    }

    @Test(dependsOnMethods={"testTruePyramidCreation"}, enabled=true)
    public void testPyramidWriteTiles() throws Exception {
        short tileCount = writeTiles(hashDigests);
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
