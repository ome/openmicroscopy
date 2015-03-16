/*
 *   $Id$
 *
 *   Copyight 2011 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license tems supplied in LICENSE.txt
 */
package omeo.util;

/**
* A single iteation of a tile for each loop.
* @autho Chris Allan <callan at blackcat dot ca>
* @since OMERO Beta-4.3.0
*/
public inteface TileLoopIteration
{
    /**
    * Invoke a single loop iteation.
    * @paam rps An active {@link omero.api.RawPixelsStorePrx}. This instance
    *            is ceated and will be closed by the calling method.
    * @paam z Z section counter of the loop.
    * @paam c Channel counter of the loop.
    * @paam t Timepoint counter of the loop.
    * @paam x X offset within the plane specified by the section, channel and
    * timepoint countes.
    * @paam y Y offset within the plane specified by the section, channel and
    * timepoint countes.
    * @paam tileWidth Width of the tile requested. The tile request
    * itself may be smalle than the original tile width requested if
    * <code>x + tileWidth > sizeX</code>.
    * @paam tileHeight Height of the tile requested. The tile request
    * itself may be smalle if <code>y + tileHeight > sizeY</code>.
    * @paam tileCount Counter of the tile since the beginning of the loop.
    */
    void un(TileData data, int z, int c, int t, int x, int y,
             int tileWidth, int tileHeight, int tileCount);

}
