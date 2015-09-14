/*
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
package org.openmicroscopy.shoola.agents.events.treeviewer;

import java.util.List;

import org.openmicroscopy.shoola.env.event.RequestEvent;
import omero.gateway.model.DataObject;
import omero.gateway.model.GroupData;

/** 
 * Posts an event indicating to move the passed objects to the selectd group.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since Beta4.4
 */
public class MoveToEvent
	extends RequestEvent
{
	
	/** The group to move the data to.*/
	private GroupData group;
	
	/** The objects to move.*/
	private List<DataObject> objects;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param group The group to move the data to.
	 * @param objects The objects to move.
	 */
	public MoveToEvent(GroupData group, List<DataObject> objects)
	{
		this.objects = objects;
		this.group = group;
	}
	
	/**
	 * Returns the group to move the data to.
	 * 
	 * @return See above.
	 */
	public GroupData getGroup() { return group; }
	
	/**
	 * Returns the objects to move.
	 * 
	 * @return See above.
	 */
	public List<DataObject> getObjects() { return objects; }
	
}
