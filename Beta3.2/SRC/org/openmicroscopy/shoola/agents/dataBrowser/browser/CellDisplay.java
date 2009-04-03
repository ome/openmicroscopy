/*
 * org.openmicroscopy.shoola.agents.dataBrowser.browser.CellDisplay 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.dataBrowser.browser;


//Java imports
import java.awt.Color;

//Third-party libraries

//Application-internal dependencies

/** 
 * Creates a cell image, this is used to display wells.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta3
 */
public class CellDisplay
	extends ImageNode
{

	/** Indicates that the cell is a vertical cell. */
	public static final int	TYPE_VERTICAL = 0;
	
	/** Indicates that the cell is a horizontal cell. */
	public static final int	TYPE_HORIZONTAL = 1;
	
	/**
     * A placeholder to simulate an hierarchy object.
     * This is required because every {@link ImageDisplay} must have one,
     * but it's not actually used.  (It only avoids a <code>NPE</code> in
     * the constructor.)
     */
    static final Object FAKE_HIERARCHY_OBJECT = new Object();
    
    /** One of the constants defined by this class. */
    private int type;
    
    /** One of the constants defined by this class. */
    private int index;
    
    /** 
     * Creates a new root display. 
     * 
     * @param index Either the column or the row index depending on the type.
     * @param value The value of the cell.
     */
    public CellDisplay(int index, String value)  
    {
        this(index, value, TYPE_HORIZONTAL);
    }
    
    /** 
     * Creates a new root display. 
     * 
     * @param index Either the column or the row index depending on the type.
     * @param value The value of the cell.
     * @param type  One of the constants defined by this class.
     */
    public CellDisplay(int index, String value, int type)  
    {
        super(value, FAKE_HIERARCHY_OBJECT, null);
        clearDefaultButtons();
        setCollapsed(true);
        this.type = type;
        this.index = index;
        setHighlight(Color.WHITE);
        setTitleBarType(SMALL_TITLE_BAR);
        setListenToBorder(false);
        setTitle(value);
    }
    
    /**
     * Returns the type of cell. One of the constants defined by this class.
     * 
     * @return See above.
     */
    public int getIndex() { return index; }
    
    /**
     * Returns the type of cell, either {@link #TYPE_HORIZONTAL} or 
     * {@link #TYPE_VERTICAL}.
     * 
     * @return See above.
     */
    public int getType() { return type; }

}
