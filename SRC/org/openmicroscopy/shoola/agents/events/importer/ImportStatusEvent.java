/*
 * org.openmicroscopy.shoola.agents.events.importer.ImportStatusEvent 
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
import java.util.List;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.event.RequestEvent;
import pojos.DataObject;

/** 
 * Event indicating if there are on-going imports.
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
public class ImportStatusEvent 
	extends RequestEvent
{

	/** Flag indicating if there are on-going import. */
	private boolean importing;
	
	/** The collection of containers that will have to be refreshed. */
	private List<DataObject> containers;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param importing Pass <code>true</code> indicating of on-going imports,
	 * 					<code>false</code> otherwise.
	 */
	public ImportStatusEvent(boolean importing)
	{
		this.importing = importing;
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param importing Pass <code>true</code> indicating of on-going imports,
	 * 					<code>false</code> otherwise.
	 * @param containers The containers to refresh.
	 */
	public ImportStatusEvent(boolean importing, List<DataObject> containers)
	{
		this.importing = importing;
	}
	
	/**
	 * Sets the containers that will have to be refreshed.
	 * 
	 * @param containers The containers to refresh.
	 */
	public void setContainers(List<DataObject> containers)
	{
		this.containers = containers;
	}
	
	/**
	 * Returns the containers that will have to be refreshed.
	 * 
	 * @return See above.
	 */
	public List<DataObject> getContainers() { return containers; }
	
	/**
	 * Returns <code>true</code> if on-going imports, 
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	public boolean isImporting() { return importing; }
	
}
