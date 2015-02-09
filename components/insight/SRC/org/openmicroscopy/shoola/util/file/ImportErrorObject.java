/*
 * org.openmicroscopy.shoola.util.file.ImportErrorObject 
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
package org.openmicroscopy.shoola.util.file;


//Java imports
import java.io.File;


//Third-party libraries

//Application-internal dependencies

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
	private Exception exception;
	
	/** The files associated to the file that failed to import. */
	private String[] usedFiles;
	
	/** The type of reader used. */
	private String readerType;
	
	/** The id of the log file.*/
	private long logFileID;
	
	/** The group indicating the security context.*/
	private long groupID;
	
	/** Retrieve the log file from the annotation.*/
	private boolean retrieveFromAnnotation;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param file The file that could not be imported.
	 * @param exception The exception.
	 * @param groupID The id of the group.
	 */
	public ImportErrorObject(File file, Exception exception, long groupID)
	{
		this.file = file;
		this.exception = exception;
		this.groupID = groupID;
		retrieveFromAnnotation = false;
	}

	/** Sets the file to submit to <code>null</code>.*/
	public void resetFile() { file = null; }

	/**
	 * Sets to <code>true</code> if the log file needs to be retrieved
	 * from the annotation, <code>false</code> otherwise.
	 * If <code>true</code>, the {@link #logFileID} is the id of the 
	 * annotation.
	 * 
	 * @param retrieveFromAnnotation The value to set.
	 */
	public void setRetrieveFromAnnotation(boolean retrieveFromAnnotation)
	{
		this.retrieveFromAnnotation = retrieveFromAnnotation;
	}
	
	/**
	 * Returns <code>true</code> if the log file needs to be retrieved
	 * from the annotation, <code>false</code> otherwise.
	 * If <code>true</code>, the {@link #logFileID} is the id of the 
	 * annotation.
	 * 
	 * @return See above.
	 */
	public boolean isRetrieveFromAnnotation() { return retrieveFromAnnotation; }
	
	/**
	 * Returns the security context.
	 * 
	 * @return See above.
	 */
	public long getSecurityContext() { return groupID; }

	/**
	 * Sets the identifier of the log file.
	 * 
	 * @param logFileID The value to set.
	 */
	public void setLogFileID(long logFileID) { this.logFileID = logFileID; }

	/**
	 * Returns the id of the log file.
	 * 
	 * @return See above.
	 */
	public long getLogFileID() { return logFileID; }

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
	public Exception getException() { return exception; }
	
}
