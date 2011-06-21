/*
 * org.openmicroscopy.shoola.env.data.model.TableParameters 
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
package org.openmicroscopy.shoola.env.data.model;


//Java imports
import java.util.List;

//Third-party libraries

//Application-internal dependencies

/** 
 * Hosts parameters required to load the tabular data.
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
public class TableParameters
{

	/** The root node type. */
	private Class nodeType;
	
	/** The identifier of the node. */
	private long nodeID;
		
	/** The identifier of the original file. */
	private List<Long> originalFileIDs;

	/** 
	 * Creates a new instance. 
	 * 
	 * @param originalFileIDs The collection of files to load.
	 */
	public TableParameters(List<Long> originalFileIDs)
	{
		this.originalFileIDs = originalFileIDs;
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param nodeType The type of node to load.
	 * @param nodeID   The identifier of the node.
	 */
	public TableParameters(Class nodeType, long nodeID)
	{
		this.nodeType = nodeType;
		this.nodeID = nodeID;
		originalFileIDs = null;
	}

	/**
	 * Returns the identifier of the original files.
	 * 
	 * @return See above.
	 */
	public List<Long> getOriginalFileIDs() { return originalFileIDs; }
	
	/**
	 * Returns the type of node.
	 * 
	 * @return See above.
	 */
	public Class getNodeType() { return nodeType; }
	
	/**
	 * Returns the identifier of node.
	 * 
	 * @return See above.
	 */
	public long getNodeID() { return nodeID; }
	
}
