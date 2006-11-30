/*
 * org.openmicroscopy.shoola.agents.treeviewer.DataObjectUpdater
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

import java.util.Map;

import org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewer;
import org.openmicroscopy.shoola.env.data.views.CallHandle;


//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since OME2.2
 */
public class DataObjectUpdater
    extends DataTreeViewerLoader
{

    /** Identifies the <code>Copy and Paste</code> action. */
    public static final int COPY_AND_PASTE = 0;
    
    /** Identifies the <code>Cut and Paste</code> action. */
    public static final int CUT_AND_PASTE = 1;
    
    /** Action id, one of the constants defined by this class. */
    private int             index;
    
    /** 
     * Map whose keys are the parent and values the collection of children
     * to remove.
     */
    private Map             objectsToUpdate;
    
    /** 
     * Map whose keys are the parent and values the collection of children
     * to remove.
     */
    private Map             objectsToRemove;
    
    /** Handle to the async call so that we can cancel it. */
    private CallHandle      handle;

    /**
     * Controls if the passed index is supported.
     * 
     * @param i The index to control.
     */
    private void checkIndex(int i)
    {
        switch (i) {
            case COPY_AND_PASTE:
            case CUT_AND_PASTE:    
                return;
            default:
                throw new IllegalArgumentException("Action not supported.");
        }
    }
    
    /**
     * Creates a new instance.
     * 
     * @param viewer    The Editor this data loader is for.
     *                      Mustn't be <code>null</code>.
     * @param objects   The objects to update.
     * @param index     One of the constants defined by this class.
     */
    public DataObjectUpdater(TreeViewer viewer, Map objects, int index)
    {
        super(viewer);
        if (objects == null)
            throw new IllegalArgumentException("No DataObject");
        checkIndex(index);
        objectsToUpdate = objects;
        this.index = index;
    }
    
    /**
     * Creates a new instance.
     * 
     * @param viewer    The Editor this data loader is for.
     *                      Mustn't be <code>null</code>.
     * @param objects   The objects to update.
     * @param toRemove  The objects to remove.
     * @param index     One of the constants defined by this class.
     */
    public DataObjectUpdater(TreeViewer viewer, Map objects, Map toRemove, 
                        int index)
    {
        super(viewer);
        if (objects == null)
            throw new IllegalArgumentException("No DataObject");
        checkIndex(index);
        objectsToUpdate = objects;
        objectsToRemove = toRemove;
        this.index = index;
    }
    
    /** 
     * Saves the data.
     * @see DataTreeViewerLoader#load()
     */
    public void load()
    {
        if (index == COPY_AND_PASTE)
            handle = dmView.addExistingObjects(objectsToUpdate, this);
        else if (index == CUT_AND_PASTE) {
            handle = dmView.cutAndPaste(objectsToUpdate, objectsToRemove, this);
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
