/*
 * org.openmicroscopy.shoola.agents.events.editor.EditFileEvent 
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
package org.openmicroscopy.shoola.agents.events.editor;


//Java imports

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.event.RequestEvent;

/** 
 * Request to edit the specified <code>file</code>.
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
public class EditFileEvent
	extends RequestEvent
{

	/** The name of the file to edit. */
	private String  fileName;
	
	/** The id of the file to edit. */
	private long	fileID;
	
	/** The size of the file to edit. */
	private long 	fileSize;
	
	/** 
	 * Flag indicating that we only have the id of the file is the 
	 * Id of the annotation.
	 */
	private boolean	annotationData;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param fileName	The name of the file to edit.
	 * @param fileID	The id of the file to edit.
	 * @param fileSize	The size of the file to edit.
	 */
	public EditFileEvent(String fileName, long fileID, long fileSize)
	{
		this.fileName = fileName;
		this.fileID = fileID;
		this.fileSize = fileSize;
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param fileID The id of the file to edit.
	 */
	public EditFileEvent(long fileID)
	{
		this.fileID = fileID;
		annotationData = true;
	}
	
	/**
	 * Returns <code>true</code> if the event for the annotation,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	public boolean isAnnotationData() { return annotationData; }
	
	/**
	 * Returns the id of the file to edit.
	 * 
	 * @return See above.
	 */
	public long getFileID() { return fileID; }
	
	/**
	 * Returns the name of the file to edit.
	 * 
	 * @return See above.
	 */
	public String getFileName() { return fileName; }
	
	/**
	 * Returns the size of the file to edit.
	 * 
	 * @return See above.
	 */
	public long getFileSize() { return fileSize; }
	
} 
