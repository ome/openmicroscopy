/*
 * org.openmicroscopy.shoola.env.ui.resultstable.TableModel 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2011 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.util.flim.resultstable;

//Java imports
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.table.AbstractTableModel;

//Third-party libraries

//Application-internal dependencies

/** 
 * Displays the results stored in the passed file.
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
class ResultsTableModel 
	extends AbstractTableModel
{
	
	/** The collection of column's names being displayed. */
	private List<String>				columnNames;
	
	/** Collection of <code>Object</code>s hosted by this model. */
	private Map<Integer, ResultsObject>		values;
		
	/** The set of all columns in the table. */
	private Set<String> allColumns;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param colNames	The collection of column's names.
	 * 						Mustn't be <code>null</code>.
	 */
	ResultsTableModel(List<String> colNames)
	{
		if (colNames == null)
			throw new IllegalArgumentException("No column's names " +
												"specified.");
		this.columnNames = colNames;
		this.values = new HashMap<Integer, ResultsObject>();
		this.allColumns = new LinkedHashSet<String>();
		for(String column : columnNames)
			allColumns.add(column);
	}
	
	/** 
	 * Adds a new row to the model.
	 * 
	 * @param row The value to add.
	 */
	void addRow(ResultsObject row)
	{
		values.put(values.size(), row);
		fireTableStructureChanged();
	}
	
	void changed()
	{
		fireTableStructureChanged();
	}
	
	/** 
	 * Get a row from the model.
	 * 
	 * @param index The row to return
	 * 
	 * @return ResultsObject the row.
	 */
	ResultsObject getRow(int index)
	{
		if(index < values.size())
			return values.get(index);
		return null;
	}
	
	/**
	 * Returns the value of the specified cell.
	 * @see AbstractTableModel#getValueAt(int, int)
	 */
	public Object getValueAt(int row, int col) 
    {
		if (row < 0 || row > values.size()) return null;
		ResultsObject rowData = values.get(row);
		String column = getKeyFromIndex(col); 
		if(column!=null)
			return rowData.getElement(column);
		return null;
	}
    
	/**
	 * Sets the specified value.
	 * @see AbstractTableModel#setValueAt(Object, int, int)
	 */
    public void setValueAt(Object value, int row, int col) 
    {
    	if(row<0 && row >= values.size())
    		return;
    	if(col<0 && col >= columnNames.size())
    		return;
    	ResultsObject rowData = values.get(row);
    	String columnName = columnNames.get(col);
    	rowData.setElement(columnName, value);
		fireTableStructureChanged();
    }
    
    /**
     * Add the column to the list of all columns.
     * @param column See above.
     */
    public void addColumn(String column)
    {
    	allColumns.add(column);
    }
    
    /**
     * Get the list of all columns used in the model.
     * @return See above.
     */
    public List<String> getAllColumns()
    {
    	List<String> cols = new ArrayList<String>();
    	Iterator<String> columnIterator = allColumns.iterator();
    	while(columnIterator.hasNext())
    		cols.add(columnIterator.next());
    	return cols;
    }
    
    /**
     * Get the list of all columns in the table.
     * @return
     */
    public List<String> getColumns()
    {
    	return columnNames;
    }
    
	/**
	 * Overridden to return the name of the specified column.
	 * @see AbstractTableModel#getColumnName(int)
	 */
	public String getColumnName(int col) 
	{
		return columnNames.get(col);
	}
	
    /**
	 * Overridden to return the number of columns.
	 * @see AbstractTableModel#getColumnCount()
	 */
	public int getColumnCount() { return columnNames.size();  }

	/**
	 * Overridden to return the number of rows.
	 * @see AbstractTableModel#getRowCount()
	 */
	public int getRowCount() { return values.size(); }
	
	/**
	 * Overridden so that the cell is not editable.
	 * @see AbstractTableModel#isCellEditable(int, int)
	 */
	public boolean isCellEditable(int row, int col) 
	{ 
		return false;
	}
	
	/**
	 * Get the key for the index'th column.
	 * @param index See above.
	 * @return See above.
	 */
	private String getKeyFromIndex(int index)
	{
		if(index < columnNames.size())
			return columnNames.get(index);
		return null;
	}
	
	/**
	 * Set the columns in the model to columns. 
	 * @param columns See above.
	 */
	public void setColumns(List<String> columns)
	{
		this.columnNames = columns;
		for(String column : columnNames)
			allColumns.add(column);
		fireTableStructureChanged();
	}
	
	/**
	 * Clear the table.
	 */
	public void clear()
	{
		values.clear();
		fireTableStructureChanged();
	}

}
