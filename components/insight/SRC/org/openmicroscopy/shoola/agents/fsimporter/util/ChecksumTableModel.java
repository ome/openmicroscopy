/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2013 University of Dundee & Open Microscopy Environment.
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
package org.openmicroscopy.shoola.agents.fsimporter.util;

import java.util.List;
import java.util.Map;

import javax.swing.table.DefaultTableModel;

/** 
 * Provides a TableModel for checksum information.
 * @author Scott Littlewood, <a href="mailto:sylittlewood@dundee.ac.uk">sylittlewood@dundee.ac.uk</a>
 * @since 4.4
 */
class ChecksumTableModel
	extends DefaultTableModel
{

	/** The column index indicating if it is valid or not.*/
	static final int VALID_COLUMN = 3;

	/** Column headers to be used by the checksum table */
	private final String[] columnNames = {"Filename", "Client Checksum",
            "Server Checksum", ""};
	
	/**
	 * Creates the table model for the data provided.
	 * 
	 * @param checksumFiles The files with checksums
	 * @param checksums The client side checksums
	 * @param failingChecksums The failing checksum values
	 */
    ChecksumTableModel(String[] checksumFiles,
			List<String> checksums, Map<Integer, String> failingChecksums) {
		setData(checksumFiles, checksums, failingChecksums);
	}

    /**
     * Overridden so that the user cannot edit the cell.
     * @see DefaultTableModel#isCellEditable(int, int)
     */
	public boolean isCellEditable(int row, int col) { return false; }
	
	/**
	 * Sets the table data using the information provided
	 * @param checksumFiles The files with checksums
	 * @param checksums The client side checksums
	 * @param failingChecksums The failing checksum values
	 */
	private void setData(String[] checksumFiles,
			List<String> checksums,
			Map<Integer, String> failingChecksums) {
		Object[][] data = new Object[checksumFiles.length][4];
		for (int i = 0; i < checksumFiles.length; i++) {
			data[i][0] = checksumFiles[i];
			data[i][1] = checksums.get(i);
			if (failingChecksums.containsKey(i)) {
				data[i][2] = failingChecksums.get(i);
				data[i][3] = Boolean.valueOf(false);
			} else {
				data[i][2] = checksums.get(i);
				data[i][3] = Boolean.valueOf(true);
			}
		}
		setDataVector(data, columnNames);
	}

}