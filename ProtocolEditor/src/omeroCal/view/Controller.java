
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

package omeroCal.view;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import omeroCal.model.CalendarEvent;
import omeroCal.model.CalendarObject;
import omeroCal.model.ICalendarModel;

/**
 * Coordinates UI events, allowing communication between UI components.
 * This also knows about the Model, where it gets data and passes changes to data
 * @author will
 *
 */
public class Controller 
	extends Observable
	implements ICalendarModel,
	Observer,
	IEventListener {
	
	ICalendarModel monthModel;
	
	ArrayList<IEventListener> eventListeners;
	
	public Controller (ICalendarModel monthModel) {
		
		this.monthModel = monthModel;
		
		// Need to observe the Model, for changes that need the view to be updated
		if (monthModel instanceof Observable) {
			((Observable)monthModel).addObserver(this);
		}
		
		eventListeners = new ArrayList<IEventListener>();
		
	}
	

	/**
	 * Delegates to the MonthModel
	 */
	public List <CalendarEvent> getEventsForDates(Calendar fromDate, Calendar toDate) {
		
		return monthModel.getEventsForDates(fromDate, toDate);
	}

	/**
	 * Delegates to the MonthModel
	 */
	public void incrementMonth(int increment) {
		
		monthModel.incrementMonth(increment);
	}
	
	/**
	 * Gets the CalendarObject that this CalendarEvent belongs to.
	 * 
	 * @param calID		The unique ID used to identify a calendar
	 * @return		A CalendarObject to which the CalendarEvent belongs (or null if not found)
	 */
	public CalendarObject getCalendarForEvent(CalendarEvent calendarEvent) {
		return monthModel.getCalendarForEvent(calendarEvent);
	}
	
	/**
	 * Get all the CalendarEvents for a Calendar.
	 * This CalendarObject must have a value for CalendarID 
	 * (CalendarObject must have been created by the database)
	 * 
	 * @return
	 */
	public List <CalendarEvent> getEventsForCalendar(CalendarObject calendar) {
		return monthModel.getEventsForCalendar(calendar);
	}

	/**
	 * This is fired when the Model changes
	 * Therefore, need to update the view with new data from Model.
	 */
	public void update(Observable o, Object arg) {
		
		setChanged();
		notifyObservers();
		
	}
	
	public void addEventListener(IEventListener ec) {
		eventListeners.add(ec);
	}
	
	public void removeEventListener(IEventListener ec) {
		eventListeners.remove(ec);
	}

	/**
	 * This is called by a UI component, eg when clicked or double-clicked
	 */
	public void calendarEventChanged(CalendarEvent calendarEvent, String propertyChanged, Object newProperty) {
		for (IEventListener eventController : eventListeners) {
			eventController.calendarEventChanged(calendarEvent, propertyChanged, newProperty);
		}
	}
}
