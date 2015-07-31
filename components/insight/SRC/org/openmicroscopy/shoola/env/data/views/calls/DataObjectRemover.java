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
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.data.RequestCallback;
import org.openmicroscopy.shoola.env.data.OmeroDataService;
import org.openmicroscopy.shoola.env.data.model.DeletableObject;
import omero.gateway.SecurityContext;
import org.openmicroscopy.shoola.env.data.views.BatchCall;
import org.openmicroscopy.shoola.env.data.views.BatchCallTree;
import org.openmicroscopy.shoola.env.data.views.ProcessBatchCall;
import org.openmicroscopy.shoola.env.data.views.ProcessCallback;

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

	/** The server call-handle to the computation. */
	private Object		callBack;
	
	/** The objects to delete.*/
	private Map<SecurityContext, Collection<DeletableObject>> map;
    
    /**
     * Creates a {@link BatchCall} to delete the specified objects.
     * 
     * @param ctx The security context.
     * @param values   The objects to delete.
     * @return The {@link BatchCall}.
     */
    private BatchCall makeDeleteCall(final SecurityContext ctx,
    		final Collection<DeletableObject> values)
    {
    	return new ProcessBatchCall("Delete the Objects") {
    		public ProcessCallback initialize() throws Exception
    		{
    			OmeroDataService os = context.getDataService();
    			RequestCallback cb = os.delete(ctx, values);
    			if (cb == null) {
    				callBack = Boolean.valueOf(false);
                	return null;
    			} else {
    				callBack = new ProcessCallback(cb);
                    return (ProcessCallback) callBack;
    			}
    		}
    	};
    }
    
    /**
     * Adds the {@link #call} to the computation tree.
     * @see BatchCallTree#buildTree()
     */
    protected void buildTree()
    {
    	Entry entry;
    	Iterator i = map.entrySet().iterator();
    	while (i.hasNext()) {
			entry = (Entry) i.next();
			final Collection<DeletableObject> l = 
				(Collection<DeletableObject>) entry.getValue();
			final SecurityContext ctx = (SecurityContext) entry.getKey();
			add(makeDeleteCall(ctx, l));
		}
    }

    /**
     * Returns the server call-handle to the computation.
     * 
     * @return See above.
     */
    protected Object getPartialResult() { return callBack; }
    
    /**
     * Returns the result.
     * @see BatchCallTree#getResult()
     */
    protected Object getResult() { return null; }
    
    /**
     * Creates a new instance.
     * If bad arguments are passed, we throw a runtime
	 * exception so to fail early and in the caller's thread.
	 * 
     * @param values The collection of object to delete.
     */
    public DataObjectRemover(Map<SecurityContext, Collection<DeletableObject>>
     values)
    {
    	if (values == null)
    		throw new IllegalArgumentException("No objects to remove.");
    	this.map = values;
    }

}
