/*
 * org.openmicroscopy.shoola.agents.editor.actions.GroupSelectionAction 
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
package org.openmicroscopy.shoola.agents.editor.actions;


//Java imports
import java.awt.event.ActionEvent;
import javax.swing.Action;

//Third-party libraries


//Application-internal dependencies
import org.openmicroscopy.shoola.agents.editor.IconManager;
import org.openmicroscopy.shoola.agents.editor.view.Editor;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import pojos.GroupData;

/** 
 * Selects the group out of the list of groups the user is member of
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since Beta4.4
 */
public class GroupSelectionAction 
	extends EditorAction
{

	/** The group the logged in user is member of. */
	private GroupData group;
	
	/** 
	 * Sets the icon and tool tip text according to the permissions of the 
	 * group.
	 */
	private void setPermissions()
	{
		int iconID = IconManager.PRIVATE_GROUP_DD_12;
		String desc = "";
		switch (group.getPermissions().getPermissionsLevel()) {
			case GroupData.PERMISSIONS_PRIVATE:
				desc = GroupData.PERMISSIONS_PRIVATE_TEXT;
				iconID = IconManager.PRIVATE_GROUP_DD_12;
				break;
			case GroupData.PERMISSIONS_GROUP_READ:
				desc = GroupData.PERMISSIONS_GROUP_READ_TEXT;
				iconID = IconManager.READ_GROUP_DD_12;
				break;
			case GroupData.PERMISSIONS_GROUP_READ_LINK:
				desc = GroupData.PERMISSIONS_GROUP_READ_LINK_TEXT;
				iconID = IconManager.READ_LINK_GROUP_DD_12;
				break;
			case GroupData.PERMISSIONS_GROUP_READ_WRITE:
				desc = GroupData.PERMISSIONS_GROUP_READ_WRITE_TEXT;
				iconID = IconManager.READ_WRITE_GROUP_DD_12;
				break;
			case GroupData.PERMISSIONS_PUBLIC_READ:
				desc = GroupData.PERMISSIONS_PUBLIC_READ_TEXT;
				iconID = IconManager.PUBLIC_GROUP_DD_12;
				break;
			case GroupData.PERMISSIONS_PUBLIC_READ_WRITE:
				desc = GroupData.PERMISSIONS_PUBLIC_READ_WRITE_TEXT;
				iconID = IconManager.PUBLIC_GROUP_DD_12;
		}


		setIcon(iconID);
		putValue(Action.SHORT_DESCRIPTION, UIUtilities.formatToolTipText(desc));
	}

	/**
	 * Creates a new instance.
	 * 
	 * @param model Reference to the Model. Mustn't be <code>null</code>.
	 * @param group The group the logged in user is a member of.
	 */
	public GroupSelectionAction(Editor model, GroupData group)
	{
		super(model);
		if (group == null)
			throw new IllegalArgumentException("No group specified.");
		this.group = group;
		putValue(Action.NAME, group.getName());
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
	 * Sets the default group for the currently logged in user.
	 * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
	 */
    public void actionPerformed(ActionEvent e)
    {
    	model.setUserGroup(group.getId());
    }
	
}
