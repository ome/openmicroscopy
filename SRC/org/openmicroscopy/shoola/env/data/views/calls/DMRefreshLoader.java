/*
 * org.openmicroscopy.shoola.env.data.views.calls.DMRefreshLoader
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

package org.openmicroscopy.shoola.env.data.views.calls;


//Java imports
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.data.OmeroDataService;
import org.openmicroscopy.shoola.env.data.OmeroMetadataService;
import org.openmicroscopy.shoola.env.data.model.TimeRefObject;
import org.openmicroscopy.shoola.env.data.views.BatchCall;
import org.openmicroscopy.shoola.env.data.views.BatchCallTree;
import pojos.DataObject;
import pojos.DatasetData;
import pojos.FileAnnotationData;
import pojos.ImageData;
import pojos.ProjectData;
import pojos.ScreenData;
import pojos.TagAnnotationData;

/** 
 * Command to refresh a data trees.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since OME2.2
 */
public class DMRefreshLoader
    extends BatchCallTree
{

    /** The results of the call. */
    private Map      	results;
    
    /** Loads the specified tree. */
    private BatchCall   loadCall;
    
    /**
     * Retrieve the data.
     * 
     * @param rootNodeType The root node.
     * @param nodes   	   The nodes to handle.
     * @throws Exception Thrown if an error occurred.
     */
    private void retrieveData(Class rootNodeType, Map<Long, List> nodes)
    	throws Exception
    {
    	OmeroDataService os = context.getDataService();
        Iterator users = nodes.keySet().iterator();
        long userID;
        List containers;
        
        Object result;
        Set set, children, newChildren, r;
        List<Long> ids, cIds;
        Iterator i, j, c, k;
        Long id;
        Class klass;
        Map topNodes;
        DataObject child, parent;
        Set s;
        while (users.hasNext()) {
        	userID = (Long) users.next();
        	containers = nodes.get(userID);
        	if (containers == null || containers.size() == 0) {
        		result = os.loadContainerHierarchy(rootNodeType, null, 
                		false, userID);
        		if (results.containsKey(userID)) {
        			s = (Set) results.get(userID);
        			s.addAll((Set) result);
        		} else {
        			results.put(userID, result);
        		}
        	} else {
        		set = os.loadContainerHierarchy(rootNodeType, null, 
                        false, userID);
                i = containers.iterator();
                ids = new ArrayList<Long>(containers.size());
                while (i.hasNext()) {
                    ids.add(new Long(((DataObject) i.next()).getId()));
                }
                j = set.iterator();
                children = null;
               
                klass = null;
                topNodes = new HashMap(set.size());
                
                while (j.hasNext()) {
                    newChildren = new HashSet();
                    parent = (DataObject) j.next();
                    if (parent instanceof ProjectData) {
                        children = ((ProjectData) parent).getDatasets();
                        klass = DatasetData.class;
                    } else if (parent instanceof ScreenData) {
                    	children = null;//((ScreenData) parent).getPlates();
                        klass = ScreenData.class;
                    } else if (parent instanceof DatasetData) {
                    	children = new HashSet(1);
                    	children.add(parent);
                    	klass = DatasetData.class;
                    } 
                    topNodes.put(parent, newChildren);
                    if (children != null) {
                    	c = children.iterator();
                        while (c.hasNext()) {
                            child = (DataObject) c.next();
                            id = new Long(child.getId());
                            if (ids.contains(id)) {
                                cIds = new ArrayList(1);
                                cIds.add(id);
                                r = os.loadContainerHierarchy(klass, cIds, 
                                        true, userID);
                                k = r.iterator();
                                while (k.hasNext()) {
                                    newChildren.add(k.next());
                                }
                            } else newChildren.add(child);
                        }
                    }
                }
                result = topNodes;
                if (results.containsKey(userID)) {
        			Map map  = (Map) results.get(userID);
        			map.putAll((Map) result);
        		} else results.put(userID, result);
        	}
        	//results.put(userID, result);
		}
    }
    
    /**
     * Creates a {@link BatchCall} to retrieve a Container tree, either
     * Project or CategoryGroup.
     * 
     * @param rootNodeType 	The type of the root node. Can either:
     *                      {@link ProjectData}.
     * @param nodes   		The nodes to handle.
     * @return The {@link BatchCall}.
     */
    private BatchCall makeBatchCall(final Class rootNodeType, 
                                    final Map<Long, List> nodes)
    {
        return new BatchCall("Loading container tree: ") {
            public void doCall() throws Exception
            {
            	results = new HashMap<Long, Object>(nodes.size());
                //retrieveData(ProjectData.class, nodes);
                retrieveData(rootNodeType, nodes);
            }
        };
    }
    
    /**
     * Creates a {@link BatchCall} to retrieve the images.
     * 
     * @param nodes The map whose keys are the id of user and the values 
     * 				are the corresponding collections of data objects to reload.  		
     * @return The {@link BatchCall}.
     */
    private BatchCall makeImagesBatchCall(final Map<Long, List> nodes)
    {
        return new BatchCall("Loading images: ") {
            public void doCall() throws Exception
            {
                OmeroDataService os = context.getDataService();
                Iterator users = nodes.keySet().iterator();
                long userID;
                List containers;
                Iterator j ;
                TimeRefObject ref;
                while (users.hasNext()) {
                	userID = (Long) users.next();
                	containers = nodes.get(userID);
                	j = containers.iterator();
                	while (j.hasNext()) {
                		ref = (TimeRefObject) j.next();
        				ref.setResults(os.getImagesPeriod(ref.getStartTime(), 
        								ref.getEndTime(), userID, true));
					}
                }
                results = nodes;
            }
        };
    }
    
    /**
     * Creates a {@link BatchCall} to retrieve the files.
     * 
     * @param nodes The map whose keys are the id of user and the values 
     * 				are the corresponding collections of data objects to reload.  		
     * @return The {@link BatchCall}.
     */
    private BatchCall makeFilesBatchCall(final Map<Long, List> nodes)
    {
        return new BatchCall("Loading files: ") {
            public void doCall() throws Exception
            {
            	OmeroMetadataService os = context.getMetadataService();
                Iterator users = nodes.keySet().iterator();
                long userID;
                List containers;
                Iterator j ;
                TimeRefObject ref;
                while (users.hasNext()) {
                	userID = (Long) users.next();
                	containers = nodes.get(userID);
                	j = containers.iterator();
                	while (j.hasNext()) {
                		ref = (TimeRefObject) j.next();
        				ref.setResults(os.loadFiles(ref.getFileType(), userID));
					}
                }
                results = nodes;
            }
        };
    }
    
    /**
     * Creates a {@link BatchCall} to retrieve the files.
     * 
     * @param nodes The map whose keys are the id of user and the values 
     * 				are the corresponding collections of data objects to reload.  		
     * @return The {@link BatchCall}.
     */
    private BatchCall makeTagsBatchCall(final Map<Long, List> nodes)
    {
        return new BatchCall("Loading files: ") {
            public void doCall() throws Exception
            {
                OmeroMetadataService os = context.getMetadataService();
                Iterator users = nodes.keySet().iterator();
                results = new HashMap<Long, Object>(nodes.size());
                long userID;
                Object result;
                while (users.hasNext()) {
                	userID = (Long) users.next();
                	result = os.loadTags(-1L, false, true, userID);
                	results.put(userID, result);
                }
            }
        };
    }
    
    /**
     * Adds the {@link #loadCall} to the computation tree.
     * @see BatchCallTree#buildTree()
     */
    protected void buildTree() { add(loadCall); }

    /**
     * Returns the root node of the requested tree.
     * @see BatchCallTree#getResult()
     */
    protected Object getResult() { return results; }
    
    /**
     * Creates a new instance.
     * If bad arguments are passed, we throw a runtime
	 * exception so to fail early and in the caller's thread.
     * 
     * @param rootNodeType	The type of the root node. 
     * @param nodes        	The map whose keys are the id of user
     * 						and the values are the corresponding collections of
     * 						data objects to reload.
     */
    public DMRefreshLoader(Class rootNodeType, Map<Long, List> nodes)
    {
        if (rootNodeType == null) 
            throw new IllegalArgumentException("No root node type.");
        
        if (nodes == null || nodes.size() == 0)
            throw new IllegalArgumentException("No container with images.");
        if (ImageData.class.equals(rootNodeType)) 
        	loadCall = makeImagesBatchCall(nodes);
        else if (ProjectData.class.equals(rootNodeType) ||
        		ScreenData.class.equals(rootNodeType))
        	loadCall = makeBatchCall(rootNodeType, nodes);
        else if (FileAnnotationData.class.equals(rootNodeType)) {
        	loadCall = makeFilesBatchCall(nodes);
        } else if (TagAnnotationData.class.equals(rootNodeType)) {
        	loadCall = makeTagsBatchCall(nodes);
        }
    }
    
}
