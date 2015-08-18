/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package omero.gateway.rnd;

import omero.util.ReadOnlyByteArray;

/** 
 * Holds structure used to mapped the raw pixels data.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since OME3.0
 */
public class Plane2D
{

    /** The number of bytes per pixel. */
    private int bytesPerPixel;

    /** The number of elements along the x-axis. */
    private int sizeX;

    /** The original array. */
    private ReadOnlyByteArray data;

    /** Strategy used to transform original data. */
    private BytesConverter strategy;

    /** The converted raw data. */
    private double[][] mappedData;

    /** 
     * Determines the offset value.
     *
     * @param x The x-coordinate.
     * @param y The y-coordinate.
     * @return See above.
     */
    private int calculateOffset(int x, int y)
    {
        return bytesPerPixel*(sizeX*y+x);
    }

    /**
     * Converts the raw data.
     *
     * @param sizeY The number of pixels along the y-axis.
     */
    private void mappedData(int sizeY)
    {
        mappedData = new double[sizeX][sizeY];
        int offset;
        for (int x = 0; x < sizeX; x++) {
            for (int y = 0; y < sizeY; y++) {
                offset = calculateOffset(x, y);
                mappedData[x][y] = strategy.pack(data, offset, bytesPerPixel);
            }
        }
    }

    /**
     * Creates a new instance.
     *
     * @param data The array of byte.
     * @param sizeX The number of pixels along the x-axis.
     * @param sizeY The number of pixels along the y-axis.
     * @param bytesPerPixel The number of bytes per pixel.
     * @param strategy Strategy to transform pixel.
     */
    public Plane2D(ReadOnlyByteArray data, int sizeX, int sizeY,
            int bytesPerPixel, BytesConverter strategy)
    {
        this.bytesPerPixel = bytesPerPixel;
        this.data = data;
        this.strategy = strategy;
        this.sizeX = sizeX;
        mappedData(sizeY);
    }

    /**
     * Returns the pixels value at the point specified by the x-coordinate
     * and y-coordinate.
     *
     * @param x The x-coordinate.
     * @param y The y-coordinate.
     * @return See above.
     */
    public double getPixelValue(int x, int y)
    {
        return mappedData[x][y];
    }

    /**
     * Returns the raw data value at the given offset
     *
     * @param offset The offset
     * @return See above.
     */
    public byte getRawValue(int offset)
    {
        return data.get(offset);
    }
}
