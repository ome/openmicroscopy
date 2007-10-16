package xmlMVC;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import table.ArrayColumnModel;
import table.InteractiveTableModel;

public class FormFieldTable extends FormField {
	
	public final static String TABLE_COL_NAMES = "tableColNames";
	public final static String TABLE_ROW_COUNT = "tableRowCount";
	public final static String ROW_DATA_NUMBER = "rowNumber";
	
	private String[] columnNames = new String[0];

    protected JTable table;
    protected JScrollPane scroller;
    protected InteractiveTableModel tableModel;

	
	public FormFieldTable(DataField dataField) {
		super(dataField);
		
		String columns = dataField.getAttribute(DataField.TABLE_COLUMN_NAMES);
		
		if (columns != null) {
			columnNames = columns.split(",");
			
			for (int i=0; i<columnNames.length; i++) {
				columnNames[i] = columnNames[i].trim();
			}
		}
		
		tableModel = new InteractiveTableModel(columnNames);
        // tableModelListener tells the table how to respond to changes in Model
        tableModel.addTableModelListener(new InteractiveTableModelListener());
        table = new JTable();
        table.addFocusListener(new FocusLostUpdatDataFieldListener());
        table.setModel(tableModel);
        table.setColumnModel(new ArrayColumnModel());
        table.setSurrendersFocusOnKeystroke(true);
        
        // need to update table with all the columns of the table model
        tableModel.fireTableStructureChanged();
        
        // add data, creating a new row each time
        String numberOfRows = dataField.getAttribute(TABLE_ROW_COUNT);
        int rowCount;
        if (numberOfRows == null) rowCount = 0;
        else rowCount = Integer.valueOf(numberOfRows).intValue();
        for (int row=0; row<rowCount; row++) {
        	
        	String rowDataString = dataField.getAttribute(ROW_DATA_NUMBER + row);
        	if (rowDataString != null) {
        		tableModel.addEmptyRow();
        		String[] rowData = rowDataString.split(",");
        		for (int col=0; col<rowData.length; col++) {
        			tableModel.setValueAt(rowData[col].trim(), row, col);
        		}
        	}
        }
        
        if (!tableModel.hasEmptyRow()) {
            tableModel.addEmptyRow();
        }
        
        
        scroller = new JScrollPane(table);
        table.setPreferredScrollableViewportSize(new Dimension(450, 100));
		
		horizontalBox.add(scroller, BorderLayout.SOUTH);
		
		// update new rows etc.
		tableModel.fireTableStructureChanged();
		
		setExperimentalEditing(false);	// default created as uneditable
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
               // System.out.println("TableChanged event: rowCount: " + table.getRowCount() + " columnCount: " + table.getColumnCount());
                
                
                if (column < table.getColumnCount()-1) {
                	table.setColumnSelectionInterval(column + 1, column + 1);
                	table.setRowSelectionInterval(row, row);
                } else if (!tableModel.hasEmptyRow()) {
                	tableModel.addEmptyRow();
                	highlightLastRow(row);
                } else {
                	table.setRowSelectionInterval(row + 1, row + 1);
                	table.setColumnSelectionInterval(0, 0);
                }
               
            }
        }
    }
    
//  overridden by subclasses (when focus lost) if they have values that need saving 
	public void updateDataField() {
		
		// first update col names
		
		String columnNames = "";
		for (int col=0; col<table.getColumnCount(); col++) {
			if (col > 0) columnNames = columnNames + ", ";
			columnNames = columnNames + table.getColumnModel().getColumn(col).getHeaderValue();
		}
		System.out.println("FormFieldTable updateDatafield: " + TABLE_COL_NAMES + " = " + columnNames);
		
		dataField.setAttribute(TABLE_COL_NAMES, columnNames, false);
		
		// now update data
		ArrayList<ArrayList<String>> data = tableModel.getData();
		
		int outputRowNumber = 0;	// count each saved row (don't save empty rows)
		for (int row=0; row<data.size(); row++) {
			if (tableModel.isRowEmpty(row)) continue; // ignore empty rows
			
			ArrayList<String> rowDataArray = data.get(row);
			String rowData = "";
			for (int col=0; col<rowDataArray.size(); col++) {
				if (col > 0) rowData = rowData + ", ";
				rowData = rowData + rowDataArray.get(col).trim();
			}
			String rowId = ROW_DATA_NUMBER + outputRowNumber;
			dataField.setAttribute(rowId, rowData, false);
			//System.out.println("FormFieldTable updateDatafield: " + rowId + " = " + rowData);
			outputRowNumber++;
		}
		
		// delete extra un-needed rows from dataField
		for (int i=outputRowNumber; i<data.size(); i++) {
			String rowId = ROW_DATA_NUMBER + i;
			dataField.setAttribute(rowId, null, false);
		}
		
		dataField.setAttribute(TABLE_ROW_COUNT, Integer.toString(outputRowNumber), false);
	}
	
	
//	 overridden by subclasses if they have other attributes to retrieve from dataField
	public void dataFieldUpdatedOtherAttributes() {
		
		String columns = dataField.getAttribute(DataField.TABLE_COLUMN_NAMES);
		
		// refresh column names....
		if (columns != null) columnNames = columns.split(",");
		
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
	}


}
