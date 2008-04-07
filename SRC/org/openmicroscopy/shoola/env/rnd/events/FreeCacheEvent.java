/*
 * org.openmicroscopy.shoola.env.rnd.events.FreeCacheEvent 
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
package org.openmicroscopy.shoola.env.rnd.events;


//Java imports

//Third-party libraries

//Application-internal dependencies
import java.util.List;

import org.openmicroscopy.shoola.env.event.RequestEvent;

/** 
 * Event posted when a component using caches is closed.
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
public class FreeCacheEvent
	extends RequestEvent
{

	/** Inidcates to free the cache for the raw data. */
	public static final int	RAW_DATA = 0;
	
	/** Inidcates to free the cache for the xy image. */
	public static final int	XY_IMAGE_DATA = 1;
	
	/** Inidcates to free the cache for the xy image. */
	public static final int	ALL_DATA = 2;
	
	/** The id of the pixels set. */
	private List<Long> pixelsID;
	
	/** One of the constants defined by this class. */
	private int  index;
	
	/** 
	 * Checks if the passed index is supported.
	 * 
	 * @param i The value to check.
	 */
	private void checkIndex(int i)
	{
		switch (i) {
			case RAW_DATA:
			case XY_IMAGE_DATA:
			case ALL_DATA:
				return;
			default:
				throw new IllegalArgumentException("Index not supported.");
		}
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param pixelsID The collection of the pixels set Id.
	 * @param index    One of the constants defined by this class.
	 */
	public FreeCacheEvent(List<Long> pixelsID, int index)
	{
		if (pixelsID == null || pixelsID.size() == 0) 
			throw new IllegalArgumentException("Pixels ID not valid.");
		checkIndex(index);
		this.index = index;
		this.pixelsID = pixelsID;
	}
	
	/**
	 * Returns the index.
	 * 
	 * @return See above.
	 */
	public int getIndex() { return index; }
	
	/**
	 * Returns the collection of the pixels set.
	 *  
	 * @return See above.
	 */
	public List<Long> getPixelsID() { return pixelsID; }
	
}
