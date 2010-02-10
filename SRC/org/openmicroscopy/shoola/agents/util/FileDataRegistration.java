/*
 * org.openmicroscopy.shoola.agents.util.FileDataRegistration 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2010 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.util;

import java.util.List;

import pojos.AnnotationData;
import pojos.DataObject;

//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * 
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
public class FileDataRegistration 
{
	private List<AnnotationData> toAdd;
	private List<AnnotationData> toRemove;
	private List<AnnotationData> toDelete;
	private List<Object> metadata;
	private DataObject data;
	
	public FileDataRegistration(List<AnnotationData> toAdd, 
				List<AnnotationData> toRemove, List<AnnotationData> toDelete,
				List<Object> metadata,
				DataObject data)
	{
		this.toAdd = toAdd;
		this.toRemove = toRemove;
		this.toDelete = toDelete;
		this.metadata = metadata;
		this.data = data;
	}

	public List<AnnotationData> getToAdd() {
		return toAdd;
	}

	public List<AnnotationData> getToRemove() {
		return toRemove;
	}

	public List<AnnotationData> getToDelete() {
		return toDelete;
	}

	public List<Object> getMetadata() {
		return metadata;
	}

	public DataObject getData() {
		return data;
	}
	
}
