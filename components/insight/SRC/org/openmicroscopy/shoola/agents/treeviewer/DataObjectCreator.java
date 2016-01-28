/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
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
package org.openmicroscopy.shoola.agents.treeviewer;

import java.util.Collection;
import java.util.List;

import org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewer;
import omero.gateway.SecurityContext;
import org.openmicroscopy.shoola.env.data.views.CallHandle;
import omero.gateway.model.DataObject;

/** 
 * Creates a <code>DataObject</code> of the specified type.
 * This class calls the <code>createDataObject</code> method in the
 * <code>DataManagerView</code>.
 * 
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * @since OME2.2
 */
public class DataObjectCreator
	extends DataTreeViewerLoader
{
    
    /** The {@link DataObject} to handle. */
    private DataObject      userObject;
    
    /** The parent of the data object to create. */
    private DataObject      parent;
    
    /** The children to add to the object.*/
    private Collection      children;
    
    /** Handle to the asynchronous call so that we can cancel it. */
    private CallHandle  	handle;
    
    /**
     * Creates a new instance.
     * 
     * @param viewer        The Editor this data loader is for.
     *                      Mustn't be <code>null</code>.
     * @param ctx The security context.
     * @param userObject    The {@link DataObject} to handle. 
     * @param parent        The parent of the object to create,
     *                      <code>null</code> if no parent.
     */
    public DataObjectCreator(TreeViewer viewer, SecurityContext ctx,
    		DataObject userObject, DataObject parent)
    {
        this(viewer, ctx, userObject, parent, null);
    }
    
    /**
     * Creates a new instance.
     * 
     * @param viewer        The Editor this data loader is for.
     *                      Mustn't be <code>null</code>.
     * @param ctx The security context.
     * @param userObject    The {@link DataObject} to handle. 
     * @param parent        The parent of the object to create,
     *                      <code>null</code> if no parent.
     */
    public DataObjectCreator(TreeViewer viewer,  SecurityContext ctx,
    		DataObject userObject, DataObject parent, Collection children)
    {
        super(viewer, ctx);
        if (userObject == null)
            throw new IllegalArgumentException("No object to create.");
        this.parent = parent;
        this.userObject = userObject;
        this.children = children;
    }
    
    /** 
     * Creates the object.
     * @see DataTreeViewerLoader#load()
     */
    public void load()
    {
    	if (children == null || children.size() == 0)
    		handle = dmView.createDataObject(ctx, userObject, parent, this);
    	else 
    		handle = mhView.createDataObject(ctx, parent, userObject, children,
    				this);
    }

    /**
     * Cancels the data loading.
     * @see DataTreeViewerLoader#cancel()
     */
    public void cancel() { handle.cancel(); }

    /** 
     * Feeds the result back to the viewer.
     * @see DataTreeViewerLoader#handleResult(Object)
     */
    public void handleResult(Object result)
    {
        if (viewer.getState() == TreeViewer.DISCARDED) return;  //Async cancel.
        List l = (List) result;
        DataObject d = null;
        if (l != null && l.size() == 1) d = (DataObject) l.get(0);
        viewer.onDataObjectSave(d, parent, TreeViewer.CREATE_OBJECT);
    }
    
}
