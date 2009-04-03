/*
 * org.openmicroscopy.shoola.util.ui.ColourIcon 
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
package org.openmicroscopy.shoola.util.ui;



//Java imports
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.Icon;

//Third-party libraries

//Application-internal dependencies

/** 
 * Used to color items as icons.
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
public class ColourIcon 
	implements Icon
{
	
	/** The default width of the icon. */
	public static final int DEFAULT_WIDTH = 16;
	
	/** The default height of the icon. */
	public static final int DEFAULT_HEIGHT = 16;
	
    /** Colour of the icon */
    private Color   colour;
    
	/** The height of the icon. */
	private int		height;
	
	/** The width of the icon. */	
	private int		width;

	/** Creates a default icon. */
	public ColourIcon()
	{
		this(DEFAULT_WIDTH, DEFAULT_HEIGHT);
	}
	
	/**
	 * Creates a new intance. 
	 * 
	 * @param d The dimension of the icon.
	 */
	public ColourIcon(Dimension d)
	{
		if (d == null) {
			width = DEFAULT_WIDTH;
			height = DEFAULT_HEIGHT;
		} else {
			width = d.width;
			height = d.height;
		}
	}
	
	/**
	 * Creates a new intance. 
	 * 
	 * @param width		The width of the icon.
	 * @param height	The height of the icon.
	 */
	public ColourIcon(int width, int height)
	{
		if (width <= 0) width = DEFAULT_WIDTH;
		if (height <= 0) height = DEFAULT_HEIGHT;
		this.width = width;
		this.height = height;
	}

	/**
	 * Creates a new intance. 
	 * 
	 * @param width		The width of the icon.
	 * @param height	The height of the icon.
	 * @param color		The color to paint.
	 */
	public ColourIcon(int width, int height, Color color)
	{
		if (width <= 0) width = DEFAULT_WIDTH;
		if (height <= 0) height = DEFAULT_HEIGHT;
		this.width = width;
		this.height = height;
		setColour(color);
	}
	
	/**
	 * Creates a new intance. 
	 * 
	 * @param d 	The dimension of the icon.
	 * @param color	The color to paint.
	 */
	public ColourIcon(Dimension d, Color color)
	{
		if (d == null) {
			width = DEFAULT_WIDTH;
			height = DEFAULT_HEIGHT;
		} else {
			width = d.width;
			height = d.height;
		}
		setColour(color);
	}
	
	/**
	 * Sets the colour of the icon.
	 * 
	 * @param c The colour to set.
	 */
	public void setColour(Color c) { colour = c; }
	
	/** 
     * Overridden to return the set height of the icon.
	 * @see Icon#getIconHeight()
	 */
	public int getIconHeight() { return height; }

    /** 
     * Overridden to return the set width of the icon.
     * @see Icon#getIconWidth()
     */
	public int getIconWidth() { return width; }
	
	/**
     * Overridden to paint filled colour icon.
	 * @see Icon#paintIcon(Component, Graphics, int, int)
	 */
	public void paintIcon(Component c, Graphics g, int x, int y) 
	{
		Graphics2D g2D = (Graphics2D) g;
		if (colour != null) {
			g2D.setColor(colour);
			g2D.fillRect(4, 4, width-3, height-3);
			g2D.setColor(colour.darker());
			g2D.drawRect(4, 4, width-3, height-3);
		}
	}

}
