package table;

import java.util.ArrayList;

import javax.swing.table.AbstractTableModel;

public class InteractiveTableModel extends AbstractTableModel {

	// columnNames is not updated when col names change.
	// used to initialise columns, 
	// then simply keeps track of NUMBER of cols, I think (names handled by ColumnModel)
    protected ArrayList<String> columnNames = new ArrayList<String>() ;
    protected ArrayList<ArrayList<String>> data = new ArrayList<ArrayList<String>>();

    public InteractiveTableModel(String[] colNames) {
    	
    	for (int i=0; i<colNames.length; i++){
    		columnNames.add(colNames[i]);
    	}
        
        System.out.println("InteractiveTableModel constructor. " +
        		"rows = " + getRowCount() + ", cols = " + getColumnCount());
    }

    public String getColumnName(int column) {
        return columnNames.get(column);
    }

    public boolean isCellEditable(int row, int column) {
        if (column > getColumnCount()) return false;
        else return true;
    }

    public Object getValueAt(int row, int column) {
    	// System.out.println("getValueAt row = " + row + ", col = " + column);
    	// System.out.println("getRowCount = " + getRowCount() + ", getColCount = " + getColumnCount());
    	if ((row < getRowCount()) && (column < getColumnCount())) {
    		String value = data.get(row).get(column);
    		if (value == null) return "";
    		else return value;
    	}
    	else return "";
    }

    public void setValueAt(Object value, int row, int column) {
        data.get(row).set(column, (String)value);
        fireTableCellUpdated(row, column);
    }

    public int getRowCount() {
        return data.size();
    }

    public int getColumnCount() {
        return columnNames.size();
    }

    public boolean hasEmptyRow() {
        if (getRowCount() == 0) return false;
        int lastRow = getRowCount() -1;
        
        for(int i=0; i<data.get(lastRow).size(); i++) {
        	String value = (String)getValueAt(lastRow, i);
        	
        	System.out.println("LastRowEmpty value = " + value + ", row = " + lastRow + ", col = " + i);
        	if (!value.trim().equals("")) return false;
        }
        return true;
    }
    
    public boolean isRowEmpty(int row) {
    	for (int col=0; col<data.get(row).size(); col++){
    		String value = (String)getValueAt(row, col);
    		if(value.trim().length() > 0) return false;
    	}
    	return true;
    }

    public void addEmptyRow() {
    	//System.out.println("Adding empty row...");
    	
    	ArrayList<String> newRow = new ArrayList<String>();
    	for (int i=0; i<getColumnCount(); i++) {
    		newRow.add(" ");
    	}
        data.add(newRow);
        
        fireTableRowsInserted(
           getRowCount() - 1,
           getRowCount() - 1);
    }
    
    public void addEmptyColumn() {
    	
    	columnNames.add("new column");
    	
    	for(ArrayList<String> row: data) {
    		row.add("");
    	}
    	
    	this.fireTableStructureChanged();
    	// this updates the table, which updates it's own ColumnModel (I think).
    }
    
    public void removeLastColumn() {
    	// System.out.println("Removing last column...");
    	
    	columnNames.remove(columnNames.size()-1);
    	
    	int colCount = getColumnCount();
    	for(ArrayList<String> row: data) {
    		row.remove(colCount-1);
    	}
    	this.fireTableStructureChanged();
    }
    
    public ArrayList<ArrayList<String>> getData() {
    	return data;
    }
    public ArrayList<String> getColumnNames() {
    	return columnNames;
    }
}
