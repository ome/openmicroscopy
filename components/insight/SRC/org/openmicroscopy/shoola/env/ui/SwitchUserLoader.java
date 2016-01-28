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
package org.openmicroscopy.shoola.env.ui;

import org.openmicroscopy.shoola.agents.dataBrowser.DataBrowserLoader;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.events.DSCallFeedbackEvent;
import org.openmicroscopy.shoola.env.data.events.UserGroupSwitched;
import omero.gateway.SecurityContext;
import org.openmicroscopy.shoola.env.data.views.CallHandle;
import omero.gateway.model.ExperimenterData;

/** 
 * Saves data and switches groups.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since 3.0-Beta4
 */
public class SwitchUserLoader
	extends UserNotifierLoader
{

	/** The experimenter to handle. */
	private ExperimenterData experimenter;
	
	/** The identifier of the group. */
	private long groupID;
	
	/** Handle to the asynchronous call so that we can cancel it. */
    private CallHandle	handle;
    
    /**
     * Creates a new instance.
     * 
     * @param viewer		The viewer this data loader is for.
     *               		Mustn't be <code>null</code>.
     * @param registry		Convenience reference for subclasses.
     * @param ctx The security context.
     * @param experimenter  The experimenter to handle.
     * @param groupID		The identifier of the group.
     */
	public SwitchUserLoader(UserNotifier viewer, Registry registry, 
			SecurityContext ctx, ExperimenterData experimenter, long groupID)
	{
		super(viewer, registry, ctx, null);
		this.experimenter = experimenter;
		this.groupID = groupID;
	}
	
	/**
     * Saves data before switching user.
     * @see UserNotifierLoader#load()
     */
    public void load()
    {
    	handle = dhView.switchUserGroup(ctx, experimenter, groupID, this);
    }
    
    /**
     * Cancels the ongoing data retrieval.
     * @see UserNotifierLoader#cancel()
     */
    public void cancel() { handle.cancel(); }
    
    /** 
     * Feeds the results back to the viewer, as they arrive. 
     * @see DataBrowserLoader#update(DSCallFeedbackEvent)
     */
    public void update(DSCallFeedbackEvent fe) 
    {
    	Object result = fe.getPartialResult();
    	if (result != null) {
    		viewer.setStatus(result);
    		if (result instanceof ExperimenterData) {
    			registry.getEventBus().post(new UserGroupSwitched(true));
    		} 
    	}
    }
    
    /**
     * Does nothing as the asynchronous call returns <code>null</code>.
     * @see UserNotifierLoader#handleNullResult()
     */
    public void handleNullResult() {}
	
}
