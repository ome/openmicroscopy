/*
 * measurement.ui.objectmanager.ObjectModel 
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
package org.openmicroscopy.shoola.util.ui.measurement.ui.objectmanager;


//Java imports
import java.util.ArrayList;
import javax.swing.table.AbstractTableModel;

//Third-party libraries
import static org.jhotdraw.draw.AttributeKeys.TEXT;
import org.jhotdraw.draw.Figure;
import org.openmicroscopy.shoola.util.ui.measurement.ui.figures.ROIFigure;
import org.openmicroscopy.shoola.util.ui.measurement.ui.figures.RectAnnotationFigure;
import static org.openmicroscopy.shoola.util.ui.roi.model.annotation.AnnotationKeys.BASIC_TEXT;

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
public class ObjectModel
	extends     AbstractTableModel
{
	
		ArrayList<ROIFigure> data;
		ArrayList<String> columnNames;		
		
		public ObjectModel()
		{
			data = new ArrayList<ROIFigure>();
			columnNames = new ArrayList<String>();
		}
		
		public void update()
		{
			for(int i = 0 ; i < data.size(); i++)
				for(int j = 0 ; j < getColumnCount(); j++)
				{
					Object crap = mapFigureToValue(i,j); 
					setValueAt(crap, i, j);
				}
			fireTableDataChanged();
		}
		
		public void update(ROIFigure fig)
		{
			int i = getRowFromFigure(fig);
			for(int j = 0 ; j < getColumnCount(); j++)
				setValueAt(mapFigureToValue(i,j), i, j);
			fireTableDataChanged();
		}
		
		public int getRowFromFigure(ROIFigure figure)
		{
			for(int i = 0 ; i < data.size(); i++)
			{
				if(figure == data.get(i))
					return i;
			}
			return -1;
		}
		
        public String getColumnName(int col) 
        {
            return columnNames.get(col);
        }

        public void addColumn(String name)
        {
        	columnNames.add(name);
        }
        
        public int getRowCount() 
        { 
        	return data.size(); 
        }
        
        public int getColumnCount() 
        { 
        	return columnNames.size(); 
        }
        
        public void addRow(ROIFigure fig)
        {
        	data.add(fig);
        	fireTableDataChanged();
        }
        
        public void removeRow(ROIFigure fig)
        {
        	data.remove(fig);
        }
        
        public Figure getRow(int row)
        {
         	return data.get(row); 
        }
        
        public Object getValueAt(int row, int col) 
        {
            return mapFigureToValue(row, col); 
        }
        
        public boolean isCellEditable(int row, int col)
        { 
        	return false; 
        }
        
        public void setValueAt(Object value, int row, int col) 
        {
        		mapValueToFigure(value, row, col);
                fireTableCellUpdated(row, col);
        }
        
        public String mapFigureToValue(int row, int col)
        {
        	ROIFigure fig = data.get(row);
        	switch(col)
        	{
        	case 0:
        		return fig.getROI().getID()+"";
        	case 1:
        		return "Depreciated";
        	case 2:
        		return "";
        	case 3:
        		return fig.isVisible() ? "true" : "false";
        	}
        	return null;
        }
        
        public void mapValueToFigure(Object value, int row, int col)
        {
        	Figure fig = data.get(row);
        	switch(col)
        	{
        	case 0:
        		break;
        	case 1:
        		break;
        	case 2:
        		TEXT.set(fig, (String)value);
        		break;
        	case 3:
        		fig.setVisible((Boolean)value);
        		break;
        	}
        }
}

	


