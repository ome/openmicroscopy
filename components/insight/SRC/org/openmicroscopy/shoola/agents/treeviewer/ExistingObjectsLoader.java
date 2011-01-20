/*
 * org.openmicroscopy.shoola.agents.treeviewer.ExistingObjectsLoader
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
import org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewer;
import org.openmicroscopy.shoola.env.data.views.CallHandle;
import pojos.DataObject;
import pojos.DatasetData;
import pojos.ProjectData;

/** 
 * Loads existing objects of a given type.
 * This class calls one of the <code>loadExistingObjects</code> method in the
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
public class ExistingObjectsLoader
    extends DataTreeViewerLoader
{

    /** The node the objects are added to. */
    private DataObject ho;

    /** Handle to the asynchronous call so that we can cancel it. */
    private CallHandle  handle;
    
    /**
     * Controls if the passed object is supported.
     * 
     * @param o The object to control.
     */
    private void checkObject(DataObject o)
    {
        if ((o instanceof DatasetData) || (o instanceof ProjectData))
            return;
        throw new IllegalArgumentException("Data type not supported.");
    }
    
    /**
     * Creates a new instance.
     * 
     * @param viewer    The TreeViewer this data loader is for.
     *                  Mustn't be <code>null</code>.
     * @param ho        The object the nodes has to be added to.
     */ 
    public ExistingObjectsLoader(TreeViewer viewer, DataObject ho)
    {
        super(viewer);
        checkObject(ho);
        this.ho = ho;
    }
    
    /** 
     * Retrieves the available objects.
     * @see DataTreeViewerLoader#load()
     */
    public void load()
    {
        Set<Long> nodes = new HashSet<Long>(1);
        nodes.add(Long.valueOf(ho.getId()));
        //handle = dmView.loadExistingObjects(ho.getClass(), nodes, 
         //       viewer.getRootID(), this);
        
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
        viewer.setExistingObjects((Set) result);
    }
    
}
