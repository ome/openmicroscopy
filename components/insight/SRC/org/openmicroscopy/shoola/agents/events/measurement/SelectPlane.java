/*
 * org.openmicroscopy.shoola.agents.events.measurement.SelectPlane 
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
package org.openmicroscopy.shoola.agents.events.measurement;

//Java imports
import java.awt.Rectangle;
//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.event.RequestEvent;

/** 
 * Selects the plane or/and region to display.
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
public class SelectPlane 
	extends RequestEvent
{
	
	/** The ID of the pixels set. */
    private long pixelsID;
    
	/** The currently selected z-section. */
    private int defaultZ;
    
    /** The currently selected timepoint. */
    private int defaultT;
    
    /* The bounds of the ROI for big image.*/
    private Rectangle	bounds;
    
    /**
     * Creates a new instance.
     * 
     * @param pixelsID  	The pixels set ID.
     * @param defaultZ		The currently selected z-section.
     * @param defaultT		The currently selected timepoint.
     */
    public SelectPlane(long pixelsID, int defaultZ, int defaultT)
    {
        if (pixelsID < 0) 
            throw new IllegalArgumentException("Pixels set ID not valid.");
      
        this.pixelsID = pixelsID;
        this.defaultT = defaultT;
        this.defaultZ = defaultZ;
        bounds = null;
    }
    
    /** 
     * Sets the bounds of the ROI for the big image only.
     * 
     * @param bounds The bounds of the roi shape.
     */
    public void setBounds(Rectangle bounds)
    {
    	this.bounds = bounds;
    }
    
    /**
     * Returns the bounds of the ROI for the big image only.
     * 
     * @return See above
     */
    public Rectangle getBounds() { return bounds; }

    /**
     * Returns the pixels set ID.
     * 
     * @return See above. 
     */
    public long getPixelsID() { return pixelsID; }
    
    /**
     * Returns the currently selected z-section.
     * 
     * @return See above.
     */
    public int getDefaultZ() { return defaultZ; }
    
    /**
     * Returns the currently selected timepoint.
     * 
     * @return See above.
     */
    public int getDefaultT() { return defaultT; }
    
}
