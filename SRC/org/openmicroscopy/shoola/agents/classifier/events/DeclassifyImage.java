/*
 * org.openmicroscopy.shoola.agents.classifier.events.DeclassifyImage
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

import org.openmicroscopy.ds.st.Classification;
import org.openmicroscopy.shoola.env.event.RequestEvent;

/**
 * Event that indicates a classification should be erased.
 * 
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2.1
 * @since OME2.2.1
 */
public class DeclassifyImage extends RequestEvent
// I'd like this to extend ReclassifyImage because it has exactly the same
// semantics, and for now, the same actions, but both will be instances of
// ReclassifyImage and that may introduce a nasty hidden dependency in any
// class that demultiplexes events.
{
    private Classification changed;
    
    /**
     * Constructs a DeclassifyImage event, with the classification to be
     * invalidated as the parameter.
     * @param classification See above.
     * @throws IllegalArgumentException If the classification is null.
     */
    public DeclassifyImage(Classification classification)
    {
        if(classification == null)
        {
            throw new IllegalArgumentException("Classification cannot be null.");
        }
        changed = classification;
    }
    
    /**
     * Returns the embedded classification to be invalidated.
     * @return See above.
     */
    public Classification getClassification()
    {
        return changed;
    }
}
