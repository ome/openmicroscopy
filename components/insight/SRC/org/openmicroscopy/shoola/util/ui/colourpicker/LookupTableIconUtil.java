/*
 * org.openmicroscopy.shoola.util.ui.PaintPot 
 *
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

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.openmicroscopy.shoola.util.ui.IconManager;

/**
 * Utility class for getting an Icon for a specific Lookup Table, see
 * {@link IconManager#LUTS}
 */
public class LookupTableIconUtil {

    /**
     * All available LUTs; the order must match the order in the
     * IconManager.LUTS png!
     */
    private static String[] LUTS = new String[] { "16_colors.lut",
            "3-3-2_rgb.lut", "5_ramps.lut", "6_shades.lut",
            "blue_orange_icb.lut", "brgbcmyw.lut", "cool.lut", "cyan_hot.lut",
            "edges.lut", "fire.lut", "gem.lut", "grays.lut",
            "green_fire_blue.lut", "hilo.lut", "ica.lut", "ica2.lut",
            "ica3.lut", "ice.lut", "magenta_hot.lut", "orange_hot.lut",
            "phase.lut", "rainbow_rgb.lut", "red-green.lut", "red_hot.lut",
            "royal.lut", "sepia.lut", "smart.lut", "spectrum.lut", "thal.lut",
            "thallium.lut", "unionjack.lut", "yellow_hot.lut" };

    /**
     * Get the LUT icon for the given LUT filename, scaled to the specified
     * dimension
     * 
     * @param filename
     *            The LUT filename
     * @param dim
     *            The dimension
     * @return See above.
     */
    public static Icon getLUTIcon(String filename, Dimension dim) {
        Image img = getLUTIconImage(filename);
        if (img != null) {
            img = img.getScaledInstance(dim.width, dim.height,
                    java.awt.Image.SCALE_SMOOTH);
            return new ImageIcon(img);
        }

        return null;
    }

    /**
     * Get the LUT icon image for the given LUT filename
     * 
     * @param filename
     *            The LUT filename
     * @return See above
     */
    public static BufferedImage getLUTIconImage(String filename) {
        BufferedImage src = getFullIconsImage();

        for (int i = 0; i < LUTS.length; i++) {
            if (LUTS[i].equals(filename))
                return src.getSubimage(0, i * 10, src.getWidth(), 10);
        }

        return null;
    }

    /**
     * Get the IconManager.LUTS icon (concatenated image file of all LUT icons)
     * as {@link BufferedImage}
     * 
     * @return See above
     */
    private static BufferedImage getFullIconsImage() {

        ImageIcon i = IconManager.getInstance().getImageIcon(IconManager.LUTS);
        Image img = i.getImage();

        if (img instanceof BufferedImage) {
            return (BufferedImage) img;
        }

        BufferedImage bimage = new BufferedImage(img.getWidth(null),
                img.getHeight(null), BufferedImage.TYPE_INT_RGB);

        Graphics2D bGr = bimage.createGraphics();
        bGr.drawImage(img, 0, 0, null);
        bGr.dispose();

        return bimage;
    }
}
