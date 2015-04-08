/*
* org.openmicroscopy.shoola.agents.measurement.WorkflowSaver
*
*------------------------------------------------------------------------------
*  Copyright (C) 2006-2009 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.measurement;


//Java imports
import java.util.List;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.measurement.view.MeasurementViewer;
import omero.gateway.SecurityContext;
import org.openmicroscopy.shoola.env.data.views.CallHandle;
import pojos.WorkflowData;

/**
 * Saves the workflow.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class WorkflowSaver
	extends MeasurementViewerLoader
{

	/** The id of the user. */
	private long		userID;
	
	/** The list of workflows to save. */
	private List<WorkflowData> workflows;
	
	/** Handle to the asynchronous call so that we can cancel it. */
    private CallHandle  handle;
   
    /**
     * Creates a new instance. 
     * 
     * @param viewer	The viewer this data loader is for.
     * @param ctx The security context.
     * @param workflows The list of workflows to save.
     * @param userID	The id of the user.
     */
	public WorkflowSaver(MeasurementViewer viewer, SecurityContext ctx,
			List<WorkflowData> workflows, long userID)
	{
		super(viewer, ctx);
		this.userID = userID;
		this.workflows = workflows;
	}
	
	/**
     * Saves the workflows.
     * @see MeasurementViewerLoader#load()
     */
    public void load()
    {
    	handle = idView.storeWorkflows(ctx, workflows, userID, this);
    }
    
    /**
     * Cancels the data loading.
     * @see MeasurementViewerLoader#cancel()
     */
    public void cancel() { handle.cancel(); }
    
    /**
     * Feeds the result back to the viewer.
     * @see MeasurementViewerLoader#handleResult(Object)
     */
    public void handleResult(Object result)
    {
    	if (viewer.getState() == MeasurementViewer.DISCARDED) return;  //Async cancel.
    }

}