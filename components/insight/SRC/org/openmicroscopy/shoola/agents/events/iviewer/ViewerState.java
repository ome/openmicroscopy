/*
 * org.openmicroscopy.shoola.agents.events.iviewer.ViewerState 
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
package org.openmicroscopy.shoola.agents.events.iviewer;


//Java imports

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.event.RequestEvent;

/** 
 * Event indicating that the state of the frame has changed.
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
public class ViewerState
	extends RequestEvent
{

	/** Indicates that the viewer is closed. */
	public static final int CLOSE = 0;
	
	/** Indicates that the viewer is iconified. */
	public static final int ICONIFIED = 1;
	
	/** Indicates that the viewer is deiconified. */
	public static final int DEICONIFIED = 2;
	
	/** The ID of the pixels set. */
    private long        pixelsID;
    
    /** One of the constants defined by this class. */
    private int			index;
    
    /**
	 * Controls if the passed index is valid.
	 * 
	 * @param i The value to control.
	 */
	private void checkIndex(int i)
	{
		switch (i) {
			case CLOSE:
			case ICONIFIED:
			case DEICONIFIED:
				return;
			default:
				throw new IllegalArgumentException("Index not supported.");
		}
	}
    /**
     * Creates a new instance.
     * 
     * @param pixelsID	The ID of the pixels set.
     * @param index		One of the constants defined by this class.
     */
    public ViewerState(long pixelsID, int index)
    {
    	checkIndex(index);
    	this.pixelsID = pixelsID;
    	this.index = index;
    }
    
    /**
     * Returns the pixels set ID.
     * 
     * @return See above. 
     */
    public long getPixelsID() { return pixelsID; }
    
    /**
	 * Returns one of the constants defined by this class.
	 * 
	 * @return See above.
	 */
	public int getIndex() { return index; }
    
}
