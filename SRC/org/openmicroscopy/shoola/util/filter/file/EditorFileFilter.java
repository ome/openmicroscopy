/*
 * org.openmicroscopy.shoola.util.filter.file.EditorFileFilter 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
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
 * Filters the <code>Editor</code> files.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta3
 */
public class EditorFileFilter 
	extends CustomizedFileFilter
{
	
	/** Possible file extensions. */
	public static final String 	PRO_XML = "pro.xml";
	
	/** The possible extensions. */
	private static final String[] extensions;
	
	static {
		extensions = new String[1];
		extensions[0] = PRO_XML;
	}

	/**
	 * 	Overriden to return the extension of the filter.
	 * 	@see CustomizedFileFilter#getExtensions()
	 */
	public String[] getExtensions() { return extensions; }
	
	/**
	 * 	Overriden to return the extension of the filter.
	 * 	@see CustomizedFileFilter#getExtension()
	 */
	public String getExtension() { return PRO_XML; }
	
    /**
     * Overriden to return the description of the filter.
     * @see FileFilter#getDescription()
     */
	public String getDescription() { return "OMERO.Editor (.pro.xml)"; }
		
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
