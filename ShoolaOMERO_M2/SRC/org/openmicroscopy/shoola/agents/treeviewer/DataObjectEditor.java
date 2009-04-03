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
import org.openmicroscopy.shoola.agents.treeviewer.editors.Editor;
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
    extends EditorLoader
{

    /** The {@link DataObject} to handle. */
    private DataObject      userObject;
    
    /** The operation to perform on the data object. */
    private int				operation;
    
    /** The parent of the {@link #userObject}. */
    private DataObject      parent;
    
    /** Handle to the async call so that we can cancel it. */
    private CallHandle  	handle;
    
    /**
     * Creates a new instance.
     * 
     * @param viewer        The Editor this data loader is for.
     *                      Mustn't be <code>null</code>.
     * @param userObject    The {@link DataObject} to handle. 
     */
    public DataObjectEditor(Editor viewer, DataObject userObject)
    {
        super(viewer);
        if (userObject == null)
            throw new IllegalArgumentException("No DataObject");
        this.userObject = userObject;
        parent = null;
        operation = Editor.UPDATE_OBJECT;
    }
    
    /**
     * Creates a new instance.
     * 
     * @param viewer        The Editor this data loader is for.
     *               	    Mustn't be <code>null</code>.
     * @param userObject    The {@link DataObject} to handle. 
     * @param parent        The parent of the {@link DataObject} to handle.
     */
    public DataObjectEditor(Editor viewer, DataObject userObject,
                            DataObject parent)
    {
        super(viewer);
        if (userObject == null)
            throw new IllegalArgumentException("No DataObject.");
        this.userObject = userObject;
        this.parent = parent;
        operation = Editor.DELETE_OBJECT;
    }
    
    /** 
     * Saves the data.
     * @see EditorLoader#load()
     */
    public void load()
    {
        if (operation == Editor.UPDATE_OBJECT)
            handle = dmView.updateDataObject(userObject, this);
        else if (operation == Editor.DELETE_OBJECT)   
            handle = dmView.removeDataObject(userObject, parent, this);
    }

    /**
     * Cancels the data loading.
     * @see EditorLoader#cancel()
     */
    public void cancel() { handle.cancel(); }

    /** 
     * Feeds the result back to the viewer.
     * @see EditorLoader#handleResult(Object)
     */
    public void handleResult(Object result)
    {
        if (viewer.getState() == Editor.DISCARDED) return;  //Async cancel.
        viewer.setSaveResult((DataObject) result, operation);
    }
    
}
