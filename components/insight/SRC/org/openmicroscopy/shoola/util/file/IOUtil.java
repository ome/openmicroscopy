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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
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
	 * @throws IOException If we cannot read the file or create a stream.
	 */
	public static InputStream readFile(String fileName)
		throws IOException
	{
		if (fileName == null)
			throw new IllegalArgumentException("No file name specified.");
		File f = new File(fileName);
		FileInputStream input = null;
		try {
			input = new FileInputStream(f);
			return new BufferedInputStream(input);
		} catch (Exception e) {
			if (input != null) input.close();
			throw new IOException("Cannot read the file "+fileName+". " +
					"Error: "+e.getMessage());
		}
	}
	
	/**
	 * Writes the file corresponding to the passed file name. Returns
	 * the outputStream or <code>null</code> if the file cannot be created.
	 * 
	 * @param fileName The name of the file to write.
	 * @return See above.
	 * @throws IOException If we cannot write the file or create a stream.
	 */
	public static OutputStream writeFile(String fileName)
		throws IOException
	{
		if (fileName == null)
			throw new IllegalArgumentException("No file name specified.");
		File f = new File(fileName);
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(f);
			return new BufferedOutputStream(out);
		} catch (Exception e) {
			if (out != null) out.close();
			throw new IOException("Cannot write the file "+fileName+". " +
					"Error: "+e.getMessage());
		}
	}
	
	/**
	 * Reads the contents of the passed text file.
	 * 
	 * @param file The file to handle.
	 * @return See above.
	 * @throws IOException If we cannot write the file or create a stream.
	 */
	public static String readTextFile(File file)
		throws IOException
	{
		StringBuffer contents = new StringBuffer();
		BufferedReader input = new BufferedReader(new FileReader(file));
		try {
			String line = null;
			while ((line = input.readLine()) != null) {
				contents.append(line);
				contents.append(System.getProperty("line.separator"));
			}
		} finally {
			input.close();
		}
		return contents.toString();
	}
	
	/**
	 * Reads the contents of the passed text file.
	 * 
	 * @param fileName The name of the file.
	 * @return See above.
	 * @throws IOException If we cannot write the file or create a stream.
	 */
	public static String readTextFile(String fileName)
		throws IOException
	{
		if (fileName == null)
			throw new IllegalArgumentException("No file name specified.");
		return readTextFile(new File(fileName));
	}
	
}
