/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.formats.importer;

import java.io.File;
import java.util.List;

import loci.formats.FileInfo;
import omero.model.IObject;
import omero.model.Pixels;

public class ImportContainer
{
	public File file;
	public Long projectID;
	public String imageName;
    public String reader;
    public String[] usedFiles;
    public Boolean isSPW;

    /** The number of Images that Bio-Formats has detected. */
    public Integer bfImageCount;

    /**
     * Image dimensions populated OMERO Pixels objects for each of the Images
     * that Bio-Formats has detected.
     */
    public List<Pixels> bfPixels;

    /**
     * Image names populated for each of the Images that Bio-Formats has
     * detected.<b>NOTE</b> This list may be sparse, contain <code>null</code>
     * values or empty length strings. Caution should be exercised when
     * working directly with this metadata.
     */    
    public List<String> bfImageNames;

    private Boolean archive;
    private Double[] userPixels;
    private String userSpecifiedImageName;
    private IObject target;
    private FileInfo[] fileInfo;
    
	public ImportContainer(File file, Long projectID,
			IObject target, 
			String imageName, Boolean archive, 
			Double[] userPixels, String reader, String[] usedFiles, Boolean isSPW)
	{
		this.file = file;
		this.projectID = projectID;
		this.target = target;
		this.imageName = imageName;
		this.archive = archive;
		this.userPixels = userPixels;
		this.reader = reader;
		this.usedFiles = usedFiles;
		this.isSPW = isSPW;
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
    
    /**
     * @return Returns the imageName.
     */
    public String getImageName()
    {
        return imageName;
    }

    /**
     * @return Returns the imageName.
     */
    public void setImageName(String imageName)
    {
        this.imageName = imageName;
    }
    
    public String getUserSpecifiedName()
    {
        return userSpecifiedImageName;
    }   

	public void setUserSpecifiedFileName(String name)
	{
	    this.userSpecifiedImageName = name;
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
