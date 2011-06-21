/*
 *   $Id$
 *
 *   Copyright 2011 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package omero.util;

import omero.ServerError;
import omero.api.RawPixelsStorePrx;
import omero.model.Pixels;

/**
 * Access strategy which can be implemented by diverse resources
 *
 */
public class RPSTileData implements TileData
{

    final protected RawPixelsStorePrx rps;

    final protected RPSTileLoop loop;

    public RPSTileData(RPSTileLoop loop, RawPixelsStorePrx rps) {
        this.loop = loop;
        this.rps = rps;
    }

    public byte[] getTile(int z, int c, int t, int x, int y, int w, int h) {
        try {
            return rps.getTile(z, c, t, x, y, w, h);
        } catch (ServerError se) {
            throw new RuntimeException(se);
        }
    }

    public void setTile(byte[] buffer, int z, int c, int t, int x, int y, int w, int h) {
        try {
            rps.setTile(buffer, z, c, t, x, y, w, h);
        } catch (ServerError se) {
            throw new RuntimeException(se);
        }
    }

    public void close() {
        try {
            Pixels pixels = rps.save();
            loop.setPixels(pixels);
            rps.close();
        } catch (ServerError se) {
            throw new RuntimeException(se);
        }
    }

}
