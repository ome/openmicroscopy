/*
 * org.openmicroscopy.shoola.env.data.model.ApplicationData 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2009 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.env.data.model;


//Java imports
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * Hosts information about an external a
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
public class ApplicationData
{

	/** The default location on <code>MAC</code> platform. */
	public static final String LOCATION_MAC = "/Applications";
	
	/** The default location on <code>Windows</code> platform. */
	public static final String LOCATION_WINDOWS = "/Applications";
	
	/** The default location <code>Linux</code> platform. */
	public static final String LOCATION_LINUX = "/Applications";
	
	/** Path to the resources of the application on MAC. */
	private static final String RESOURCES_MAC = "/Contents/Resources";
	
	/** The path to the application. */
	private File file;
	
	public static String getDefaultLocation()
	{
		if (UIUtilities.isMacOS()) return LOCATION_MAC;
		if (UIUtilities.isWindowsOS()) return LOCATION_WINDOWS;
		return LOCATION_LINUX;
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param file the application.
	 */
	public ApplicationData(File file)
	{
		this.file = file;
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param path The path to the application.
	 */
	public ApplicationData(String path)
	{
		this(new File(path));
	}
	
	/**
	 * Returns the name of the application.
	 * 
	 * @return See above.
	 */
	public String getApplicationName()
	{
		return UIUtilities.removeFileExtension(file.getName());
	}
	
	/**
	 * Returns the icon associated to the application.
	 * 
	 * @return See above.
	 */
	public Icon getApplicationIcon()
	{
		if (UIUtilities.isMacOS()) {
			String path = getApplicationPath()+RESOURCES_MAC;
			File f = new File(path);
			String[] l = f.list();
			String name = null;
			for (int i = 0; i < l.length; i++) {
				name = l[i];
				if (name.endsWith(".icns")) break;
			}
			if (name == null) return null;
			return new ImageIcon(name);
		}
		return null;
	}
	
	/**
	 * Returns the application's path.
	 * 
	 * @return See above.
	 */
	public String getApplicationPath()
	{
		return file.getAbsolutePath();
	}
	
	/**
	 * Returns the arguments.
	 * 
	 * @return See above.
	 */
	public List<String> getArguments()
	{
		List<String> list = new ArrayList<String>();
		list.add("open");
		String name = getApplicationPath();
		if (name != null && name.length() > 0)
			list.add(name);
		return list;
	}
	
}
