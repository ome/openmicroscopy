/*
 * org.openmicroscopy.shoola.env.data.events.UserGroupSwitched 
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
package org.openmicroscopy.shoola.env.data.events;

import org.openmicroscopy.shoola.env.event.RequestEvent;

/** 
 * Event indicating if the group switch was successful or not.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since 3.0-Beta4
 */
public class UserGroupSwitched
	extends RequestEvent
{

	/** Flag indicating the switch was successful or not. */
	private boolean successful;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param successful 	Pass <code>true</code> if the switch was successful,
	 * 					<code>false</code> otherwise.
	 */
	public UserGroupSwitched(boolean successful)
	{
		this.successful = successful;
	}
	
	/**
	 * Returns <code>true</code> if the switch was successful, 
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	public boolean isSuccessful() { return successful; }

}
