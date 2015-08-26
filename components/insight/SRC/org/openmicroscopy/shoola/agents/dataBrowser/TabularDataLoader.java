/*
 * org.openmicroscopy.shoola.agents.metadata.TabularDataLoader 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2011 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.agents.dataBrowser;


import java.util.List;

import org.openmicroscopy.shoola.agents.dataBrowser.view.DataBrowser;
import org.openmicroscopy.shoola.env.data.model.TableParameters;
import omero.gateway.model.TableResult;
import omero.gateway.SecurityContext;
import org.openmicroscopy.shoola.env.data.views.CallHandle;
import pojos.DataObject;
import pojos.PlateData;
import pojos.ScreenData;

/** 
 * Loads the tabular data.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since 3.0-Beta4
 */
public class TabularDataLoader 
	extends DataBrowserLoader
{

	/** Parameters to load the table. */
	private TableParameters parameters;

    /** Handle to the asynchronous call so that we can cancel it. */
    private CallHandle	handle;
    
    /**
     * Flag indicating to load all annotations available or 
     * to only load the user's annotation.
     */
    private boolean loadAll;
    
    /**	
     * Creates a new instance.
     * 
     * @param viewer The viewer this data loader is for.
     *               Mustn't be <code>null</code>.
     * @param ctx The security context.
     * @param ids The identifier of the files hosting the tabular data.
     * @param loadAll Pass <code>true</code> indicating to load all
     * 				  annotations available if the user can annotate,
     *                <code>false</code> to only load the user's annotation.
     */
    public TabularDataLoader(DataBrowser viewer, SecurityContext ctx,
    		List<Long> ids, boolean loadAll)
    {
    	 super(viewer, ctx);
    	 if (ids == null || ids.size() <= 0)
    		 throw new IllegalArgumentException("No file to retrieve.");
    	 parameters = new TableParameters(ids);
    	 this.loadAll = loadAll;
    }
    
    /**	
     * Creates a new instance.
     * 
     * @param viewer The viewer this data loader is for.
     *               Mustn't be <code>null</code>.
     * @param ctx The security context.
     * @param object The object to handle. Mustn't be <code>null</code>.
     * @param loadAll Pass <code>true</code> indicating to load all
     * 				  annotations available if the user can annotate,
     *                <code>false</code> to only load the user's annotation.
     */
    public TabularDataLoader(DataBrowser viewer, SecurityContext ctx,
    		DataObject object, boolean loadAll)
    {
    	 super(viewer, ctx);
    	 if (object == null)
    		 throw new IllegalArgumentException("No file to retrieve.");
    	 if (!(object instanceof PlateData || object instanceof ScreenData)) {
    		 throw new IllegalArgumentException("Object not supported.");
    	 }
    	 parameters = new TableParameters(object.getClass(), object.getId());
    }
    
    /** 
	 * Loads the tabular data. 
	 * @see DataBrowserLoader#cancel()
	 */
	public void load()
	{
		long userID = getCurrentUser();
		if (loadAll) userID = -1;
		handle = mhView.loadTabularData(ctx, parameters, userID, this);
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
    	if (viewer.getState() == DataBrowser.DISCARDED) return;  //Async cancel.
    	//decide what to do with result
    	if (result == null) return;
    	viewer.setTabularData((List<TableResult>) result);
    } 

}
