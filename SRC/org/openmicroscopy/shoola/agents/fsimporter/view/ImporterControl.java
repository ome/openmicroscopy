/*
 * org.openmicroscopy.shoola.agents.fsimporter.view.ImporterControl 
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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

import org.openmicroscopy.shoola.agents.fsimporter.ImporterAgent;
import org.openmicroscopy.shoola.agents.fsimporter.chooser.ImporterChooserDialog;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

//Third-party libraries

//Application-internal dependencies

/** 
 * The {@link Importer}'s controller. 
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
class ImporterControl
	implements PropertyChangeListener
{

	/** 
	 * Reference to the {@link Importer} component, which, in this context,
	 * is regarded as the Model.
	 */
	private Importer 		model;
	
	/** Reference to the View. */
	private ImporterUI		view;

	/** The chooser. */
	private ImporterChooserDialog	chooser;
	
	
	/**
	 * Creates a new instance.
	 * The {@link #initialize(FSImportUI) initialize} method 
	 * should be called straight 
	 * after to link this Controller to the other MVC components.
	 * 
	 * @param model  Reference to the {@link Importer} component, which, in 
	 *               this context, is regarded as the Model.
	 *               Mustn't be <code>null</code>.
	 */
	ImporterControl(Importer model)
	{
		if (model == null) throw new NullPointerException("No model.");
		this.model = model;
	}
	
	/** Creates or recycles a <code>FSChooserDialog</code>. */
	void setDialogOnScreen()
	{
		if (chooser == null) {
			Registry reg = ImporterAgent.getRegistry();
			chooser = new ImporterChooserDialog(view, 
					reg.getImageService().getFSFileSystemView());
			chooser.addPropertyChangeListener(this);
			chooser.pack();
		}
		UIUtilities.centerAndShow(chooser);
	}
	
	/**
	 * Links this Controller to its View.
	 * 
	 * @param view   Reference to the View. Mustn't be <code>null</code>.
	 */
	void initialize(ImporterUI view)
	{
		if (view == null) throw new NullPointerException("No view.");
		this.view = view;
	}

	/**
	 * Reacts to property changes.
	 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt)
	{
		String name = evt.getPropertyName();
		if (ImporterChooserDialog.IMPORT_PROPERTY.equals(name)) {
			File[] data = (File[]) evt.getNewValue();
			model.importData(data);
		} else if (ImporterChooserDialog.MONITOR_PROPERTY.equals(name)) {
			File dir = (File) evt.getNewValue();
			model.monitorDirectory(dir);
		}
	}
	
}
