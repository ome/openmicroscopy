/*
 * org.openmicroscopy.shoola.util.ui.WellGridElement 
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
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
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
 * Indicates the row number and the column number and various other information.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class WellGridElement 
{

	/** The row number. */
	private int row;
	
	/** The column number. */
	private int column;
	
	/** Flag indicating if the well is valid or not. */
	private boolean valid;
	
	/** The tool tip text for that component.*/
	private String text;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param row The row to set.
	 * @param column The column to set.
	 * @param valid Pass <code>true</code> to indicate that it is a valid well.
	 *				<code>false</code> otherwise.
	 */
	public WellGridElement(int row, int column, boolean valid)
	{
		if (row < 0) 
			throw new IllegalArgumentException("row not valid.");
		if (column < 0) 
			throw new IllegalArgumentException("column not valid.");
		this.row = row;
		this.column = column;
		this.valid = valid;
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param row The row to set.
	 * @param column The column to set.
	 */
	public WellGridElement(int row, int column)
	{
		this(row, column, true);
	}
	
	/**
	 * Returns the row.
	 * 
	 * @return See above.
	 */
	public int getRow() { return row; }
	
	/**
	 * Returns the column.
	 * 
	 * @return See above.
	 */
	public int getColumn() { return column; }
	
	/**
	 * Returns <code>true</code> if it is a valid well, <code>false</code>
	 * otherwise.
	 * 
	 * @return See above.
	 */
	public boolean isValid() { return valid; }
	
}
