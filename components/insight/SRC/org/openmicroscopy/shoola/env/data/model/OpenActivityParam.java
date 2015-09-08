/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2010 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
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

import javax.swing.Icon;

import org.openmicroscopy.shoola.util.image.geom.Factory;

/** 
 * Helper class storing information about the data object to open with an
 * external application.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since 3.0-Beta4
 */
public class OpenActivityParam
{

	/** Information about the external application. */
	private ApplicationData application;
	
	/** The object to open. */
	private omero.gateway.model.DataObject object; 
	
	/** The path to the folder. */
	private String folderPath;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param application Information about the external application.
	 * @param object The object to open.
	 */
	public OpenActivityParam(ApplicationData application, 
			omero.gateway.model.DataObject object, String folderPath)
	{
		this.application = application;
		this.object = object;
		this.folderPath = folderPath;
	}
	
	/**
	 * Returns the path to the folder.
	 * 
	 * @return See above.
	 */
	public String getFolderPath() { return folderPath; }
	
	/**
	 * Returns a 22x22 icon corresponding to the activity.
	 * 
	 * @return See above.
	 */
	public Icon getIcon()
	{
		Icon icon = getApplication().getApplicationIcon();
		if (icon == null) return null;
		return Factory.scaleIcon(icon, 22, 22);
	}
	
	/**
	 * Returns the name of the application w/o extension if any.
	 * 
	 * @return See above.
	 */
	public String getLabel()
	{
		return getApplication().getApplicationName();
	}
	
	/**
	 * Returns information about the external application.
	 * 
	 * @return See above.
	 */
	public ApplicationData getApplication() { return application; }
	
	/**
	 * Returns the object to open.
	 * 
	 * @return See above.
	 */
	public omero.gateway.model.DataObject getObject() { return object; }
	
}
