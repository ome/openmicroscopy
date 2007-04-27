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
package org.openmicroscopy.shoola.util.ui.measurement.ui.objectinspector;

//Java imports
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import javax.swing.table.AbstractTableModel;

//Third-party libraries
import org.jhotdraw.draw.AttributeKey;
import org.jhotdraw.draw.AttributeKeys;
import org.jhotdraw.draw.Figure;

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
public 	class 	FigureModel 
		extends AbstractTableModel
{
		Figure 						figure;
		ArrayList<String> 			columnNames;		
		ArrayList<AttributeKey>		keys;
		ArrayList<Object>			values;
		ArrayList<AttributeField>	fieldList;
		
		public FigureModel(ArrayList<AttributeField> fieldList)
		{
			columnNames = new ArrayList<String>();
			keys = new ArrayList<AttributeKey>();
			values = new ArrayList<Object>();
			this.fieldList = fieldList;
		}
		
		public void setModelData(Figure fig)
		{
			figure = fig;
			keys.clear();
			values.clear();
			
			
			for(AttributeField fieldName : fieldList)
			{
				boolean found = false;
				Iterator<AttributeKey> attributeIterator = figure.getAttributes().keySet().iterator();
				while(attributeIterator.hasNext()) 
				{
					AttributeKey key = attributeIterator.next();
					if(key==fieldName.key)
					{
						keys.add(key);
						values.add(figure.getAttributes().get(key));
						found = true;
						break;
					}
				}
				if(found == false)
				{
					keys.add(fieldName.key);
					values.add("N/A");
				}
    		}
    		this.fireTableDataChanged();
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
        	if(col == 0)
        		return fieldList.get(row).fieldName;
        	return values.get(row);
    	}
        
        public void setValueAt(Object value, int row, int col) 
        {
        	if(figure.getAttribute(keys.get(row)) instanceof Double)
        	{
        		figure.setAttribute(keys.get(row), new Double((String)value));
        		values.set(row, value);
            }
        	else
        	{
        		figure.setAttribute(keys.get(row), value);
        		values.set(row, value);
            }
        	fireTableCellUpdated(row, col);
        }
        
        public boolean isCellEditable(int row, int col)
        { 
        	if(values.get(row) instanceof String)
        		if(values.get(row) == "N/A")
        			return false;
        	return fieldList.get(row).editable;
        }

		public int getRowCount() 
		{
			return keys.size();
		}
        
}


