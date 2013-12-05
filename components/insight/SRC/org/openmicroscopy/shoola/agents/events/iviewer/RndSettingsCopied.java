/*
 * org.openmicroscopy.shoola.agents.events.iviewer.RndSettingsCopied 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2014 University of Dundee. All rights reserved.
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


//Java imports
import java.util.Collection;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.event.RequestEvent;

/** 
 * Event posted when the rendering settings have been copied.
 * The event contains a collection of pixelsID successfully copied.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since OME3.0
 */
public class RndSettingsCopied
    extends RequestEvent
{

    /** 
     * Collection of images ID for which the rendering settings where
     * successfully copied.
     */
    private Collection<Long> imagesIDs;

    /** The id of the pixels set of reference. */
    private long refPixelsID;

    /**
     * Creates a new instance.
     * 
     * @param imagesIDs The collection of images ID for which the
     *                  rendering settings where successfully copied.
     * @param refPixelsID The id of the pixels set of reference.
     */
    public RndSettingsCopied(Collection<Long> imagesIDs, long refPixelsID)
    {
        this.imagesIDs = imagesIDs;
        this.refPixelsID = refPixelsID;
    }

    /**
     * Returns the collection of pixels set ID.
     * 
     * @return See above.
     */
    public Collection<Long> getImagesIDs() { return imagesIDs; }

    /**
     * Returns the id of the pixels set of reference.
     * 
     * @return See above.
     */
    public long getRefPixelsID() { return refPixelsID; }

}
