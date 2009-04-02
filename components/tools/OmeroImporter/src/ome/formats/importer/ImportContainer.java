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
	private Class<? extends IObject> targetClass;
	private Long targetID; 
	public String imageName;
	public boolean archive;

	public ImportContainer(File file, Long projectID,
			Class<? extends IObject> targetClass, 
			Long targetID, String imageName,
			boolean archive)
	{
		this.file = file;
		this.projectID = projectID;
		this.targetClass = targetClass;
		this.targetID = targetID;
		this.imageName = imageName;
		this.archive = archive;
	}

	public Class<? extends IObject> getTargetClass()
	{
		return targetClass;
	}

	public Long getTargetID()
	{
		return targetID;
	}
}
