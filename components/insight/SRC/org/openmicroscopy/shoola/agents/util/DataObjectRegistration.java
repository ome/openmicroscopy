/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2010 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.util;

import java.util.List;

import omero.gateway.model.AnnotationData;
import omero.gateway.model.DataObject;

/** 
 * Utility classes storing elements required to register and updates a file.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since 3.0-Beta4
 */
public class DataObjectRegistration 
{
	
	/** The collection of annotations to add to the element when registered. */
	private List<AnnotationData> toAdd;
	
	/** 
	 * The collection of annotations to remove from the element 
	 * when unregistered. 
	 */
	private List<Object> toRemove;
	
	/**  The collection of annotations to delete when unregistered. */
	private List<AnnotationData> toDelete;
	
	/** The collection of metadata to save. */
	private List<Object> metadata;
	
	/** The object to register. */
	private DataObject data;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param toAdd     The collection of annotations to add to the element when 
	 * 					registered.
	 * @param toRemove	The collection of annotations to remove from the element 
	 * 					when unregistered. 
	 * @param toDelete	The collection of annotations to delete when 
	 * 					unregistered.
	 * @param metadata  The metadata to save.
	 * @param data		The object to register or un-register.
	 */
	public DataObjectRegistration(List<AnnotationData> toAdd, 
			List<Object> toRemove, List<AnnotationData> toDelete,
			List<Object> metadata, DataObject data)
	{
		this.toAdd = toAdd;
		this.toRemove = toRemove;
		this.toDelete = toDelete;
		this.metadata = metadata;
		this.data = data;
	}

	/**
	 * Returns the collection of annotations to add to the element when 
	 * 					registered.
	 * 
	 * @return See above
	 */
	public List<AnnotationData> getToAdd() { return toAdd; }

	/**
	 * Returns the collection of annotations to remove from the element 
	 * when unregistered. 
	 * 
	 * @return See above
	 */
	public List<Object> getToRemove() { return toRemove; }

	/**
	 * Returns the collection of annotations to delete when unregistered.
	 * 
	 * @return See above
	 */
	public List<AnnotationData> getToDelete() { return toDelete; }

	/**
	 * Returns the metadata to save.
	 * 
	 * @return See above
	 */
	public List<Object> getMetadata() { return metadata; }

	/**
	 * Returns the object to register or un-register.
	 * 
	 * @return See above
	 */
	public DataObject getData() { return data; }
	
}
