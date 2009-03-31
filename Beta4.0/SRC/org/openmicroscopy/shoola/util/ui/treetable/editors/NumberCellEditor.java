/*
 * org.openmicroscopy.shoola.util.ui.treetable.editors.NumberCellEditor 
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
import javax.swing.DefaultCellEditor;
import javax.swing.JTable;
import javax.swing.JTextField;

//Third-party libraries

//Application-internal dependencies

/** 
 *  Edits number values.
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
public class NumberCellEditor
	extends DefaultCellEditor
	implements ActionListener
{

	/** Reference to textfield component. */
	private JTextField	textField;

	/**
	 * 	Create a new instance.
	 * 	@param textField the textField to use.
	 */
	public NumberCellEditor(JTextField textField)
	{
		super(textField);
		this.textField = textField;
	}

	/**
	 * Overridden to return the component used to edit number cells.
	 * @see DefaultCellEditor#getTableCellEditorComponent(JTable, Object, 
	 * 												boolean, int, int)
	 */
	public Component getTableCellEditorComponent(JTable table, Object value,
			boolean isSelected, int row, int column)
	{
		if (value == null || !(acceptedValue(value))) return textField;
		
		textField.setText(value.toString());
		textField.addActionListener(this);
		return textField;
	}
	
	/**
	 * Returns <code>true</code> if the value passed is an accepted value,
	 * <code>false</code> otherwise.
	 * 
	 * @param value The value to handle.
	 * @return See above.
	 */
	private boolean acceptedValue(Object value)
	{
		if (value instanceof Integer || value instanceof Float ||
			value instanceof Double || value instanceof Long)
			return true;
		return false;
	}
	
	/**
	 * Returns the value of the editor item.
	 * 
	 * @return See above.
	 */
	public Object getCellEditorValue()
	{
		textField.removeActionListener(this);
		return textField.getText();
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