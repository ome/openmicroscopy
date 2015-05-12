/*
 * org.openmicroscopy.shoola.agents.fsimporter.chooser.DataNodeElement 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2015 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.fsimporter.chooser;


import org.openmicroscopy.shoola.util.CommonsLangUtils;

import org.openmicroscopy.shoola.agents.util.browser.DataNode;

import pojos.DataObject;
import pojos.DatasetData;
import pojos.ScreenData;

/** 
 * Hosts location information displayed in queue.
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
class DataNodeElement 
{

	/** Text indicating that no container specified. */
	private static final String NO_LOCATION = "--";
	
	/** Host information about where to import the file. */
	private DataNode location;
	
	/** The name of the element. */
	private String name;

	/**
	 * Creates a new instance.
	 * 
	 * @param node The node to host.
	 * @param value The name of the directory.
	 */
	DataNodeElement(DataNode location, String value)
	{
		this.location = location;
		if (this.location != null && this.location.isNoDataset()) {
		    this.location = null;
		    value = null;
		}
		setName(value);
	}

	/**
	 * Sets the name.
	 * 
	 * @param value The value to handle.
	 */
	void setName(String value)
	{
	    name = "";
	    if (CommonsLangUtils.isBlank(value)) {
	        if (location == null) name = NO_LOCATION;
	        else {
	            if (location.getDataObject() instanceof ScreenData ||
	                    location.isDefaultScreen()) {
	                name += location.toString();
	            } else {
	                DataNode parent = location.getParent();
	                if (parent != null && !parent.isDefaultNode() 
	                        && !location.isDefaultNode())
	                    name = parent.toString()+"/";
	                name += location.toString();
	            }
	        }
	    } else {
	        if (location == null) name = value;
	        else {
	            DataNode parent = location.getParent();
	            if (parent != null && !parent.isDefaultNode())
	                name = parent.toString()+"/";
	            if (!location.isDefaultNode())
	                name += location.toString();
	            else name += value;
	        }
	    }
	}

	/**
	 * Returns the parent.
	 * 
	 * @return See above.
	 */
	DataObject getParent()
	{
		if (location == null) return null;
		DataNode parent = location.getParent();
		if (isHCSContainer()) {
			DataObject object = location.getDataObject();
			if (location.isDefaultNode()) return null;
			return object;
		}
		if (parent == null || parent.isDefaultNode()) 
			return null;//location.getDataObject();
		return parent.getDataObject();
	}
	
	/**
	 * Returns <code>true</code> of the container is a Screen,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	Boolean isHCSContainer()
	{
	    if (location == null) return null;
		Object object = location.getDataObject();
		if (object == null) {
			DatasetData d = getLocation();
			if (d != null) return false;
			return null;
		}
		return (object instanceof ScreenData);
	}
	/**
	 * Returns the location.
	 * 
	 * @return See above.
	 */
	DatasetData getLocation()
	{
		if (location == null) return null;
		if (location.isDefaultNode()) return null;
		if (location.getDataObject() instanceof DatasetData)
			return (DatasetData) location.getDataObject();
		return null;
	}
	
	/**
	 * Returns the node of reference if set.
	 * 
	 * @return See above. 
	 */
	Object getRefNode()
	{
		if (location == null) return null;
		return location.getRefNode();
	}
	
	/** 
	 * Overridden to return the name to give to the imported file.
	 * @see Object#toString()
	 */
	public String toString() { return name; }
	
}
