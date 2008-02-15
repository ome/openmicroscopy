package cmd;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import ui.IModel;
import util.PreferencesManager;

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

public class OpenFileCmd
	implements ActionCmd
	{
	
	IModel model;
	
	public OpenFileCmd(IModel model) {
		this.model = model;
	}

	public void execute() {
		File file = getFileFromUser();
		if (file != null)
			model.openThisFile(file);
	}
	
	//open a file
	public static File getFileFromUser() {
		
		// Create a file chooser
		final JFileChooser fc = new JFileChooser();
		fc.setFileFilter(new OpenProExpXmlFileFilter());
		
		File currentLocation = null;
		if (PreferencesManager.getPreference(PreferencesManager.CURRENT_FILES_FOLDER) != null) {
			currentLocation = new File(PreferencesManager.getPreference(PreferencesManager.CURRENT_FILES_FOLDER));
		} 
		fc.setCurrentDirectory(currentLocation);

		int returnVal = fc.showOpenDialog(null);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
            File xmlFile = fc.getSelectedFile();
         // remember where the user last saved a file
            PreferencesManager.setPreference(PreferencesManager.CURRENT_FILES_FOLDER, xmlFile.getParent());
           
            return xmlFile;
		}
		else return null;
	}
	
	public static class OpenProExpXmlFileFilter extends FileFilter {
		public boolean accept(File file) {
			boolean recognisedFileType = 
				//	allows "MS Windows" to see directories
				((file.getName().endsWith("pro")) || (file.getName().endsWith("exp")) || 
						(file.getName().endsWith("xml")) || (file.isDirectory()));
			return recognisedFileType;
		}
		public String getDescription() {
			return " .pro .exp .xml files";
		}
	}

}
