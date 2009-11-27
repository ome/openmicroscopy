/*
 * org.openmicroscopy.shoolautil.ui.treetable.editors.BooleanCellEditor 
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
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JTable;

//Third-party libraries


//Application-internal dependencies

/** 
 * Edits boolean values.
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
	
	/**
	 * Creates a new instance.
	 * 
	 * @param box The component to use.
	 */
	public BooleanCellEditor(JCheckBox box)
	{
		super(box);
		this.checkBox = box;
	}
	
	/**
	 * Overridden to return the component used to edit boolean cells.
	 * @see DefaultCellEditor#getTableCellEditorComponent(JTable, Object, 
	 * 												boolean, int, int)
	 */
	public Component getTableCellEditorComponent(JTable table, Object value,
									boolean isSelected, int row, int column)
	{
		if (value == null || !(value instanceof Boolean))
			return checkBox;
		
		checkBox.setSelected((Boolean) value);
		checkBox.addItemListener(this);
        return checkBox;
	}

	/**
	 * Returns the value of the editor item.
	 * 
	 * @return See above.
	 */
	public Object getCellEditorValue()
    {
		checkBox.removeItemListener(this);
        return checkBox.isSelected();
    }
    
	/**
	 * The listener method called when the object in the cell changes. 
	 * Posts message to the table.
	 * @param e The item event.
	 */
    public void itemStateChanged(ItemEvent e)
    {
        super.fireEditingStopped();
    }
	
}