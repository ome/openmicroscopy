/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2012 University of Dundee & Open Microscopy Environment.
 *  All rights reserved.
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
package org.openmicroscopy.shoola.agents.fsimporter.chooser;

import org.openmicroscopy.shoola.agents.util.browser.DataNode;

import pojos.ExperimenterData;
import pojos.GroupData;

/** 
 * Provides an abstract class for specialisation of use import settings.
 * @author Scott Littlewood,
 * <a href="mailto:sylittlewood@dundee.ac.uk">sylittlewood@dundee.ac.uk</a>
 * @since Beta4.4
 */
public abstract class ImportLocationSettings {

	/** Defines the group data to be imported in to */
	private GroupData importGroup;
	
	/** Defines the type of data being imported */
	private int importDataType;

	/** Defines the user that will own the data on import*/
	private ExperimenterData importUser;
	
	/**
	 * Creates a ImportLocationSettings for passing user selection data.
	 * @param type The identifier for this type of data.
	 * @param group The user group to import data in to.
	 */
	ImportLocationSettings(int type, GroupData group, ExperimenterData user)
	{
		this.importDataType = type;
		this.importGroup = group;
		this.importUser = user;
	}

	/**
	 * The group to import data in to
	 * @return See above.
	 */
	GroupData getImportGroup()
	{
		return importGroup;
	}
	
	/**
	 * The user to import data for
	 * @return See above.
	 */
	ExperimenterData getImportUser()
	{
		return importUser;
	}
	
	/**
	 * The type of data being imported, Project / Screen.
	 * @return See above.
	 */
	int getImportDataType()
	{
		return importDataType;
	}

	/** To be implemented by the subclass to say which object
	 * an item should be imported in to
	 */
	abstract DataNode getImportLocation();

	/** To be implemented by the subclass to say which parent object
	 * an item should be imported in to
	 */
	abstract DataNode getParentImportLocation();

	/** To be implemented by the subclass to say whether an images directory
	 * should be used as a new dataset name.
	 */
	abstract boolean isParentFolderAsDataset();

}

