/*
 * org.openmicroscopy.shoola.env.data.views.calls.ClassifierLoader
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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.data.SemanticTypesService;
import org.openmicroscopy.shoola.env.data.model.ImageSummary;
import org.openmicroscopy.shoola.env.data.views.BatchCall;
import org.openmicroscopy.shoola.env.data.views.BatchCallTree;

/** 
 * Command to find the data trees of a given <i>OME</i> hierarchy type 
 * containing some given images.
 * <p>The hierarchy that will be searched is Category Group/Category/Image 
 * (CG/C/I). All root nodes in the
 * specified hierarchy will be loaded that have at least one of the given 
 * images among their leaves. A node <code>n</code> is retrieved <i>only</i>
 * if there's a path among the root node and one of the specified images that
 * contains <code>n</code>.</p>
 * <p>The object returned in the <code>DSCallOutcomeEvent</code> will be a
 * <code>Set</code> with all root nodes that were found. Every root node is
 * linked to the found objects and so on until the leaf nodes, which are the
 * <i>passed in</i> <code>ImageSummary</code>s.</p>
 * <p>The type of the returned objects are <code>CategoryGroupData, 
 * CategoryData, ImageSummary</code> for a CG/C/I hierarchy.</p>
 * Two types of hierarchy are retrieved. The <code>in</code> type corresponds
 * to the hierarchy the image is classified into, the <code>out</code> type
 * otherwise.
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
public class ClassifierLoader
    extends BatchCallTree
{
    
    /** The root nodes of the found trees. */
    private Set         inRootNodes, outRootNodes;
    
    /** Searches the specified hierarchy. */
    private BatchCall   inCall, outCall;
    
    /**
     * Creates a {@link BatchCall} to search the CG/C hierarchy.
     * This call only retrieves the Categories which contain the specified
     * <code>ImageSummary</code>.
     * @return The {@link BatchCall}.
     */
    private BatchCall makeClassificationIn(final ImageSummary imgSummary)
    {
        return new BatchCall("Loading classification tree for: "
                            +imgSummary.getID()) {
            public void doCall() throws Exception
            {
                SemanticTypesService sts = context.getSemanticTypesService();
                ArrayList list = new ArrayList(1);
                list.add(imgSummary);
                List nodesIn = sts.retrieveCGCIHierarchy(list, true);
                inRootNodes = new HashSet(nodesIn);
            }
        };
    }
    
    /**
     * Creates a {@link BatchCall} to search the CG/C hierarchy.
     * This call only retrieves the Categories which don't contain the specified
     * <code>ImageSummary</code>.
     * @return The {@link BatchCall}.
     */
    private BatchCall makeClassificationOut(final ImageSummary imgSummary)
    {
        return new BatchCall("Loading classification tree for: "
                            +imgSummary.getID()) {
            public void doCall() throws Exception
            {
                SemanticTypesService sts = context.getSemanticTypesService();
                ArrayList list = new ArrayList(1);
                list.add(imgSummary);
                List nodesOut = sts.retrieveCGCIHierarchy(list, false);
                outRootNodes = new HashSet(nodesOut);
            }
        };
    }
    
    /**
     * Adds the {@link #inCall} and {@link #outCall} to the computation tree.
     * 
     * @see BatchCallTree#buildTree()
     */
    protected void buildTree()
    {
        add(inCall);
        add(outCall);
    }

    /**
     * Returns a 2-dimensional array of Sets.
     * The first element is the root node of the <code>in</code> found trees.
     * The second element is the root node of the <code>out</code> found trees.
     * 
     * @see BatchCallTree#getResult()
     */
    protected Object getResult()
    {
        Object[] array = new Object[2];
        array[0] = inRootNodes;
        array[1] = outRootNodes;
        return array;
    }

    /**
     * Creates a new instance to search the specified classification hierarchy 
     * for trees containing the specified image.
     * If bad arguments are passed, we throw a runtime exception so to fail
     * early and in the caller's thread.
     * 
     * @param imageID  .
     */
    public ClassifierLoader(int imageID)
    {
        if (imageID < 0) 
            throw new IllegalArgumentException("image ID not valid ");
        ImageSummary is = new ImageSummary();
        is.setID(imageID);
        inCall = makeClassificationIn(is);
        outCall = makeClassificationOut(is);
    }
    
}
