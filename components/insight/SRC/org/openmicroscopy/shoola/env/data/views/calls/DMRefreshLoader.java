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
import java.util.Map.Entry;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.data.AdminService;
import org.openmicroscopy.shoola.env.data.OmeroDataService;
import org.openmicroscopy.shoola.env.data.OmeroMetadataService;
import org.openmicroscopy.shoola.env.data.model.TimeRefObject;
import org.openmicroscopy.shoola.env.data.views.BatchCall;
import org.openmicroscopy.shoola.env.data.views.BatchCallTree;
import pojos.DataObject;
import pojos.DatasetData;
import pojos.ExperimenterData;
import pojos.FileAnnotationData;
import pojos.GroupData;
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
    private Object      results;
    
    /** Loads the specified tree. */
    private BatchCall   loadCall;
    
    /**
     * Retrieve the data.
     * 
     * @param rootNodeType The root node.
     * @param nodes   	   The nodes to handle.
     * @param mapResult	   Map hosting the results.
     * @throws Exception Thrown if an error occurred.
     */
    private void retrieveData(Class rootNodeType, Map<Long, List> nodes, 
    		Map<Long, Object> mapResult)
    	throws Exception
    {
    	OmeroDataService os = context.getDataService();
        Iterator users = nodes.entrySet().iterator();
        long userID;
        //TODO Review that code for refresh.
        long groupID = -1;
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
        Entry entry;
        while (users.hasNext()) {
        	entry = (Entry) users.next();
        	userID = (Long) entry.getKey();
        	containers = (List) entry.getValue();
        	if (containers == null || containers.size() == 0) {
        		result = os.loadContainerHierarchy(rootNodeType, null, 
                		false, userID, groupID);
        		if (mapResult.containsKey(userID)) {
        			s = (Set) mapResult.get(userID);
        			s.addAll((Set) result);
        		} else {
        			mapResult.put(userID, result);
        		}
        	} else {
        		set = os.loadContainerHierarchy(rootNodeType, null, 
                        false, userID, groupID);
                i = containers.iterator();
                ids = new ArrayList<Long>(containers.size());
                while (i.hasNext()) {
                    ids.add(Long.valueOf(((DataObject) i.next()).getId()));
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
                            id = Long.valueOf(child.getId());
                            if (ids.contains(id)) {
                                cIds = new ArrayList(1);
                                cIds.add(id);
                                r = os.loadContainerHierarchy(klass, cIds, 
                                        true, userID, groupID);
                                k = r.iterator();
                                while (k.hasNext()) {
                                    newChildren.add(k.next());
                                }
                            } else newChildren.add(child);
                        }
                    }
                }
                result = topNodes;
                if (mapResult.containsKey(userID)) {
        			Map map  = (Map) mapResult.get(userID);
        			map.putAll((Map) result);
        		} else mapResult.put(userID, result);
        	}
		}
    }
    
    /**
     * Creates a {@link BatchCall} to retrieve a Container tree, either
     * <code>Project</code> or <code>Screen</code>.
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
            	Map<Long, Object> r = new HashMap<Long, Object>(nodes.size());
            	results = r;
                //retrieveData(ProjectData.class, nodes);
                retrieveData(rootNodeType, nodes, r);
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
                Iterator i = nodes.entrySet().iterator();
                long userID;
                List containers;
                Iterator j ;
                TimeRefObject ref;
                Entry entry;
                while (i.hasNext()) {
                	entry = (Entry) i.next();
                	userID = (Long) entry.getKey();
                	containers = (List) entry.getValue();
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
     * Creates a {@link BatchCall} to retrieve the groups.
     * 
     * @param nodes The map whose keys are the id of user and the values 
     * 				are the corresponding collections of data objects to reload.  		
     * @return The {@link BatchCall}.
     */
    private BatchCall makeGroupsBatchCall(final Map<Long, List> nodes)
    {
        return new BatchCall("Loading groups: ") {
            public void doCall() throws Exception
            {
                AdminService svc = context.getAdminService();
                Entry entry;
                Iterator i = nodes.entrySet().iterator();
                Iterator j;
                long groupID;
                //Check if the user is an administrator
                Boolean admin = (Boolean)
                	context.lookup(LookupNames.USER_ADMINISTRATOR);
                if (admin != null && admin.booleanValue()) {
                	List<GroupData> groups = svc.loadGroups(-1);
                    List<GroupData> r = new ArrayList<GroupData>();
                    List<Long> toRemove = new ArrayList<Long>();
                    List<GroupData> l;
                    List list;
                    while (i.hasNext()) {
    					entry = (Entry) i.next();
    					list = (List) entry.getValue();
    					j = list.iterator();
    					while (j.hasNext()) {
    						groupID = (Long) j.next();
    						l = svc.loadGroups(groupID);
    						toRemove.add(groupID);
    						if (l.size() == 1) r.add(l.get(0));
    					}
    				}
                    i = groups.iterator();
                    GroupData g;
                    while (i.hasNext()) {
    					g = (GroupData) i.next();
    					if (!toRemove.contains(g.getId())) 
    						r.add(g);
    				}
                    results = r;
                } else {
                	ExperimenterData exp = 
        				(ExperimenterData) context.lookup(
        						LookupNames.CURRENT_USER_DETAILS);
                	List<GroupData> groups = svc.reloadPIGroups(exp);
                	Iterator<GroupData> g = groups.iterator();
                	GroupData gd;
                	List<GroupData> toKeep = new ArrayList<GroupData>();
                	Set leaders;
                	Iterator k;
                	ExperimenterData leader;
                	while (g.hasNext()) {
						gd = (GroupData) g.next();
						leaders = gd.getLeaders();
						if (leaders != null) {
							k = leaders.iterator();
							while (k.hasNext()) {
								leader = (ExperimenterData) k.next();
								if (leader.getId() == exp.getId()) {
									toKeep.add(gd);
								}
							}
						}
					}
                	results = toKeep;
                }
                
                
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
                Map<Long, Object> r = new HashMap<Long, Object>(nodes.size());
                long userID;
                Object result;
                while (users.hasNext()) {
                	userID = (Long) users.next();
                	result = os.loadTags(-1L, false, true, userID, -1);
                	r.put(userID, result);
                }
                results = r;
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
        } else if (GroupData.class.equals(rootNodeType)) {
        	loadCall = makeGroupsBatchCall(nodes);
        }
    }
    
}
