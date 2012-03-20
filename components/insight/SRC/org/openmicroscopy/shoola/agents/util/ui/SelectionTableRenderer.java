/*
 * org.openmicroscopy.shoola.agents.util.ui.SelectionTableRenderer 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2012 University of Dundee & Open Microscopy Environment.
 *  All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.agents.util.ui;


//Java imports
import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

//Third-party libraries

//Application-internal dependencies
import pojos.ExperimenterData;
import pojos.GroupData;

/** 
 * 
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since Beta4.4
 */
class SelectionTableRenderer 
	extends DefaultTableCellRenderer
{

	/**
	 * Creates a new instance.
	 * 
	 * @param icon The icon to display.
	 */
	SelectionTableRenderer(Icon icon)
	{
		setIcon(icon);
	}
	
	/**
	 * Overridden to set the correct renderer.
	 * @see DefaultTableCellRenderer#getTableCellRendererComponent(JTable, 
	 * Object, boolean, boolean, int, int)
	 */
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column)
	{
		super.getTableCellRendererComponent(table, value, isSelected, 
				hasFocus, row, column);
		DefaultTableModel dtm = (DefaultTableModel) table.getModel();
		if (column == 0) {
			Object element = (Object) dtm.getValueAt(row, column);
			if (element instanceof ExperimenterData) {
				ExperimenterData exp = (ExperimenterData) element;
				setText(exp.getFirstName()+" "+exp.getLastName());
			} else if (element instanceof GroupData) {
				GroupData group = (GroupData) element;
				setText(group.getName());
			}
		} 
		return this;
	}
	
}
