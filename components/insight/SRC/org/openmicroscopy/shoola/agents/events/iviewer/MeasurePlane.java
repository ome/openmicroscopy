/*
 * org.openmicroscopy.shoola.agents.events.iviewer.MeasurePlane 
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
 * Event indicating that a new plane has been selected or zoomed.
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
public class MeasurePlane 
	extends RequestEvent
{

	 /** The ID of the pixels set. */
    private long        pixelsID;
    
    /** The currently selected z-section. */
    private int			defaultZ;
    
    /** The currently selected timepoint. */
    private int			defaultT;
    
    /** The magnification factor of the currently viewed plane. */
    private double		magnification;
    
    /** The size along the X-axis.*/
    private int			sizeX;
    
    /** The size along the Y-axis.*/
    private int			sizeY;
    
    /**
     * Creates a new instance.
     * 
     * @param pixelsID  	The pixels set ID.
     * @param defaultZ		The currently selected z-section.
     * @param defaultT		The currently selected timepoint.
     * @param magnification	The magnification factor used to display the plane.
     */
    public MeasurePlane(long pixelsID, int defaultZ, int defaultT,
    				double magnification)
    {
    	if (pixelsID < 0) 
            throw new IllegalArgumentException("Pixels set ID not valid.");
    	 this.defaultT = defaultT;
         this.defaultZ = defaultZ;
         this.magnification = magnification;
         this.pixelsID = pixelsID;
         sizeX = 0;
         sizeY = 0;
    }
    
    /**
     * Sets the size along the X-axis and Y-axis.
     * 
     * @param sizeX The size along the X-axis.
     * @param sizeY The size along the Y-axis.
     */
    public void setSize(int sizeX, int sizeY)
    {
    	this.sizeX = sizeX;
    	this.sizeY = sizeY;
    }
    
    /**
     * Returns the size along the X-axis.
     * 
     * @return See above.
     */
    public int getSizeX() { return sizeX; }
    
    /**
     * Returns the size along the Y-axis.
     * 
     * @return See above.
     */
    public int getSizeY() { return sizeY; }
    
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
    
    /**
     * Returns the magnification factor.
     * 
     * @return See above.
     */
    public double getMagnification() { return magnification; }
    
}
