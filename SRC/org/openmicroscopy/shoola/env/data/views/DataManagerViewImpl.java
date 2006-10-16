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
import java.util.List;
import java.util.Map;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.data.views.calls.AnnotationLoader;
import org.openmicroscopy.shoola.env.data.views.calls.AnnotationSaver;
import org.openmicroscopy.shoola.env.data.views.calls.ClassificationLoader;
import org.openmicroscopy.shoola.env.data.views.calls.ClassificationSaver;
import org.openmicroscopy.shoola.env.data.views.calls.ContainerCounterLoader;
import org.openmicroscopy.shoola.env.data.views.calls.DMLoader;
import org.openmicroscopy.shoola.env.data.views.calls.DMRefreshLoader;
import org.openmicroscopy.shoola.env.data.views.calls.DataObjectSaver;
import org.openmicroscopy.shoola.env.data.views.calls.ExistingObjectsLoader;
import org.openmicroscopy.shoola.env.data.views.calls.ExistingObjectsSaver;
import org.openmicroscopy.shoola.env.data.views.calls.ImagesLoader;
import org.openmicroscopy.shoola.env.data.views.calls.ThumbnailLoader;
import org.openmicroscopy.shoola.env.event.AgentEventListener;
import pojos.AnnotationData;
import pojos.DataObject;
import pojos.ImageData;

