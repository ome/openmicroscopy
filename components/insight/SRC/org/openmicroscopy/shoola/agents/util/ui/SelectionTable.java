/*
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

import java.util.List;
import java.util.Vector;
import javax.swing.Icon;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import omero.gateway.model.GroupData;

/** 
 * Customized table to display the groups/users to select.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since Beta4.4
 */
class SelectionTable 
	extends JTable
{

	/** The columns.*/
	private static Vector<String> COLUMNS;
	
	static {
		COLUMNS = new Vector<String>(2);
		COLUMNS.add("");
		COLUMNS.add("");
	}
	
	/** The groups to display.*/
	private List<GroupData> groups;
	
	/** Creates a new instance.*/
	SelectionTable()
	{
		this(null);
	}
	
	/** 
	 * Creates a new instance.
	 * 
	 * @param icon The icon to display.
	 */
	SelectionTable(Icon icon)
	{
		setTableHeader(null);
		setModel(new SelectionTableModel(COLUMNS));
		TableColumnModel tcm = getColumnModel();
		TableColumn tc = tcm.getColumn(0);
		if (icon != null) tc.setCellRenderer(new SelectionTableRenderer(icon));
		else tc.setCellRenderer(new SelectionTableRenderer(this));
		
		tc = tcm.getColumn(1);
		tc.setCellEditor(getDefaultEditor(Boolean.class));
		tc.setCellRenderer(getDefaultRenderer(Boolean.class));
	}

	/**
	 * Sets the groups.
	 * 
	 * @param groups The groups to set.
	 */
	void setGroups(List<GroupData> groups)
	{
		this.groups = groups;
	}
	
	/**
	 * Returns the permissions level of the group.
	 * 
	 * @param group The group to handle.
	 * @return See above.
	 */
	int getLevel(GroupData group)
	{
		return group.getPermissions().getPermissionsLevel();
	}
	
	/** Inner class to display checkbox.*/
	class SelectionTableModel 
		extends DefaultTableModel
	{

		/**
		 * Creates a new instance.
		 * 
		 * @param columns The name of the columns.
		 */
		SelectionTableModel(Vector<String> columns)
		{
			super(null, columns);
		}
		
		/**
		 * Overridden so that some cells cannot be edited.
		 * @see DefaultTableModel#isCellEditable(int, int)
		 */
		public boolean isCellEditable(int row, int column)
		{ 
			return (column == getColumnCount()-1);
		}
		
		/**
		 * Returns the class so we can display checkboxes
		 * @see DefaultTableModel#getColumnClass(int)
		 */
		public Class getColumnClass(int column)
		{
	        return getValueAt(0, column).getClass();
	    }
	}

}
