/*
 * org.openmicroscopy.shoola.env.data.views.calls.ExistingObjectsSaver
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
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.data.OmeroDataService;
import org.openmicroscopy.shoola.env.data.views.BatchCall;
import org.openmicroscopy.shoola.env.data.views.BatchCallTree;
import pojos.DataObject;
import pojos.DatasetData;
import pojos.ImageData;
import pojos.ProjectData;

/** 
 * Command to save existing <code>DataObject</code>s.
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
     * @param parent    The <code>DataObject</code>s to update. Either a 
     *                  <code>ProjectData</code> or <code>DatasetData</code>.
     * @param children  The items to add.
     * @return The {@link BatchCall}.
     */
    private BatchCall makeBatchCall(final Collection parent, 
    		final Collection children)
    {
        return new BatchCall("Loading container tree: ") {
            public void doCall() throws Exception
            {
                OmeroDataService os = context.getDataService();
                Iterator i = parent.iterator();
                Object obj;
                while (i.hasNext()) {
					obj = i.next();
					if (obj instanceof DataObject) {
						os.addExistingObjects((DataObject) obj, children);
					}
				}
                
                result = parent;
            }
        };
    }
    
    /**
     * Creates a {@link BatchCall} to add the given children to the 
     * specified parent.
     * 
     * @param toPaste   The <code>DataObjects</code> to update.
     * @return The {@link BatchCall}.
     */
    private BatchCall makeBatchCall(final Map toPaste)
    {
        return new BatchCall("Loading container tree: ") {
            public void doCall() throws Exception
            {
                OmeroDataService os = context.getDataService();
                Iterator i = toPaste.keySet().iterator();
                Object p;
                while (i.hasNext()) {
                    p = i.next();
                    if (p instanceof DataObject) {
                        os.addExistingObjects((DataObject) p, (Set)
                                toPaste.get(p));
                    }
                }
                result = toPaste;
            }
        };
    }
    
    /**
     * Creates a {@link BatchCall} to add the given children to the 
     * specified parent.
     * 
     * @param toPaste   The <code>DataObjects</code> to update.
     * @param toRemove  The <code>DataObjects</code> to remove.
     * @return The {@link BatchCall}.
     */
    private BatchCall makeBatchCall(final Map toPaste, final Map toRemove)
    {
        return new BatchCall("Loading container tree: ") {
            public void doCall() throws Exception
            {
                OmeroDataService os = context.getDataService();
                os.cutAndPaste(toPaste, toRemove);
                result = toPaste;
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
     * If bad arguments are passed, we throw a runtime
	 * exception so to fail early and in the caller's thread.
	 * 
     * @param parent    The <code>DataObject</code> to update. Either a 
     *                  <code>ProjectData</code> or <code>DatasetData</code>.
     * @param children  The items to add.
     */
    public ExistingObjectsSaver(Collection parent, Collection children)
    {
        if (children == null || children.size() == 0)
            throw new IllegalArgumentException("No item to add.");
        if (parent == null)
            throw new IllegalArgumentException("No parent to update.");
        /*
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
        } else
            throw new IllegalArgumentException("parent object not supported");
            */
        call = makeBatchCall(parent, children);
    }

    /**
     * Creates a new instance.
     * If bad arguments are passed, we throw a runtime
	 * exception so to fail early and in the caller's thread.
     * 
     * @param toPaste   The <code>DataObject</code>s to update. 
     * @param toRemove  The <code>DataObject</code>s to cut. 
     */
    public ExistingObjectsSaver(Map toPaste, Map toRemove)
    {
    	if (toPaste == null) toPaste = new HashMap();
        if (toRemove == null) call = makeBatchCall(toPaste);
        else call = makeBatchCall(toPaste, toRemove);
    }
    
}
