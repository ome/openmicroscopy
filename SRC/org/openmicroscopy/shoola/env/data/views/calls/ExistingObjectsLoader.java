/*
 * org.openmicroscopy.shoola.env.data.views.calls.ExistingObjectsLoader
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
import org.openmicroscopy.shoola.env.data.OmeroService;
import org.openmicroscopy.shoola.env.data.views.BatchCall;
import org.openmicroscopy.shoola.env.data.views.BatchCallTree;
import pojos.CategoryData;
import pojos.CategoryGroupData;
import pojos.DatasetData;
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
     * @param rootLevel The level of the hierarchy either 
     *                  <code>GroupData</code> or 
     *                  <code>ExperimenterData</code>.
     * @param rootID    The id of the root.
     * @return The {@link BatchCall}.
     */
    private BatchCall makeBatchCall(final Class nodeType, 
                                    final Set nodeIDs, final Class rootLevel,
                                    final long rootID)
    {
        return new BatchCall("Loading container tree: ") {
            public void doCall() throws Exception
            {
                OmeroService os = context.getOmeroService();
                nodes = os.loadExistingObjects(nodeType, nodeIDs, rootLevel, 
                                                rootID);
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
     * 
     * @param nodeType      The type of the root node. One out of the
     *                      following list: <code>ProjectData</code>, 
     *                      <code>DatasetData</code>, 
     *                      <code>CategoryData</code> or
     *                      <code>CategoryGroupData</code>.
     * @param nodeIDs       The set of node ids.
     * @param rootLevel     The level of the hierarchy either 
     *                      <code>GroupData</code> or 
     *                      <code>ExperimenterData</code>.
     * @param rootLevelID   The id of the root's level.
     */
    public ExistingObjectsLoader(Class nodeType, Set nodeIDs, 
            Class rootLevel, long rootLevelID)
    {
        if (!(nodeType.equals(ProjectData.class) || 
            nodeType.equals(DatasetData.class) || 
            nodeType.equals(CategoryData.class) || 
            nodeType.equals(CategoryGroupData.class))) 
            throw new IllegalArgumentException("DataObject not supported.");
        if (nodeIDs == null && nodeIDs.size() == 0)
            throw new IllegalArgumentException("No root node ids.");
        try {
            nodeIDs.toArray(new Long[] {});
        } catch (ArrayStoreException ase) {
            throw new IllegalArgumentException("nodeIDs only contain " +
                                                "Long.");
        }  
        loadCall = makeBatchCall(nodeType, nodeIDs, rootLevel, rootLevelID);
    }  
    
}
