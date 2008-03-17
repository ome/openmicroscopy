
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

import java.util.List;
import java.util.Observable;
import java.util.Observer;

import omeroCal.model.CalendarEvent;
import omeroCal.model.ICalendarModel;

/**
 * Coordinates UI events, allowing communication between UI components.
 * This also knows about the Model, where it gets data and passes changes to data
 * @author will
 *
 */
public class Controller 
	implements ICalendarModel,
	Observer,
	IEventController {
	
	ICalendarModel monthModel;
	
	MonthView monthView;
	
	public Controller (ICalendarModel monthModel) {
		
		this.monthModel = monthModel;
		
		// Need to observe the Model, for changes that need the view to be updated
		if (monthModel instanceof Observable) {
			((Observable)monthModel).addObserver(this);
		}
		
	}

	public void setMonthView(MonthView monthView) {
		
		this.monthView = monthView;
	}
	

	/**
	 * Delegates to the MonthModel
	 */
	public List<CalendarEvent> getEventsForMonth() {
		
		return monthModel.getEventsForMonth();
	}

	/**
	 * Delegates to the MonthModel
	 */
	public void incrementMonth(int increment) {
		
		monthModel.incrementMonth(increment);
	}

	/**
	 * This is fired when the Model changes
	 * Therefore, need to update the view with new data from Model.
	 */
	public void update(Observable o, Object arg) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * This is called by a UI component, eg when clicked or double-clicked
	 */
	public void calendarEventChanged(CalendarEvent calendarEvent, String propertyChanged, Object newProperty) {
		// TODO Auto-generated method stub
		
		monthView.calendarEventChanged(calendarEvent, propertyChanged, newProperty);
	}
}
