/*
 * org.openmicroscopy.shoola.env.data.model.ImportErrorObject 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2011 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
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
package org.openmicroscopy.shoola.env.data.model;

//Java imports
import java.io.File;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.data.ImportException;

/** 
 * Object information about files that cannot be imported.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class ImportErrorObject
{

	/** The file that could not be imported. */
	private File file;
	
	/** The exception thrown during the import. */
	private ImportException exception;
	
	/** The files associated to the file that failed to import. */
	private String[] usedFiles;
	
	/** The type of reader used. */
	private String readerType;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param file The file that could not be imported.
	 * @param exception The exception.
	 */
	public ImportErrorObject(File file, ImportException exception)
	{
		this.file = file;
		this.exception = exception;
	}
	
	/**
	 * Sets the type of reader.
	 * 
	 * @param readerType The type of reader used.
	 */
	public void setReaderType(String readerType)
	{
		this.readerType = readerType;
	}
	
	/**
	 * Sets the files associated to the file that failed to import.
	 * 
	 * @param usedFiles The associated files.
	 */
	public void setUsedFiles(String[] usedFiles)
	{
		this.usedFiles = usedFiles;
	}
	
	/**
	 * Returns the type of reader used.
	 * 
	 * @return See above.
	 */
	public String getReaderType() { return readerType; }
	
	/**
	 * Returns the files associated to the file failing to import.
	 * 
	 * @return See above.
	 */
	public String[] getUsedFiles() { return usedFiles; }
	
	/**
	 * Returns the file that could not be imported.
	 * 
	 * @return See above.
	 */
	public File getFile() { return file; }
	
	/**
	 * Returns the exception thrown during the import.
	 * 
	 * @return See above.
	 */
	public ImportException getException() { return exception; }
	
	
}
