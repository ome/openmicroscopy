/*
 * org.openmicroscopy.shoola.agents.events.FocusGainedEvent 
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
package org.openmicroscopy.shoola.agents.events;


//Java imports

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.event.RequestEvent;

/** 
 * Event posted when a window gained focus.
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
public class FocusGainedEvent
	extends RequestEvent
{

	/** Indicate that the viewer gained focus. */
	public static final int VIEWER_FOCUS = 0;
	
	/** Indicate that the measurement tool gained focus. */
	public static final int MEASUREMENT_TOOL_FOCUS = 1;
	
	/** The id of the pixels set the windows are for. */
	private long	pixelsID;
	
	/** One of the constants defined by this class. */
	private int		index;
	
	/**
	 * Controls if the index is supported.
	 * 
	 * @param i The index to handle.
	 */
	private void checkIndex(int i) 
	{
		switch (i) {
			case VIEWER_FOCUS:
			case MEASUREMENT_TOOL_FOCUS:
				return;
			default:
				throw new IllegalArgumentException("Index not supported.");
		}
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param pixelsID The Id of the pixels set.
	 * @param index		The index. 
	 */
	public FocusGainedEvent(long pixelsID, int index)
	{
		if (pixelsID < 0)
			throw new IllegalArgumentException("Pixels ID not valid.");
		checkIndex(index);
		this.pixelsID = pixelsID;
		this.index = index;
	}
	
	/**
	 * Returns the id of the pixels set.
	 * 
	 * @return See above.
	 */
	public long getPixelsID() { return pixelsID; }
	
	/**
	 * Returns the index of the event.
	 * 
	 * @return See above.
	 */
	public int getIndex() { return index; }
	
}
