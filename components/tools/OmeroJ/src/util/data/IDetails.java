/*
 * util.data.IDetails 
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
package util.data;


//Java imports

//Third-party libraries

//Application-internal dependencies
import omero.model.Event;
import omero.model.Experimenter;
import omero.model.ExperimenterGroup;
import omero.model.IObject;
import omero.model.Permissions;

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class IDetails
{	
	/** The wrapped base IObject of the container. */
	private IObject object;
	
	/**
	 * Create the IDetails object of the container.
	 * @param object
	 */
	public IDetails(IObject object)
	{
		this.object = object;
	}
	
	/**
	 * Get the owner of the idetails object.
	 * @return see above.
	 */
	public Experimenter getOwner()
	{
		return object.details.owner;
	}	
	
	/**
	 * Get the group of the idetails object.
	 * @return see above.
	 */
	public ExperimenterGroup getGroup()
	{
		return object.details.group;
	}
	
	/**
	 * Get the permissions of the idetails object.
	 * @return see above.
	 */
	public Permissions getPermissions()
	{
		return object.details.permissions;
	}
	
	/**
	 * Get the creationEvent of the idetails object.
	 * @return see above.
	 */
	public Event getCreationEvent()
	{
		return object.details.creationEvent;
	}
	
	/**
	 * Get the updateEvent of the idetails object.
	 * @return see above.
	 */
	public Event getUpdateEvent()
	{
		return object.details.updateEvent;
	}
	
	
}


