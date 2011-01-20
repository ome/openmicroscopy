/*
 * org.openmicroscopy.shoola.util.ui.PlateGridObject 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2010 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
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
package org.openmicroscopy.shoola.util.ui;

//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * Hosts information about the cell selection.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class PlateGridObject 
{

	/** The selected row. */
	private int row;
	
	/** The selected column. */
	private int column;
	
	/** The flag indicating if several cells are selected. */
	private boolean multipleSelection;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param row The selected row.
	 * @param column The selected column.
	 * @param multipleSelection Pass <code>true</code> if several cells are 
	 * 							selected, <code>false</code> otherwise.
	 */
	public PlateGridObject(int row, int column, boolean multipleSelection)
	{
		this.row = row;
		this.column = column;
		this.multipleSelection = multipleSelection;
	}
	
	/**
	 * Returns the selected row.
	 * 
	 * @return See above.
	 */
	public int getRow() { return row; }
	
	/**
	 * Returns the selected row.
	 * 
	 * @return See above.
	 */
	public int getColumn() { return column; }
	
	/**
	 * Returns <code>true</code> if the several cells are selected,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	public boolean isMultipleSelection() { return multipleSelection; }
	
}
