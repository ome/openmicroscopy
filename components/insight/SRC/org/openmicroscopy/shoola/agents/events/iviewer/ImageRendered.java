/*
 * org.openmicroscopy.shoola.agents.events.iviewer.ImageRendered 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
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
import java.awt.image.BufferedImage;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.event.RequestEvent;

/** 
 * Event indicating that a new image has been rendered. A thumbnail of the
 * rendered image is posted.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta3
 */
public class ImageRendered
	extends RequestEvent
{

	/** The ID of the pixels set. */
    private long        	pixelsID;
    
    /** Thumbnail of the rendered image. */
    private BufferedImage	thumbnail;
    
    /** The rendered image. */
    private Object	renderedImage;
 
    /**
     * Creates a new instance.
     * 
     * @param pixelsID  	The id of the pixels set.
     * @param thumbnail 	Thumbnail of the rendered image.
     * @param renderedImage	The rendered image.
     */
    public ImageRendered(long pixelsID, BufferedImage thumbnail, 
    				Object renderedImage)
    {
    	if (pixelsID < 0) 
            throw new IllegalArgumentException("Pixels set ID not valid.");
    	this.pixelsID = pixelsID;
    	this.thumbnail = thumbnail;
    	this.renderedImage = renderedImage;
    }
    
    /**
     * Returns the pixels set ID.
     * 
     * @return See above. 
     */
    public long getPixelsID() { return pixelsID; }
    
    /**
     * Returns the thumbnail.
     * 
     * @return See above. 
     */
    public BufferedImage getThumbnail() { return thumbnail; }
    
    /**
     * Returns the rendered image.
     * 
     * @return See above. 
     */
    public Object getRenderedImage() { return renderedImage; }
    
}
