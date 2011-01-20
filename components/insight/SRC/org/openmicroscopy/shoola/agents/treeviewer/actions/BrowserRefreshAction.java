/*
 * org.openmicroscopy.shoola.agents.treeviewer.actions.BrowserRefreshAction
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.treeviewer.actions;


//Java imports
import java.awt.event.ActionEvent;
import javax.swing.Action;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.IconManager;
import org.openmicroscopy.shoola.agents.treeviewer.browser.Browser;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/**
 * Refreshes the browser.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 *         <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 *         <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class BrowserRefreshAction
	extends BrowserAction
{

    /** Name of the action. */
    private static final String NAME = "Refresh current tree";
    
    /** Description of the action. */
    private static final String DESCRIPTION = "Refresh the tree.";
    
    
    /** 
     * Sets the action enabled depending on the state of the {@link Browser}.
     * @see BrowserAction#onStateChange()
     */
    protected void onStateChange()
    {
    	 switch (model.getState()) {
             case Browser.LOADING_DATA:
             case Browser.LOADING_LEAVES:
             case Browser.COUNTING_ITEMS:  
                 setEnabled(false);
                 break;
             default:
            	 setEnabled(true);
         }
    }

    /**
     * Creates a new instance.
     * 
     * @param model Reference to the Model. Mustn't be <code>null</code>.
     */
	public BrowserRefreshAction(Browser model)
	{
		super(model);
		setEnabled(true);
        putValue(Action.SHORT_DESCRIPTION, 
                UIUtilities.formatToolTipText(DESCRIPTION));
        IconManager im = IconManager.getInstance();
        putValue(Action.SMALL_ICON, im.getIcon(IconManager.REFRESH));
	}
	
    /**
     * Refreshes the browser. 
     * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
     */
    public void actionPerformed(ActionEvent e)
    {
        model.refreshBrowser();
    }
    
}
