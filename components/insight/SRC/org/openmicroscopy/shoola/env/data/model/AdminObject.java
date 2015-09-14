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
package org.openmicroscopy.shoola.env.data.model;


import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import omero.IllegalArgumentException;

import org.openmicroscopy.shoola.env.data.login.UserCredentials;
import omero.gateway.model.ExperimenterData;
import omero.gateway.model.GroupData;

/** 
 * Holds information about the group, users to handle.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since 3.0-Beta4
 */
public class AdminObject 
{

	/** Indicates to create a group. */
	public static final int CREATE_GROUP = 0;
	
	/** Indicates to create a group. */
	public static final int CREATE_EXPERIMENTER = 1;
	
	/** Indicates to update a group. */
	public static final int UPDATE_GROUP = 2;
	
	/** Indicates to update experimenter. */
	public static final int UPDATE_EXPERIMENTER = 3;
	
	/** Indicates to reset the password. */
	public static final int RESET_PASSWORD = 4;
	
	/** Indicates to add experimenters to group. */
	public static final int ADD_EXPERIMENTER_TO_GROUP = 5;
	
	/** Indicates to reset the password. */
	public static final int ACTIVATE_USER = 6;
	
	/**
	 * Validates the index. 
	 * 
	 * @param index The value to control.
	 */
	private void checkIndex(int index)
	{
		switch (index) {
			case CREATE_EXPERIMENTER:
			case CREATE_GROUP:
			case UPDATE_GROUP:
			case UPDATE_EXPERIMENTER:
			case RESET_PASSWORD:
			case ADD_EXPERIMENTER_TO_GROUP:
			case ACTIVATE_USER:
				return;
			default:
				throw new IllegalArgumentException("Index not supported");
		}
	}
	
	/** 
	 * Can be the group to create or the group to add the experimenters to
	 * depending on the index.
	 */
	private GroupData 		group;
	
	/** The collection of groups to create. */
	private List<GroupData> groups;
	
	/** 
	 * Can be the owners of the group or the experimenters to create
	 * depending on the index.
	 */
	private Map<ExperimenterData, UserCredentials> experimenters;
	
	/** One of the constants defined by this class. */
	private int index;
	
	/** Indicates the permissions associated to the group. */
	private int permissions = -1;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param group The group to handle.
	 * @param experimenters The experimenters to handle.
	 * @param index One of the constants defined by this class.
	 */
	public AdminObject(GroupData group, Map<ExperimenterData, UserCredentials>
		experimenters, int index)
	{
		checkIndex(index);
		this.group = group;
		this.experimenters = experimenters;
		this.index = index;
		this.permissions = -1;
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param group The group to handle.
	 * @param values The experimenters to handle.
	 */
	public AdminObject(GroupData group, Collection<ExperimenterData> values)
	{
		if (values != null) {
			Iterator<ExperimenterData> i = values.iterator();
			experimenters = new HashMap<ExperimenterData, UserCredentials>();
			while (i.hasNext()) {
				experimenters.put(i.next(), null);
			}
		}
		this.group = group;
		this.index = ADD_EXPERIMENTER_TO_GROUP;
		this.permissions = -1;
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param experimenters The experimenters to handle.
	 * @param index One of the constants defined by this class.
	 */
	public AdminObject(Map<ExperimenterData, UserCredentials> experimenters, 
			int index)
	{
		this(null, experimenters, index);
	}

	/**
	 * Sets the permissions associated to the group.
	 * 
	 * @param permissions 	The value to set. One of the constants defined 
	 * 						by this class.
	 */
	public void setPermissions(int permissions)
	{
		switch (permissions) {
			case GroupData.PERMISSIONS_PRIVATE:
			case GroupData.PERMISSIONS_GROUP_READ:
			case GroupData.PERMISSIONS_GROUP_READ_LINK:
			case GroupData.PERMISSIONS_GROUP_READ_WRITE:
			case GroupData.PERMISSIONS_PUBLIC_READ:
			case GroupData.PERMISSIONS_PUBLIC_READ_WRITE:
				this.permissions = permissions;
				break;
			default:
				this.permissions = GroupData.PERMISSIONS_PRIVATE;
		}
	}
	
	/**
	 * Returns the permissions associated to the group.
	 * 
	 * @return See above.
	 */
	public int getPermissions() { return permissions; }
	
	/**
	 * Returns the experimenters to create.
	 * 
	 * @return See above
	 */
	public Map<ExperimenterData, UserCredentials> getExperimenters()
	{
		return experimenters;
	}
	
	/**
	 * Returns the group to create or to add the experimenters to.
	 * 
	 * @return See above.
	 */
	public GroupData getGroup() { return group; }
	
	/**
	 * Sets the groups.
	 * 
	 * @param groups The value to handle.
	 */
	public void setGroups(List<GroupData> groups) { this.groups = groups; }
	
	/**
	 * Returns the groups to add the new users to.
	 * 
	 * @return See above.
	 */
	public List<GroupData> getGroups() { return groups; }
	
	/**
	 * Returns one of the constants defined by this class.
	 * 
	 * @return See above.
	 */
	public int getIndex() { return index; }

}
