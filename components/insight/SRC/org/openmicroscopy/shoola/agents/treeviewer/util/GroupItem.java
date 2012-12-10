/*
 * org.openmicroscopy.shoola.agents.treeviewer.util.GroupItem
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
package org.openmicroscopy.shoola.agents.treeviewer.util;



//Java imports
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JMenu;

//Third-party libraries

//Application-internal dependencies
import pojos.ExperimenterData;
import pojos.GroupData;

/**
 * Hosts the group and its associated menu.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since 4.4
 */
public class GroupItem 
	extends JMenu
{
	
	/** The group hosted by this component.*/
	private GroupData group;

	/** The List of components hosting the user.*/
	private List<UserMenuItem> usersItem;
	
	/** The menu displaying the users.*/
	private JComponent usersMenu;

	/** The box indicating if the group is selected or not.*/
	private JCheckBox groupBox;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param group The group hosted by this component.
	 * @param icon The icon associated to the group permissions.
	 */
	public GroupItem(GroupData group, Icon icon)
	{
		if (group == null) 
			throw new IllegalArgumentException("No group");
		this.group = group;
		setText(group.getName());
		setIcon(icon);
	}

	/**
	 * Sets the component indicating if the group is displayed or not.
	 * 
	 * @param groupBox The value to set.
	 */
	public void setGroupBox(JCheckBox groupBox)
	{
		this.groupBox = groupBox;
	}
	
	/**
	 * Selects or not the group.
	 * 
	 * @param selected Pass <code>true</code> to select the group,
	 * <code>false</code> otherwise.
	 */
	public void setGroupSelection(boolean selected)
	{
		groupBox.setSelected(selected);
	}
	
	/**
	 * Returns <code>true</code> if the group is selected or not.
	 * 
	 * @return See above.
	 */
	public boolean isGroupSelected() { return groupBox.isSelected(); }
	
	
	/**
	 * Sets the list of components hosting the users.
	 * 
	 * @param usersItem The value to set.
	 */
	public void setUsersItem(List<UserMenuItem> usersItem)
	{
		this.usersItem = usersItem;
	}
	
	/**
	 * Sets the component displaying the users.
	 * 
	 * @param usersMenu The value to set.
	 */
	public void setUsersMenu(JComponent usersMenu)
	{
		this.usersMenu = usersMenu;
	}
	
	/**
	 * Returns the component displaying the users.
	 * 
	 * @return See above.
	 */
	public JComponent getUsersMenu() { return usersMenu; }
	
	/**
	 * Returns the group.
	 * 
	 * @return See above.
	 */
	public GroupData getGroup() { return group; }
	
	/**
	 * Returns the selected users.
	 * 
	 * @return See above.
	 */
	public List<ExperimenterData> getSeletectedUsers()
	{
		List<ExperimenterData> users = new ArrayList<ExperimenterData>();
		Iterator<UserMenuItem> i = usersItem.iterator();
		UserMenuItem item;
		while (i.hasNext()) {
			item = i.next();
			if (item.isSelected()) users.add(item.getExperimenter());
		}
		return users;
	}
	
}
