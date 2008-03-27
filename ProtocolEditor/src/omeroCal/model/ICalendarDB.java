
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
	 * Updates a CalendarEvent which is already in the database. 
	 * This will replace all the values in the corresponding row of the Events table
	 * with values from the calendarEvent. 
	 * The correct row in the database is identified by the UID attribute returned by
	 * calendarEvent.getUID();
	 * If the corresponding row is not found in the DB, this method returns false. 
	 * 
	 * @param calendarEvent		The calendar 
	 * @return				If the update is successful.
	 */
	public boolean updateEvent(CalendarEvent calendarEvent);
	
	
	/**
	 * Get all the events for a specified Calendar ID
	 * 
	 * @param calendarID
	 * @return	A List of the CalendarEvents that belong to the calendar identified by the ID
	 */
	public List<CalendarEvent> getEventsForCalendar (int calendarID);
	
	
	/**
	 * Removes all events from the Events table if they belong to the calendar
	 * specified by calendarID.
	 * 
	 * @param calendarID
	 * @return		The number of rows deleted.
	 */
	public int deleteEventsForCalendar(int calendarID);
	
	
	/**
	 * Gets a list of CalendarEvents that fall between 2 dates (start-date and end-date).
	 * Events are returned if they start before the end date, or end after the start date.
	 * 
	 * @param fromThisDate
	 * @param toThisDate
	 * @return			A List of events as CalendarEvent objects.
	 */
	public List<CalendarEvent> getEventsForDates(Calendar fromThisDate, Calendar toThisDate);
	
	
	/**
	 * Gets a list of CalendarEvents with alarm times that fall between 2 dates
	 * Events are returned if alarm time is before the end date, or after the start date.
	 * 
	 * @param fromThisDate
	 * @param toThisDate
	 * @return			A List of events as CalendarEvent objects.
	 */
	public List<CalendarEvent> getEventsForAlarmTimes(Calendar fromThisTime, Calendar toThisTime);
	
	
	/**
	 * Simply gets the CalendarObject specified by its unique ID from the calendar table
	 * 
	 * @param calID		The unique ID used to identify a calendar
	 * @return		A CalendarObject that contains all the info from that row of the DB (or null if not found)
	 */
	public CalendarObject getCalendar(int calID);
	
	
	/**
	 * Gets a list of the CalendarObjects that matches the string calendarInfo.
	 * 
	 * @param calID		The unique ID used to identify a calendar
	 * @return		A CalendarObject that contains all the info from that row of the DB (or null if not found)
	 */
	public List<CalendarObject> getCalendarsByInfo(String calendarInfo);
	
	
	/**
	 * Updates a CalendarObjectt which is already in the database. 
	 * This will replace all the values in the corresponding row of the Calendar table
	 * with values from the calendarEvent. 
	 * The correct row in the database is identified by the UID attribute returned by
	 * CalendarObject.getCalendarID();
	 * If no entries are modified by this method, this method returns false. 
	 * 
	 * @param calendarEvent		The calendar 
	 * @return				true if the update is successful.
	 */
	public boolean updateCalendar(CalendarObject calendarObject);
	
	
	/**
	 * Clears all data from the tables in the database. 
	 * USE WITH CARE!!
	 */
	public void clearTables();
}
