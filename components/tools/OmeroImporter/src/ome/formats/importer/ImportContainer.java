/*
 * ome.formats.importer.gui.GuiCommonElements
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
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

package ome.formats.importer;

import java.io.File;
import java.util.List;

import loci.formats.FileInfo;
import omero.model.IObject;
import omero.model.Pixels;

public class ImportContainer
{
	private Long projectID;
	private String reader;
    private String[] usedFiles;
    private Boolean isSPW;
	private File file;

    /**
     * The number of Images (also referred to as <i>Series</i>) that
     * Bio-Formats has detected.
     */
    private Integer bfImageCount;

	/**
     * Image dimensions populated OMERO Pixels objects for each of the Images
     * (also referred to as <i>Series</i>) that Bio-Formats has detected.
     * The length of this collection is equivilent to the <i>Series</i> count
     * and is ordered as the <i>Series</i> are ordered. <b>NOTE:</b> Each
     * OMERO Pixels object is populated with a bogus <code>PixelsType</code>
     * whose value has been extracted from Bio-Formats. It <b>must not</b> be
     * saved into OMERO.
     */
    private List<Pixels> bfPixels;

    /**
     * Image names populated for each of the Images (also referred to as
     * <i>Series</i>)that Bio-Formats has detected. The length of this
     * collection is equivilent to the <i>Series</i> count and is ordered as
     * the <i>Series</i> are ordered. <b>NOTE:</b> This listmay be sparse,
     * contain <code>null</code> values or empty length strings. Caution should
     * be exercised when working directly with this metadata.
     */
    private List<String> bfImageNames;
    
    private Boolean archive;
    private Double[] userPixels;
    private String customImageName;
    private IObject target;
    private FileInfo[] fileInfo;
    
	public ImportContainer(File file, Long projectID,
			IObject target, 
			Boolean archive, 
			Double[] userPixels, String reader, String[] usedFiles, Boolean isSPW)
	{
		this.file = file;
		this.projectID = projectID;
		this.target = target;
		this.archive = archive;
		this.userPixels = userPixels;
		this.reader = reader;
		this.usedFiles = usedFiles;
		this.isSPW = isSPW;
	}
	
	// Various Getters and Setters //
	
    public Integer getBfImageCount() {
		return bfImageCount;
	}

	public void setBfImageCount(Integer bfImageCount) {
		this.bfImageCount = bfImageCount;
	}

	public List<Pixels> getBfPixels() {
		return bfPixels;
	}

	public void setBfPixels(List<Pixels> bfPixels) {
		this.bfPixels = bfPixels;
	}

	public List<String> getBfImageNames() {
		return bfImageNames;
	}

	public void setBfImageNames(List<String> bfImageNames) {
		this.bfImageNames = bfImageNames;
	}
    
    /**
     * @return custom image name string
     */
    public String getCustomImageName()
    {
        return customImageName;
    }   

	/**
	 * Sets the custom image name for import. If this value is left
	 * null, the image name supplied by bio-formats will be used.
	 * 
	 * @param name - a custom image name to set for this image
	 */
	public void setCustomImageName(String name)
	{
	    this.customImageName = name;
	}
	
    public String getReader() {
		return reader;
	}

	public void setReader(String reader) {
		this.reader = reader;
	}

	public String[] getUsedFiles() {
		return usedFiles;
	}

	public void setUsedFiles(String[] usedFiles) {
		this.usedFiles = usedFiles;
	}

	public Boolean getIsSPW() {
		return isSPW;
	}

	public void setIsSPW(Boolean isSPW) {
		this.isSPW = isSPW;
	}
	
	/**
	 * @return the File
	 */
	public File getFile() {
		return file;
	}
	
	/**
	 * Package-private setter added during the 4.1 release to fix name ordering.
	 * A better solution would be to have a copy-constructor which also takes
	 * a chosen file.
	 */
	void setFile(File file)
	{
	    this.file = file;
	}
    
    /**
     * @return Returns the fileInfo.
     */
    public FileInfo[] getFileInfo()
    {
        return fileInfo;
    }

    
    /**
     * @param fileInfo The fileInfo to set.
     */
    public void setFileInfo(FileInfo[] fileInfo)
    {
        this.fileInfo = fileInfo;
    }

    
    /**
     * @return Returns the projectID.
     */
    public Long getProjectID()
    {
        return projectID;
    }

    /**
     * @return Returns the projectID.
     */
    public void setProjectID(Long projectID)
    {
        this.projectID = projectID;
    }
    	
	public IObject getTarget()
	{
	    return target;
	}
	
	public void setTarget(IObject obj) {
	    this.target = obj;
	}
	
	public Double[] getUserPixels()
	{
        return userPixels;
    }
	
    public void setUserPixels(Double[] userPixels)
    {
        this.userPixels = userPixels;
    }   
    
    public void setArchive(boolean archive)
    {
        this.archive = archive;
    }

    public boolean getArchive() {
        return this.archive;
    }
}
