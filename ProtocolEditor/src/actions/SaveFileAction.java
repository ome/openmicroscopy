package actions;

/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
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

import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.event.ChangeEvent;

import ui.IModel;
import util.ImageFactory;

public class SaveFileAction
	extends ProtocolEditorAction {
	
	public SaveFileAction(IModel model) {
		super(model);
	
		putValue(Action.NAME, "Save File");
		putValue(Action.SHORT_DESCRIPTION, "Save the current file");
		putValue(Action.SMALL_ICON, ImageFactory.getInstance().getIcon(ImageFactory.SAVE_ICON)); 
	}

	
	
	public void actionPerformed(ActionEvent event) {
		// if the current file is not saved (still called eg "untitled2")
		if (model.getCurrentFile() == null) return;
		
		// This will work unless the user has previously saved their file as "untitled"!
		if (model.getCurrentFile().getName().contains("untitled")) {
			Action action = new SaveFileAsAction(model);
			action.actionPerformed(event);
		}
		else {
			int option = JOptionPane.showConfirmDialog(null, "Save changes? " + 
					"\n This will over-write the original file",
					"Save Changes?", JOptionPane.OK_CANCEL_OPTION);
			if (option == JOptionPane.OK_OPTION) {
				model.saveTreeToXmlFile(model.getCurrentFile());
				JOptionPane.showMessageDialog(frame, "Experiment saved.");
			}
		}
	}
	
	public void stateChanged(ChangeEvent e) {
		
		String[] fileList = model.getOpenFileList();
		
		this.setEnabled(!(fileList.length == 0));
	}
}
