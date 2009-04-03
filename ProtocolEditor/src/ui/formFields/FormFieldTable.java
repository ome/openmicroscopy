/*
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
 *	author Will Moore will@lifesci.dundee.ac.uk
 */

package ui.formFields;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;

import table.InteractiveTableModel;
import tree.DataFieldConstants;
import tree.IDataFieldObservable;
import util.ImageFactory;

public class FormFieldTable extends FormField {
	
	private String[] columnNames = new String[0];

    protected JTable table;
    protected JScrollPane tableScroller;
    protected InteractiveTableModel tableModel;
    JLabel warningMessage;
    public static final String EDIT_COLS_MESSAGE ="<html>Start by editing column names<br> in the right-hand panel</html>";
	
    JButton addRowButton;
    JButton removeRowsButton;
    
    InteractiveTableModelListener interactiveTableModelListener = new InteractiveTableModelListener();
    
	public FormFieldTable(IDataFieldObservable dataFieldObs) {
		super(dataFieldObs);
		
		String columns = dataField.getAttribute(DataFieldConstants.TABLE_COLUMN_NAMES);
		table = new JTable();
		table.addFocusListener(componentFocusListener);
		
		if (columns != null) {
			columnNames = columns.split(",");
			
			for (int i=0; i<columnNames.length; i++) {
				columnNames[i] = columnNames[i].trim();
			}
		}
		
		tableModel = new InteractiveTableModel(columnNames);
       // table.addFocusListener(new FocusLostUpdatDataFieldListener());
        table.setModel(tableModel);
        table.setColumnModel(new DefaultTableColumnModel());
        table.setSurrendersFocusOnKeystroke(true);
        
        // need to update table with all the columns of the table model
        tableModel.fireTableStructureChanged();
        
        // add data, creating a new row each time, if needed
        updateTableModelFromDataField();
        
        if (!tableModel.hasEmptyRow()) {
            tableModel.addEmptyRow();
        }
        
     // tableModelListener tells the table how to respond to changes in Model
        tableModel.addTableModelListener(interactiveTableModelListener);
        
     // Disable auto resizing
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tableScroller = new JScrollPane(table, 
        		JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        table.setPreferredScrollableViewportSize(new Dimension(450, 100));
		
        Icon addRowIcon = ImageFactory.getInstance().getIcon(ImageFactory.NEW_ROW_ICON);
        Icon clearRowIcon = ImageFactory.getInstance().getIcon(ImageFactory.CLEAR_ROW_ICON);
        addRowButton = new JButton("Add New Row", addRowIcon);
        addRowButton.addActionListener(new AddRowListener());
        addRowButton.setBorder(new EmptyBorder(0,2,2,2));
        addRowButton.setBackground(null);
        addRowButton.setEnabled(false);		// enabled when field is highlighted
        removeRowsButton = new JButton("Remove Selected Rows", clearRowIcon);
        removeRowsButton.addActionListener(new RemoveRowsListener());
        removeRowsButton.setBorder(new EmptyBorder(0,2,2,2));
        removeRowsButton.setBackground(null);
        removeRowsButton.setEnabled(false);		// enabled when field is highlighted
        
        horizontalBox.add(addRowButton);
        horizontalBox.add(removeRowsButton);
        
        warningMessage = new JLabel();
        if (columns == null) {
        	warningMessage.setText(EDIT_COLS_MESSAGE);
        }
        horizontalBox.add(warningMessage);
        
        /*
         * Want to add the table to the SOUTH of contentsPanel (where descriptionLabel is). 
         * Create new panel to hold both. 
         */
        JPanel tableContainer = new JPanel(new BorderLayout());
        tableContainer.setBackground(null);
        tableContainer.add(descriptionLabel, BorderLayout.NORTH);
        tableContainer.add(tableScroller, BorderLayout.SOUTH);
        
		contentsPanel.add(tableContainer, BorderLayout.SOUTH);
		
		// update new rows etc.
		tableModel.fireTableStructureChanged();
		
		// refresh layout of columns and height of viewport
		refreshColumnAutoResizeMode();
		refreshViewportSize();
	
		// enable or disable components based on the locked status of this field
		refreshLockedStatus();
	}
	
	/**
	 * This method gets the row count from dataField. 
	 * For each row, get the data from dataField, and fill the cells of tableModel.
	 * Fires tableStructureChanged() when done, to update table. 
	 */
	public void updateTableModelFromDataField() {
		
		tableModel.removeTableModelListener(interactiveTableModelListener);
		
		int tableModelCols = tableModel.getColumnCount();
		
		// add data, creating a new row each time, if needed
        String numberOfRows = dataField.getAttribute(DataFieldConstants.TABLE_ROW_COUNT);
        int rowCount;
        if (numberOfRows == null) rowCount = 0;
        else rowCount = Integer.valueOf(numberOfRows).intValue();
        
        for (int row=0; row<rowCount; row++) {
        	
        	
        	if(!(row <  tableModel.getRowCount())) {
        		tableModel.addEmptyRow(row);
        	}
        	
        	String rowDataString = dataField.getAttribute(DataFieldConstants.ROW_DATA_NUMBER + row);
        	if (rowDataString != null) {
        	
        		String[] rowData = rowDataString.split(",");
        		for (int col=0; col<tableModelCols && col<rowData.length ; col++) {	
        			tableModel.setValueAtNoUpdate(rowData[col].trim(), row, col);
        		}
        	}
        	else {
        		for (int col=0; col<tableModelCols; col++) {	
        			tableModel.setValueAtNoUpdate("", row, col);
        		}
        	}
        }
        
        tableModel.addTableModelListener(interactiveTableModelListener);
        
     // update new rows etc.
		tableModel.fireTableStructureChanged();
	}
	
	/**
	 * This simply enables or disables all the editable components of the 
	 * FormField.
	 * Gets called (via refreshLockedStatus() ) from dataFieldUpdated()
	 * 
	 * @param enabled
	 */
	public void enableEditing(boolean enabled) {
		
		if (addRowButton != null)	// just in case!
			addRowButton.setEnabled(enabled);
		
		if (removeRowsButton != null)	// just in case!
			removeRowsButton.setEnabled(enabled);
		
		if (table != null)	// just in case!
			table.setEnabled(enabled);
	}
	
	/**
	 * Gets the names of the attributes where this field stores its "value"s.
	 * This is used by EditClearFields to set all values back to null. 
	 * For table field, this returns all the rowData attributes 
	 *  
	 * @return	the name of the attribute that holds the "value" of this field
	 */
	public String[] getValueAttributes() {
		String rowCount = dataField.getAttribute(DataFieldConstants.TABLE_ROW_COUNT);
		if (rowCount == null) {
			return new String[0];
		}
		int rows = Integer.parseInt(rowCount);
		String[] rowData = new String[rows];
		for(int i=0; i<rowData.length; i++) {
			rowData[i] = DataFieldConstants.ROW_DATA_NUMBER + i;
		}
		return rowData;
	}
	
	/**
	 * This method tests to see whether the field has been filled out. 
	 * If there is any row data (@see getValueAttributes()) then this is true.
	 * 
	 * @see FormField.isFieldFilled()
	 * @return	True if the field has been filled out by user (Required values are not null)
	 */
	public boolean isFieldFilled() {
		return (getValueAttributes().length > 0);
	}
	
	public class RemoveRowsListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			int delete = JOptionPane.showConfirmDialog(table, "Are you sure you want to delete rows?\n" +
					"This cannot be undone.", "Really delete rows?", JOptionPane.OK_CANCEL_OPTION);
			if (delete == JOptionPane.OK_OPTION) {
				removeSelectedRows();
				refreshViewportSize();
			}
		}
	}
	
	public void removeSelectedRows() {
		int[] highlightedRows = table.getSelectedRows();
		tableModel.removeRows(highlightedRows);
		// update datafield with changes.
		copyTableModelToDataField();
	}
	
	public class AddRowListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			addRow();
			refreshViewportSize();
		}
	}
	// inserts an empty row at the specified index
	public void addRow() {
		int index = table.getSelectedRow();
		if (index < 0)
			index = 0;
		tableModel.addEmptyRow(index);
		tableModel.fireTableRowsInserted(index, index);
		
		table.setRowSelectionInterval(index, index);
		// update datafield with changes.
		copyTableModelToDataField();
	}
	
	public void highlightLastRow(int row) {
        int lastrow = tableModel.getRowCount();
        if (row == lastrow - 1) {
            table.setRowSelectionInterval(lastrow - 1, lastrow - 1);
        } else {
            table.setRowSelectionInterval(row + 1, row + 1);
        }

        table.setColumnSelectionInterval(0, 0);
    }
	
	
    // moves active cell one column to the right, when cell is updated (table changed)
    public class InteractiveTableModelListener implements TableModelListener {
        public void tableChanged(TableModelEvent evt) {
            if (evt.getType() == TableModelEvent.UPDATE) {
                int column = evt.getColumn();
                int row = evt.getFirstRow();
                
                if (column < 0) return;
                if (row < 0) return;
                
                //System.out.println("TableChanged event: row: " + row + " column: " + column);
                
                copyTableModelToDataField();
                
               // System.out.println("TableChanged event: rowCount: " + table.getRowCount() + " columnCount: " + table.getColumnCount());
                /*
                String rowDataString = "";
                for (int col=0; col<tableModel.getColumnCount(); col++) {
                	if (col > 0) rowDataString = rowDataString + ", ";
        			rowDataString = rowDataString + tableModel.getValueAt(row, col);
        		}
                
                dataField.setAttribute(ROW_DATA_NUMBER + row, rowDataString, false);
                */
                
                
                // if you can, move one column to the right
                if (column < table.getColumnCount()-1) {
                	table.setColumnSelectionInterval(column + 1, column + 1);
                	table.setRowSelectionInterval(row, row);
                // or make a new row (if the last one isn't empty)
                } else if (!tableModel.hasEmptyRow()) {
                	tableModel.addEmptyRow();
                	highlightLastRow(row);
                	refreshViewportSize();
                // or simply move to the start of the next row
                } else {
                	table.setRowSelectionInterval(row + 1, row + 1);
                	table.setColumnSelectionInterval(0, 0);
                }
               
            }
        }
    }
    
    /**
     * Update all the column data from the tableModel to dataField. 
     */
	public void copyTableModelToDataField() {
		
		// first update col names
		
		String columnNames = "";
		for (int col=0; col<table.getColumnCount(); col++) {
			if (col > 0) columnNames = columnNames + ", ";
			columnNames = columnNames + table.getColumnModel().getColumn(col).getHeaderValue();
		}
		
		dataField.setAttribute(DataFieldConstants.TABLE_COLUMN_NAMES, columnNames, false);
		
		// now update data
		ArrayList<ArrayList<String>> data = tableModel.getData();
		int outputRowNumber = 0;	// count each saved row (don't save empty rows)
		for (int row=0; row<data.size(); row++) {
			//if (tableModel.isRowEmpty(row)) continue; // ignore empty rows
			
			ArrayList<String> rowDataArray = data.get(row);
			String rowData = "";
			for (int col=0; col<table.getColumnCount(); col++) {
				if (col > 0) rowData = rowData + ", ";
				rowData = rowData + rowDataArray.get(col).trim();
			}
			String rowId = DataFieldConstants.ROW_DATA_NUMBER + outputRowNumber;
			dataField.setAttribute(rowId, rowData, false);
			//System.out.println("FormFieldTable updateDatafield: " + rowId + " = " + rowData);
			outputRowNumber++;
		}
		
		dataField.setAttribute(DataFieldConstants.TABLE_ROW_COUNT, Integer.toString(outputRowNumber), false);

		
		// delete extra un-needed rows from dataField
		while (dataField.getAttribute(DataFieldConstants.ROW_DATA_NUMBER + outputRowNumber) != null) {
			String rowId = DataFieldConstants.ROW_DATA_NUMBER + outputRowNumber;
			dataField.setAttribute(rowId, null, false);
			outputRowNumber++;
		}
		
	}
	
	
