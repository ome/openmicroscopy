 /*
 * org.openmicroscopy.shoola.agents.editor.actions.SaveNewCmd 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2009 University of Dundee. All rights reserved.
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
import org.openmicroscopy.shoola.util.ui.MessageBox;

/** 
 * This command has functionality for Choosing where to save a new file, 
 * and calling the Save command, E.g. Save to server (if available) or
 * save locally. 
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class SaveNewCmd 
	implements ActionCmd
{

	/** Reference to the model */
	private Editor 					model;
	
	/**
	 * Creates an instance.
	 * 
	 * @param model		The {@link Editor} model for saving. 
	 */
	public SaveNewCmd(Editor model) 
	{
		if (model == null)
			throw new IllegalArgumentException("No model.");
		this.model = model;
	}
	
	/**
	 * Implemented as specified by the {@link ActionCmd} interface. 
	 * Save new file, offer option to either save locally or save back to 
	 * server. 
	 * @see ActionCmd#execute()
	 */
	public void execute()
	{
		
		ActionCmd save;
		// if server available, ask where to save
		if (EditorAgent.isServerAvailable()) {
			//Custom button text
			
			JFrame f = EditorAgent.getRegistry().getTaskBar().getFrame();
			MessageBox msg = new MessageBox(f, "Save As...", 
					"Would you like to save this file locally, or save it to " +
				    "the OMERO.server?");
			msg.setYesText("Save to Server");
			msg.setNoText("Save locally");
			msg.addCancelButton();
			
			int option = msg.centerMsgBox();
			if (option == MessageBox.YES_OPTION) {
				save = new SaveServerCmd(model);
				save.execute();
			} else if (option == MessageBox.NO_OPTION) {
				save = new SaveLocallyCmd(model);
				save.execute();
			}
		} else {
			// server not available, Save locally
			save = new SaveLocallyCmd(model);
			save.execute();
		}
	}

}
