/*
 * org.openmicroscopy.shoola.util.filter.file.PythonFilter 
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
package org.openmicroscopy.shoola.util.filter.file;


//Java imports
import java.io.File;
import javax.swing.filechooser.FileFilter;

//Third-party libraries

//Application-internal dependencies

/** 
 * Filters the <code>Python</code> files.
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
public class PythonFilter 	
	extends CustomizedFileFilter
{
	
	/** Possible file extensions. */
	public static final String 	PYTHON = "py";
    
	/** The possible extensions. */
	public static final String[] 	extensions;
	
	/** The description of the filter. */
	private static final String		description;
	
	static {
		extensions = new String[1];
		extensions[0] = PYTHON;
		StringBuffer s = new StringBuffer();
		s.append("Python (");
		for (int i = 0; i < extensions.length; i++) {
			s.append("*."+extensions[i]);
			if (i < extensions.length-1)
				s.append(", ");
		}
		s.append(")");
		description = s.toString();
	}
	
	/**
	 * 	Overridden to return the MIME type.
	 * 	@see CustomizedFileFilter#getMIMEType()
	 */
	public String getMIMEType() { return "application/octet-stream"; }
	
	/**
	 * 	Overridden to return the extension of the filter.
	 * 	@see CustomizedFileFilter#getExtension()
	 */
	public String getExtension() { return PYTHON; }
	
    /**
     * Overridden to return the description of the filter.
     * @see FileFilter#getDescription()
     */
	public String getDescription() { return description; }
		
    /**
     * Overridden to accept file with the declared file extensions.
     * @see FileFilter#accept(File)
     */
	public boolean accept(File f)
	{
		if (f == null) return false;
		if (f.isDirectory()) return true;
		return isSupported(f.getName(), extensions);
	}
	
	/**
	 * Overridden to accept the file identified by its name.
	 * @see CustomizedFileFilter#accept(String)
	 */
	public boolean accept(String fileName)
	{
		return isSupported(fileName, extensions);
	}
	
}
