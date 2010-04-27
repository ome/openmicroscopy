/*
 * org.openmicroscopy.shoola.util.filter.file.CustomizedFileFilter 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.util.filter.file;


//Java imports
import javax.swing.filechooser.FileFilter;

//Third-party libraries

//Application-internal dependencies

/** 
 * Customized version of a file filter that each filter should extend.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public abstract class CustomizedFileFilter 
	extends FileFilter
{

	/**
	 * Returns <code>true</code> if the file identified by the passed name 
	 * ends with one of the specified extensions, <code>false</code>
	 * otherwise.
	 * 
	 * @param name			The name of the file.
	 * @param extensions	The supported extensions.
	 * @return See above.
	 */
	protected boolean isSupported(String name, String[] extensions)
	{
		if (name == null || extensions == null) return false;
		String value = name.toLowerCase();
		for (int i = 0; i < extensions.length; i++) {
			if (value.endsWith("."+extensions[i])) return true;
		}
		return false;
	}
	
	/**
	 * Returns <code>true</code> if the file identified by its name 
	 * is supported by the filter, <code>false</code> otherwise.
	 * 
	 * @param fileName The name of the file.
	 * @return See above.
	 */
	public abstract boolean accept(String fileName);
	
	/**
	 * Returns the default extension of the file format.
	 * 
	 * @return See above.
	 */
	public abstract String getExtension();
	
	/**
	 * Returns the default extension of the file format.
	 * 
	 * @return See above.
	 */
	public abstract String getMIMEType();
	
}
