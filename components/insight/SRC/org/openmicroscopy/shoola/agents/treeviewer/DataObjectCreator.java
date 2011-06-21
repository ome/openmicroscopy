/*
 * org.openmicroscopy.shoola.agents.treeviewer.DataObjectCreator
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

package org.openmicroscopy.shoola.agents.treeviewer;



//Java imports
import java.util.List;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewer;
import org.openmicroscopy.shoola.env.data.views.CallHandle;
import pojos.DataObject;

/** 
 * Creates a <code>DataObject</code> of the specified type.
 * This class calls the <code>createDataObject</code> method in the
 * <code>DataManagerView</code>.
 * 
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$Date: )
 * </small>
 * @since OME2.2
 */
public class DataObjectCreator
	extends DataTreeViewerLoader
{
    
    /** The {@link DataObject} to handle. */
    private DataObject      userObject;
    
    /** The parent of the data object to create. */
    private DataObject      parent;
    
    /** Handle to the asynchronous call so that we can cancel it. */
    private CallHandle  	handle;
    
    /**
     * Creates a new instance.
     * 
     * @param viewer        The Editor this data loader is for.
     *                      Mustn't be <code>null</code>.
     * @param userObject    The {@link DataObject} to handle. 
     * @param parent        The parent of the object to create,
     *                      <code>null</code> if no parent.
     */
    public DataObjectCreator(TreeViewer viewer, DataObject userObject, 
                            DataObject parent)
    {
        super(viewer);
        if (userObject == null)
            throw new IllegalArgumentException("No object to create.");
        this.parent = parent;
        this.userObject = userObject;
    }
    
    /** 
     * Creates the object.
     * @see DataTreeViewerLoader#load()
     */
    public void load()
    {
        handle = dmView.createDataObject(userObject, parent, this);
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
