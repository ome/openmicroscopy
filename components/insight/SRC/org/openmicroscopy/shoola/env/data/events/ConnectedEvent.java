/*
 * org.openmicroscopy.shoola.env.data.events.ConnectedEvent 
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
package org.openmicroscopy.shoola.env.data.events;


//Java imports

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.event.RequestEvent;

/** 
 * Event sent when the user is connected.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since Beta4.4
 */
public class ConnectedEvent 
	extends RequestEvent
{

	/** Flag indicating that the user is connected to the server.*/
	private boolean connected;
	
	/** Creates a new instance.*/
	public ConnectedEvent()
	{
		this(true);
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param connected Pass <code>true</code> if the user is connected,
	 * <code>false</code> otherwise.
	 */
	public ConnectedEvent(boolean connected)
	{
		this.connected = connected;
	}
	
	/**
	 * Returns <code>true</code> if the user is connected to the server,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above
	 */
	public boolean isConnected() { return connected; }
	
}
