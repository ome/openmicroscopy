/*
 * org.openmicroscopy.shoola.agents.treeviewer.view.DataObjectRemover
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

package org.openmicroscopy.shoola.agents.treeviewer.view;


//Java imports

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.DataTreeViewerLoader;
import org.openmicroscopy.shoola.env.data.views.CallHandle;
import pojos.DataObject;

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class DataObjectRemover
    extends DataTreeViewerLoader
{

    /** The {@link DataObject} to handle. */
    private DataObject      userObject;
    
    /** The parent of the {@link #userObject}. */
    private Object          parent;
    
    /** Handle to the async call so that we can cancel it. */
    private CallHandle      handle;
    
    /**
     * Creates a new instance.
     * 
     * @param viewer        The Editor this data loader is for.
     *                      Mustn't be <code>null</code>.
     * @param userObject    The {@link DataObject} to handle. 
     * @param parent        The parent of the {@link DataObject} to handle.
     */
    public DataObjectRemover(TreeViewer viewer, DataObject userObject,
                            Object parent)
    {
        super(viewer);
        if (userObject == null)
            throw new IllegalArgumentException("No DataObject");
        this.userObject = userObject;
        this.parent = parent;
    }

    /** 
     * Saves the data.
     * @see DataTreeViewerLoader#load()
     */
    public void load()
    {
        handle = dmView.removeDataObject(userObject, parent, this);
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
        viewer.setSaveResult((DataObject) result, TreeViewer.REMOVE_OBJECT);
    }
    
}
