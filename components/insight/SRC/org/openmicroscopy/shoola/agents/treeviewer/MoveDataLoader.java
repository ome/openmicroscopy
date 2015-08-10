/*
 * org.openmicroscopy.shoola.agents.treeviewer.MoveDataLoader 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2012 University of Dundee & Open Microscopy Environment.
 *  All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
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
package org.openmicroscopy.shoola.agents.treeviewer;

//Java imports
import java.util.Collection;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.treeviewer.util.MoveGroupSelectionDialog;
import org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewer;
import omero.gateway.SecurityContext;
import org.openmicroscopy.shoola.env.data.views.CallHandle;

/** 
 * Loads the data for the specified user.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since Beta4.4
 */
public class MoveDataLoader
	extends DataTreeViewerLoader
{

	/** The dialog where to add the loaded data.*/
	private MoveGroupSelectionDialog dialog;
	
    /** Handle to the asynchronous call so that we can cancel it. */
    private CallHandle 	handle;
    
    /** The type.*/
    private Class type;
    
    /** The identifier of the user.*/
    private long userID;
    
    /**
     * Creates a new instance.
     * 
     * @param viewer Reference to the Model. Mustn't be <code>null</code>.
     * @param ctx The security context.
     * @param type The root node type.
     * @param dialog The component where to display the result.
     * @param userID The id of the user to move the data to.
     */
	public MoveDataLoader(TreeViewer viewer, SecurityContext ctx,
			Class type, MoveGroupSelectionDialog dialog, long userID)
	{
		super(viewer, ctx);
		if (dialog == null)
			throw new IllegalArgumentException("No dialog set.");
		this.dialog = dialog;
		this.type = type;
		this.userID = userID;
	}
	
	/**
     * Retrieves the data.
     * @see DataTreeViewerLoader#load()
     */
    public void load()
    {
    	handle = dmView.loadContainerHierarchy(ctx, type, null, false, userID,
    			this);	
    }

    /**
     * Cancels the data loading.
     * @see DataTreeViewerLoader#cancel()
     */
    public void cancel() { handle.cancel(); }

    /**
     * Feeds the result back to the viewer.
     * @see DataTreeViewerLoader#handleResult(Object)
     */
    public void handleResult(Object result)
    {
        if (viewer.getState() == Browser.DISCARDED) return;  //Async cancel.
        if (dialog.getStatus() != MoveGroupSelectionDialog.CANCEL)
        	dialog.setTargets((Collection) result);
    }

}
