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
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.data.OmeroDataService;
import org.openmicroscopy.shoola.env.data.model.TimeRefObject;
import org.openmicroscopy.shoola.env.data.views.BatchCall;
import org.openmicroscopy.shoola.env.data.views.BatchCallTree;
import pojos.CategoryData;
import pojos.CategoryGroupData;
import pojos.DataObject;
import pojos.DatasetData;
import pojos.ImageData;
import pojos.ProjectData;

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
     * Creates a {@link BatchCall} to retrieve a Container tree, either
     * Project or CategoryGroup.
     * 
     * @param rootNodeType 	The type of the root node. Can either:
     *                      {@link ProjectData} or {@link CategoryGroupData}.
     * @param nodes   		The nodes to handle.
     * @return The {@link BatchCall}.
     */
    private BatchCall makeBatchCall(final Class rootNodeType, 
                                    final Map<Long, List> nodes)
    {
        return new BatchCall("Loading container tree: ") {
            public void doCall() throws Exception
            {
                OmeroDataService os = context.getDataService();
                Iterator users = nodes.keySet().iterator();
                long userID;
                List containers;
                results = new HashMap<Long, Object>(nodes.size());
                Object result;
                while (users.hasNext()) {
                	userID = (Long) users.next();
                	containers = nodes.get(userID);
                	if (containers == null || containers.size() == 0) {
                		result = os.loadContainerHierarchy(rootNodeType, null, 
                        		false, userID);
                	} else {
                		Set set = os.loadContainerHierarchy(rootNodeType, null, 
                                false, userID);
                        Set<Long> ids;
                        Iterator i = containers.iterator();
                        ids = new HashSet<Long>(containers.size());
                        while (i.hasNext()) {
                            ids.add(new Long(((DataObject) i.next()).getId()));
                        }
                        Iterator j = set.iterator(), c;
                        Set children = null;
                        Long id;
                        Class klass = null;
                        Set newChildren;
                        Map topNodes = new HashMap(set.size());
                        DataObject child, parent;
                        Set cIds, r;
                        Iterator k;
                        while (j.hasNext()) {
                            newChildren = new HashSet();
                            parent = (DataObject) j.next();
                            if (parent instanceof ProjectData) {
                                children = ((ProjectData) parent).getDatasets();
                                klass = DatasetData.class;
                            } else if (parent instanceof CategoryGroupData) {
                                klass = CategoryData.class;
                                children = 
                                	((CategoryGroupData) parent).getCategories();
                            } else if (parent instanceof DatasetData) {
                            	children = new HashSet(1);
                            	children.add(parent);
                            	klass = DatasetData.class;
                            } else if (parent instanceof CategoryData) {
                            	children = new HashSet(1);
                            	children.add(parent);
                            	klass = CategoryData.class;
                            }
                            topNodes.put(parent, newChildren);
                            c = children.iterator();
                            while (c.hasNext()) {
                                child = (DataObject) c.next();
                                id = new Long(child.getId());
                                if (ids.contains(id)) {
                                    cIds = new HashSet(1);
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
                        result = topNodes;
                	}
                	results.put(userID, result);
				}
            }
        };
    }
    
    /**
     * Creates a {@link BatchCall} to retrieve the images
     * 
     * @param nodes   		
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
                Timestamp lowerTime, upperTime;
                while (users.hasNext()) {
                	userID = (Long) users.next();
                	containers = nodes.get(userID);
                	j = containers.iterator();
                	while (j.hasNext()) {
                		ref = (TimeRefObject) j.next();
    
        				ref.setResults(os.getImagesPeriod(ref.getStartTime(), ref.getEndTime(), 
									userID));
					}
                }
                results = nodes;
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
     * @param rootNodeType	The type of the root node. Can either:
     *                      {@link ProjectData} or
     *                      {@link CategoryGroupData}.
     * @param nodes           The Id of the root. 
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
        		CategoryGroupData.class.equals(rootNodeType))
        	loadCall = makeBatchCall(rootNodeType,  nodes);
    }
    
}
