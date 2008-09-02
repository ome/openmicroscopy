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
package org.openmicroscopy.shoola.agents.editor.model.params;

import java.util.ArrayList;

//Java imports

import javax.swing.table.AbstractTableModel;

//Third-party libraries

//Application-internal dependencies

/** 
 * The data structure for the TableParam to store the table data. 
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
{

	/**
	 * The array to hold a list of column names. 
	 */
    protected ArrayList<String> 				columnNames;
    
    /**
     * The 2-dimensional arrayList to hold the data.
     */
    protected ArrayList<ArrayList<String>> 		data;

    /**
     * Creates an instance of this class.
     * Initialises the data arrays. 
     */
    public MutableTableModel() 
    {	
    	columnNames = new ArrayList<String>();
    	
    	data = new ArrayList<ArrayList<String>>();
    }

    /**
     * Gets the column name
     */
    public String getColumnName(int column) {
        return columnNames.get(column);
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
    		String value = data.get(row).get(column);
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
     * Is the last row empty?
     * 
     * @return	True if the last row has no filled cells.
     */
    public boolean isLastRowEmpty() 
    {
        if (getRowCount() == 0) return false;
        int lastRow = getRowCount() -1;
        
        return isRowEmpty(lastRow);
    }
    
    /**
     * Is the specified row empty? 
     * 
     * @param row
     * @return	True if the row has no filled cells.
     */
    public boolean isRowEmpty(int row) 
    {
    	for (int col=0; col<data.get(row).size(); col++){
    		String value = (String)getValueAt(row, col);
    		if(value.trim().length() > 0) return false;
    	}
    	return true;
    }

    /**
     * Adds a new row to the data model, at the bottom of the table
     */
    public void addEmptyRow() 
    {
    	ArrayList<String> newRow = new ArrayList<String>();
    	for (int i=0; i<getColumnCount(); i++) {
    		newRow.add(" ");
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
    	ArrayList<String> newRow = new ArrayList<String>();
    	for (int i=0; i<getColumnCount(); i++) {
    		newRow.add(" ");
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
    	
    	for(ArrayList<String> row: data) {
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
    	for(ArrayList<String> row: data) {
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
    		System.out.println("InteractiveTableModel Removing row " + rowToRemove);
    		data.remove(rowToRemove);
    		fireTableRowsDeleted(rowToRemove, rowToRemove);
    	}
    }
}
