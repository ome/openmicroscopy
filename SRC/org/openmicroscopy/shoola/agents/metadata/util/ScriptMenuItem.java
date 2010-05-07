/*
 * org.openmicroscopy.shoola.agents.metadata.util.ScriptMenuItem 
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
import javax.swing.JMenuItem;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.data.model.ScriptObject;

/** 
 * Displays the script.
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
public class ScriptMenuItem 
	extends JMenuItem
{
	
	/** The script to handle. */
	private ScriptObject script;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param script The script to set.
	 */
	public ScriptMenuItem(ScriptObject script)
	{
		if (script == null)
			throw new IllegalArgumentException("A script cannot be null.");
		this.script = script;
		setText(script.getName()+"...");
		setIcon(script.getIcon());
	}
	
	/**
	 * Returns the script.
	 * 
	 * @return See above.
	 */
	public ScriptObject getScript() { return script; }

}
