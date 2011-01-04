/*
 * org.openmicroscopy.shoola.agents.events.importer.LoadImporter 
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
package org.openmicroscopy.shoola.agents.events.importer;


//Java imports

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.event.RequestEvent;
import pojos.DataObject;


/** 
 * Event to bring the Importer's chooser.
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
public class LoadImporter 
	extends RequestEvent
{

	/** Indicates that the type is for project. */
	public static final int		PROJECT_TYPE = 0;
	
	/** Indicates that the type is for Screening data. */
	public static final int		SCREEN_TYPE = 1;
	
	/** The container where to load the image. */
	private DataObject container;
	
	/** The type of the import to handle. */
	private int type;
	
	/** 
	 * Creates a new instance.
	 * 
	 * @param type The type of importer.
	 */
	public LoadImporter(int type)
	{
		this.type = type;
	}
	
	/**
	 *  Creates a new instance. 
	 * 
	 * @param container The container where import the image.
	 */
	public LoadImporter(DataObject container)
	{
		this.container = container;
		type = -1;
	}
	
	/**
	 * Returns the container.
	 * 
	 * @return See above.
	 */
	public DataObject getContainer() { return container; }
	
	/**
	 * Returns the type of import.
	 * 
	 * @return See above.
	 */
	public int getType() { return type; }

}
