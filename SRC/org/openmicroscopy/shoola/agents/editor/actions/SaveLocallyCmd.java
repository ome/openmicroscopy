 /*
 * org.openmicroscopy.shoola.agents.editor.actions.SaveLocallyCmd 
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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.filechooser.FileFilter;

//Third-party libraries

//Application-internal dependencies

import org.openmicroscopy.shoola.agents.editor.EditorAgent;
import org.openmicroscopy.shoola.agents.editor.view.Editor;
import org.openmicroscopy.shoola.util.filter.file.CustomizedFileFilter;
import org.openmicroscopy.shoola.util.filter.file.EditorFileFilter;
import org.openmicroscopy.shoola.util.ui.MessageBox;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.filechooser.FileChooser;


/** 
 * Allows users to choose a local file to save the currently-edit file to. 
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class SaveLocallyCmd 
	implements ActionCmd,
	PropertyChangeListener
{
	
	/** Reference to the model */
	private Editor 					model;
	
	/** 
	 * Collection of supported file formats. 
	 * These should be instances of {@link CustomizedFileFilter}, so that
	 * the file extension can be retrieved. 
	 */
	private List<FileFilter>		filters;
	
	/**
	 * Creates an instance.
	 * 
	 * @param model		The {@link Editor} model for saving. 
	 */
	SaveLocallyCmd(Editor model) 
	{
		if (model == null)
			throw new IllegalArgumentException("No model.");
		this.model = model;
		filters = new ArrayList<FileFilter>();
		filters.add(new EditorFileFilter());
	}

	/**
	 * Implemented as specified by the {@link ActionCmd} interface. 
	 * Opens a file chooser for users to choose a local file to save their 
	 * file to. 
	 * @see ActionCmd#execute()
	 */
	public void execute()
	{
		
		FileChooser chooser = new FileChooser(null, FileChooser.SAVE, 
				"Save File", "Choose a location and name to save the file", 
				filters);
		File startDir = UIUtilities.getDefaultFolder();
		if (startDir != null)
			chooser.setCurrentDirectory(startDir);
		// set default name according to the file title 
		String text = model.getEditorTitle();
		if (model.isExperiment()) text += Editor.EXPERIMENT_EXTENSION;
		// this method doesn't truncate name
		chooser.setSelectedFile(new File(text));	
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
		if (! (evt.getSource() instanceof FileChooser)) return;
		FileChooser fileChooser = (FileChooser)evt.getSource();
		
		String name = evt.getPropertyName();
		if (FileChooser.APPROVE_SELECTION_PROPERTY.equals(name)) {
			File file = (File) evt.getNewValue();
			
			FileFilter filter = fileChooser.getSelectedFilter();
			String filterExtension = "";
			if (filter instanceof CustomizedFileFilter) {
				filterExtension = 
					((CustomizedFileFilter) filter).getExtension();
			}
			
			// check if file is allowed. If not, add extension. 
			if (!filter.accept(file)) {
				String filePath = file.getAbsolutePath();
				filePath = filePath + "." + filterExtension;
				file = new File(filePath);
			}
				
			// if file exists, get user to confirm. Otherwise exit! 
			if (file.exists()) {
				String title = "File Exists";
				String message = "File Exists. Overwrite Existing File?";
				JFrame f = EditorAgent.getRegistry().getTaskBar().getFrame();
				MessageBox msg = new MessageBox(f, title, message);
				int option = msg.centerMsgBox();
				if (option != MessageBox.YES_OPTION) {
					return;
				}
			}
			
			model.saveFileLocally(file);
		}
	}
	
}
