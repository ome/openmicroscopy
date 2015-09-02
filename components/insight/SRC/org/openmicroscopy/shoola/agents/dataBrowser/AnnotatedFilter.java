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
package org.openmicroscopy.shoola.agents.dataBrowser;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.openmicroscopy.shoola.agents.dataBrowser.view.DataBrowser;
import omero.gateway.SecurityContext;
import org.openmicroscopy.shoola.env.data.views.CallHandle;
import omero.gateway.model.DataObject;
import omero.gateway.model.TagAnnotationData;
import omero.gateway.model.TextualAnnotationData;

/** 
 * Filters the nodes commented (or not), tagged (or not).
 * This class calls the <code>filterByAnnotation</code> method in the
 * <code>MetadataHandlerView</code>.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since OME3.0
 */
public class AnnotatedFilter 
	extends DataBrowserLoader
{

	/** The collection of nodes to filter. */
    private List<Long>				nodeIds;
    
    /** Store the nodes for later reused. */
    private Map<Long, DataObject> 	nodes;
    
    /** The type of node to handle. */
    private Class					nodeType;
    
    /** The type of node to handle. */
    private Class					annotationType;
    
    /** Flag indicating to filter the annotated or not annotated nodes. */
    private boolean					annotated;
    
    /** Handle to the async call so that we can cancel it. */
    private CallHandle				handle;

    /**
     * Controls if the passed type is supported.
     * 
     * @param value The value to check.
     */
    private void checkType(Class value)
    {
    	if (value == null)
    		throw new IllegalArgumentException("Annotation type" +
    				" cannot be null.");
    	if (value.equals(TagAnnotationData.class)) return;
    	if (value.equals(TextualAnnotationData.class)) return;
    }
    
    /**
     * Creates a new instance.
     * 
     * @param viewer 	The viewer this data loader is for.
     *               	Mustn't be <code>null</code>.
     * @param ctx The security context.
     * @param type		One of the annotation type. 
     * 					Mustn't be <code>null</code>.
     * @param annotated	Pass <code>true</code> to filter the annotated nodes,
     * 					<code>false</code> otherwise.
     * @param nodes		The collection of objects to filter. 
     * 					Mustn't be <code>null</code>.
     */
	public AnnotatedFilter(DataBrowser viewer, SecurityContext ctx, Class type,
			boolean annotated, Collection<DataObject> nodes)
	{
		super(viewer, ctx);
		if (nodes == null || nodes.size() == 0)
			throw new IllegalArgumentException("No nodes to filter.");
		checkType(type);
		this.annotated = annotated;
		annotationType = type;
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
	 * Filters the nodes.
	 * @see DataBrowserLoader#load()
	 */
	public void load()
	{
		long userID = -1;//DataBrowserAgent.getUserDetails().getId();
		handle = mhView.filterByAnnotated(ctx, nodeType, nodeIds,
				annotationType, annotated, userID, this);
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

    	while (i.hasNext()) 
			filteredNodes.add(nodes.get((Long) i.next()));
	
    	viewer.setFilteredNodes(filteredNodes, null);
    }
    
}
