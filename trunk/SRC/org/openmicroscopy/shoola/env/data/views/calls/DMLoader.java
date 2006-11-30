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
import org.openmicroscopy.shoola.env.data.OmeroDataService;
import org.openmicroscopy.shoola.env.data.views.BatchCall;
import org.openmicroscopy.shoola.env.data.views.BatchCallTree;
import pojos.CategoryData;
import pojos.CategoryGroupData;
import pojos.DatasetData;
import pojos.ExperimenterData;
import pojos.GroupData;
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
    private void checkRootLevel(Class level)
    {
        if (level.equals(ExperimenterData.class) ||
                level.equals(GroupData.class)) return;
	    throw new IllegalArgumentException("Root level not supported");
    }
    
    /**
     * Creates a {@link BatchCall} to retrieve a Container tree, either
     * Project, Dataset, CategoryGroup or Category.
     * 
     * @param rootNodeType  The type of the root node.
     * @param rootNodeIDs   A set of the IDs of top-most containers.
     * @param withLeaves    Passes <code>true</code> to retrieve the leaves
     *                      nodes, <code>false</code> otherwise.
     * @param rootLevel		The level of the hierarchy either 
     *                      <code>GroupData</code> or 
     *                      <code>ExperimenterData</code>.
     * @param rootLevelID	The Id of the root.
     * @return The {@link BatchCall}.
     */
    private BatchCall makeBatchCall(final Class rootNodeType,
                                    final Set rootNodeIDs,
                                    final boolean withLeaves,
                                    final Class rootLevel, 
                                    final long rootLevelID)
    {
        return new BatchCall("Loading container tree: ") {
            public void doCall() throws Exception
            {
                OmeroDataService os = context.getDataService();
                results = os.loadContainerHierarchy(rootNodeType, rootNodeIDs, 
                        		withLeaves, rootLevel, rootLevelID);
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
     * @param rootNodeIDs   A set of the IDs of top-most containers. Passed
     *                      <code>null</code> to retrieve all the top-most
     *                      container specified by the rootNodeType.
     * @param withLeaves    Passes <code>true</code> to retrieve the images.
     *                      <code>false</code> otherwise.
     * @param rootLevel		The level of the hierarchy either 
     *                      <code>GroupData</code> or 
     *                      <code>ExperimenterData</code>.
     * @param rootLevelID	The Id of the root. 
     */
    public DMLoader(Class rootNodeType, Set rootNodeIDs, boolean withLeaves,
                    Class rootLevel, long rootLevelID)
    {
        if (rootNodeType == null) 
            throw new IllegalArgumentException("No root node type.");
        checkRootLevel(rootLevel);
        if (rootLevelID < 0) 
            throw new IllegalArgumentException("No root ID not valid.");
        try {
            if (rootNodeIDs != null) rootNodeIDs.toArray(new Long[] {});
        } catch (ArrayStoreException ase) {
            throw new IllegalArgumentException("rootNodeIDs only contains " +
                                                "Long.");
        }  
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
