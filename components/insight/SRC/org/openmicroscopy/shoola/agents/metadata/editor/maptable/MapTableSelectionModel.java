/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2015 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
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

package org.openmicroscopy.shoola.agents.metadata.editor.maptable;

import javax.swing.DefaultListSelectionModel;
import javax.swing.ListSelectionModel;

/**
 * SelectionModel for a {@link MapTable}
 */
public class MapTableSelectionModel extends DefaultListSelectionModel {

	/** Reference to the {@link MapTable} */
	private MapTable table;

	/**
	 * Creates a new instance
	 * 
	 * @param table
	 *            Reference to the {@link MapTable}
	 */
	public MapTableSelectionModel(MapTable table) {
		this.table = table;
		setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
	}

	/**
	 * Get the {@link MapTable} this model belongs to
	 * 
	 * @return See above
	 */
	public MapTable getTable() {
		return table;
	}

}
