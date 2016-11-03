/*
 * org.openmicroscopy.shoola.util.ui.colourpicker.ColourIcon
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
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

//Java imports
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.Icon;

//Third-party libraries

//Application-internal dependencies

/** 
 * ColourIcon used in the ColourListRenderer to paint the colours next
 * to the colour names.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since OME2.2
 */
class ColourIcon
    implements Icon
{
	
    /** Colour of the icon */
    private Color   colour;
    
	/** Height of the icon. */
	private int 	h;
	
	/** Width of the icon. */	
	private int	w;

	/**
	 * Creates a new intance. 
	 * 
	 * @param width    The width of the icon.
	 * @param height   The height of the icon.
	 */
	ColourIcon(int width, int height)
	{
		w = width;
		h = height;
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
	public int getIconHeight() { return h; }

    /** 
     * Overridden to return the set width of the icon.
     * @see Icon#getIconWidth()
     */
	public int getIconWidth() { return w; }
	
	/**
     * Overridden to paint filled colour icon.
	 * @see Icon#paintIcon(Component, Graphics, int, int)
	 */
	public void paintIcon(Component c, Graphics og, int arg2, int arg3) 
	{
		Graphics2D g = (Graphics2D) og;
		g.setColor(colour);
		g.fillRect(4, 4, w-3, h-3);
		g.setColor(colour.darker());
		g.drawRect(4, 4, w-3, h-3);
	}

}
