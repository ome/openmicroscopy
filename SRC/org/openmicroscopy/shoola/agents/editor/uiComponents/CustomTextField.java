 /*
 * uiComponents.CustomTextField 
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

import java.awt.Dimension;

import javax.swing.JTextField;

//Third-party libraries

//Application-internal dependencies

/** 
 * A Custom text field.
 * Uses the same font as other custom UI components. 
 * Overrides getPreferredSize() so that the field is at least a minimum width.
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class CustomTextField
	extends JTextField {
	
	private int customMinWidth = 0;
	
	public CustomTextField(String text) {
		super (text);
		init();
	}
	
	public CustomTextField() {
		super();
		init();
	}
	
	/**
	 * Creates an instance
	 * 
	 * @param minWidth		Field will appear at least this wide.
	 * Will expand bigger if needed. 
	 */
	public CustomTextField(int minWidth) {
		super();
		init();
		setMinWidth(minWidth);
	}
	
	/**
	 * This is called manually by the constructors specified in this class.
	 * Sets the font. 
	 */
	private void init() {
		this.setFont(new CustomFont());
	}

	/**
	 * Returns a dimension that is at least the minimum width. 
	 * Default is UIsizes.TEXT_FIELD_MIN_WIDTH, but can be set manually
	 * using setMinWidth();
	 */
	public Dimension getPreferredSize() {
		
		Dimension size = super.getPreferredSize();
		
		int h = (int)size.getHeight();
		int w = (int)size.getWidth();
		
		int minW;
		if (customMinWidth > 0) {
			minW = customMinWidth;
		} else {
			minW = UIUtilities.getInstance().
				getDimension(UIUtilities.TEXT_FIELD_MIN_WIDTH);
		}
		w = Math.max(w, minW);
		
		size.setSize(w, h);
		
		return size;
	}
	
	public void setMinWidth(int minWidth) {
		customMinWidth = minWidth;
	}
}
