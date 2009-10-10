/*
 * org.openmicroscopy.shoola.agents.dataBrowser.DatasetsLoader 
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

import java.util.Collection;

import org.openmicroscopy.shoola.agents.dataBrowser.view.DataBrowser;
import org.openmicroscopy.shoola.agents.metadata.MetadataViewerAgent;
import org.openmicroscopy.shoola.env.data.views.CallHandle;

import pojos.DatasetData;
import pojos.TagAnnotationData;

//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * Loads the datasets owned by the user.
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
public class DatasetsLoader
	extends DataBrowserLoader
{

    /** Handle to the asynchronous call so that we can cancel it. */
    private CallHandle	handle;
    
    /**
     * Creates a new instance.
     * 
     * @param viewer The viewer this data loader is for.
     *               Mustn't be <code>null</code>.
     */
	public DatasetsLoader(DataBrowser viewer)
	{
		super(viewer);
	}

	/** 
	 * Cancels the data loading. 
	 * @see DataBrowserLoader#cancel()
	 */
	public void cancel() { handle.cancel(); }

	/** Overridden so the status is not displayed. */
	public void onEnd() {}
	
	/** 
	 * Loads the tags for the specified nodes.
	 * @see DataBrowserLoader#load()
	 */
	public void load()
	{
		long userID = MetadataViewerAgent.getUserDetails().getId();
		handle = dmView.loadContainerHierarchy(DatasetData.class, null, 
				false, userID, this);	
	}
	
	/**
     * Feeds the result back to the viewer.
     * @see DataBrowserLoader#handleResult(Object)
     */
    public void handleResult(Object result) 
    {
    	if (viewer.getState() == DataBrowser.DISCARDED) return;  //Async cancel.
    	viewer.setExistingDatasets((Collection) result);
    }
    
}
