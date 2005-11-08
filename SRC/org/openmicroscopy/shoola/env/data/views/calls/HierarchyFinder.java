/*
 * org.openmicroscopy.shoola.env.data.views.calls.HierarchyFinder
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
import pojos.CategoryGroupData;
import pojos.ImageData;
import pojos.ProjectData;

/** 
 * Command to find the data trees of a given <i>OME</i> hierarchy type 
 * containing some given images.
 * <p>The hierarchy that will be searched can be either Project/Dataset/Image
 * (P/D/I) or Category Group/Category/Image (CG/C/I).  All root nodes in the
 * specified hierarchy will be loaded that have at least one of the given 
 * images among their leaves.  A node <code>n</code> is retrieved <i>only</i>
 * if there's a path among the root node and one of the specified images that
 * contains <code>n</code>.</p>
 * <p>The object returned in the <code>DSCallOutcomeEvent</code> will be a
 * <code>Set</code> with all root nodes that were found.  Every root node is
 * linked to the found objects and so on until the leaf nodes, which are the
 * <i>passed in</i> <code>ImageData</code>s.</p>
 * <p>The type of the returned objects are <code>ProjectData, 
 * DatasetData, ImageData</code> in the case of a P/D/I hierarchy,
 * as <code>CategoryGroupData, CategoryData, ImageData</code> for a CG/C/I
 * hierarchy.</p>
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
public class HierarchyFinder
    extends BatchCallTree
{
    
    /** The root nodes of the found trees. */
    private Set         rootNodes;
    
    /** Searches the specified hierarchy. */
    private BatchCall   findCall;
    
    
    /**
     * Creates a {@link BatchCall} to search the P/D/I, CG/C/I hierarchy.
     * 
     * @return The {@link BatchCall}.
     */
    private BatchCall makeCHBatchCall(
            final Class hierarchyRootNodeType, final Set images)
    {
        return new BatchCall("Searching Container hierarchy") {
            public void doCall() throws Exception
            {
                OmeroPojoService os = context.getOmeroService();
                rootNodes = os.findContainerHierarchy(hierarchyRootNodeType,
                                                    images);
            }
        };
    }
    
    /**
     * Adds the {@link #findCall} to the computation tree.
     * 
     * @see BatchCallTree#buildTree()
     */
    protected void buildTree() { add(findCall); }
    
    /**
     * Returns the root node of the found trees.
     * 
     * @see BatchCallTree#getResult()
     */
    protected Object getResult() { return rootNodes; }
    
    /**
     * Creates a new instance to search the specified hierarchy for trees
     * containing the specified images.
     * If bad arguments are passed, we throw a runtime exception so to fail
     * early and in the caller's thread.
     * 
     * @param hierarchyRootNodeType The type of the root node in the hierarchy
     *                     to search.  Can be one out of: 
     *                     {@link ProjectData} or {@link CategoryGroupData}.
     * @param images Contains {@link ImageData}s, one for each leaf image node.
     */
    public HierarchyFinder(Class hierarchyRootNodeType, Set images)
    {
        if (images == null) throw new IllegalArgumentException("No images.");
        if (hierarchyRootNodeType == null) 
            throw new NullPointerException("No root node type.");
        try {
            images.toArray(new ImageData[] {});
        } catch (ArrayStoreException ase) {
            throw new IllegalArgumentException(
                    "images can only contain ImageData objects.");
        }    
        if (hierarchyRootNodeType.equals(ProjectData.class) ||
            hierarchyRootNodeType.equals(CategoryGroupData.class))
            findCall = makeCHBatchCall(hierarchyRootNodeType, images);
        else
            throw new IllegalArgumentException("Unsupported type: "+
                                                hierarchyRootNodeType+".");
    }
    
}
