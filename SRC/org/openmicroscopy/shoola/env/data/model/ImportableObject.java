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
package org.openmicroscopy.shoola.env.data.model;


//Java imports
import java.io.File;
import java.util.Collection;
import java.util.List;
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
	 * The collection of files to import. The value is used if the file
	 * is a directory to find out if the folder has to be turned into a 
	 * dataset or screen for the first value and if the file has to be archived
	 * for the second value.
	 */
	private Map<File, List<Boolean>> files;
	
	/** The depth. */
	private int			depth;
	
	/** 
	 * Flag indicating to override the name set by B-F when importing the data. 
	 */
	private boolean		overrideName;
	
	/** The collection of tags. */
	private Collection<TagAnnotationData> tags;
	
	/** The containers where to import the data if set. */
	private List<DataObject> containers;
	
	/** The array containing pixels size.*/
	private double[]	pixelsSize;
	
	/** The objects to import. */
	private List<ImportObject> toImport;
	
	/** The type to create if the folder has to be saved as a container. */
	private Class type;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param files 	The collection of files to import.
	 * @param overrideName Pass <code>true</code> to override the name of the 
	 *                     file set while importing the data,
	 *                     <code>false</code> otherwise.
	 */
	public ImportableObject(Map<File, List<Boolean>> files, boolean overrideName)
	{
		this.files = files;
		this.overrideName = overrideName;
		type = DatasetData.class;
		depth = -1;
	}
	
	/**
	 * Sets the type to use when creating a folder as container.
	 * 
	 * @param type The type to use.
	 */
	public void setType(Class type) { this.type = type; }
	
	/**
	 * Sets the containers where to import the data if set.
	 * 
	 * @param containers The containers to set.
	 */
	public void setContainers(List<DataObject> containers)
	{
		this.containers = containers;
	}
	
	/**
	 * Sets the default size of the pixels if the value is not found.
	 * 
	 * @param pixelsSize The value to set.
	 */
	public void setPixelsSize(double[] pixelsSize)
	{ 
		this.pixelsSize = pixelsSize;
	}
	
	/** 
	 * Sets the collection of tags.
	 * 
	 * @param tags The tags to use.
	 */
	public void setTags(Collection<TagAnnotationData> tags)
	{
		this.tags = tags;
	}

	/**
	 * Sets the depth.
	 * 
	 * @param depth The value to set.
	 */
	public void setDepth(int depth) { this.depth = depth; }
	
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
	public Map<File, List<Boolean>> getFiles() { return files; }
	
	/**
	 * Returns <code>true</code> if the file has to be archived.
	 * <code>false</code> otherwise.
	 * 
	 * @param file The file to handle.
	 * @return See above.
	 */
	public boolean isArchivedFile(File file)
	{
		if (file == null) return false;
		List<Boolean> list = files.get(file);
		if (list.size() < 2) return false;
		Boolean b = list.get(1);
		if (b == null) return false;
		return b.booleanValue();
	}
	
	/**
	 * Returns the <code>DataObject</code> corresponding to the folder 
	 * be saved as a container.
	 * 
	 * @param file The file to handle.
	 * @return See above.
	 */
	public DataObject createFolderAsContainer(File file)
	{
		if (file == null || file.isFile()) return null;
		List<Boolean> list = files.get(file);
		Boolean b = list.get(0);
		if (b == null || !b.booleanValue()) return null;
		if (DatasetData.class.equals(type)) {
			DatasetData dataset = new DatasetData();
			dataset.setName(file.getName());
			return dataset;
		} else if (ScreenData.class.equals(type)) {
			ScreenData screen = new ScreenData();
			screen.setName(file.getName());
			return screen;
		}
		return null;
	}
	
	/**
	 * Returns the type used when creating the object.
	 * 
	 * @return See above.
	 */
	public Class getType() { return type; }
	
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
	public Double[] getPixelsSize()
	{ 
		if (pixelsSize != null && pixelsSize.length > 0) {
			Double[] array = new Double[pixelsSize.length];
			for (int i = 0; i < pixelsSize.length; i++) {
				array[i] = new Double(pixelsSize[i]);
			}
			return array;
		}
		return null; 
	}
	
	/**
	 * Returns the containers where to import the data if set.
	 * 
	 * @return See above.
	 */
	public List<DataObject> getContainers() { return containers; }
	
	/**
	 * Returns the collection of tags.
	 * 
	 * @return See above.
	 */
	public Collection<TagAnnotationData> getTags() { return tags; }

	/**
	 * Sets the files to import.
	 * 
	 * @param toImport
	 */
	public void setFilesToImport(List<ImportObject> toImport)
	{
		this.toImport = toImport;
	}
	
	/**
	 * Returns the files to import as <code>ImportObject</code>s.
	 * 
	 * @return See above.
	 */
	public List<ImportObject> getFilesToImport() { return toImport; }
	
}
