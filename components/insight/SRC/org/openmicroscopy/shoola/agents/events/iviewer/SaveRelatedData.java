/*
 * org.openmicroscopy.shoola.agents.events.iviewer.SaveRelatedData 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2015 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
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

import org.openmicroscopy.shoola.util.CommonsLangUtils;
import org.openmicroscopy.shoola.agents.events.SaveData;
import org.openmicroscopy.shoola.env.event.RequestEvent;

/**
 * Event posted by agents related to the viewer if they have data
 * to save before closing the viewer.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since OME3.0
 */
public class SaveRelatedData
    extends RequestEvent
{

    /** The ID of the pixels set. */
    private long pixelsID;

    /** The message to display. */
    private String message;

    /** The event to post. */
    private SaveData saveEvent;

    /** 
     * Flag set to <code>true</code> to save the data the event,
     * set to <code>false</code> to ignore.
     */
    private boolean toSave;

    /**
     * Creates a new instance.
     * 
     * @param pixelsID The pixels set ID.
     * @param saveEvent The event to post if this event is taken into account.
     *                  Mustn't be <code>null</code>.
     * @param message The message to display.
     * @param toSave  Pass <code>true</code> to save the data the event,
     *                set to <code>false</code> to ignore.
     */
    public SaveRelatedData(long pixelsID, SaveData saveEvent,
            String message, boolean toSave)
    {
        if (pixelsID < 0) 
            throw new IllegalArgumentException("Pixels set ID not valid.");
        if (saveEvent == null)
            throw new IllegalArgumentException("Event to post cannot be " +
                    "null.");
        if (CommonsLangUtils.isBlank(message))
            throw new IllegalArgumentException("Please enter a valid message.");
        this.pixelsID = pixelsID;
        this.message = message;
        this.saveEvent = saveEvent;
        this.toSave = toSave;
    }

    /**
     * Returns <code>true</code> to add the event to the list,
     * <code>false</code> to remove the event from the list.
     * 
     * @return See above.
     */
    public boolean isToSave() { return toSave; }

    /**
     * Returns the Id of the pixels set.
     * 
     * @return See above.
     */
    public long getPixelsID() { return pixelsID; }

    /**
     * Returns the event to post if selected.
     * 
     * @return See above.
     */
    public SaveData getSaveEvent() { return saveEvent; }

    /**
     * Overridden to return the message associated to this event.
     * @see java.lang.Object#toString()
     */
    public String toString() { return message; }

}
