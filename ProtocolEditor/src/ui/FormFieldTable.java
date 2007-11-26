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

package ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableColumnModel;

import table.InteractiveTableModel;
import tree.DataField;
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
    
	public FormFieldTable(DataField dataField) {
		super(dataField);
		
		String columns = dataField.getAttribute(DataField.TABLE_COLUMN_NAMES);
		table = new JTable();
		
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
        
        // add data, creating a new row each time
        String numberOfRows = dataField.getAttribute(DataField.TABLE_ROW_COUNT);
        int rowCount;
        if (numberOfRows == null) rowCount = 0;
        else rowCount = Integer.valueOf(numberOfRows).intValue();
        for (int row=0; row<rowCount; row++) {
        	
        	String rowDataString = dataField.getAttribute(DataField.ROW_DATA_NUMBER + row);
        	// System.out.println("FormFieldTable constructor row " + row + " data = " + rowDataString);
        	if (rowDataString != null) {
        		tableModel.addEmptyRow();
        		String[] rowData = rowDataString.split(",");
        		for (int col=0; col<columnNames.length && col<rowData.length ; col++) {
        			tableModel.setValueAt(rowData[col].trim(), row, col);
        		}
        	}
        }
        
        if (!tableModel.hasEmptyRow()) {
            tableModel.addEmptyRow();
        }
        
     // tableModelListener tells the table how to respond to changes in Model
        tableModel.addTableModelListener(new InteractiveTableModelListener());
        
        tableScroller = new JScrollPane(table);
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
        
		this.add(tableScroller, BorderLayout.SOUTH);
		
		// update new rows etc.
		tableModel.fireTableStructureChanged();
		
		setExperimentalEditing(false);	// default created as uneditable
	}
	
	public class RemoveRowsListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			int delete = JOptionPane.showConfirmDialog(table, "Are you sure you want to delete rows?\n" +
					"This cannot be undone.", "Really delete rows?", JOptionPane.OK_CANCEL_OPTION);
			if (delete == JOptionPane.OK_OPTION) {
				removeSelectedRows();
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
		}
	}
	// inserts an empty row at the specified index
	public void addRow() {
		int index = table.getSelectedRow();
		if (index < 0)
			index = 0;
		tableModel.addEmptyRow(index);
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
                // or simply move to the start of the next row
                } else {
                	table.setRowSelectionInterval(row + 1, row + 1);
                	table.setColumnSelectionInterval(0, 0);
                }
               
            }
        }
    }
    
//  overridden by subclasses (when focus lost) if they have values that need saving 
	public void copyTableModelToDataField() {
		
		// first update col names
		
		String columnNames = "";
		for (int col=0; col<table.getColumnCount(); col++) {
			if (col > 0) columnNames = columnNames + ", ";
			columnNames = columnNames + table.getColumnModel().getColumn(col).getHeaderValue();
		}
		
		dataField.setAttribute(DataField.TABLE_COLUMN_NAMES, columnNames, false);
		
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
			String rowId = DataField.ROW_DATA_NUMBER + outputRowNumber;
			dataField.setAttribute(rowId, rowData, false);
			//System.out.println("FormFieldTable updateDatafield: " + rowId + " = " + rowData);
			outputRowNumber++;
		}
		
		// delete extra un-needed rows from dataField
		while (dataField.getAttribute(DataField.ROW_DATA_NUMBER + outputRowNumber) != null) {
			String rowId = DataField.ROW_DATA_NUMBER + outputRowNumber;
			dataField.setAttribute(rowId, null, false);
			outputRowNumber++;
		}
		
		dataField.setAttribute(DataField.TABLE_ROW_COUNT, Integer.toString(outputRowNumber), false);
	}
	
	
//	 overridden by subclasses if they have other attributes to retrieve from dataField
	public void dataFieldUpdated() {
		super.dataFieldUpdated();	// takes care of name etc.
		
		String columns = dataField.getAttribute(DataField.TABLE_COLUMN_NAMES);
		
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
	}
	
	public void setHighlighted(boolean highlight) {
		super.setHighlighted(highlight);
		
		addRowButton.setEnabled(highlight);
		removeRowsButton.setEnabled(highlight);
		table.setEnabled(highlight);
	}


}
