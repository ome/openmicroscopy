 /*
 * org.openmicroscopy.shoola.agents.editor.uiComponents.CustomFont 
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
package org.openmicroscopy.shoola.agents.editor.uiComponents;

import java.awt.Font;

//Java imports

//Third-party libraries

//Application-internal dependencies

/**
 * A custom font used by {@link CustomLabel} and used by many other custom
 * UI components. 
 * Changing the size of this font may require you to change the size of
 * other components that have their size set explicitly. 
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class CustomFont
	extends Font {
	
	/**
	 * Creates an instance of this font, Sans Serif, plain, size 11. 
	 */
	public CustomFont() 
	{
		super("SansSerif", Font.PLAIN, 11);
	}
	
	/**
	 * Returns an instance of this Font with the specified size. 
	 * 
	 * @param size		Size of the font. 
	 * @return
	 */
	public static Font getFontBySize( int size)
	{
		Font f = new CustomFont();
		return f.deriveFont((float)size);
	}

}
