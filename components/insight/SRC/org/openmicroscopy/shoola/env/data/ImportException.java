/*
 * org.openmicroscopy.shoola.env.data.ImportException 
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
package org.openmicroscopy.shoola.env.data;



//Java imports
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import ome.conditions.ResourceError;

//Third-party libraries
import loci.formats.FormatException;
import loci.formats.UnsupportedCompressionException;

//Application-internal dependencies

/** 
 * Reports an error occurred while importing an image.
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
public class ImportException 
	extends Exception
{
	
	/** Indicates that the compression is not supported.*/
	public static int COMPRESSION = 0;
	
	/** Indicates that a library is missing.*/
	public static int MISSING_LIBRARY = 1;
	
	/** Indicates that a library is missing.*/
	public static int FILE_ON_TAPE = 2;
	
	/** Indicates that there is no space left.*/
	public static int NO_SPACE = 3;

	/** Indicates that there was a checksum mismatch.*/
	public static int CHECKSUM_MISMATCH = 4;

	/** The status associated to the exception.*/
	private int status;
	
	/**
	 * Returns the message corresponding to the error thrown while importing the
	 * files.
	 * 
	 * @param t The exception to handle.
	 * @return See above.
	 */
	public static String getImportFailureMessage(Throwable t)
	{
		String message;
		Throwable cause = t.getCause();
		if (cause instanceof FormatException) {
			message = cause.getMessage();
			cause.printStackTrace();
			if (message == null) return null;
			if (message.contains("ome-xml.jar"))
				return "Missing ome-xml.jar required to read OME-TIFF files";
			String[] s = message.split(":");
			if (s.length > 0) return s[0];
		} else if (cause instanceof IOException) {
			
		} else if (cause instanceof omero.ChecksumValidationException) {
			return ((omero.ChecksumValidationException) cause).getMessage();
		}
		return null;
	}
	
	/**
	 * Constructs a new exception with the specified detail message.
	 * 
	 * @param message		Short explanation of the problem.
	 * @param readerType 	The type of reader used while trying to import an 
	 * 						image.
	 */
	public ImportException(String message)
	{
		this(message, null);
	}
	
	/**
	 * Constructs a new exception with the specified detail message and cause.
	 * 
	 * @param message		Short explanation of the problem.
	 * @param cause			The exception that caused this one to be risen.
	 * @param readerType 	The type of reader used while trying to import an 
	 * 						image.
	 */
	public ImportException(Throwable cause) 
	{
		this((String) getImportFailureMessage(cause), cause);
	}
	
	/**
	 * Constructs a new exception with the specified detail message and cause.
	 * 
	 * @param message		Short explanation of the problem.
	 * @param cause			The exception that caused this one to be risen.
	 * @param readerType 	The type of reader used while trying to import an 
	 * 						image.
	 */
	public ImportException(String message, Throwable cause) 
	{
		super(message, cause);
		status = -1;
	}
	
	/**
	 * Returns one of the constant defined by this class.
	 * 
	 * @return See above.
	 */
	public int getStatus()
	{
		Throwable cause = getCause();
		if (cause == null) return status;
		if (cause instanceof UnsupportedCompressionException) {
			return COMPRESSION;
		} else if (cause instanceof FormatException) {
			String message = cause.getMessage();
			if (message.contains("missing libary"))
				return MISSING_LIBRARY;
		} else if (cause instanceof IOException) {
			String message = cause.getMessage();
			if (message.contains(
					"The specified network name is no longer available"))
				return FILE_ON_TAPE;
		} else if (cause.getCause() instanceof IOException) {
			String message = cause.getCause().getMessage();
			if (message.contains(
					"The specified network name is no longer available"))
				return FILE_ON_TAPE;
		} else if (cause instanceof ResourceError) {
			String message = cause.getMessage();
			if (message.contains("No space left on device"))
				return NO_SPACE;
		} else if (cause instanceof omero.ChecksumValidationException) {
			return CHECKSUM_MISMATCH;
		}
		return status;
	}
	
	/**
	 * Overridden to return the cause of the problem.
	 * @see Exception#toString()
	 */
	public String toString()
	{
		Throwable cause = getCause();
		if (cause != null) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			cause.printStackTrace(pw);
			return sw.toString();
		}
		return super.toString();
	}
	
}
