/*
 * org.openmicroscopy.shoola.agents.dataBrowser.PlateSaver 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2009 University of Dundee. All rights reserved.
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
import java.util.List;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.dataBrowser.view.DataBrowser;
import omero.gateway.SecurityContext;
import org.openmicroscopy.shoola.env.data.views.CallHandle;
import pojos.DataObject;

/** 
 * Updates the specified plate.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class PlateSaver 
	extends DataBrowserLoader
{

	/** The data object to update, either plate or wells. */
	private List<DataObject> toUpdate;
	
    /** Handle to the async call so that we can cancel it. */
    private CallHandle	handle;
   
	/**
	 * Creates a new instance.
	 * 
	 * @param viewer The viewer this data loader is for.
     *               Mustn't be <code>null</code>.
     * @param ctx The security context.
	 * @param toUpdate	 The collection of objects to update.
	 */
	public PlateSaver(DataBrowser viewer, SecurityContext ctx,
			List<DataObject> toUpdate)
	{
		super(viewer, ctx);	
		if (toUpdate == null || toUpdate.size() == 0) 
			throw new IllegalArgumentException("No objects to update.");
		this.toUpdate = toUpdate;
	}
	
	/** 
	 * Cancels the data loading. 
	 * @see DataBrowserLoader#cancel()
	 */
	public void cancel() { handle.cancel(); }

	/** 
	 * Updates the plate back to the server.
	 * @see DataBrowserLoader#load()
	 */
	public void load()
	{
		handle = mhView.updateDataObjects(ctx, toUpdate, this);
	}
	
	/**
     * Feeds the result back to the viewer.
     * @see DataBrowserLoader#handleResult(Object)
     */
    public void handleResult(Object result) 
    {
    	if (viewer.getState() == DataBrowser.DISCARDED) return;  //Async cancel.
    	//ignore.
    }
    
}
