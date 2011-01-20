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

import java.io.PrintWriter;
import java.io.StringWriter;

//Java imports

//Third-party libraries

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

	/** The type of reader used while trying to import an image. */
	private String readerType;
	
	/**
	 * Constructs a new exception with the specified detail message.
	 * 
	 * @param message		Short explanation of the problem.
	 * @param readerType 	The type of reader used while trying to import an 
	 * 						image.
	 */
	public ImportException(String message, String readerType)
	{
		super(message);
		this.readerType = readerType;
	}
	
	/**
	 * Constructs a new exception with the specified detail message and cause.
	 * 
	 * @param message		Short explanation of the problem.
	 * @param cause			The exception that caused this one to be risen.
	 * @param readerType 	The type of reader used while trying to import an 
	 * 						image.
	 */
	public ImportException(String message, Throwable cause, String readerType) 
	{
		super(message, cause);
		this.readerType = readerType;
	}
	
	/**
	 * Returns the type of reader used while trying to import an image.
	 * 
	 * @return See above
	 */
	public String getReaderType() { return readerType; }
	
	/**
	 * Overridden to return the cause of the problem
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
