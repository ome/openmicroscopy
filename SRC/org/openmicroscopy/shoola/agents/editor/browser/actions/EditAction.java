 /*
 * org.openmicroscopy.shoola.agents.editor.browser.actions.EditAction 
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
package org.openmicroscopy.shoola.agents.editor.browser.actions;

//Java imports
import java.awt.event.ActionEvent;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.editor.IconManager;
import org.openmicroscopy.shoola.agents.editor.browser.Browser;

/** 
 * An action that toggles the Editing state of the Browser.
 * Turns on/off editing. 
 * ** This Action is not currently used. **
 * Haven't deleted this Action as it may be re-implemented. 
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class EditAction 
	extends BrowserAction
{
	
	/**
	 * Creates an instance.
	 * 
	 * @param model		Reference to the Model. Mustn't be <code>null</code>.
	 */
	public EditAction(Browser model)
	{
		super (model);
		
		setEnabled(true);
		
		setName("Edit");
		setIcon(IconManager.CONFIGURE_ICON);
		onStateChange(); 	// update description.
	}

	/** 
     * Turns on/off editing of the Browser
     * 
     * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
     */
    public void actionPerformed(ActionEvent e) 
    {
    	int state = model.getState();
    	//model.setEditable(state == Browser.TREE_DISPLAY);
    }
    
    /**
     * Update the description, based on the state
     * 
     */
    public void onStateChange() 
    {
    	int state = model.getState();
    	
    	if (state == Browser.TREE_DISPLAY) {
    		setDescription("Enable editing of the file");
    	} else {
    		setDescription("Turn off editing of the file");
    	}
    }
}
