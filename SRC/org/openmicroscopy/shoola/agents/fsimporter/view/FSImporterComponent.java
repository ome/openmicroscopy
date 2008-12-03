/*
 * org.openmicroscopy.shoola.agents.fsimporter.view.FSImporterComponent 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.fsimporter.view;


//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * Implements the {@link FSImporter} interface to provide the functionality
 * required of the hierarchy viewer component.
 * This class is the component hub and embeds the component's MVC triad.
 * It manages the component's state machine and fires state change 
 * notifications as appropriate, but delegates actual functionality to the
 * MVC sub-components.
 *
 * @see org.openmicroscopy.shoola.agents.fsimporter.view.FSImporterModel
 * @see org.openmicroscopy.shoola.agents.fsimporter.view.FSImporterUI
 * @see org.openmicroscopy.shoola.agents.fsimporter.view.FSImporterControl
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
class FSImporterComponent 
	implements FSImporter
{

	/** The Model sub-component. */
	private FSImporterModel 	model;
	
	/** The Controller sub-component. */
	private FSImporterControl 	controller;
	
	/** The View sub-component. */
	private FSImporterUI 		view;
	
	/**
	 * Creates a new instance.
	 * The {@link #initialize() initialize} method should be called straigh 
	 * after to complete the MVC set up.
	 * 
	 * @param model The Model sub-component. Mustn't be <code>null</code>.
	 */
	FSImporterComponent(FSImporterModel model)
	{
		if (model == null) throw new NullPointerException("No model.");
		this.model = model;
		controller = new FSImporterControl(this);
		view = new FSImporterUI();
	}

	/** Links up the MVC triad. */
	void initialize()
	{
		controller.initialize(view);
		view.initialize(model, controller);
	}

	/** 
	 * Implemented as specified by the {@link FSImporter} interface.
	 * @see FSImporter#activate()
	 */
	public void activate()
	{
		switch (model.getState()) {
			case NEW:
				view.setDialogOnScreen();
				model.setState(READY);
				break;
			case DISCARDED:
				throw new IllegalStateException(
						"This method can't be invoked in the DISCARDED state.");
		} 
	}

	/** 
	 * Implemented as specified by the {@link FSImporter} interface.
	 * @see FSImporter#discard()
	 */
	public void discard()
	{
		// TODO Auto-generated method stub
		
	}

	/** 
	 * Implemented as specified by the {@link FSImporter} interface.
	 * @see FSImporter#getState()
	 */
	public int getState()
	{
		// TODO Auto-generated method stub
		return 0;
	}
	
}
