/*
 * org.openmicroscopy.shoola.env.data.views.calls.DataObjectSaver
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.data.OmeroDataService;
import omero.gateway.SecurityContext;
import org.openmicroscopy.shoola.env.data.views.BatchCall;
import org.openmicroscopy.shoola.env.data.views.BatchCallTree;
import pojos.DataObject;

/** 
 * Command to create a <code>DataObject</code>.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class DataObjectSaver
    extends BatchCallTree
{

    /** Indicates to create a <code>DataObject</code>. */
    public static final int CREATE = 0;
    
    /** Indicates to update a <code>DataObject</code>. */
    public static final int UPDATE = 1;

    /** The save call. */
    private BatchCall saveCall;
    
    /** The result of the call. */
    private Object result;
    
    /** The security context.*/
    private SecurityContext ctx;
    
    /**
     * Creates a {@link BatchCall} to create the specified {@link DataObject}s.
     * 
     * @param objects    The <code>DataObject</code>s to create.
     * @param parent    The parent of the <code>DataObject</code>.
     * @param children	The children to add to the newly created node.
     * @return The {@link BatchCall}.
     */
    private BatchCall create(final List<DataObject> objects,
    		final DataObject parent, final Collection children)
    {
        return new BatchCall("Create Data object.") {
            public void doCall() throws Exception
            {
                OmeroDataService os = context.getDataService();
                List<DataObject> l = new ArrayList<DataObject>();
                Iterator<DataObject> i = objects.iterator();
                while (i.hasNext()) {
                	l.add(os.createDataObject(ctx, i.next(), parent, children));
				}
                result = l;
            }
        };
    }
    
    /**
     * Creates a {@link BatchCall} to update the specified {@link DataObject}s.
     * 
     * @param objects    The <code>DataObject</code>s to update.
     * @return The {@link BatchCall}.
     */
    private BatchCall update(final List<DataObject> objects)
    {
        return new BatchCall("Create Data object.") {
            public void doCall() throws Exception
            {
                OmeroDataService os = context.getDataService();
                List<DataObject> l = new ArrayList<DataObject>();
                Iterator<DataObject> i = objects.iterator();
                while (i.hasNext()) {
                	l.add(os.updateDataObject(ctx, i.next()));
				}
                result = l;
            }
        };
    }
 
    /**
     * Adds the {@link #saveCall} to the computation tree.
     * @see BatchCallTree#buildTree()
     */
    protected void buildTree() { add(saveCall); }

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
     * @param ctx The security context.
     * @param userObject    The {@link DataObject} to create or update.
     *                      Mustn't be <code>null</code>.
     * @param parent     	The parent of the <code>DataObject</code>. 
     * 						The value is <code>null</code> if there 
     * 						is no parent.
     * @param index         One of the constants defined by this class.
     */
    public DataObjectSaver(SecurityContext ctx, DataObject userObject,
    		DataObject parent, int index)
    {
        if (userObject == null)
            throw new IllegalArgumentException("No DataObject.");
        List<DataObject> objects = new ArrayList<DataObject>();
        objects.add(userObject);
        this.ctx = ctx;
        switch (index) {
            case CREATE:
                saveCall = create(objects, parent, null);
                break;
            case UPDATE:
                saveCall = update(objects);
                break;
            default:
                throw new IllegalArgumentException("Operation not supported.");
        }
    }
    
    /**
     * Creates a new instance.
     * If bad arguments are passed, we throw a runtime
	 * exception so to fail early and in the caller's thread.
     * 
     * @param ctx The security context.
     * @param objects	The {@link DataObject}s to create or update.
     *                  Mustn't be <code>null</code>.
     * @param parent    The parent of the <code>DataObject</code>. 
     * 				    The value is <code>null</code> if there 
     * 					is no parent.
     * @param index     One of the constants defined by this class.
     */
    public DataObjectSaver(SecurityContext ctx,
    	List<DataObject> objects, DataObject parent, int index)
    {
        if (objects == null)
            throw new IllegalArgumentException("No DataObject.");
        this.ctx = ctx;
        switch (index) {
            case CREATE:
                saveCall = create(objects, parent, null);
                break;
            case UPDATE:
                saveCall = update(objects);
                break;
            default:
                throw new IllegalArgumentException("Operation not supported.");
        }
    }
    
    /**
     * Creates a new instance.
     * If bad arguments are passed, we throw a runtime
	 * exception so to fail early and in the caller's thread.
     * 	
     * @param ctx The security context.
     * @param parent	The parent of the <code>DataObject</code> to create
	 * 					or <code>null</code> if no parent specified.
	 * @param data		The <code>DataObject</code> to create.
	 * @param children	The nodes to add to the newly created 
	 * 					<code>DataObject</code>.
     */
    public DataObjectSaver(SecurityContext ctx,
    		DataObject parent, DataObject data, Collection children)
    {
    	if (data == null) 
    		throw new IllegalArgumentException("No object to create.");
    	this.ctx = ctx;
    	List<DataObject> objects = new ArrayList<DataObject>();
    	objects.add(data);
    	saveCall = create(objects, parent, children);
    }

}
