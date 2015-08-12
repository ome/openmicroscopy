/*
 * org.openmicroscopy.shoola.agents.dataBrowser.TagsFilter 
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
package org.openmicroscopy.shoola.agents.dataBrowser;

//Java imports
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.dataBrowser.view.DataBrowser;
import omero.gateway.SecurityContext;
import org.openmicroscopy.shoola.env.data.views.CallHandle;
import pojos.DataObject;
import pojos.TagAnnotationData;

/** 
 * Filters the nodes by tags.
 * This class calls the <code>filterByAnnotation</code> method in the
 * <code>MetadataHandlerView</code>.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class TagsFilter 
	extends DataBrowserLoader
{

	/** The collection of nodes to filter. */
    private List<Long>				nodeIds;
    
    /** Store the nodes for later reused. */
    private Map<Long, DataObject> 	nodes;
    
    /** The type of node to handle. */
    private Class					nodeType;
    
    /** The collection of tags to search for. */
    private List<String> tags;
    
    /** Handle to the asynchronous call so that we can cancel it. */
    private CallHandle handle;

    /**
     * Creates a new instance.
     * 
     * @param viewer 	The viewer this data loader is for.
     *               	Mustn't be <code>null</code>.
     * @param ctx The security context.
     * @param tags		The collection of tags to filter by.
     * 					If <code>null</code> or <code>empty</code>
     *					retrieve the uncommented objects.
     * @param nodes		The collection of objects to filter. 
     * 					Mustn't be <code>null</code>.
     */
	public TagsFilter(DataBrowser viewer, SecurityContext ctx, 
			List<String> tags, Collection<DataObject> nodes)
	{
		super(viewer, ctx);
		if (nodes == null || nodes.size() == 0)
			throw new IllegalArgumentException("No nodes to filter.");
		this.tags = tags;
		this.nodes = new HashMap<Long, DataObject>();
		nodeIds  = new ArrayList<Long>();
		Iterator<DataObject> i = nodes.iterator();
		DataObject data;
		while (i.hasNext()) {
			data = i.next();
			nodeIds.add(data.getId());
			nodeType = data.getClass();
			this.nodes.put(data.getId(), data);
		}
	}

	/** 
	 * Cancels the data loading. 
	 * @see DataBrowserLoader#cancel()
	 */
	public void cancel() { handle.cancel(); }

	/** 
	 * Filters by specified tags
	 * @see DataBrowserLoader#load()
	 */
	public void load()
	{
		long userID = -1;
		handle = mhView.filterByAnnotation(ctx, nodeType, nodeIds, 
							TagAnnotationData.class, tags, userID, this);
	}
	
	/**
     * Feeds the result back to the viewer.
     * @see DataBrowserLoader#handleResult(Object)
     */
    public void handleResult(Object result) 
    {
    	if (viewer.getState() == DataBrowser.DISCARDED) return;  //Async cancel.
    	Collection l = (Collection) result;
    	List<DataObject> filteredNodes = new ArrayList<DataObject>();
    	if (l == null) {
    		viewer.setFilteredNodes(filteredNodes, null);
    		return;
    	}
    	Iterator i = l.iterator();
    	long id;
    	while (i.hasNext()) {
			id = (Long) i.next();
			filteredNodes.add(nodes.get(id));
		}
    	viewer.setFilteredNodes(filteredNodes, null);
    }
    
}
