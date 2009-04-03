 /*
 * omeroCal.view.IDayDisplay 
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
package omeroCal.view;

//Java imports

import java.util.Date;

import javax.swing.JComponent;

//Third-party libraries

//Application-internal dependencies

/** 
 * An interface for UI components that represent a day in the calendar.
 * Have methods for changing the appearance and for adding 
 * UI components representing events. 
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public interface IDayDisplay {
	
	/**
	 * Adds an event component, for display in this panel, or maybe 
	 * simply change the way this day looks?
	 * 
	 * @param event		The event UI component to add
	 */
	public void addEvent(JComponent event);
	
	/**
	 * The month display includes days that are from the previous and next
	 * months. 
	 * These should be displayed differently, using this method. 
	 * 
	 * @param dayFromOtherMonth		True if this day is from another month
	 */
	public void setDayFromOtherMonth(boolean dayFromOtherMonth);
	
	/**
	 * If this day is today, it should look a bit different. 
	 * 
	 * @param today		True if this day is today!
	 */
	public void setToday(boolean today);

	/**
	 * Gets the date of this day
	 * 
	 * @return	The date that this day is displaying.
	 */
	public Date getDate();
}
