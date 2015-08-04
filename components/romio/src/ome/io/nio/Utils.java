/*
 * ome.io.nio.Utils
 *
 *   Copyright 2011 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.io.nio;

/**
 * General utility methods for working with ROMIO classes.
 * @author Chris Allan <callan at blackcat dot ca>
 * @since OMERO Beta-4.3.0
 */
public class Utils
{
    /**
     * Iterates over every tile in a given pixel buffer based on the
     * over arching dimensions and a requested maximum tile width and height.
     * @param iteration Invoker to call for each tile.
     * @param pixelBuffer Pixel buffer which is backing the pixel data.
     * @param tileWidth <b>Maximum</b> width of the tile requested. The tile
     * request itself will be smaller than the original tile width requested if
     * <code>x + tileWidth > sizeX</code>.
     * @param tileHeight <b>Maximum</b> height of the tile requested. The tile
     * request itself will be smaller if <code>y + tileHeight > sizeY</code>.
     * @return The total number of tiles iterated over.
     */
    public static int forEachTile(TileLoopIteration iteration,
                                  PixelBuffer pixelBuffer,
                                  int tileWidth, int tileHeight)

    {
        int sizeX = pixelBuffer.getSizeX();
        int sizeY = pixelBuffer.getSizeY();
        int sizeZ = pixelBuffer.getSizeZ();
        int sizeC = pixelBuffer.getSizeC();
        int sizeT = pixelBuffer.getSizeT();
        return forEachTile(iteration, sizeX, sizeY, sizeZ, sizeC, sizeT,
                tileWidth, tileHeight);
    }

    /**
     * Iterates over every tile in a given pixel buffer based on the
     * over arching dimensions and a requested maximum tile width and height.
     * @param iteration Invoker to call for each tile.
     * @param tileWidth <b>Maximum</b> width of the tile requested. The tile
     * request itself will be smaller than the original tile width requested if
     * <code>x + tileWidth > sizeX</code>.
     * @param tileHeight <b>Maximum</b> height of the tile requested. The tile
     * request itself will be smaller if <code>y + tileHeight > sizeY</code>.
     * @return The total number of tiles iterated over.
     */
    public static int forEachTile(TileLoopIteration iteration,
                                  int sizeX, int sizeY, int sizeZ,
                                  int sizeC, int sizeT,
                                  int tileWidth, int tileHeight)
    {
        int tileCount = 0;
        int x, y, w, h;
        for (int t = 0; t < sizeT; t++)
        {
            for (int c = 0; c < sizeC; c++)
            {
                for (int z = 0; z < sizeZ; z++)
                {
                    for (int tileOffsetY = 0;
                         tileOffsetY < (sizeY + tileHeight - 1) / tileHeight;
                         tileOffsetY++)
                    {
                        for (int tileOffsetX = 0;
                             tileOffsetX < (sizeX + tileWidth - 1) / tileWidth;
                             tileOffsetX++)
                        {
                            x = tileOffsetX * tileWidth;
                            y = tileOffsetY * tileHeight;
                            w = tileWidth;
                            if (w + x > sizeX)
                            {
                                w = sizeX - x;
                            }
                            h = tileHeight;
                            if (h + y > sizeY)
                            {
                                h = sizeY - y;
                            }
                            iteration.run(z, c, t, x, y, w, h, tileCount);
                            tileCount++;
                        }
                    }
                }
            }
        }
        return tileCount;
    }
}
