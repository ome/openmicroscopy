/*
 * org.openmicroscopy.shoola.agents.treeviewer.DataObjectEditor
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
import java.util.HashSet;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import org.jdesktop.swingx.auth.UserPermissions;
import org.openmicroscopy.shoola.agents.treeviewer.editors.Editor;
import org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewer;
import org.openmicroscopy.shoola.env.data.views.CallHandle;

import pojos.CategoryData;
import pojos.DataObject;
import pojos.ImageData;

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

	/** Indicates to update the data object. */
	public static final int UPDATE = 0;
	
	/** Indicates to remove the data object. */
	public static final int	REMOVE = 1;
	
	/** Indicates to tag the data object. */
	public static final int	TAG = 2;
	
    /** The {@link DataObject} to handle. */
    private DataObject      	userObject;
    
    /** The operation to perform on the data object. */
    private int					index;
    
    /** The parent of the {@link #userObject}. */
    private DataObject      	parent;
    
    private Set<CategoryData> 	tags;
    
    /** Handle to the async call so that we can cancel it. */
    private CallHandle  		handle;
    
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
        index = UPDATE;
    }
    
    /**
     * Creates a new instance.
     * 
     * @param viewer        The Editor this data loader is for.
     *               	    Mustn't be <code>null</code>.
     * @param userObject    The {@link DataObject} to handle. 
     * @param parent        The parent of the {@link DataObject} to handle.
     * @param index			The operation to perform. One of the constants 
     * 						defined by this class.
     */
    public DataObjectEditor(Editor viewer, DataObject userObject,
                            DataObject parent, int index)
    {
        super(viewer);
        if (userObject == null)
            throw new IllegalArgumentException("No DataObject.");
        this.userObject = userObject;
        this.parent = parent;
        this.index = index;
    }
    
    /**
     * Creates a new instance.
     * 
     * @param viewer        The Editor this data loader is for.
     *               	    Mustn't be <code>null</code>.
     * @param userObject   	The {@link DataObject} to handle. 
     * @param tags        	The parent of the {@link DataObject} to handle.
     * @param index			The operation to perform. One of the constants 
     * 						defined by this class.
     */
    public DataObjectEditor(Editor viewer, DataObject userObject,
                            Set<CategoryData> tags, int index)
    {
        super(viewer);
        if (userObject == null)
            throw new IllegalArgumentException("No DataObject.");
        this.userObject = userObject;
        this.tags = tags;
        this.index = index;
    }
    
    /** 
     * Saves the data.
     * @see EditorLoader#load()
     */
    public void load()
    {
    	Set<DataObject> l = new HashSet<DataObject>(1);
        l.add(userObject);
    	switch (index) {
			case UPDATE:
				handle = dmView.updateDataObject(userObject, this);
				break;
	
			case REMOVE:
	        	if (tags != null) handle = dhView.declassify(l, tags, this);
	        	else handle = dmView.removeDataObjects(l, parent, this);
				break;
			case TAG:
				if (tags == null) {
					tags = new HashSet<CategoryData>(1);
					tags.add((CategoryData) parent);
				}
				handle = dmView.classify(l, tags, this);
		}
       
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
        //viewer.setSaveResult((DataObject) result, operation);
        viewer.onTagsUpdate();
    }
    
}
