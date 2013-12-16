/*
 * org.openmicroscopy.shoola.agents.dataBrowser.layout.LayoutFactory 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2013 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.dataBrowser.layout;


//Java imports

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.util.ViewerSorter;

/** 
 * A factory to create {@link Layout} objects.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since OME3.0
 */
public class LayoutFactory
{

    /** Identifies the <i>Squary</i> layout.*/
    public static final int SQUARY_LAYOUT = 1;

    /** Identifies the <i>Flat</i> layout.*/
    public static final int FLAT_LAYOUT = 2;

    /** Identifies the <i>Plate</i> layout.*/
    public static final int PLATE_LAYOUT = 3;

    /**
     * Creates the specified layout.
     * 
     * @param type One of the constants defined by this class.
     * @param sorter Class used to sort the nodes by date or alphabetically.
     * @param itemsPerRow The number of items per row.
     * @return A layout object for the given layout <code>type</code>.
     * @throws IllegalArgumentException If <code>type</code> is not one of
     *          the constants defined by this class.
     */
    public static Layout createLayout(int type, ViewerSorter sorter, int
            itemsPerRow)
    {
        if (sorter == null)
            throw new IllegalArgumentException("Invalid argument.");
        switch (type) {
        case SQUARY_LAYOUT:
            return new SquaryLayout(sorter, itemsPerRow);
        case FLAT_LAYOUT:
            return new FlatLayout(sorter);
        case PLATE_LAYOUT:
            return new PlateLayout();
        default:
            throw new IllegalArgumentException("Unsupported layout type: "
                    +type+".");
        }
    }

    /** 
     * Creates a {@link PlateLayout}
     * 
     * @return See above.
     */
    public static Layout createPlateLayout() { return new PlateLayout(); }

    /**
     * Returns the default layout.
     * 
     * @param sorter Class used to sort the nodes by date or alphabetically.
     * @param itemsPerRow The number of images per row.
     * @return See above.
     */
    public static Layout getDefaultLayout(ViewerSorter sorter, int itemsPerRow)
    { 
        return createLayout(getDefaultLayoutIndex(), sorter, itemsPerRow);
    }

    /**
     * Returns the default layout index.
     * 
     * @return See above.
     */
    public static int getDefaultLayoutIndex() { return SQUARY_LAYOUT; }

    /**
     * Returns the description corresponding to the layout specified by the
     * passed <i>type</i>.
     * 
     * @param type The layout's type.
     * @return See above.
     */
    public static String getLayoutDescription(int type)
    {
        switch (type) {
        case SQUARY_LAYOUT: return SquaryLayout.DESCRIPTION;
        case FLAT_LAYOUT: return FlatLayout.DESCRIPTION;
        case PLATE_LAYOUT: return PlateLayout.DESCRIPTION;
        default:
            return "";
        }
    }

}
