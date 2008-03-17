
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

import java.beans.PropertyChangeEvent;
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
public class MonthModel 
	extends Observable 
	implements ICalendarModel,
	Observer {

	
	/**
	 * A reference to the Year and Month represented by this class
	 */
	GregorianCalendar thisMonth;
	
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
	public MonthModel(ICalendarDB calendarDB) {
		this.calendarDB = calendarDB;
		
		thisMonth = new GregorianCalendar();
		thisMonth.setTime(new Date());
		
		if (calendarDB instanceof Observable) {
			((Observable)calendarDB).addObserver(this);
		}
	}
	
	/**
	 * Get all the CalendarEvents for this month.
	 * 
	 * @return
	 */
	public List <CalendarEvent> getEventsForMonth() {
		
		List <CalendarEvent> events = calendarDB.getEventsForMonth(thisMonth);
		
		return events;
	}
	
	
	public void incrementMonth(int increment) {
		thisMonth.add(Calendar.MONTH, increment);
	}

	/**
	 * Database has been updated
	 * Need to notifyObservers, eg. Controller, so changes are shown in view
	 */
	public void update(Observable o, Object arg) {
		
		// TODO !! 
	}
	
}
