/*
 * org.openmicroscopy.shoola.agents.events.treeviewer.ChangeUserGroupEvent 
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
package org.openmicroscopy.shoola.agents.events.treeviewer;


//Java imports

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.event.RequestEvent;


/** 
 * Event indicating that the current group of the currently logged in user
 * has been modified.
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
public class ChangeUserGroupEvent
	extends RequestEvent
{

	/** The id of the new group. */
	private long groupID;
	
	/** The id of the group before switching. */
	private long oldGroupID;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param groupID 		The id of the current group.
	 * @param oldGroupID 	The id of the previous group.
	 */
	public ChangeUserGroupEvent(long groupID, long oldGroupID)
	{
		this.groupID = groupID;
		this.oldGroupID = oldGroupID;
	}
	
	/**
	 * Returns the identifier of the group.
	 * 
	 * @return See above.
	 */
	public long getGroupID() { return groupID; }

	/**
	 * Returns the identifier of the old group.
	 * 
	 * @return See above.
	 */
	public long getOldGroupID() { return oldGroupID; }
	
}
