/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2016 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
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
package org.openmicroscopy.shoola.util.ui.colourpicker;

import java.awt.Color;

/**
 * Just some static utility methods for comparing Color and lookup tables
 * 
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 */
public class ColourPickerUtil {

    /**
     * Checks if Color c1 and Color c2 have the same red, green and blue values
     * 
     * @param c1
     *            Color 1
     * @param c2
     *            Color 2
     * @return See above.
     */
    public static boolean sameColor(Color c1, Color c2) {
        return c1.getRed() == c2.getRed() && c1.getGreen() == c2.getGreen()
                && c1.getBlue() == c2.getBlue();
    }

    /**
     * Checks if lookup table lut1 and lut2 are the same. If both are
     * <code>null</code> the will be considered as the same.
     * 
     * @param lut1
     *            Lookup table 1
     * @param lut2
     *            Lookup table 2
     * @return See above.
     */
    public static boolean sameLookuptable(String lut1, String lut2) {
        if (lut1 != null && lut1.trim().length() == 0)
            lut1 = null;

        if (lut2 != null && lut2.trim().length() == 0)
            lut2 = null;

        if (lut1 == null ^ lut2 == null)
            return false;

        if (lut1 == null & lut2 == null)
            return true;

        return lut1.equals(lut2);
    }

}
