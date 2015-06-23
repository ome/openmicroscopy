/*
 * org.openmicroscopy.shoola.agents.dataBrowser.RateFilter 
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
import pojos.RatingAnnotationData;

/** 
 * Filters the nodes by rate.
 * This class calls the <code>loadRatings</code> method in the
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
public class RateFilter
	extends DataBrowserLoader
{

	/** Indicates to retrieve the node rated one or higher. */
	public static final int			RATE_ONE = 1;
	
	/** Indicates to retrieve the node rated two or higher. */
	public static final int			RATE_TWO = 2;
	
	/** Indicates to retrieve the node rated three or higher. */
	public static final int			RATE_THREE = 3;
	
	/** Indicates to retrieve the node rated four or higher. */
	public static final int			RATE_FOUR = 4;
	
	/** Indicates to retrieve the node rated five. */
	public static final int			RATE_FIVE = 5;
	
	/** Indicates to retrieve the unrated node. */
	public static final int			UNRATED = 0;
	
	/** The collection of nodes to filter. */
    private List<Long>				nodeIds;
    
    /** Store the nodes for later reused. */
    private Map<Long, DataObject> 	nodes;
    
    /** The type of node to handle. */
    private Class					nodeType;
    
    /** One of the rating level defined by this class. */
    private int						ratingLevel;
    
    /** Handle to the asynchronous call so that we can cancel it. */
    private CallHandle				handle;
    
    /**
     * Checks if the passed value is supported.
     * 
     * @param value The value to handle.
     */
    private void checkRate(int value)
    {
    	switch (value) {
			case UNRATED:
			case RATE_ONE:
			case RATE_TWO:
			case RATE_THREE:
			case RATE_FOUR:
			case RATE_FIVE:
				break;
			default:
				throw new IllegalArgumentException("Rate type not supported.");
		}
    }
    
    /**
     * Returns the rating owned by the user currently logged in.
     * 
     * @param ratings The ratings to handle.
     * @return See above.
     */
    private RatingAnnotationData getAnnotation(List ratings)
    {
    	if (ratings == null || ratings.size() == 0) return null;
    	long userID = DataBrowserAgent.getUserDetails().getId();
    	Iterator i = ratings.iterator();
    	RatingAnnotationData data;
    	while (i.hasNext()) {
			data = (RatingAnnotationData) i.next();
			if (data.getOwner().getId() == userID)
				return data;
		}
    	return null;
    }

    /**
     * Creates a new instance.
     * 
     * @param viewer 	The viewer this data loader is for.
     *               	Mustn't be <code>null</code>.
     * @param ctx The security context.
     * @param rate		One of the constants defined by this class.
     * @param nodes		The collection of objects to filter. 
     * 					Mustn't be <code>null</code>.
     */
	public RateFilter(DataBrowser viewer, SecurityContext ctx, int rate,
					Collection<DataObject> nodes)
	{
		super(viewer, ctx);
		if (nodes == null || nodes.size() == 0)
			throw new IllegalArgumentException("No nodes to filter.");
		checkRate(rate);
		ratingLevel = rate;
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
	 * Loads the rating annotations for the specified nodes.
	 * @see DataBrowserLoader#load()
	 */
	public void load()
	{
		long userID = -1;//DataBrowserAgent.getUserDetails().getId();
		handle = mhView.loadRatings(ctx, nodeType, nodeIds, userID, this);
	}
	
	/**
     * Feeds the result back to the viewer.
     * @see DataBrowserLoader#handleResult(Object)
     */
    public void handleResult(Object result) 
    {
    	if (viewer.getState() == DataBrowser.DISCARDED) return;  //Async cancel.
    	Map map = (Map) result;
    	long id;
    	Iterator i = map.keySet().iterator();
    	List<DataObject> filteredNodes = new ArrayList<DataObject>();
    	if (ratingLevel == UNRATED) {
    		while (i.hasNext()) {
    			id = (Long) i.next();
    			nodes.remove(id);
    		}
    		i = nodes.keySet().iterator();
    		while (i.hasNext()) {
    			filteredNodes.add(nodes.get(i.next()));
    		}
    	} else {
    		RatingAnnotationData data;
    		while (i.hasNext()) {
    			id = (Long) i.next();
    			data = getAnnotation((List) map.get(id));
    			if (data != null) {
    				if (data.getRating() >= ratingLevel) 
    					filteredNodes.add(nodes.get(id));
    				
    			}
    		}
    	}
    	viewer.setFilteredNodes(filteredNodes, null);
    }

}
