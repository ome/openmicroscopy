/*
 * org.openmicroscopy.shoola.agents.measurement.util.ChannelSummaryModel 
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
package org.openmicroscopy.shoola.agents.measurement.util;


//Java imports
import java.util.List;
import javax.swing.table.AbstractTableModel;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.UIUtilities;

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
public class ChannelSummaryModel
	extends AbstractTableModel
{	

	/** The data in the table. */
	private Double[][]	data;
	
	/** The number of columns in the model. */
	private int			numColumns;
	
	/** The number of the rows in the model. */
	private int			numRows;
	
	/** The names of the channels being summarised. */
	private List<String> columnNames;
	
	/** The names of the data being summarised. */
	private List<String> rowNames;

	/**
	 * Model of the ChannelSummaryTable to the summary of the values in the
	 * current ROI selection.
	 * 
	 * @param rowNames the names of the rows in the table.
	 * @param columnNames the names of the columns in the table.
	 * @param data The data in the table.
	 */
	public ChannelSummaryModel(List<String> rowNames, List<String> columnNames, 
			Double[][] data)
	{
		this.data = data;
		this.rowNames = rowNames;
		this.columnNames = columnNames;
		this.setColumnCount(columnNames.size());
		this.setRowCount(rowNames.size());
		this.numColumns = columnNames.size()+1;
		this.numRows = rowNames.size();
	}
	
	/**
	 * Returns the name of the rows.
	 * 
	 * @return See above.
	 */
	public List<String> getRowNames() { return rowNames; }
	
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
	 * 
	 * @see javax.swing.table.TableModel#setValueAt(Object, int, int)
	 */
	public void setValueAt(Object value, int row, int col)
	{

	}
	
	/**
	 * Overridden to return the name of the specified column.
	 * @see AbstractTableModel#getColumnName(int)
	 */
	public String getColumnName(int col)
	{
		if (col == 0) return "";
		return columnNames.get(col-1);
	}
	
	/**
	 * Overridden to return the value of the model to the object.
	 * @see javax.swing.table.TableModel#getValueAt(int, int)
	 */
	public Object getValueAt(int row, int col)
	{
		if (col >= numColumns || row >= numRows)  return null;
		if (col == 0) return(rowNames.get(row));
		else
			if (data[col-1][row] != null)
				return UIUtilities.formatToDecimal(data[col-1][row]);
			else
				return null;
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
	 * @param row  The value to set.
	 */
	public void setRowCount(int row) { numRows = row; }

}

