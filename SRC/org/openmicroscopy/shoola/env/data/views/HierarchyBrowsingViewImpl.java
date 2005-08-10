/*
 * org.openmicroscopy.shoola.env.data.views.HierarchyBrowsingViewImpl
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
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.data.model.CategoryData;
import org.openmicroscopy.shoola.env.data.model.CategoryGroupData;
import org.openmicroscopy.shoola.env.data.model.ProjectSummary;
import org.openmicroscopy.shoola.env.data.views.calls.AnnotationLoader;
import org.openmicroscopy.shoola.env.data.views.calls.ClassificationLoader;
import org.openmicroscopy.shoola.env.data.views.calls.ClassificationSaver;
import org.openmicroscopy.shoola.env.data.views.calls.HierarchyFinder;
import org.openmicroscopy.shoola.env.data.views.calls.HierarchyLoader;
import org.openmicroscopy.shoola.env.data.views.calls.ThumbnailLoader;
import org.openmicroscopy.shoola.env.event.AgentEventListener;

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
     * @see HierarchyBrowsingView#loadHierarchy(Class, int, AgentEventListener)
     */
    public CallHandle loadHierarchy(Class rootNodeType, int nodeID, 
                                    AgentEventListener observer)
    {
        BatchCallTree cmd = new HierarchyLoader(rootNodeType, nodeID);
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
     * @see HierarchyBrowsingView#findPDIHierarchies(Set, AgentEventListener)
     */
    public CallHandle findPDIHierarchies(Set imgSummaries, 
                                         AgentEventListener observer)
    {
        BatchCallTree cmd = 
                        new HierarchyFinder(ProjectSummary.class, imgSummaries);
        return cmd.exec(observer);
    }

    /**
     * Implemented as specified by the view interface.
     * @see HierarchyBrowsingView#findCGCIHierarchies(Set, AgentEventListener)
     */
    public CallHandle findCGCIHierarchies(Set imgSummaries, 
                                          AgentEventListener observer)
    {
        BatchCallTree cmd = 
                    new HierarchyFinder(CategoryGroupData.class, imgSummaries);
        return cmd.exec(observer);
    }

    /**
     * Implemented as specified by the view interface.
     * @see HierarchyBrowsingView#loadClassificationPaths(int, boolean,
     *                                  AgentEventListener)
     */
    public CallHandle loadClassificationPaths(int imageID, boolean classsified,
            AgentEventListener observer)
    {
        BatchCallTree cmd = new ClassificationLoader(imageID, classsified);
        return cmd.exec(observer);
    }

    /**
     * Implemented as specified by the view interface.
     * @see HierarchyBrowsingView#classify(CategoryData, Set,
     *                                      AgentEventListener)
     */
    public CallHandle classify(CategoryData data, Set imgIDs, 
                        AgentEventListener observer)
    {
        BatchCallTree cmd = new ClassificationSaver(data, imgIDs, true);
        return cmd.exec(observer);
    }

    /**
     * Implemented as specified by the view interface.
     * @see HierarchyBrowsingView#declassify(CategoryData, Set,
     *                                      AgentEventListener)
     */
    public CallHandle declassify(CategoryData data, Set imgIDs, 
                                AgentEventListener observer)
    {
        BatchCallTree cmd = new ClassificationSaver(data, imgIDs, false);
        return cmd.exec(observer);
    }

    /**
     * Implemented as specified by the view interface.
     * @see HierarchyBrowsingView#loadAnnotations(int, int,
     *                                      AgentEventListener)
     */
    public CallHandle loadAnnotations(int nodeTypeID, int nodeID,
                                    AgentEventListener observer)
    {
        BatchCallTree cmd = new AnnotationLoader(nodeTypeID, nodeID);
        return cmd.exec(observer);
    }
    
}
