/*
 * org.openmicroscopy.shoola.agents.treeviewer.DataObjectRemover
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
import java.util.Collection;
import java.util.List;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewer;
import omero.gateway.SecurityContext;
import org.openmicroscopy.shoola.env.data.views.CallHandle;
import pojos.DataObject;

/** 
 * Removes data objects. Depending on the specified parameters, 
 * This class calls one of the {@code deleteObjects} methods in the
 * <code>AdminView</code>.
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

    /** The groups or experimenters to remove. */
    private List<DataObject> values;

    /** Handle to the asynchronous call so that we can cancel it. */
    private CallHandle     	 handle;

    /**
     * Creates a new instance.
     * 
     * @param viewer	The Editor this data loader is for.
     * 					Mustn't be <code>null</code>.
     * @param ctx The security context.
     * @param values The groups or experimenters to delete.
     */
    public DataObjectRemover(TreeViewer viewer, SecurityContext ctx,
    		List<DataObject> values)
    {
        super(viewer, ctx);
        if (values == null)
            throw new IllegalArgumentException("No object to delete");
        this.values = values;
    }
    
    /** 
     * Deletes the objects.
     * @see DataTreeViewerLoader#load()
     */
    public void load()
    {
    	 handle = adminView.deleteObjects(ctx, values, this);
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
        viewer.onNodesDeleted((Collection<DataObject>) result);
    }
    
}
