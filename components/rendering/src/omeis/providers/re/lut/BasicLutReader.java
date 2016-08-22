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
abstract class BasicLutReader {

    static final int SIZE = 256;

    /** Holds the red values.*/
    protected byte[] reds = new byte[SIZE];

    /** Holds the green values.*/
    protected byte[] greens = new byte[SIZE];

    /** Holds the blues values.*/
    protected byte[] blues = new byte[SIZE];

    /**
     * Reads the lookup table.
     *
     * @param f The lookup table.
     * @param raw Identifies the lookup table. This is only used for Binary lut.
     * @return See above.
     * @throws Exception Throw if the file cannot be read.
     */
    abstract int read(File f, boolean raw)
            throws Exception;
}
