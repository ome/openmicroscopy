/*
 *   $Id$
 *
 *   Copyright 2011 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package omero.util;

/**
* A single iteration of a tile for each loop.
* @author Chris Allan <callan at blackcat dot ca>
* @since OMERO Beta-4.3.0
*/
public interface TileLoopIteration
{
    /**
    * Invoke a single loop iteration.
    * @param data the tile access strategy
    * @param z Z section counter of the loop.
    * @param c Channel counter of the loop.
    * @param t Timepoint counter of the loop.
    * @param x X offset within the plane specified by the section, channel and
    * timepoint counters.
    * @param y Y offset within the plane specified by the section, channel and
    * timepoint counters.
    * @param tileWidth Width of the tile requested. The tile request
    * itself may be smaller than the original tile width requested if
    * <code>x + tileWidth > sizeX</code>.
    * @param tileHeight Height of the tile requested. The tile request
    * itself may be smaller if <code>y + tileHeight > sizeY</code>.
    * @param tileCount Counter of the tile since the beginning of the loop.
    */
    void run(TileData data, int z, int c, int t, int x, int y,
             int tileWidth, int tileHeight, int tileCount);

}
