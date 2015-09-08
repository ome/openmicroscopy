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

import org.openmicroscopy.shoola.agents.fsimporter.view.Importer;
import org.openmicroscopy.shoola.agents.util.browser.DataNode;

import omero.gateway.model.ExperimenterData;
import omero.gateway.model.GroupData;

/** 
 * Provides cohesion of the import settings when importing project/dataset data.
 * @author Scott Littlewood, 
 * <a href="mailto:sylittlewood@dundee.ac.uk">sylittlewood@dundee.ac.uk</a>
 * @since Beta4.4
 */
class ProjectImportLocationSettings extends ImportLocationSettings
{
	/** Defines the parent project where data will be imported */
	private DataNode importToProject;
	
	/** Defines the parent dataset where data will be imported */
	private DataNode importToDataset;
	
	/**
	 * Creates a DTO for collating Project/Dataset/Image import settings.
	 * @param group The permission group to import data in to
	 * @param project The project to import data in to
	 * @param dataset The dataset to import data in to
	 */
	ProjectImportLocationSettings(GroupData group, ExperimenterData user,
			DataNode project, DataNode dataset) {
		super(Importer.PROJECT_TYPE, group, user);
		
		this.importToProject = project;
		this.importToDataset = dataset;
	}

	/**
	 * Implemented as specified by @see ImportLocationSettings.
	 * @return The dataset selected to import data in to.
	 */
	DataNode getImportLocation() {
		return importToDataset;
	}

	/**
	 * Implemented as specified by @see ImportLocationSettings.
	 * @return The project selected to import data in to.
	 */
	DataNode getParentImportLocation() {
		return importToProject;
	}

	/**
	 * Implemented as specified by @see ImportLocationSettings.
	 * @return @see ImportLocationSettings
	 */
	boolean isParentFolderAsDataset() {
		if (importToDataset == null)
			return false;
		
		return importToDataset.isDefaultDataset();
	}
}
