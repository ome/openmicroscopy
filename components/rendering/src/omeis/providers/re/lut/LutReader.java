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
 * Initializes the reader corresponding to the specified lookup table.
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since 5.3
 */
public class LutReader {

    /** The file to read. */
    private File file;

    /** The reader used.*/
    private BasicLutReader reader;

    /**
     * Creates a new instance.
     *
     * @param filePath The path to the file.
     * @param fileName The name of the file.
     */
    public LutReader(String filePath, String fileName)
    {
        file = new File(filePath, fileName);
    }

    /**
     * Creates a new instance.
     *
     * @param file The file to read.
     */
    public LutReader(File file)
    {
        this.file = file;
    }

    /** Reads the file.*/
    public void read()
        throws Exception
    {
        long length = file.length();
        int size = 0;
        if (length > 768) { // attempt to read NIH Image LUT
            reader = new BinaryLutReader(file);
            size = reader.read();
        }
        //read raw lut
        if (size == 0 && (length == 0 || length == 768 || length == 970)) {
            reader = new BinaryLutReader(file, true);
            size = reader.read();
        }
        if (size == 0 && length > 768) {
            reader = new TextLutReader(file);
            size = reader.read();
        }
        if (size == 0) {
            throw new Exception("Cannot read the lookup table.");
        }
    }

    /**
     * Returns the red value.
     *
     * @param value The value to handle.
     * @return See above
     */
    public byte getRed(int value)
    {
        return reader.reds[value];
    }

    /**
     * Returns the green value.
     *
     * @param value The value to handle.
     * @return See above
     */
    public byte getGreen(int value)
    {
        return reader.greens[value];
    }

    /**
     * Returns the blue value.
     *
     * @param value The value to handle.
     * @return See above
     */
    public byte getBlue(int value)
    {
        return reader.blues[value];
    }

}
