/*
 * org.openmicroscopy.shoola.agents.events.iviewer.ViewImage
 * 
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
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
import java.awt.Rectangle;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.event.RequestEvent;

/** 
 * Event to retrieve and view a given image.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">a.falconi@dundee.ac.uk</a>
 * @author	Donald McDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since OME2.2
 */
public class ViewImage          
    extends RequestEvent
{

    /** The ID of the pixels set. */
    private long        pixelsID;
    
    /** The ID of the image. */
    private long        imageID;
    
    /** The name of the image. */
    private String      name;

    /** The bounds of the component posting the event. */
    private Rectangle   requesterBounds;
    
    /**
     * Creates a new instance.
     * 
     * @param imageID   The image ID.
     * @param pixelsID  The pixels set ID.
     * @param name      The name of the image.
     * @param bounds    The bounds of the component posting the event.
     */
    public ViewImage(long imageID, long pixelsID, String name, Rectangle bounds)
    {
        if (pixelsID < 0) 
            throw new IllegalArgumentException("Pixels set ID not valid.");
        if (imageID < 0) 
            throw new IllegalArgumentException("Image ID not valid.");
        this.pixelsID = pixelsID;
        this.imageID = imageID;
        this.name = name;
        requesterBounds = bounds;
    }
    
    /**
     * Returns the image ID.
     * 
     * @return See above. 
     */
    public long getImageID() { return imageID; }

    /**
     * Returns the name of the image.
     * 
     * @return See above. 
     */
    public String getName() { return name; }

    /**
     * Returns the pixels set ID.
     * 
     * @return See above. 
     */
    public long getPixelsID() { return pixelsID; }
    
    /**
     * Returns the bounds of the component posting the event. 
     * Returns <code>null</code> if not available.
     * 
     * @return See above.
     */
    public Rectangle getRequesterBounds() { return requesterBounds; }
    
}
