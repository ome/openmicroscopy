/*
 * org.openmicroscopy.shoolautil.ui.treetable.editors.DateCellEditor 
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
package org.openmicroscopy.shoola.util.ui.treetable.editors;

//Java imports
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DateFormat;
import java.util.Date;

import javax.swing.AbstractCellEditor;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

//Third-party libraries
import org.jdesktop.swingx.JXDatePicker;

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
public class DateCellEditor 
	extends AbstractCellEditor 
	implements TableCellEditor, ActionListener
{

	/** Reference to date component. */
	private JXDatePicker	datePicker;
	
	/**
	 * Create a new instance.
	 * @param picker the date picker component.
	 */
	public DateCellEditor(JXDatePicker picker)
	{
		super();
		datePicker = picker;
		datePicker.setFormats(new DateFormat[] { DateFormat
			.getDateInstance(DateFormat.SHORT) });
	}
	
	/**
	 * Get the value of the DateCellEditor
	 * @return see above.
	 */
	public Object getCellEditorValue()
	{		datePicker.removeActionListener(this);
		return datePicker.getDate();
	}
	
	/**
	 * Get the component used to edit date cells
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
		
		if (value!=null&&value instanceof Date)
		{
			datePicker.setDate((Date) value);
			datePicker.addActionListener(this);
		}
		return datePicker;
	}
	
	/**
	 * listener method called when the object in the cell changes. Posts message
	 * to the table.
	 * 
	 * @param e the actionevent.
	 */
	public void actionPerformed(ActionEvent e)
	{
		super.fireEditingStopped();
	}

	
	
}
