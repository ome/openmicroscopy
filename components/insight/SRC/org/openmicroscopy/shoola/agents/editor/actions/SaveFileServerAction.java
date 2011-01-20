 /*
 * org.openmicroscopy.shoola.agents.editor.actions.SaveFileServerAction 
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

/** 
 * Action allows users to Save a file to the server. 
 * Delegates this functionality to the {@link SaveServerCmd} command.
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class SaveFileServerAction 
	extends EditorAction
{
	
	/** The description of the action. */
	private static final String 	NAME = "Save As (to server)...";
	
	/** The description of the action. */
	private static final String 	DESCRIPTION = 
		"Save As a new file to the OMERO.server.";
	
	/** 
	 * Implement this method to disable the Save Action if no file is open,
	 * or if the server is not available.
	 */
	protected void onStateChange()
	{
		int state = model.getState();
		setEnabled(state == Editor.READY);
		
		// if server not available, disable
		//boolean serverAvailable = EditorAgent.isServerAvailable();
		//if (!serverAvailable) setEnabled(false);
	}
	
	/** 
	 * Creates a new instance.
	 * 
	 * @param model Reference to the Model. Mustn't be <code>null</code>.
	 */
	public SaveFileServerAction(Editor model)
	{
		super(model);
		setEnabled(true);
		setName(NAME);
		setDescription(DESCRIPTION);
		setIcon(IconManager.SAVE_AS_ICON);
		// refresh enabled status
		onStateChange();
	}
	
	/**
	 * Saves file to server.
	 * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) 
	{
		ActionCmd save = new SaveServerCmd(model);
		save.execute();
	}

}
