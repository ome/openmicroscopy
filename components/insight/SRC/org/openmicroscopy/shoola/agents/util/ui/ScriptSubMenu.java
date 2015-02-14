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
package org.openmicroscopy.shoola.agents.util.ui;


import java.util.List;

import javax.swing.JMenu;

import org.openmicroscopy.shoola.env.data.model.ScriptObject;
import org.openmicroscopy.shoola.util.CommonsLangUtils;
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

	/** The text before being formatted. */
	private String unformattedText;

	/**
	 * Moves up in the path if the name is already taken.
	 * 
	 * @param values The path as an array.
	 * @param index The current index.
	 * @param sep   The file separator.
	 * @param names The names already taken.
	 * @param value The value to handle.
	 * @return See above.
	 */
	private String getValue(String[] values, int index, String sep,
			List<String> names, String value)
	{
		if (value == null || value.length() == 0) return value;
		if (names.contains(value)) {
			int n = values.length-1;
			if (n-index >= 0) {
				String v = values[n-index]+sep+value;
				return v;
			}
			return value;
		}
		return value;
	}

	/** 
	 * Formats the name of the menu.
	 * 
	 * @param names The collection of formatted names already taken.
	 * @return See above.
	 * */
	private String formatName(List<String> names)
	{
		if (path == null) return NAME;
		String[] values = UIUtilities.splitString(path);
		String sep = UIUtilities.getStringSeparator(path);
		if (values == null || sep == null) return path;
		int index = 0;
		if (path.endsWith(sep)) index = 1;
		String value = getValue(values, index, sep, names,
				values[values.length-1]);
		if (value == null || value.trim().length() == 0) return NAME;
		unformattedText = value;
		value = value.replace(ScriptObject.PARAMETER_SEPARATOR,
				ScriptObject.PARAMETER_UI_SEPARATOR);
		return CommonsLangUtils.capitalize(value);
	}

	/**
	 * Creates a new instance.
	 * 
	 * @param path The full path to the directory.
	 * @param names The collection of formatted names already taken.
	 */
	public ScriptSubMenu(String path, List<String> names)
	{
		this.path = path;
		setText(formatName(names));
		setToolTipText(path);
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

	/** 
	 * Returns the text before being formatted.
	 * 
	 * @return See above.
	 */
	public String getUnformattedText() { return unformattedText; }

}
