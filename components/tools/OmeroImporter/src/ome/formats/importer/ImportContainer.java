/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.formats.importer;

import java.io.File;

import omero.model.IObject;

public class ImportContainer
{
	public File file;
	public Long projectID;
	private IObject target;
	public String imageName;
	public boolean archive;
    public Double[] userPixels;

	public ImportContainer(File file, Long projectID,
			IObject target, 
			String imageName, boolean archive, 
			Double[] userPixels)
	{
		this.file = file;
		this.projectID = projectID;
		this.target = target;
		this.imageName = imageName;
		this.archive = archive;
		this.userPixels = userPixels;
	}

	public IObject getTarget()
	{
	    return target;
	}	
}
