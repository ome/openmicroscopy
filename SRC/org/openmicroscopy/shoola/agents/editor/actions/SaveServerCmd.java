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

import java.io.File;

import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.editor.view.Editor;
import org.openmicroscopy.shoola.util.filter.file.EditorFileFilter;

/** 
 * 
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
	
	
	SaveServerCmd(Editor model) 
	{
		this.model = model;
	}

	/**
	 * Implemented as specified by the {@link ActionCmd} interface. 
	 * Saves file to the OMERO.server
	 */
	public void execute() {
		
		String fileName = JOptionPane.showInputDialog(null, 
				"Please enter a name for saving the file to the server:", 
				"Save to server", JOptionPane.QUESTION_MESSAGE);
		if (fileName != null) 
		{
			EditorFileFilter editor = new EditorFileFilter();
			if (! editor.accept(new File(fileName))) {
				fileName = fileName + "." + editor.getExtension();
			}
			model.saveFileServer(fileName);
		}
	}


	
}