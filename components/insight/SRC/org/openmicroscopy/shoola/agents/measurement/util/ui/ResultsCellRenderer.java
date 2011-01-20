/*
 * org.openmicroscopy.shoola.agents.measurement.util.ui.ResultsCellRenderer 
 *
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
 */
package org.openmicroscopy.shoola.agents.measurement.util.ui;




//Java imports
import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.roi.model.util.FigureType;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * Basic cell renderer displaying analysis results.
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
public class ResultsCellRenderer 
	extends JComponent
	implements TableCellRenderer
{
	
	/**
	 * Formats the passed object to two decimal places and returns as a string.
	 * 
	 * @param value The object to handle.
	 * @return See above.
	 */
	private String twoDecimalPlaces(Float value)
	{
		return UIUtilities.twoDecimalPlaces(value.doubleValue());
	}
	
	/**
	 * Formats the passed object to two decimal places and returns as a string.
	 * 
	 * @param value The object to handle.
	 * @return See above.
	 */
	private String twoDecimalPlaces(Double  value)
	{
		return UIUtilities.twoDecimalPlaces(value);
	}
		
	/**
	 * Creates and returns a {@link JList} from the passed object.
	 * 
	 * @param value The object to handle.
	 * @return See above.
	 */
	private JList createList(Object value)
	{
		ArrayList elementList = (ArrayList)value;
		JList list = new JList();
		DefaultListModel model = new DefaultListModel();
		String v;
		for(Object element : elementList)
		{
			if(element instanceof Float)
			{
				v = twoDecimalPlaces((Float) element);
				if (v == null) return list;
				model.addElement(v);
			}
			else if (element instanceof Double)
			{
				v = twoDecimalPlaces((Double) element);
				if (v == null)
					return list;
				model.addElement(v);
			}
		}
		list.setModel(model);
		return list;
	}
	
	/**
	 * Creates a new instance. Sets the opacity of the label to 
	 * <code>true</code>.
	 */
	public ResultsCellRenderer()
	{
		setOpaque(true);
	}
	
	/**
	 * @see TableCellRenderer#getTableCellRendererComponent(JTable, Object, 
	 * 										boolean, boolean, int, int)
	 */
	public Component getTableCellRendererComponent(JTable table, Object value, 
			boolean isSelected, boolean hasFocus, int row, int column)
	{
		Component thisComponent = new JLabel();
		if ((value instanceof Integer) || (value instanceof Long) ||
				(value instanceof Double) || (value instanceof String) || 
				(value instanceof Float) || (value instanceof FigureType))
		{
			JLabel label = new JLabel();
			label.setOpaque(true);
			if (value instanceof Double)
				label.setText(twoDecimalPlaces((Double) value));
			else if (value instanceof Float)
				label.setText(twoDecimalPlaces((Float) value));
			else label.setText(value+"");
    		thisComponent = label;
    	}  else if (value instanceof Color)  {
    		JLabel label = new JLabel();
    		label.setOpaque(true);
    		label.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
    		label.setBackground((Color) value);
    		thisComponent = label;
      	} else if( value instanceof Boolean) {
      		JCheckBox checkBox = new JCheckBox();
    		checkBox.setSelected((Boolean)value);
    		thisComponent = checkBox;
    	} else if(value instanceof ArrayList) {
    		thisComponent = createList(value);
    		//return list;
    	}
		if (!(value instanceof Color)) {
			RendererUtils.setRowColor(thisComponent, table.getSelectedRow(), 
									row);
		}
		return thisComponent;
	}

}
