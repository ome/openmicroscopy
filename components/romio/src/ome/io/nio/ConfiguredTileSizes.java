/*
 *   $Id$
 *
 *   Copyright 2011 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.io.nio;

/**
 * Implementation which has all values injected during configuration.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since Beta4.3.1
 * @see <a href="https://trac.openmicroscopy.org/ome/ticket/5952">ticket 5952</a>
 */
public class ConfiguredTileSizes implements TileSizes {

    private final int tileWidth, tileHeight, maxPlaneWidth, maxPlaneHeight;

    public ConfiguredTileSizes() {
        this(256, 256, 3192, 3192); // Default as in omero.properties
    }

    public ConfiguredTileSizes(int tileWidth, int tileHeight,
            int maxPlaneWidth, int maxPlaneHeight) {
        this.tileWidth = tileWidth;
        this.tileHeight = tileHeight;
        this.maxPlaneWidth = maxPlaneWidth;
        this.maxPlaneHeight = maxPlaneHeight;
    }

    public int getTileWidth() {
        return tileWidth;
    }

    public int getTileHeight() {
        return tileHeight;
    }

    public int getMaxPlaneWidth() {
        return maxPlaneWidth;
    }
    public int getMaxPlaneHeight() {
        return maxPlaneHeight;
    }

    @Override
    public String toString() {
        return String.format("%s(w=%s,h=%s,W=%s,H=%s)", getClass().getName(),
                tileWidth, tileHeight, maxPlaneWidth, maxPlaneHeight);
    }


}
