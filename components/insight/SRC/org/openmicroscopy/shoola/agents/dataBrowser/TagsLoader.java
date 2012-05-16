/*
 * org.openmicroscopy.shoola.agents.dataBrowser.TagsLoader 
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
import java.util.Collection;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.dataBrowser.view.DataBrowser;
import org.openmicroscopy.shoola.env.data.util.SecurityContext;
import org.openmicroscopy.shoola.env.data.views.CallHandle;
import pojos.ExperimenterData;
import pojos.GroupData;
import pojos.TagAnnotationData;

/** 
 * Loads the available tags owned by the currently logged in user.
 * This class calls the <code>loadExistingAnnotations</code> method in the
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
public class TagsLoader 	
	extends DataBrowserLoader
{
    
    /** Handle to the asynchronous call so that we can cancel it. */
    private CallHandle				handle;
    
    /**
     * Creates a new instance.
     * 
     * @param viewer The viewer this data loader is for.
     *               Mustn't be <code>null</code>.
     * @param ctx The security context.
     */
	public TagsLoader(DataBrowser viewer, SecurityContext ctx)
	{
		super(viewer, ctx);
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
		ExperimenterData exp = DataBrowserAgent.getUserDetails();
		long userID = exp.getId();//viewer.getUserID();
		long groupID = -1;
		switch (exp.getPermissions().getPermissionsLevel()) {
			case GroupData.PERMISSIONS_GROUP_READ_LINK:
				groupID = exp.getDefaultGroup().getId();
				userID = -1;
				break;
			case GroupData.PERMISSIONS_PUBLIC_READ_WRITE:
				userID = -1;
		}
		
		handle = mhView.loadExistingAnnotations(ctx, TagAnnotationData.class,
												userID, groupID, this);
	}
	
	/**
     * Feeds the result back to the viewer.
     * @see DataBrowserLoader#handleResult(Object)
     */
    public void handleResult(Object result) 
    {
    	if (viewer.getState() == DataBrowser.DISCARDED) return;  //Async cancel.
    	viewer.setExistingTags((Collection) result);
    }
    
}
