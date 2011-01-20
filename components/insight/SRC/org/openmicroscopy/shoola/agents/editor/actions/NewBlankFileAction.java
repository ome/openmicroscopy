 /*
 * org.openmicroscopy.shoola.agents.editor.actions.NewBlankFileAction 
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

//Third-party libraries

//Application-internal dependencies

import org.openmicroscopy.shoola.agents.editor.IconManager;
import org.openmicroscopy.shoola.agents.editor.view.Editor;
import org.openmicroscopy.shoola.util.ui.filechooser.FileChooser;

/** 
 * This action opens a new blank file, for the user to start editing. 
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class NewBlankFileAction
extends EditorAction
{

	/** The description of the action. */
	private static final String 	NAME = "New Blank File";
	
	 /** The description of the action. */
	private static final String 	DESCRIPTION = "Create a New Blank File";
	
	/** Creates a new instance.
	 * 
	 * @param model Reference to the Model. Mustn't be <code>null</code>.
	 */
	public NewBlankFileAction(Editor model)
	{
	   super(model);
	   setEnabled(true);
	   setName(NAME);
	   setDescription(DESCRIPTION);
	   setIcon(IconManager.NEW_FILE_ICON);
	}
	
	/**
	* Brings up on screen the {@link FileChooser}.
	* @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
	*/
	public void actionPerformed(ActionEvent e) 
	{
	   model.newBlankFile();
	}
}
