/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2015 University of Dundee. All rights reserved.
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
package omero.gateway.util;

import java.io.File;

/**
 * Methods to check if an image is a <code>TIFF</code> image.
 *
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 * @since 5.1
 *
 */
public class TIFFFilter {

    /** The MIMEType associated to this type of file. */
    public static final String MIMETYPE = "image/tiff";

    /** Possible file extension. */
    public static final String TIFF = "tiff";

    /** Possible file extension. */
    public static final String TIF = "tif";

    /** Possible file extension. */
    public static final String TF_2 = "tf2";

    /** Possible file extension. */
    public static final String TF_8 = "tf8";

    /** Possible file extension. */
    public static final String BTF = "btf";

    /** The possible extensions. */
    public static final String[] extensions;

    /** The description of the filter. */
    private static final String description;

    static {
        extensions = new String[5];
        extensions[0] = TIFF;
        extensions[1] = TIF;
        extensions[2] = TF_2;
        extensions[3] = TF_8;
        extensions[4] = BTF;

        StringBuffer s = new StringBuffer();
        s.append("Tagged Image File Format (");
        for (int i = 0; i < extensions.length; i++) {
            s.append("*." + extensions[i]);
            if (i < extensions.length - 1)
                s.append(", ");
        }
        s.append(")");
        description = s.toString();
    }

    /**
     * Returns the MIME type.
     *
     * @return See above.
     */
    public String getMIMEType() {
        return MIMETYPE;
    }

    /**
     * Returns the extension of the filter.
     *
     * @return See above.
     */
    public String getExtension() {
        return TIFF;
    }

    /**
     * Returns the description of the filter.
     *
     * @return See above.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Accepts or not the file with the declared file extensions.
     * 
     * @param f The file to handle.
     * @return See above.
     */
    public boolean accept(File f) {
        if (f == null)
            return false;
        if (f.isDirectory())
            return true;
        return isSupported(f.getName(), extensions);
    }

    /**
     * Accepts or not the file identified by its name.
     *
     * @param fileName The name of the file.
     * @return See above.
     */
    public boolean accept(String fileName) {
        return isSupported(fileName, extensions);
    }

    /**
     * Returns <code>true</code> if the file identified by the passed name ends
     * with one of the specified extensions, <code>false</code> otherwise.
     * 
     * @param name
     *            The name of the file.
     * @param extensions
     *            The supported extensions.
     * @return See above.
     */
    private boolean isSupported(String name, String[] extensions) {
        if (name == null || extensions == null)
            return false;
        String value = name.toLowerCase();
        for (int i = 0; i < extensions.length; i++) {
            if (value.endsWith("." + extensions[i]))
                return true;
        }
        return false;
    }
}
