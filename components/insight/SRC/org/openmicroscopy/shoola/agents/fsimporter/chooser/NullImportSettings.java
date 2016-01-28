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

import omero.gateway.model.DataObject;
import omero.gateway.model.ExperimenterData;
import omero.gateway.model.GroupData;

/** 
 * Provides a Null implementation of import settings, should NEVER be returned.
 * @author Scott Littlewood, <a href="mailto:sylittlewood@dundee.ac.uk">sylittlewood@dundee.ac.uk</a>
 * @since Beta4.4
 */
class NullImportSettings extends ImportLocationSettings {

	/** Local constant to specify an unknown data type*/
	private static final int UNKNOWN_DATA_TYPE = -1;

	/**
	 * Creates a NullImportSettings with the group @see ImportLocationSettings
	 * @param group The group to import data in to.
	 */
	NullImportSettings(GroupData group, ExperimenterData user) {
		super(UNKNOWN_DATA_TYPE, group, user);
	}

	/**
	 * Returns a new Null Data Node
	 * @return See above.
	 */
	public DataNode getImportLocation() {
		return new DataNode((DataObject) null);
	}

	/**
	 * Returns a new Null Data Node
	 * @return See above.
	 */
	public DataNode getParentImportLocation() {
		return new DataNode((DataObject) null);
	}

	/**
	 * Returns false.
	 * @return See above.
	 */
	public boolean isParentFolderAsDataset() {
		return false;
	}

}
