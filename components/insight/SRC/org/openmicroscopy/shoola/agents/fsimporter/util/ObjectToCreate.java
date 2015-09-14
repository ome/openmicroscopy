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
package org.openmicroscopy.shoola.agents.fsimporter.util;

import omero.IllegalArgumentException;
import omero.gateway.model.DataObject;
import omero.gateway.model.ExperimenterData;
import omero.gateway.model.GroupData;

/** 
 * Utility class to save object for import.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since Beta4.4
 */
public class ObjectToCreate 
{

	/** Where to create the group.*/
	private GroupData group;
	
	/** The data object to create.*/
	private DataObject child;
	
	/** The parent of the data object.*/
	private DataObject parent;
	
	/** The experimenter to create data for.*/
	private ExperimenterData experimenter;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param group The group where to create the object.
	 * @param child The data object to create. 
	 * @param parent The parent of the data object if any.
	 */
	public ObjectToCreate(GroupData group, DataObject child, DataObject parent,
			ExperimenterData experimenter)
	{
		if (group == null)
			throw new IllegalArgumentException("No group specified");
		if (child == null)
			throw new IllegalArgumentException("No object to create");
		this.group = group;
		this.child = child;
		this.parent = parent;
		this.experimenter = experimenter;
	}
	
	/**
	 * Returns the experimenter.
	 * 
	 * @return See above.
	 */
	public ExperimenterData getExperimenter() { return experimenter; }
	
	/**
	 * Returns the group where to create the object.
	 * 
	 * @return See above.
	 */
	public GroupData getGroup() { return group; }
	
	/**
	 * Returns the data object to create.
	 * 
	 * @return See above.
	 */
	public DataObject getChild() { return child; }
	
	/**
	 * Returns the parent of the data object or <code>null</code>.
	 * 
	 * @return See above.
	 */
	public DataObject getParent() { return parent; }
}
