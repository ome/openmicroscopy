/*
 * org.openmicroscopy.shoola.agents.events.measurement.SaveData 
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
 * Event posted back to an agent to perform is saving action.
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
public class SaveData 
	extends RequestEvent
{

	/** Indicates that this event is for the measurement tool. */
	public static final int	MEASUREMENT_TYPE = 0;
	
	/** Indicates that this event is for the data manager. */
	public static final int	DATA_MANAGER_EDIT = 1;
	
	/** Indicates that this event is for the data manager. */
	public static final int	DATA_MANAGER_ANNOTATION = 2;
	
	/** Indicates that this event is for the viewer. */
	public static final int	VIEWER_RND_SETTINGS = 3;
	
	/** Indicates that this event is for the viewer. */
	public static final int	VIEWER_ANNOTATION = 4;
	
	/** Indicates that this event is for the viewer. */
	public static final int	VIEWER_ROI = 5;
	
	/** Indicates that this event is for the hiviewer. */
	public static final int	HIVIEWER_EDIT = 6;
	
	/** Indicates that this event is for the hiviewer. */
	public static final int	HIVIEWER_ANNOTATION = 7;
	
	/** The object this event is for. */
	private Object	origin;
	
	/** The Id of the pixels set, this event is for. */
	private long 	pixelsID;
	
	/** One of the constants defined by this class. */
	private int 	type;
	
	/** The message associated to this event. */
	private String	message;
	
	/**
	 * Controls if the specified type is supported.
	 * 
	 * @param t The valye to check.
	 */
	private void checkType(int t)
	{
		switch (t) {
			case MEASUREMENT_TYPE:
			case DATA_MANAGER_EDIT:
			case DATA_MANAGER_ANNOTATION:
			case VIEWER_RND_SETTINGS:
			case VIEWER_ANNOTATION:
			case VIEWER_ROI:
			case HIVIEWER_ANNOTATION:
			case HIVIEWER_EDIT:
				return;
			default:
				throw new IllegalArgumentException("Type not supported.");
		}
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param pixelsID	The pixels set ID.
	 * @param type 		One of the constants defined by this class.
	 */
	public SaveData(long pixelsID, int type)
	{
		 if (pixelsID < 0) 
             throw new IllegalArgumentException("Pixels set ID not valid.");
		 checkType(type);
		 this.pixelsID = pixelsID;
		 this.type = type;
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param type	One of the constants defined by this class.
	 */
	public SaveData(int type)
	{
		checkType(type);
		this.type = type;
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param origin	The object this event is related to.
	 * @param type	One of the constants defined by this class.
	 */
	public SaveData(Object origin, int type)
	{
		 if (origin == null)
			 throw new IllegalArgumentException("No origin specified.");
		 checkType(type);
		 this.type = type;
		 this.origin = origin;
	}
	
	/**
	 * Sets the message associated to this event.
	 * 
	 * @param v The value to set.
	 */
	public void setMessage(String v) { message = v; }
	
	/**
     * Returns the Id of the pixels set.
     * 
     * @return See above.
     */
	public long getPixelsID() { return pixelsID; }
	
	/**
	 * Returns the type.
	 * 
	 * @return See above.
	 */
	public int getType() { return type; }
	
	/**
	 * Returns the object this event is for.
	 * 
	 * @return See above.
	 */
	public Object getOrigin() { return origin; }
	
	/**
	 * Overridden to return the message associated to this event.
	 * @see java.lang.Object#toString()
	 */
	public String toString() { return message; }
	
}
