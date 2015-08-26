/*
 * org.openmicroscopy.shoola.agents.util.dnd.ObjectToTransfer 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2011 University of Dundee & Open Microscopy Environment.
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
package org.openmicroscopy.shoola.agents.util.dnd;


import java.util.List;

import org.openmicroscopy.shoola.agents.util.browser.TreeImageDisplay;

/** 
 * The D&D objects to transfer and the target.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since Beta4.4
 */
public class ObjectToTransfer
{

	/** The nodes to transfer.*/
	private List<TreeImageDisplay> nodes;
	
	/** The target node.*/
	private TreeImageDisplay target;
	
	/** The drop action.*/
	private int dropAction;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param target The node where to add the
	 * @param nodes  The nodes to transfer.
	 * @param dropAction The value to set. One of the constants defined by 
	 * <code>java.awt.dnd.DnDConstants</code>.
	 */
	public ObjectToTransfer(TreeImageDisplay target,
			List<TreeImageDisplay> nodes, int dropAction)
	{
		this.target = target;
		this.nodes = nodes;
		this.dropAction = dropAction;
	}

	/**
	 * Returns the drop action.
	 * 
	 * @return See above.
	 */
	public int getDropAction() { return dropAction; }
	
	/**
	 * Returns the target.
	 * 
	 * @return See above.
	 */
	public TreeImageDisplay getTarget() { return target; }
	
	/**
	 * Returns the collection of nodes to transfer.
	 * 
	 * @return See above.
	 */
	public List<TreeImageDisplay> getNodes() { return nodes; }
	
}
