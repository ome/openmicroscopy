/*
 * org.openmicroscopy.shoola.util.ui.colourpicker.ColourObject 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2016 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.util.ui.colourpicker;

import java.awt.Color;

/**
 * Utility object.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:donald@lifesci.dundee.ac.uk"
 *         >donald@lifesci.dundee.ac.uk</a>
 * @version 3.0 <small> (<b>Internal version:</b> $Revision: $Date: $) </small>
 * @since 3.0-Beta4
 */
public class ColourObject {
    
    /** The color */
    public Color color;
    
    /** The description */
    public String description;
    
    /** The lookup table */
    public String lut;
    
    /** The reverse intensity flag */
    public boolean revInt;
    
    /** The preview flag */
    public boolean preview;
    
    /**
     * Creates a new instance.
     */
    public ColourObject() {
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((color == null) ? 0 : colorHashcode(color));
        result = prime * result
                + ((description == null) ? 0 : description.hashCode());
        result = prime * result
                + ((lut == null) ? 0 : lut.trim().toLowerCase().hashCode());
        result = prime * result + (revInt ? 1231 : 1237);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ColourObject other = (ColourObject) obj;
        if (color == null) {
            if (other.color != null)
                return false;
        } else if (!isSameColor(other.color))
            return false;
        if (description == null) {
            if (other.description != null)
                return false;
        } else if (!description.equals(other.description))
            return false;
        if (lut == null) {
            if (other.lut != null)
                return false;
        } else if (other.lut == null) {
            if (lut != null)
                return false;
        } else if (lut != null
                && other.lut != null
                && !lut.trim().toLowerCase()
                        .equals(other.lut.trim().toLowerCase()))
            return false;
        if (revInt != other.revInt)
            return false;
        return true;
    }

    /**
     * Checks if the given {@link Color} is the same color as this
     * {@link ColourObject}'s {@link Color}, by comparing the RGB and alpha
     * values.
     * 
     * @param c
     *            The {@link Color} to check
     * @return See above.
     */
    private boolean isSameColor(Color c) {
        return (c.getRed() == color.getRed()
                && c.getGreen() == color.getGreen()
                && c.getBlue() == color.getBlue() && c.getAlpha() == color
                .getAlpha());
    }

    /**
     * Calculates a hash code based on the {@link Color}'s RGB and alpha value
     * 
     * @param c
     *            The {@link Color} to calculate the hash code for
     * @return See above
     */
    private int colorHashcode(Color c) {
        final int prime = 31;
        int result = 1;
        result = prime * result + c.getRed();
        result = prime * result + c.getGreen();
        result = prime * result + c.getBlue();
        result = prime * result + c.getAlpha();
        return result;
    }

}
