/*
 *   $Id$
 *
 *   Copyright 2011 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.io.nio.utests;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

import loci.formats.FormatTools;
import ome.io.bioformats.BfPyramidPixelBuffer;
import ome.io.nio.PixelBuffer;
import ome.io.nio.PixelsService;
import ome.io.nio.TileLoopIteration;
import ome.io.nio.Utils;
import ome.io.nio.Utils.FailedTileLoopException;
import ome.model.core.Pixels;
import ome.model.enums.PixelsType;
import ome.util.checksum.ChecksumProviderFactory;
import ome.util.checksum.ChecksumProviderFactoryImpl;
import ome.util.checksum.ChecksumType;

import org.apache.commons.io.FileUtils;

/**
 * Tests the logic for creating {@link BfPyramidPixelBuffer} instances.
 * @since 4.3
 */
public abstract class AbstractPyramidPixelBufferUnitTest {

    protected String root;

    protected ome.model.core.Pixels pixels;

    protected PixelBuffer pixelBuffer;

    protected PixelsService service;

    // We're using 1000 rather than the evenly divisible 1024 below to expose
    // uneven tile sizes to the test case.
    protected static final int sizeX = 1000;

    protected static final int sizeY = 1010;

    protected static final int sizeZ = 4;

    protected static final int sizeC = 2;

    protected static final int sizeT = 6;

    protected static final int tileWidth = 256;

    protected static final int tileHeight = 256;

    protected int bytesPerPixel;

    protected void createService() {
        root = PathUtil.getInstance().getTemporaryDataFilePath();
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
            public boolean requiresPixelsPyramid(Pixels pixels) {
              return true;
            }
        };
    }

    protected void deleteRoot() throws IOException {
        FileUtils.deleteDirectory(new File(root));
    }

    protected short writeTiles(final List<String> hashDigests) throws FailedTileLoopException {
        return writeTiles(hashDigests, new Runnable(){
            public void run() {
                // Do nothing.
            }});
    }

    /**
     * Calls {@link Runnable#run()} after each successful call to
     * {@link PixelBuffer#setTile(byte[], Integer, Integer, Integer, Integer, Integer, Integer, Integer)}.
     */
    protected short writeTiles(final List<String> hashDigests, final Runnable run) throws FailedTileLoopException {
        short tileCount = (short) Utils.forEachTile(new TileLoopIteration() {
            public void run(int z, int c, int t, int x, int y, int tileWidth,
                            int tileHeight, int tileCount) {
                ChecksumProviderFactory cpf = new ChecksumProviderFactoryImpl();
                byte[] tile = new byte[tileWidth * tileHeight * bytesPerPixel];
                ByteBuffer.wrap(tile).asShortBuffer().put(0, (short) tileCount);
                hashDigests.add(cpf.getProvider(ChecksumType.MD5).putBytes(tile)
                        .checksumAsString());
                try
                {
                    pixelBuffer.setTile(
                            tile, z, c, t, x, y, tileWidth, tileHeight);
                    run.run();
                }
                catch (IOException e)
                {
                    throw new RuntimeException(e);
                }
                tileCount++;
            }
        }, pixelBuffer, tileWidth, tileHeight);
        return tileCount;
    }

}
