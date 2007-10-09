/*
 * org.openmicroscopy.shoola.agents.measurement.util.actions.ROIStatsAction 
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
package org.openmicroscopy.shoola.agents.measurement.util.actions;



//Java imports

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.measurement.util.roitable.ROIActionController;
import org.openmicroscopy.shoola.agents.measurement.util.roitable.ROIActionController.StatsActionType;

/** 
 * 
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
public class ROIStatsAction
extends AbstractAction
{
	/** action for this 'action' to perform. */
	protected StatsActionType 				action;
	
	/** The controller for the event. */
	protected ROIActionController 		controller;

	/**
	 * Create instance;
	 *@param controller the reference to the action controller.
	 */
	public ROIStatsAction(ROIActionController controller, StatsActionType action)
	{
		this.controller = controller;
		this.action = action;
		switch(action)
		{
			case CALCULATE:
				this.
				setName("Calculate ROI Statistics");
				break;
		}
	}

	/**
	 * Set the anem of the aciton.
	 * @param name see above.
	 */
	public void setName(String name)
	{
		putValue(Action.NAME, name);
		putValue(Action.SHORT_DESCRIPTION, name);
	}

	/**
	 * Returns the name of the action.
	 * 
	 * @return See above.
	 */
    public String getName()
    { 
    	return (String) getValue(Action.NAME);  
    } 
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e)
	{
		switch(action)
		{
			case CALCULATE:
				this.
				setName("Calculate");
				controller.calculateStats();
				break;
		}
	}	
}


