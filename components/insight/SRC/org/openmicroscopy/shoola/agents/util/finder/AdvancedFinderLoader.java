/*
 * org.openmicroscopy.shoola.agents.util.finder.AdvancedFinderLoader 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2014 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.util.finder;

//Java imports
import java.util.Collections;



//Application-internal dependencies
import org.openmicroscopy.shoola.agents.dataBrowser.DataBrowserLoader;
import org.openmicroscopy.shoola.env.data.events.DSCallFeedbackEvent;

import omero.gateway.SecurityContext;
import omero.gateway.model.SearchResultCollection;
import omero.gateway.model.SearchParameters;

import org.openmicroscopy.shoola.env.data.views.CallHandle;

/** 
 * Searches for data
 * This class calls <code>advancedSearchFor</code> method in the
 * <code>DataHandlerView</code>.
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
public class AdvancedFinderLoader
	extends FinderLoader
{

	/** Collection of terms to search for. */
	private SearchParameters searchContext;
	
	/** Handle to the asynchronous call so that we can cancel it. */
    private CallHandle handle;

	/**
     * Creates a new instance.
     * 
     * @param viewer	The viewer this data loader is for.
     *               	Mustn't be <code>null</code>.
     * @param ctx The security context.
     * @param context	The context of the search.
     * 					Mustn't be <code>null</code>.
     */
    public AdvancedFinderLoader(Finder viewer, SecurityContext ctx,
            SearchParameters context)
    {
    	super(viewer, Collections.singletonList(ctx));
    	if (context == null) 
    		throw new IllegalArgumentException("No scope defined.");
    	searchContext = context;
    }
    
    /**
     * Searches for values.
     * @see FinderLoader#load()
     */
    public void load()
    {
    	handle = dhView.advancedSearchFor(ctx.get(0), searchContext, this);
    }

    /** 
     * Feeds the results of the search as they arrive.
     * @see FinderLoader#update(DSCallFeedbackEvent)
     */
    public void update(DSCallFeedbackEvent fe) 
    {
    	
    }
    
    /**
     * Does nothing as the asynchronous call returns <code>null</code>.
     * The actual pay-load (result) is delivered progressively
     * during the updates.
     * @see DataBrowserLoader#handleNullResult()
     */
    public void handleNullResult() {}
    
    /**
     * Cancels the ongoing data retrieval.
     * @see FinderLoader#cancel()
     */
    public void cancel() { handle.cancel(); }
    
    /** 
     * Feeds the result back to the viewer. 
     * @see FinderLoader#handleResult(Object)
     */
    public void handleResult(Object result)
    {
    	if (viewer.getState() == Finder.DISCARDED) return;  //Async cancel.
        viewer.setResult((SearchResultCollection) result);
    }


}
