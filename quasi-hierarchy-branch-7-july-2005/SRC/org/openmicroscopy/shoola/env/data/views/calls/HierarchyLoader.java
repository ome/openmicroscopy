/*
 * org.openmicroscopy.shoola.env.data.views.calls.HierarchyLoader
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

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.data.DataManagementService;
import org.openmicroscopy.shoola.env.data.SemanticTypesService;
import org.openmicroscopy.shoola.env.data.model.CategoryData;
import org.openmicroscopy.shoola.env.data.model.CategoryGroupData;
import org.openmicroscopy.shoola.env.data.model.DatasetSummaryLinked;
import org.openmicroscopy.shoola.env.data.model.ProjectSummary;
import org.openmicroscopy.shoola.env.data.views.BatchCall;
import org.openmicroscopy.shoola.env.data.views.BatchCallTree;

/** 
 * Command to load a data hierarchy rooted by a given node.
 * <p>The root node can be one out of: Project, Dataset, Category Group, or
 * Category.  Image and Dataset items are retrieved with annotations.
 * The final <code>DSCallOutcomeEvent</code> will contain the requested node
 * as root and all of its descendants.</p>
 * <p>A Project tree will be represented by <code>ProjectSummary, 
 * DatasetSummaryLinked, and ImageSummary</code> objects.  A Dataset tree
 * will only have objects of the latter two types.</p>
 * <p>A Category Group tree will be represented by <code>CategoryGroupData, 
 * CategoryData</code>, and <code>ImageSummary</code> objects.  A Category 
 * tree will only have objects of the latter two types.</p>
 * <p>So the object returned in the <code>DSCallOutcomeEvent</code> will be
 * a <code>ProjectSummary, DatasetSummaryLinked, CategoryGroupData</code> or
 * <code>CategoryData</code> depending on whether you asked for a Project, 
 * Dataset, Category Group, or Category tree.</p>
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
public class HierarchyLoader
    extends BatchCallTree
{

    /** The returned value. */
    private Object      rootNode;
    
    /** Loads the specified tree. */
    private BatchCall   loadCall;
    
    
    /**
     * Creates a {@link BatchCall} to retrieve a Project tree.
     * 
     * @param id The id of the root Project node. 
     * @return The {@link BatchCall}.
     */
    private BatchCall makeProjectBatchCall(final int id)
    {
        return new BatchCall("Loading project tree: "+id) {
            public void doCall() throws Exception
            {
                DataManagementService dms = context.getDataManagementService();
                rootNode = dms.retrieveProjectTree(id, true);
            }
        };
    }
    
    /**
     * Creates a {@link BatchCall} to retrieve a Dataset tree.
     * 
     * @param id The id of the root Dataset node. 
     * @return The {@link BatchCall}.
     */
    private BatchCall makeDatasetBatchCall(final int id)
    {
        return new BatchCall("Loading dataset tree: "+id) {
            public void doCall() throws Exception
            {
                DataManagementService dms = context.getDataManagementService();
                rootNode = dms.retrieveDatasetTree(id, true);
            }
        };
    }
    
    /**
     * Creates a {@link BatchCall} to retrieve a Category Group tree.
     * 
     * @param id The id of the root Category Group node. 
     * @return The {@link BatchCall}.
     */
    private BatchCall makeCategoryGroupBatchCall(final int id)
    {
        return new BatchCall("Loading category group tree: "+id) {
            public void doCall() throws Exception
            {
                SemanticTypesService sts = context.getSemanticTypesService();
                rootNode = sts.retrieveCategoryGroupTree(id, true);
            }
        };
    }
    
    /**
     * Creates a {@link BatchCall} to retrieve a Category tree.
     * 
     * @param id The id of the root Category node. 
     * @return The {@link BatchCall}.
     */
    private BatchCall makeCategoryBatchCall(final int id)
    {
        return new BatchCall("Loading category tree: "+id) {
            public void doCall() throws Exception
            {
                SemanticTypesService sts = context.getSemanticTypesService();
                rootNode = sts.retrieveCategoryTree(id, true);
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
    protected Object getResult() { return rootNode; }
    
    /**
     * Creates a new instance to load a tree rooted by the object having the
     * specified type and id.
     * If bad arguments are passed, we throw a runtime exception so to fail
     * early and in the caller's thread.
     * 
     * @param rootNodeType  The type of the root node.  Can only be one out of:
     *                      <code>ProjectSummary, DatasetSummaryLinked, 
     *                      CategoryGroupData, CategoryData</code>.
     * @param nodeID        The id of the root node.
     */
    public HierarchyLoader(Class rootNodeType, int nodeID)
    {
        if (rootNodeType == null) 
            throw new NullPointerException("No root node type.");
        if (rootNodeType.equals(ProjectSummary.class))
            loadCall = makeProjectBatchCall(nodeID);
        else if (rootNodeType.equals(DatasetSummaryLinked.class))
            loadCall = makeDatasetBatchCall(nodeID);
        else if (rootNodeType.equals(CategoryGroupData.class))
            loadCall = makeCategoryGroupBatchCall(nodeID);
        else if (rootNodeType.equals(CategoryData.class))
            loadCall = makeCategoryBatchCall(nodeID);
        else
            throw new IllegalArgumentException("Unsupported type: "+
                                                rootNodeType);
    }
    
}
