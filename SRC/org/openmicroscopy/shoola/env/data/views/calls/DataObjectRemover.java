/*
 * org.openmicroscopy.shoola.env.data.views.calls.DataObjectRemover 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

//Third-party libraries

//Application-internal dependencies

import org.openmicroscopy.shoola.env.data.OmeroDataService;
import org.openmicroscopy.shoola.env.data.model.DeletableObject;
import org.openmicroscopy.shoola.env.data.views.BatchCall;
import org.openmicroscopy.shoola.env.data.views.BatchCallTree;

/** 
 * Command to delete the passed objects.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class DataObjectRemover
	extends BatchCallTree
{

	 /** The call. */
    private BatchCall       call;
    
    /** The result of the call. */
    private Object          result;
    
    /**
     * Creates a {@link BatchCall} to delete the specified objects.
     * 
     * @param values   The objects to delete.
     * @return The {@link BatchCall}.
     */
    private BatchCall makeDeleteCall(final Collection<DeletableObject> values)
    {
        return new BatchCall("Delete the object.") {
            public void doCall() throws Exception
            {
            	OmeroDataService os = context.getDataService();
            	result = os.delete(values);
            }
        };
    }
    
    /**
     * Adds the {@link #call} to the computation tree.
     * @see BatchCallTree#buildTree()
     */
    protected void buildTree() { add(call); }

    /**
     * Returns the saved <code>DataObject</code>.
     * @see BatchCallTree#getResult()
     */
    protected Object getResult() { return result; }
    
    /**
     * Creates a new instance.
     * If bad arguments are passed, we throw a runtime
	 * exception so to fail early and in the caller's thread.
	 * 
     * @param values The collection of object to delete.
     */
    public DataObjectRemover(Collection<DeletableObject> values)
    {
    	 if (values == null)
             throw new IllegalArgumentException("No objects to remove.");
    	 call = makeDeleteCall(values);
    }
    
    /**
     * Creates a new instance.
     * If bad arguments are passed, we throw a runtime
	 * exception so to fail early and in the caller's thread.
	 * 
     * @param value The object to delete.
     */
    public DataObjectRemover(DeletableObject value)
    {
    	if (value == null)
            throw new IllegalArgumentException("No object to remove.");
    	List<DeletableObject> l = new ArrayList<DeletableObject>(1);
    	l.add(value);
    	call = makeDeleteCall(l);
    }
    
}
