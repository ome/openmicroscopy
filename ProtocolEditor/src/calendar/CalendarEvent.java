
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
import java.util.Date;

/**
 * This represents an event in the calendar. Either created from a DateTime field, or from a Time field
 * that follows a date.
 * Basically, this class is a Date & Time (Gregorian Calendar instance) and a name (String).
 * A CalendarFile may contain several CalendarEvent instances. 
 * eg Transfection-Date, followed by (2 days later) Fixation time. 
 * 
 * @author will
 *
 */
public class CalendarEvent {

	String eventName;
	
	Calendar eventTime;
	
	public CalendarEvent(String name, Calendar time) {
		
		eventName = name;
		eventTime = time;
	}
	
	public String getName() {
		return eventName;
	}
	
	public Date getTime() {
		return eventTime.getTime();
	}
	
}
