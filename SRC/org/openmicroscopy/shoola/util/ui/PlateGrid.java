/*
 * org.openmicroscopy.shoola.util.ui.PlateGrid 
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
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

//Third-party libraries

//Application-internal dependencies

/** 
 * Displays a grid.
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
public class PlateGrid 
	extends JTable
{

	/** Indicates the row or column is an ascending letter. */
	public static final int 	ASCENDING_LETTER = 0;
	
	/** Indicates the row or column is an ascending number. */
	public static final int 	ASCENDING_NUMBER = 1;
	
	/** The maximum number of rows. */
	public final static int		MAX_ROWS = 16;
	
	/** The maximum number of columns. */
	public final static int		MAX_COLUMNS = 24;
	
	/** Bound property indicating that a well is selected. */
	public static final String  WELL_FIELDS_PROPERTY = "wellFields";
	
	/** The color of the grid in the table. */
	private final static Color	GRID_COLOR = new Color(180, 213, 255);

	/** The color of the selected cell. */
	private final static Color  SELECTED_COLOR = new Color(255, 206, 206);
	
	/** The color of the focused cell. */
	private final static Color  FOCUS_COLOR = new Color(255, 135, 135);
	
	/** The default size of a cell. */
	private final static Dimension CELL_SIZE = new Dimension(10, 10);
	
	/** One of the constants defined by this class. */
	private int typeRow;
	
	/** One of the constants defined by this class. */
	private int typeColumn;
	
	/** Hosts the valid wells. */
	private boolean[][] validValues;
	
	/** Initializes the component. */
	private void initialize()
	{
		setModel(new GridModel(MAX_ROWS, MAX_COLUMNS));
		TableColumn col;
		int width = CELL_SIZE.width;
		for (int i = 0 ; i < getColumnCount(); i++) {
			col = getColumnModel().getColumn(i);
			col.setMinWidth(width);
			col.setMaxWidth(width);
			col.setPreferredWidth(width);
			col.setResizable(false);
		}
		setRowHeight(CELL_SIZE.height);
		setDefaultRenderer(Object.class, new GridRenderer(this));
		setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		setGridColor(GRID_COLOR);
		addMouseListener(new MouseAdapter() {
			
			/**
			 * Loads the fields for the selected well.
			 * @see MouseAdapter#mouseReleased(MouseEvent)
			 */
			public void mouseReleased(MouseEvent e)
			{
				int row = getSelectedRow();
				int column = getSelectedColumn();
				if (isCellValid(row, column)) {
					Point p = new Point(row, column);
					firePropertyChange(WELL_FIELDS_PROPERTY, null, p);
				}
			}
		});
	}
	
	/** Creates a default instance. 
	 * 
	 * @param typeRow     One of the constants defined by this class.
	 * @param typeColumn  One of the constants defined by this class.
	 * @param validValues Host the valid wells.
	 */
	public PlateGrid(int typeRow, int typeColumn, boolean[][] validValues)
	{
		this.typeColumn = typeColumn;
		this.typeRow = typeRow;
		this.validValues = validValues;
		initialize();
	}
	
	/**
	 * Sets the selected cell.
	 * 
	 * @param row 	 The row identifying the cell.
	 * @param column The column identifying the cell.
	 */
	public void selectCell(int row, int column)
	{
		if (!isCellValid(row, column)) return;
		setColumnSelectionInterval(column, column);
		setRowSelectionInterval(row, row);
		//editCellAt(row, column);
		repaint();
	}
	
	/**
	 * Returns <code>true</code> if the passed cell contains a valid well,
	 * <code>false</code> otherwise.
	 * 
	 * @param row 	 The row of the selected cell.
	 * @param column The column of the selected cell.
	 * @return See above.
	 */
	boolean isCellValid(int row, int column) 
	{
		if (validValues == null) return false;
		return validValues[row][column];
	}
	
	/**
	 * Returns the displayed for the tool tip.
	 * 
	 * @param row The row to handle.
	 * @param column The column to handle.
	 * @return See above.
	 */
	String getCellToolTip(int row, int column)
	{
		String r = "";
		String c = "";
		row = row+1;
		column = column+1;
		if (typeRow == ASCENDING_LETTER) 
			r = UIUtilities.LETTERS.get(row);
		else r = ""+row;
		if (typeColumn == ASCENDING_LETTER) 
			c = UIUtilities.LETTERS.get(row);
		else c = ""+column;
		return r+"-"+c;
	}
	
	/**
	 * Inner class to render the cell.
	 */
	class GridRenderer 
		extends DefaultTableCellRenderer
	{
	
		/** Reference to the model. */
		private PlateGrid model;
		
		/**
		 * Creates a new instance.
		 * 
		 * @param model Reference to the model.
		 */
		GridRenderer(PlateGrid model)
		{
			this.model = model;
		}
		
		/**
		 * Overridden to set the color of the selected cell.
		 */
		public Component getTableCellRendererComponent(JTable table, 
				Object value, boolean isSelected, boolean hasFocus, 
				int row, int column)
		{
			setToolTipText(model.getCellToolTip(row, column));
			if (model.isCellValid(row, column)) {
				setBackground(SELECTED_COLOR);
				if (hasFocus) setBackground(FOCUS_COLOR);
			} else {
				setBackground(Color.WHITE);
			}
			return this;
		}
	}
	
	/** Creates an inner class, so that the cell cannot be edited. */
	class GridModel
		extends DefaultTableModel
	{
		
		/**
		 * Creates a new instance.
		 * 
		 * @param numRows		The number of rows the table holds.
		 * @param numColumns    The number of columns the table holds.
		 */
		GridModel(int numRows, int numColumns)
		{
			super(numRows, numColumns);
		}
		
		/**
		 * Overridden to return <code>false</code> regardless of the value.
		 * @see DefaultTableModel#isCellEditable(int, int)
		 */
	    public boolean isCellEditable(int row, int column) { return false; }

	}
	
}
