/*
 * org.openmicroscopy.shoola.agents.treeviewer.DataObjectUpdater
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2015 University of Dundee. All rights reserved.
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

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewer;
import omero.gateway.SecurityContext;
import omero.gateway.model.DataObject;
import org.openmicroscopy.shoola.env.data.views.CallHandle;


/** 
 * Updates the data objects. 
 * This class calls one of the <code>addExistingObjects</code>
 * and <code>cutAndPaste</code> methods in the
 * <code>DataManagerView</code>.
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
    
    /** Identifies the <code>Cut</code> action. */
    public static final int CUT = 2;
    
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
    
    /** Handle to the asynchronous call so that we can cancel it. */
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
            case CUT:
                return;
            default:
                throw new IllegalArgumentException("Action not supported.");
        }
    }
    
    /**
     * Creates a new instance.
     * 
     * @param viewer The Editor this data loader is for.
     *               Mustn't be <code>null</code>.
     * @param ctx The security context.
     * @param objects The objects to update.
     * @param index One of the constants defined by this class.
     */
    public DataObjectUpdater(TreeViewer viewer, SecurityContext ctx,
    		Map objects, int index)
    {
        super(viewer, ctx);
        if (objects == null)
            throw new IllegalArgumentException("No DataObject");
        checkIndex(index);
        objectsToUpdate = objects;
        this.index = index;
    }
    
    /**
     * Creates a new instance.
     * 
     * @param viewer The Editor this data loader is for.
     *               Mustn't be <code>null</code>.
     *  @param ctx The security context.
     * @param objects The objects to update.
     * @param toRemove The objects to remove.
     * @param index One of the constants defined by this class.
     */
    public DataObjectUpdater(TreeViewer viewer,SecurityContext ctx,
    		Map objects, Map toRemove, int index)
    {
        super(viewer, ctx);
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
            handle = dmView.addExistingObjects(ctx, objectsToUpdate, null,
            		this);
        else if ((index == CUT_AND_PASTE) || (index == CUT)) {
        	boolean admin = false;
        	Browser browser = viewer.getSelectedBrowser();
        	if (browser != null)
        		 admin = browser.getBrowserType() == Browser.ADMIN_EXPLORER;
        	handle = dmView.cutAndPaste(ctx, objectsToUpdate, objectsToRemove,
    				admin, this);
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
        if (viewer.getState() == TreeViewer.DISCARDED)
            return; // Async cancel.
        DataObject target = null;
        if (Map.class.isAssignableFrom(result.getClass())) {
            Map m = (Map) result;
            if (m.size() == 1) {
                Object obj = m.keySet().iterator().next();
                if (DataObject.class.isAssignableFrom(obj.getClass()))
                    target = (DataObject) obj;
            }
        }
        viewer.onNodesMoved(target);
    }
    
}
