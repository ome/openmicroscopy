/*
 * org.openmicroscopy.shoola.agents.hiviewer.DataObjectSaver
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

package org.openmicroscopy.shoola.agents.hiviewer;


//Java imports
import java.util.ArrayList;
import java.util.List;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.hiviewer.view.HiViewer;
import org.openmicroscopy.shoola.env.data.views.CallHandle;
import pojos.DataObject;

/** 
 * Updates the specified <code>DataObject</code>.
 * This class calls the <code>updateDataObject</code> method in the
 * <code>HierarchyBrowsingView</code> to update.
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
public class DataObjectSaver
    extends DataLoader
{

    /** The <code>DataObject</code> to update. */
    private DataObject object;
    
    /** Handle to the async call so that we can cancel it. */
    private CallHandle  handle;
    
    /**
     * Creates a new instance.
     * 
     * @param viewer    The viewer this data loader is for.
     *                  Mustn't be <code>null</code>.
     * @param object    The <code>DataObject</code> to update 
     *                  Mustn't be <code>null</code>.
     */
    public DataObjectSaver(HiViewer viewer, DataObject object)
    {
        super(viewer);
        if (object == null)
            throw new IllegalArgumentException("No object to update.");
        this.object = object;
    }
    
    /**
     * Saves the data object.
     * @see DataLoader#load()
     */
    public void load()
    {
        handle = hiBrwView.updateDataObject(object, this);
    }
    
    /** 
     * Cancels the data saving. 
     * @see DataLoader#cancel()
     */
    public void cancel() { handle.cancel(); }
    
    /**
     * Feeds the result back to the viewer. 
     * @see DataLoader#handleResult(Object)
     */
    public void handleResult(Object result)
    {
        if (viewer.getState() == HiViewer.DISCARDED) return;  //Async cancel.
        if (result instanceof DataObject) {
        	List set = new ArrayList(1);
        	set.add(result);
        	viewer.onDataObjectSave(set);
        } else if (result instanceof List) {
        	viewer.onDataObjectSave((List) result);
        }
    }
    
}