/** 
 * Implementation of the {@link DataManagerView} implementation.
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
     * @see DataManagerView#loadContainerHierarchy(Class, Set, boolean, 
     * 						Class, long, AgentEventListener)
     */
    public CallHandle loadContainerHierarchy(Class rootNodeType,
                                            Set rootNodeIDs,
                                            boolean withLeaves,
                                            Class rootLevel,
                                            long rootLevelID,
                                            AgentEventListener observer)
    {
        BatchCallTree cmd = new DMLoader(rootNodeType, rootNodeIDs, withLeaves,
                						rootLevel, rootLevelID);
        return cmd.exec(observer);
    }

    /**
     * Implemented as specified by the view interface.
     * @see DataManagerView#loadImages(AgentEventListener)
     */
    public CallHandle loadImages(AgentEventListener observer)
    {
        BatchCallTree cmd = new ImagesLoader();
        return cmd.exec(observer);
    }

    /**
     * Implemented as specified by the view interface.
     * @see DataManagerView#getImages(Class, Set, Class, long,
     *                              AgentEventListener)
     */
    public CallHandle getImages(Class nodeType, Set nodeIDs, Class rootLevel,
            					long rootLevelID, AgentEventListener observer)
    {
        BatchCallTree cmd = new ImagesLoader(nodeType, nodeIDs, rootLevel, 
                							rootLevelID);
        return cmd.exec(observer);
    }

    /**
     * Implemented as specified by the view interface.
     * @see DataManagerView#createDataObject(DataObject, DataObject, 
     * 										AgentEventListener)
     */
    public CallHandle createDataObject(DataObject userObject, DataObject parent,
                                        AgentEventListener observer)
    {
        BatchCallTree cmd = new DataObjectSaver(userObject, parent,
                                                DataObjectSaver.CREATE);
        return cmd.exec(observer);
    } 
    
    /**
     * Implemented as specified by the view interface.
     * @see DataManagerView#updateDataObject(DataObject, AgentEventListener)
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
     * @see DataManagerView#removeDataObjects(List, DataObject, 
     * 										AgentEventListener)
     */
    public CallHandle removeDataObjects(List userObjects, DataObject parent, 
            						AgentEventListener observer)
    {
        BatchCallTree cmd = new DataObjectSaver(userObjects, parent, 
                							DataObjectSaver.REMOVE);
        return cmd.exec(observer);  
    }
    
    /**
     * Implemented as specified by the view interface.
     * @see DataManagerView#removeDataObjects(Map, AgentEventListener)
     */
    public CallHandle removeDataObjects(Map objects,
                                    AgentEventListener observer)
    {
        BatchCallTree cmd = new DataObjectSaver(objects, 
                                           DataObjectSaver.REMOVE);
        return cmd.exec(observer);                            
    }
    /**
     * Implemented as specified by the view interface.
     * @see DataManagerView#countContainerItems(Set, AgentEventListener)
     */
    public CallHandle countContainerItems(Set rootIDs, 
            								AgentEventListener observer)
    {
        BatchCallTree cmd = new ContainerCounterLoader(rootIDs);
        return cmd.exec(observer);  
    }

    /**
     * Implemented as specified by the view interface.
     * @see DataManagerView#createAnnotation(DataObject, AnnotationData,
     *                                      AgentEventListener)
     */
    public CallHandle createAnnotation(DataObject annotatedObject, 
                                       AnnotationData data,
                                        AgentEventListener observer)
    {
        BatchCallTree cmd = new AnnotationSaver(annotatedObject, data, 
                                    AnnotationSaver.CREATE);
        return cmd.exec(observer);
    }
    
    /**
     * Implemented as specified by the view interface.
     * @see DataManagerView#updateAnnotation(DataObject, AnnotationData,
     *                                      AgentEventListener)
     */
    public CallHandle updateAnnotation(DataObject annotatedObject,
                                        AnnotationData data,
                                        AgentEventListener observer)
    {
        BatchCallTree cmd = new AnnotationSaver(annotatedObject, data, 
                                                AnnotationSaver.UPDATE);
        return cmd.exec(observer);
    }

    /**
     * Implemented as specified by the view interface.
     * @see DataManagerView#deleteAnnotation(DataObject, AnnotationData,
     *                                       AgentEventListener)
     */
    public CallHandle deleteAnnotation(DataObject annotatedObject,
                                        AnnotationData data,
                                        AgentEventListener observer)
    {
        BatchCallTree cmd = new AnnotationSaver(annotatedObject, data, 
                                                AnnotationSaver.DELETE);
        return cmd.exec(observer);
    }
    
    /**
     * Implemented as specified by the view interface.
     * @see DataManagerView#loadAnnotations(Class, int, 
     *                                          AgentEventListener)
     */
    public CallHandle loadAnnotations(Class nodeType, long nodeID,
                                       AgentEventListener observer)
    {
        BatchCallTree cmd = new AnnotationLoader(nodeType, nodeID);
        return cmd.exec(observer);
    }

    /**
     * Implemented as specified by the view interface.
     * @see DataManagerView#loadThumbnail(ImageData, int, int,
     *                                          AgentEventListener)
     */
    public CallHandle loadThumbnail(ImageData image, int maxWidth, 
            int maxHeight, AgentEventListener observer)
    {
        BatchCallTree cmd = new ThumbnailLoader(image, maxWidth, maxHeight);
        return cmd.exec(observer);
    }
    
    /**
     * Implemented as specified by the view interface.
     * @see DataManagerView#loadClassificationPaths(Set, int, 
     *                                              AgentEventListener)
     */
    public CallHandle loadClassificationPaths(Set imageIDs, int algorithm,
                                            AgentEventListener observer)
    {
        BatchCallTree cmd = new ClassificationLoader(imageIDs, algorithm);
        return cmd.exec(observer);
    }

    /**
     * Implemented as specified by the view interface.
     * @see DataManagerView#classify(Set, Set, AgentEventListener)
     */
    public CallHandle classify(Set images, Set categories, 
                                AgentEventListener observer)
    {
        BatchCallTree cmd = new ClassificationSaver(images, categories, true);
        return cmd.exec(observer);
    }

    /**
     * Implemented as specified by the view interface.
     * @see DataManagerView#declassify(Set, Set, AgentEventListener)
     */
    public CallHandle declassify(Set images, Set categories, 
                                AgentEventListener observer)
    {
        BatchCallTree cmd = new ClassificationSaver(images, categories, false);
        return cmd.exec(observer);
    }

    /**
     * Implemented as specified by the view interface.
     * @see DataManagerView#loadExistingObjects(Class, Set, Class, long, 
     *                                          AgentEventListener)
     */
    public CallHandle loadExistingObjects(Class nodeType, Set nodeIDs, 
            Class rootLevel, long rootLevelID, AgentEventListener observer)
    {
        BatchCallTree cmd = new ExistingObjectsLoader(nodeType, nodeIDs, 
                                rootLevel, rootLevelID);
        return cmd.exec(observer);
    }

    /**
     * Implemented as specified by the view interface.
     * @see DataManagerView#addExistingObjects(DataObject, Set, 
     *                                          AgentEventListener)
     */
    public CallHandle addExistingObjects(DataObject parent, Set children, 
                                        AgentEventListener observer)
    {
        BatchCallTree cmd = new ExistingObjectsSaver(parent, children);
        return cmd.exec(observer);
    }

    /**
     * Implemented as specified by the view interface.
     * @see DataManagerView#addExistingObjects(Map, 
     *                                          AgentEventListener)
     */
    public CallHandle addExistingObjects(Map toPaste, 
                                        AgentEventListener observer)
    {
        BatchCallTree cmd = new ExistingObjectsSaver(toPaste, null);
        return cmd.exec(observer);
    }
    
    /**
     * Implemented as specified by the view interface.
     * @see DataManagerView#refreshHierarchy(Class, List, Class, long, 
     *                                      AgentEventListener)
     */
    public CallHandle refreshHierarchy(Class rootNodeType, 
            List containerWithImages, Class rootLevel, long rootLevelID, 
            AgentEventListener observer)
    {
        BatchCallTree cmd = new DMRefreshLoader(rootNodeType, 
                    containerWithImages, rootLevel, rootLevelID);
        return cmd.exec(observer);
    }

    /**
     * Implemented as specified by the view interface.
     * @see DataManagerView#cutAndPaste(Map, Map, AgentEventListener)
     */
    public CallHandle cutAndPaste(Map toPaste, Map toCut, 
                                    AgentEventListener observer)
    {
        BatchCallTree cmd = new ExistingObjectsSaver(toPaste, toCut);
        return cmd.exec(observer);
    }
    
}
