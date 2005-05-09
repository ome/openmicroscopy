/*
 * org.openmicroscopy.shoola.env.data.views.calls.ClassificationLoader
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
 * Command to find the Category Group/Category paths that end or don't end with
 * a specified Image.
 * <p>This command can be created to load either all the Categories under which
 * a given Image was classified and all enclosing Category Groups or to do the
 * opposite &#151; to load all the Categories the given Image doesn't belong in,
 * and then all the Category Groups that contain those Categories.</p>
 * <p>The object returned in the <code>DSCallOutcomeEvent</code> will be a
 * <code>Set</code> with all Category Group nodes (as <code>CategoryGroupData
 * </code> objects) that were found.  Those objects will also be linked to the 
 * matching Categories (represented by <code>CategoryData</code> objects).</p>
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
public class ClassificationLoader
    extends BatchCallTree
{
    
    /** The root nodes of the found trees. */
    private Set         rootNodes;
    
    /** Searches the CG/C/I hierarchy. */
    private BatchCall   loadCall;
    
    
    /** 
     * Utility method to create a list with one <code>Integer</code> element.
     * 
     * @param imgID The Image id to add.
     * @return A list containing an <code>Integer</code> object to wrap
     *         <code>imgID</code>.
     */
    private List prepareList(int imgID)
    {
        ImageSummary is = new ImageSummary();
        is.setID(imgID);
        ArrayList list = new ArrayList(1);
        list.add(is);
        return list;
    }
    
    /**
     * Creates a {@link BatchCall} to load all Category Group/Category paths
     * that end with the specified Image.
     * 
     * @param id The Image id.
     * @return The {@link BatchCall}.
     */
    private BatchCall makeClassificationIn(final int id)
    {
        return new BatchCall("Loading classification tree for image: "+id) {
            public void doCall() throws Exception
            {
                SemanticTypesService sts = context.getSemanticTypesService();
                List nodesIn = sts.retrieveCGCIHierarchy(prepareList(id), true);
                rootNodes = new HashSet(nodesIn);
            }
        };
    }
    
    /**
     * Creates a {@link BatchCall} to load all Category Group/Category paths
     * that don't end with the specified Image.
     * 
     * @param id The Image id.
     * @return The {@link BatchCall}.
     */
    private BatchCall makeClassificationOut(final int id)
    {
        return new BatchCall("Loading classification tree for image: "+id) {
            public void doCall() throws Exception
            {
                SemanticTypesService sts = context.getSemanticTypesService();
                List nodesOut = sts.retrieveCGCIHierarchy(prepareList(id), 
                                false);
                rootNodes = new HashSet(nodesOut);
            }
        };
    }
    
    /**
     * Adds the {@link #loadCall} to the computation tree.
     * @see BatchCallTree#buildTree()
     */
    protected void buildTree() { add(loadCall); }

    /**
     * Returns, in a <code>Set</code>, the root nodes of the found trees.
     * @see BatchCallTree#getResult()
     */
    protected Object getResult() { return rootNodes; }

    /**
     * Creates a new instance to find the Category Group/Category paths that 
     * end or don't end with the specified Image.
     * If bad arguments are passed, we throw a runtime exception so to fail
     * early and in the caller's thread.
     * 
     * @param imageID   The id of the Image.
     * @param classified Whether to retrieve CG/C paths leading to the given
     *                   Image (<code>true</code>) or not leading to the given
     *                   Image (<code>false</code>).
     */
    public ClassificationLoader(int imageID, boolean classified)
    {
        if (imageID < 0) 
            throw new IllegalArgumentException("image ID not valid ");
        if (classified) loadCall = makeClassificationIn(imageID);
        else loadCall = makeClassificationOut(imageID);
    }
    
}
