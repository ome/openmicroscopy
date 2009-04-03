/*
 * org.openmicroscopy.shoola.env.data.views.HierarchyBrowsingViewImpl
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
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

package org.openmicroscopy.shoola.env.data.views;


//Java imports
import java.util.Collection;
import java.util.List;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.data.views.calls.HierarchyLoader;
import org.openmicroscopy.shoola.env.data.views.calls.ThumbnailLoader;
import org.openmicroscopy.shoola.env.data.views.calls.ThumbnailSetLoader;
import org.openmicroscopy.shoola.env.event.AgentEventListener;
import pojos.ImageData;

/** 
 * Implementation of the {@link HierarchyBrowsingView} interface.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
class HierarchyBrowsingViewImpl
    implements HierarchyBrowsingView
{

    /** Package private constructor. */
    HierarchyBrowsingViewImpl() {}
    
    /**
     * Implemented as specified by the view interface.
     * @see HierarchyBrowsingView#loadHierarchy(Class, List, long, 
     *                                      AgentEventListener)
     */
    public CallHandle loadHierarchy(Class rootNodeType, List nodesID, 
                                    long userID, AgentEventListener observer)
    {
        BatchCallTree cmd = new HierarchyLoader(rootNodeType, nodesID, userID);
        return cmd.exec(observer);
    }
    
    /**
     * Implemented as specified by the view interface.
     * @see HierarchyBrowsingView#loadThumbnails(Collection, int, int, long,
     *                                           AgentEventListener)
     */
    public CallHandle loadThumbnails(Collection<ImageData> images, 
    								int maxWidth, int maxHeight, long userID,
                                     AgentEventListener observer)
    {
    	BatchCallTree cmd = new ThumbnailSetLoader(images, maxHeight);
        return cmd.exec(observer);
    }

    /**
     * Implemented as specified by the view interface.
     * @see HierarchyBrowsingView#loadImagesAsThumbnails(Collection, long,
     *                                           AgentEventListener)
     */
    public CallHandle loadImagesAsThumbnails(Collection<ImageData> images, 
    								long userID, AgentEventListener observer)
    {
        BatchCallTree cmd = new ThumbnailLoader(images, userID);
        return cmd.exec(observer);
    }

}
