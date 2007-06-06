/*
 * org.openmicroscopy.shoola.util.file.IOUtil 
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
package org.openmicroscopy.shoola.util.file;


//Java imports
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

//Third-party libraries

//Application-internal dependencies

/** 
 * Collection of static methods to read and write files.
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
public class IOUtil
{

	/**
	 * Reads the file corresponding to the passed file name. Returns
	 * the inputstream or <code>null</code> if the file doesn't exist.
	 * 
	 * @param fileName The name of the file to read.
	 * @return See above.
	 */
	public static InputStream readFile(String fileName)
	{
		if (fileName == null)
			throw new IllegalArgumentException("No file name specified.");
		File f = new File(fileName);
		BufferedInputStream in;
		try {
			in = new BufferedInputStream(new FileInputStream(f));
		} catch (Exception e) {
			return null;
		}
		return in;
	}
	
	/**
	 * Writes the file corresponding to the passed file name. Returns
	 * the outputStream or <code>null</code> if the file cannot be created.
	 * 
	 * @param fileName The name of the file to write.
	 * @return See above.
	 */
	public static OutputStream writeFile(String fileName)
	{
		if (fileName == null)
			throw new IllegalArgumentException("No file name specified.");
		File f = new File(fileName);
		BufferedOutputStream out;
		try {
			out = new BufferedOutputStream(new FileOutputStream(f));
		} catch (Exception e) {
			return null;
		}
		return out;
	}
	
}
