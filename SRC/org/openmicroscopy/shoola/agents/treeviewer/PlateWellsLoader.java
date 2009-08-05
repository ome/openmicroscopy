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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

//Third-party libraries

//Application-internal dependencies
import omero.IllegalArgumentException;
import org.openmicroscopy.shoola.agents.treeviewer.browser.TreeImageSet;
import org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewer;
import org.openmicroscopy.shoola.env.data.views.CallHandle;
import pojos.PlateData;


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
	extends DataTreeViewerLoader
{
    
    /** The parent the nodes to retrieve are for. */
    private Map<Long, TreeImageSet>		nodes;
    
    /** Handle to the asynchronous call so that we can cancel it. */
    private CallHandle  		handle;
    
    /**
     * Creates a new instance.
     * 
     * @param viewer  The viewer this data loader is for.
     *                Mustn't be <code>null</code>.
     * @param plates  The parent the nodes are for.
     */
	public PlateWellsLoader(TreeViewer viewer, List<TreeImageSet> plates)
	{
		super(viewer);
		if (plates == null || plates.size() == 0)
			throw new IllegalArgumentException("No plates specified.");
		nodes = new HashMap<Long, TreeImageSet>(plates.size());
		Iterator<TreeImageSet> i = plates.iterator();
		TreeImageSet p;
		PlateData plate;
		while (i.hasNext()) {
			p = i.next();
			plate = (PlateData) p.getUserObject();
			nodes.put(plate.getId(), p);
		}
	}
	
	 /**
     * Retrieves the data.
     * @see DataTreeViewerLoader#load()
     */
    public void load()
    {
    	handle = dmView.loadPlateWells(nodes.keySet(), -1, this);
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
        Map m = (Map) result;
        Map<TreeImageSet, Set> plates = new HashMap<TreeImageSet, Set>();
        
        Iterator i = m.entrySet().iterator();
        Entry entry;
        TreeImageSet p;
        while (i.hasNext()) {
			entry = (Entry) i.next();
			plates.put(nodes.get(entry.getKey()), (Set) entry.getValue());
		}
        viewer.setPlates(plates);
    }
    
}
