/*
 *   $Id$
 *
 *   Copyright 2011 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package omero.util;

/**
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 4.3.0
 */
public abstract class TileLoop {

    /**
     * Subclasses must provide a fresh instance of {@link TileData}.
     * The instance will be closed after the run of forEachTile.
     */
    public abstract TileData createData();

    /**
     * Iterates over every tile in a given pixel based on the
     * over arching dimensions and a requested maximum tile width and height.
     * @param iteration Invoker to call for each tile.
     * @param pixel Pixel instance
     * @param tileWidth <b>Maximum</b> width of the tile requested. The tile
     * request itself will be smaller than the original tile width requested if
     * <code>x + tileWidth > sizeX</code>.
     * @param tileHeight <b>Maximum</b> height of the tile requested. The tile
     * request itself will be smaller if <code>y + tileHeight > sizeY</code>.
     * @return The total number of tiles iterated over.
     */
    public int forEachTile(int sizeX, int sizeY,
                           int sizeZ, int sizeC, int sizeT,
                           int tileWidth, int tileHeight,
                           TileLoopIteration iteration) {

        final TileData data = createData();

        try
        {
            int x, y, w, h;
            int tileCount = 0;
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
                                iteration.run(data, z, c, t, x, y, w, h, tileCount);
                                tileCount++;
                            }
                        }
                    }
                }
            }

            return tileCount;

        } finally {

            data.close();

        }
    }
}
