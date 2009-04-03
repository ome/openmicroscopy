/*
 * org.openmicroscopy.shoola.agents.util.annotator.view.OwnerNode 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.util.annotator.view;



//Java imports
import javax.swing.tree.DefaultMutableTreeNode;

//Third-party libraries

//Application-internal dependencies
import pojos.ExperimenterData;

/** 
 * Utility class hosting information about the experimenter who annotates
 * the data object.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
class OwnerNode 	
	extends DefaultMutableTreeNode
{
	
	/**
	 * Creates a new instance.
	 * 
	 * @param ho The original object. Cannot be <code>null</code>. 
	 */
	OwnerNode(ExperimenterData ho)
	{
		super();
		if (ho == null)
			throw new NullPointerException("No experimenter.");
		setUserObject(ho);
	}
	
	/**
	 * Returns the id of the experimenter.
	 * 
	 * @return See above.
	 */
	long getOwnerID()
	{
		return ((ExperimenterData) getUserObject()).getId();
	}
	
	/**
	 * Overridden to return the first and last name of the experimenter.
	 * @see Object#toString()
	 */
	public String toString()
	{ 
		ExperimenterData data = (ExperimenterData) getUserObject();
		String n = "Name not available"; //TODO: REMOVE ASAP
        try {
        	n = data.getFirstName()+" "+data.getLastName();
        } catch (Exception e) {}
		return n; 
	}
	
}
