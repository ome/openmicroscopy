/*
 * org.openmicroscopy.shoola.util.ui.lens.LensColourAction 
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
package org.openmicroscopy.shoola.util.ui.lens;

//Java imports
import java.awt.Color;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;

//Third-party libraries

//Application-internal dependencies

/** 
 * Sets the color of the lens border.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */

class LensColorAction 		
	extends AbstractAction
{
	
	/** The number of items in the actions. */
	final static int  		MAX = 12;
	
	/** Identify the default color. */
	final static int 		DEFAULT = 0;
	
	/** Lens is to be red. */
	final static int		RED = 1;
	
	/** Lens is to be orange. */
	final static int		ORANGE = 2;
	
	/** Lens is to be yellow. */
	final static int		YELLOW = 3;
	
	/** Lens is to be green. */
	final static int		GREEN = 4;
	
	/** Lens is to be blue. */
	final static int		BLUE = 5;
	
	/** Lens is to be indigo. */
	final static int		INDIGO = 6;
	
	/** Lens is to be violet. */
	final static int		VIOLET = 7;
	
	/** Lens is to be black. */
	final static int		BLACK = 8;
	
	/** Lens is to be light gray. */
	final static int		LIGHT_GRAY = 9;
	
	/** Lens is to be dark gray. */
	final static int		DARK_GRAY = 10;
	
	/** Lens is to be white. */
	final static int		WHITE = 11;
	
	/** the parent component of the magnifying lens. */
	private LensComponent	lens;
	
	/** The index which refers to the change in the lens colour.*/
	private int				index;
	
	/** Names for each action associated with the change in lens colour. */
	private static String[]     names;
	   
	static {
	        names = new String[MAX];
	        names[DEFAULT] = "Default";
	        names[RED] = "Red";
	        names[ORANGE] = "Orange";
	        names[YELLOW] = "Yellow";
	        names[GREEN] = "Green";
	        names[BLUE] = "Blue";
	        names[INDIGO] = "Indigo";
	        names[VIOLET] = "Violet";
	        names[BLACK] = "Black";
	        names[LIGHT_GRAY] = "Light Gray";
	        names[DARK_GRAY] = "Dark Gray";
	        names[WHITE] = "White";
	}
	
	/** Colours associated with the lens colour action. */
	private static Color[]     colors;
	   
	static {
	        colors = new Color[MAX+1];
	        colors[DEFAULT] = LensUI.DEFAULT_LENS_COLOR;
	        colors[RED] = Color.red;
	        colors[ORANGE] = Color.orange;
	        colors[YELLOW] = Color.yellow;
	        colors[GREEN] = Color.green;
	        colors[BLUE] = Color.blue;
	        colors[INDIGO] = new Color(75, 0, 130);
	        colors[VIOLET] = new Color(238, 130, 238);
	        colors[BLACK] = Color.black;
	        colors[LIGHT_GRAY] = Color.lightGray;
	        colors[DARK_GRAY] = Color.darkGray;
	        colors[WHITE] = Color.white;
	}

	
	/** 
	 * Controls if the specified index is valid.
	 * 
	 * @param i The index to check.
	 */
	private void checkIndex(int i)
	{
		switch (i) {
			case DEFAULT:
			case	RED:
			case ORANGE:
			case YELLOW:
			case GREEN:
			case BLUE:
			case INDIGO:
			case VIOLET:
			case BLACK:
			case LIGHT_GRAY:
			case DARK_GRAY:
			case WHITE:
				return;
			default:
				throw new IllegalArgumentException("Index not supported.");
		}
	}
   
	/**
	 * Creates a new instance. 
	 * 
	 * @param lens          The parent component. Mustn't be <code>null</code>.
	 * @param displayIndex  The index of the action. One of the constants
	 * 						defined by this class.
	 */
	LensColorAction(LensComponent lens, int displayIndex)
	{
		if (lens == null)
			throw new IllegalArgumentException("No parent.");
		this.lens = lens;
		checkIndex(displayIndex);
		index = displayIndex;
		putValue(Action.NAME, names[index]);
		
	}
	
	/**
	 * Returns the color corresponding to the {@link #index}.
	 * 
	 * @return See above.
	 */
	Color getColor() { return colors[index]; }
	
	/**
	 * Returns the name corresponding to the {@link #index}.
	 * 
	 * @return See above.
	 */
	String getName() { return names[index]; }
	
	/** 
     * Sets the color of the lens border.
	 * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e)
    {
		lens.setLensColour(colors[index]);
    }
   
}


