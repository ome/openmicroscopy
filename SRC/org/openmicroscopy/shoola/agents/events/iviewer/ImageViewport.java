/*
 * org.openmicroscopy.shoola.agents.events.iviewer.ImageViewport
 *
 *------------------------------------------------------------------------------
 * Copyright (C) 2006-2009 University of Dundee. All rights reserved.
 *
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
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
 * Event to display the rectangle on the viewport.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 *         <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 *         <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class ImageViewport
	extends RequestEvent
{

	/** The id of the image. */
	private long imageID;
	
	/** The id of the pixels set. */
	private long pixelsID;
	
	/** The bounds of the object to display. */
	private Rectangle bounds;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param imageID 	The id of the image.
	 * @param pixelsID  The id of the pixels set.
	 * @param bounds	The bounds to display.
	 */
	public ImageViewport(long imageID, long pixelsID, Rectangle bounds)
	{
		this.imageID = imageID;
		this.pixelsID = pixelsID;
		this.bounds = bounds;
	}
	
	/** 
	 * Returns the bounds to display.
	 * 
	 * @return See above.
	 */
	public Rectangle getBounds() { return bounds; }
	
	/**
	 * Returns the id of the image. 
	 * 
	 * @return See above.
	 */
	public long getImageID() { return imageID; }
	
	/**
	 * Returns the id of the pixels set. 
	 * 
	 * @return See above.
	 */
	public long getPixelsID() { return pixelsID; }
	
}
