/*
 * org.openmicroscopy.shoola.agents.treeviewer.actions.GroupSelectionAction 
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
package org.openmicroscopy.shoola.agents.treeviewer.actions;



//Java imports
import java.awt.event.ActionEvent;

import javax.swing.Action;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.IconManager;
import org.openmicroscopy.shoola.agents.treeviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewer;
import pojos.GroupData;

/** 
 * Selects the group the user is member of.
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
public class GroupSelectionAction 
	extends TreeViewerAction
{

	/** The group the logged in user is member of. */
	private GroupData group;
	
    /** 
     * Enables the action if the browser is not ready.
     * @see TreeViewerAction#onBrowserStateChange(Browser)
     */
    protected void onBrowserStateChange(Browser browser)
    {
    	if (browser == null) return;
    	setEnabled(browser.getState() == Browser.READY);
    }
    
	/**
	 * Creates a new instance.
	 * 
	 * @param model Reference to the Model. Mustn't be <code>null</code>.
	 * @param group The group the logged in user is a member of.
	 */
	public GroupSelectionAction(TreeViewer model, GroupData group)
	{
		super(model);
		if (group == null)
			throw new IllegalArgumentException("No group specified.");
		this.group = group;
		name = group.getName();
		putValue(Action.NAME, name);
        IconManager im = IconManager.getInstance();
        putValue(Action.SMALL_ICON, im.getIcon(IconManager.PERSONAL));
	}
	
	/**
	 * Returns <code>true</code> if the passed id corresponds to the group
	 * hosted by this component, <code>false</code> otherwise.
	 * 
	 * @param groupID The id to check.
	 * @return See above.
	 */
	public boolean isSameGroup(long groupID)
	{
		return group.getId() == groupID;
	}
	
	/**
	 * Sets the default group for the currently logged in user.
	 * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
	 */
    public void actionPerformed(ActionEvent e) { model.setUserGroup(group); }
	
}
