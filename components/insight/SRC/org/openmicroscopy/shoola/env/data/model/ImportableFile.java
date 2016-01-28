/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2011 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.env.data.model;

import java.io.File;

import org.openmicroscopy.shoola.env.data.util.StatusLabel;
import omero.gateway.model.DatasetData;
import omero.gateway.model.ExperimenterData;
import omero.gateway.model.GroupData;

/** 
 * Store information about the file or folder to import.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since 3.0-Beta4
 */
public class ImportableFile
{
	
	/** The group where to import the data. */
	private GroupData group;
	
	/** The file or folder to import. */
	private FileObject file;
	
	/** Indicates to use the folder as a container if <code>true</code>.*/
	private boolean folderAsContainer;
	
	/** Object used to find result back. */
	private StatusLabel status;
	
	/** Indicate where to import the file, either a project or screen. */
	private omero.gateway.model.DataObject parent;
	
	/** Indicate where to import the images. */
	private DatasetData dataset;
	
	/** The node of reference if set. */
	private Object refNode;

	/** The user importing data for */
	private ExperimenterData user;

	/** The file object before possible changes.*/
	private FileObject originalFile;

	/**
	 * Creates a new instance.
	 * 
	 * @param file The object to import.
	 * @param folderAsContainer Pass <code>true</code> to make the folder a 
	 * 							container e.g. a dataset, <code>false</code>
	 * 							otherwise.
	 */
	public ImportableFile(FileObject file, boolean folderAsContainer)
	{
		this.file = file;
		this.folderAsContainer = folderAsContainer;
		originalFile = file;
	}
	
	/**
	 * Sets the flag indicating to create a container from the folder.
	 * 
	 * @param folderAsContainer Pass <code>true</code> to make the folder a 
	 * 							container e.g. a dataset, <code>false</code>
	 * 							otherwise.
	 */
	public void setFolderAsContainer(boolean folderAsContainer)
	{
		this.folderAsContainer = folderAsContainer;
	}
	
	/**
	 * Sets where to import the files.
	 * 
	 * @param parent The parent either a project or a screen.
	 * @param dataset The dataset where to import the images.
	 */
	public void setLocation(omero.gateway.model.DataObject parent, DatasetData dataset)
	{
		this.parent = parent;
		this.dataset = dataset;
	}
	
	/**
	 * Returns the parent, either a project or a screen.
	 * 
	 * @return See above.
	 */
	public omero.gateway.model.DataObject getParent() { return parent; }
	
	/**
	 * Returns the dataset.
	 * 
	 * @return See above.
	 */
	public DatasetData getDataset() { return dataset; }
	
	/**
	 * Returns the object to import.
	 * 
	 * @return See above.
	 */
	public FileObject getFile() { return file; }

	/**
	 * Returns the object to import.
	 * 
	 * @return See above.
	 */
    public FileObject getOriginalFile()
    {
        if (originalFile == null) return file;
        return originalFile; }
    
	/**
	 * Returns <code>true</code> to make the folder a container e.g. a dataset, 
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	public boolean isFolderAsContainer() { return folderAsContainer; }
	
	/**
	 * Sets the component used to notify of the progress.
	 * 
	 * @param status The component to set.
	 */
	public void setStatus(StatusLabel status) { this.status = status; }
	
	/**
	 * Returns the component used to notify of the progress.
	 * 
	 * @return See above.
	 */
	public StatusLabel getStatus() { return status; }
	
	/**
	 * Returns the node of reference if set.
	 * 
	 * @return See above. 
	 */
	public Object getRefNode() { return refNode; }
	
	/**
	 * Sets the node of reference if set.
	 *  
	 * @param refNode The node to set.
	 */
	public void setRefNode(Object refNode) { this.refNode = refNode; }
	
	/**
	 * Sets the file.
	 * 
	 * @param file The value to set.
	 */
	public void setFile(File file) { this.file = new FileObject(file); }
	
	/**
	 * Sets the group.
	 * 
	 * @param group The group where to import the data.
	 */
	public void setGroup(GroupData group) { this.group = group; }
	
	/**
	 * Returns the group.
	 * 
	 * @return See above.
	 */
	public GroupData getGroup() { return group; }
	
	/**
	 * Sets the user.
	 * 
	 * @param user The user to import data and set as the owner.
	 */
	public void setUser(ExperimenterData user) { this.user = user;}
	
	/**
	 * Returns the user.
	 * 
	 * @return See above.
	 */
	public ExperimenterData getUser() { return user;}
	
	/**
	 * Returns a copy of the object.
	 * 
	 * @return See above.
	 */
	public ImportableFile copy()
	{
		ImportableFile newObject = new ImportableFile(this.file,
				this.folderAsContainer);
		newObject.dataset = this.dataset;
		newObject.parent = this.parent;
		newObject.file = this.file;
		newObject.refNode = this.refNode;
		newObject.group = this.group;
		newObject.user = this.user;
		newObject.status = new StatusLabel(this.file);
		return newObject;
	}
	
	/**
	 * Returns the details about the absolute path, group id and user id.
	 * @see #toString()
	 */
	public String toString()
	{
		StringBuffer buf = new StringBuffer();
		buf.append(originalFile.getAbsolutePath());
		if (group != null)
			buf.append("_"+group.getId());
		if (user != null)
			buf.append("_"+user.getId());
		return buf.toString();
	}

}
