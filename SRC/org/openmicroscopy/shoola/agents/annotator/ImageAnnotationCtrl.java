/*
 * org.openmicroscopy.shoola.agents.annotator.ImageAnnotationCtrl
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
 
package org.openmicroscopy.shoola.agents.annotator;

import java.util.List;

import org.openmicroscopy.ds.st.ImageAnnotation;
import org.openmicroscopy.shoola.agents.annotator.events.AnnotateImage;
import org.openmicroscopy.shoola.agents.annotator.events.ImageAnnotated;
import org.openmicroscopy.shoola.agents.browser.util.Filter;
import org.openmicroscopy.shoola.agents.browser.util.MapOperator;
import org.openmicroscopy.shoola.env.data.model.ImageSummary;

/**
 * Control for the image annotator.
 *
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
public class ImageAnnotationCtrl extends AnnotationCtrl
{
    private ImageSummary imageInfo;
    private AnnotateImage requestEvent;
    /**
     * Creates an image annotation controller using the specified image
     * as a basis.
     * @param imageID The ID of the image to annotate.
     */
    public ImageAnnotationCtrl(Annotator annotator, ImageSummary imageInfo,
                               AnnotateImage triggeringEvent)
    {
        if(annotator == null || imageInfo == null)
        {
            throw new IllegalArgumentException("Cannot construct an" +
                " ImageAnnotationCtrl with null parameters");
        }
        
        this.annotator = annotator;
        this.imageInfo = imageInfo;
        
        annotationList = annotator.getImageAnnotations(imageInfo.getID());
        attributeList = null; // do not use this for attributes yet (if ever)
    }
    
    /**
     * @see org.openmicroscopy.shoola.agents.annotator.AnnotationCtrl#getTargetDescription()
     */
    public String getTargetDescription()
    {
        return "Image " + imageInfo.getName();
    }
    
    /**
     * Returns all the data about the specified image.
     * @return
     */
    public ImageSummary getInfo()
    {
        return imageInfo;
    }
    
    /**
     * @see org.openmicroscopy.shoola.agents.annotator.AnnotationCtrl#getTextAnnotations()
     */
    public List getTextAnnotations()
    {
        return Filter.map(annotationList,new MapOperator()
        {
            public Object execute(Object o)
            {
                ImageAnnotation annotation = (ImageAnnotation)o;
                return annotation.getContent();
            }
        });
    }
   
    /**
     * Saves the annotation to DB and indicates such a change.
     */
    public boolean save()
    {
        // TODO save to DB
        ImageAnnotated annotated = new ImageAnnotated(requestEvent);
        
        // TODO support multiple
        if(annotationList.size() > 0)
        {
            annotated.setAnnotation((ImageAnnotation)annotationList.get(0));
        }
        annotator.respondWithEvent(annotated);
        setSaved(true);
        return true;
    }
}
