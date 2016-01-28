/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2010 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.metadata;

import java.util.Map;

import org.openmicroscopy.shoola.agents.metadata.view.MetadataViewer;
import org.openmicroscopy.shoola.env.data.login.UserCredentials;
import omero.gateway.SecurityContext;
import org.openmicroscopy.shoola.env.data.views.CallHandle;
import omero.gateway.model.ExperimenterData;
import omero.gateway.model.GroupData;

/** 
 * Updates the specified experimenters.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since 3.0-Beta4
 */
public class AdminEditor
	extends MetadataLoader
{

	/** The experimenters to handle. */
	private Map<ExperimenterData, UserCredentials> details;
	
	/** The group to handle. */
	private GroupData group;
	
	/** Handle to the asynchronous call so that we can cancel it. */
    private CallHandle	handle;
    
    /**
     * Creates a new instance.
     * 
     * @param viewer  The viewer this data loader is for.
     *                Mustn't be <code>null</code>.
     * @param ctx The security context.
     * @param group   The group to handle.
     * @param details The experimenters to update. Mustn't be <code>null</code>.
     * @param loaderID The identifier of the loader.
     */
    public AdminEditor(MetadataViewer viewer, SecurityContext ctx,
    		GroupData group, Map<ExperimenterData, UserCredentials> details,
    		int loaderID)
    {
    	super(viewer, ctx, null, loaderID);
    	if (details == null)
    		throw new IllegalArgumentException("No eperimenters to update.");
    	this.details = details;
    	this.group = group;
    }
    
    /** 
	 * Loads the data.
	 * @see MetadataLoader#cancel()
	 */
	public void load()
	{
		handle = adminView.updateExperimenters(ctx, group, details, this);
	}
	
	/** 
	 * Cancels the data loading. 
	 * @see MetadataLoader#cancel()
	 */
	public void cancel() { handle.cancel(); }

	/**
     * Feeds the result back to the viewer.
     * @see MetadataLoader#handleResult(Object)
     */
    public void handleResult(Object result) 
    {
    	if (viewer.getState() == MetadataViewer.DISCARDED) return;  //Async cancel.
    	viewer.onAdminUpdated(result);
    }
    
}
