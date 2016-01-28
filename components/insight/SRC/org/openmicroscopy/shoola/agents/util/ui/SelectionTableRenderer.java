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

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import org.openmicroscopy.shoola.agents.util.EditorUtil;
import org.openmicroscopy.shoola.util.ui.IconManager;

import omero.gateway.model.ExperimenterData;
import omero.gateway.model.GroupData;

/** 
 * Displays the object added to the selection table.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since Beta4.4
 */
class SelectionTableRenderer 
	extends DefaultTableCellRenderer
{

	/** The private group icon.*/
	private static Icon PRIVATE;
	
	/** The icon for <code>RWR---</code> group.*/
	private static Icon READ_GROUP;
	
	/** The icon for <code>RWRA--</code> group.*/
	private static Icon READ_LINK;
	
	/** The icon for <code>RWRW--</code> group.*/
	private static Icon READ_WRITE;
	
	/** The private group icon.*/
	private static Icon PUBLIC;
	
	static {
		IconManager icons = IconManager.getInstance();
		PRIVATE = icons.getIcon(IconManager.PRIVATE_GROUP);
		READ_GROUP = icons.getIcon(IconManager.READ_GROUP);
		READ_LINK = icons.getIcon(IconManager.READ_LINK_GROUP);
		READ_WRITE = icons.getIcon(IconManager.READ_WRITE_GROUP);
		PUBLIC = icons.getIcon(IconManager.PUBLIC_GROUP);
	}
	
	/** The ref table.*/
	private SelectionTable model;
	
	/**
	 * Sets the icon corresponding to the permissions of the specified group.
	 * 
	 * @param group The group to handle.
	 */
	private void setGroupIcon(GroupData group)
	{
		switch (model.getLevel(group)) {
			case GroupData.PERMISSIONS_PRIVATE:
				setIcon(PRIVATE);
				break;
			case GroupData.PERMISSIONS_GROUP_READ:
				setIcon(READ_GROUP);
				break;
			case GroupData.PERMISSIONS_GROUP_READ_LINK:
				setIcon(READ_LINK);
				break;
			case GroupData.PERMISSIONS_GROUP_READ_WRITE:
				setIcon(READ_WRITE);
				break;
			case GroupData.PERMISSIONS_PUBLIC_READ:
				setIcon(PUBLIC);
		}
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param model The value to set.
	 */
	SelectionTableRenderer(SelectionTable model)
	{
		this.model = model;
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param icon The icon to display.
	 */
	SelectionTableRenderer(Icon icon)
	{
		if (icon != null) setIcon(icon);
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
				setText(EditorUtil.formatExperimenter(exp));
			} else if (element instanceof GroupData) {
				GroupData group = (GroupData) element;
				setText(group.getName());
				setGroupIcon(group);
			}
		} 
		return this;
	}
	
}
