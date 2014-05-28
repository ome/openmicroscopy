/*
 * org.openmicroscopy.shoola.util.ui.search.GroupContext 
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
package org.openmicroscopy.shoola.util.ui.search;


//Java imports

//Third-party libraries

//Application-internal dependencies
import pojos.GroupData;

/** 
 * Host information about the group to search into.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since Beta4.4
 */
class GroupContext
{
        /** ID indicating all groups should be included in the search */
        public static final int ALL_GROUPS_ID = Integer.MAX_VALUE;
        
	/** The group to handle.*/
	private String group;
	
	/** The identifier of the group.*/
	private long id;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param group The name of the group to handle.
	 * @param id The identifier of the group.
	 */
	GroupContext(String group, long id)
	{
		this.group = group;
		this.id = id;
	}
	
	/**
	 * Returns the id of the group hosted by the component.
	 * 
	 * @return See above.
	 */
	long getId() { return id; }
	
	/**
	 * Overridden to return the name of the group.
	 * @see Object#toString()
	 */
	public String toString() { return group; }

}
