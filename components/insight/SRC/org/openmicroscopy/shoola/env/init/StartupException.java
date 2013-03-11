/*
 * org.openmicroscopy.shoola.env.init.StartupException
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
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

package org.openmicroscopy.shoola.env.init;


//Java imports

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.config.PluginInfo;


/** 
 * Reports an error occurred during initialization.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2 
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */

public class StartupException 
	extends Exception
{
	
	/** The information about the plugin if any.*/
	private PluginInfo pluginInfo;
	
	/**
	 * Constructs a new exception with the specified detail message.
	 * 
	 * @param message	Short explanation of the problem.
	 */
	public StartupException(String message) 
	{
		super(message);
	}
	
	/**
	 * Constructs a new exception with the specified detail message and cause.
	 * 
	 * @param message	Short explanation of the problem.
	 * @param cause		The exception that caused this one to be risen.
	 */
	public StartupException(String message, Throwable cause)
	{
		super(message, cause);
	}

	/**
	 * Returns the fully qualified name of the class whose instance rose this
	 * exception.
	 * 
	 * @return	See above.
	 */
	public String getOriginator() 
	{
		String originator = "unknown";
		StackTraceElement[] stack = getStackTrace();
		if (stack != null && 0 < stack.length)
			originator = stack[0].getClassName();
		return originator;
	}

	/**
	 * Returns the information about the plugin if used in plugin mode.
	 * 
	 * @return See above.
	 */
	public PluginInfo getPlugin() { return pluginInfo; }
	
	/**
	 * Sets the plugin's information.
	 * 
	 * @param plugin The value to set.
	 */
	public void setPluginInfo(PluginInfo pluginInfo)
	{
		this.pluginInfo = pluginInfo;
	}

}
