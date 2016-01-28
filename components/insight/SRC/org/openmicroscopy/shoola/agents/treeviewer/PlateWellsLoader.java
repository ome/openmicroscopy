/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewer;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageDisplay;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageSet;
import omero.gateway.SecurityContext;
import org.openmicroscopy.shoola.env.data.views.CallHandle;
import omero.gateway.model.DataObject;
import omero.gateway.model.ExperimenterData;
import omero.gateway.model.PlateData;
import omero.gateway.model.PlateAcquisitionData;


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
 * @since 3.0-Beta3
 */
public class PlateWellsLoader 	
	extends DataTreeViewerLoader
{
    
	/** The parent the nodes to retrieve are for. */
    private Map<Long, TreeImageSet>		nodes;
    
    /** Handle to the asynchronous call so that we can cancel it. */
    private CallHandle  				handle;
    
    /** Flag indicating to load or not the thumbnails. */
    private boolean						withThumbnails;
    
    /** 
     * Map whose keys are the plate ID and values are the screen acquisition
     * ID or <code>-1</code>.
     */
    private Map<Long, Long>				ids;
    
    /**
     * Creates a new instance.
     * 
     * @param viewer  The viewer this data loader is for.
     *                Mustn't be <code>null</code>.
     * @param ctx The security context.
     * @param plates  The parent the nodes are for.
	 * @param withThumbnails Pass <code>true</code> to load the thumbnails,
     * 						 <code>false</code> otherwise.
     */
	public PlateWellsLoader(TreeViewer viewer, SecurityContext ctx,
		List<TreeImageSet> plates, boolean withThumbnails)
	{
		super(viewer, ctx);
		if (plates == null || plates.size() == 0)
			throw new IllegalArgumentException("No plates specified.");
		this.withThumbnails = withThumbnails;
		nodes = new HashMap<Long, TreeImageSet>(plates.size());
		ids = new HashMap<Long, Long>(plates.size());
		Iterator<TreeImageSet> i = plates.iterator();
		TreeImageSet p;
		TreeImageDisplay parent;
		DataObject data, parentData;
		PlateAcquisitionData sa;
		while (i.hasNext()) {
			p = i.next();
			data = (DataObject) p.getUserObject();
			if (data instanceof PlateAcquisitionData) {
				sa = (PlateAcquisitionData) data;
				parent = p.getParentDisplay();
				parentData = (DataObject) parent.getUserObject();
				if (parentData instanceof PlateData) {
					nodes.put(parentData.getId(), p);
					ids.put(parentData.getId(), data.getId());
				}
			} else if (data instanceof PlateData) {
				nodes.put(data.getId(), p);
				ids.put(data.getId(), -1L);
			}
		}
	}
	
	/**
	 * Retrieves the data.
	 * @see DataTreeViewerLoader#load()
	 */
    public void load()
    {
    	ExperimenterData exp = TreeViewerAgent.getUserDetails();
    	handle = dmView.loadPlateWells(ctx, ids, exp.getId(), this);
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
        while (i.hasNext()) {
			entry = (Entry) i.next();
			plates.put(nodes.get(entry.getKey()), (Set) entry.getValue());
		}
        viewer.setPlates(plates, withThumbnails);
    }

}
