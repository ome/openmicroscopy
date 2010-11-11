/*
 * org.openmicroscopy.shoola.agents.fsimporter.chooser.ImportableObject 
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
package org.openmicroscopy.shoola.agents.fsimporter.chooser;


//Java imports
import java.io.File;
import java.util.Collection;
import java.util.Map;

//Third-party libraries

//Application-internal dependencies
import pojos.DataObject;
import pojos.DatasetData;
import pojos.ScreenData;
import pojos.TagAnnotationData;

/** 
 * Helper class where parameters required for the imports are stored.
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
public class ImportableObject
{

	/** 
	 * The collection of files to import. The flag indicate
	 * to either archived the files or not.
	 */
	private Map<File, Boolean> 	files;
	
	/** Flag indicating to archive the files or not. */
	private boolean 	archived;
	
	/** The depth. */
	private int			depth;
	
	/** 
	 * Flag indicating to override the name set by B-F when importing the data. 
	 */
	private boolean		overrideName;
	
	/** 
	 * If not <code>null</code>, this will indicate to use the selected folder
	 * as a <code>Dataset</code> or a <code>Screen</code>.
	 */
	private Class		folderAsContainer;
	
	/** The collection of tags. */
	private Collection<TagAnnotationData> tags;
	
	/** The container where to import the data if set. */
	private DataObject container;
	
	/** The array containing pixels size.*/
	private double[]	pixelsSize;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param files 	The collection of files to import.
	 * @param archived 	Pass <code>true</code> to archive the files, 
	 * 					<code>false</code> otherwise.
	 * @param overrideName Pass <code>true</code> to override the name of the 
	 *                     file set while importing the data,
	 *                     <code>false</code> otherwise.
	 */
	ImportableObject(Map<File, Boolean> files, boolean archived, 
			boolean overrideName)
	{
		this.files = files;
		this.archived = archived;
		this.overrideName = overrideName;
		folderAsContainer = null;
		depth = -1;
	}
	
	/**
	 * Sets the container where to import the data if set.
	 * 
	 * @param container The container to set.
	 */
	void setContainer(DataObject container)
	{
		this.container = container;
	}
	
	/**
	 * Indicates to use the selected folders as a container. If, for example,
	 * the passed value is of type <code>DatasetData</code>, then a Dataset
	 * with the folder's name will be created and the images imported 
	 * in that dataset.
	 * 
	 * @param type One of the following type: DatasetData, ScreenData.
	 */
	void setFolderAsContainer(Class type)
	{
		if (DatasetData.class.getName().equals(type.getName()) || 
			ScreenData.class.getName().equals(type.getName()))
			folderAsContainer = type;
	}
	
	/**
	 * Sets the default size of the pixels if the value is not found.
	 * 
	 * @param pixelsSize The value to set.
	 */
	void setPixelsSize(double[] pixelsSize) { this.pixelsSize = pixelsSize; }
	
	/** 
	 * Sets the collection of tags.
	 * 
	 * @param tags The tags to use.
	 */
	void setTags(Collection<TagAnnotationData> tags)
	{
		this.tags = tags;
	}

	/**
	 * Sets the depth.
	 * 
	 * @param depth The value to set.
	 */
	void setDepth(int depth) { this.depth = depth; }
	
	/**
	 * Returns the depth.
	 * 
	 * @return See above.
	 */
	public int getDepth() { return depth; }
	
	/**
	 * Returns the collection of files to import.
	 * 
	 * @return See above.
	 */
	public Map<File, Boolean> getFiles() { return files; }
	
	/**
	 * Returns <code>true</code> to archive the files, 
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	public boolean isArchived() { return archived; }
	
	/** 
	 * Returns <code>true</code> if the name set while importing the data
	 * has to be overridden, <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	public boolean isOverrideName() { return overrideName; }
	
	/** 
	 * Returns the pixels size to use of the value is not found in the 
	 * file.
	 * 
	 * @return See above.
	 */
	public double[] getPixelsSize() { return pixelsSize; }
	
	/**
	 * Returns the container where to import the data if set.
	 * 
	 * @return See above.
	 */
	public DataObject getContainer() { return container; }
	
	/**
	 * Returns the collection of tags.
	 * 
	 * @return See above.
	 */
	public Collection<TagAnnotationData> getTags() { return tags; }

	/**
	 * Returns <code>null</code> or one of the following types:
	 * DatasetData, ScreenData.
	 * 
	 * @return See above.
	 */
	public Class getFolderAsContainer() { return folderAsContainer; }
	
}
