/*
 * org.openmicroscopy.shoola.env.data.views.calls.ExistingObjectsLoader
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
import org.openmicroscopy.shoola.env.data.views.BatchCall;
import org.openmicroscopy.shoola.env.data.views.BatchCallTree;
import pojos.CategoryData;
import pojos.CategoryGroupData;
import pojos.DatasetData;
import pojos.ProjectData;

/** 
 * Command to retrieve existing <code>DataObject</code>s.
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
public class ExistingObjectsLoader
    extends BatchCallTree
{

    /** The nodes of the existing objects. */
    private Set         nodes;
    
    /** Loads the specified tree. */
    private BatchCall   loadCall;
    
    /**
     * Creates a {@link BatchCall} to retrieve the existing objects not
     * contained in the given containers.
     * 
     * @param nodeType  The type of the root node.
     * @param nodeIDs   Collection of container's id. 
     * @param userID    The id of the user.
     * @return The {@link BatchCall}.
     */
    private BatchCall makeBatchCall(final Class nodeType, final List nodeIDs, 
    								final long userID)
    {
        return new BatchCall("Loading container tree: ") {
            public void doCall() throws Exception
            {
                OmeroDataService os = context.getDataService();
                nodes = os.loadExistingObjects(nodeType, nodeIDs, userID);
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
     * Returns the found objects.
     * 
     * @see BatchCallTree#getResult()
     */
    protected Object getResult() { return nodes; }
    
    /**
     * Creates a new instance.
     * If bad arguments are passed, we throw a runtime
	 * exception so to fail early and in the caller's thread.
     * 
     * @param nodeType	The type of the root node. One out of the
     *                  following list: <code>ProjectData</code>, 
     *                  <code>DatasetData</code>, <code>CategoryData</code> or
     *                  <code>CategoryGroupData</code>.
     * @param nodeIDs 	The set of node ids.
     * @param userID   	The id of the root's level.
     */
    public ExistingObjectsLoader(Class nodeType, List nodeIDs, long userID)
    {
        if (!(nodeType.equals(ProjectData.class) || 
            nodeType.equals(DatasetData.class) || 
            nodeType.equals(CategoryData.class) || 
            nodeType.equals(CategoryGroupData.class))) 
            throw new IllegalArgumentException("DataObject not supported.");
        if (nodeIDs == null && nodeIDs.size() == 0)
            throw new IllegalArgumentException("No root node ids.");
        loadCall = makeBatchCall(nodeType, nodeIDs, userID);
    }  
    
}
