 /*
 * org.openmicroscopy.shoola.agents.editor.uiComponents.CustomButton 
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
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.border.EmptyBorder;

//Third-party libraries

//Application-internal dependencies

/** 
 * A Custom button. 
 * Should use this button for all cases where the button contains a single
 * icon. 
 * This button is set to have an empty border, null background, 
 * and same font as other custom components (if used). 
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class CustomButton 
	extends JButton {
	
	public CustomButton() {
		super();
	}
	
	public CustomButton(String name) {
		super(name);
	}
	
	public CustomButton(String name, Icon icon) {
		super(name, icon);
	}
	
	public CustomButton(Icon icon) {
		super(icon);
	}
	
	public CustomButton(Action action) {
		super(action);
	}

	/**
	 * This is called by all constructors (may be called at other times?).
	 * So it is a handy place to set custom properties. 
	 * Doesn't seem to overwrite these properties if they are subsequently 
	 * set by other classes e.g. if you set a different border, that will be
	 * respected. This method doesn't seem to get called again to re-set 
	 * these props. 
	 * 
	 * @see JButton#updateUI()
	 */
	public void updateUI()
	{
		super.updateUI();
		
		this.setBackground(null);
		this.setFont(new CustomFont());
		int padding = UIUtilities.getInstance().
			getDimension(UIUtilities.EMPTY_BORDER_THINKNESS);
		this.setBorder(new EmptyBorder(padding,padding,padding,padding));
	}
	
	
}
