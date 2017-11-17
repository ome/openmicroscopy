/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2016 University of Dundee & Open Microscopy Environment.
 *  All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package omeis.providers.re.lut;

import java.io.File;


/**
 * Class to be extended by LUT reader.
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since 5.3
 */
public abstract class LutReader {

    /** The size of the color interval.*/
    static final int SIZE = 256;

    /** Holds the red values.*/
    protected byte[] reds = new byte[SIZE];

    /** Holds the green values.*/
    protected byte[] greens = new byte[SIZE];

    /** Holds the blues values.*/
    protected byte[] blues = new byte[SIZE];

    /** The file to read.*/
    protected File file;

    /** Flag indicating to read 32 byte NIH Image LUT header.*/
    protected boolean raw;

    /**
     * Creates a new instance.
     *
     * @param file The file to read.
     */
    LutReader(File file)
    {
        this.file = file;
    }

    /**
     * Reads the lookup table.
     * @return See above.
     * @throws Exception Throw if the file cannot be read.
     */
    abstract int read()
            throws Exception;

    /**
     * Returns the red value.
     *
     * @param value The value to handle.
     * @return See above
     */
    public byte getRed(int value)
    {
        return reds[value];
    }

    /**
     * Returns the green value.
     *
     * @param value The value to handle.
     * @return See above
     */
    public byte getGreen(int value)
    {
        return greens[value];
    }

    /**
     * Returns the blue value.
     *
     * @param value The value to handle.
     * @return See above
     */
    public byte getBlue(int value)
    {
        return blues[value];
    }

}
