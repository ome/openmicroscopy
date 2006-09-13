/*
 * org.openmicroscopy.shoola.agents.treeviewer.ExistingObjectsSaver
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
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewer;
import org.openmicroscopy.shoola.env.data.views.CallHandle;

import pojos.CategoryGroupData;
import pojos.DataObject;
import pojos.DatasetData;
import pojos.ProjectData;

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
public class ExistingObjectsSaver
    extends DataTreeViewerLoader
{

    /** The data object to update. */
    private DataObject  parent;
    
    /** The items to add. */
    private Set         children;
    
    /** Handle to the async call so that we can cancel it. */
    private CallHandle  handle;
    
    /**
     * Controls if the specified element is supported.
     * 
     * @param o The object to control.
     */
    private void checkParent(DataObject o)
    {
        if ((o instanceof ProjectData) || (o instanceof DatasetData) ||
                (o instanceof CategoryGroupData))
            return;
        throw new IllegalArgumentException("Data object not supported.");   
    }
    
    /**
     * Creates a new instance.
     * 
     * @param viewer    The TreeViewer this data loader is for.
     *                  Mustn't be <code>null</code>.
     * @param parent    The data object to update.
     * @param children  The items to add to the data object.
     */
    public ExistingObjectsSaver(TreeViewer viewer, DataObject parent,
                                Set children)
    {
        super(viewer);
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
        handle = dmView.addExistingObjects(parent, children, this);
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
        viewer.getSelectedBrowser().refresh();
        //viewer.onDataObjectSave((DataObject) result, TreeViewer.UPDATE_OBJECT);
    }
    
}
