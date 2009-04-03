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
import java.util.Map;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewer;
import org.openmicroscopy.shoola.env.data.views.CallHandle;
import pojos.DataObject;

/** 
 * Removes data objects. Depending on the specified parameters, 
 * This class calls one of the <code>removeDataObjects</code> methods in the
 * <code>DataManagerView</code>.
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

    /** The {@link DataObject}s to remove. */
    private Set            userObjects;
    
    /** The parent of the <code>DataObject to remove</code>. */
    private DataObject      parent;
    
    /** 
     * Map whose keys are the parent and values the collection of children
     * to remove.
     */
    private Map             objectsToRemove;
    
    /** Handle to the async call so that we can cancel it. */
    private CallHandle      handle;

    /**
     * Creates a new instance.
     * 
     * @param viewer        The Editor this data loader is for.
     *                      Mustn't be <code>null</code>.
     * @param userObjects   The {@link DataObject}s to remove from the parent. 
     * @param parent        The parent of the {@link DataObject} to remove.
     */
    public DataObjectRemover(TreeViewer viewer, Set userObjects,
                            DataObject parent)
    {
        super(viewer);
        if (userObjects == null)
            throw new IllegalArgumentException("No DataObject");
        if (userObjects.size()  == 0)
            throw new IllegalArgumentException("No DataObject");
        this.userObjects = userObjects;
        this.parent = parent;
    }
    
    /**
     * Creates a new instance.
     * This constructor should be invoked when we wish to remove 
     * children from several parents which aren't top containers.
     * 
     * @param viewer    The Editor this data loader is for.
     *                      Mustn't be <code>null</code>.
     * @param objects   The collection of parent, children objects.
     */
    public DataObjectRemover(TreeViewer viewer, Map objects)
    {
        super(viewer);
        if (objects == null)
            throw new IllegalArgumentException("No DataObject");
        objectsToRemove = objects;
    }
    
    /** 
     * Saves the data.
     * @see DataTreeViewerLoader#load()
     */
    public void load()
    {
        if (objectsToRemove == null) {
            handle = dmView.removeDataObjects(userObjects, parent, this);
        } else {
            handle = dmView.removeDataObjects(objectsToRemove, this);
        }
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
        viewer.onNodesRemoved();
        //viewer.onDataObjectSave((DataObject) result, TreeViewer.REMOVE_OBJECT);
    }
    
}
