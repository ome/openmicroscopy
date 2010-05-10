/*
 * org.openmicroscopy.shoola.agents.metadata.util.ScriptSubMenu 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2010 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.metadata.util;


//Java imports
import javax.swing.JMenu;


//Third-party libraries
import org.apache.commons.lang.WordUtils;

//Application-internal dependencies
import org.openmicroscopy.shoola.env.data.model.ScriptObject;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * Displays the available scripts within a given directory.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class ScriptSubMenu 
	extends JMenu
{

	/** The default name of the menu. */
	private static final String NAME = "";
	
	/** The full path to the directory. */
	private String path;
	
	/** 
	 * Formats the name of the menu. 
	 * 
	 * @return See above.
	 * */
	private String formatName()
	{
		if (path == null) return NAME;
		String[] values = UIUtilities.splitString(path);
		int n = values.length-1;
		String value = values[n];
		if (value.length() == 0) {
			if (n > 1) {
				value = values[n-1];
			}
		}
		if (value == null || value.trim().length() == 0) return NAME;
		value = value.replace(ScriptObject.PARAMETER_SEPARATOR, 
				ScriptObject.PARAMETER_UI_SEPARATOR);
		return WordUtils.capitalize(value);
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param path The full path to the directory.
	 */
	public ScriptSubMenu(String path)
	{
		this.path = path;
		setText(formatName());
	}
	
	/**
	 * Adds the passed script to the menu.
	 * 
	 * @param script The script to add.
	 * @return See above.
	 */
	public ScriptMenuItem addScript(ScriptObject script)
	{
		ScriptMenuItem item = new ScriptMenuItem(script);
		add(item);
		return item;
	}
	
}
