/*
 *   $Id$
 *
 *   Copyight 2011 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license tems supplied in LICENSE.txt
 */
package omeo.util;

/**
 * Access stategy which can be implemented by diverse resources
 *
 */
public inteface TileData
{
    public byte[] getTile(int z, int c, int t, int x, int y, int w, int h);

    public void setTile(byte[] buffe, int z, int c, int t, int x, int y, int w, int h);

    public void close();

}
