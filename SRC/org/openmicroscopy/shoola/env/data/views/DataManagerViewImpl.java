/*
 * org.openmicroscopy.shoola.env.data.views.DataManagerViewImpl
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

package org.openmicroscopy.shoola.env.data.views;

//Java imports

//Third-party libraries

//Application-internal dependencies
import java.util.Set;

import org.openmicroscopy.shoola.env.event.AgentEventListener;

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:a.falconi@dundee.ac.uk">
 *                  a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
class DataManagerViewImpl
    implements DataManagerView
{

    /**
     * Implemented as specified by the view interface.
     * @see DataManagerView#loadContainerHierarchy(Class, boolean, 
     * AgentEventListener)
     */
    public CallHandle loadContainerHierarchy(Class rootNodeType,
                                            boolean withLeaves,
                                            AgentEventListener observer)
    {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * Implemented as specified by the view interface.
     * @see DataManagerView#getImages(Class, Set, boolean, AgentEventListener)
     */
    public CallHandle getImages(Class rootNodeType, Set rootNodeIDs, boolean in,
                                AgentEventListener observer)
    {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * Implemented as specified by the view interface.
     * @see DataManagerView#getInnerContainer(Class, Set, boolean,
     *                                      AgentEventListener)
     */
    public CallHandle getInnerContainer(Class rootNodeType, Set rootNodeIDs,
                                boolean in, AgentEventListener observer)
    {
        // TODO Auto-generated method stub
        return null;
    }

    
    
}
