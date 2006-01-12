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
public class DataObjectEditor
    extends DataTreeViewerLoader
{

    /** Indicates to create a new {@link DataObject}. */
    public static final int CREATE = 0;
    
    /** Indicates to update the {@link DataObject}. */
    public static final int EDIT = 1;
    
    /** The {@link DataObject} to handle. */
    private DataObject      userObject;
    
    /** 
     * The ID of the parent of the {@link DataObject}. The value is 
     * set to -1, if it's an object without parent.
     */
    private int             parentID;
    
    /** One of the constant defined by this class. */
    private int             index;
    
    /**
     * Checks if the specified index is valid.
     * 
     * @param i The index to control.
     */
    private void checkIndex(int i)
    {
        switch (i) {
            case CREATE:
            case EDIT:    
                return;
            default:
                throw new IllegalArgumentException("Index not valid");
        }
    }
    
    /**
     * Creates a new instance.
     * 
     * @param viewer The TreeViewer this data loader is for.
     *               Mustn't be <code>null</code>.
     * @param userObject The {@link DataObject} to handle. 
     * @param index The index of the loader. One of the constants defined by 
     *              this class.
     */
    public DataObjectEditor(TreeViewer viewer, DataObject userObject, int index)
    {
        this(viewer, userObject, index, -1);
    }
    
    /**
     * Creates a new instance.
     * 
     * @param viewer The TreeViewer this data loader is for.
     *               Mustn't be <code>null</code>.
     * @param userObject The {@link DataObject} to handle. 
     * @param index The index of the loader. One of the constants defined by 
     *              this class.
     * @param parentID The ID of the parent.
     */
    public DataObjectEditor(TreeViewer viewer, DataObject userObject, int index, 
                            int parentID)
    {
        super(viewer);
        if (userObject == null)
            throw new IllegalArgumentException("No DataObject");
        checkIndex(index);
        this.parentID = parentID;
        this.index = index;
        this.userObject = userObject;
    }
    
    /** Saves the data.*/
    public void load()
    {
        switch (index) {
            case CREATE:
                dmView.createDataObject(userObject, parentID, this);
                break;
            case EDIT:
                dmView.updateDataObject(userObject, this);
        }
    }

    /**
     * Cancels the data loading.
     * @see DataBrowserLoader#cancel()
     */
    public void cancel() { viewer.cancel(); }

    /** 
     * Feeds the result back to the viewer.
     * @see org.openmicroscopy.shoola.env.data.events.DSCallAdapter
     * 		#handleResult(Object)
     */
    public void handleResult(Object result)
    {
        if (viewer.getState() == TreeViewer.DISCARDED) return;  //Async cancel.
        viewer.setSaveResult((DataObject) result);
    }
    
}
