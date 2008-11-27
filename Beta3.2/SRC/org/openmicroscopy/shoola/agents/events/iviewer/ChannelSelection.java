/*
 * org.openmicroscopy.shoola.agents.events.iviewer.ChannelSelection 
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
import java.util.Map;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.event.RequestEvent;

/** 
 * Event posted when a new channel is selected or when an active channel 
 * is mapped to a new color.
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
public class ChannelSelection
	extends RequestEvent
{

	/** Indicates that a new color has been selected for an active channel. */
	public static final int	COLOR_SELECTION = 0;
	
	/** Indicates that a new channel is selected. */
	public static final int	CHANNEL_SELECTION = 1;
	
	/** The ID of the pixels set. */
    private long        pixelsID;
    
    /** Collection of pairs (channel's index, channel's color). */
    private Map			channels;
 
    /** One of the constants defined by this class. */
    private int			index;
    
    /**
     * Checks if the passed index is supported.
     * 
     * @param i The value to control.
     */
    private void checkIndex(int i)
    {
    	switch (i) {
			case COLOR_SELECTION:
			case CHANNEL_SELECTION:
				break;
			default:
				throw new IllegalArgumentException("Index not supported.");
		}
    }
    
    /**
     * Creates a new instance.
     * 
     * @param pixelsID	The pixels set ID.
     * @param channels	Collection of pairs (channel's index, channel's color).
     * @param index		One of the constants defined by this class.
     */
    public ChannelSelection(long pixelsID, Map channels, int index)
    {
    	if (pixelsID < 0) 
            throw new IllegalArgumentException("Pixels set ID not valid.");
    	if (channels == null)
    		throw new IllegalArgumentException("Channels cannot be null.");
    	checkIndex(index);
    	this.channels = channels;
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
     * Returns the collection of pairs (channel's index, channel's color).
     * 
     * @return See above.
     */
    public Map getChannels() { return channels; }
    
    /**
     * Returns the index associated to this event. One of the constants defined
     * by this class.
     * 
     * @return See above.
     */
    public int getIndex() { return index; }

}
