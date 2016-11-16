/*
 * org.openmicroscopy.shoola.util.roi.figures.MeasureLineFigure 
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
package org.openmicroscopy.shoola.util.roi.figures;

import org.jhotdraw.draw.ArrowTip;
import org.jhotdraw.draw.LineDecoration;

/**
 * Maps OMERO shape attributes for start/end markers to the corresponding
 * JHotDraw {@link LineDecoration}s.
 * 
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 */
public enum Cap {

    /** No decoration */
    NONE("None", null),

    /** Arrow */
    ARROW("Arrow", ArrowTip.class);

    private String value = null;
    private Class<? extends LineDecoration> type = null;

    private Cap(String value, Class<? extends LineDecoration> type) {
        this.value = value;
        this.type = type;
    }

    /**
     * Get the OMERO shape attribute value
     * 
     * @return See above.
     */
    public String getValue() {
        return value;
    }

    /**
     * Get the JHotDraw {@link LineDecoration} class
     * 
     * @return See above.
     */
    public Class<? extends LineDecoration> getType() {
        return type;
    }

    /**
     * Creates a new JHotDraw {@link LineDecoration} instance
     * 
     * @return See above.
     */
    public <T extends LineDecoration> T newLineDecorationInstance() {
        if (type == null)
            return null;

        T obj = null;
        try {
            obj = (T) type.newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Could not instantiate " + type, e);
        }
        return obj;
    }

    @Override
    public String toString() {
        return value;
    }

    /**
     * Find the specific Cap with the given value
     * 
     * @param value
     *            The value to look for
     * @return See above.
     */
    public static Cap findByValue(String value) {
        for (Cap c : values()) {
            if (c.getValue().equalsIgnoreCase(value))
                return c;
        }
        return null;
    }

    /**
     * Find the specific Cap with the given {@link LineDecoration} class
     * 
     * @param type
     *            The class to look for
     * @return See above.
     */
    public static Cap findByType(Class<? extends LineDecoration> type) {
        for (Cap c : values()) {
            if (c.getType() != null && c.getType().equals(type))
                return c;
        }
        return null;
    }

    /**
     * Find the specific Cap with the given {@link LineDecoration} instance
     * 
     * @param type
     *            The {@link LineDecoration} instance to look for
     * @return See above.
     */
    public static <T extends LineDecoration> Cap findByPrototype(T type) {
        if (type != null) {
            for (Cap c : values()) {
                if (c.getType() != null
                        && c.getType().isAssignableFrom(type.getClass()))
                    return c;
            }
        }
        return null;
    }
}
