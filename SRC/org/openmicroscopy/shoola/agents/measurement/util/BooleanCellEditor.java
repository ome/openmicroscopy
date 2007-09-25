/*
 * org.openmicroscopy.shoola.agents.measurement.util.BooleanCellEditor 
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

//Java imports
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.EventObject;

import javax.swing.AbstractCellEditor;
import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JRadioButton;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
//Third-party libraries

import org.openmicroscopy.shoola.agents.measurement.view.ROITable;

import com.sun.corba.se.spi.legacy.connection.GetEndPointInfoAgainException;

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
public class BooleanCellEditor
	extends DefaultCellEditor
    implements ItemListener
{

	/** Reference to boolean component. */
	private JCheckBox	checkBox;
	
	private ROITable	table;
	
	int row, column;
	
	//static boolean 		cascadeUpdate;
	
	/**
	 * Create a new instance.
	 */
	/*
	public BooleanCellEditor(ROITable table)
	{
		this.table = table;
		checkBox=new JCheckBox();
		checkBox.addItemListener(this);
	//	cascadeUpdate = false;
	}
	*/
	public BooleanCellEditor(JCheckBox box)
	{
		super(box);
		this.checkBox = box;
	}
	//public static void setCascadeUpdate(boolean value)
	//{
	//	cascadeUpdate = value;
	//}
	
	/**
	 * Get the value of the DateCellEditor
	 * @return see above.
	 */
	/*
	public Object getCellEditorValue()
	{
		return checkBox.isSelected();
	}
	*/
	
	/**
	 * Get the component used to edit boolean cells
	 * @param table  the table this object edits.
	 * @param value  the value to be edited
	 * @param isSelected  indicates whether or not the cell is selected
	 * @param row  number of the row being edited
	 * @param column number of the column being edited
	 * @return  editor component to use
	 */
	
	public Component getTableCellEditorComponent(
	JTable table, Object value, boolean isSelected, int row, int column)
	{
		if (value == null || !(value instanceof Boolean))
				return checkBox;
		
		checkBox.setSelected((Boolean) value);
		checkBox.addItemListener(this);
        return checkBox;
		
        /*
		this.row = row;
		this.column = column;
		if (value!=null&&value instanceof Boolean)
			checkBox.setSelected((Boolean) value);
		return checkBox;
		*/
	}

	public Object getCellEditorValue()
    {
		checkBox.removeItemListener(this);
        return checkBox.isSelected();
    }
    
    public void itemStateChanged(ItemEvent e)
    {
        super.fireEditingStopped();
    }
	
}