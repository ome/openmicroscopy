/*
 * org.openmicroscopy.shoola.env.data.model.DeletableObject 
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
package org.openmicroscopy.shoola.env.data.model;


//Java imports
import java.util.List;

//Third-party libraries

//Application-internal dependencies

/** 
 * Hosts the parameters to delete. 
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
public class DeletableObject
{

	/** The data object to delete. */
	private DataObject	objectToDelete;
	
	/** 
	 * Flag indicating to delete the objects contained in the object to delete,
	 * the flag will be taken into account when the objec to delete is a 
	 * container e.g. <code>Project</code>.
	 */
	private boolean		content;
	
	/** Flag indicating to delete the annotations. */
	private boolean		attachment;
	
	/** 
	 * Sets to <code>null</code> or <code>empty</code>
	 * if all annotations have to be deleted,
	 * otherwise contains the types of annotation to delete.
	 */
	private List<Class> attachmentTypes;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param objectToDelete 	The object to delete.
	 * @param content			Pass <code>true</code> to delete the objects
	 * 							contained in the object to delete, 
	 * 							<code>false</code> otherwise.
	 * @param attachment		Pass <code>true</code> to delete the 
	 * 							annotations, <code>false</code> otherwise.
	 */
	public DeletableObject(DataObject objectToDelete, 
			boolean content, boolean attachment)
	{
		if (objectToDelete == null) 
			throw new IllegalArgumentException("No object to delete.");
		this.objectToDelete = objectToDelete;
		this.content = content;
		this.attachment = attachment;
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param objectToDelete The object to delete.
	 */
	public DeletableObject(DataObject objectToDelete)
	{
		this(objectToDelete, false, false);
	}
	
	/** 
	 * Returns <code>true</code> if the objects contained in the object to 
	 * delete have to be deleted, <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	public boolean deleteContent() { return content; }
	
	/** 
	 * Returns <code>true</code> if the related annotations have to be deleted,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	public boolean deleteAttachment() { return attachment; }
	
	/**
	 * Returns the types of attachments to delete or <code>null</code>
	 * if all types have to be deleted.
	 * 
	 * @return See above.
	 */
	public List<Class> getAttachmentTypes() { return attachmentTypes; }
	
	/**
	 * Sets the types of attachments to delete.
	 * 
	 * @param attachmentTypes The types of attachments to delete.
	 */
	public void setAttachmentTypes(List<Class> attachmentTypes)
	{
		this.attachmentTypes = attachmentTypes;
	}
	
	/**
	 * Returns the object to delete.
	 * 
	 * @return See above.
	 */
	public DataObject getObjectToDelete() { return objectToDelete; }
	
}
