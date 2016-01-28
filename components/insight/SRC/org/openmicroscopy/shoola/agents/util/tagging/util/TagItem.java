/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
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
package org.openmicroscopy.shoola.agents.util.tagging.util;

import javax.swing.JMenuItem;

import omero.gateway.model.DataObject;
import omero.gateway.model.TagAnnotationData;

/** 
 *  Utility class hosting a tag.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since OME3.0
 */
public class TagItem 
	extends JMenuItem
{
	
	/** The category hosted by the component. */
	private DataObject 	data;
	
	/** Flag indicating if the tag is available or not. */
	private boolean		available;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param data	The category hosting by this node.
	 */
	public TagItem(DataObject data)
	{
		if (data == null)
			throw new IllegalArgumentException("No tag specified.");
		
		this.data = data;
		setText(getObjectName());
		setToolTipText(getObjectDescription());
		setAvailable(true);
	}
	
	/**
	 * Returns <code>true</code> if the tag is available, 
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	public boolean isAvailable() { return available; }
	
	/**
	 * Sets to <code>true</code> if the tag is available, 
	 * <code>false</code> otherwise.
	 * 
	 * @param available
	 */
	public void setAvailable(boolean available) { this.available = available; }
	
	/**
	 * Returns the object hosted by this component.
	 * 
	 * @return See above.
	 */
	public DataObject getDataObject() { return data; }
	
	/**
	 * Sets the description of the data object.
	 * 
	 * @param des The value to set.
	 */
	public void setObjectDescription(String des)
	{ 
		
	}
	
	/**
	 * Sets the name of the data object.
	 * 
	 * @param name The value to set.
	 */
	public void setObjectName(String name)
	{ 
		if (data instanceof TagAnnotationData)
			((TagAnnotationData) data).setTagValue(name);
	}
	
	/**
	 * Returns the description of the data object.
	 * 
	 * @return See above.
	 */
	public String getObjectDescription()
	{ 
		return null;
	}
	
	/**
	 * Returns the name of the data object.
	 * 
	 * @return See above.
	 */
	public String getObjectName()
	{ 
		if (data instanceof TagAnnotationData)
			return ((TagAnnotationData) data).getTagValue();
		return null;
	}
	
	/**
	 * Returns the id of the category owner.
	 * 
	 * @return See above.
	 */
	public long getOwnerID() { return data.getOwner().getId(); }
	
	/**
	 * Returns the id of the object hosted by this component.
	 * 
	 * @return See above.
	 */
	public long getObjectID() { return data.getId(); }
	
	/**
	 * Overridden to return the name of the data object.
	 * @see Object#toString()
	 */
	public String toString() { return getObjectName(); }

}
