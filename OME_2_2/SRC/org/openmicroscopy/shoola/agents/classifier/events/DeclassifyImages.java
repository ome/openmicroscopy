/*
 * org.openmicroscopy.shoola.agents.classifier.events.DeclassifyImages
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
import org.openmicroscopy.shoola.agents.classifier.AttributeComparator;

/**
 * Event that indicates multiple classifications should be erased.
 * 
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2.1
 * @since OME2.2.1
 */
public class DeclassifyImages
// just like DeclassifyImage, I'd ideally like this to extend ReclassifyImages,
// because, in the short term, that's exactly what's happening; but (a) that could
// change and (b) it would introduce a hidden dependency into any class that
// demultiplexes events (both would be ReclassifyImages instances... uh oh!)
{
    private List classificationList;
    
    /**
     * Creates an empty event (classifications to invalidate are added using
     * the addClassification method)
     */
    public DeclassifyImages()
    {
        classificationList = new ArrayList();
    }
    
    /**
     * Creates an event with the specified collection of classification objects
     * to invalidate.
     * @param c The collection of classifications to "erase".
     */
    public DeclassifyImages(Collection c)
    {
        if(c == null)
        {
            classificationList = new ArrayList();
        }
        else
        {
            classificationList = new ArrayList(c);
            Collections.sort(classificationList,new AttributeComparator());
        }
    }
    
    /**
     * Adds a classification to the event package.
     * @param c The classification to add.
     */
    public void addClassification(Classification c)
    {
        if(c != null)
        {
            classificationList.add(c);
            Collections.sort(classificationList,new AttributeComparator());
        }
    }
    
    /**
     * Removes a classification from the event package.
     * @param c The classification to remove.
     */
    public void removeClassification(Classification c)
    {
        if(c != null && classificationList.contains(c))
        {
            classificationList.remove(c);
        }
    }
    
    /**
     * Returns the list of classifications to be invalidated/erased.
     * @return See above.
     */
    public List getClassifications()
    {
        return Collections.unmodifiableList(classificationList);
    }
}
