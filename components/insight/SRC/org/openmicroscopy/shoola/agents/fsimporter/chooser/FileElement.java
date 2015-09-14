/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2010 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.openmicroscopy.shoola.agents.fsimporter.view.Importer;
import org.openmicroscopy.shoola.env.data.model.FileObject;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

import omero.gateway.model.ExperimenterData;
import omero.gateway.model.GroupData;

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
	private FileObject file;
	
	/** The name of the imported file. */
	private String name;
	
	/** The size of the file. */
	private long length;
	
	/** 
	 * Flag indicating that it is allowed to modify the container location.
	 * The value will only be taken into account if the object is a file.
	 */
	private boolean toggleContainer;
	
	/** The type when the file was added.*/
	private int type;
	
	/** The group where the file will be imported.*/
	private GroupData group;
	
	/** The user to import data as */
	private ExperimenterData user;
	
	/** 
	 * Creates a new instance. 
	 * 
	 * @param file The file to import.
	 * @param type The type of container when the file was added.
	 * @param group The group where to import the data.
	 * @param user The user to import data as.
	 */
	FileElement(FileObject file, int type, GroupData group, ExperimenterData user)
	{
		if (file == null)
			throw new IllegalArgumentException("No file set");
		this.file = file;
		this.type = type;
		this.group = group;
		this.user = user;
		length = -1;
		if (type == Importer.SCREEN_TYPE)
			this.toggleContainer = false;
	}
	
	/**
	 * Sets to <code>true</code> if the container can be modified, 
	 * <code>false</code> otherwise. The value will only be taken into account
	 * if the object is a file.
	 * 
	 * @param toggleContainer The value to set.
	 */
	void setToggleContainer(boolean toggleContainer)
	{
		this.toggleContainer = toggleContainer;
	}
	
	/**
	 * Returns <code>true</code> if the container can be modified, 
	 * <code>false</code> otherwise. The value will only be taken into account
	 * if the object is a file.
	 * 
	 * @return See above.
	 */
	boolean isToggleContainer()
	{ 
		if (type == Importer.SCREEN_TYPE) return false;
		return toggleContainer;
	}
	
	/**
	 * Returns the type associated to that element.
	 * 
	 * @return See above.
	 */
	int getType() { return type; }
	
	/**
	 * Returns the length of the file
	 * 
	 * @return See above.
	 */
	long getFileLength()
	{
		if (length > 0) return length;
		return file.getLength();
	}
	
	/**
	 * Returns the length of the file in as a formatted string.
	 * 
	 * @return See above.
	 */
	String getFileLengthAsString()
	{
		long l = getFileLength();
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
	    if (file.getFile() instanceof File) {
	        File f = (File) file.getFile();
	        if (f.isFile()) return false;
	        File[] list = f.listFiles();
	        if (list == null || list.length == 0) return false;
	        return true;
	    }
		return false;
	}
	
	/**
	 * Returns the files within the directory.
	 * 
	 * @return See above.
	 */
	List<File> getFiles()
	{
		if (!isDirectory()) return null;
		 File f = (File) file.getFile();
		File[] list = f.listFiles();
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
		if (name == null) return file.getName();
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
	public FileObject getFile() { return file; }
	
	/**
	 * Returns the group.
	 * 
	 * @return See above.
	 */
	public GroupData getGroup() { return group; }
	
	/** 
	 * Overridden to return the name to give to the imported file.
	 * @see Object#toString()
	 */
	public String toString() { return getName(); }
	
	/**
	 * Returns the user.
	 * 
	 * @return See above.
	 */
	public ExperimenterData getUser() { return user; }
}
