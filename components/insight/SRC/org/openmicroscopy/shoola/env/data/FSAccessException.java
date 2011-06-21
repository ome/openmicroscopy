/*
 * org.openmicroscopy.shoola.env.data.FSAccessException 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2010 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.env.data;

//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * Reports an error occurred while trying to interact w/ the File System.
 *
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class FSAccessException 
	extends Exception
{

	/** Indicates that the pyramid is not ready.*/
	public static final int PYRAMID = 1;
	
	/** Indicates that the pyramid is not ready file is locked.*/
	public static final int LOCKED = 0;
	
	/** One of the constants defined by this class.*/
	private int index;
	
	/** The time to wait before the pyramid is ready.*/
	private Long backOffTime;
	
	/**
	 * Constructs a new exception with the specified detail message.
	 * 
	 * @param message	Short explanation of the problem.
	 */
	public FSAccessException(String message)
	{
		super(message);
		index = LOCKED;
	}
	
	/**
	 * Constructs a new exception with the specified detail message and cause.
	 * 
	 * @param message	Short explanation of the problem.
	 * @param cause		The exception that caused this one to be risen.
	 */
	public FSAccessException(String message, Throwable cause) 
	{
		super(message, cause);
	}
	
	/**
	 * Sets the index, one of the constants defined by this class.
	 * 
	 * @param index The value to set.
	 */
	public void setIndex(int index) { this.index = index; }
	
	/**
	 * Returns the index, one of the constants defined by this class.
	 * 
	 * @return See above.
	 */
	public int getIndex() { return index; }
	
	/**
	 * Sets the time to wait before the pyramid is ready.
	 * 
	 * @param backOfftime The value to set.
	 */
	public void setBackOffTime(Long backOfftime)
	{ 
		this.backOffTime = backOfftime;
	}
	
	/**
	 * Returns the time to wait before the pyramid is ready.
	 * 
	 * @return See above.
	 */
	public Long getBackOffTime() { return backOffTime; }
	

}
