/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.formats.importer;

import java.io.File;

import loci.formats.FileInfo;

import omero.model.IObject;

public class ImportContainer
{
	final public File file;
	final public Long projectID;
	final public String imageName;
    final public String reader;
    final public String[] usedFiles;
    final public boolean isSPW;
    
    private boolean archive;
    private Double[] userPixels;
    private String userSpecifiedImageName;
    private IObject target;
    private FileInfo[] fileInfo;
    
	public ImportContainer(File file, Long projectID,
			IObject target, 
			String imageName, boolean archive, 
			Double[] userPixels, String reader, String[] usedFiles, boolean isSPW)
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
