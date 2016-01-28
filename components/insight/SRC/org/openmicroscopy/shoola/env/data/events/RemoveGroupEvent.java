/*
 * org.openmicroscopy.shoola.env.data.events.RemoveGroupEvent
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2013 University of Dundee & Open Microscopy Environment.
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

import omero.gateway.SecurityContext;
//Application-internal dependencies
import org.openmicroscopy.shoola.env.event.RequestEvent;

/**
 * Event posted when a group is removed from the display.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since 4.4
 */
public class RemoveGroupEvent
	extends RequestEvent
{

	/** The security context to remove.*/
	private SecurityContext ctx;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param ctx The context to remove.
	 */
	public RemoveGroupEvent(SecurityContext ctx)
	{
		this.ctx = ctx;
	}

	/**
	 * Returns the security context.
	 * 
	 * @return See above.
	 */
	public SecurityContext getContext() { return ctx; }
	
}
