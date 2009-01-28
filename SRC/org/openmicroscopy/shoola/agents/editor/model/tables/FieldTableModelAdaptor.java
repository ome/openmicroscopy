 /*
 * org.openmicroscopy.shoola.agents.editor.model.params.FieldDataTableModel 
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

//Java imports

import java.util.List;

import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

//Third-party libraries

//Application-internal dependencies

import org.openmicroscopy.shoola.agents.editor.model.IField;
import org.openmicroscopy.shoola.agents.editor.model.params.AbstractParam;
import org.openmicroscopy.shoola.agents.editor.model.params.FieldParamsFactory;
import org.openmicroscopy.shoola.agents.editor.model.params.IParam;
import org.openmicroscopy.shoola.agents.editor.model.params.NumberParam;

/** 
 * A table model adaptor that enables multiple values for the parameters of
 * a {@link IField} to be represented in a table. 
 * Each {@link Param} parameter represents a column, with the values for that
 * parameter forming the rows. 
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class FieldTableModelAdaptor
	extends AbstractTableModel 
	implements IMutableTableModel {
	
	/** The field that provides the data for the table */
	private IField 					field;

	/**
	 * Creates an instance. 
	 * Creates an empty row. 
	 * 
	 * @param field			The field that provides data for this table
	 */
	public FieldTableModelAdaptor(IField field) {
		this.field = field;
	}

	/**
	 * Implemented as specified by the {@link TableModel} interface
	 * Returns the number of {@link IParam} parameters in the field.
	 * 
	 * @see TableModel#getColumnCount()
	 */
	public int getColumnCount() {
		return field.getAtomicParams().size();
	}

	/**
	 * Implemented as specified by the {@link TableModel} interface
	 * Returns the length of the longest value list for 
	 * each {@link IParam} parameter in the field.
	 * 
	 * @see TableModel#getRowCount()
	 */
	public int getRowCount() {
		List<IParam> params = field.getAtomicParams();
		int maxRows = 0;
		int rows;
		for (IParam param : params) {
			rows = param.getValueCount();
			maxRows = Math.max(maxRows, rows);
		}
		return maxRows;
	}

	/**
	 * Implemented as specified by the {@link TableModel} interface
	 * Returns the Object from the {@link IParam} specified by columnIndex, at
	 * the index specified by rowIndex.
	 * 
	 * @see TableModel#getValueAt(int, int)
	 */
	public Object getValueAt(int rowIndex, int columnIndex) {
		
		List<IParam> params = field.getAtomicParams();
		if (columnIndex > params.size())
			return null;
		
		IParam param = params.get(columnIndex);
		if (param.getValueCount()-1 < rowIndex)
			return null;
		
		return param.getValueAt(rowIndex);
	}
	
	/**
	 * Overrides the empty implementation of {@link AbstractTableModel} to 
	 * allow setting of values for parameters of this field. 
	 * 
	 * @see AbstractTableModel#setValueAt(Object, int, int)
	 */
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
    	
    	List<IParam> params = field.getAtomicParams();
		if (columnIndex > params.size()) return;
		
		params.get(columnIndex).setValueAt(rowIndex, aValue);
    }
	
    /**
     * Overrides the default implementation of {@link AbstractTableModel} to 
     * display the name (or type) of each parameter 
     * 
     * @see AbstractTableModel#getColumnName(int)
     */
	public String getColumnName(int col) {
		
		List<IParam> params = field.getAtomicParams();
		if (col > params.size())
			return "";
		
		IParam p = params.get(col);
		String name = p.getAttribute(AbstractParam.PARAM_NAME);
		
		// add units if they exist (Number parameter or enumeration parameter)
		String units = p.getAttribute(NumberParam.PARAM_UNITS);
		if (units != null)
			name = (name == null ? "" : name + " ") + "(" + units + ")";
		
		// if no name for this parameter, return it's type. 
		if (name == null) {
			name = params.get(col).getAttribute(AbstractParam.PARAM_TYPE);
			name = FieldParamsFactory.getTypeForDisplay(name);
		}
		
        return (name == null ? "" : name);
    }
	
	/**
     * Overrides the default implementation of {@link AbstractTableModel} to 
     * allow editing (returns true). 
     * 
     * @see AbstractTableModel#isCellEditable(int, int)
     */
	 public boolean isCellEditable(int rowIndex, int columnIndex) {
		return true;
	 }
	 
	 /**
     * Overrides the default implementation of {@link AbstractTableModel} to 
     * return the class of the Object in the first row of the column. 
     * Causes {@link JTable} to display a checkBox for boolean data. 
     * 
     * @see AbstractTableModel#getColumnClass(int)
     */
     public Class<? extends Object> getColumnClass(int col) {
    	 
    	 List<IParam> params = field.getAtomicParams();
    	 if (col > params.size())
    		 return Object.class;
    	 
    	 // JTable should render the Boolean as a check-box
    	 if (params.get(col).getValueCount() > 0) {
    		 Object obj = params.get(col).getValueAt(0);
    	 
    		 if (obj != null) {
    			 return obj.getClass();
    		 }
    	 }
         return Object.class;
     }

     /**
      * Implemented as specified by the {@link IMutableTableModel} interface. 
      * Adds a row after the last row. 
      * 
      * @see IMutableTableModel#addEmptyRow()
      */
     public void addEmptyRow() {
    	 int rowCount = getRowCount();
    	 addEmptyRow(rowCount);
     }
	
     /**
      * Implemented as specified by the {@link IMutableTableModel} interface. 
      * Adds a row at the specified index, by adding blank data to each 
      * {@link IParam} of the field. 
      * 
      * @see IMutableTableModel#addEmptyRow()
      */
     public void addEmptyRow(int addAtThisRow) {
		
    	 List<IParam> params = field.getAtomicParams();
    	 for (IParam param : params) {
    		 param.insertValue(addAtThisRow, "");
    	 }
    	 
    	 fireTableRowsInserted(addAtThisRow, addAtThisRow);
     }
	
     /**
      * Implemented as specified by the {@link IMutableTableModel} interface. 
      * Removes rows at the specified indexes, by adding blank data to each 
      * {@link IParam} of the field. 
      * 
      * @see IMutableTableModel#addEmptyRow()
      */
     public void removeRows(int[] rowIndecies) {
    	 List<IParam> params = field.getAtomicParams();
    	 for (IParam param : params) {
    		// remove rows, starting at the highest!
    	    	for (int i=rowIndecies.length-1; i>-1; i--) {
    	    		int rowToRemove = rowIndecies[i];
    	    		param.removeValueAt(rowToRemove);
    	    		fireTableRowsDeleted(rowToRemove, rowToRemove);
    	    	}
    	 }
     }

     /**
      * Override this so that many JTables don't become listeners, since a 
      * new JTable will be created each time the JTree is rendered.
      * When the data in a {@link IField} is updated, a new JTree node is 
      * created for display, instead of updating the existing node. 
      * So, existing tables do not need to be notified of changes. 
      * Also, don't want lots of old tables to remain as listeners, because they
      * won't be Garbage collected, even though they are not being used/ displayed. 
      */
     public void addTableModelListener(TableModelListener l) {}
}
