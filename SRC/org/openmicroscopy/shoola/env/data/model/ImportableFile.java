/*
 * org.openmicroscopy.shoola.env.data.model.ImportableFile 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2011 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
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


//Java imports
import java.io.File;

import org.openmicroscopy.shoola.env.data.util.StatusLabel;

//Third-party libraries

//Application-internal dependencies

/** 
 * Store information about the file or folder to import.
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
public class ImportableFile
{

	/** The file or folder to import. */
	private File file;
	
	/** Flag indicating to archive the images. */
	private boolean archived;
	
	/** Indicates to use the folder as a container if <code>true</code>.*/
	private boolean folderAsContainer;
	
	/** Object used to find result back. */
	private StatusLabel status;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param file The object to import.
	 * @param archived  Pass <code>true</code> if the images will be archived, 
	 * 					<code>false</code> otherwise.
	 */
	public ImportableFile(File file, boolean archived)
	{
		this(file, archived, false);
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param file The object to import.
	 * @param archived  Pass <code>true</code> if the images have to be archived, 
	 * 					<code>false</code> otherwise.
	 * @param folderAsContainer Pass <code>true</code> to make the folder a 
	 * 							container e.g. a dataset, <code>false</code>
	 * 							otherwise.
	 */
	public ImportableFile(File file, boolean archived, boolean folderAsContainer)
	{
		this.file = file;
		this.archived = archived;
		this.folderAsContainer = folderAsContainer;
	}
	
	/**
	 * Returns the object to import.
	 * 
	 * @return See above.
	 */
	public File getFile() { return file; }
	
	/**
	 * Returns <code>true</code> if the images have to be archived, 
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	public boolean isArchived() { return archived; }
	
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
	
}
