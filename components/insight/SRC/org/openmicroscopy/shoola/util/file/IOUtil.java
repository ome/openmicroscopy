/*
 * org.openmicroscopy.shoola.util.file.IOUtil 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2015 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
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
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FilenameUtils;
import org.openmicroscopy.shoola.util.CommonsLangUtils;

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
 * @since OME3.0
 */
public class IOUtil
{

	/** The extension used for the zip.*/
	public static final String ZIP_EXTENSION = ".zip";
	
	/** Filter for Microsoft Word documents. */
	private static final WordFilter WORD_FILTER;
	
	/** Filter for Microsoft Excel documents. */
	private static final ExcelFilter EXCEL_FILTER;
	
	/** Filter for PDF documents. */
	private static final PDFFilter PDF_FILTER;
	
	/** The class path.*/
	private static String CLASS_PATH_PROPERTY_NAME = "java.class.path";
	
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
		if (isJavaWebStart()) {
			// We're running under Java Web Start, read configuration file
			// from the CLASSPATH.
			return IOUtil.class.getClassLoader().getResourceAsStream(fileName);
		}
		// We're running normally, return as so.
		return new FileInputStream(fileName);
	}
	
	/**
	 * Returns <code>true</code> if the application is running under Java
	 * Web Start, <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	public static boolean isJavaWebStart()
	{
		return System.getProperty("javawebstart.version", null) != null;
	}

	/**
	 * Zips directory.
	 * 
	 * @param directory The directory to zip.
	 * @param out The output stream.
	 * @throws Exception Thrown if an error occurred during the operation.
	 */
	private static void zipDir(File directory, ZipOutputStream out,
	        String parentDirectoryName)
	        throws Exception
	        {
	    File[] entries = directory.listFiles();
	    byte[] buffer = new byte[4096]; // Create a buffer for copying
	    int bytesRead;
	    FileInputStream in = null;
	    File f;
	    for (int i = 0; i < entries.length; i++) {
	        try {
	            f = entries[i];
	            if (f.isHidden())
	                continue;
	            if (f.isDirectory()) {
	                zipDir(f, out, f.getName());
	                continue;
	            }
	            in = new FileInputStream(f); // Stream to read file
	            String zipName = f.getName();
	            if (!CommonsLangUtils.isEmpty(parentDirectoryName)) {
	                zipName = FilenameUtils.concat(parentDirectoryName, zipName);
	            }
	            out.putNextEntry(new ZipEntry(zipName)); // Store entry
	            while ((bytesRead = in.read(buffer)) != -1)
	                out.write(buffer, 0, bytesRead);
	           
            } catch (Exception e) {
                throw new Exception("Failure while creating zip.", e);
            } finally {
                if (out != null) out.closeEntry();
                if (in != null) in.close();
            }
	    }
	}

	/**
	 * Makes the zip.
	 * 
	 * @param zip The zip file.
	 * @param compress Pass <code>true</code> to compress,
	 * <code>false</code> otherwise.
	 */
	public static File zipDirectory(File zip, boolean compress)
	        throws Exception
	{
	    if (zip == null)
            throw new IllegalArgumentException("No name specified.");
        if (!zip.isDirectory() || !zip.exists())
            throw new IllegalArgumentException("Not a valid directory.");
        //Check if the name already has the extension
        String extension = FilenameUtils.getExtension(zip.getName());
        String name = zip.getName();
        if (CommonsLangUtils.isEmpty(extension) ||
                !ZIP_EXTENSION.equals("."+extension)) {
            name += ZIP_EXTENSION;
        }
        File file = new File(zip.getParentFile(), name);
        ZipOutputStream out = null;
        try {
            out = new ZipOutputStream(new FileOutputStream(file));
            if (!compress) out.setLevel(ZipOutputStream.STORED);
            zipDir(zip, out, null);
        } catch (Exception e) {
            throw new Exception("Cannot create the zip.", e);
        } finally {
            if (out != null) out.close();
        }
        return file;
	}
	
	/**
	 * Makes the zip.
	 * 
	 * @param zip The zip.
	 */
	public static File zipDirectory(File zip)
		throws Exception
	{
		return zipDirectory(zip, true);
	}
	
	/**
	 * Extracts the specified jar name from the class path.
	 * 
	 * @param name Value contained in the jar name.
	 * @return See above.
	 */
	public static Map<String, InputStream> extractJarFromPath(String name)
	throws Exception
	{
		Map<String, InputStream> values = new HashMap<String, InputStream>();
		if (name == null) return values;
		ClassLoader loader = IOUtil.class.getClassLoader();
		if (isJavaWebStart())
			loader = Thread.currentThread().getContextClassLoader();
        //Get the URLs
        URL[] urls = ((URLClassLoader) loader).getURLs();
		try {
			File f;
			String n;
			for (URL url : urls) {
				n = url.getFile();
				f = new File(n);
				if (f.getName().contains(name)) {
					readJar(values, f);
				}
			}
		} catch (Exception e) {
			throw new Exception("Cannot read the requested jar.", e);
		}
		return values;
	}
	
	/**
	 * Reads the specified jar file.
	 * 
	 * @param values The values to add the entries to.
	 * @param f The file to handle.
	 * @throws Exception
	 */
	private static void readJar(Map<String, InputStream> values, File f)
		throws Exception
	{
		ZipFile zfile;
		Enumeration<? extends ZipEntry> entries;
		ZipEntry entry;
		zfile = new ZipFile(f);
		entries = zfile.entries();
		while (entries.hasMoreElements()) {
			entry = entries.nextElement();
			if (!entry.isDirectory()) {
				values.put(entry.getName(), zfile.getInputStream(entry));
			}
		}
		zfile.close();
	}

}
