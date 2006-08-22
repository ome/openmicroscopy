/*
 * org.openmicroscopy.shoola.env.data.views.calls.ImagesLoader
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
import pojos.ExperimenterData;
import pojos.GroupData;


/** 
 * Command to retrieve the images contained in the specified containers.
 * The containers can either be <code>Dataset</code> or <code>Category</code>.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class ImagesLoader
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
     * Creates a {@link BatchCall} to retrieve the user images.
     * 
     * @return The {@link BatchCall}.
     */
    private BatchCall makeBatchCall()
    {
        return new BatchCall("Loading container tree: ") {
            public void doCall() throws Exception
            {
                OmeroService os = context.getOmeroService();
                results = os.getUserImages();
            }
        };
    }
    
    /**
     * Creates a {@link BatchCall} to retrieve the images contained 
     * in the container specified by the set of IDs.
     * 
     * @param nodeType  	The type of the node.
     * @param nodeIDs   	A set of the IDs of top-most containers.
     * @param rootLevel		The level of the hierarchy either 
     *                      <code>GroupData</code> or 
     *                      <code>ExperimenterData</code>.
     * @param rootLevelID	The Id of the root.
     * @return The {@link BatchCall}.
     */
    private BatchCall makeImagesInContainerBatchCall(final Class nodeType,
                                        			final Set nodeIDs,
                                        			final Class rootLevel,
                                        			final long rootLevelID)
    {
        return new BatchCall("Loading container tree: ") {
            public void doCall() throws Exception
            {
                OmeroService os = context.getOmeroService();
                results = os.getImages(nodeType, nodeIDs, rootLevel, 
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
    
    /** Creates a new instance. */
    public ImagesLoader()
    {
        loadCall = makeBatchCall();
    }

    /**
     * Creates a new instance.
     * 
     * @param nodeType		The type of the root node. Can only be one out of:
     * 						{@link DatasetData} or {@link CategoryGroupData}.
     * @param nodeIDs		A set of the IDs of top-most containers.
     * @param rootLevel		The level of the hierarchy either 
     *                      <code>GroupData</code> or 
     *                      <code>ExperimenterData</code>.
     * @param rootLevelID	The Id of the root.
     */
    public ImagesLoader(Class nodeType, Set nodeIDs, Class rootLevel,
            			long rootLevelID)
    {
        if (nodeType == null) 
            throw new IllegalArgumentException("No node type.");
        if (nodeIDs == null || nodeIDs.size() == 0)
            throw new IllegalArgumentException("Collection of node ID" +
                                                " not valid.");
        try {
            nodeIDs.toArray(new Long[] {});
        } catch (ArrayStoreException ase) {
            throw new IllegalArgumentException("nodeIDs only contains Long.");
        }  
        checkRootLevel(rootLevel);
        if (nodeType.equals(DatasetData.class) || 
            nodeType.equals(CategoryData.class))
            loadCall = makeImagesInContainerBatchCall(nodeType, nodeIDs, 
                    								rootLevel, rootLevelID);
        else throw new IllegalArgumentException("Unsupported type: "+
                nodeType);
    }
    
}
