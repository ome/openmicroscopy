/*
 * org.openmicroscopy.shoola.agents.util.ui.JComboBoxImageObject 
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


import javax.swing.Icon;

import org.openmicroscopy.shoola.agents.util.EditorUtil;

import pojos.DataObject;
import pojos.ExperimenterData;
import pojos.GroupData;

/** 
 * Helper class hosting information about group and icon.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since Beta4.4
 */
public class JComboBoxImageObject {

	
	/** The node hosted by this class.*/
	private DataObject data;
	
	/** The icon displayed.*/
	private Icon icon;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param data The node hosted by this class.
	 * @param icon The icon associated.
	 */
	public JComboBoxImageObject(DataObject data, Icon icon)
	{
		this.data = data;
		this.icon = icon;
	}
	
	/**
	 * Returns the icon.
	 * 
	 * @return See above.
	 */
	public Icon getIcon() { return icon; }
	
	/**
	 * Returns the object hosted by this class.
	 * 
	 * @return See above.
	 */
	public DataObject getData() { return data; }
	
	/**
	 * Returns the text associated to the object.
	 * 
	 * @return See above.
	 */
	public String getText()
	{
		if (data instanceof GroupData) {
			return EditorUtil.truncate(((GroupData) data).getName());
		} else if (data instanceof ExperimenterData) {
			return EditorUtil.formatExperimenter((ExperimenterData) data);
		}
		return ""; 
	}
	
}
