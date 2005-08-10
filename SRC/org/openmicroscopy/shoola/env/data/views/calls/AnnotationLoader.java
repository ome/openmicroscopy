/*
 * org.openmicroscopy.shoola.env.data.views.calls.AnnotationLoader
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
import java.util.Map;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.data.SemanticTypesService;
import org.openmicroscopy.shoola.env.data.views.BatchCall;
import org.openmicroscopy.shoola.env.data.views.BatchCallTree;
import org.openmicroscopy.shoola.env.data.views.HierarchyBrowsingView;

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:a.falconi@dundee.ac.uk">
 *                  a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class AnnotationLoader
    extends BatchCallTree
{
    
    /** The set of retrieve annotations. */
    private Map         annotations;
    
    /** Loads the specified annotations. */
    private BatchCall   loadCall;
    
    /**
     * Creates a {@link BatchCall} to retrieve the dataset's annotation.
     * 
     * @param id The id of the dataset. 
     * @return The {@link BatchCall}.
     */
    private BatchCall makeDatasetBactchCall(final int id)
    {
        return new BatchCall("Loading dataset annotation: "+id) {
            public void doCall() throws Exception
            {
                SemanticTypesService sts = context.getSemanticTypesService();
                annotations = sts.getDatasetAnnotations(id);
            }
        };
    }
    
    /**
     * Creates a {@link BatchCall} to retrieve the image's annotation.
     * 
     * @param id The id of the image. 
     * @return The {@link BatchCall}.
     */
    private BatchCall makeImageBactchCall(final int id)
    {
        return new BatchCall("Loading image annotation: "+id) {
            public void doCall() throws Exception
            {
                SemanticTypesService sts = context.getSemanticTypesService();
                annotations = sts.getImageAnnotations(id);
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
     * Returns, in a <code>Set</code>, the root nodes of the found trees.
     * @see BatchCallTree#getResult()
     */
    protected Object getResult() { return annotations; }

    /**
     * Creates a new instance to load the annotations related to the specified
     * node. We retrieve either DatasetAnnotation or ImageAnnotaion according
     * to the specified class.
     * If bad arguments are passed, we throw a runtime exception so to fail
     * early and in the caller's thread.
     * 
     * @param nodeTypeID The id of the node type. One of the following constant:
     *                  {@link HierarchyBrowsingView#DATASET_ANNOTATION}
     *                  or {@link HierarchyBrowsingView#IMAGE_ANNOTATION}
     * @param nodeID        The id of the root node.
     */
    public AnnotationLoader(int nodeTypeID, int nodeID)
    {
        if (nodeTypeID == HierarchyBrowsingView.DATASET_ANNOTATION) 
            loadCall = makeDatasetBactchCall(nodeID);
        else if (nodeTypeID == HierarchyBrowsingView.IMAGE_ANNOTATION) 
            loadCall = makeImageBactchCall(nodeID);
        else throw new IllegalArgumentException("Unsupported type: "+
                                                nodeTypeID);
    }
    
}
