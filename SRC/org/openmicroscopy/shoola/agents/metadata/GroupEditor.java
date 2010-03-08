/*
 * org.openmicroscopy.shoola.agents.metadata.GroupEditor 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2010 University of Dundee. All rights reserved.
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
import org.openmicroscopy.shoola.env.data.views.CallHandle;
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

	/** The group to update. */
	private GroupData	group;
	
	/** The permissions level or <code>-1</code>. */
	private int 		permissions;
	
	/** Handle to the asynchronous call so that we can cancel it. */
    private CallHandle	handle;
    
    /**
     * Creates a new instance.
     * 
     * @param viewer The viewer this data loader is for.
     *               Mustn't be <code>null</code>.
     * @param group	 The group to update. Mustn't be <code>null</code>.
     * @param permissions The desired permissions level or <code>-1</code>.
     */
    public GroupEditor(MetadataViewer viewer, GroupData group, int permissions)
    {
    	super(viewer, null);
    	if (group == null)
    		throw new IllegalArgumentException("No group to edit.");
    	this.group = group;
    	this.permissions = permissions;
    }
    
    /** 
	 * Loads the data.
	 * @see MetadataLoader#cancel()
	 */
	public void load()
	{
		handle = adminView.updateGroup(group, permissions, this);
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
    	viewer.onAdminUpdated((GroupData) result);
    }
    
}
