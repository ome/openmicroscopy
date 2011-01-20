 /*
 * uiComponents.CustomLabel 
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
package org.openmicroscopy.shoola.agents.editor.uiComponents;

//Java imports

import javax.swing.Icon;
import javax.swing.JLabel;

//Third-party libraries

//Application-internal dependencies

/** 
 * A Custom Label, which should be used by the UI instead of using 
 * JLabel. Sets the font to CUSTOM FONT.
 * 
 * This font is also used by many other Custom UI components in this
 * package, making it easy to change the font in many components in 
 * one place (here!). 
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class CustomLabel 
	extends JLabel {
	
	private int fontSize;
	
	/**
	 * Simply delegates to JLabel superclass.
	 */
	public CustomLabel() {
		super();
		setFont();
	}
	
	/**
	 * Simply delegates to JLabel superclass.
	 */
	public CustomLabel(Icon image) {
		super(image);
		setFont();
	}
	
	/**
	 * Simply delegates to JLabel superclass.
	 */
	public CustomLabel(String text) {
		super(text);
		setFont();
	}
	
	/**
	 * Simply delegates to JLabel superclass.
	 */
	public CustomLabel(String text, int fontSize) {
		super(text);
		this.fontSize = fontSize;
		setFont();
	}
	
	private void setFont()
	{
		if (fontSize == 0)
		setFont(new CustomFont());
		
		else {
			setFont(CustomFont.getFontBySize(fontSize));
		}
	}
}
