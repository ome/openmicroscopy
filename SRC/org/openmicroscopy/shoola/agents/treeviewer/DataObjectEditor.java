/*
 * org.openmicroscopy.shoola.agents.treeviewer.DataObjectEditor
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

package org.openmicroscopy.shoola.agents.treeviewer;




//Java imports

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewer;
import org.openmicroscopy.shoola.env.data.views.CallHandle;
import pojos.DataObject;

/** 
 * Updates or deletes the specified <code>DataObject</code>.
 * This class calls the <code>updateDataObject</code> method in the
 * <code>DataManagerView</code> to update and <code>removeDataObject</code> 
 * method in the <code>DataManagerView</code> to delete.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class DataObjectEditor
    extends DataTreeViewerLoader
{
    
    /** Identifies the <code>Update</code> operation. */
    private static final int	UPDATE_OBJECT = 0;
    
    /** Identifies the <code>Delete</code> operation. */
    private static final int	DELETE_OBJECT = 1;

    /** The {@link DataObject} to handle. */
    private DataObject      userObject;
    
    /** The operation to perform on the data object. */
    private int				operation;
    
    /** The parent of the {@link #userObject}. */
    private Object			parent;
    
    /** Handle to the async call so that we can cancel it. */
    private CallHandle  	handle;
    
    /**
     * Returns the contant corresponding the the {@link #operation}.
     * 
     * @return See above.
     */
    private int getViewerOp()
    {
        switch (operation) {
            case UPDATE_OBJECT:
                return TreeViewer.UPDATE_OBJECT;
            case DELETE_OBJECT:
                return TreeViewer.DELETE_OBJECT;
            default:
                throw new IllegalArgumentException("Operation not valid.");
        }
    }
    
    /**
     * Creates a new instance.
     * 
     * @param viewer        The TreeViewer this data loader is for.
     *                      Mustn't be <code>null</code>.
     * @param userObject    The {@link DataObject} to handle. 
     */
    public DataObjectEditor(TreeViewer viewer, DataObject userObject)
    {
        super(viewer);
        if (userObject == null)
            throw new IllegalArgumentException("No DataObject");
        this.userObject = userObject;
        parent = null;
        operation = UPDATE_OBJECT;
    }
    
    /**
     * Creates a new instance.
     * 
     * @param viewer        The TreeViewer this data loader is for.
     *               	    Mustn't be <code>null</code>.
     * @param userObject    The {@link DataObject} to handle. 
     * @param parent        The parent of the {@link DataObject} to handle.
     */
    public DataObjectEditor(TreeViewer viewer, DataObject userObject,
            				Object parent)
    {
        super(viewer);
        if (userObject == null)
            throw new IllegalArgumentException("No DataObject");
        this.userObject = userObject;
        this.parent = parent;
        operation = DELETE_OBJECT;
    }
    
    /** 
     * Saves the data.
     * @see DataBrowserLoader#load()
     */
    public void load()
    {
        if (operation == UPDATE_OBJECT)
            handle = dmView.updateDataObject(userObject, this);
        else if (operation == DELETE_OBJECT)   
            handle = dmView.removeDataObject(userObject, parent, this);
    }

    /**
     * Cancels the data loading.
     * @see DataBrowserLoader#cancel()
     */
    public void cancel() { handle.cancel(); }

    /** 
     * Feeds the result back to the viewer.
     * @see DataTreeViewerLoader#handleResult(Object)
     */
    public void handleResult(Object result)
    {
        if (viewer.getState() == TreeViewer.DISCARDED) return;  //Async cancel.
        viewer.setSaveResult((DataObject) result, getViewerOp());
    }
    
}
