/*
 * org.openmicroscopy.shoola.agents.fsimporter.chooser.FileElement
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2010 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.fsimporter.chooser;


//Java imports
import java.io.File;
import java.util.ArrayList;
import java.util.List;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/**
 * Hosts information about the file to import.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
class FileElement
{
	
	/** The file to host. */
	private File file;
	
	/** The name of the imported file. */
	private String name;
	
	/** The size of the file. */
	private double length;
	
	/** 
	 * Creates a new instance. 
	 * 
	 * @param file The file to import.
	 */
	FileElement(File file)
	{
		if (file == null)
			throw new IllegalArgumentException("No file set");
		this.file = file;
		length = -1;
	}
	
	/**
	 * Returns the length of the file
	 * 
	 * @return See above.
	 */
	double getFileLength()
	{
		if (length > 0) return length;
		if (file.isFile())
			length = file.length()/1000;
		else { //directory.
			File[] files = file.listFiles();
			if (files != null) {
				double value = 0;
				for (int i = 0; i < files.length; i++) {
					value += files[i].length();
				}
				length = value/1000;
			}
		}
		return length;
	}
	
	/**
	 * Returns the length of the file in as a formatted string.
	 * 
	 * @return See above.
	 */
	String getFileLengthAsString()
	{
		long l = (long) (getFileLength());
		if (l <= 0) return "--";
		return UIUtilities.formatFileSize(l);
	}
	
	/**
	 * Returns <code>true</code> if the file is a directory,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean isDirectory()
	{ 
		if (file.isFile()) return false;
		File[] list = file.listFiles();
		if (list == null || list.length == 0) return false;
		return true;
	}
	
	/**
	 * Returns the files within the directory.
	 * 
	 * @return See above.
	 */
	List<File> getFiles()
	{
		if (!isDirectory()) return null;
		File[] list = file.listFiles();
		List<File> files = new ArrayList<File>();
		if (list == null || list.length == 0) return files;
		for (int i = 0; i < list.length; i++) {
			if (!list[i].isHidden() && !list[i].isDirectory()) 
				files.add(list[i]);
		}
		return files;
	}
	
	/**
	 * Returns the name to give to the imported file.
	 * 
	 * @return See above.
	 */
	String getName()
	{
		if (name == null) return file.getAbsolutePath();
		return name;
	}
	
	/**
	 * Sets the name to give to the imported file.
	 * 
	 * @param name The name to set.
	 */
	void setName(String name) { this.name = name; }
	
	/** 
	 * Returns the file hosted by this component.
	 * 
	 * @return See above.
	 */
	public File getFile() { return file; }
	
	/** 
	 * Overridden to return the name to give to the imported file.
	 * @see Object#toString()
	 */
	public String toString() { return getName(); }
	
}
