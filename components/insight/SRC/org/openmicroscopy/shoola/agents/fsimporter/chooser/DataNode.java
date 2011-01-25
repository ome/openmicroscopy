/*
 * org.openmicroscopy.shoola.agents.fsimporter.chooser.DataNode 
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
package org.openmicroscopy.shoola.agents.fsimporter.chooser;


//Java imports

//Third-party libraries

//Application-internal dependencies
import omero.IllegalArgumentException;
import pojos.DatasetData;

/** 
 * Hosts the dataset for display.
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
class DataNode
{

	/** The dataset to host. */
	private DatasetData dataset;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param dataset The dataset to host.
	 */
	DataNode(DatasetData dataset)
	{
		if (dataset == null)
			throw new IllegalArgumentException("No Dataset specified.");
		this.dataset = dataset;
	}
	
	/**
	 * Returns the dataset.
	 * 
	 * @return See above.
	 */
	DatasetData getDataset() { return dataset; }
	
	/**
	 * Returns <code>true</code> if the dataset corresponding to the passed 
	 * name is new, <code>false</code> otherwise.
	 * 
	 * @param name The name to handle.
	 * @return See above.
	 */
	boolean isNewDataset(String name)
	{
		return (dataset.getName().equals(name) && dataset.getId() <= 0);
	}
	
	/**
	 * Overridden to set the name of the dataset.
	 * @see #toString()
	 */
	public String toString() { return dataset.getName(); }
	
}
