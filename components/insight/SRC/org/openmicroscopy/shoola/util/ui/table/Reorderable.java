/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2014-2015 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
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

/** 
 * Reorderable interface intended to be used for JTables
 *
 * Based on a stackoverflow post by Aaron Davidson:
 * http://stackoverflow.com/questions/638807/how-do-i-drag-and-drop-a-row-in-a-jtable
 * 
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp;
 *          <a href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 */
package org.openmicroscopy.shoola.util.ui.table;

import javax.swing.table.TableModel;

/**
 * Interface for a {@link TableModel}, declaring that it supports
 * reordering of rows; see {@link TableRowTransferHandler}
 * 
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 */
public interface Reorderable {

	/**
	 * Moves certain rows of a table from fromIndices to toIndex
	 * @param fromIndices See above
	 * @param toIndex See above
	 * @return The new index where the rows have been inserted
	 */
	public int reorder(int[] fromIndices, int toIndex);

}
