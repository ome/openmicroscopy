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
import java.util.Collection;

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
	
	/** 
	 * Indicates that the object correspond to a smart folder for time
	 * interval.
	 */
	public static final int TIME = 0;
	
	/** 
	 * Indicates that the object correspond to a smart folder for certain
	 * file type.
	 */
	public static final int FILE = 1;
	
	/** 
	 * Indicates to load the images if the index is <code>FILE</code>.
	 */
	public static final int FILE_IMAGE_TYPE = 100;
	
	/** User's id. */
	private long 		userID;
	
	/** One of the constants defined by this class. */
	private int			index;
	
	/** Time of reference. */
	private Timestamp 	endTime;
	
	/** Time of reference. */
	private Timestamp 	startTime;
	
	/** Constants identifying the supported type. */
	private int			fileType;
	
	/** The result of the call. */
	private Collection	results;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param userID	The user's id.
	 * @param index		One of the constants defined by this class. 
	 */
	public TimeRefObject(long userID, int index)
	{
		this.userID = userID;
		this.index = index;
		fileType = -1;
		
	}
	
	/**
	 * Sets the time interval.
	 * 
	 * @param startTime The time of reference. 
	 * @param endTime	The time of reference.
	 */
	public void setTimeInterval(Timestamp startTime, Timestamp endTime)
	{
		if (startTime == null && endTime == null)
			throw new IllegalArgumentException("Time interval not valid.");
		this.endTime = endTime;
		this.startTime = startTime;
	}

	/**
	 * Returns the index.
	 * 
	 * @return See above.
	 */
	public int getIndex() { return index; }
	
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
	public void setResults(Collection results) { this.results = results; }
	
	/**
	 * Returns the results of the call.
	 * 
	 * @return See above.
	 */
	public Collection getResults() { return results; }
	
	/**
	 * Sets the file type, only relevant if the index is {@link #FILE}.
	 * 
	 * @param fileType See above.
	 */
	public void setFileType(int fileType) { this.fileType = fileType; }
	
	/**
	 * Returns the file type.
	 * 
	 * @return See above.
	 */
	public int getFileType() { return fileType; }
	
}
