/*
 * org.openmicroscopy.shoola.env.data.views.calls.DMLoader
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
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.data.OmeroPojoService;
import org.openmicroscopy.shoola.env.data.views.BatchCall;
import org.openmicroscopy.shoola.env.data.views.BatchCallTree;
import org.openmicroscopy.shoola.env.data.views.DataManagerView;
import pojos.CategoryData;
import pojos.CategoryGroupData;
import pojos.DatasetData;
import pojos.ProjectData;

/** 
 * Command to find the data trees of a given <i>OME</i> hierarchy type 
 * i.e. Project/Dataset/(Image), Dataset/(Image),
 * CategoryGroup/Category/(Image), Category/(Image).
 * Note that the images are not always retrieved depending on the specified
 * flag.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class DMLoader
    extends BatchCallTree
{

    /** The results of the call. */
    private Set         results;
    
    /** Loads the specified tree. */
    private BatchCall   loadCall;
    
    /**
     * Checks if the specified level is supported.
     * 
     * @param level The level to control.
     */
    private void checkRootLevel(int level)
    {
        switch (level) {
	        case DataManagerView.WORLD_HIERARCHY_ROOT:
	        case DataManagerView.GROUP_HIERARCHY_ROOT:
	        case DataManagerView.USER_HIERARCHY_ROOT:
	            return;
	        default:
	            throw new IllegalArgumentException("Root level not supported");
        }
    }
    
    /**
     * Converts the specified level its corresponding value defined by
     * {@link OmeroPojoService}.
     * 
     * @param level The level to convert.
     * @return See above.
     */
    private int convertRootLevel(int level)
    {
        switch (level) {
	        case DataManagerView.WORLD_HIERARCHY_ROOT:
	            return OmeroPojoService.WORLD_HIERARCHY_ROOT;
	        case DataManagerView.GROUP_HIERARCHY_ROOT:
	            return OmeroPojoService.GROUP_HIERARCHY_ROOT;
	        case DataManagerView.USER_HIERARCHY_ROOT:
	            return OmeroPojoService.USER_HIERARCHY_ROOT;
        }	
        return -1;
    }
    
    /**
     * Creates a {@link BatchCall} to retrieve a Container tree, either
     * Project, Dataset, CategoryGroup or Category.
     * 
     * @param rootNodeType  The type of the root node.
     * @param rootNodeIDs   A set of the IDs of top-most containers.
     * @param withLeaves    Passes <code>true</code> to retrieve the leaves
     *                      nodes, <code>false</code> otherwise.
     * @param rootLevel		The level of the hierarchy, one of the following
     * 						constants:
     * 						{@link DataManagerView#WORLD_HIERARCHY_ROOT},
     * 						{@link DataManagerView#GROUP_HIERARCHY_ROOT},
     * 						{@link DataManagerView#USER_HIERARCHY_ROOT}.
     * @param rootLevelID	The Id of the root. The value is set to <i>-1</i>
     * 						if the rootLevel is either 
     * 						{@link DataManagerView#WORLD_HIERARCHY_ROOT} or 
     * 						{@link DataManagerView#USER_HIERARCHY_ROOT}.
     * @return The {@link BatchCall}.
     */
    private BatchCall makeBatchCall(final Class rootNodeType,
                                    final Set rootNodeIDs,
                                    final boolean withLeaves,
                                    final int rootLevel, final int rootLevelID)
    {
        return new BatchCall("Loading container tree: ") {
            public void doCall() throws Exception
            {
                OmeroPojoService os = context.getOmeroService();
                results = os.loadContainerHierarchy(rootNodeType, rootNodeIDs, 
                        		withLeaves, convertRootLevel(rootLevel),
                        		rootLevelID);
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
     * @param rootNodeType  The type of the root node. Can only be one out of:
     *                      {@link ProjectData}, {@link DatasetData},
     *                      {@link CategoryGroupData} or {@link CategoryData}.
     * @param rootNodeIDs   A set of the IDs of top-most containers.
     * @param withLeaves    Passes <code>true</code> to retrieve the images.
     *                      <code>false</code> otherwise.
     * @param rootLevel		The level of the hierarchy, one of the following
     * 						constants:
     * 						{@link DataManagerView#WORLD_HIERARCHY_ROOT},
     * 						{@link DataManagerView#GROUP_HIERARCHY_ROOT},
     * 						{@link DataManagerView#USER_HIERARCHY_ROOT}.
     * @param rootLevelID	The Id of the root. The value is set to <i>-1</i>
     * 						if the rootLevel is either 
     * 						{@link DataManagerView#WORLD_HIERARCHY_ROOT} or 
     * 						{@link DataManagerView#USER_HIERARCHY_ROOT}.
     */
    public DMLoader(Class rootNodeType, Set rootNodeIDs, boolean withLeaves,
            		int rootLevel, int rootLevelID)
    {
        if (rootNodeType == null) 
            throw new IllegalArgumentException("No root node type.");
        checkRootLevel(rootLevel);
        if (rootNodeType.equals(ProjectData.class) ||
                rootNodeType.equals(DatasetData.class) ||
                rootNodeType.equals(CategoryGroupData.class) ||
                rootNodeType.equals(CategoryData.class))
                loadCall = makeBatchCall(rootNodeType, rootNodeIDs, withLeaves,
                        				rootLevel, rootLevelID);
            else
                throw new IllegalArgumentException("Unsupported type: "+
                                                    rootNodeType);
        
    }

}
