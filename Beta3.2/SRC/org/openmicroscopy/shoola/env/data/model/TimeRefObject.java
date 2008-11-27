/*
 * org.openmicroscopy.shoola.env.data.model.TimeRefObject 
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
package org.openmicroscopy.shoola.env.data.model;



//Java imports
import java.sql.Timestamp;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies

/** 
 * Utility class while refreshing smart folders.
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
public class TimeRefObject
{
	
	/** User's id. */
	private long 		userID;
	
	/** Time of reference. */
	private Timestamp 	endTime;
	
	/** Time of reference. */
	private Timestamp 	startTime;
	
	/** The result of the call. */
	private Set			results;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param userID	The user's id.
	 * @param startTime	The time of reference. 
	 * @param endTime	The time of reference.
	 * @param constrain	Value indicating to retrieve the value before or
	 * 					after the time of reference 
	 */
	public TimeRefObject(long userID, Timestamp startTime, Timestamp endTime)
	{
		if (startTime == null && endTime == null)
			throw new IllegalArgumentException("Time interval not valid.");
		this.userID = userID;
		this.endTime = endTime;
		this.startTime = startTime;
	}

	/**
	 * Returns the time of reference.
	 * 
	 * @return See above.
	 */
	public Timestamp getStartTime() { return startTime; }
	
	/**
	 * Returns the time of reference.
	 * 
	 * @return See above.
	 */
	public Timestamp getEndTime() { return endTime; }
	
	/**
	 * Returns the user's id.
	 * 
	 * @return See above.
	 */
	public long getUserID() { return userID; }

	/**
	 * Sets the results of the call.
	 * 
	 * @param results The value to set.
	 */
	public void setResults(Set results) { this.results = results; }
	
	/**
	 * Returns the results of the call.
	 * 
	 * @return See above.
	 */
	public Set getResults() { return results; }
	
}
