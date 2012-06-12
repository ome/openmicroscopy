/*
 * org.openmicroscopy.shoola.util.filter.file.HCSFilter 
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
package org.openmicroscopy.shoola.util.filter.file;


//Java imports
import java.io.File;
import javax.swing.filechooser.FileFilter;

//Third-party libraries

//Application-internal dependencies

/** 
 * The list of supported HCS formats.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since Beta4.4
 */
public class HCSFilter 
	extends CustomizedFileFilter
{

	/** The description of the filter. */
	private static final String description = "All supported HCS types";
	
	/** The supported extensions.*/
	private String[] extensions;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param extensions The supported extensions.
	 */
	public HCSFilter(String[] extensions)
	{
		this.extensions = extensions;
	}
	
	/**
	 * 	Overridden to return the MIME type.
	 * 	@see CustomizedFileFilter#getMIMEType()
	 */
	public String getMIMEType() { return ""; }
	
	/**
	 * 	Overridden to return the extension of the filter.
	 * 	@see CustomizedFileFilter#getExtension()
	 */
	public String getExtension() { return ""; }
	
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
