/*
 * org.openmicroscopy.shoola.util.file.TableModelAdapter 
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
package org.openmicroscopy.shoola.util.file;


//Java imports
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

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
public class TableModelAdapter
	implements TableModel
{	
	/** The object array which is mapping to the tableModel. */
	Object[][] 	objectArray;
	
	/** Use first row as column headers. */
	boolean 	firstRowHeadings;
	
	/** The current number of columns in the model. */
	int columnCount;
	
	/** The number of rows in the model. */
	int rowCount;
	
	/**
	 * Create the table adapter for the array, and set the firstRow to be the column
	 * headings if firstRowHeadings is true.
	 * @param array see above.
	 * @param firstRowHeadings see above.
	 */
	TableModelAdapter(Object[][] array, boolean firstRowHeadings)
	{
		this.objectArray = array;
		columnCount = array[0].length;
		rowCount = array.length;
		this.firstRowHeadings = firstRowHeadings;
	}
	
	/**
	 * Create the table adapter for the array, and set the firstRow to be the column
	 * headings if firstRowHeadings is true.
	 * @param array see above.
	 * @param firstRowHeadings see above.
	 */
	TableModelAdapter(int[][] array, boolean firstRowHeadings)
	{
		this.objectArray = mapToObject(array);
		this.firstRowHeadings = firstRowHeadings;
	}

	/**
	 * Create the table adapter for the array, and set the firstRow to be the column
	 * headings if firstRowHeadings is true.
	 * @param array see above.
	 * @param firstRowHeadings see above.
	 */
	TableModelAdapter(long[][] array, boolean firstRowHeadings)
	{
		this.objectArray = mapToObject(array);
		this.firstRowHeadings = firstRowHeadings;
	}

	/**
	 * Create the table adapter for the array, and set the firstRow to be the column
	 * headings if firstRowHeadings is true.
	 * @param array see above.
	 * @param firstRowHeadings see above.
	 */
	TableModelAdapter(double[][] array, boolean firstRowHeadings)
	{
		this.objectArray = mapToObject(array);
		this.firstRowHeadings = firstRowHeadings;
	}
	
	/**
	 * Create the table adapter for the array, and set the firstRow to be the column
	 * headings if firstRowHeadings is true.
	 * @param array see above.
	 * @param firstRowHeadings see above.
	 */
	TableModelAdapter(float[][] array, boolean firstRowHeadings)
	{
		this.objectArray = mapToObject(array);
		this.firstRowHeadings = firstRowHeadings;
	}

	/**
	 * Create the table adapter for the array, and set the firstRow to be the column
	 * headings if firstRowHeadings is true.
	 * @param array see above.
	 * @param firstRowHeadings see above.
	 */
	TableModelAdapter(boolean[][] array, boolean firstRowHeadings)
	{
		this.objectArray = mapToObject(array);
		this.firstRowHeadings = firstRowHeadings;
	}
	
	/**
	 * Map the typeArray of primitive type to the object of that Type.
	 * @param typeArray see above.
	 * @return see above.
	 */
	private Object[][] mapToObject(int[][] typeArray)
	{
		Integer[][] array = new Integer[typeArray.length][typeArray[0].length];
		columnCount = array[0].length;
		rowCount = array.length;
		for (int i=0; i<typeArray.length; i++)
			for (int j=0; j<typeArray[i].length; j++)
				array[i][j] = new Integer(typeArray[i][j]);
		return array;
	}
	
	/**
	 * Map the typeArray of primitive type to the object of that Type.
	 * @param typeArray see above.
	 * @return see above.
	 */
	private Object[][] mapToObject(long[][] typeArray)
	{
		
		Long[][] array = new Long[typeArray.length][typeArray[0].length];
		columnCount = array[0].length;
		rowCount = array.length;
		for (int i=0; i<typeArray.length; i++)
			for (int j=0; j<typeArray[i].length; j++)
				array[i][j] = new Long(typeArray[i][j]);
		return array;
	}
	
	/**
	 * Map the typeArray of primitive type to the object of that Type.
	 * @param typeArray see above.
	 * @return see above.
	 */
	private Object[][] mapToObject(double[][] typeArray)
	{
		
		Double[][] array = new Double[typeArray.length][typeArray[0].length];
		columnCount = array[0].length;
		rowCount = array.length;
		for (int i=0; i<typeArray.length; i++)
			for (int j=0; j<typeArray[i].length; j++)
				array[i][j] = new Double(typeArray[i][j]);
		return array;
	}
	
	/**
	 * Map the typeArray of primitive type to the object of that Type.
	 * @param typeArray see above.
	 * @return see above.
	 */
	private Object[][] mapToObject(float[][] typeArray)
	{
		
		Double[][] array = new Double[typeArray.length][typeArray[0].length];
		columnCount = array[0].length;
		rowCount = array.length;
		for (int i=0; i<typeArray.length; i++)
			for (int j=0; j<typeArray[i].length; j++)
				array[i][j] = new Double(typeArray[i][j]);
		return array;
	}
	
	/**
	 * Map the typeArray of primitive type to the object of that Type.
	 * @param typeArray see above.
	 * @return see above.
	 */
	private Object[][] mapToObject(boolean[][] typeArray)
	{
		
		Boolean[][] array = new Boolean[typeArray.length][typeArray[0].length];
		columnCount = array[0].length;
		rowCount = array.length;
		for (int i=0; i<typeArray.length; i++)
			for (int j=0; j<typeArray[i].length; j++)
				array[i][j] = new Boolean(typeArray[i][j]);
		return array;
	}

		
	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#addTableModelListener(javax.swing.event.TableModelListener)
	 */
	public void addTableModelListener(TableModelListener l)
	{
		// TODO Auto-generated method stub
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getColumnClass(int)
	 */
	public Class<?> getColumnClass(int columnIndex)
	{
		return getColumn(columnIndex).getClass();
	}
	
	/**
	 * Get the column with index columnIndex, this is real data (row 0/1)
	 * @param columnIndex see above.
	 * @return see above.
	 */
	private Object getColumn(int columnIndex)
	{
		if(firstRowHeadings)
			return objectArray[1][columnIndex];
		if(rowCount>=2)
			return objectArray[1][columnIndex];
		return "";
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getColumnCount()
	 */
	public int getColumnCount()
	{
		return columnCount;
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getColumnName(int)
	 */
	public String getColumnName(int columnIndex)
	{
		if(firstRowHeadings)
			return objectArray[0][columnIndex].toString();
		return columnIndex+"";
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getRowCount()
	 */
	public int getRowCount()
	{
		if(firstRowHeadings)
			return rowCount-1;
		return rowCount;
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getValueAt(int, int)
	 */
	public Object getValueAt(int rowIndex, int columnIndex)
	{
		if(firstRowHeadings)
			return objectArray[rowIndex+1][columnIndex];
		return objectArray[rowIndex][columnIndex];
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#isCellEditable(int, int)
	 */
	public boolean isCellEditable(int rowIndex, int columnIndex)
	{
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#removeTableModelListener(javax.swing.event.TableModelListener)
	 */
	public void removeTableModelListener(TableModelListener l)
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#setValueAt(java.lang.Object, int, int)
	 */
	public void setValueAt(Object value, int rowIndex, int columnIndex)
	{
		if(firstRowHeadings)
			objectArray[rowIndex+1][columnIndex] = value;
		else 
			objectArray[rowIndex][columnIndex] = value;
	}
	
	
}


