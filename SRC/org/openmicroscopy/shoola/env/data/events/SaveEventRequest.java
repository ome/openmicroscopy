/*
 * org.openmicroscopy.shoola.env.data.events.SaveEventRequest 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
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


//Java imports

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.event.RequestEvent;

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class SaveEventRequest 
	extends RequestEvent
{

	/** The caller. */
	private Object 			origin;
	
	/** The answer to handle. */
	private RequestEvent	answer;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param origin	The origin of the event. Mustn't be <code>null</code>.
	 * @param answer	The answer to this event. Mustn't be <code>null</code>.
	 */
	public SaveEventRequest(Object origin, RequestEvent answer)
	{
		if (origin == null)
			throw new IllegalArgumentException("Origin cannot be null.");
		if (answer == null)
			throw new IllegalArgumentException("The answer cannot be null.");
		this.origin = origin;
		this.answer = answer;
	}
	
	/**
	 * Returns the origin.
	 * 
	 * @return See above.
	 */
	public Object getOrigin() { return origin; }
	
	/**
	 * Returns the answer.
	 * 
	 * @return See above.
	 */
	public RequestEvent getAnswer() { return answer; }
	
	/**
	 * Overridden to return the message if any.
	 * @see java.lang.Object#toString()
	 */
	public String toString() { return answer.toString(); }
	
}
