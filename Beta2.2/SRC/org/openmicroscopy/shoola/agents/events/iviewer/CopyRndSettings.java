/*
 * org.openmicroscopy.shoola.agents.events.iviewer.CopyRndSettings 
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
import org.openmicroscopy.shoola.env.rnd.RndProxyDef;

/** 
 * Event to copy rendering settings across a collection of pixels set.
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
public class CopyRndSettings
	extends RequestEvent
{
	
	/** The id of the pixels set to copy. */
	private long 			pixelsID;
	
	/** The rendering settings to copy. */
	private RndProxyDef		rndSettings;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param pixelsID 	The id of the pixels set.
	 */
	public CopyRndSettings(long pixelsID)
	{
		this.pixelsID = pixelsID;
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param pixelsID 		The id of the pixels set.
	 * @param rndSettings	The rendering settings to copy.
	 */
	public CopyRndSettings(long pixelsID, RndProxyDef rndSettings)
	{
		this.pixelsID = pixelsID;
		if (rndSettings == null)
			throw new IllegalArgumentException("No rendering settings " +
					"to copy.");
		this.rndSettings = rndSettings;
	}
	
	 /**
     * Returns the pixels set ID.
     * 
     * @return See above. 
     */
    public long getPixelsID() { return pixelsID; }
    
    /**
     * Returns the rendering settings.
     * 
     * @return See above.
     */
    public RndProxyDef getRndSettings() { return rndSettings; }
    
}
