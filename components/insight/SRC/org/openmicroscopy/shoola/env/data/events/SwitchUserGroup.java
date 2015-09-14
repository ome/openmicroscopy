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
package org.openmicroscopy.shoola.env.data.events;

import org.openmicroscopy.shoola.env.event.RequestEvent;
import omero.gateway.model.ExperimenterData;

/** 
 * Event fired to switch group and to ask agent to save data.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since 3.0-Beta4
 */
public class SwitchUserGroup 
	extends RequestEvent
{

	/** The experimenter to handle. */
	private ExperimenterData	experimenter;
	
	/** The identifier of the group. */
	private long 				groupID;
	
	/** 
	 * Creates a new instance.
	 * 
	 * @param experimenter 	The experimenter to handle.
	 * @param groupID		The identifier of the group.
	 */
	public SwitchUserGroup(ExperimenterData experimenter, long groupID)
	{
		this.experimenter = experimenter;
		this.groupID = groupID;
	}
	
	/**
	 * Returns the identifier of the group.
	 * 
	 * @return See above.
	 */
	public long getGroupID() { return groupID; }
	
	/**
	 * Returns the experimenter to handle.
	 * 
	 * @return See above.
	 */
	public ExperimenterData getExperimenterData() { return experimenter; }
	
}
