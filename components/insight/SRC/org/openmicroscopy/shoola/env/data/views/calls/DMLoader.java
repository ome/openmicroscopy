/*
 * org.openmicroscopy.shoola.env.data.views.calls.DMLoader
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
import java.util.List;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.data.OmeroDataService;
import omero.gateway.SecurityContext;
import org.openmicroscopy.shoola.env.data.views.BatchCall;
import org.openmicroscopy.shoola.env.data.views.BatchCallTree;
import pojos.DatasetData;
import pojos.ProjectData;
import pojos.ScreenData;

/** 
 * Command to find the data trees of a given <i>OME</i> hierarchy type 
 * i.e. Project/Dataset/(Image), Dataset/(Image)..
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
    private Set results;
    
    /** Loads the specified tree. */
    private BatchCall loadCall;
    
    /** The security context.*/
    private SecurityContext ctx;
    
    /**
     * Creates a {@link BatchCall} to retrieve a Container tree, either
     * Project, Dataset.
     * 
     * @param rootNodeType  The type of the root node.
     * @param rootNodeIDs   A set of the IDs of top-most containers.
     * @param withLeaves    Passes <code>true</code> to retrieve the leaves
     *                      nodes, <code>false</code> otherwise.
     * @param userID		The identifier of the user.
     * @return The {@link BatchCall}.
     */
    private BatchCall makeBatchCall(final Class rootNodeType,
                                    final List rootNodeIDs,
                                    final boolean withLeaves,
                                    final long userID)
    {
        return new BatchCall("Loading container tree: ") {
            public void doCall() throws Exception
            {
                OmeroDataService os = context.getDataService();
                results = os.loadContainerHierarchy(ctx, rootNodeType, 
    					rootNodeIDs, withLeaves, userID);
            }
        };
    }
    
    /**
     * Creates a {@link BatchCall} to retrieve a Container tree.
     * 
     * @param userID The identifier of the user.
     * @return The {@link BatchCall}.
     */
    private BatchCall makeBatchCall(final long userID)
    {
        return new BatchCall("Loading container tree: ") {
            public void doCall() throws Exception
            {
                OmeroDataService os = context.getDataService();
                results = os.loadContainerHierarchy(ctx, ProjectData.class, 
                		null, false, userID);
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
     * @param ctx The security context.
     * @param rootNodeType  The type of the root node. Can only be one out of:
     *                      {@link ProjectData}, {@link DatasetData} or
     *                      {@link ScreenData}.
     * @param rootNodeIDs   A set of the IDs of top-most containers. Passed
     *                      <code>null</code> to retrieve all the top-most
     *                      container specified by the rootNodeType.
     * @param withLeaves    Passes <code>true</code> to retrieve the images.
     *                      <code>false</code> otherwise.
     * @param userID		The identifier of the user.
     */
    public DMLoader(SecurityContext ctx, Class rootNodeType,
    	List<Long> rootNodeIDs, boolean withLeaves, long userID)
    {
        this.ctx = ctx;
        if (rootNodeType == null) {
        	loadCall = makeBatchCall(userID);
        } else if (ProjectData.class.equals(rootNodeType) ||
        		DatasetData.class.equals(rootNodeType) 
                || ScreenData.class.equals(rootNodeType))
                loadCall = makeBatchCall(rootNodeType, rootNodeIDs, withLeaves,
                        				userID);
        else 
            throw new IllegalArgumentException("Unsupported type: "+
                                                rootNodeType);
    }

}
