/*
 * org.openmicroscopy.shoola.agents.treeviewer.actions.MoveToAction 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2012 University of Dundee & Open Microscopy Environment.
 *  All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
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
import javax.swing.Icon;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.IconManager;
import org.openmicroscopy.shoola.agents.treeviewer.TreeViewerAgent;
import org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewer;
import org.openmicroscopy.shoola.env.data.model.AdminObject;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

import pojos.GroupData;

/** 
 * 
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since Beta4.4
 */
public class MoveToAction
	extends TreeViewerAction
{

	/** The name of the action.*/
	public static final String NAME = "Move to";
	
	/** The name of the action.*/
	public static final String DESCRIPTION = "Select the group where to" +
			" move the data.";
	
	/** The group to move the data to.*/
	private GroupData group;
	
	/** 
	 * Sets the icon and tool tip text according to the permissions of the 
	 * group.
	 */
	private void setPermissions()
	{
		IconManager im = IconManager.getInstance();
		Icon icon = im.getIcon(IconManager.PERSONAL);
        int level = 
        	TreeViewerAgent.getRegistry().getAdminService().getPermissionLevel(
        			group);
        String desc = "";
        switch (level) {
			case AdminObject.PERMISSIONS_PRIVATE:
				desc = AdminObject.PERMISSIONS_PRIVATE_TEXT;
				icon = im.getIcon(IconManager.PRIVATE_GROUP);
				break;
			case AdminObject.PERMISSIONS_GROUP_READ:
				desc = AdminObject.PERMISSIONS_GROUP_READ_TEXT;
				icon = im.getIcon(IconManager.READ_GROUP);
				break;
			case AdminObject.PERMISSIONS_GROUP_READ_LINK:
				desc = AdminObject.PERMISSIONS_GROUP_READ_LINK_TEXT;
				icon = im.getIcon(IconManager.READ_LINK_GROUP);
				break;
			case AdminObject.PERMISSIONS_PUBLIC_READ:
				desc = AdminObject.PERMISSIONS_PUBLIC_READ_TEXT;
				icon = im.getIcon(IconManager.PUBLIC_GROUP);
				break;
			case AdminObject.PERMISSIONS_PUBLIC_READ_WRITE:
				desc = AdminObject.PERMISSIONS_PUBLIC_READ_WRITE_TEXT;
				icon = im.getIcon(IconManager.PUBLIC_GROUP);
		}
        
        putValue(Action.SMALL_ICON, icon);
        putValue(Action.SHORT_DESCRIPTION, UIUtilities.formatToolTipText(desc));
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param model Reference to the Model. Mustn't be <code>null</code>.
	 * @param group The selected group.
	 */
	public MoveToAction(TreeViewer model, GroupData group)
	{
		super(model);
		if (group == null)
			throw new IllegalArgumentException("No group.");
		this.group = group;
		setEnabled(true);
		name = group.getName()+"...";
		putValue(Action.NAME, name);
		setPermissions();
	}

	/**
     * Moves the selected objects to the group.
     * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
     */
    public void actionPerformed(ActionEvent e)
    {
    	model.moveTo(group);
    }

}
