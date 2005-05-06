/*
 * org.openmicroscopy.shoola.env.data.views.calls.ClassificationSaver
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
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.data.SemanticTypesService;
import org.openmicroscopy.shoola.env.data.model.CategoryData;
import org.openmicroscopy.shoola.env.data.views.BatchCall;
import org.openmicroscopy.shoola.env.data.views.BatchCallTree;

/** 
 * 
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
public class ClassificationSaver
    extends BatchCallTree
{

    private Set             imgIDs;
    
    private CategoryData    data;
    
    /** Searches the specified hierarchy. */
    private BatchCall       saveCall;
    
    /**
     * Classifies the specified images into the Category.
     * 
     * @return The {@link BatchCall}.
     */
    private BatchCall classify()
    {
        return new BatchCall("Saving classification tree.") {
            public void doCall() throws Exception
            {
                SemanticTypesService sts = context.getSemanticTypesService();
                ArrayList list = new ArrayList(imgIDs);
                sts.updateCategory(data, null, list);
            }
        };
    }
    
    /**
     * Delassifies the specified images from the Category.
     * 
     * @return The {@link BatchCall}.
     */
    private BatchCall declassify()
    {
        return new BatchCall("Declassifying images.") {
            public void doCall() throws Exception
            {
                SemanticTypesService sts = context.getSemanticTypesService();
                ArrayList list = new ArrayList(imgIDs);
                sts.updateCategory(data, list, null);
            }
        };
    }
    
    /**
     * Adds the {@link #findCall} to the computation tree.
     * 
     * @see BatchCallTree#buildTree()
     */
    protected void buildTree() { add(saveCall); }

    /**
     * Returns the root node of the found trees.
     * 
     * @see BatchCallTree#getResult()
     */
    protected Object getResult() { return Boolean.TRUE; }

    public ClassificationSaver(CategoryData data, Set imgIDs, boolean classify)
    {
        if (data == null) 
            throw new NullPointerException("No category.");
        if (imgIDs == null)
            throw new NullPointerException("No image ids.");
        try {
            imgIDs.toArray(new Integer[] {});
        } catch (ArrayStoreException ase) {
            throw new IllegalArgumentException(
                    "imgIDs can only contain Integer objects.");
        }
        this.data = data;
        this.imgIDs = imgIDs;
        if (classify) saveCall = classify();
        else saveCall = declassify();
    }
    
}
