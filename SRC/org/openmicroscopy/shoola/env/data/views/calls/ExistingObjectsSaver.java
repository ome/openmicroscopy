/*
 * org.openmicroscopy.shoola.env.data.views.calls.ExistingObjectsSaver
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

import java.util.Set;

import org.openmicroscopy.shoola.env.data.OmeroService;
import org.openmicroscopy.shoola.env.data.views.BatchCall;
import org.openmicroscopy.shoola.env.data.views.BatchCallTree;

import pojos.CategoryData;
import pojos.CategoryGroupData;
import pojos.DataObject;
import pojos.DatasetData;
import pojos.ImageData;
import pojos.ProjectData;


//Java imports

//Third-party libraries

//Application-internal dependencies

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
public class ExistingObjectsSaver
    extends BatchCallTree
{

    /** Loads the specified tree. */
    private BatchCall   call;
    
    
    /** The result of the save action. */
    private Object      result;
    
    /**
     * Creates a {@link BatchCall} to add the given children to the 
     * specified parent.
     * 
     * @param parent    The <code>DataObject</code> to update. Either a 
     *                  <code>ProjectData</code> or <code>DatasetData</code>.
     * @param children  The items to add.
     * @return The {@link BatchCall}.
     */
    private BatchCall makeBatchCall(final DataObject parent, final Set children)
    {
        return new BatchCall("Loading container tree: ") {
            public void doCall() throws Exception
            {
                OmeroService os = context.getOmeroService();
                os.addExistingObjects(parent, children);
                result = parent;
            }
        };
    }
    
    /**
     * Adds the {@link #call} to the computation tree.
     * 
     * @see BatchCallTree#buildTree()
     */
    protected void buildTree() { add(call); }

    /**
     * Returns the found objects.
     * 
     * @see BatchCallTree#getResult()
     */
    protected Object getResult() { return result; }
    
    /**
     * Creates a new instance.
     * 
     * @param parent    The <code>DataObject</code> to update. Either a 
     *                  <code>ProjectData</code> or <code>DatasetData</code>.
     * @param children  The items to add.
     */
    public ExistingObjectsSaver(DataObject parent, Set children)
    {
        if (children == null || children.size() == 0)
            throw new IllegalArgumentException("No item to add.");
        if (parent == null)
            throw new IllegalArgumentException("No parent to update.");
        if (parent instanceof ProjectData) {
            try {
                children.toArray(new DatasetData[] {});
            } catch (ArrayStoreException ase) {
                throw new IllegalArgumentException(
                        "items can only be datasets.");
            }
        } else if (parent instanceof DatasetData) {
            try {
                children.toArray(new ImageData[] {});
            } catch (ArrayStoreException ase) {
                throw new IllegalArgumentException(
                        "items can only be images.");
            }
        } else if (parent instanceof CategoryGroupData) {
            try {
                children.toArray(new CategoryData[] {});
            } catch (ArrayStoreException ase) {
                throw new IllegalArgumentException(
                        "items can only be categories.");
            }
        } else
            throw new IllegalArgumentException("parent object not supported");
        call = makeBatchCall(parent, children);
    }
    
}
