/*
 * org.openmicroscopy.shoola.env.data.model.ImportContext 
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

import java.util.List;

/** 
 * Holds the information on where to import the files and
 * metadata that need to be imported alongside the image files.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since 3.0-Beta4
 */
public class ImportContext
{

	/** Indicates to the use the folder as a dataset. */
	public static final int FOLDER_AS_DATASET = 0;
	
	/** Indicates to the use the folder as a screen. */
	public static final int FOLDER_AS_SCREEN = 1;
	
	/** The location where to import the data. */
	private DataObject  container;
	
	/** One of the constants defined by this class. */
	private int 		folderAs;
	
	/** The depth to retrieve for the container's name. */
	private int 		depth;

	/** The metadata to import with the object. */
	private ImportMetadataContext metadata;
	
	/**
	 * Checks if the specified type is supported.
	 * 
	 * @param type The type to handle.
	 */
	private void checkType(int type)
	{
		switch (type) {
			case FOLDER_AS_DATASET:
			case FOLDER_AS_SCREEN:
				return;
		}
		throw new IllegalArgumentException("Folder Type not supported.");
	}
	
	/** 
	 * Creates a new instance. 
	 * 
	 * @param container The location where to import the data.
	 * @param files The files to import.
	 */
	public ImportContext(DataObject container, List<ImportableFile> files)
	{
		if (files == null || files.size() == 0)
			throw new IllegalArgumentException("No files to import");
		this.container = container;
		this.folderAs = -1;
	}
	
	/** 
	 * Creates a new instance. 
	 * 
	 * @param folderAs Indicates to use the folder as a container.
	 * @param depth The depth of the scanning.
	 * @param files The files to import.
	 */
	public ImportContext(int folderAs, int depth, List<ImportableFile> files)
	{
		if (files == null || files.size() == 0)
			throw new IllegalArgumentException("No files to import");
		checkType(folderAs);
		this.folderAs = folderAs;
		if (depth < 0) depth = 0;
		this.depth = depth;
	}
	
	/**
	 * Returns the container.
	 * 
	 * @return See above.
	 */
	public DataObject getContainer() { return container; }
	
	/**
	 * Returns the type of folder to create.
	 * 
	 * @return See above.
	 */
	public int getFolderAs() { return folderAs; }
	
	/**
	 * Returns the depth.
	 * 
	 * @return See above.
	 */
	public int getDepth() { return depth; }
	
	/**
	 * Sets the metadata to import with the files.
	 * 
	 * @param metadata See above.
	 */
	public void setMetadata(ImportMetadataContext metadata)
	{
		this.metadata = metadata;
	}
	
	/**
	 * Returns the metadata to import with the files.
	 * 
	 * @return See above.
	 */
	public ImportMetadataContext getMetadata() { return metadata; }
	
	
}
