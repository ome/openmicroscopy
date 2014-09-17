/*
 * org.openmicroscopy.shoola.agents.events.iviewer.CopyRndSettings 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2014 University of Dundee. All rights reserved.
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
import pojos.ImageData;

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
	
	/** The image to copy the renderig settings from. */
	private ImageData		image;
	
	/** 'Pending' rendering settings */
	private RndProxyDef rndDef;
	
	/**
	 * Creates a new instance.
	 * Used to copy saved rendering settings from an other image.
	 * 
	 * @param image The image to copy the rendering settings from.
	 */
	public CopyRndSettings(ImageData image)
	{
		this.image = image;
	}
	
	/**
         * Creates a new instance.
         * Used for copying 'pending' rendering settings, i. e.
         * which have not yet been saved with an image.
         * 
         * @param rndDef The copied rendering settings 
         */
        public CopyRndSettings(RndProxyDef rndDef)
        {
                this.rndDef = rndDef;
        }

	/**
	 * Returns the image to copy the rendering settings from.
	 * 
	 * @return See above. 
	 */
    public ImageData getImage() { return image; }

    	/**
    	 * Returns the copied rendering settings;
    	 * @return
    	 */
   	 public RndProxyDef getRndDef() {
  	      return rndDef;
  	 }
    
    
}
