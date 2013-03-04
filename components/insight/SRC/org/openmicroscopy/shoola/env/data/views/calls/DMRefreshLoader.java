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
import java.util.Arrays;
import java.util.Collection;
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
import org.openmicroscopy.shoola.env.data.util.SecurityContext;
import org.openmicroscopy.shoola.env.data.views.BatchCall;
import org.openmicroscopy.shoola.env.data.views.BatchCallTree;
import pojos.DataObject;
import pojos.DatasetData;
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
    private Object results;
    
    /** Loads the specified tree. */
    private BatchCall loadCall;
    
    /**
     * Retrieve the data.
     * 
     * @param rootNodeType The root node.
     * @param nodes   	   The nodes to handle.
     * @param mapResult	   Map hosting the results.
     * @throws Exception Thrown if an error occurred.
     */
    private void retrieveData(Class rootNodeType,
    		Map<SecurityContext, List> nodes,
    		Map<SecurityContext, Object> mapResult)
    	throws Exception
    {
    	OmeroDataService os = context.getDataService();
        Iterator<Entry<SecurityContext, List>> 
        users = nodes.entrySet().iterator();
        long userID;
        List containers;
        
        Object result;
        Set set, children, newChildren, r;
        List<Long> ids;
        Iterator i, j, c, k;
        Long id;
        Class klass;
        Map topNodes;
        DataObject child, parent;
        Set s;
        Entry<SecurityContext, List> entry;
        SecurityContext ctx;
        TimeRefObject ref;
        Object object;
        
        while (users.hasNext()) {
        	entry = users.next();
        	ctx = entry.getKey();
        	userID = ctx.getExperimenter();
        	containers = entry.getValue();
        	if (containers == null || containers.size() == 0) {
        		result = os.loadContainerHierarchy(ctx, rootNodeType, null, 
                		false, ctx.getExperimenter());
        		if (mapResult.containsKey(ctx)) {
        			s = (Set) mapResult.get(userID);
        			s.addAll((Set) result);
        		} else {
        			mapResult.put(ctx, result);
        		}
        	} else {
        		//First need to extract any TimeRefObject
        		 topNodes = new HashMap();
        		i = containers.iterator();
        		ids = new ArrayList<Long>();
        		while (i.hasNext()) {
					object = i.next();
					if (object instanceof TimeRefObject) {
						ref = (TimeRefObject) object;
						if (ref.getFileType() == TimeRefObject.FILE_IMAGE_TYPE)
						ref.setResults(
								os.getExperimenterImages(ctx, userID, true));
						topNodes.put(ref, ref);
					} else {
						ids.add(Long.valueOf(((DataObject) object).getId()));
					}
				}
        		//load the rest.
        		set = os.loadContainerHierarchy(ctx, rootNodeType, null,
                        false, userID);
                j = set.iterator();
                children = null;
               
                klass = null;
               
                
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
                                r = os.loadContainerHierarchy(ctx, klass,
                                		Arrays.asList(id), true, userID);
                                k = r.iterator();
                                while (k.hasNext()) {
                                    newChildren.add(k.next());
                                }
                            } else newChildren.add(child);
                        }
                    }
                }
                result = topNodes;
                if (mapResult.containsKey(ctx)) {
        			Map map  = (Map) mapResult.get(userID);
        			map.putAll((Map) result);
        		} else mapResult.put(ctx, result);
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
                                    final Map<SecurityContext, List> nodes)
    {
        return new BatchCall("Loading container tree: ") {
            public void doCall() throws Exception
            {
            	Map<SecurityContext, Object> r = 
            		new HashMap<SecurityContext, Object>(nodes.size());
            	results = r;
                retrieveData(rootNodeType, nodes, r);
            }
        };
    }
    
    /**
     * Creates a {@link BatchCall} to retrieve the images.
     * 
     * @param nodes The map whose keys are the security context and the values 
     * 				are the corresponding collections of data objects to reload.
     * @return The {@link BatchCall}.
     */
    private BatchCall makeImagesBatchCall(final Map<SecurityContext, List> nodes)
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
                SecurityContext ctx;
                while (i.hasNext()) {
                	entry = (Entry) i.next();
                	ctx = (SecurityContext) entry.getKey();
                	userID = ctx.getExperimenter();
                	containers = (List) entry.getValue();
                	j = containers.iterator();
                	while (j.hasNext()) {
                		ref = (TimeRefObject) j.next();
        				ref.setResults(os.getImagesPeriod(ctx,
        					ref.getStartTime(), ref.getEndTime(), userID,
        					true));
					}
                }
                results = nodes;
            }
        };
    }
    
    /**
     * Creates a {@link BatchCall} to retrieve the groups.
     * 
     * @param nodes The map whose keys are the security context and the values 
     * 				are the corresponding collections of data objects to reload.
     * @return The {@link BatchCall}.
     */
    private BatchCall makeGroupsBatchCall(final Map<SecurityContext, List> nodes)
    {
        return new BatchCall("Loading groups: ") {
            public void doCall() throws Exception
            {
            	AdminService svc = context.getAdminService();
            	//Check if the user is an administrator
                Boolean admin = (Boolean)
                	context.lookup(LookupNames.USER_ADMINISTRATOR);
                if (admin != null && admin.booleanValue()) {
                	Iterator<Entry<SecurityContext, List>> i = 
                		nodes.entrySet().iterator();
                	Entry<SecurityContext, List> e;
                	SecurityContext ctx;
                	while (i.hasNext()) {
    					e = i.next();
    					ctx = e.getKey();
    					List<GroupData> groups = svc.loadGroups(ctx, -1);
                        List<GroupData> r = new ArrayList<GroupData>();
                        List<Long> toRemove = new ArrayList<Long>();
                        List<GroupData> l;
                        List list;
                        list = e.getValue();
    					Iterator j = list.iterator();
    					while (j.hasNext()) {
    						long groupID = (Long) j.next();
    						l = svc.loadGroups(ctx, groupID);
    						toRemove.add(groupID);
    						if (l.size() == 1) r.add(l.get(0));
    					}
    					Iterator<GroupData> k = groups.iterator();
                        GroupData g;
                        while (k.hasNext()) {
        					g = (GroupData) k.next();
        					if (!toRemove.contains(g.getId())) 
        						r.add(g);
        				}
                        results = r;
    				}
                } else { //Not admin groups owner.
                	Collection groups = (Collection) context.lookup(
        						LookupNames.USER_GROUP_DETAILS);
                	Iterator i = groups.iterator();
                	GroupData group;
                	SecurityContext ctx;
                	List<GroupData> l = new ArrayList<GroupData>();
                	while (i.hasNext()) {
                		group = (GroupData) i.next();
						ctx = new SecurityContext(group.getId());
						l.addAll(svc.loadGroups(ctx, group.getId()));
					}
                	context.bind(LookupNames.USER_GROUP_DETAILS, l);
                	results = l;
                }
            }
        };
    }
    
    /**
     * Creates a {@link BatchCall} to retrieve the files.
     * 
     * @param nodes The map whose keys are the security context and the values 
     * 				are the corresponding collections of data objects to reload.
     * @return The {@link BatchCall}.
     */
    private BatchCall makeFilesBatchCall(final Map<SecurityContext, List> nodes)
    {
        return new BatchCall("Loading files: ") {
            public void doCall() throws Exception
            {
            	OmeroMetadataService os = context.getMetadataService();
                //Iterator users = nodes.keySet().iterator();
                long userID;
                List containers;
                Iterator j ;
                TimeRefObject ref;
                Entry entry;
                SecurityContext ctx;
                Iterator i = nodes.entrySet().iterator();
                while (i.hasNext()) {
                	entry = (Entry) i.next();
					ctx = (SecurityContext) entry.getKey();
					userID = ctx.getExperimenter();
					containers = (List) entry.getValue();
					j = containers.iterator();
                	while (j.hasNext()) {
                		ref = (TimeRefObject) j.next();
        				ref.setResults(os.loadFiles(ctx, ref.getFileType(),
        					userID));
					}
				}
                results = nodes;
            }
        };
    }
    
    /**
     * Creates a {@link BatchCall} to retrieve the files.
     * 
     * @param nodes The map whose keys are the security context and the values 
     * 				are the corresponding collections of data objects to reload.
     * @return The {@link BatchCall}.
     */
    private BatchCall makeTagsBatchCall(final Map<SecurityContext, List> nodes)
    {
        return new BatchCall("Loading files: ") {
            public void doCall() throws Exception
            {
                OmeroMetadataService os = context.getMetadataService();
                Map<SecurityContext, Object> r = 
                	new HashMap<SecurityContext, Object>(nodes.size());
                long userID;
                Object result;
                Entry entry;
                Iterator j = nodes.entrySet().iterator();
                List l;
                Collection tags;
                Iterator k;
                Iterator<TagAnnotationData> i;
                TagAnnotationData tag, child;
                Map<Long, Collection> values;
                String ns;
                Set<TagAnnotationData> set;
                SecurityContext ctx;
                while (j.hasNext()) {
                	entry = (Entry) j.next();
                	ctx = (SecurityContext) entry.getKey();
                	userID = ctx.getExperimenter();
                	l = (List) entry.getValue();
                	
                	tags = os.loadTags(ctx, -1L, false, true, userID, -1);
                	List<Object> tagResults = new ArrayList<Object>();
                	if (l == null || l.size() == 0) {
                		r.put(ctx, tags);
                	} else {
                		values = new HashMap<Long, Collection>();
                		k = l.iterator();
                		Object ob;
                		TimeRefObject ref;
                		while (k.hasNext()) {
                			ob = k.next();
                			if (ob instanceof TagAnnotationData) {
                				tag = (TagAnnotationData) ob;
    							values.put(tag.getId(), os.loadTags(ctx,
    									tag.getId(), true, false, userID, -1));
                			} else {
                				ref = (TimeRefObject) ob;
                				ref.setResults(os.loadFiles(ctx, 
                					ref.getFileType(), userID));
                				tagResults.add(ref);
                			}
						}
                		k = tags.iterator();
                    	while (k.hasNext()) {
                    		tag = (TagAnnotationData) k.next();
                    		tagResults.add(tag);
                    		ns = tag.getNameSpace();
							if (TagAnnotationData.INSIGHT_TAGSET_NS.equals(ns))
							{
								set = tag.getTags();
								i = set.iterator();
								while (i.hasNext()) {
									child = i.next();
									if (values.containsKey(child.getId())) {
										child.setDataObjects(
	    	        					(Set<DataObject>) values.get(
	    	        							child.getId()));
									}
								}
							} else {
								if (values.containsKey(tag.getId()))
    								tag.setDataObjects(
    										(Set<DataObject>) values.get(
    												tag.getId()));
							}
    					}
                		r.put(ctx, tagResults);
                	}
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
     * @param nodes        	The map whose keys are the security context
     * 						and the values are the corresponding collections of
     * 						data objects to reload.
     */
    public DMRefreshLoader(Class rootNodeType,
    		Map<SecurityContext, List> nodes)
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
