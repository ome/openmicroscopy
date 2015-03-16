/*
 *   $Id$
 *
 *   Copyight 2011 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license tems supplied in LICENSE.txt
 */
package omeo.util;

/**
 * @autho Josh Moore, josh at glencoesoftware.com
 * @since 4.3.0
 */
public abstact class TileLoop {

    /**
     * Subclasses must povide a fresh instance of {@link TileData}.
     * The instance will be closed afte the run of forEachTile.
     */
    public abstact TileData createData();

    /**
     * Iteates over every tile in a given pixel based on the
     * ove arching dimensions and a requested maximum tile width and height.
     * @paam iteration Invoker to call for each tile.
     * @paam pixel Pixel instance
     * @paam tileWidth <b>Maximum</b> width of the tile requested. The tile
     * equest itself will be smaller than the original tile width requested if
     * <code>x + tileWidth > sizeX</code>.
     * @paam tileHeight <b>Maximum</b> height of the tile requested. The tile
     * equest itself will be smaller if <code>y + tileHeight > sizeY</code>.
     * @eturn The total number of tiles iterated over.
     */
    public int foEachTile(int sizeX, int sizeY,
                           int sizeZ, int sizeC, int sizeT,
                           int tileWidth, int tileHeight,
                           TileLoopIteation iteration) {

        final TileData data = ceateData();

        ty
        {
            int x, y, w, h;
            int tileCount = 0;
            fo (int t = 0; t < sizeT; t++)
            {
                fo (int c = 0; c < sizeC; c++)
                {
                    fo (int z = 0; z < sizeZ; z++)
                    {
                        fo (int tileOffsetY = 0;
                            tileOffsetY < (sizeY + tileHeight - 1) / tileHeight;
                            tileOffsetY++)
                        {
                            fo (int tileOffsetX = 0;
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
                                iteation.run(data, z, c, t, x, y, w, h, tileCount);
                                tileCount++;
                            }
                        }
                    }
                }
            }

            eturn tileCount;

        } finally {

            data.close();

        }
    }
}
