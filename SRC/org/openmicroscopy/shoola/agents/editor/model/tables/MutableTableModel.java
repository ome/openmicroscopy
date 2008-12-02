 /*
 * org.openmicroscopy.shoola.agents.editor.model.params.MutableTableModel 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.editor.model.tables;

import java.util.ArrayList;

//Java imports

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

//Third-party libraries

//Application-internal dependencies

/** 
 * The data structure for the TableParam to store the table data. 
 * Allows rows to be added and deleted. 
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class MutableTableModel 
	extends AbstractTableModel 
	implements IMutableTableModel
{

	/**
	 * The array to hold a list of column names. 
	 */
    protected ArrayList<String> 				columnNames;
    
    /**
     * The 2-dimensional arrayList to hold the data.
     */
    protected ArrayList<ArrayList<Object>> 		data;

    /**
     * Creates an instance of this class.
     * Initialises the data arrays. 
     */
    public MutableTableModel() 
    {	
    	columnNames = new ArrayList<String>();
    	data = new ArrayList<ArrayList<Object>>();
    }
    
    /**
     * Creates an instance of this table model, duplicating the data 
     * in the TableModel argument. 
     * NB. Objects are passed to Strings, to copy from one table to the other, 
     * instead of copying the reference. 
     * 
     * @param tModel
     */
    public MutableTableModel(TableModel tModel)
    {
    	this();
    	
    	int rowCount = tModel.getRowCount();
    	int colCount = tModel.getColumnCount();
    	
    	// populate column names from tModel
    	for (int c=0; c<colCount; c++) {
    		columnNames.add(tModel.getColumnName(c));
		}
    	
    	// populate data from tModel
    	ArrayList<Object> row;
    	for (int r=0 ; r<rowCount ; r++) {
    		row = new ArrayList<Object>();
    		for (int c=0; c<colCount; c++) {
    			row.add(tModel.getValueAt(r, c).toString());
    		}
    		data.add(row);
    	}
    }

    /**
     * Gets the column name
     */
    public String getColumnName(int column) {
        return columnNames.get(column);
    }
    
    /**
     * Sets the name of the column at the specified index. 
     * If the column index is too high (too few columns) then
     * a new column is created. 
     * Either way, {@link #fireTableStructureChanged()} is called
     * to notify the Table that the model has changed.
     * 
     * @param columnIndex		The index of the column		
     * @param name			The new name for the column
     */
    public void setColumnName(int columnIndex, String name)
    {
    	if (columnIndex >= getColumnCount()) {
    		addEmptyColumn(name);
    	} else {
    		columnNames.remove(columnIndex);
    		columnNames.add(columnIndex, name);
    	}
    }

    /**
     * All cells are editable. Returns true.
     */
    public boolean isCellEditable(int row, int column) {
        return true;
    }

    /**
     * Returns the value in the specified cell. 
     * If the row or column count are out of range, 
     * an empty string is returned. 
     * 
     * @see TableModel#getValueAt(int, int)
     */
    public Object getValueAt(int row, int column) {
    	
    	if ((row < getRowCount()) && (column < getColumnCount())) {
    		Object value = data.get(row).get(column);
    		if (value == null) 
    			return "";
    		
    		return value;
    	}
    	return "";
    }

    /**
     * Edits the value in the data,
     * then calls fireTableCellUpdated(row, column)
     * 
     * @see TableModel#setValueAt(Object, int, int)
     */
    public void setValueAt(Object value, int row, int column) {
        data.get(row).set(column, (String)value);
        fireTableCellUpdated(row, column);
    }

    /**
     * Returns the number of rows.
     * 
     * @see TableModel#getRowCount()
     */
    public int getRowCount() { return data.size(); }

    /**
     * Returns the column count, as defined by the number of column names. 
     */
    public int getColumnCount() { return columnNames.size(); }

    /**
     * Adds a new row to the data model, at the bottom of the table
     */
    public void addEmptyRow() 
    {
    	ArrayList<Object> newRow = new ArrayList<Object>();
    	for (int i=0; i<getColumnCount(); i++) {
    		newRow.add("");
    	}
        data.add(newRow);
        
        fireTableRowsInserted(
           getRowCount() - 1,
           getRowCount() - 1);
    }
    
    /**
     * Adds a new row to the data model, at the specified row of the table
     */
    public void addEmptyRow(int addAtThisRow) 
    {
    	ArrayList<Object> newRow = new ArrayList<Object>();
    	for (int i=0; i<getColumnCount(); i++) {
    		newRow.add("");
    	}
    	int newRowIndex = addAtThisRow;
    	if (newRowIndex > data.size()) {
    		data.add(newRow);
    		newRowIndex = getRowCount() -1;
    	} else {
    		data.add(newRowIndex, newRow);
    	}
    	fireTableRowsInserted(newRowIndex, newRowIndex);
    }
    
    /**
     * Adds a new Column.
     * Adds a new cell to the end of every row.
     * Then calls fireTableStructureChanged()
     */
    public void addEmptyColumn(String colName) 
    {	
    	columnNames.add(colName);
    	
    	for(ArrayList<Object> row: data) {
    		row.add("");
    	}
    	
    	// this updates the table, which updates it's own ColumnModel (I think).
    	fireTableStructureChanged();
    }
    
    /**
     * Removes the last column
     */
    public void removeLastColumn() 
    {	
    	columnNames.remove(columnNames.size()-1);
    	
    	// getColumnCount is now one smaller
    	int colCount = getColumnCount();
    	for(ArrayList<Object> row: data) {
    		row.remove(colCount);
    	}
    	fireTableStructureChanged();
    }
    
    /**
     * Remove rows.
     * Rows in array must be in increasing order eg 1,2,5
     * 
     * @param rowIndecies
     */
    public void removeRows(int[] rowIndecies) 
    {
    	// remove rows, starting at the highest!
    	for (int i=rowIndecies.length-1; i>-1; i--) {
    		int rowToRemove = rowIndecies[i];
    		data.remove(rowToRemove);
    		fireTableRowsDeleted(rowToRemove, rowToRemove);
    	}
    }
}
