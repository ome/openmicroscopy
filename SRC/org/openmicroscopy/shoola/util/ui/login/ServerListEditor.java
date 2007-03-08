/*
 * org.openmicroscopy.shoola.util.ui.login.ServerListEditor 
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
package org.openmicroscopy.shoola.util.ui.login;


//Java imports
import java.awt.Component;
import javax.swing.AbstractCellEditor;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.TableCellEditor;

//Third-party libraries

//Application-internal dependencies

/** 
 * Customized editor to indent the text when a new row is added to the 
 * table.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class ServerListEditor 
	extends AbstractCellEditor 
	implements TableCellEditor
{

	/** The component handling the editing of the cell value. */
	private JTextField component;

	/** Creates a new instance. */
	public ServerListEditor()
	{
		component = new JTextField();
	}
	
    /**
     * Implements as specified by the {@link TableCellEditor} Interface.
     * @see TableCellEditor#getTableCellEditorComponent(JTable, Object, boolean,
     * 													 int, int)
     */
    public Component getTableCellEditorComponent(JTable table, Object value,
            boolean isSelected, int rowIndex, int vColIndex) 
    {
    	//Invokdes when a cell value is edited by the user.
        if (value != null) {
        	String v = (String) value;
        	if (v == null || v.trim().length() == 0) v = " ";
        	component.setText(v);
        }
        return component;
    }

    /**
     * returns the edited text. This method is invoked when the editing is
     * completed
     * 
     * @return The edited text. 
     */
    public Object getCellEditorValue() { return component.getText(); }
    
}
