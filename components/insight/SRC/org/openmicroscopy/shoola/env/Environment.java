/*
 * org.openmicroscopy.shoola.env.Environment
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

package org.openmicroscopy.shoola.env;


//Java imports
import java.io.File;

//Third-party libraries

//Application-internal dependencies

/** 
 * Lets agents access information about the container's runtime environment.
 * Agents can retrieve an <code>Environment</code> object from their registries:
 * <p><code>
 * Environment env = (Environment) registry.lookup(LookupNames.ENV);
 * </code></p> 
 * 
 * @see LookupNames
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @author <br>Jeff Mellen &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a>
 * @version 2.2.1
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class Environment 
{

	/** Reference to the container, not to be leaked. */
	private Container	container;
	
	
	/**
	 * Creates a new instance.
	 * This constructor is only meant to be used by the container.
	 * 
	 * @param c	Reference to the container.
	 */
	Environment(Container c) 
	{
		container = c;
	}
	
	/**
	 * Returns the absolute path to the installation directory.
	 * 
	 * @return	See above.
	 */
	public String getHomeDir()
	{
		return container.getHomeDir();
	}
	
	/**
	 * Returns the location of the temporary directory.
	 * 
	 * @return See above.
	 */
	public String getTmpDir()
	{
		return System.getProperty("java.io.tmpdir");
	}
	
	/**
	 * Resolves the specified pathname against the installation directory.
	 * 
	 * @param relPathName	The pathname to resolve.
	 * @return	The absolute pathname obtained by resolving
	 * 			<code>relPathName</code> against the installation directory.
	 */
	public String resolvePathName(String relPathName)
	{
		File f = new File(getHomeDir(), relPathName);
		return f.getAbsolutePath();
	}

	/**
	 * Returns the value of the plug-in or <code>-1</code>.
	 * 
	 * @return See above.
	 */
	public int runAsPlugin()
	{
		Integer v = (Integer) container.getRegistry().lookup(
				LookupNames.PLUGIN);
		if (v == null) return -1;
		return v.intValue();
	}
	
	/**
	 * Returns <code>true</code> if the application is run as a plugin,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	public boolean isRunAsPlugin() { return runAsPlugin() > 0; }
	
	/**
	 * Returns the default hierarchy i.e. P/D, HCS etc.
	 * 
	 * @return See above.
	 */
	public int getDefaultHierarchy()
	{
		Integer v = (Integer) container.getRegistry().lookup(
				LookupNames.ENTRY_POINT_HIERARCHY);
		if (v == null) return LookupNames.PD_ENTRY;
		int value = v.intValue();
		switch (value) {
			case LookupNames.PD_ENTRY:
			case LookupNames.HCS_ENTRY:
			case LookupNames.TAG_ENTRY:
			case LookupNames.ATTACHMENT_ENTRY:
				return value;
			default:
				return LookupNames.PD_ENTRY;
		}
	}
	
	/**
	 * Returns the location of the <code>omero</code> directory 
	 * on the user's machine.
	 * 
	 * @return See above.
	 */
	public String getOmeroHome()
	{
		return (String) 
			container.getRegistry().lookup(LookupNames.USER_HOME_OMERO);
	}
	
	/**
	 * Returns the location of the <code>omero</code> directory 
	 * on the user's machine.
	 * 
	 * @return See above.
	 */
	public String getOmeroFilesHome()
	{
		String home = (String) 
			container.getRegistry().lookup(LookupNames.USER_HOME_OMERO);
		String name = (String) 
			container.getRegistry().lookup(LookupNames.OMERO_FILES_HOME);
		return home+File.separator+name;
	}
	
}

