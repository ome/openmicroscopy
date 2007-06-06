/*
 * org.openmicroscopy.shoola.agents.measurement.util.ResultsCellRenderer 
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
package org.openmicroscopy.shoola.agents.measurement.util;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.ScrollPane;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import org.openmicroscopy.shoola.util.ui.UIUtilities;

//Java imports

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
public class ResultsCellRenderer 
	extends JComponent
	implements TableCellRenderer
{
	
	final static Color BACKGROUND_COLOUR_EVEN = new Color(241, 245, 250);
	final static Color BACKGROUND_COLOUR_ODD = new Color(255, 255, 255);
	final static Color SELECTED_BACKGROUND_COLOUR = new Color(180, 213, 255);
	final static Color FOREGROUND_COLOUR = new Color(0, 0, 0);
	
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
				(value instanceof Float))
		{
			JLabel label = new JLabel();
			label.setOpaque(true);
			if(value instanceof Double)
			{
				label.setText(twoDecimalPlaces((Double)value));
			}
			else if(value instanceof Float)
			{
				label.setText(twoDecimalPlaces((Float)value));
			}
			else
				label.setText(value+"");
    		thisComponent = label;
    	} 
		else if (value instanceof Color) 
    	{
    		JLabel label = new JLabel();
    		label.setOpaque(true);
    		label.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
    		label.setBackground((Color) value);
    		thisComponent = label;
    	}
    	else if( value instanceof Boolean)
    	{
    		JCheckBox checkBox = new JCheckBox();
    		checkBox.setSelected((Boolean)value);
    		thisComponent = checkBox;
    	}
    	else if(value instanceof ArrayList)
    	{
    		JList list = createList(value);
    		//return new JScrollPane(list);
    		thisComponent = list;
    	}
		if(table.getSelectedRow() == row)
		{
			thisComponent.setBackground(SELECTED_BACKGROUND_COLOUR);
			thisComponent.setForeground(FOREGROUND_COLOUR);
		}
		else
		{
			if(row % 2 == 0)
				thisComponent.setBackground(BACKGROUND_COLOUR_EVEN);
			else
				thisComponent.setBackground(BACKGROUND_COLOUR_ODD);
			thisComponent.setForeground(FOREGROUND_COLOUR);
		}
		return thisComponent;
	}

	private String twoDecimalPlaces(Float value)
	{
		Double newValue = value.doubleValue();
		return UIUtilities.twoDecimalPlaces(newValue);
	}
	
	private String twoDecimalPlaces(Double  value)
	{
		return UIUtilities.twoDecimalPlaces(value);
	}
	
	private JList createList(Object value)
	{
		ArrayList elementList = (ArrayList)value;
		JList list = new JList();
		
		DefaultListModel model = new DefaultListModel();
		for(Object element : elementList)
		{
			if(element instanceof Float)
				model.addElement(twoDecimalPlaces((Float)element));
			else if(element instanceof Double)
				model.addElement(twoDecimalPlaces((Double)element));
			else 
				model.addElement(twoDecimalPlaces((Float)element));
		}
		list.setModel(model);
		return list;
	}
}
