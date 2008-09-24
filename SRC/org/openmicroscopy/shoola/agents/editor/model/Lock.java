 /*
 * org.openmicroscopy.shoola.agents.editor.model.Lock 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.editor.model;

import java.text.SimpleDateFormat;
import java.util.Date;

//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * This object represents a "Lock" placed on a field, which prevents that
 * field being edited. 
 * The Lock can be at different "Levels": {@link #TEMPLATE_LOCKED} means 
 * that the experimental variables can still be edited, while 
 * {@link #FULLY_LOCKED} means nothing can be edited. 
 * Editing locks are passed down to a fields' children, so that they are
 * also locked at the same level as their parent.  
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class Lock {
	
	/**
	 * A locking state that indicates that the field is not locked.
	 */
	public static final int 	NOT_LOCKED = 0;
	
	/**
	 * A locking state that indicates a "Template Lock", which allows 
	 * experimental variables of the field parameters to be edited.
	 */
	public static final int 	TEMPLATE_LOCKED = 1;
	
	/**
	 * A locking state that indicates the field is "Fully Locked" and
	 * no attributes or parameters can be edited. 
	 */
	public static final int 	FULLY_LOCKED = 2;
	
	/**
	 * The state or locked level of this Lock. 
	 */
	private int 				lockedLevel;
	
	/**
	 * A String to refer to the user who applied the lock to this field.
	 */
	private String 				lockedByUserName;
	
	/**
	 * A timeStamp to indicate when this lock was applied. 
	 */
	private Date				lockedTimeStamp;

	
	/**
	 * Creates an instance. 
	 * 
	 * @param lockLevel			The lock level. Should be 
	 * 					{@link #TEMPLATE_LOCKED} or {@link #FULLY_LOCKED}
	 * @param userName			The name of the user applying the lock. 
	 */
	public Lock(int lockLevel, String userName) 
	{
		setLockLevel(lockLevel);
		lockedByUserName = userName;
		lockedTimeStamp = new Date();
	}
	
	/**
	 * Returns the {@link Date} that the lock was created / applied. 
	 * 
	 * @return	see above.
	 */
	public Date getTimeStamp() { return lockedTimeStamp; }
	
	/**
	 * Uses a String (UTC milliseconds) to set the {@link lockedTimeStamp} of
	 * this lock. 
	 * This is a convenience method, since timeStamps are stored as  
	 * UTCmillisecs Strings in XML.
	 * 
	 * @param utcMillisecs
	 */
	public void setTimeStamp(String utcMillisecs) 
	{
		if (utcMillisecs == null)	return;
		lockedTimeStamp.setTime(new Long(utcMillisecs));
	}
	
	/**
	 * Returns the {@link #lockedTimeStamp} as a String of UTCmillisecs. 
	 * 
	 * @return	see above. 
	 */
	public String getTimeStampAsString() 
	{
		if (lockedTimeStamp == null) return null;
		return Long.toString(lockedTimeStamp.getTime());
	}
	
	/**
	 * Sets the lock level, which should be 
	 * {@link #TEMPLATE_LOCKED} or {@link #FULLY_LOCKED}
	 * 
	 * @param lockLevel		The new lock level
	 */
	public void setLockLevel(int lockLevel) {
		checkLockLevel(lockLevel);
		lockedLevel = lockLevel;
	}
	
	/**
	 * Returns the lock level.
	 * 
	 * @return		see above.
	 */
	public int getLockLevel() { return lockedLevel; }
	
	/**
	 * Checks that the lock level is one of the accepted lock levels. 
	 * If not, a {@link RuntimeException} is thrown.
	 * This method is used by {@link #setLockLevel(int)}
	 * 
	 * @param level		The lock level to check
	 */
	private void checkLockLevel(int level)
	{
		switch (level) {
		case NOT_LOCKED:
			break;
		case TEMPLATE_LOCKED:
			break;
		case FULLY_LOCKED:
			break;
		default:
			throw new RuntimeException("Invalid Locking Level");
		}
	}
	
	/**
	 * Returns a convenient String representation of this lock. 
	 * eg "Template Locked by will at 13:25 on Fri, May 9th, 2008"
	 */
	public String toString()
	{
		String s = "";
		
		switch (lockedLevel) {
		case NOT_LOCKED:
			return "Unlocked";
		case TEMPLATE_LOCKED:
			s = "Template Locked";
			break;
		case FULLY_LOCKED:
			s = "Field Locked";
			break;
		}
		
		if ((lockedByUserName != null) && (lockedByUserName.length() >0)) 
			s = s + " by " + lockedByUserName;
		
		if (lockedTimeStamp != null) {
			SimpleDateFormat time = new SimpleDateFormat
											("HH:mm 'on' EEE, MMM d, yyyy");
			s = s + " at " + time.format(lockedTimeStamp.getTime());
		}
		
		return s;
	}
}
