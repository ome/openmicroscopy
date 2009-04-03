/*
 * org.openmicroscopy.shoola.agents.util.annotator.view.AnnotateNode 
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
import pojos.DataObject;
import pojos.DatasetData;
import pojos.ImageData;

/** 
 * 
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
class AnnotateNode 
	extends DefaultMutableTreeNode
{

	/**
	 * Creates a new instance.
	 * 
	 * @param ho The original object. Cannot be <code>null</code>. 
	 */
	AnnotateNode(Object ho)
	{
		super();
		setUserObject(ho);
	}
	
	/**
	 * Returns the Id of the <code>DataObject</code> hosted by this node, 
	 * or <code>-1</code> if the information is not available.
	 * 
	 * @return See above.
	 */
	long getUserObjectID()
	{
		Object uo = getUserObject();
		if (uo instanceof DataObject) 
			return ((DataObject) uo).getId();
		return -1;
	}
	
	/**
	 * Returns the full name of the object.
	 * 
	 * @return See above.
	 */
	String getObjectName() 
	{
		Object uo = getUserObject();
		if (uo instanceof ImageData) {
			return ((ImageData) uo).getName();
		} else if (uo instanceof DatasetData) {
			return ((DatasetData) uo).getName();
		}
		return uo.toString();
	}
	
	/**
	 * Overridden to return the first and last name of the experimenter.
	 * @see Object#toString()
	 */
	public String toString()
	{ 
		Object uo = getUserObject();
		if (uo instanceof ImageData) {
			return AnnotatorUtil.getPartialName(((ImageData) uo).getName());
		} else if (uo instanceof DatasetData) {
			return ((DatasetData) uo).getName();
		}
		return uo.toString();
	}
	
}
