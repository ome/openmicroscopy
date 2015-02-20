/*
 * org.openmicroscopy.shoola.agents.metadata.GroupEditor 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2015 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.metadata;


//Java imports

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.metadata.view.MetadataViewer;
import org.openmicroscopy.shoola.env.data.AdminService;
import org.openmicroscopy.shoola.env.data.DSAccessException;
import org.openmicroscopy.shoola.env.data.DSOutOfServiceException;
import org.openmicroscopy.shoola.env.data.ProcessReport;
import org.openmicroscopy.shoola.env.data.util.SecurityContext;
import org.openmicroscopy.shoola.env.data.views.CallHandle;

import pojos.ExperimenterData;
import pojos.GroupData;

/** 
 * Updates the group.
 * This class calls the <code>updateGroup</code> method in the
 * <code>AdminView</code>.
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
public class GroupEditor 
	extends MetadataLoader
{

    /** Indicates to update the group.*/
    public static final int UPDATE = 0;
    
    /** Indicates to change the default group.*/
    public static final int CHANGE = 1;
    
    private static final String GROUP_PERMISSION_ERROR_MSG = "Could not change group permissions";
    
	/** The group to update. */
	private GroupData group;
	
	/** The permissions level or <code>-1</code>. */
	private int permissions = -1;
	
	/** The index indicating the action to perform.*/
	private int index;
	
	/** Handle to the asynchronous call so that we can cancel it. */
    private CallHandle handle;
    
    /**
     * Creates a new instance.
     * 
     * @param viewer The viewer this data loader is for.
     *               Mustn't be <code>null</code>.
     * @param ctx The security context.
     * @param group The group to update. Mustn't be <code>null</code>.
     * @param permissions The desired permissions level or <code>-1</code>.
     * @param loaderID The identifier of the loader.
     * @param index The indicating what action to perform.
     */
    public GroupEditor(MetadataViewer viewer, SecurityContext ctx,
    		GroupData group, int permissions, int loaderID, int index)
    {
    	super(viewer, ctx, loaderID);
    	if (group == null)
    		throw new IllegalArgumentException("No group to edit.");
    	this.group = group;
    	this.permissions = permissions;
    	this.index = index;
    }
    
    /**
     * Creates a new instance.
     * 
     * @param viewer The viewer this data loader is for.
     *               Mustn't be <code>null</code>.
     * @param ctx The security context.
     * @param group The group to update. Mustn't be <code>null</code>.
     * @param loaderID The identifier of the loader.
     * @param index The indicating what action to perform.
     */
    public GroupEditor(MetadataViewer viewer, SecurityContext ctx,
            GroupData group, int loaderID, int index)
    {
        super(viewer, ctx, loaderID);
        if (group == null)
            throw new IllegalArgumentException("No group to edit.");
        this.group = group;
        this.permissions = -1;
        this.index = index;
    }
    
    /** 
	 * Loads the data.
	 * @see MetadataLoader#cancel()
	 */
	public void load()
	{
	    switch (index) {
	    case UPDATE:
	        handle = adminView.updateGroup(ctx, group, this);
	        break;
	    case CHANGE:
	    	ExperimenterData exp = viewer.getCurrentUser();
	    	if(viewer.getRefObject()!=null && viewer.getRefObject() instanceof ExperimenterData)
	    		exp = (ExperimenterData) viewer.getRefObject();
	        handle = adminView.changeGroup(ctx, group, exp, this);
	    }
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
    	
    	AdminService os = MetadataViewerAgent.getRegistry()
                .getAdminService();
    	
    	switch (index) {
        case UPDATE:
            if (result instanceof GroupData) {
                // result of a GroupData update (name, desc., ...)
                viewer.onAdminUpdated((GroupData) result);
                
                if (permissions>-1) {
                    // if the permissions have also changed,
                    // apply this change in an extra step and
                    // reuse 'this' instance as callback adapter
                    try {
                        os.updateGroupPermissions(ctx, (GroupData)result, permissions, this);
                    } catch (DSOutOfServiceException e) {
                        MetadataViewerAgent.getRegistry().getUserNotifier().notifyError("Error", GROUP_PERMISSION_ERROR_MSG);
                    } catch (DSAccessException e) {
                        MetadataViewerAgent.getRegistry().getUserNotifier().notifyError("Error", GROUP_PERMISSION_ERROR_MSG);
                    }
                }
            }
            else if (result instanceof ProcessReport){
                // result of a permission change - error
                MetadataViewerAgent.getRegistry().getUserNotifier().notifyError("Error", GROUP_PERMISSION_ERROR_MSG);
            }
            else if (result instanceof omero.cmd.OK) {
                // result of a permission change - success
                try {
                    group = os.reloadGroup(ctx, group);
                    viewer.onAdminUpdated(group);
                } catch (DSOutOfServiceException e) {
                    MetadataViewerAgent.getRegistry().getLogger().debug(this, "Couldn't reload group.");
                } catch (DSAccessException e) {
                    MetadataViewerAgent.getRegistry().getLogger().debug(this, "Couldn't reload group.");
                }
            }
            break;
        }
    }
    
}
