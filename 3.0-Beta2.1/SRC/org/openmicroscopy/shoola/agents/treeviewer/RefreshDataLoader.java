/*
 * org.openmicroscopy.shoola.agents.treeviewer.RefreshDataLoader
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

package org.openmicroscopy.shoola.agents.treeviewer;


//Java imports
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.browser.Browser;
import org.openmicroscopy.shoola.env.data.views.CallHandle;

import pojos.CategoryData;
import pojos.CategoryGroupData;
import pojos.DataObject;
import pojos.DatasetData;
import pojos.ProjectData;

/** 
 * Refreshes the tree.
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
public class RefreshDataLoader
    extends DataBrowserLoader
{

    /** The type of the root node. */
    private Class       rootNodeType;
    
    /** 
     * Collection of expanded nodes. Only the nodes containing images
     * are in this list. 
     */
    private List        expandedNodes;
    
    /** Contains the expanded top container nodes ID. */
    private Map         expandedTopNodes;
    
    /** Handle to the async call so that we can cancel it. */
    private CallHandle  handle;
    
    /**
     * Controls if the passed class is supported.
     * 
     * @param klass The class to check.
     */
    private void checkClass(Class klass)
    {
        if (klass.equals(ProjectData.class) || 
                klass.equals(CategoryGroupData.class))
            return;
        throw new IllegalArgumentException("Root node not supported.");
    }
    
    /**
     * Creates a new instance. 
     * 
     * @param viewer        	The viewer this data loader is for.
     *                      	Mustn't be <code>null</code>.
     * @param rootNodeType  	The root node either <code>Project</code> or 
     *                      	<code>CategoryGroup</code>
     * @param expandedNodes 	The collection of expanded nodes containing 
     * 							images.
     * @param expandedTopNodes  The expanded top nodes IDs.
     */
    public RefreshDataLoader(Browser viewer, Class rootNodeType,
                            List expandedNodes, Map expandedTopNodes)
    {
        super(viewer);
        checkClass(rootNodeType);
        this.rootNodeType = rootNodeType;
        this.expandedNodes = expandedNodes;
        this.expandedTopNodes = expandedTopNodes;
    }
    
    /**
     * Retrieves the data.
     * @see DataBrowserLoader#load()
     */
    public void load()
    {
    	if (expandedNodes == null || expandedNodes.size() == 0)
            handle = dmView.loadContainerHierarchy(rootNodeType, null, false,
                    convertRootLevel(), viewer.getRootID(), this);
        else
            handle = dmView.refreshHierarchy(rootNodeType, expandedNodes, 
                    convertRootLevel(), viewer.getRootID(), this);
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
        Map<DataObject, Set> map;
        if (expandedNodes == null || expandedNodes.size() == 0) {
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
        viewer.setRefreshedHierarchy(map, expandedTopNodes);
    }
    
}
