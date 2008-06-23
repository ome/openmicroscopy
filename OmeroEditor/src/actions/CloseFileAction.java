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
import javax.swing.Icon;
import javax.swing.JOptionPane;
import javax.swing.event.ChangeEvent;

import ui.IModel;
import util.ImageFactory;

public class CloseFileAction 
	extends ProtocolEditorAction {
	
	Icon closeIcon = ImageFactory.getInstance().getIcon(ImageFactory.N0);
	Icon redBallIcon = ImageFactory.getInstance().getIcon(ImageFactory.RED_BALL_ICON);
	
	public CloseFileAction(IModel model) {
		
		super(model);
		
		putValue(Action.NAME, "Close current file");
		putValue(Action.SHORT_DESCRIPTION, "Close the currently opened file");
		putValue(Action.SMALL_ICON, closeIcon); 
	}
	
	public void actionPerformed(ActionEvent e) {
		
		closeCurrentFile(e);
	}
	
	public void closeCurrentFile(ActionEvent e) {
		
//		 check whether you want to save edited file 
		if (model.isCurrentFileEdited()) {
			int result = JOptionPane.showConfirmDialog
				(frame, "Save the current file before closing?");
			if (result == JOptionPane.YES_OPTION) {
				// save Protocol (no exp details)	Experiment must be saved by user manually
				
				SaveFileAction saveFile = new SaveFileAction(model);
				saveFile.actionPerformed(e);
				
			} else if (result == JOptionPane.CANCEL_OPTION) {
				return;
			}
		}
		
		model.closeCurrentFile();
	}
	
	public void stateChanged(ChangeEvent e) {
		// if file is edited, show a different close icon
		refreshFileEdited();

		// if no files open, disable action
		String[] fileList = model.getOpenFileList();
		setEnabled(!(fileList.length == 0));
	}
	
	public void refreshFileEdited() {
		
		boolean protocolEdited = model.isCurrentFileEdited();
			
		// if file edited, the close button looks different
		putValue(Action.SMALL_ICON,protocolEdited ? redBallIcon : closeIcon);
	}
}



