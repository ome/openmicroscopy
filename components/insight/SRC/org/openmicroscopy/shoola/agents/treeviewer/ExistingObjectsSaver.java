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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewer;
import omero.gateway.SecurityContext;
import org.openmicroscopy.shoola.env.data.views.CallHandle;
import omero.gateway.model.DataObject;
import omero.gateway.model.DatasetData;
import omero.gateway.model.ProjectData;

/** 
 * Loads existing objects of a given type.
 * This class calls one of the <code>addExistingObjects</code> method in the
 * <code>DataManagerView</code>.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since OME2.2
 */
public class ExistingObjectsSaver
    extends DataTreeViewerLoader
{

    /** The data object to update. */
    private DataObject  parent;
    
    /** The items to add. */
    private Set         children;
    
    /** Handle to the asynchronous call so that we can cancel it. */
    private CallHandle  handle;
    
    /**
     * Controls if the specified element is supported.
     * 
     * @param o The object to control.
     */
    private void checkParent(DataObject o)
    {
        if ((o instanceof ProjectData) || (o instanceof DatasetData))
            return;
        throw new IllegalArgumentException("Data object not supported.");   
    }
    
    /**
     * Creates a new instance.
     * 
     * @param viewer    The TreeViewer this data loader is for.
     *                  Mustn't be <code>null</code>.
     * @param ctx The security context.
     * @param parent    The data object to update.
     * @param children  The items to add to the data object.
     */
    public ExistingObjectsSaver(TreeViewer viewer, SecurityContext ctx,
    		DataObject parent, Set children)
    {
        super(viewer, ctx);
        if (parent == null)
            throw new IllegalArgumentException("Data object cannot be null"); 
        if (children == null || children.size() == 0)
            throw new IllegalArgumentException("No children to add."); 
        checkParent(parent);
        this.parent = parent;
        this.children = children;
    }

    /** 
     * Adds the items to the parent.
     * @see DataTreeViewerLoader#load()
     */
    public void load()
    {
    	List l = new ArrayList();
    	l.add(parent);
        handle = dmView.addExistingObjects(ctx, l, children, this);
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
        viewer.getSelectedBrowser().refreshLoggedExperimenterData();
        //viewer.getSelectedBrowser().refreshTree();
        //viewer.onDataObjectSave((DataObject) result, TreeViewer.UPDATE_OBJECT);
    }
    
}
