/*
 * org.openmicroscopy.shoola.util.ui.colourpicker.ColourListRenderer
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

import java.awt.Color;
import java.util.Objects;

/**
 * Item for the Lookup Table list
 */
public class LookupTableItem implements Comparable<LookupTableItem> {

    /** Item for being used as Separator */
    public static final LookupTableItem SEPARATOR = new LookupTableItem("---");

    /** The file name */
    private String filename;

    /** The color */
    private Color color;

    /**
     * More readable name (in case of lookup table it's generated from the
     * filename)
     */
    private String label;

    /**
     * Create new instance for a lookup table
     * 
     * @param filename
     *            The lut file name
     */
    public LookupTableItem(String filename) {
        this.filename = filename;
        this.label = generateLabel(filename);
    }

    /**
     * Create new instance for defined color
     * 
     * @param color
     *            The color
     * @param label
     *            The label
     */
    public LookupTableItem(Color color, String label) {
        this.color = color;
        this.label = label;
    }

    /**
     * Generates a more readable name for the given lut filename by removing
     * '*.lut' extension, underscores and using upper case at the beginning of
     * words.
     * 
     * @param filename
     *            The filename
     * @return See above
     */
    private String generateLabel(String filename) {
        filename = filename.replace(".lut", "");
        String[] parts = filename.replace(".lut", "").split("_");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            sb.append(part.substring(0, 1).toUpperCase());
            if (part.length() > 1) {
                sb.append(part.substring(1));
            }

            if (i < parts.length - 1)
                sb.append(' ');
        }

        return sb.toString();
    }

    /**
     * Checks if this {@link LookupTableItem} represents a lookup table file
     * 
     * @return See above
     */
    public boolean hasLookupTable() {
        return this.filename != null;
    }

    /**
     * Get the label text
     * 
     * @return See above
     */
    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return getLabel();
    }

    /**
     * Get the lut file name
     * 
     * @return See above
     */
    public String getFilename() {
        return this.filename;
    }

    /**
     * Get the color
     * 
     * @return See above
     */
    public Color getColor() {
        return this.color;
    }

    /**
     * @param filename
     *            The file name
     * @return <code>true</code> if the given filename matches the filename of
     *         this {@link LookupTableItem}
     */
    public boolean matchesFilename(String filename) {
        return this.filename == null ? false : this.filename.equals(filename);
    }

    @Override
    public int compareTo(LookupTableItem o) {
        return this.label.compareTo(o.label);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this);
    }

    @Override
    public boolean equals(Object obj) {
        return Objects.equals(this, obj);
    }

}
