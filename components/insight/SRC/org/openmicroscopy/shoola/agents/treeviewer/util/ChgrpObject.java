/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2013 University of Dundee & Open Microscopy Environment.
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

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections.CollectionUtils;

import omero.gateway.SecurityContext;
import omero.gateway.model.DataObject;
import omero.gateway.model.GroupData;

/**
 * Helper class used to store information about the object to transfer.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since 5.0
 */
public class ChgrpObject
{

	/** The group to move the data to.*/
	private GroupData group;
	
	/** The destination object if any specified.*/
	private DataObject target;
	
	/** The data objects to move.*/
	private Map<SecurityContext, List<DataObject>> trans;

	/** The id of the data owner.*/
	private long userID;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param group The group to move the data to.
	 * @param target The destination object if any specified.
	 * @param trans The data objects to move.
	 */
	public ChgrpObject(GroupData group, DataObject target,
			Map<SecurityContext, List<DataObject>> trans)
	{
		this.group = group;
		this.target = target;
		this.trans = trans;
		userID = -1;
	}
	
	/**
	 * Sets the id of one owner the data.
	 * 
	 * @param userID The value to set.
	 */
	public void setUserID(long userID) { this.userID = userID; }
	
	/**
	 * Returns the id of one owner of the data.
	 * 
	 * @return See above.
	 */
	public long getUserID() { return userID; }

	/**
	 * Returns the group to move the data to.
	 * 
	 * @return See above.
	 */
	public GroupData getGroupData() { return group; }
	
	/**
	 * Returns the destination object if any specified.
	 * 
	 * @return See above.
	 */
	public DataObject getTarget() { return target; }
	
	/**
	 * Returns the data objects to move.
	 * 
	 * @return See above.
	 */
	public Map<SecurityContext, List<DataObject>> getTransferable()
	{
		return trans;
	}

	/** 
	 * Returns the type of data to move.
	 * 
	 * @return See above.
	 */
	public Class<?> getDataType()
	{
		if (trans == null) return null;
		Entry<SecurityContext, List<DataObject>> e;
		Iterator<Entry<SecurityContext, List<DataObject>>> i =
				trans.entrySet().iterator();
		List<DataObject> l;
		while (i.hasNext()) {
			e = i.next();
			l = e.getValue();
			if (!CollectionUtils.isEmpty(l))
				return l.get(0).getClass();
		}
		return null;
	}
}
