/*
 * org.openmicroscopy.shoola.util.ui.colourpicker.ColourObject 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2009 University of Dundee. All rights reserved.
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

//Third-party libraries

//Application-internal dependencies

/** 
 * Utility object.
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
public class ColourObject
{

	/** The selected color. */
	private Color color;
	
	/** The description associated to it. */
	private String description;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param color		  The color to set.
	 * @param description The description associated to the color.
	 */
	public ColourObject(Color color, String description)
	{
		this.color = color;
		this.description = description;
	}
	
	/**
	 * Sets the color.
	 * 
	 * @param color The value to set.
	 */
	public void setColor(Color color) { this.color = color; }
	
	/**
	 * Sets the description.
	 * 
	 * @param description The value to set.
	 */
	public void setDescription(String description)
	{ 
		this.description = description;
	}
	
	/**
	 * Returns the color.
	 * 
	 * @return See above.
	 */
	public Color getColor() { return color; }
	
	/**
	 * Returns the description.
	 * 
	 * @return See above.
	 */
	public String getDescription() { return description; }
	
}
