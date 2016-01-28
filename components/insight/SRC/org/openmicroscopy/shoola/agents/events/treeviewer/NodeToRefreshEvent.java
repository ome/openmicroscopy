/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2011 University of Dundee. All rights reserved.
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
 * Request to indicate the <code>DataObject</code>s that need to be refreshed.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since 3.0-Beta4
 */
public class NodeToRefreshEvent 
	extends RequestEvent
{

	/** The objects to delete. */
	private List<DataObject> objects;
	
	/** Flag indicating to mark the nodes to refresh or not. */
	private boolean refresh;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param objects The objects to mark.
	 * @param refresh Pass <code>true</code> to refresh the nodes, 
	 * 				  <code>false</code> otherwise.
	 */
	public NodeToRefreshEvent(List<DataObject> objects, boolean refresh)
	{
		this.objects = objects;
		this.refresh = refresh;
	}
	
	/**
	 * Returns the list of objects to refresh.
	 * 
	 * @return See above.
	 */
	public List<DataObject> getObjects() { return objects; }
	
	/**
	 * Returns <code>true</code> to refresh the nodes, 
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	public boolean getRefresh() { return refresh; }
	
}
