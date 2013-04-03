/*
 * org.openmicroscopy.shoola.util.ui.border.PartialLineBorder 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
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
import javax.swing.border.LineBorder;

//Third-party libraries

//Application-internal dependencies

/** 
 * Border to remove the top or bottom part of a <code>LineBorder</code>.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class PartialLineBorder
	extends LineBorder
{

	/** Indicates to remove the top segment of the border. */
	public static final int TOP_REMOVE = 0;
	
	/** Indicates to remove the bottom segment of the border. */
	public static final int BOTTOM_REMOVE = 1;
	
	/** Indicates to keep the top segment of the border. */
	public static final int TOP_KEEP = 2;
	
	/** Indicates to keep the bottom segment of the border. */
	public static final int BOTTOM_KEEP = 3;
	
	/** Indicates with segment of the border has to be removed. */
	private int removeIndex;
	
    /** 
     * Creates a line border with the specified color and a 
     * thickness equals to 1. The bottom part of the border will be removed.
     * 
     * @param color The color for the border.
     */
	public PartialLineBorder(Color color)
	{
		super(color);
		removeIndex = BOTTOM_REMOVE;
	}

    /**
     * Creates a line border with the specified color and thickness.
     * The bottom part of the border will be removed.
     * @param color the color of the border
     * @param thickness the thickness of the border
     */
	public PartialLineBorder(Color color, int thickness) 
	{
		super(color, thickness);
		removeIndex = BOTTOM_REMOVE;
	}
	
	/**
	 * Sets the index. One of the constants defined by this class.
	 * 
	 * @param index The value to set.
	 */
    public void setRemoveIndex(int index) { removeIndex = index; }
    
    /**
     * Overridden to remove one segment of the border, either the top or 
     * bottom part.
     * @see LineBorder#paintBorder(Component, Graphics, int, int, int, int)
     */
    public void paintBorder(Component c, Graphics g, int x, int y, int width, 
    						int height) 
    {
    	int i;
    	g.setColor(c.getBackground());
    	switch (removeIndex) {
    		case TOP_KEEP:
    			for (i = 0; i < thickness; i++)  {
    				if (!roundedCorners)
    					g.drawRect(x+i, y+i, width-2*i-1, height-2*i-1);
    				else
    					g.drawRoundRect(x+i, y+i, width-2*i-1, height-2*i-1, 
    							thickness, thickness);
    		    }
    			g.setColor(getLineColor());
    			g.drawLine(x, y, x+width-1, y);
    			break;
    		case BOTTOM_KEEP:
    			for (i = 0; i < thickness; i++)  {
    				if (!roundedCorners)
    					g.drawRect(x+i, y+i, width-2*i-1, height-2*i-1);
    				else
    					g.drawRoundRect(x+i, y+i, width-2*i-1, height-2*i-1, 
    							thickness, thickness);
    		    }
    			g.setColor(getLineColor());
    			g.drawLine(x, y+height-1, x+width-1, y+height-1);
    			
    			break;
			case TOP_REMOVE:
				super.paintBorder(c, g, x, y, width, height);
				for (i = 0; i < thickness; i++) 
				    g.drawLine(x+i, y+i, x+width-i-1, y+i);
				break;
			case BOTTOM_REMOVE:
			default:
				super.paintBorder(c, g, x, y, width, height);
				 for (i = 0; i < thickness; i++) 
					 g.drawLine(x+width-i-1, y+height-i-1, x+i+1, y+height-i-1);
		}
    }
    
}
