
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

package calendar;

import java.util.Calendar;
import java.util.List;

public interface ICalendarDB {

	/**
	 * Adds CalendarFile as a new entry in Table CALENDAR_TABLE
	 * Then adds the CalendarEvents as entries in EVENT_TABLE, with fileID linking to FILE_TABLE
	 * 
	 * @param calendarFile		The CalendarFile to add to the DB.
	 */
	public int saveCalendar(CalendarObject calendar);
	
	
	/**
	 * Add a CalendarEvent to the database, and associate it with a calendar, identified by ID
	 * 
	 * @param calEvt		The CalendarEvent object to add to the database	
	 * @param calendar_ID	The unique_ID for the calendar to which this event should be added
	 */
	public void saveEvent(CalendarEvent calEvt, int calendar_ID);
	
	
	/**
	 * Get all the events for a specified Calendar ID
	 * 
	 * @param calendarID
	 * @return	A List of the CalendarEvents that belong to the calendar identified by the ID
	 */
	public List<CalendarEvent> getEvents (int calendarID);
	
	
	/**
	 * Gets a list of CalendarEvents for a given Month (and year). 
	 * Events are returned if they start before the end of the month, or end after the start of the month
	 * 
	 * @param yearMonth		A date-time that includes a year and month. Other values (day etc) are ignored
	 * @return			A List of events 
	 */
	public List<CalendarEvent> getEventsForMonth(Calendar yearMonth);
}
