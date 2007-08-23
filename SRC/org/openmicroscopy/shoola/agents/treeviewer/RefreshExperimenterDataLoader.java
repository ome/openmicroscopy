/*
 * org.openmicroscopy.shoola.agents.treeviewer.RefreshExperimenterDataLoader 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.treeviewer;


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
import org.openmicroscopy.shoola.agents.treeviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.treeviewer.browser.TreeImageTimeSet;
import org.openmicroscopy.shoola.env.data.model.TimeRefObject;
import org.openmicroscopy.shoola.env.data.views.CallHandle;
import pojos.CategoryData;
import pojos.CategoryGroupData;
import pojos.DataObject;
import pojos.DatasetData;
import pojos.ImageData;
import pojos.ProjectData;

/** 
 * Reloads the data for the specified experimenters.
 * This class calls the <code>refreshHierarchy</code> in the
 * <code>DataManagerView</code>.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class RefreshExperimenterDataLoader
	extends DataBrowserLoader
{
	
    /** The type of the root node. */
    private Class								rootNodeType;
    
    /** Collection of {@link RefreshExperimenterDef} objects. */
    private Map<Long, RefreshExperimenterDef>	expNodes;
    
    /** Handle to the async call so that we can cancel it. */
    private CallHandle  						handle;
    
    /**
     * Controls if the passed class is supported.
     * 
     * @param klass The class to check.
     */
    private void checkClass(Class klass)
    {
        if (ProjectData.class.equals(klass) || ImageData.class.equals(klass) ||
        	CategoryGroupData.class.equals(klass))
            return;
        throw new IllegalArgumentException("Root node not supported.");
    }
    
    /**
     * Formats the results.
     * 
     * @param expId		The user's id.
     * @param result	The result of the call for the passed user.
     */
    private void setExperimenterResult(long expId, Object result)
    {
    	RefreshExperimenterDef node = expNodes.get(expId);
    	Map<DataObject, Set> map;
    	Map expandedNodes = node.getExpandedTopNodes();
        if (expandedNodes == null || expandedNodes.size() == 0 
        	|| result instanceof Set) {
        	Set set = (Set) result;
            Iterator j = set.iterator();
            map = new HashMap<DataObject, Set>();
            DataObject parent;
            Set children = null;
            while (j.hasNext()) {
                parent = (DataObject) j.next();
                if (parent instanceof ProjectData) {
                    children = ((ProjectData) parent).getDatasets();
                } else if (parent instanceof CategoryGroupData) {
                    children = ((CategoryGroupData) parent).getCategories();
                } else if (parent instanceof DatasetData) {
                	children = new HashSet(1);
                	children.add(parent);
                } else if (parent instanceof CategoryData) {
                	children = new HashSet(1);
                	children.add(parent);
                }
                map.put(parent, children);
            }
        } else map = (Map) result;
        node.setResults(map);
    }
    
    /**
     * Formats the results.
     * 
     * @param expId		The user's id.
     * @param result	The result of the call for the passed user.
     */
    private void formatImageResult(long expId, List result)
    {
    	RefreshExperimenterDef node = expNodes.get(expId);
    	List nodes = node.getExpandedNodes();
    	int n = nodes.size();
    	TreeImageTimeSet display;
    	TimeRefObject ref;
    	Map m = new HashMap();
    	
    	for (int i = 0; i < n; i++) {
			display = (TreeImageTimeSet) nodes.get(i);
			ref = (TimeRefObject) result.get(i);
			m.put(display, ref.getResults());
		}
    	node.setResults(m);
    }
    
    /**
     * Creates a new instance. 
     * 
     * @param viewer        	The viewer this data loader is for.
     *                      	Mustn't be <code>null</code>.
     * @param rootNodeType  	The root node either <code>Project</code> or 
     *                      	<code>CategoryGroup</code>
     * @param expNodes			Collection of nodes hosting information about
     * 							the nodes to refresh.
     * 							Mustn't be <code>null</code>.
     */
    public RefreshExperimenterDataLoader(Browser viewer, 
    			Class rootNodeType, Map<Long, RefreshExperimenterDef> expNodes)
    {
        super(viewer);
        if (expNodes == null || expNodes.size() == 0)
        	throw new IllegalArgumentException("Experimenter nodes not valid.");
        checkClass(rootNodeType);
        this.rootNodeType = rootNodeType;
        this.expNodes = expNodes;
    }
    
    /**
     * Retrieves the data.
     * @see DataBrowserLoader#load()
     */
    public void load()
    {
    	Iterator i = expNodes.keySet().iterator();
    	RefreshExperimenterDef def;
    	long userID;
    	TimeRefObject ref;
    	List nodes;
    	List<TimeRefObject> times;
    	Iterator j;
    	TreeImageTimeSet node;
    	Map<Long, List> m = new HashMap<Long, List>(expNodes.size());
    	if (ImageData.class.equals(rootNodeType)) {
    		while (i.hasNext()) {
        		userID = (Long) i.next();
        		def = expNodes.get(userID);
        		nodes = def.getExpandedNodes();
        		j = nodes.iterator();
        		times = new ArrayList<TimeRefObject>(nodes.size());
        		while (j.hasNext()) {
        			node = (TreeImageTimeSet) j.next();
					ref = new TimeRefObject(userID, node.getLowerTime(),
							node.getTime(), 
							getTimeConstrain(node.getIndex()));
					times.add(ref);
				}
    			m.put(userID, times);
    		}
    	} else {
        	while (i.hasNext()) {
        		userID = (Long) i.next();
        		def = expNodes.get(userID);
    			m.put(userID, def.getExpandedNodes());
    		}
    	}
    	handle = dmView.refreshHierarchy(rootNodeType, m, this);
    }

    /**
     * Cancels the data loading.
     * @see DataBrowserLoader#cancel()
     */
    public void cancel() { handle.cancel(); }

    /**
     * Feeds the result back to the viewer.
     * @see DataBrowserLoader#handleResult(Object)
     */
    public void handleResult(Object result)
    {
        if (viewer.getState() == Browser.DISCARDED) return;  //Async cancel.
        Map m = (Map) result;
        Iterator i = m.keySet().iterator();
        long expId;
        if (ImageData.class.equals(rootNodeType)) {
        	while (i.hasNext()) {
            	expId = (Long) i.next();
            	formatImageResult(expId, (List) m.get(expId));
    		}
        } else {
        	while (i.hasNext()) {
            	expId = (Long) i.next();
            	setExperimenterResult(expId, m.get(expId));
    		}
        }
        
        viewer.setRefreshExperimenterData(expNodes);
    }

}
