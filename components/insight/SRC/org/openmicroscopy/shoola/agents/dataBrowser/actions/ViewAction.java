/*
 * org.openmicroscopy.shoola.agents.dataBrowser.actions.ViewAction 
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
package org.openmicroscopy.shoola.agents.dataBrowser.actions;


//Java imports
import java.awt.event.ActionEvent;

import javax.swing.Action;

//Third-party libraries


//Application-internal dependencies
import org.openmicroscopy.shoola.agents.dataBrowser.IconManager;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.Browser;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageDisplay;
import org.openmicroscopy.shoola.agents.dataBrowser.view.DataBrowser;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

import pojos.ImageData;
import pojos.WellSampleData;

/** 
 * Action to view an image or a collection of images.
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
public class ViewAction 
	extends DataBrowserAction
{

	/** The default name of the action. */
    private static final String NAME = "View...";
    
    /** Description of the action. */
    private static final String DESCRIPTION = "View the selected image.";
    
    /**
     * Sets the action enabled depending on the currently selected display
     * @see DataBrowserAction#onDisplayChange(ImageDisplay)
     */
    protected void onDisplayChange(ImageDisplay node)
    {
    	if (node == null) {
            setEnabled(false);
            return;
        }
    	Object ho = node.getHierarchyObject();
    	setEnabled(ho instanceof ImageData || ho instanceof WellSampleData);
    }
    
	/**
	 * Creates a new instance.
	 * 
	 * 
	 * @param model Reference to the Model. Mustn't be <code>null</code>.
	 */
	public ViewAction(DataBrowser model)
	{
		super(model);
		putValue(Action.NAME, NAME);
		putValue(Action.SHORT_DESCRIPTION, 
				UIUtilities.formatToolTipText(DESCRIPTION));
		IconManager icons = IconManager.getInstance();
		putValue(Action.SMALL_ICON, icons.getIcon(IconManager.VIEWER));
	}
	
	/**
     * Views the selected images.
     * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
     */
    public void actionPerformed(ActionEvent e)
    {
    	Browser browser = model.getBrowser();
    	if (browser != null) {
    		browser.viewDisplay(browser.getLastSelectedDisplay(), true);
    	}
    }
	
}
