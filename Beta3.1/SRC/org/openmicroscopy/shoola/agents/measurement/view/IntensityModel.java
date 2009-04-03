/*
 * org.openmicroscopy.shoola.agents.measurement.view.IntensityModel 
 *
  *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.measurement.view;


//Java imports
import javax.swing.table.AbstractTableModel;

//Third-party libraries

//Application-internal dependencies

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class IntensityModel
	extends AbstractTableModel
{	
	
	/** The data in the table. */
	private Double[][] 					data;
	
	
	/** The number of columns in the model. */
	private int							numColumns;
	
	/** The number of the rows in the model. */
	private int 						numRows;
	
	
	/**
	 * Model of the IntensityTable to the intensity values in the current ROI
	 * selection.
	 * 
	 * @param data	The data in the table.
	 */
	IntensityModel(Double[][] data)
	{
		this.data = data;
		this.setColumnCount(data.length);
		this.setRowCount(data[0].length);
	}
		
	/**
	 * Overridden to return the number of columns.
	 * @see javax.swing.table.TableModel#getColumnCount()
	 */
	public int getColumnCount() { return numColumns; }

	/**
	 * Overridden to return the number of rows.
	 * @see javax.swing.table.TableModel#getRowCount()
	 */
	public int getRowCount() { return numRows; }

	/** 
	 * Overridden to set the value of the model to the object.
	 * @see javax.swing.table.TableModel#setValueAt(Object, int, int)
	 */
	public void setValueAt(Object value, int row, int col)
	{
		
	}
	
	/**
	 * Overridden to return the value of the model to the object.
	 * @see javax.swing.table.TableModel#getValueAt(int, int)
	 */
	public Object getValueAt(int row, int col)
	{
		if (col >= numColumns || row >= numRows)
			return null;
		return data[col][row];
	}
	
	/**
	 * Sets the number of columns in the table to col.
	 * 
	 * @param col The value to set.
	 */
	public void setColumnCount(int col) { numColumns = col; }
	
	/**
	 * Sets the number of rows in the table to col.
	 * 
	 * @param row The value to set.
	 */
	public void setRowCount(int row) { numRows = row; }
	
}

