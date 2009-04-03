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
import java.util.Map;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.data.views.calls.ChannelMetadataLoader;
import org.openmicroscopy.shoola.env.data.views.calls.ClassificationLoader;
import org.openmicroscopy.shoola.env.data.views.calls.ClassificationSaver;
import org.openmicroscopy.shoola.env.data.views.calls.DataObjectSaver;
import org.openmicroscopy.shoola.env.data.views.calls.HierarchyFinder;
import org.openmicroscopy.shoola.env.data.views.calls.HierarchyLoader;
import org.openmicroscopy.shoola.env.data.views.calls.ImagesLoader;
import org.openmicroscopy.shoola.env.data.views.calls.ThumbnailLoader;
import org.openmicroscopy.shoola.env.event.AgentEventListener;
import pojos.CategoryGroupData;
import pojos.DataObject;
import pojos.ImageData;
import pojos.ProjectData;

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
     * @see HierarchyBrowsingView#loadHierarchy(Class, Set, long, 
     *                                      AgentEventListener)
     */
    public CallHandle loadHierarchy(Class rootNodeType, Set nodesID, 
                                    long userID, AgentEventListener observer)
    {
        BatchCallTree cmd = new HierarchyLoader(rootNodeType, nodesID, userID);
        return cmd.exec(observer);
    }
    
    /**
     * Implemented as specified by the view interface.
     * @see HierarchyBrowsingView#loadThumbnails(Set, int, int, 
     *                                           AgentEventListener)
     */
    public CallHandle loadThumbnails(Set imgSummaries, 
                                     int maxWidth, int maxHeight,
                                     AgentEventListener observer)
    {
        BatchCallTree cmd = 
                        new ThumbnailLoader(imgSummaries, maxWidth, maxHeight);
        return cmd.exec(observer);
    }

    /**
     * Implemented as specified by the view interface.
     * @see HierarchyBrowsingView#findPDIHierarchies(Set, long, 
     *                                              AgentEventListener)
     */
    public CallHandle findPDIHierarchies(Set ids, long userID,
                                        AgentEventListener observer)
    {
        BatchCallTree cmd = new HierarchyFinder(ProjectData.class, ids, userID);
        return cmd.exec(observer);
    }

    /**
     * Implemented as specified by the view interface.
     * @see HierarchyBrowsingView#findCGCIHierarchies(Set, long,
     *                                              AgentEventListener)
     */
    public CallHandle findCGCIHierarchies(Set ids, long userID, 
                                        AgentEventListener observer)
    {
        BatchCallTree cmd = new HierarchyFinder(CategoryGroupData.class, ids, 
        										userID);
        return cmd.exec(observer);
    }

    /**
     * Implemented as specified by the view interface.
     * @see HierarchyBrowsingView#loadClassificationPaths(Set, int, long,
     *                                  AgentEventListener)
     */
    public CallHandle loadClassificationPaths(Set imageIDs, int algorithm, 
    		long userID, AgentEventListener observer)
    {
        BatchCallTree cmd = new ClassificationLoader(imageIDs, algorithm, 
        											userID);
        return cmd.exec(observer);
    }
    
    /**
     * Implemented as specified by the view interface.
     * @see HierarchyBrowsingView#classify(Set, Set, AgentEventListener)
     */
    public CallHandle classify(Set images, Set categories, 
                                AgentEventListener observer)
    {
        BatchCallTree cmd = new ClassificationSaver(images, categories, true);
        return cmd.exec(observer);
    }

    /**
     * Implemented as specified by the view interface.
     * @see HierarchyBrowsingView#declassify(Set, Set, AgentEventListener)
     */
    public CallHandle declassify(Set images, Set categories, 
                                AgentEventListener observer)
    {
        BatchCallTree cmd = new ClassificationSaver(images, categories, false);
        return cmd.exec(observer);
    }

    /**
     * Implemented as specified by the view interface.
     * @see HierarchyBrowsingView#loadImages(Set, long, AgentEventListener)
     */
    public CallHandle loadImages(Set imageIDs, long userID, 
    							AgentEventListener observer)
    {
        BatchCallTree cmd = new ImagesLoader(ImageData.class, imageIDs, userID);
        return cmd.exec(observer);
    }

    /**
     * Implemented as specified by the view interface.
     * @see HierarchyBrowsingView#updateDataObject(DataObject, 
     *                                          AgentEventListener)
     */
    public CallHandle updateDataObject(DataObject userObject, 
                    AgentEventListener observer)
    {
        BatchCallTree cmd = new DataObjectSaver(userObject, null,
                DataObjectSaver.UPDATE);
        return cmd.exec(observer); 
    }

    /**
     * Implemented as specified by the view interface.
     * @see HierarchyBrowsingView#loadChannelsData(long, AgentEventListener)
     */
    public CallHandle loadChannelsData(long pixelsID, 
                                        AgentEventListener observer)
    {
        BatchCallTree cmd = new ChannelMetadataLoader(pixelsID);
        return cmd.exec(observer);
    }
    
    /**
     * Implemented as specified by the view interface.
     * @see HierarchyBrowsingView#removeDataObjects(Set, DataObject, 
     *                                      AgentEventListener)
     */
    public CallHandle removeDataObjects(Set userObjects, DataObject parent, 
                                    AgentEventListener observer)
    {
        BatchCallTree cmd = new DataObjectSaver(userObjects, parent, 
                                            DataObjectSaver.REMOVE);
        return cmd.exec(observer);  
    }
    
    /**
     * Implemented as specified by the view interface.
     * @see HierarchyBrowsingView#removeDataObjects(Map, AgentEventListener)
     */
    public CallHandle removeDataObjects(Map objects,
                                    AgentEventListener observer)
    {
        BatchCallTree cmd = new DataObjectSaver(objects, 
                                           DataObjectSaver.REMOVE);
        return cmd.exec(observer);                            
    }
    
}
