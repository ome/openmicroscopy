/*
 * org.openmicroscopy.shoola.agents.annotator.DatasetAnnotationCtrl
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

import org.openmicroscopy.ds.st.DatasetAnnotation;
import org.openmicroscopy.shoola.agents.annotator.events.AnnotateDataset;
import org.openmicroscopy.shoola.agents.browser.util.Filter;
import org.openmicroscopy.shoola.agents.browser.util.MapOperator;

/**
 * Controls the dataset annotation/attribute editor GUI.
 *
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
public class DatasetAnnotationCtrl extends AnnotationCtrl
{
    private int datasetID;
    private String datasetName;
    
    /**
     * Creates an image annotation controller using the specified image
     * as a basis.
     * @param imageID The ID of the image to annotate.
     */
    public DatasetAnnotationCtrl(Annotator annotator,
                                 AnnotateDataset triggeringEvent)
    {
        if(annotator == null || triggeringEvent == null)
        {
            throw new IllegalArgumentException("Cannot construct an" +
                " ImageAnnotationCtrl with a null Annotator");
        }
        
        this.annotator = annotator;
        this.datasetID = triggeringEvent.getID();
        this.datasetName = triggeringEvent.getName();
        
        annotationList = annotator.getDatasetAnnotations(datasetID);
        attributeList = null; // ignore for now
    }
    
    /**
     * @see org.openmicroscopy.shoola.agents.annotator.AnnotationCtrl#getTargetDescription()
     */
    public String getTargetDescription()
    {
        return "Dataset " + datasetName;
    }
    
    public int getID()
    {
        return datasetID;
    }
    
    /**
     * @see org.openmicroscopy.shoola.agents.annotator.AnnotationCtrl#getTextAnnotations()
     */
    public List getTextAnnotations()
    {
        return Filter.map(annotationList, new MapOperator()
        {
            public Object execute(Object o)
            {
                DatasetAnnotation annotation = (DatasetAnnotation)o;
                return annotation.getContent();
            }
        });
    }
    
    /**
     * @see org.openmicroscopy.shoola.agents.annotator.AnnotationCtrl#getAnnotation(int)
     */
    public String getAnnotation(int index)
    {
        return (String)getTextAnnotations().get(index);
    }
    
    /**
     * @see org.openmicroscopy.shoola.agents.annotator.AnnotationCtrl#newAnnotation(java.lang.String)
     */
    public void newAnnotation(String annotation)
    {
        DatasetAnnotation da = annotator.createDatasetAnnotation(annotation);
        annotationList.add(da);
    }
    
    /**
     * @see org.openmicroscopy.shoola.agents.annotator.AnnotationCtrl#setAnnotation(int, java.lang.String)
     */
    public void setAnnotation(int annotationIndex, String annotation)
    {
        DatasetAnnotation da =
            (DatasetAnnotation)annotationList.get(annotationIndex);
        da.setContent(annotation);
    }

    public boolean save()
    {
        // TODO implement
        return true;
    }
	
}
