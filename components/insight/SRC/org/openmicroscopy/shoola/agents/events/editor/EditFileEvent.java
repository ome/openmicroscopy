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
import pojos.FileAnnotationData;

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

	
	/** The annotation to edit. */
	private FileAnnotationData fileAnnotation;
	
	/** The id of the annotation to edit. */
	private long			   fileAnnotationID;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param fileAnnotation	The annotation hosting the information about
	 * 							the file to edit.
	 */
	public EditFileEvent(FileAnnotationData fileAnnotation)
	{
		if (fileAnnotation == null)
			throw new IllegalArgumentException("No file annotation.");
		this.fileAnnotation = fileAnnotation;
	}

	/**
	 * Creates a new instance.
	 * 
	 * @param fileAnnotationID	The id of the annotation to edit.
	 */
	public EditFileEvent(long fileAnnotationID)
	{
		this.fileAnnotationID = fileAnnotationID;
	}
	
	/**
	 * Returns the annotation hosting the information about the file to edit.
	 * 
	 * @return See above.
	 */
	public FileAnnotationData getFileAnnotation() { return fileAnnotation; }

	/**
	 * Returns the id of the file annotation to edit.
	 * 
	 * @return See above.
	 */
	public long getFileAnnotationID() { return fileAnnotationID; }
	
} 
