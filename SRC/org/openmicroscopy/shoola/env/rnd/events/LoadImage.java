/*
 * org.openmicroscopy.shoola.env.rnd.events.LoadImage
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

package org.openmicroscopy.shoola.env.rnd.events;

//Java imports

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.event.RequestEvent;

/** 
 * Encapsulates a request to load a given image into an image viewer agent.
 * Currently, the Browser and the Data Manager agents post this event to
 * tell the Viewer agent to start the loading process of an image and then
 * display it on screen &#151; after it has been rendered.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class LoadImage
	extends RequestEvent
{	
	
	/** The ID of the image to load. */
	private int		imageID;
	
	/** The ID of the set of pixels. */
	
	private int		pixelsID;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param imageID	The ID of the image to load.
	 * @param pixelsID	The ID of the set of pixels.
	 */
	public LoadImage(int imageID, int pixelsID)
	{
		this.imageID = imageID;
		this.pixelsID = pixelsID;
	}
	
	/**
	 * Returns the ID of the image to load.
	 * 
	 * @return	See above.
	 */
	public int getImageID()
	{
		return imageID;
	}

	/**
	 * Return the ID of the set of pixels.
	 * 
	 * @return	See above.
	 */
	public int getPixelsID()
	{
		return pixelsID;
	}

}

