/*
 * org.openmicroscopy.shoola.agents.annotator.events.ImageAnnotated
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
package org.openmicroscopy.shoola.agents.annotator.events;

import org.openmicroscopy.ds.st.ImageAnnotation;
import org.openmicroscopy.shoola.env.event.ResponseEvent;

/**
 * An event that signals that an image has been annotated, or the contents
 * of the annotation have changed.
 * 
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
public class ImageAnnotated
	extends ResponseEvent
{
    private ImageAnnotation annotation;
    
    /**
     * Creates a new instance.
     * 
     * @param re The RequestEvent that this is a response to.
     */
    public ImageAnnotated(AnnotateImage re)
    {
        super(re);
    }
    
    /**
     * Gets the new ImageAnnotation (if any).  Saves a round trip to the DB.
     * @return The new annotation.
     */
    public ImageAnnotation getAnnotation()
    {
        return annotation;
    }
    
    /**
     * Sets the new annotation associated with the response.  Saves a round
     * trip back to the DB for verification.
     * @param annotation The new ImageAnnotation in the response.
     */
    public void setAnnotation(ImageAnnotation annotation)
    {
        this.annotation = annotation;
    }
    
}
