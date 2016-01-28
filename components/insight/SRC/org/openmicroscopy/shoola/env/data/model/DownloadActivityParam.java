/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2015 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.env.data.model;

import java.io.File;

import javax.swing.Icon;
import javax.swing.JComponent;

import omero.RString;
import omero.model.OriginalFile;

import org.openmicroscopy.shoola.env.ui.FileLoader;

import omero.gateway.model.FileAnnotationData;

/** 
 * Parameters required to download a file.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since 3.0-Beta4
 */
public class DownloadActivityParam
{

	/** Indicates to load the original file if original file is not set. */
	public static final int ORIGINAL_FILE = FileLoader.ORIGINAL_FILE;
	
	/** Indicates to load the file annotation if original file is not set. */
	public static final int FILE_ANNOTATION = FileLoader.FILE_ANNOTATION;
	
	/** Indicates to load the metadata file associated to the image id. */
	public static final int METADATA_FROM_IMAGE = FileLoader.METADATA_FROM_IMAGE;

    /** The icon associated to the parameters. */
    private Icon icon;
    
    /** The folder where to download the file. */
    private File folder; 
    
    /** The file to download. */
    private OriginalFile file;
    
    /** Create a legend as a separate text file. */
    private String legend;
    
    /** The name of the file to save. */
    private String fileName;
    
    /** The extension for the legend. Default is <code>null</code>.*/
    private String legendExtension;
    
    /** One of the constants defined by this class. */
    private int index;
    
    /** The id of the original file or of the annotation. */
    private long id;
    
    /** The source triggering the operation.*/
    private JComponent source;

    /** Indicates to register in UI.*/
    private boolean uiRegister;
    
    /** FileAnnotation to delete after downloading */
    private FileAnnotationData toDelete;
    
    /** Overwrite local file, if it already exists */
    private boolean overwrite = false;
    
    /** 
     * Checks if the index is valid.
     * 
     * @param index The index to handle.
     */
    private void checkIndex(int index)
    {
    	switch (index) {
			case ORIGINAL_FILE:
			case FILE_ANNOTATION:
			case METADATA_FROM_IMAGE:
				return;
			default:
				throw new IllegalArgumentException("Index not supported.");
		}
    }
    
    /**
     * Downloads the passed file.
     * 
     * @param file The file to download.
     * @param folder The folder where to download the file.
     * @param icon The associated icon.
     */
    public DownloadActivityParam(OriginalFile file, File folder, Icon icon)
    {
    	if (file == null) 
    		throw new IllegalArgumentException("No file to download.");
    	this.file = file;
    	this.folder = folder;
    	this.icon = icon;
    	legend = null;
    	fileName = null;
    	legendExtension = null;
    	index = -1;
    	id = -1;
    	uiRegister = true;
    }

    /**
     * Downloads the file.
     * 
     * @param id The id of the object to download.
     * @param index One of the constants defined by this class.
     * @param folder 	The folder where to download the file.
     * @param icon	 	The associated icon.
     */
    public DownloadActivityParam(long id, int index, File folder, Icon icon)
    {
    	checkIndex(index);
    	this.index = index;
    	this.folder = folder;
    	this.icon = icon;
    	this.id = id;
    	uiRegister = true;
    }
    
    /**
     * Sets to <code>true</code> if the component does not need to be
     * registered, <code>false</code> otherwise.
     * 
     * @param uiRegister Pass <code>true</code> to register, <code>false</code>
     * 					 otherwise.
     */
    public void setUIRegister(boolean uiRegister)
    { 
    	this.uiRegister = uiRegister; 
    }
    
    /**
     * Returns <code>true</code> if the component does not need to be
     * registered, <code>false</code> otherwise.
     * 
     * @return See above.
     */
    public boolean isUIRegister() { return uiRegister; }
    
    /**
     * Returns the name to give to the file. 
     * 
     * @return See above.
     */
    public String getFileName() { return fileName; }
    
    /**
     * Sets the name to give to the file to download.
     * 
     * @param fileName The value to set.
     */
    public void setFileName(String fileName) { this.fileName = fileName; }
    
    /**
	 * Sets the legend associated to the file.
	 * 
	 * @param legend  The value to set.
	 */
    public void setLegend(String legend) { this.legend = legend; }
    
    /**
     * Returns the legend associated to the text.
     * 
     * @return See above.
     */
    public String getLegend() { return legend; }
    
    /**
     * Sets the extension of the legend associated to the file.
     * 
     * @param legendExtension The value to set.
     */
    public void setLegendExtension(String legendExtension)
    {
    	this.legendExtension = legendExtension;
    }
    
    /**
     * Returns the legend extension.
     * 
     * @return See above.
     */
    public String getLegendExtension() { return legendExtension; }
    
	/**
	 * Returns the icon if set or <code>null</code>.
	 * 
	 * @return See above.
	 */
	public Icon getIcon() { return icon; }
	
	/** 
	 * Returns the folder where to download the file.
	 * 
	 * @return See above.
	 */
	public File getFolder() { return folder; }
	
	/** 
	 * Returns the file to download.
	 * 
	 * @return See above.
	 */
	public OriginalFile getFile() { return file; }
	
	/**
	 * Returns the name of the original file if not <code>null</code>,
	 * otherwise returns an empty string.
	 * 
	 * @return See above.
	 */
	public String getOriginalFileName()
	{
		if (file == null) return "";
		if (!file.isLoaded()) return "";
		RString name = file.getName();
		if (name == null) return "";
		return name.getValue();
	}
	
	/**
	 * Returns the identifier.
	 * 
	 * @return See above.
	 */
	public long getId() { return id; }
	
	/**
	 * Returns the index.
	 * 
	 * @return See above.
	 */
	public int getIndex() { return index; }
	
	/**
	 * Sets the source.
	 * 
	 * @param source The source triggering the operation.
	 */
	public void setSource(JComponent source) { this.source = source; }
	
	/**
	 * Returns the source triggering the operation.
	 * 
	 * @return See above.
	 */
	public JComponent getSource() { return source; }

	/**
	 * Get the {@link FileAnnotationData} which should get
	 * deleted after download finished
	 * @return See above.
	 */
	public FileAnnotationData getToDelete() {
		return toDelete;
	}

	/**
	 * Set the {@link FileAnnotationData} which should get
	 * deleted after download finished
	 * 
	 * @param toDelete The {@link FileAnnotationData} to delete
	 */
	public void setToDelete(FileAnnotationData toDelete) {
		this.toDelete = toDelete;
	}

	/**
	 * Returns <code>true</code> if the local file should be overwritten, if it
	 * already exists
	 * 
	 * @return See above.
	 */
	public boolean isOverwrite() {
		return overwrite;
	}

	/**
	 * Determines if the local file should be overwritten, if it already exists
	 * 
	 * @param overwrite
	 *            Set to <code>true</code> if local file should be overwritten
	 */
	public void setOverwrite(boolean overwrite) {
		this.overwrite = overwrite;
	}
	
	
}
