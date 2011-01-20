/*
 * org.openmicroscopy.shoola.util.ui.omeeditpane.AttributeSetFactory 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2010 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.util.ui.omeeditpane;


//Java imports
import java.awt.Color;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

//Third-party libraries

//Application-internal dependencies

/** 
 * Helper class offering a collection of methods to create 
 * <code>SimpleAttributeSet</code>.
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
public class AttributeSetFactory 
{

	/** The default color. */
	public static final Color DEFAULT_COLOR = Color.BLACK;
	
	/** The default color for URL. */
	public static final Color DEFAULT_URL = Color.BLUE;
	
	/** The default color for image, dataset etc. linkages. */
	public static final Color DEFAULT_LINK = Color.BLUE;
	
	/** The default color for image, dataset etc. linkages. */
	public static final Color PROTOCOL_LINK = Color.BLUE;
	
	/**
	 * Identifies URL.
	 * 
	 * @return See above.
	 */
	public static SimpleAttributeSet createURLAttributeSet()
	{
		SimpleAttributeSet urlSet = new SimpleAttributeSet();
		StyleConstants.setForeground(urlSet, DEFAULT_URL);
		StyleConstants.setUnderline(urlSet, true);
		return urlSet;
	}
	
	/**
	 * Creates a default set.
	 * 
	 * @return See above.
	 */
	public static SimpleAttributeSet createDefaultAttibuteSet()
	{
		return createDefaultAttibuteSet(DEFAULT_LINK);
	}
	
	/**
	 * Creates a set with the specified foreground color.
	 * 
	 * @param color The color to set.
	 * @return See above.
	 */
	public static SimpleAttributeSet createDefaultAttibuteSet(Color color)
	{
		if (color == null) color = DEFAULT_LINK;
		SimpleAttributeSet set = new SimpleAttributeSet();
		StyleConstants.setForeground(set, color);
		return set;
	}
	
	
}
