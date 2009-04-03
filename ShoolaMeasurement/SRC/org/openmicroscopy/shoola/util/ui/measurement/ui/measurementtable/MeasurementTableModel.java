/*
 * measurement.ui.objectinspector.FigureModel 
 *
  *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.util.ui.measurement.ui.measurementtable;

//Java imports
import java.util.ArrayList;
import javax.swing.table.AbstractTableModel;

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
public 	class 	MeasurementTableModel 
		extends AbstractTableModel
{
		ArrayList<String> 			columnNames;		
		ArrayList					values;
		
		public MeasurementTableModel()
		{
			columnNames = new ArrayList<String>();
			values = new ArrayList();
			
		}
		
		
		public void addRow(TableRow row)
		{
			values.add(row);
        	this.fireTableStructureChanged();
		}
    	
        public String getColumnName(int col) 
        {
            return columnNames.get(col);
        }

        public void addColumn(String name)
        {
        	columnNames.add(name);
        }
                
        public int getColumnCount() 
        { 
        	return columnNames.size(); 
        }
                
        public Object getValueAt(int row, int col) 
        {
        	
        	TableRow rowData = (TableRow)values.get(row);
        	return rowData.get(col);
    	}
        
        public void setValueAt(Object value, int row, int col) 
        {
        	TableRow rowData = (TableRow)values.get(row);
        	rowData.set(col, value);
        	fireTableCellUpdated(row, col);
        }
        
        public void print()
        {
        	for(int i = 0 ; i < getRowCount() ; i++)
        		for( int j = 0 ; j < getColumnCount() ; j++)
        		{
        			TableRow row = (TableRow)values.get(i);
        		}
        }
        
        public boolean isCellEditable(int row, int col)
        { 
        	return false;
        }

		public int getRowCount() 
		{
			return values.size();
		}
        
}


