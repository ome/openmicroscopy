
/*
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
 *	author Will Moore will@lifesci.dundee.ac.uk
 */

package omeroCal.model;

import java.util.Calendar;
import java.util.List;



public interface ICalendarModel {

	/**
	 * Get all the CalendarEvents for this month.
	 * 
	 * @return
	 */
	public List <CalendarEvent> getEventsForDates(Calendar fromDate, Calendar toDate);
	
	public void incrementMonth(int increment);
	
	
	/**
	 * Gets the CalendarObject that this CalendarEvent belongs to.
	 * 
	 * @param calID		The unique ID used to identify a calendar
	 * @return		A CalendarObject to which the CalendarEvent belongs (or null if not found)
	 */
	public CalendarObject getCalendarForEvent(CalendarEvent calendarEvent);
	
	/**
	 * Get all the CalendarEvents for a Calendar.
	 * This CalendarObject must have a value for CalendarID 
	 * (CalendarObject must have been created by the database)
	 * 
	 * @return
	 */
	public List <CalendarEvent> getEventsForCalendar(CalendarObject calendar);
}
