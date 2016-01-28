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
package org.openmicroscopy.shoola.agents.treeviewer;

import java.util.Collection;
import java.util.List;

import org.openmicroscopy.shoola.agents.treeviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageSet;
import omero.gateway.SecurityContext;
import org.openmicroscopy.shoola.env.data.views.CallHandle;

import omero.gateway.model.GroupData;

/** 
 * Loads the groups in the system.
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
public class AdminLoader 
	extends DataBrowserLoader
{

    /** Handle to the asynchronous call so that we can cancel it. */
    private CallHandle 	handle;
    
    /** The group to attach the elements to or <code>null</code>. */
    private TreeImageSet group;
    
    /**
     * Creates a new instance.
     * 
     * @param viewer Reference to the Model. Mustn't be <code>null</code>.
     * @param ctx The security context.
     * @param group  The node to attach the result to or <code>null</code>.
     */
	public AdminLoader(Browser viewer, SecurityContext ctx, TreeImageSet group)
	{
		super(viewer, ctx);
		this.group = group;
	}
	
	/**
     * Retrieves the data.
     * @see DataBrowserLoader#load()
     */
    public void load()
    {
    	long id = -1;
    	if (group != null) {
    		GroupData g = (GroupData) group.getUserObject();
    		id = g.getId();
    	}
    	handle = adminView.loadExperimenterGroups(ctx, id, this);
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
        if (viewer.getState() == Browser.DISCARDED) return;  //Async cancel.
        if (group == null)
        	viewer.setGroups((Collection) result, null);
        else viewer.setExperimenters(group, (List) result);
    }
    
}
