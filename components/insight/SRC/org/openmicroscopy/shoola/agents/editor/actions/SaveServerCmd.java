 /*
 * org.openmicroscopy.shoola.agents.editor.actions.SaveServerCmd 
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
import javax.swing.JFrame;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.editor.EditorAgent;
import org.openmicroscopy.shoola.agents.editor.view.Editor;
import org.openmicroscopy.shoola.util.filter.file.EditorFileFilter;
import org.openmicroscopy.shoola.util.ui.InputDialog;

/** 
 * Saves the current file as an XML file to the OMERO.server. 
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class SaveServerCmd 
	implements ActionCmd
{
	
	/** Reference to the model */
	private Editor 					model;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param model Reference to the model. Mustn't be <code>null</code>.
	 */
	SaveServerCmd(Editor model) 
	{
		if (model == null)
			throw new IllegalArgumentException("No model.");
		this.model = model;
	}

	/**
	 * Implemented as specified by the {@link ActionCmd} interface. 
	 * Saves file to the OMERO.server
	 */
	public void execute()
	{
		JFrame f = EditorAgent.getRegistry().getTaskBar().getFrame();
		InputDialog dialog = new InputDialog(f, 
				"Save to server: Enter file name", "");
		String text = model.getEditorTitle();
		if (model.isExperiment()) text += Editor.EXPERIMENT_EXTENSION;
		dialog.setText(text);	// default file name
		
		int option = dialog.centerMsgBox();
		if (option == InputDialog.SAVE) {
			String fileName = dialog.getText();
			if (fileName == null || fileName.length() == 0) {
				// try again!
				execute();
			} else {
				EditorFileFilter editor = new EditorFileFilter();
				if (!editor.accept(fileName)) 
					fileName = fileName + "." + editor.getExtension();
				model.saveFileServer(fileName);
			}
		}
	}

}