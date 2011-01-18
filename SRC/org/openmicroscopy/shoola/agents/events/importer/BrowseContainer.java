/*
 * org.openmicroscopy.shoola.agents.events.importer.BrowseContainer 
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
package org.openmicroscopy.shoola.agents.events.importer;


//Java imports

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.event.RequestEvent;

/** 
 * Browses the container where the images have been imported.
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
public class BrowseContainer 
	extends RequestEvent
{

	/** The data object to handle. */
	private Object data;
	
	/** The parent node if any specified. */
	private Object node;
	
	/**
	 * Creates the object to browse after import.
	 * 
	 * @param data The object to handle.
	 */
	public BrowseContainer(Object data)
	{
		this(data, null);
	}
	
	/**
	 * Creates the object to browse after import.
	 * 
	 * @param data The object to handle.
	 * @param node The node hosting the object to browse if any.
	 */
	public BrowseContainer(Object data, Object node)
	{
		if (data == null)
			throw new IllegalArgumentException("No data to browse.");
		this.data = data;
		this.node = node;
	}
	
	/**
	 * Returns the node hosting the object to browse if any.
	 * 
	 * @return See above.
	 */
	public Object getNode() { return node; }
	
	/**
	 * Returns the data object to browse.
	 * 
	 * @return See above.
	 */
	public Object getData() { return data; }
	
}
