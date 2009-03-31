/*
 * org.openmicroscopy.shoola.agents.fsimporter.view.ImporterUI 
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
import org.openmicroscopy.shoola.agents.fsimporter.chooser.ImporterChooserDialog;
import org.openmicroscopy.shoola.env.ui.TopWindow;

/** 
 * The {@link Importer}'s View.
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
class ImporterUI 
	extends TopWindow
{

	/** Reference to the model. */
	private ImporterModel 			model;
	
	/** Reference to the control. */
	private ImporterControl			controller;
	
	/** The chooser. */
	private ImporterChooserDialog	chooser;
	
	/** Creates a new instance. */
	ImporterUI()
	{
		super("");
	}

	/**
     * Links this View to its Controller.
     * 
     * @param model 		The Model.
     * @param controller	The Controller.
     */
	void initialize(ImporterModel model, ImporterControl controller)
	{
		this.model = model;
		this.controller = controller;
	}
	

	
}
