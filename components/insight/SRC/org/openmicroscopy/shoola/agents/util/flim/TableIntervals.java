/*
 * org.openmicroscopy.shoola.env.ui.flim.TableIntervals 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2010 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.util.flim;


//Java imports
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * Displays the selected intervals and the number of pixels, etc.
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
class TableIntervals
	extends JTable
{

	/** The number of columns. */
	private static final int COLUMNS = 3;
	
	/** The index for the percentage. */
	private static final int INDEX_PERCENTAGE = 2;
	
	/** The index for the number of pixels. */
	private static final int INDEX_NUMBER_OF_PIXELS = 1;
	
	/** The index for the number of pixels. */
	private static final int INDEX_INTERVAL = 0;
	
	/** Reference to the parent. */
	private FLIMResultsDialog parent;
	
	/** The model used. */
	private TableIntervalsModel model;
	
	/** The listener. */
	private TableModelListener listener;
	
	/**
	 * Populates the specified row. 
	 * 
	 * @param row The selected row.
	 */
	private void populateRow(int row)
	{
		String value = (String) getValueAt(row, 0);
		if (value == null) return;
		String[] values = value.split("-");
		Double lowerBound = null;
		Double upperBound = null;
		int n = values.length;
		if (n == 0) return;
		if (n == 1) {
			try {
				lowerBound = Double.parseDouble(values[0]);
			} catch (Exception e) {}	
		}
		if (n > 1) {
			try {
				lowerBound = Double.parseDouble(values[0]);
			} catch (Exception e) {}	
			try {
				upperBound = Double.parseDouble(values[1]);
			} catch (Exception e) {}
		}
		Double number = parent.getValueInInterval(lowerBound, upperBound);
		if (number != null) {
			model.removeTableModelListener(listener);
			model.setValueAt(number.intValue(), row, INDEX_NUMBER_OF_PIXELS);
			Double total = parent.getTotalValue();
			if (total == 0)
				model.setValueAt(0, row, INDEX_PERCENTAGE);
			else {
				double v = UIUtilities.roundTwoDecimals(
						number.doubleValue()/total*100);
				model.setValueAt(v, row, INDEX_PERCENTAGE);
			}
			model.addTableModelListener(listener);
		}
	}
	
	/**
	 * Initializes the component.
	 * 
	 * @param nameYAxis The name of the axis.
	 */
	private void initComponents(String nameYAxis)
	{
		String[] names = new String[COLUMNS];
		names[INDEX_INTERVAL] = "Interval e.g. 1-3";
		names[INDEX_NUMBER_OF_PIXELS] = nameYAxis;
		names[INDEX_PERCENTAGE] = "Percentage %";
		putClientProperty("terminateEditOnFocusLost",  Boolean.FALSE);
		setSurrendersFocusOnKeystroke(true);
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		Object[][] objects = new Object[1][COLUMNS];
		for (int i = 0; i <COLUMNS; i++) {
			objects[0][i] = null;
		}
		model = new TableIntervalsModel(objects, names);
		setModel(model);
		getTableHeader().setReorderingAllowed(false);
		highlightRow(model.getRowCount()-1);
		listener = new TableModelListener() {
			
			public void tableChanged(TableModelEvent evt) {
				if (evt.getType() == TableModelEvent.UPDATE) {
					populateRow(evt.getFirstRow());
		            addEmptyRow();
		        }
			}
		};
		model.addTableModelListener(listener);
	}
	
	/**
	 * Highlights the specified row.
	 * 
	 * @param row The row to highlight.
	 */
	private void highlightRow(int row)
	{
		int n = model.getRowCount();
		int v;
		if (row == (n-1)) v = n-1;
		else v = row+1;
		setRowSelectionInterval(v, v);
		setColumnSelectionInterval(INDEX_INTERVAL, INDEX_INTERVAL);
	}
	
	/** Adds an empty row to the table. */
	private void addEmptyRow()
	{
		Object[] objects = new Object[COLUMNS];
		for (int i = 0; i < objects.length; i++) {
			objects[i] = null;
		}
		((DefaultTableModel) getModel()).addRow(objects);
		highlightRow(model.getRowCount()-1);
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param parent The parent of the table.
	 * @param nameYAxis The name along the Y-axis.
	 */
	TableIntervals(FLIMResultsDialog parent, String nameYAxis)
	{
		this.parent = parent;
		initComponents(nameYAxis);
	}
	
	/** 
	 * Populates the table.
	 * 
	 * @param data The value to display
	 */
	void populateTable()
	{
		for (int i = 0; i < model.getRowCount(); i++)
			populateRow(i);
	}
	
	/** Clears the table. */
	void clearTable()
	{
		for (int i = 0; i < model.getRowCount(); i++)
			model.removeRow(i);
		model.setRowCount(0);
		addEmptyRow();
		repaint();
	}
	
	/**
	 * Inner class used to override the 
	 * {@link DefaultTableModel#isCellEditable(int, int)} method.
	 */
	class TableIntervalsModel 
		extends DefaultTableModel
	{
		
	    /**
	     *  Constructs a <code>DefaultTableModel</code> and initializes the 
	     *  table by passing <code>data</code> and <code>columnNames</code>
	     *  to the <code>setDataVector</code>
	     *  method. The first index in the <code>Object[][]</code> array is
	     *  the row index and the second is the column index.
	     *
	     * @param data			The data of the table.
	     * @param columnNames	The names of the columns.
	     */
	    public TableIntervalsModel(Object[][] data, Object[] columnNames)
	    {
	        super(data, columnNames);
	    }
	    
		/**
		 * Overridden so that only the cells displaying the server's name
		 * can be edited.
		 * @see DefaultTableModel#isCellEditable(int, int)
		 */
	    public boolean isCellEditable(int row, int column)
	    {
	    	return (column == 0);
	    }
	}
	
}
