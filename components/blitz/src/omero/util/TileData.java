/*
 *   $Id$
 *
 *   Copyright 2011 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package omero.util;

/**
 * Access strategy which can be implemented by diverse resources
 *
 */
public interface TileData
{
    public byte[] getTile(int z, int c, int t, int x, int y, int w, int h);

    public void setTile(byte[] buffer, int z, int c, int t, int x, int y, int w, int h);

    public void close();

}
