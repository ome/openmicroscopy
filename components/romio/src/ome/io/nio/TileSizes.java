/*
 *   $Id$
 *
 *   Copyright 2011 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.io.nio;

/**
 * Strategy interface which is used by {@link PixelsService} to
 * specify the default tile sizes as well as when tiling is necessary.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since Beta4.3.1
 * @see <a href="https://trac.openmicroscopy.org/ome/ticket/5192">ticket 5192</a>
 */
public interface TileSizes {

    int getTileWidth();
    int getTileHeight();
    int getMaxPlaneWidth();
    int getMaxPlaneHeight();

}
