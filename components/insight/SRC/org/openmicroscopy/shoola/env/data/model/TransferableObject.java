/*
 * org.openmicroscopy.shoola.env.data.model.TransferableObject 
 *
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


//Java imports
import java.util.Arrays;
import java.util.List;
import java.util.Map;

//Third-party libraries

//Application-internal dependencies

import omero.gateway.SecurityContext;
/** 
 * Hosts information necessary to transfer objects between groups.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since Beta4.4
 */
public class TransferableObject
{

	/** The security context of the target object.*/
	private SecurityContext targetContext;
	
	/** The destination of the change group action.*/
	private List<pojos.DataObject> target;
	
	/** The elements to move.*/
	private Map<SecurityContext, List<pojos.DataObject>> source;
	
	/** The name of the target group.*/
	private String groupName;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param targetContext Information about the destination group.
	 * Mustn't be <code>null</code>.
	 * @param target The object where to move data into or <code>null</code>.
	 * @param source The elements to move. Mustn't be <code>null</code>.
	 */
	public TransferableObject(SecurityContext targetContext, 
			pojos.DataObject target, 
			Map<SecurityContext, List<pojos.DataObject>> source)
	{
		if (targetContext == null)
			throw new IllegalArgumentException("No context specified.");
		if (source == null || source.size() == 0)
			throw new IllegalArgumentException("No elements to move.");
		this.targetContext = targetContext;
		if (target != null) this.target = Arrays.asList(target);
		this.source = source;
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param targetContext Information about the destination group.
	 * Mustn't be <code>null</code>.
	 * @param target The object where to move data into or <code>null</code>.
	 * @param source The elements to move. Mustn't be <code>null</code>.
	 */
	public TransferableObject(SecurityContext targetContext, 
			List<pojos.DataObject> target, 
			Map<SecurityContext, List<pojos.DataObject>> source)
	{
		if (targetContext == null)
			throw new IllegalArgumentException("No context specified.");
		if (source == null || source.size() == 0)
			throw new IllegalArgumentException("No elements to move.");
		this.targetContext = targetContext;
		this.target = target;
		this.source = source;
	}
	
	/**
	 * Returns the name of the group.
	 * 
	 * @return See above.
	 */
	public String getGroupName() { return groupName; }
	
	/**
	 * Sets the name of the group.
	 * 
	 * @param groupName The value to set.
	 */
	public void setGroupName(String groupName) { this.groupName = groupName; }
	
	/**
	 * Returns the security context of the target object.
	 * 
	 * @return See above.
	 */
	public SecurityContext getTargetContext() { return targetContext; }
	
	/**
	 * Returns the destination of the change group action.
	 * 
	 * @return See above.
	 */
	public List<pojos.DataObject> getTarget() { return target; }
	
	
	/**
	 * Returns the elements to move.
	 * 
	 * @return See above.
	 */
	public Map<SecurityContext, List<pojos.DataObject>> getSource()
	{
		return source;
	}
	
}
