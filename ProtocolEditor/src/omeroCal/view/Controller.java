
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

import omeroCal.model.CalendarEvent;
import omeroCal.model.IMonthModel;

/**
 * Coordinates UI events, allowing communication between UI components.
 * This also knows about the Model, where it gets data and passes changes to data
 * @author will
 *
 */
public class Controller {
	
	IMonthModel monthModel;
	
	MonthView monthView;
	
	public Controller (IMonthModel monthModel) {
		
		this.monthModel = monthModel;
		
	}

	public void setMonthView(MonthView monthView) {
		
		this.monthView = monthView;
	}
	
	/**
	 * This is called by a UI component 
	 */
	public void calendarEventSelected(CalendarEvent calendarEvent) {
		
		
	}
}
