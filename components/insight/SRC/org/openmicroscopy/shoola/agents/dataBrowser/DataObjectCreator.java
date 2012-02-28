/*
 * org.openmicroscopy.shoola.agents.dataBrowser.DataObjectCreator 
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
package org.openmicroscopy.shoola.agents.dataBrowser;


//Java imports
import java.util.Collection;
import java.util.List;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.dataBrowser.view.DataBrowser;
import org.openmicroscopy.shoola.env.data.util.SecurityContext;
import org.openmicroscopy.shoola.env.data.views.CallHandle;
import pojos.DataObject;

/** 
 * Creates a data objects e.g. a dataset and links it to the specified children.
 * This class calls the <code>filterByAnnotation</code> method in the
 * <code>createDataObject</code>.
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
public class DataObjectCreator
	extends DataBrowserLoader
{

	/** The parent of the <code>DataObject</code> to create. */
	private DataObject	parent;
	
	/** The <code>DataObject</code> to create. */
	private DataObject	data;
	
	/** The nodes to add to the newly created object. */
	private Collection	children;
	
    /** Handle to the asynchronous call so that we can cancel it. */
    private CallHandle	handle;
   
    /**
     * Creates a new instance.
     * 
     * @param viewer	The viewer this data loader is for.
     *               	Mustn't be <code>null</code>.
     * @param ctx The security context.
     * @param parent	The parent of the <code>DataObject</code> to create
     * 					or <code>null</code>.
     * @param data		The <code>DataObject</code> to create.
     * 					Mustn't be <code>null</code>.
     * @param children	The nodes to add to the newly created object.
     */
    public DataObjectCreator(DataBrowser viewer, SecurityContext ctx,
    	DataObject parent, DataObject data, Collection children)
    {
    	super(viewer, ctx);
    	if (data == null) 
    		throw new IllegalArgumentException("No object to create.");
    	this.data = data;
    	this.parent = parent;
    	this.children = children;
    }
    
    /** 
	 * Cancels the data loading. 
	 * @see DataBrowserLoader#cancel()
	 */
	public void cancel() { handle.cancel(); }

	/** 
	 * Creates a new <code>DataObject</code>.
	 * @see DataBrowserLoader#load()
	 */
	public void load()
	{
		handle = mhView.createDataObject(ctx, parent, data, children, this);
	}
	
	/**
     * Feeds the result back to the viewer.
     * @see DataBrowserLoader#handleResult(Object)
     */
    public void handleResult(Object result) 
    {
    	if (viewer.getState() == DataBrowser.DISCARDED) return;  //Async cancel.
    	List list = (List) result;
    	if (list.size() == 1)
    		viewer.setDataObjectCreated((DataObject) list.get(0), parent);
    }
    
}
