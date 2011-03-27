/*
 * org.openmicroscopy.shoola.env.data.model.UserDiskQuota 
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
package org.openmicroscopy.shoola.env.data.model;

//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * Store the disk space used and available for a given user.
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
public class DiskQuota
{

	/** Indicates that the values retrieved are for a given group. */
	public static final int GROUP = 0;
	
	/** Indicates that the values retrieved are for a given user. */
	public static final int USER = 1;
	
	/** The disk space used. */
	private long used;
	
	/** The disk space available. */
	private long available;
	
	/** The identifier of the user or the group. */
	private long id;
	
	/** One of the constants defined by this class. */
	private int type;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param type One of the constants defined by this class.
	 * @param id
	 * @param used
	 * @param available
	 */
	public DiskQuota(int type, long id, long used, long available)
	{
		this.type = type;
		this.used = used;
		this.id = id;
		this.available = available;
	}
	
	/**
	 * Returns the identifier of the user or group depending on the type.
	 * 
	 * @return See above.
	 */
	public long getID() { return id; }
	
	/**
	 * Returns the used space.
	 * 
	 * @return See above.
	 */
	public long getUsedSpace() { return used; }
	
	/**
	 * Returns the available space.
	 * 
	 * @return See above.
	 */
	public long getAvailableSpace() { return available; }

}
