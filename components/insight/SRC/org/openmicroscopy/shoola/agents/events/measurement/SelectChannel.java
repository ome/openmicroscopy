/*
 * org.openmicroscopy.shoola.agents.events.measurement.SelectChannel 
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
package org.openmicroscopy.shoola.agents.events.measurement;


import java.util.Arrays;
import java.util.List;

import org.openmicroscopy.shoola.env.event.RequestEvent;

/** 
 * Turns the specified channel on.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since Beta4.4
 */
public class SelectChannel
extends RequestEvent
{

    /** The ID of the pixels set. */
    private long pixelsID;

    /** The channel to turn on if the value is set.*/
    private List<Integer> channels;

    /**
     * Creates a new instance.
     *
     * @param pixelsID The pixels set ID.
     * @param channels The channels to turn on.
     */
    public SelectChannel(long pixelsID, List<Integer> channels)
    {
        if (pixelsID < 0) 
            throw new IllegalArgumentException("Pixels set ID not valid.");

        this.pixelsID = pixelsID;
        this.channels = channels;
    }

    /**
     * Creates a new instance.
     *
     * @param pixelsID The pixels set ID.
     * @param channel The channel to turn on.
     */
    public SelectChannel(long pixelsID, int channel)
    {
        if (pixelsID < 0) 
            throw new IllegalArgumentException("Pixels set ID not valid.");

        this.pixelsID = pixelsID;
        this.channels = Arrays.asList(channel);
    }

    /**
     * Returns the channels.
     *
     * @return See above.
     */
    public List<Integer> getChannels() { return channels; }

    /**
     * Returns the pixels set ID.
     * 
     * @return See above.
     */
    public long getPixelsID() { return pixelsID; }

}
