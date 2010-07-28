/*
 * org.openmicroscopy.shoola.agents.measurement.util.workflow.WorkflowDialog 
 *
  *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.measurement.util.workflow;


//Java imports
import java.util.List;

//Third-party libraries

//Application-internal dependencies
import pojos.WorkflowData;

/** 
 * The create dialog controller, allows the creation and deletion of 
 * workflows.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class CreateWorkflowDialog
{
	/** The Model for the UI, manages the workflows in the system. */
	private WorkflowModel workflowModel;

	/** The view of the model. */
	private WorkflowView view;
	
	/** Has the call been canceled, <code>true</code> */
	private boolean cancelled;
	
	/**
	 * Instantiate the workflow dialog with the list of workflows.
	 * @param workflowList See above.
	 */
	public CreateWorkflowDialog(List<WorkflowData> workflowList)
	{
		init(workflowList);
		buildUI(workflowModel);
	}
	
	/**
	 * Initialise the variables in the model from the provided workflows.
	 * @param workflowList See above.
	 */
	private void init(List<WorkflowData> workflowList)
	{
		workflowModel = new WorkflowModel(workflowList);
		cancelled = false;
	}
	
	/**
	 * Create the UI based on the model.
	 * @param model See above.
	 */
	private void buildUI(WorkflowModel model)
	{
		view = new WorkflowView(this, model);
	}
	
	/**
	 * Show the dialog and return the new workflows when complete, returns
	 * <code>null</code> if cancelled. 
	 * @return See above.
	 */
	public List<WorkflowData> show()
	{
		view.show();
		if(cancelled)
			return null;
		return workflowModel.getWorkflowList(); 
	}
	
	/**
	 * If cancelled, the cancelled flag is set to <code>true</code>.
	 */
	public void cancel()
	{
		cancelled = true;
	}
}
