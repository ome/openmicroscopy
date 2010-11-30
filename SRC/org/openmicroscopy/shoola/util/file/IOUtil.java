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
import java.util.Iterator;
import java.util.List;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


//Third-party libraries


//Application-internal dependencies
import org.openmicroscopy.shoola.util.filter.file.ExcelFilter;
import org.openmicroscopy.shoola.util.filter.file.PDFFilter;
import org.openmicroscopy.shoola.util.filter.file.WordFilter;

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

	/** Filter for Microsoft Word documents. */
	private static final WordFilter WORD_FILTER;
	
	/** Filter for Microsoft Excel documents. */
	private static final ExcelFilter EXCEL_FILTER;
	
	/** Filter for PDF documents. */
	private static final PDFFilter PDF_FILTER;
	
	
	static {
		WORD_FILTER = new WordFilter();
		PDF_FILTER = new PDFFilter();
		EXCEL_FILTER = new ExcelFilter();
	}
	
	/**
	 * Reads the content of the passed Microsoft Word file.
	 * 
	 * @param file The file to handle.
	 * @return See above.
	 */
	private static String readWordFile(File file)
		throws Exception
	{
		return null;
	}
	
	/**
	 * Reads the content of the passed Microsoft Excel file.
	 * 
	 * @param file The file to handle.
	 * @return See above.
	 */
	private static String readExcelFile(File file)
		throws Exception
	{
		return null;
	}
	
	/**
	 * Reads the content of the passed PDF file.
	 * 
	 * @param file The file to handle.
	 * @return See above.
	 */
	private static String readPDFFile(File file)
		throws Exception
	{
		return null;
	}
	
	/**
	 * Reads the file corresponding to the passed file name. Returns
	 * the input stream or <code>null</code> if the file doesn't exist.
	 * 
	 * @param fileName The name of the file to read.
	 * @return See above.
	 * @throws IOException If we cannot read the file or create a stream.
	 */
	public static InputStream readFileAsInputStream(String fileName)
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
	
	/**
	 * Reads the contents of the passed file.
	 * 
	 * @param file The file to handle.
	 * @return See above.
	 */
	public static String readFile(File file)
		throws Exception
	{
		if (file == null)
			throw new IllegalArgumentException("No file name specified.");
		if (WORD_FILTER.accept(file)) return readWordFile(file);
		else if (EXCEL_FILTER.accept(file)) return readExcelFile(file);
		else if (PDF_FILTER.accept(file)) return readPDFFile(file);
		return null;
	}
	
	/**
	 * Reads the contents of the passed text file.
	 * 
	 * @param fileName The name of the file.
	 * @return See above.
	 * @throws IOException If we cannot write the file or create a stream.
	 */
	public static String readFile(String fileName)
		throws Exception
	{
		if (fileName == null || fileName.trim().length() == 0)
			throw new IllegalArgumentException("No file name specified.");
		return readFile(new File(fileName));
	}
	
	/**
	 * Returns an input stream for the contents of the passed configuration
	 * file path. If we're running under Java Web Start, this input stream is
	 * loaded from the CLASSPATH otherwise it is loaded directly.
	 * 
	 * @param fileName The name of the file.
	 * @return See above.
	 * @throws IOException If we cannot create the input stream.
	 */
	public static InputStream readConfigFile(String fileName)
		throws IOException
	{
		if (System.getProperty("javawebstart.version", null) != null) {
			// We're running under Java Web Start, read configuration file
			// from the CLASSPATH.
			return IOUtil.class.getClassLoader().getResourceAsStream(
					new java.io.File(fileName).getName());
		}
		// We're running normally, return as so.
		return new FileInputStream(fileName);
	}
	
	/**
	 * Makes the zip.
	 * 
	 * @param zipName The name of the zip.
	 * @param files   The files to add.
	 */
	public static File zipDirectory(File zip)
		throws Exception
	{
		if (zip == null)
			throw new IllegalArgumentException("No name specified.");
		if (!zip.isDirectory())
			throw new IllegalArgumentException("Not a directory.");
		File[] entries = zip.listFiles();
	    byte[] buffer = new byte[4096]; // Create a buffer for copying
	    int bytesRead;

	    String name = zip.getName()+".zip";
	    File f;
		try {
			f = new File(zip.getParentFile(), name);
			ZipOutputStream out = new ZipOutputStream(
					new FileOutputStream(f));

		    FileInputStream in;
		    
		    for (int i = 0; i < entries.length; i++) {
		    	f = entries[i];
		    	if (f.isDirectory())
		    		continue;//Ignore directory TODO
		    	in = new FileInputStream(f); // Stream to read file
		    	out.putNextEntry(new ZipEntry(f.getName())); // Store entry
		    	while ((bytesRead = in.read(buffer)) != -1)
		    		out.write(buffer, 0, bytesRead);
		    	out.closeEntry();
		    	in.close(); 
		    }
		    out.close();
			return zip;
		} catch (Exception e) {
			throw new Exception("Cannot create the zip.", e);
		}
	}
	
}
