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
import org.openmicroscopy.shoola.agents.browser.util.Filter;
import org.openmicroscopy.shoola.agents.browser.util.MapOperator;
import org.openmicroscopy.shoola.env.data.model.DatasetSummary;

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
    private DatasetSummary datasetInfo;
    
    /**
     * Creates an image annotation controller using the specified image
     * as a basis.
     * @param imageID The ID of the image to annotate.
     */
    public DatasetAnnotationCtrl(Annotator annotator, DatasetSummary datasetInfo)
    {
        if(annotator == null || datasetInfo == null)
        {
            throw new IllegalArgumentException("Cannot construct an" +
                " ImageAnnotationCtrl with a null Annotator");
        }
        
        this.annotator = annotator;
        this.datasetInfo = datasetInfo;
        
        annotationList = annotator.getDatasetAnnotations(datasetInfo.getID());
        attributeList = null; // ignore for now
    }
    
    /**
     * @see org.openmicroscopy.shoola.agents.annotator.AnnotationCtrl#getTargetDescription()
     */
    public String getTargetDescription()
    {
        return "Dataset " + datasetInfo.getName();
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

    public boolean save()
    {
        // TODO implement
        return true;
    }

	/* (non-Javadoc)
	 * @see org.openmicroscopy.shoola.agents.annotator.AnnotationCtrl#save()
	 */
	public void save() 
	{
		//TODO Auto-generated method stub	
	}
	
}
