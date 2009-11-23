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
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

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

	/** 
	 * Bound property indicating to show the dialog to set the color 
	 * and its description.
	 */
	public static final String	DESCRIPTOR_PROPERTY = "descriptor";
	
	/** Indicates that the cell is a vertical cell. */
	public static final int		TYPE_VERTICAL = 0;
	
	/** Indicates that the cell is a horizontal cell. */
	public static final int		TYPE_HORIZONTAL = 1;
	
	/** The default color of the cell. */
	public static final Color	DEFAULT_COLOR = Color.WHITE;
	
	/**
     * A place holder to simulate an hierarchy object.
     * This is required because every {@link ImageDisplay} must have one,
     * but it's not actually used.  (It only avoids a <code>NPE</code> in
     * the constructor.)
     */
    static final Object FAKE_HIERARCHY_OBJECT = new Object();
    
    /** The tooltip text if it is a column. */
    private static final String	TEXT_COLUMN = 
    	"Click to edit the selected column.";
    
    /** The tooltip text if it is a row. */
    private static final String	TEXT_ROW = "Click to edit the selected row.";
    
    /** One of the constants defined by this class. */
    private int 	type;
    
    /** One of the constants defined by this class. */
    private int 	index;
    
    /** The location of the mouse click. */
    private Point 	location;
    
    /** The description of the column or row. */
    private String 	description;
    
    /** The color of the column if vertical or the row if horizontal. */
    private Color	cellColor;
    
    /**
     * Sets the location and fires a property change.
     * 
     * @param location The mouse click location.
     */
    private void showDescriptor(Point location)
    {
    	this.location = location;
    	firePropertyChange(DESCRIPTOR_PROPERTY, null, this);
    }
    
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
    	super(value, FAKE_HIERARCHY_OBJECT, null, SMALL_BAR);
    	clearDefaultButtons();
    	setCollapsed(true);
    	this.type = type;
    	this.index = index;
    	super.setHighlight(DEFAULT_COLOR);
    	cellColor = DEFAULT_COLOR;
    	setTitleBarType(SMALL_TITLE_BAR);
    	setListenToBorder(false);
    	setTitle(value);
    	if (type == TYPE_HORIZONTAL) setToolTipText(TEXT_COLUMN);
    	else setToolTipText(TEXT_ROW);
    	addMouseListener(new MouseAdapter() {
    		public void mousePressed(MouseEvent evt)
    		{
    			showDescriptor(evt.getPoint());
    		}
    	});
    	//setNodeDecoration();
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

    /**
     * Returns <code>false</code> if the highlight color is
     * the default color or <code>null</code>, <code>true</code> otherwise.
     * 
     * @return See above.
     */
    public boolean isSpecified()
    {
    	if (cellColor == null) return false;
    	return !(cellColor.getRed() == DEFAULT_COLOR.getRed() &&
    			cellColor.getGreen() == DEFAULT_COLOR.getGreen() && 
    			cellColor.getBlue() == DEFAULT_COLOR.getBlue() && 
    			cellColor.getAlpha() == DEFAULT_COLOR.getAlpha());
    }
    
    /**
     * Returns the location.
     * 
     * @return See above.
     */
    public Point getLocation() { return location; }
    
    /**
     * Sets the description.
     * 
     * @param description The value to set.
     */
    public void setDescription(String description) 
    { 
    	this.description = description;
    	if (description != null) setToolTipText(description);
    }
    
    /**
     * Returns the description.
     * 
     * @return See above.
     */
    public String getDescription() { return description; }
    
    /** 
     * Overridden to set the edit icon. 
     * @see ImageNode#setNodeDecoration()
     */
    public void setNodeDecoration()
    {
    }
    
    /**
     * Overridden to return the color associated to the column or row
     * @see ImageNode#getHighlight()
     */
    public Color getHighlight() { return cellColor; }
    
    /**
     * Overridden to make sure that the default color is set.
     * @see ImageNode#setHighlight(Color) 
     */
    public void setHighlight(Color highlight)
    {
    	if (highlight == null) highlight = DEFAULT_COLOR;
    	cellColor = highlight;
    }
    
    /**
     * Overridden to be on the save side.
     * @see ImageNode#setBounds(int, int, int, int)
     */
    public void setBounds(int x, int y, int w, int h)
    {
    	super.setBounds(x, y, w, h);
    }
    
}
