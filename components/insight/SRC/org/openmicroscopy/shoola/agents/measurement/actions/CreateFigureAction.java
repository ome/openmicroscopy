/*
 * org.openmicroscopy.shoola.agents.measurement.actions.createFigureAction 
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
package org.openmicroscopy.shoola.agents.measurement.actions;

//Java imports
import java.awt.event.ActionEvent;
import javax.swing.Action;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.measurement.view.MeasurementViewer;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * Action to create a single or several figures.
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
public class CreateFigureAction
	extends MeasurementViewerAction
{

	/** Show the pixels in create single. */
	private static final String NAME_CREATE_SINGLE = "Create Single ROI";

	/** Show the pixels in create multiple. */
	private static final String NAME_CREATE_MULTIPLE = 
		"Create Multiple ROIs";

	/** The description of the action for create single. */
	private static final String DESCRIPTION_CREATE_SINGLE = 
		"Create a single ROI and go back to selection tool.";

	/** The description of the action for create multiple. */
	private static final String DESCRIPTION_CREATE_MULTIPLE = 
		"Create multiple ROIs, user needs to select selection tool by hand.";

	/** Create a single figure. */
	private boolean				createSingleFigure; 

	/** 
	 * Sets the action enabled if the ROIs are server ROI. 
	 * @see MeasurementViewerAction#onStateChange()
	 */
	protected void onStateChange()
	{
		if (model.isHCSData()) setEnabled(false);
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param model 	The model. Mustn't be <code>null</code>.
	 * @param createSingleFigure Passed <code>true</code> to set the tool to 
	 * 						create a single figure at a time,
	 * 						<code>false</code> otherwise.
	 */
	public CreateFigureAction(MeasurementViewer model, 
			boolean createSingleFigure)
	{
		super(model);
		this.createSingleFigure = createSingleFigure;
		if (createSingleFigure)
		{
			name = NAME_CREATE_SINGLE;
			putValue(Action.NAME, NAME_CREATE_SINGLE);
			putValue(Action.SHORT_DESCRIPTION, 
					UIUtilities.formatToolTipText(DESCRIPTION_CREATE_SINGLE));
		}
		else
		{
			name = NAME_CREATE_MULTIPLE;
			putValue(Action.NAME, NAME_CREATE_MULTIPLE);
			putValue(Action.SHORT_DESCRIPTION, 
					UIUtilities.formatToolTipText(DESCRIPTION_CREATE_MULTIPLE));
		}
	}

	/** 
	 * Creates figures.
	 * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) 
	{ 
		model.createSingleFigure(createSingleFigure); 
	}
	
}