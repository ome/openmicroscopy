 /*
 * org.openmicroscopy.shoola.agents.editor.actions.SaveFileLocallyAction 
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
package org.openmicroscopy.shoola.agents.editor.actions;

//Java imports
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.filechooser.FileFilter;

//Third-party libraries

//Application-internal dependencies

import org.openmicroscopy.shoola.agents.editor.IconManager;
import org.openmicroscopy.shoola.agents.editor.model.XMLexport;
import org.openmicroscopy.shoola.agents.editor.view.Editor;
import org.openmicroscopy.shoola.util.filter.file.EditorFileFilter;
import org.openmicroscopy.shoola.util.filter.file.HTMLFilter;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.filechooser.FileChooser;

/** 
 * This Action allows users to choose a local 
 * location to save an OMERO.editor file. 
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class SaveFileLocallyAction 
	extends EditorAction
	implements PropertyChangeListener
{

	/** The description of the action. */
	private static final String 	NAME = "Open File";

	/** The description of the action. */
	private static final String 	DESCRIPTION = 
		"Open a Local File on your computer";

	/** Collection of supported file formats. */
	private List<FileFilter>		filters;

	/** Creates a new instance.
	 * 
	 * @param model Reference to the Model. Mustn't be <code>null</code>.
	 */
	public SaveFileLocallyAction(Editor model)
	{
		super(model);
		setEnabled(true);
		setName(NAME);
		setDescription(DESCRIPTION);
		setIcon(IconManager.SAVE_ICON);

		filters = new ArrayList<FileFilter>();
		filters.add(new EditorFileFilter());
		filters.add(new HTMLFilter());
	}

	/**
	 * Brings up on screen the {@link FileChooser}.
	 * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) 
	{
		FileChooser chooser = new FileChooser(null, FileChooser.SAVE, 
				"Save File", "Choose a location and name to save the file", 
				filters);
		File startDir = UIUtilities.getDefaultFolder();
		if (startDir != null)
			chooser.setCurrentDirectory(startDir);
		chooser.addPropertyChangeListener(
				FileChooser.APPROVE_SELECTION_PROPERTY, this);
		UIUtilities.centerAndShow(chooser);
	}

	/**
	 * Responds to the user choosing a file to save.
	 * Calls {@link XMLexport#export(javax.swing.tree.TreeModel, File)}
	 * 
	 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt) 
	{
		String name = evt.getPropertyName();
		if (FileChooser.APPROVE_SELECTION_PROPERTY.equals(name)) {
			File f = (File) evt.getNewValue();

			// if file exists, get user to confirm. Otherwise exit! 
			if (f.exists()) {
				String title = "File Exists";
				String message = "File Exists.\nOverwrite Existing File?";
				if (! org.openmicroscopy.shoola.agents.editor.uiComponents.
						UIUtilities.showConfirmDialog(title, message)) {
					return;
				}
			}

			XMLexport xmlExport = new XMLexport();
			xmlExport.export(model.getBrowser().getTreeModel(), f);
		}
	}
	
}
