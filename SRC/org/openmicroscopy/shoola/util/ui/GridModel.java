/*
 * org.openmicroscopy.shoola.util.ui.GridModel 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2009 University of Dundee. All rights reserved.
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
import javax.swing.table.DefaultTableModel;

//Third-party libraries

//Application-internal dependencies

/** 
 * A non editable model. 
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
public class GridModel
	extends DefaultTableModel
{
	
	/**
	 * Creates a new instance.
	 * 
	 * @param rows		The number of rows the table holds.
	 * @param columns    The number of columns the table holds.
	 */
	public GridModel(int rows, int columns)
	{
		super(rows, columns);
	}
	
	/**
	 * Overridden to return <code>false</code> regardless of the value.
	 * @see DefaultTableModel#isCellEditable(int, int)
	 */
    public boolean isCellEditable(int row, int column) { return false; }

}
