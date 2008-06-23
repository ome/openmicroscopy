
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
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Observable;
import java.util.Observer;


/**
 * This class is the Model of data (currently a Month, but may include week / day in future).
 * It implements the IMonthModel interface, for getting data. eg getEvents()
 * It also acts as an observer of the calendarDB, so that it is notified of changes to DB. 
 * 
 * @author will
 *
 */
public class CalendarModel 
	extends Observable 
	implements ICalendarModel,
	Observer {

	
	/**
	 * A reference to the current date represented by this class
	 */
	GregorianCalendar currentDate;
	
	/**
	 * The database, or other source of data for getting events to display etc. 
	 */
	ICalendarDB calendarDB;
	
	
	/**
	 * Creates an instance of this class.
	 * Needs to know about the source of info (eg database).
	 * 
	 * @param calendarDB	the source of data!
	 */
	public CalendarModel(ICalendarDB calendarDB) {
		this.calendarDB = calendarDB;
		
		currentDate = new GregorianCalendar();
		currentDate.setTime(new Date());
		
		if (calendarDB instanceof Observable) {
			((Observable)calendarDB).addObserver(this);
		}
	}
	
	/**
	 * Get all the CalendarEvents for the month of the current date.
	 * Not sure if this will be used in future.
	 * 
	 * @return
	 */
	public List <CalendarEvent> getEventsForMonth() {
		
		// make a copy of the Calendar (don't want to alter the one referenced in arguments)
		Calendar fromDate = new GregorianCalendar();
		fromDate.setTime(currentDate.getTime());
		// set the fromDate to the start of the month
		fromDate.set(Calendar.DAY_OF_MONTH, 1);
		fromDate.set(Calendar.HOUR_OF_DAY, 0);
		fromDate.set(Calendar.MINUTE, 0);
		
		// make a copy of the Calendar (don't want to alter the one referenced in arguments)
		Calendar toDate = new GregorianCalendar();
		toDate.setTime(fromDate.getTime());
		// set the toDate to the end of the month
		toDate.add(Calendar.MONTH, 1);
		
		return getEventsForDates(fromDate, toDate);

	}
	
	
	/**
	 * Get all the CalendarEvents for this month.
	 * 
	 * @return
	 */
	public List <CalendarEvent> getEventsForDates(Calendar fromDate, Calendar toDate) {
		return calendarDB.getEventsForDates(fromDate, toDate);
	}
	
	
	/**
	 * Get all the CalendarEvents for a Calendar.
	 * This CalendarObject must have a value for CalendarID 
	 * (CalendarObject must have been created by the database)
	 * 
	 * @return
	 */
	public List <CalendarEvent> getEventsForCalendar(CalendarObject calendar) {
		int calendarID = calendar.getCalendarID();
		return calendarDB.getEventsForCalendar(calendarID);
	}
	
	
	public void incrementMonth(int increment) {
		currentDate.add(Calendar.MONTH, increment);
	}
	
	/**
	 * Gets the CalendarObject that this CalendarEvent belongs to.
	 * 
	 * @param calID		The unique ID used to identify a calendar
	 * @return		A CalendarObject to which the CalendarEvent belongs (or null if not found)
	 */
	public CalendarObject getCalendarForEvent(CalendarEvent calendarEvent) {
		
		int calID = calendarEvent.getCalendarID();
		
		return calendarDB.getCalendar(calID);
	}
	

	/**
	 * Database has been updated
	 * Need to notifyObservers, eg. Controller, so changes are shown in view
	 */
	public void update(Observable o, Object arg) {
		
		// notify observers of change to DB
		setChanged();
		notifyObservers();
	}

	
}
