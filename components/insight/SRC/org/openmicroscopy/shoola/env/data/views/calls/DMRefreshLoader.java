/*
 * org.openmicroscopy.shoola.env.data.views.calls.DMRefreshLoader
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2014 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
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
import org.apache.commons.collections.CollectionUtils;

//Application-internal dependencies
import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.config.AgentInfo;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.AdminService;
import org.openmicroscopy.shoola.env.data.OmeroDataService;
import org.openmicroscopy.shoola.env.data.OmeroMetadataService;
import org.openmicroscopy.shoola.env.data.model.TimeRefObject;
import omero.gateway.SecurityContext;
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
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 *         <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 *         <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
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
        	if (CollectionUtils.isEmpty(containers)) {
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
     * Returns all the groups the user is a member of.
     *
     * @return See above.
     */
    private Collection getAllGroups()
    {
        return (Collection) context.lookup(LookupNames.USER_GROUP_DETAILS);
    }

    /**
     * Returns the collection of groups the current user is the leader of.
     * 
     * @return See above.
     */
    public Set getGroupsLeaderOf()
    {
        Set values = new HashSet();
        Collection groups = getAllGroups();
        Iterator i = groups.iterator();
        GroupData g;
        Set leaders;
        ExperimenterData exp = (ExperimenterData) context.lookup(
                LookupNames.CURRENT_USER_DETAILS);
        long id = exp.getId();
        Iterator j;
        while (i.hasNext()) {
            g = (GroupData) i.next();
            leaders = g.getLeaders();
            if (leaders != null && leaders.size() > 0) {
                j = leaders.iterator();
                while (j.hasNext()) {
                    exp = (ExperimenterData) j.next();
                    if (exp.getId() == id) {
                        values.add(g);
                        break;
                    }
                }
            }
        }
        return values;
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
                    Collection allgroups = getAllGroups();
                	Collection groups = getGroupsLeaderOf();
                	Iterator i = groups.iterator();
                	GroupData group;
                	SecurityContext ctx;
                	List<GroupData> l = new ArrayList<GroupData>();
                	while (i.hasNext()) {
                		group = (GroupData) i.next();
						ctx = new SecurityContext(group.getId());
						l.addAll(svc.loadGroups(ctx, group.getId()));
						allgroups.remove(group);
					}
                	Collection all = new ArrayList();
                	all.addAll(l);
                	all.addAll(allgroups);
                	context.bind(LookupNames.USER_GROUP_DETAILS, all);
                	List agents = (List) context.lookup(LookupNames.AGENTS);
                	Iterator kk = agents.iterator();
                	AgentInfo agentInfo;
                	Registry reg;
                	while (kk.hasNext()) {
                	    agentInfo = (AgentInfo) kk.next();
                	    if (agentInfo.isActive()) {
                	        reg = agentInfo.getRegistry();
                	        reg.bind(LookupNames.USER_GROUP_DETAILS, all);
                	    }
                	}
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
                OmeroDataService ds = context.getDataService();
                Map<SecurityContext, Object> r = 
                        new HashMap<SecurityContext, Object>(nodes.size());
                long userID;
                Entry<SecurityContext, List> entry;
                Iterator<Entry<SecurityContext, List>>
                j = nodes.entrySet().iterator();
                List<?> l;
                Collection tags;
                Iterator k;
                Iterator<TagAnnotationData> i;
                TagAnnotationData tag, child;
                Map<Long, Collection<?>> values;
                SecurityContext ctx;
                Map<DataObject, Set<?>> mapForDataObject;
                while (j.hasNext()) {
                    entry = j.next();
                    ctx = entry.getKey();
                    userID = ctx.getExperimenter();
                    l = entry.getValue();

                    tags = os.loadTags(ctx, -1L, true, userID, ctx.getGroupID());
                    List<Object> tagResults = new ArrayList<Object>();
                    mapForDataObject = new HashMap<DataObject, Set<?>>();
                    if (CollectionUtils.isEmpty(l)) {
                        r.put(ctx, tags);
                    } else {
                        values = new HashMap<Long, Collection<?>>();
                        k = l.iterator();
                        Object ob;
                        TimeRefObject ref;
                        DataObject ho;
                        List<TagAnnotationData> refTags =
                                new ArrayList<TagAnnotationData>();
                        while (k.hasNext()) {
                            ob = k.next();
                            if (ob instanceof TagAnnotationData) {
                                tag = (TagAnnotationData) ob;
                                values.put(tag.getId(), os.loadTags(ctx,
                                        tag.getId(), false, userID,
                                        ctx.getGroupID()));
                            } else if (ob instanceof TimeRefObject) {
                                ref = (TimeRefObject) ob;
                                ref.setResults(os.loadFiles(ctx,
                                        ref.getFileType(), userID));
                                refTags.addAll(ref.getResults());
                                tagResults.add(ref);
                            } else if (ob instanceof DataObject) {
                                //retrieve the data for the data object
                                ho = (DataObject) ob;
                                mapForDataObject.put(ho,
                                    ds.loadContainerHierarchy(ctx,
                                    ob.getClass(), Arrays.asList(ho.getId()),
                                                true, userID));
                            }
                        }
                        handleTags(tags, tagResults, values, mapForDataObject);
                        if (refTags.size() > 0) {
                            handleTags(refTags, null, values, mapForDataObject);
                        }
                        r.put(ctx, tagResults);
                    }
                }
                results = r;
            }
        };
    }
    
    /**
     * Maps the tags to the correct objects.
     * 
     * @param tags The tags to handle.
     * @param tagResults The tags loading for smart folder.
     * @param values The value for the loaded objects.
     * @param mapForDataObject The loaded values.
     */
    private void handleTags(Collection<TagAnnotationData> tags,
            List<Object> tagResults, Map<Long, Collection<?>> values,
            Map<DataObject, Set<?>> mapForDataObject)
    {
        Iterator<TagAnnotationData> k = tags.iterator();
        TagAnnotationData tag, child;
        String ns;
        Set<TagAnnotationData> set;
        Iterator<TagAnnotationData> i;
        while (k.hasNext()) {
            tag = (TagAnnotationData) k.next();
            if (tagResults != null) tagResults.add(tag);
            ns = tag.getNameSpace();
            if (TagAnnotationData.INSIGHT_TAGSET_NS.equals(ns)) {
                set = tag.getTags();
                i = set.iterator();
                while (i.hasNext()) {
                    child = i.next();
                    if (values.containsKey(child.getId())) {
                        populateTag(child, values, mapForDataObject);
                    }
                }
            } else {
                if (values.containsKey(tag.getId())) {
                    populateTag(tag, values, mapForDataObject);
                }
            }
        }
    }
    
    /**
     * Populates the specified tag with the passed value.
     * 
     * @param tag The tag to populate
     * @param values Track the object.
     * @param mapForDataObject The values to set.
     */
    private void populateTag(TagAnnotationData tag,
            Map<Long, Collection<?>> values,
            Map<DataObject, Set<?>> mapForDataObject)
    {
        if (mapForDataObject.isEmpty()) {
            tag.setDataObjects((Set<DataObject>) values.get(tag.getId()));
        } else {
            Set<DataObject> objects = (Set<DataObject>) values.get(tag.getId());
            Set<DataObject> newList = new HashSet<DataObject>(objects.size());
            Iterator<DataObject> kk = objects.iterator();
            while (kk.hasNext()) {
                newList.add(getLoadedObject(mapForDataObject, kk.next()));
            }
            tag.setDataObjects(newList);
        }
    }
    
    /**
     * Checks the object has been reloaded.
     * 
     * @param map The list of reloaded object.
     * @param ho The data object of reference.
     */
    private DataObject getLoadedObject(Map<DataObject, Set<?>> map,
            DataObject ho)
    {
        Set<DataObject> sets = map.keySet();
        Iterator<DataObject> i = sets.iterator();
        DataObject object;
        Set<?> s;
        while (i.hasNext()) {
            object = i.next();
            if (object.getClass().equals(ho.getClass()) &&
                    object.getId() == ho.getId()) {
                s = map.get(object);
                return (DataObject) s.iterator().next();
            } else if (ho instanceof ProjectData) { //need to check the dataset
                ProjectData p = (ProjectData) ho;
                Set<DatasetData> datasets = p.getDatasets();
                Iterator<DatasetData> j = datasets.iterator();
                Set<DatasetData> loaded = new HashSet<DatasetData>();
                boolean modified = false;
                while (j.hasNext()) {
                    DataObject data = j.next();
                    if (object.getClass().equals(data.getClass()) &&
                            object.getId() == data.getId()) {
                        s = map.get(object);
                        loaded.add((DatasetData) s.iterator().next());
                        modified = true;
                    } else loaded.add((DatasetData) data);
                }
                if (modified) {
                    p.setDatasets(loaded);
                    return p;
                }
            }
        }
        return ho;
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
