 /*
 * org.openmicroscopy.shoola.agents.editor.browser.ToolBar 
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
package org.openmicroscopy.shoola.agents.editor.browser;

//Java imports

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;

//Third-party libraries

//Application-internal dependencies

import org.openmicroscopy.shoola.agents.editor.uiComponents.CustomButton;

/** 
 * A toolBar for the browser. 
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class ToolBar 
	extends JPanel {
	
	/**
	 * The controller.
	 */
	private BrowserControl 			controller;

	/**
	 * Builds the UI. 
	 */
	private void buildUI()
	{
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		
		add(createButton(BrowserControl.EDIT));
	}
	
	/**
	 * Convenience method for creating buttons. 
	 * 
	 * @param actionID		The ID of the action, retrieved from controller.
	 * @return			A button displaying the specified action.
	 */
	private JButton createButton(int actionID)
	{
		JButton b = new CustomButton(controller.getAction(actionID));
		b.setText("");
		return b;
	}
	
	/**
	 * Creates an instance.
	 * 
	 * @param controller		The controller as a source of Actions.
	 */
	ToolBar(BrowserControl controller)
	{
		if (controller == null) 
            throw new NullPointerException("No controller.");
        this.controller = controller;
        buildUI();
	}
}
