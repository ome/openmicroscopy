/*
 * org.openmicroscopy.shoola.agents.classifier.events.ReclassifyImages
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
 
package org.openmicroscopy.shoola.agents.classifier.events;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.openmicroscopy.ds.st.Classification;
import org.openmicroscopy.shoola.env.event.RequestEvent;

/**
 * An event for changing image classification.
 *
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
public class ReclassifyImages extends RequestEvent
{
    // classfications already contain image refs, we don't need em
    private List classificationList;
    
    // empty constructor, as to build the map iteratively
    public ReclassifyImages()
    {
        classificationList = new ArrayList();
    }
    
    public ReclassifyImages(Collection c)
    {
        if(c != null)
        {
            classificationList = new ArrayList(c);
            Collections.sort(classificationList);
        }
        else classificationList = new ArrayList();
    }
    
    public void addClassification(Classification c)
    {
        if(c != null)
        {
            classificationList.add(c);
        }
    }
    
    public void removeClassification(Classification c)
    {
        if(c != null)
        {
            classificationList.remove(c);
        }
    }
    
    public List getClassifications()
    {
        return Collections.unmodifiableList(classificationList);
    }
}
