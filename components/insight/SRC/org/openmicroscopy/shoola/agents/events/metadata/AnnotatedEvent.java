/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2012 University of Dundee & Open Microscopy Environment.
 *  All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
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
package org.openmicroscopy.shoola.agents.events.metadata;

import java.util.List;

import org.openmicroscopy.shoola.env.event.RequestEvent;
import omero.gateway.model.DataObject;

/** 
 * Event indicating that the objects have been annotated.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since Beta4.4
 */
public class AnnotatedEvent 
	extends RequestEvent
{

	/** The data object annotated.*/
	private List<DataObject> data;
	
	/** Indicates the annotation added or removed.*/
	private int count;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param data The annotated object.
	 * @param count Pass <code>0</code> if no annotation, a positive value if
	 * annotations are added, negative value if annotations are removed.
	 */
	public AnnotatedEvent(List<DataObject> data, int count)
	{
		this.data = data;
		this.count = count;
	}
	
	/**
	 * Returns the annotated object.
	 * 
	 * @return See above.
	 */
	public List<DataObject> getData() { return data; }
	
	/**
	 * Returns a positive value if annotations are added, a negative value
	 * if annotations are removed.
	 * 
	 * @return See above.
	 */
	public int getCount() { return count; }
	
}
