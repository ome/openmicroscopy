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
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.agents.events.treeviewer;

import java.util.List;

import org.openmicroscopy.shoola.env.event.RequestEvent;
import omero.gateway.model.DataObject;

/** 
 * Request to indicate the <code>DataObject</code>s to delete.
 *
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since 3.0-Beta4
 */
public class DeleteObjectEvent 
	extends RequestEvent
{

	/** The objects to delete. */
	private List<DataObject> objects;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param objects The objects to delete.
	 */
	public DeleteObjectEvent(List<DataObject> objects)
	{
		this.objects = objects;
	}
	
	/**
	 * Returns the objects to delete.
	 * 
	 * @return See above.
	 */
	public List<DataObject> getObjects() { return objects; }
	
}
