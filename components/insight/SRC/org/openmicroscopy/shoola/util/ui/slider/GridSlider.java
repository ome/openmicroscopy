/*
 * org.openmicroscopy.shoola.util.ui.slider.GridSlider 
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
package org.openmicroscopy.shoola.util.ui.slider;


//Java imports
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.GridModel;
import org.openmicroscopy.shoola.util.ui.PlateGrid;

/** 
 * Selects cells of the table with one row so it behaves a bit like a slider.
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
public class GridSlider
	extends JTable
{

	/** Bound property indicating that a column is selected. */
	public static final String	COLUMN_SELECTION_PROPERTY = "columnSelection";
	
	/** The dimension of a cell. */
	public static final Dimension CELL_SIZE = new Dimension(14, 14);
	
	/** Holds the selected cells. */
	private Map<Integer, Boolean> 	selectedCells;
	
	/** The name associated to a cell. */
	private Map<Integer, String>	cellNames;
	
	/** 
	 * The value by which the column value should be incremented by to 
	 * set the text associated to that given column.
	 */
	private int						textIncrement;
	
	/** 
	 * Initializes the component. 
	 * 
	 * @param columns The number of columns.
	 */
	private void initiliaze(int columns)
	{
		setTableHeader(null);
		selectedCells = new HashMap<Integer, Boolean>();
		setModel(new GridModel(1, columns));
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
		setDefaultRenderer(Object.class, new GridSliderRenderer(this));
		setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		setGridColor(PlateGrid.GRID_COLOR);
		addMouseListener(new MouseAdapter() {
			
			/**
			 * Selects or not the specified column.
			 * @see MouseAdapter#mouseReleased(MouseEvent)
			 */
			public void mouseReleased(MouseEvent e)
			{
				int column = getSelectedColumn();
				boolean selected = !selectedCells.containsKey(column);
				selectCell(column, selected);
				firePropertyChange(COLUMN_SELECTION_PROPERTY, 
						Boolean.valueOf(!selected), Boolean.valueOf(selected));
			}
		});
		addMouseMotionListener(new MouseMotionAdapter() {
			
			public void mouseDragged(MouseEvent e)
			{
				int column = getSelectedColumn();
				boolean selected = !selectedCells.containsKey(column);
				//if (!selected) return;
				selectCell(column, selected);
				firePropertyChange(COLUMN_SELECTION_PROPERTY, 
						Boolean.valueOf(!selected), Boolean.valueOf(selected));
			}
			
		});
		selectCells(1);
	}

	/**
	 * Creates a new instance.
	 * 
	 * @param columns The number of columns.
	 */
	public GridSlider(int columns)
	{
		initiliaze(columns);
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param columns 		The number of columns.
	 * @param textIncrement The value by which the column value should be 
	 * 						incremented by to set the text associated to 
	 * 						that given column.
	 */
	public GridSlider(int columns, int textIncrement)
	{
		initiliaze(columns);
		this.textIncrement = textIncrement;
	}
	
	/**
	 * Returns the text associated to a given column.
	 * 
	 * @param column 		The column to handle.
	 * 
	 * @return See above.
	 */
	String getCellTooltipText(int column)
	{
		if (cellNames != null && cellNames.containsKey(column))
			return cellNames.get(column);
		return ""+(column+textIncrement);
	}
	
	/**
	 * Returns <code>true</code> if the column is selected,
	 * <code>false</code> otherwise.
	 * 
	 * @param column The selected column.
	 * @return See above.
	 */
	boolean isSelected(int column)
	{
		return selectedCells.containsKey(column);
	}
	
	/**
	 * Sets the name associated to cells.
	 * 
	 * @param cellNames The value to set.
	 */
	public void setCellNames(Map<Integer, String> cellNames)
	{
		this.cellNames = cellNames;
	}
	
	/**
	 * Selects or not the selected cell.
	 * 
	 * @param column    The column identifying the cell.
	 * @param selected  Pass <code>true</code> to select the cell, 
	 * 					<code>false</code> otherwise.
	 */
	public void selectCell(int column, boolean selected)
	{
		//if (!isCellValid(row, column)) return;
		if (selected) selectedCells.put(column, Boolean.valueOf(selected));
		else selectedCells.remove(column);
		int count = getColumnCount();
		if (column >= 0 && column < count)
			setColumnSelectionInterval(column, column);
		setRowSelectionInterval(0, 0);
		repaint();
	}
	
	/**
	 * Selects the cells corresponding to the passed frequency.
	 * 
	 * @param frequency
	 */
	public void selectCells(int frequency)
	{
		selectedCells.clear();
		for (int i = 0; i < getColumnCount(); i++) {
			if (i%frequency == 0)
				selectedCells.put(i, Boolean.valueOf(true));
		}
		repaint();
	}
	
	/**
	 * Returns the selected cells.
	 * 
	 * @return See above.
	 */
	public List<Integer> getSelectedCells()
	{ 
		List<Integer> list = new ArrayList<Integer>();
		Iterator<Integer> i = selectedCells.keySet().iterator();
		while (i.hasNext())
			list.add(i.next());
		return list;
	}
	
	/**
	 * Returns the number of selected cells.
	 * 
	 * @return See above.
	 */
	public int getNumberOfSelectedCells()
	{
		return selectedCells.size();
	}
	
	/**
	 * Inner class to render the cell.
	 */
	class GridSliderRenderer 
		extends DefaultTableCellRenderer
	{
	
		/** Reference to the model. */
		private GridSlider model;
		
		/**
		 * Creates a new instance.
		 * 
		 * @param model Reference to the model.
		 */
		GridSliderRenderer(GridSlider model)
		{
			this.model = model;
		}
		
		/**
		 * Overridden to set the color of the selected cell.
		 * @see DefaultTableCellRenderer#getTableCellRendererComponent(JTable, 
		 * Object, boolean, boolean, int, int)
		 */
		public Component getTableCellRendererComponent(JTable table, 
				Object value, boolean isSelected, boolean hasFocus, 
				int row, int column)
		{
			setToolTipText(model.getCellTooltipText(column));
			if (model.isSelected(column)) {
				setBackground(PlateGrid.SELECTED_COLOR);
			} else {
				setBackground(PlateGrid.BACKGROUND_COLOR);
			}
			return this;
		}
	}
	
}
