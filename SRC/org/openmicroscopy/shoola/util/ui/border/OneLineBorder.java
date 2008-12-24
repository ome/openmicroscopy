/*
 * org.openmicroscopy.shoola.util.ui.border.OneLineBorder 
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
package org.openmicroscopy.shoola.util.ui.border;


//Java imports
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;

import javax.swing.border.AbstractBorder;

//Third-party libraries

//Application-internal dependencies

/** 
 * Creates a line border with only one line, either a top, bottom, left or right
 * line.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class OneLineBorder 
	extends AbstractBorder
{

	/** Indicates to draw a line at the top of the component. */
	public static final int TOP = 0;
	
	/** Indicates to draw a line at the bottom of the component. */
	public static final int BOTTOM = 1;
	
	/** Indicates to draw a line on the left-hand side of the component. */
	public static final int LEFT = 2;
	
	/** Indicates to draw a line on the right-hand side of the component. */
	public static final int RIGHT = 3;
	
	/** The default line color. */
	private static final Color DEFAULT_COLOR = Color.black;
	
	/** The color of the line. */
	private Color 	color;
	
	/** The index of the line. One of the constants defined by this class. */
	private int 	index;
	
	/** The thickness of the line. */
	private int		thickness;
	
	/**
	 * Checks if the index is supported and sets the value.
	 * 
	 * @param value The value to handle.
	 */
	private void checkIndex(int value)
	{
		switch (value) {
			case TOP:
			case BOTTOM:
			case LEFT:
			case RIGHT:
				index = value;
				break;
			default:
				index = TOP;
		}
	}
	
	/** Creates a default instance. */
	public OneLineBorder()
	{
		this(TOP, DEFAULT_COLOR, 1);
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param index The location of the line. One of the constants defined 
	 * 				by this class.
	 */
	public OneLineBorder(int index)
	{
		this(index, DEFAULT_COLOR, 1);
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param index 	The location of the line. One of the constants defined 
	 * 					by this class.
	 * @param thickness The thickness of the line.
	 */
	public OneLineBorder(int index, int thickness)
	{
		this(index, DEFAULT_COLOR, thickness);
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param color The color of the line.
	 */
	public OneLineBorder(Color color)
	{
		this(TOP, color, 1);
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param index 	The location of the line. One of the constants defined 
	 * 					by this class.
	 * @param color 	The color of the line. 
	 */
	public OneLineBorder(int index, Color color)
	{
		this(index, color, 1);
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param index 	The location of the line. One of the constants defined 
	 * 					by this class.
	 * @param color 	The color of the line.
	 * @param thickness The thickness of the line.
	 */
	public OneLineBorder(int index, Color color, int thickness)
	{
		checkIndex(index);
		if (color == null) color = DEFAULT_COLOR;
		this.color = color;
		if (thickness < 1) thickness = 1;
		this.thickness = thickness;
	}
	
	/**
     * Returns the insets of the border.
     * 
     * @param c The component for which this border insets value applies.
     * @return See above.
     */
    public Insets getBorderInsets(Component c)
    {
        return new Insets(thickness, thickness, thickness, thickness);
    }

    /** 
     * Reinitializes the insets parameter with this Border's current Insets.
     *  
     * @param c 	 The component for which this border insets value applies.
     * @param insets The object to be reinitialized.
     * @return See above.
     */
    public Insets getBorderInsets(Component c, Insets insets)
    {
        insets.left = insets.top = insets.right = insets.bottom = thickness;
        return insets;
    }

    /**
     * Returns the color of the border.
     * 
     * @return See above.
     */
    public Color getLineColor() { return color; }

    /**
     * Returns the thickness of the border.
     * 
     * @return See above.
     */
    public int getThickness() { return thickness; }
    
	 /**
     * Paints the border for the specified component with the 
     * specified position and size.
     * 
     * @param c 	 The component for which this border is being painted.
     * @param g 	 The paint graphics.
     * @param x 	 The x position of the painted border.
     * @param y 	 The y position of the painted border.
     * @param width  The width of the painted border.
     * @param height The height of the painted border.
     */
    public void paintBorder(Component c, Graphics g, int x, int y, int width, 
    					int height) 
    {
    	int i;
    	Color color = getLineColor();
    	if (color == null) color = c.getBackground();
    	if (color == null) color = DEFAULT_COLOR;
    	g.setColor(color);
    	switch (index) {
			case TOP:
				for (i = 0; i < thickness; i++) 
					g.drawLine(x, y+i, x+width-1, y+i);
				break;
			case BOTTOM:
				for (i = 0; i < thickness; i++) 
					g.drawLine(x, y+height-1-i, x+width-1, y+height-1-i);
				break;
		}
    }
    
}