//	 overridden by subclasses if they have other attributes to retrieve from dataField
	public void dataFieldUpdated() {
		super.dataFieldUpdated();	// takes care of name etc.
		
		String columns = dataField.getAttribute(DataFieldConstants.TABLE_COLUMN_NAMES);
		
		// refresh column names....
		if (columns != null) { 
			columnNames = columns.split(",");
			warningMessage.setText("");
		} else {
			warningMessage.setText(EDIT_COLS_MESSAGE);
		}
		
		for (int i=0; i<columnNames.length; i++) {
			columnNames[i] = columnNames[i].trim();
		}
		
		/*
		 * refresh table data...
		 * 
		 */
		// THIS DOESN'T WORK!!
		// Can't seem to remove all table listeners. Datafield
		updateTableModelFromDataField();
		
		// add or remove extra cols needed
		
		int extraColsNeeded = columnNames.length - tableModel.getColumnCount();
		if (extraColsNeeded > 0) {
			for (int i=0; i<extraColsNeeded; i++) {
				// System.out.println("adding extra column " + i);
				tableModel.addEmptyColumn();	// this calls fireTableStructureChanged()
			}
		} else if (extraColsNeeded < 0) {
			for (int i=0; i>extraColsNeeded; i--) {
				tableModel.removeLastColumn();
			}
		}
		
		// rename cols
		for (int i=0; i<table.getColumnCount() && i<columnNames.length; i++) {
			// System.out.println("renaming column number " + i + ": " + columnNames[i]);
			
			table.getColumnModel().getColumn(i).setHeaderValue(columnNames[i]);
		}
		
		copyTableModelToDataField();
		
		// finally refresh the column layout
		refreshColumnAutoResizeMode();
	}
	
	public void setHighlighted(boolean highlight) {
		super.setHighlighted(highlight);
		
		addRowButton.setEnabled(highlight);
		removeRowsButton.setEnabled(highlight);
		
		// if the user highlighted this field by clicking the field (not the table itself) 
		// need to get focus, otherwise focus will remain elsewhere. 
		if (highlight && (!table.hasFocus()))
			table.requestFocusInWindow();
	}
	
	public void refreshViewportSize() {
		setScrollPaneRowCount(table.getRowCount());
		
		// size of this panel has changed. Need to validate the ScrollPane that holds the 
		// tree of FormFieldContainers...
		Component parentOfRootContainer = null;
		FormFieldContainer parent = (FormFieldContainer)getParent();
		if (parent != null)
			parentOfRootContainer = parent.getParentOfRootContainer();
		if (parentOfRootContainer != null) {
			parentOfRootContainer.validate();
		}
		
	}
	
	public void setScrollPaneRowCount(int rows) {
		int height = rows * table.getRowHeight();
		table.setPreferredScrollableViewportSize(new Dimension(450, height));
	}

	// if there is not enough space for each column to have it's preferred width,
	// turn off the AutoResize so that the table expands horizontally
	public void refreshColumnAutoResizeMode() {
		int scrollPaneWidth = tableScroller.getWidth();
		
		int colCount = table.getColumnModel().getColumnCount();
		// if there are no columns, forget about it! 
		if (colCount < 1)
			return;
		
		TableColumn tc = table.getColumnModel().getColumn(0);
		
		int prefColWidth = tc.getPreferredWidth();
		int cols = table.getColumnCount();
		
		if (prefColWidth * cols > scrollPaneWidth) {
			table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		} else {
			table.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
		}
	}
}
