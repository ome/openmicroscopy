/*
 * org.openmicroscopy.shoola.agents.treeviewer.PlateWellsLoader 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
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
import java.util.Set;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.treeviewer.browser.TreeImageSet;
import org.openmicroscopy.shoola.env.data.views.CallHandle;


/** 
 * Loads the plate/wells.
 * This class calls the <code>loadPlateWells</code> method in the
 * <code>DataManagerView</code>. 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta3
 */
public class PlateWellsLoader 	
	extends DataBrowserLoader
{
    
    /** The parent the nodes to retrieve are for. */
    private TreeImageSet		parent;
    
    /** The id of the plate. */
    private long 				plateID;
    
    /** Handle to the async call so that we can cancel it. */
    private CallHandle  		handle;
    
    /**
     * Creates a new instance.
     * 
     * @param viewer  The viewer this data loader is for.
     *                Mustn't be <code>null</code>.
     * @param expNode The node hosting the experimenter the data are for.
     * 				  Mustn't be <code>null</code>.
     * @param parent  The parent the nodes are for.
     * @param plateID The id of the plate.
     */
	public PlateWellsLoader(Browser viewer, TreeImageSet parent, long plateID)
	{
		super(viewer);
		this.parent = parent;
		this.plateID = plateID;
	}
	
	 /**
     * Retrieves the data.
     * @see DataBrowserLoader#load()
     */
    public void load()
    {
    	handle = dmView.loadPlateWells(plateID, -1, this);
    }

    /**
     * Cancels the data loading.
     * @see DataBrowserLoader#cancel()
     */
    public void cancel() { handle.cancel(); }

    /**
     * Feeds the result back to the viewer.
     * @see DataBrowserLoader#handleResult(Object)
     */
    public void handleResult(Object result)
    {
        if (viewer.getState() == Browser.DISCARDED) return;  //Async cancel.
        viewer.setWells((Set) result, parent);
    }

}
