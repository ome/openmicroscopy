package ui.components;

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

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

/**
 * An extension of JFileChooser, which provides a getFileFromUser() method that returns a file (or 
 * null if the user cancels). 
 * 
 * @author will
 *
 */

public class FileChooserReturnFile 
	extends JFileChooser {
	
	String[] fileExtensions;
	
	public FileChooserReturnFile(String[] fileExtensions, String currentFolderPath) {
		super();
		
		this.fileExtensions = fileExtensions;
		
		setFileFilter(new TheFileFilter());
		
		if (currentFolderPath != null) {
			setCurrentDirectory(new File(currentFolderPath));
		}
		
	}
	
	
	public File getFileFromUser() {
		int returnVal = showOpenDialog(null);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
            File fileFromUser = getSelectedFile();
            return fileFromUser;
		}
		else {
			return null;
		}
	}
	
	
	public class TheFileFilter extends FileFilter {
		public boolean accept(File file) {
			String fileName = file.getName();
			for(int i=0; i< fileExtensions.length; i++) {
				if (fileName.endsWith(fileExtensions[i]))
					return true;
			}
			//	allows "MS Windows" to see directories
			if (file.isDirectory())
				return true;
				
			return false;
		}
		public String getDescription() {
			String description = "files";
			
			for(int i=0; i< fileExtensions.length; i++) {
				description = fileExtensions[i] + " " + description;
			}
			
			return description;
		}
	}

}
