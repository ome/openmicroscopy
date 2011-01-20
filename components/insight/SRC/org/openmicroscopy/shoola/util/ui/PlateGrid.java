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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
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
	
	/** The default number of rows. */
	public final static int		DEFAULT_ROWS = 16;
	
	/** The default number of columns. */
	public final static int		DEFAULT_COLUMNS = 24;
	
	/** Bound property indicating that a well is selected. */
	public static final String  WELL_FIELDS_PROPERTY = "wellFields";
	
	/** The background color of a cell. */
	public final static Color	BACKGROUND_COLOR = Color.WHITE;
	
	/** The color of the grid in the table. */
	public final static Color	GRID_COLOR = new Color(180, 213, 255);

	/** The color of the selected cell. */
	public final static Color  SELECTED_COLOR = new Color(255, 206, 206);
	
	/** The color of the focused cell. */
	public final static Color  FOCUS_COLOR = new Color(255, 135, 135);
	
	/** The default size of a cell. */
	private final static Dimension CELL_SIZE = new Dimension(10, 10);
	
	/** One of the constants defined by this class. */
	private int typeRow;
	
	/** One of the constants defined by this class. */
	private int typeColumn;
	
	/** Hosts the valid wells. */
	private List<WellGridElement> validValues;
	
	/** Identifies the row of the selected cell. */
	private int selectedRow;
	
	/** Identifies the column of the selected cell. */
	private int selectedColumn;
	
	/** 
	 * Initializes the component. 
	 * 
	 * @param rows The number of rows.
	 * @param columns The number of columns.
	 */
	private void initialize(int rows, int columns)
	{
		selectedColumn = -1;
		selectedRow = -1;
		setTableHeader(null);
		setModel(new GridModel(rows, columns));
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
			public void mousePressed(MouseEvent e)
			{
				int row = getSelectedRow();
				int column = getSelectedColumn();
				if (isCellValid(row, column)) {
					selectedColumn = column;
					selectedRow = row;
					boolean b = e.isShiftDown() || e.isControlDown();
					if (UIUtilities.isMacOS()) 
						b = e.isShiftDown() || e.isMetaDown();
					firePropertyChange(WELL_FIELDS_PROPERTY, null, 
							new PlateGridObject(row, column, b));
				}
			}
		});
	}
	
	/** 
	 * Creates a default instance. 
	 * 
	 * @param typeRow     One of the constants defined by this class.
	 * @param typeColumn  One of the constants defined by this class.
	 * @param values Host the valid wells.
	 */
	public PlateGrid(int typeRow, int typeColumn, List<WellGridElement> values, 
			int rows, int columns)
	{
		this.typeColumn = typeColumn;
		this.typeRow = typeRow;
		this.validValues = values;
		initialize(DEFAULT_ROWS, DEFAULT_COLUMNS);
	}
	
	/** 
	 * Creates a default instance. 
	 * 
	 * @param typeRow     One of the constants defined by this class.
	 * @param typeColumn  One of the constants defined by this class.
	 * @param values Host the valid wells.
	 */
	public PlateGrid(int typeRow, int typeColumn, List<WellGridElement> values)
	{
		this(typeRow, typeColumn, values, DEFAULT_ROWS, DEFAULT_COLUMNS);
	}

	/** 
	 * Creates a default instance, one row and multiple column.
	 * 
	 * @param columns The number of columns.
	 */
	public PlateGrid(int columns)
	{
		validValues = new ArrayList<WellGridElement>();//boolean[1][columns];
		for (int i = 0; i < columns; i++)
			validValues.add(new WellGridElement(0, i));
		initialize(1, columns);
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
		selectedRow = row;
		selectedColumn = column;
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
		Iterator<WellGridElement> i = validValues.iterator();
		WellGridElement well;
		while (i.hasNext()) {
			well = i.next();
			if (well.getRow() == row && well.getColumn() == column)
				return well.isValid();
		}
		return false;
	}
	
	/**
	 * Returns <code>true</code> if the passed cell is the displayed one,
	 * <code>false</code> otherwise.
	 * 
	 * @param row The row identifying the cell.
	 * @param column The column identifying the cell.
	 * @return See above.
	 */
	boolean isCellDisplayed(int row, int column)
	{
		return (selectedColumn == column && selectedRow == row);
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
				if (model.isCellDisplayed(row, column))
					setBackground(FOCUS_COLOR);
				else setBackground(SELECTED_COLOR);
			} else {
				setBackground(BACKGROUND_COLOR);
			}
			return this;
		}
	}

}
