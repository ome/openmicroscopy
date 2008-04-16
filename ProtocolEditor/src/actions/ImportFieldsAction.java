
/*
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
 *	author Will Moore will@lifesci.dundee.ac.uk
 */

package actions;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.Action;
import javax.swing.event.ChangeEvent;

import cmd.OpenFileCmd;

import ui.IModel;
import util.ImageFactory;


public class ImportFieldsAction 
	extends ProtocolEditorAction {
	
	public ImportFieldsAction(IModel model) {

		super(model);
	
		putValue(Action.NAME, "Import Fields");
		putValue(Action.SHORT_DESCRIPTION, "Open an OMERO.editor file or XML file, and select fields to import.");
		putValue(Action.SMALL_ICON, ImageFactory.getInstance().getIcon(ImageFactory.IMPORT_ICON)); 
		
		// update enabled status
		setEnabled(fieldTemplatesEditable());
	}
	
	public void actionPerformed(ActionEvent e) {
		File file = OpenFileCmd.getFileFromUser();
		model.setImportFile(file);
	}
	

	public void stateChanged(ChangeEvent e) {
		
		/*
		 * This action should only be enabled if a file is open and the
		 * currently highlighted fields are unlocked. 
		 */
		setEnabled(fieldTemplatesEditable());
	}

}
