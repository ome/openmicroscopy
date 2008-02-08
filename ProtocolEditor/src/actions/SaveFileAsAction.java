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
import java.io.File;

import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.event.ChangeEvent;

import ui.IModel;
import ui.components.ProtocolFileFilter;
import util.ImageFactory;
import util.PreferencesManager;

public class SaveFileAsAction 
	extends ProtocolEditorAction {
	
	public SaveFileAsAction(IModel model) {
		super(model);
	
		putValue(Action.NAME, "Save File As...");
		putValue(Action.SHORT_DESCRIPTION, "Save the current file as...");
		putValue(Action.SMALL_ICON, ImageFactory.getInstance().getIcon(ImageFactory.SAVE_FILE_AS_ICON)); 
	}

	public void actionPerformed(ActionEvent event) {
		saveFileAs();
	}
	
	
public void saveFileAs() {
		
		final JFileChooser fc = new JFileChooser();
		fc.setFileFilter(new ProtocolFileFilter());
		
		File currentLocation = null;
		if (PreferencesManager.getPreference(PreferencesManager.CURRENT_FILES_FOLDER) != null) {
			currentLocation = new File(PreferencesManager.getPreference(PreferencesManager.CURRENT_FILES_FOLDER));
		} 
		fc.setCurrentDirectory(currentLocation);

		int returnVal = fc.showSaveDialog(frame);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
            File xmlFile = fc.getSelectedFile();  // this may be a directory! 
            
            // remember where the user last saved a file
            PreferencesManager.setPreference(PreferencesManager.CURRENT_FILES_FOLDER, xmlFile.getParent());
            
            if (xmlFile.isDirectory()) {
                JOptionPane.showMessageDialog(frame, "Please choose a file name (not a directory)");
                // try again! 
                saveFileAs();
                return;
            }

            // first, make sure the filename ends .exp.xml
            String filePathAndName = xmlFile.getAbsolutePath();
            if (!(filePathAndName.endsWith(".pro.xml"))) {	// if not..
            	filePathAndName = filePathAndName + ".pro.xml";
            	xmlFile = new File(filePathAndName);
            }
               
            // now check if the file exists. If so, take appropriate action
            if (xmlFile.exists()) {
            	int result = JOptionPane.showConfirmDialog(frame, "File exists. Overwrite it?");
            	if (!(result == JOptionPane.YES_OPTION)) {	// if not yes, then forget it!
            		return;
            	}
        	}
            
            model.saveTreeToXmlFile(xmlFile);
            
            // refreshFileEdited();
		}
	}

	/*
	 * if no files are open, disable the Save option. 
	 * @see actions.ProtocolEditorAction#stateChanged(javax.swing.event.ChangeEvent)
	 */
	public void stateChanged(ChangeEvent e) {
	
		String[] fileList = model.getOpenFileList();
		
		this.setEnabled(!(fileList.length == 0));
	}
}
