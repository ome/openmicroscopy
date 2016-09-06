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
public class LutReaderFactory {

    /**
     * Reads the specified file.
     *
     * @param filePath The path to the directory.
     * @param fileName The name of the file.
     */
    public static LutReader read(String filePath, String fileName)
        throws Exception
    {
        return LutReaderFactory.read(new File(filePath, fileName));
    }

    /**
     * Reads the specified file.
     *
     * @param file The file to read.
     */
    public static LutReader read(File file)
        throws Exception
    {
        LutReader reader = null;
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
        return reader;
    }

}
