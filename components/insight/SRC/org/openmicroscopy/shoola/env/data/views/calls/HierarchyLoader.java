/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
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

import java.util.List;
import java.util.Set;

import org.openmicroscopy.shoola.env.data.OmeroDataService;
import omero.gateway.SecurityContext;
import org.openmicroscopy.shoola.env.data.views.BatchCall;
import org.openmicroscopy.shoola.env.data.views.BatchCallTree;
import omero.gateway.model.DatasetData;
import omero.gateway.model.ProjectData;

/** 
 * Command to load a data hierarchy rooted by a given node.
 * <p>The root node can be one out of: Project, Dataset, Category Group, or
 * Category.  Image and Dataset items are retrieved with annotations.
 * The final <code>DSCallOutcomeEvent</code> will contain the requested node
 * as root and all of its descendants.</p>
 * <p>A Project tree will be represented by <code>ProjectData, 
 * DatasetData, and ImageData</code> objects. A Dataset tree
 * will only have objects of the latter two types.</p>
 * <p>So the object returned in the <code>DSCallOutcomeEvent</code> will be
 * a <code>ProjectData, DatasetData, ScreenData</code> depending on whether
 * you asked for a Project, Dataset, or Screen.</p>
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * @since OME2.2
 */
public class HierarchyLoader
    extends BatchCallTree
{
    
    /** The Id of the user. */
    private long        userID;
    
    /** The root nodes of the found trees. */
    private Set         rootNodes;
    
    /** Loads the specified tree. */
    private BatchCall   loadCall;
    
    /** The security context.*/
    private SecurityContext ctx;
    
    /**
     * Creates {@link BatchCall} if the type is supported.
     * 
     * @param rootNodeType  The type of the root node. Can only be one out of:
     *                      {@link ProjectData}, {@link DatasetData}.
     * @param rootNodeIDs   Collection of root node identifiers.
     */
    private void validate(Class rootNodeType, List<Long> rootNodeIDs)
    {
        if (rootNodeType == null) 
            throw new IllegalArgumentException("No root node type.");
        if (rootNodeIDs == null)
            throw new IllegalArgumentException("No root node ids.");
        if (rootNodeIDs.size() == 0)
            throw new IllegalArgumentException("No root node ids.");
        
        if (rootNodeType.equals(ProjectData.class) ||
            rootNodeType.equals(DatasetData.class))
            loadCall = makeBatchCall(rootNodeType, rootNodeIDs);
        else
            throw new IllegalArgumentException("Unsupported type: "+
                                                rootNodeType);
    }
    
    /**
     * Creates a {@link BatchCall} to retrieve a Container tree, either
     * Project, Dataset.
     * 
     * @param rootNodeType The type of the root node.
     * @param rootNodeIDs Collection of container's id. 
     * @return The {@link BatchCall}.
     */
    private BatchCall makeBatchCall(final Class rootNodeType, 
                                    final List rootNodeIDs)
    {
        return new BatchCall("Loading container tree: ") {
            public void doCall() throws Exception
            {
                OmeroDataService os = context.getDataService();
                rootNodes = os.loadContainerHierarchy(ctx, rootNodeType,
                              rootNodeIDs, true, userID);
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
    protected Object getResult() { return rootNodes; }
    
    /**
     * Creates a new instance to load a tree rooted by the object having the
     * specified type and id.
     * If bad arguments are passed, we throw a runtime exception so to fail
     * early and in the caller's thread.
     * 
     * @param ctx The security context.
     * @param rootNodeType  The type of the root node. Can only be one out of:
     *                      {@link ProjectData}, {@link DatasetData}.
     * @param rootNodeIDs   The identifiers of the root nodes.
     * @param userID   		The identifier of the user.
     */
    public HierarchyLoader(SecurityContext ctx,
    	Class rootNodeType, List<Long> rootNodeIDs, long userID)
    {
    	this.ctx = ctx;
    	this.userID = userID;
        validate(rootNodeType, rootNodeIDs);
    }
    
}
