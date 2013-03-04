/*
 * org.openmicroscopy.shoola.agents.treeviewer.util.DataMenuItem
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
package org.openmicroscopy.shoola.agents.treeviewer.util;


//Java imports
import javax.swing.Icon;
import javax.swing.JCheckBox;

//Third-party libraries

import pojos.DataObject;
import pojos.GroupData;
//Application-internal dependencies
import pojos.ExperimenterData;
import org.openmicroscopy.shoola.agents.util.EditorUtil;

/**
 * Hosts the experimenter or the group to add to the menu.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since 4.4
 */
public class DataMenuItem 
	extends JCheckBox
{

	/** The object to host.*/
	private DataObject data;
	
	/** Flag indicating if the item can be enabled or not.*/
	private boolean canBeEnabled;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param data The data to host.
	 * @param icon The icon to set.
	 */
	public DataMenuItem(DataObject data, Icon icon)
	{
		this(data, icon, true);
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param data The data to host.
	 * @param icon The icon to set.
	 * @param canBeEnabled Flag indicating if the item can be enabled or not.
	 */
	public DataMenuItem(DataObject data, Icon icon, boolean canBeEnabled)
	{
		if (data == null) 
			throw new IllegalArgumentException("No data");
		if (data instanceof ExperimenterData)
			setText(EditorUtil.formatExperimenter((ExperimenterData) data));
		else if (data instanceof GroupData)
			setText(((GroupData) data).getName());
		if (icon != null) setIcon(icon);
		this.canBeEnabled = canBeEnabled;
		this.data = data;
		setEnabled(true);
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param data The data to host.
	 * @param canBeEnabled Flag indicating if the item can be enabled or not.
	 */
	public DataMenuItem(DataObject data, boolean canBeEnabled)
	{
		this(data, null, canBeEnabled);
	}
	
	/**
	 * Returns the data object.
	 * 
	 * @return See above.
	 */
	public DataObject getDataObject() { return data; }
	
	/**
	 * Overridden to set the enabled flag.
	 * @see #setEnabled(boolean)
	 */
	public void setEnabled(boolean enabled)
	{
		if (!canBeEnabled) enabled = false;
		super.setEnabled(enabled);
	}

}
