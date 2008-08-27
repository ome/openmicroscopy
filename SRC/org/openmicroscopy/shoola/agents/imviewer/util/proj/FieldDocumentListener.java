/*
 * org.openmicroscopy.shoola.agents.imviewer.util.proj.FieldDocumentListener 
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
package org.openmicroscopy.shoola.agents.imviewer.util.proj;


//Java imports
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

//Third-party libraries

//Application-internal dependencies

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta3
 */
class FieldDocumentListener
	implements DocumentListener
{

	/** The action command linked to the listener. */
	private int 				command;
	
	/** Reference to the model. */
	private ProjectionDialog 	model;
	
	/** Updates the field corresponding to the command. */
	private void updateUI()
	{
		switch (command) {
			case ProjectionDialogControl.START_Z:
				model.setStartZ();
				break;
			case ProjectionDialogControl.END_Z:
				model.setEndZ();
		}
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param command 	The action command to set.
	 * @param model		Reference to the model.
	 */
	FieldDocumentListener(int command, ProjectionDialog model)
	{
		if (model == null)
			throw new IllegalArgumentException("No model specified.");
		this.model = model;
		this.command = command;
	}
	
	/**
	 * Updates the field corresponding to the command.
	 * @see DocumentListener#insertUpdate(DocumentEvent)
	 */
	public void insertUpdate(DocumentEvent e) { updateUI(); }

	/**
	 * Updates the field corresponding to the command.
	 * @see DocumentListener#removeUpdate(DocumentEvent)
	 */
	public void removeUpdate(DocumentEvent e) { updateUI(); }

	/**
	 * Required by the {@link DocumentListener} but no-op implementation in 
	 * our case.
	 * @see DocumentListener#changedUpdate(DocumentEvent)
	 */
	public void changedUpdate(DocumentEvent e) {}
	
}
