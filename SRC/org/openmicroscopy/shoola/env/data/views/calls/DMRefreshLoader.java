/*
 * org.openmicroscopy.shoola.env.data.views.calls.DMRefreshLoader
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

package org.openmicroscopy.shoola.env.data.views.calls;


//Java imports
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.data.OmeroDataService;
import org.openmicroscopy.shoola.env.data.views.BatchCall;
import org.openmicroscopy.shoola.env.data.views.BatchCallTree;

import pojos.CategoryData;
import pojos.CategoryGroupData;
import pojos.DataObject;
import pojos.DatasetData;
import pojos.ExperimenterData;
import pojos.GroupData;
import pojos.ProjectData;

/** 
 * 
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
     * Checks if the specified level is supported.
     * 
     * @param level The level to control.
     */
    private void checkRootLevel(Class level)
    {
        if (level.equals(ExperimenterData.class) ||
                level.equals(GroupData.class)) return;
        throw new IllegalArgumentException("Root level not supported");
    }
    
    /**
     * Creates a {@link BatchCall} to retrieve a Container tree, either
     * Project or CategoryGroup.
     * 
     * @param rootNodeType          The type of the root node. Can only be one 
     *                              out of:
     *                              {@link ProjectData} or
     *                              {@link CategoryGroupData}.
     * @param containerWithImages   A set of container whose leaves are images
     *                              i.e. <code>Dataset</code> or 
     *                              <code>Category</code>.
     * @param rootLevel             The level of the hierarchy either 
     *                              <code>GroupData</code> or 
     *                              <code>ExperimenterData</code>.
     * @param rootLevelID           The Id of the root. 
     * @return The {@link BatchCall}.
     */
    private BatchCall makeBatchCall(final Class rootNodeType,
                                    final List containerWithImages,
                                    final Class rootLevel, 
                                    final long rootLevelID)
    {
        return new BatchCall("Loading container tree: ") {
            public void doCall() throws Exception
            {
                OmeroDataService os = context.getDataService();
                Set set = os.loadContainerHierarchy(rootNodeType, null, 
                        false, rootLevel, rootLevelID);

                Set ids;
                Iterator i = containerWithImages.iterator();
                ids = new HashSet(containerWithImages.size());
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
                        children = ((CategoryGroupData) parent).getCategories();
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
                                    true, rootLevel, rootLevelID);
                            k = r.iterator();
                            while (k.hasNext()) {
                                newChildren.add(k.next());
                            }
                        } else newChildren.add(child);
                    }
                    /*
                    if (parent instanceof ProjectData) {
                        ((ProjectData) parent).setDatasets(newChildren);
                    } else if (parent instanceof CategoryGroupData) {
                        ((CategoryGroupData) parent).setCategories(newChildren);
                    } 
                    */
                }
                //results = set;
                results = topNodes;
            }
        };
    }
    
    /**
     * Adds the {@link #loadCall} to the computation tree.
     * 
     * @see BatchCallTree#buildTree()
     */
    protected void buildTree() { add(loadCall); }

    /**
     * Returns the root node of the requested tree.
     * 
     * @see BatchCallTree#getResult()
     */
    protected Object getResult() { return results; }
    
    /**
     * Creates a new instance.
     * 
     * @param rootNodeType          The type of the root node. Can only be one 
     *                              out of:
     *                              {@link ProjectData} or
     *                              {@link CategoryGroupData}.
     * @param containerWithImages   A set of container whose leaves are images
     *                              i.e. <code>Dataset</code> or 
     *                              <code>Category</code>.
     * @param rootLevel             The level of the hierarchy either 
     *                              <code>GroupData</code> or 
     *                              <code>ExperimenterData</code>.
     * @param rootLevelID           The Id of the root. 
     */
    public DMRefreshLoader(Class rootNodeType, List containerWithImages,
                            Class rootLevel, long rootLevelID)
    {
        if (rootNodeType == null) 
            throw new IllegalArgumentException("No root node type.");
        checkRootLevel(rootLevel);
        if (rootLevelID < 0) 
            throw new IllegalArgumentException("No root ID not valid.");
        if (containerWithImages == null || containerWithImages.size() == 0)
            throw new IllegalArgumentException("No container with images.");
        if (rootNodeType.equals(ProjectData.class) ||
                rootNodeType.equals(CategoryGroupData.class))
                loadCall = makeBatchCall(rootNodeType, containerWithImages, 
                                        rootLevel, rootLevelID);
        else 
            throw new IllegalArgumentException("Unsupported type: "+
                                                rootNodeType);
    }
    
}
