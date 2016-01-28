/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2012 University of Dundee & Open Microscopy Environment.
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
package org.openmicroscopy.shoola.env.data.model;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.Icon;

import omero.gateway.SecurityContext;

/** 
 * Parameters required to move a collection of objects between groups.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since Beta4.4
 */
public class TransferableActivityParam
{

	/** The icon associated to the parameters. */
    private Icon icon;
    
    /** The icon associated to the parameters in case of failure. */
    private Icon failureIcon;
    
    /** The object to transfer.*/
    private TransferableObject object;
    
    /**
     * Creates a new instance.
     * 
     * @param icon The icon associated to the parameters.
     * @param object The collection of objects to transfer.
     */
    public TransferableActivityParam(Icon icon, TransferableObject object)
    {
    	if (object == null)
    		throw new IllegalArgumentException("No Objects to transfer.");
    	this.icon = icon;
    	this.object = object;
    }
    
    /**
     * Sets the failure icon.
     * 
     * @param failureIcon The icon to set.
     */
    public void setFailureIcon(Icon failureIcon)
    { 
    	this.failureIcon = failureIcon;
    }
    
    /**
     * Returns the failure icon.
     * 
     * @return See above.
     */
    public Icon getFailureIcon() { return failureIcon; }
    
    /**
	 * Returns the icon if set or <code>null</code>.
	 * 
	 * @return See above.
	 */
	public Icon getIcon() { return icon; }
	
	/**
	 * Returns the collection of objects to transfer.
	 * 
	 * @return See above.
	 */
	public TransferableObject getObject() { return object; }

	/**
	 * Returns the number of objects to transfer.
	 * 
	 * @return See above.
	 */
	public int getNumber()
	{
		int number = 0;
		Map<SecurityContext, List<omero.gateway.model.DataObject>> map = object.getSource();
		Iterator<List<omero.gateway.model.DataObject>> i = map.values().iterator();
		while (i.hasNext()) {
			number += i.next().size();
		}
		return number;
	}
	
	/**
	 * Returns the name of the group.
	 * 
	 * @return See above.
	 */
	public String getGroupName() { return object.getGroupName(); }

}
