/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2010 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
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

import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.Icon;

import org.openmicroscopy.shoola.agents.treeviewer.IconManager;
import org.openmicroscopy.shoola.agents.treeviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewer;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

import omero.gateway.model.GroupData;

/** 
 * Selects the group the user is member of.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since 3.0-Beta4
 */
public class GroupSelectionAction 
	extends TreeViewerAction
{

	/** The name of the action. */
	public static final String NAME = "Switch Group";
	
	/** The name of the action. */
	public static final String NAME_ADD = "Add Group...";
	
	/** The name of the action. */
	public static final String DESCRIPTION = "Select the sole group to " +
			"display in the tree.";
	
	/** The name of the action. */
	public static final String DESCRIPTION_ADD = "Select the group to " +
			"add to the tree.";
	
	/** The group the logged in user is member of. */
	private GroupData group;
	
	/** Flag indicating to add the group or to switch.*/
	private boolean add;
	
	/** 
	 * Sets the icon and tool tip text according to the permissions of the 
	 * group.
	 */
	private void setPermissions()
	{
		IconManager im = IconManager.getInstance();
		Icon icon = im.getIcon(IconManager.PERSONAL);
        String desc = "";
        switch (group.getPermissions().getPermissionsLevel()) {
			case GroupData.PERMISSIONS_PRIVATE:
				desc = GroupData.PERMISSIONS_PRIVATE_TEXT;
				icon = im.getIcon(IconManager.PRIVATE_GROUP);
				break;
			case GroupData.PERMISSIONS_GROUP_READ:
				desc = GroupData.PERMISSIONS_GROUP_READ_TEXT;
				icon = im.getIcon(IconManager.READ_GROUP);
				break;
			case GroupData.PERMISSIONS_GROUP_READ_LINK:
				desc = GroupData.PERMISSIONS_GROUP_READ_LINK_TEXT;
				icon = im.getIcon(IconManager.READ_LINK_GROUP);
				break;
			case GroupData.PERMISSIONS_GROUP_READ_WRITE:
				desc = GroupData.PERMISSIONS_GROUP_READ_WRITE_TEXT;
				icon = im.getIcon(IconManager.READ_WRITE_GROUP);
				break;
			case GroupData.PERMISSIONS_PUBLIC_READ:
				desc = GroupData.PERMISSIONS_PUBLIC_READ_TEXT;
				icon = im.getIcon(IconManager.PUBLIC_GROUP);
				break;
			case GroupData.PERMISSIONS_PUBLIC_READ_WRITE:
				desc = GroupData.PERMISSIONS_PUBLIC_READ_WRITE_TEXT;
				icon = im.getIcon(IconManager.PUBLIC_GROUP);
		}
        
        putValue(Action.SMALL_ICON, icon);
        putValue(Action.SHORT_DESCRIPTION, UIUtilities.formatToolTipText(desc));
	}
	
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
	 * @param add Passes <code>true</code> to add the group, <code>false</code>
	 * to switch.
	 */
	public GroupSelectionAction(TreeViewer model, GroupData group, boolean add)
	{
		super(model);
		if (group == null)
			throw new IllegalArgumentException("No group specified.");
		this.group = group;
		this.add = add;
		name = group.getName();
		putValue(Action.NAME, name);
        setPermissions();
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
	 * Returns the id of the group hosted by this component.
	 * 
	 * @return See above.
	 */
	public long getGroupId() { return group.getId(); }
	
	/**
	 * Sets the default group for the currently logged in user.
	 * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
	 */
    public void actionPerformed(ActionEvent e)
    {
    	model.retrieveUserGroups(null, group);
    	//model.setUserGroup(group, add);
    }
	
}
