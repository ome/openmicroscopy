/*
* org.openmicroscopy.shoola.agents.measurement.actions.CreateWorkflowAction
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
package org.openmicroscopy.shoola.agents.measurement.actions;



//Java imports
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JComboBox;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.measurement.view.MeasurementViewer;

/**
 *
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
public class WorkflowAction
	extends MeasurementViewerAction
{

	/** Create a new workflow string. */
	private String CREATEWORKFLOW = "Create Workflow";
	
	/** If <code>true</code> create a new workflow when activated. */
	private boolean createWorkflow;
		
	/** 
	 * Create a workflow action, this will either allow the user to select the
	 * current workflow or create a new one.
	 * @param model
	 * @param createWorkflow
	 */
	public WorkflowAction(MeasurementViewer model, boolean createWorkflow)
	{
		super(model);
		this.createWorkflow = createWorkflow;
		if(this.createWorkflow)
			this.name = CREATEWORKFLOW;
	}

	/** 
     * Implemented by sub-classes.
     * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
     */
	public void actionPerformed(ActionEvent e) 
	{
		if(e.getActionCommand()==CREATEWORKFLOW)
			model.createWorkflow();
		else
		{
			if(e.getActionCommand()=="comboBoxChanged")
			{
				JComboBox comboBox = (JComboBox)e.getSource();
				model.setWorkflow((String)comboBox.getSelectedItem());
				
			}
			else 			
				model.setWorkflow(e.getActionCommand());
		}
	}
    
	/** 
     * Implemented by sub-classes.
     * @see MeasurementViewerAction#onStateChange()
     */
	protected void onStateChange() 
    {
    	
    }

	/** 
     * Reacts to state changes in the {@link MeasurementViewer}.
     * @see ChangeListener#stateChanged(ChangeEvent)
     */
	public void stateChanged(ChangeEvent e)
	{
		//setEnabled(model.getState() == MeasurementViewer.READY);
		onStateChange();
	}
}
