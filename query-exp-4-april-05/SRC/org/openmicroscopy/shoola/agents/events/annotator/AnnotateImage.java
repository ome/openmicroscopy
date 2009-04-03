/*
 * org.openmicroscopy.shoola.agents.events.annotator.AnnotateImage
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2004 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */

/*------------------------------------------------------------------------------
 *
 * Written by:    Jeff Mellen <jeffm@alum.mit.edu>
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.agents.events.annotator;

//Java imports
import java.awt.Point;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.event.RequestEvent;

/**
 * An event signaling that the user wants to annotate a particular image.
 * 
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">
 * jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
public class AnnotateImage
	extends RequestEvent
{
	
	/** Image's id. */
    private int     id;
        
    /** Image's name. */
    private String  name;
    
    private int     pixelsID;
    
    /** The location to popup the window. */
    private Point   customLocation = null;
    
    /**
     * Constructs a request to bring up the annotator with parameters from
     * the image with the specified ID.
     * 
     * @param id 		The id of the specified image.
     * @param name		The name of the specified image.
     * @param pixelsID  The id of the set of pixels to view.
     */
    public AnnotateImage(int id, String name, int pixelsID)
    {
       this.id = id;
       this.name = name;
       this.pixelsID = pixelsID;
    }
    
    /**
     * Returns the ID of the image to be annotated.
     * @return See above.
     */
    public int getID() { return id; }
    
    /**
     * Returns the name of the image to be annotated.
     * @return See above.
     */
    public String getName() { return name; }
    
    /**
     * Returns the ID of the set of pixels.
     * @return See above.
     */
    public int getPixelsID() { return pixelsID; }
    
    /**
     * Returns whether or not this event specifies an annotator location;
     * that is, the onscreen point where the annotator should be launched.
     * @return See above.
     */
    public boolean isLocationSpecified() { return (customLocation != null); }
    
    /**
     * Gets the desired popup location of the annotator.
     * @return See above.
     */
    public Point getSpecifiedLocation() { return customLocation; }
    
    /**
     * Sets the desired popup location of the annotator to the specified
     * location.
     * @param pixelLocation See above.
     */
    
    public void setSpecifiedLocation(Point pixelLocation)
    {
        customLocation = pixelLocation;
    }
    
    
}
