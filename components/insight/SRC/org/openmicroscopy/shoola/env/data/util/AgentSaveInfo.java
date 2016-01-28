/*
 * org.openmicroscopy.shoola.env.data.util.AgentSaveInfo 
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
package org.openmicroscopy.shoola.env.data.util;


import java.util.List;

/** 
 * Holds information about instances of a given agent to save.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since 3.0-Beta4
 */
public class AgentSaveInfo
{

	/** The name associated to the agent. */
	private String 	name;
	
	/** The instances to save. */
	private List<Object> instances;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param name  The name associated to the agent.
	 * @param instances The instances to save.
	 */
	public AgentSaveInfo(String name, List<Object> instances)
	{
		this.instances = instances;
		this.name = name;
	}
	
	/**
	 * Returns the name associated to the agent.
	 * 
	 * @return See above.
	 */
	public String getName() { return name; }
	
	/**
	 * Returns the number of instances to save.
	 * 
	 * @return See above.
	 */
	public int getCount()
	{ 
		if (instances == null) return 0;
		return instances.size(); 
	}
	
	/**
	 * Returns the instances to save.
	 * 
	 * @return See above.
	 */
	public List<Object> getInstances() { return instances; }
	
}
