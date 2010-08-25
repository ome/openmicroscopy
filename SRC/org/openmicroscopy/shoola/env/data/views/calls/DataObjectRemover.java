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
import org.openmicroscopy.shoola.env.data.DeleteCallback;
import org.openmicroscopy.shoola.env.data.OmeroDataService;
import org.openmicroscopy.shoola.env.data.model.DeletableObject;
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

	/** The call. */
	private BatchCall       call;

	/** The server call-handle to the computation. */
	private Object		callBack;
    
    /**
     * Creates a {@link BatchCall} to delete the specified objects.
     * 
     * @param values   The objects to delete.
     * @return The {@link BatchCall}.
     */
    private BatchCall makeDeleteCall(final Collection<DeletableObject> values)
    {
    	return new ProcessBatchCall("Delete the Objects") {
    		public ProcessCallback initialize() throws Exception
    		{
    			OmeroDataService os = context.getDataService();
    			DeleteCallback cb = os.delete(values);
    			if (cb == null) {
    				callBack = Boolean.valueOf(false);
                	return null;
    			} else {
    				callBack = new ProcessCallback(cb);
                    return (ProcessCallback) callBack;
    			}
    		}
    	};
    	
    	/*
        return new BatchCall("Delete the object.") {
            public void doCall() throws Exception
            {
            	Iterator<DeletableObject> i = values.iterator();
            	DeletableObject o;
            	DataObject data;
            	List<ExperimenterData> list = new ArrayList<ExperimenterData>();
            	while (i.hasNext()) {
					o = i.next();
					data = o.getObjectToDelete();
					if (data instanceof ExperimenterData)
						list.add((ExperimenterData) data);
				}
            	if (list.size() > 0) {
            		AdminService os = context.getAdminService();
            		list =  os.deleteExperimenters(list);
            		List<DeletableObject> l = new ArrayList<DeletableObject>();
            		if (list != null && list.size() > 0) {
            			Iterator<ExperimenterData> j = list.iterator();
            			while (j.hasNext()) {
            				l.add(new DeletableObject(j.next()));
						}
            		}
                	result = l;
            	} else {
            		OmeroDataService os = context.getDataService();
                	result = os.delete(values);
            	}
            }
        };
        */
    }
    
    /**
     * Adds the {@link #call} to the computation tree.
     * @see BatchCallTree#buildTree()
     */
    protected void buildTree() { add(call); }

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
    protected Object getResult() { return Boolean.valueOf(true); }
    
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
