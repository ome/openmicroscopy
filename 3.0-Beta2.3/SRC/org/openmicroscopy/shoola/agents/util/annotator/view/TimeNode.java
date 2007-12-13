/*
 * org.openmicroscopy.shoola.agents.util.annotator.view.TimeNode 
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
import java.sql.Timestamp;
import javax.swing.tree.DefaultMutableTreeNode;

//Third-party libraries

//Application-internal dependencies

/** 
 * Node hosting the annotation time.
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
class TimeNode 
	extends DefaultMutableTreeNode
{
	
	/** The index in the annotation list. */
	private int index;
	
	/** The id of the experimenter who entered the annotation. */
	private long ownerID;
	
	/**
	 * Creates a new instance.
	 *
	 * @param ownerID	The id of the experimenter who entered the 
	 * 					annotation.
	 * @param index		The index in the annotation list.
	 * @param date		The timestamp.
	 */
	TimeNode(long ownerID, int index, Timestamp date)
	{
		super();
		setUserObject(date);
		this.index = index;
		this.ownerID = ownerID;
	}
	
	/**
	 * Returns the id of the experimenter.
	 * 
	 * @return See above.
	 */
	long getOwnerID() { return ownerID; }
	
	/**
	 * Returns the index.
	 * 
	 * @return See above.
	 */
	int getIndex() { return index; }
	
	/**
	 * Overridden to return a formatted date
	 * @see Object#toString()
	 */
	public String toString()
	{ 
		if (getUserObject() == null) return AnnotatorUtil.NEW_ANNOTATION;
		String s = getUserObject().toString();
		return s.substring(0, s.indexOf("."));//df.format((Timestamp) getUserObject()) ;
	}
}
