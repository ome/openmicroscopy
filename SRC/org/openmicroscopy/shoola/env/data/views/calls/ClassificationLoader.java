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
import java.util.HashSet;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.data.OmeroPojoService;
import org.openmicroscopy.shoola.env.data.views.BatchCall;
import org.openmicroscopy.shoola.env.data.views.BatchCallTree;
import org.openmicroscopy.shoola.env.data.views.HierarchyBrowsingView;

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
     * Checks if the index specified is supported.
     * 
     * @param i The passed index.
     * @return <code>true</code> it the index is supported.
     */
    private boolean checkAlgorithmIndex(int i)
    {
        switch (i) {
            case HierarchyBrowsingView.DECLASSIFICATION:
            case HierarchyBrowsingView.CLASSIFICATION_ME:
            case HierarchyBrowsingView.CLASSIFICATION_NME:    
                return true;
            default:
                return false;
        }
    }
    
    private int convertAlgo(int index)
    {
        switch (index) {
	        case HierarchyBrowsingView.DECLASSIFICATION:
	            return OmeroPojoService.DECLASSIFICATION;
	        case HierarchyBrowsingView.CLASSIFICATION_ME:
	            return OmeroPojoService.CLASSIFICATION_ME;
	        case HierarchyBrowsingView.CLASSIFICATION_NME:
	            return OmeroPojoService.CLASSIFICATION_NME;
	    }
        return 0;
    }
    
    /**
     * Creates a {@link BatchCall} to load all Category Group/Category paths
     * that don't end with the specified Image.
     * 
     * @param imageIDs The set of image ids.
     * @param algorithm  One of the following constants:
     *                  {@link HierarchyBrowsingView#DECLASSIFICATION},
     *                  {@link HierarchyBrowsingView#CLASSIFICATION_ME},
     *                  {@link HierarchyBrowsingView#CLASSIFICATION_NME}.
     * @return The {@link BatchCall}.
     */
    private BatchCall loadCGCPaths(final Set imageIDs, final int algorithm)
    {
        return new BatchCall("Loading CGC paths. ") {
            public void doCall() throws Exception
            {
                OmeroPojoService os = context.getOmeroService();
                rootNodes = os.findCGCPaths(imageIDs, convertAlgo(algorithm));
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
     * @param algorithm  One of the following constants:
     *                  {@link HierarchyBrowsingView#DECLASSIFICATION},
     *                  {@link HierarchyBrowsingView#CLASSIFICATION_ME},
     *                  {@link HierarchyBrowsingView#CLASSIFICATION_NME}.
     */
    public ClassificationLoader(int imageID, int algorithm)
    {
        if (imageID < 0) 
            throw new IllegalArgumentException("image ID not valid ");
        if (!checkAlgorithmIndex(algorithm))
            throw new IllegalArgumentException("Algorithm not supported.");
        Set set = new HashSet(1);
        set.add(new Integer(imageID));
        loadCall  = loadCGCPaths(set, algorithm);
    }
    
    /**
     * Creates a new instance to find the Category Group/Category paths that 
     * end or don't end with the specified Image.
     * If bad arguments are passed, we throw a runtime exception so to fail
     * early and in the caller's thread.
     * 
     * @param imageIDs   The collection of image's ids.
     * @param algorithm  One of the following constants:
     *                  {@link HierarchyBrowsingView#DECLASSIFICATION},
     *                  {@link HierarchyBrowsingView#CLASSIFICATION_ME},
     *                  {@link HierarchyBrowsingView#CLASSIFICATION_NME}.
     */
    public ClassificationLoader(Set imageIDs, int algorithm)
    {
        if (imageIDs == null || imageIDs.size() == 0) 
            throw new IllegalArgumentException("The collection of ids" +
                    "cannot be null or of size 0.");
        if (!checkAlgorithmIndex(algorithm))
            throw new IllegalArgumentException("Algorithm not supported.");
        loadCall  = loadCGCPaths(imageIDs, algorithm);
    }
    
}
