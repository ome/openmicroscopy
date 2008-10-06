/*
 * util.data.IObjectData 
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
public class IObjectData
{	
	/** The IObject, the base object of the constructed class. */
	private IObject iObject;
	
	/** 
	 * Every IObject has an IDetails object aggregated inside it, this is a 
	 * wrapper method to extract the IDetails object details. 
	 */
	private IDetails iDetails;
	
	/**
	 * Create the IObject data.
	 * @param object see above.
	 */
	public IObjectData(IObject object)
	{
		iObject = object;
		iDetails = new IDetails(object);
	}
	
	/** 
	 * Get the id of the IObject.
	 * @return see above.
	 */
	public long getID()
	{
		return iObject.getId().val;
	}
	
	/**
	 * Is the IObject loaded.
	 * @return see above.
	 */
	public boolean loaded()
	{
		return iObject.isLoaded();
	}
	
	/**
	 * Call to the iDetails objects method {@link IDetails#getOwner()}
	 * @return see above.
	 */
	public Experimenter getOwner()
	{
		return iDetails.getOwner();
	}	
	
	/**
	 * Call to the iDetails objects method {@link IDetails#getGroup()}
	 * @return see above.
	 */
	public ExperimenterGroup getGroup()
	{
		return iDetails.getGroup();
	}
	
	/**
	 * Call to the iDetails objects method {@link IDetails#getPermissions()}
	 * @return see above.
	 */
	public Permissions getPermissions()
	{
		return iDetails.getPermissions();
	}
	
	/**
	 * Call to the iDetails objects method {@link IDetails#getCreationEvent()}
	 * @return see above.
	 */
	public Event getCreationEvent()
	{
		return iDetails.getCreationEvent();
	}
	
	/**
	 * Call to the iDetails objects method {@link IDetails#getUpdateEvent()}
	 * @return see above.
	 */
	public Event getUpdateEvent()
	{
		return iDetails.getUpdateEvent();
	}
	
}


