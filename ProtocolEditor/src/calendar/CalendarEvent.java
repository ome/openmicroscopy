
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
import java.util.GregorianCalendar;
import java.util.Observable;

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
public class CalendarEvent 
	extends Observable {

	/**
	 * A name for this event
	 */
	private String eventName;
	
	/**
	 * The time that this event starts. This must be set when the class is instantiated 
	 */
	private Calendar startTime;
	
	/**
	 * The time that this event ends
	 */
	private Calendar endTime;
	
	/**
	 * A time for an alarm for this event
	 */
	private Calendar alarmTime;
	
	/**
	 * This indicates whether an event occurs all day (or days), instead of at a particular time
	 */
	boolean allDayEvent = false;
	
	public CalendarEvent(String name, Calendar startTime) {
		eventName = name;
		initialiseTimes(startTime);
	}
	
	public CalendarEvent(String name, Date startTime) {
		eventName = name;
		initialiseTimes(startTime);
	}
	
	public CalendarEvent(Calendar startTime) {
		initialiseTimes(startTime);
	}
	
	public CalendarEvent(Date startTime) {
		initialiseTimes(startTime);
	}
		
	public void initialiseTimes(Calendar startTime) {
		this.startTime = startTime;
		endTime = startTime;
	}
	
	public void initialiseTimes(Date startTime) {
		GregorianCalendar time = new GregorianCalendar();
		time.setTime(startTime);
		
		initialiseTimes(time);
	}
	
	public CalendarEvent() {
		
	}
	
	public String getName() {
		return eventName;
	}
	
	public Date getStartTime() {
		if (startTime != null)
			return startTime.getTime();
		else 
			return null;
	}
	
	public Calendar getStartCalendar() {
		return startTime;
	}
	
	public Date getEndTime() {
		if (endTime != null)
			return endTime.getTime();
		else 
			return null;
	}
	
	public Calendar getEndCalendar() {
		return startTime;
	}
	
	public Date getAlarmTime() {
		if (alarmTime != null)
			return alarmTime.getTime();
		else 
			return null;
	}
	
	public Calendar getAlarmCalendar() {
		return alarmTime;
	}
	
	public void setAllDayEvent(boolean allDay) {
		allDayEvent = allDay;
	}
	
	public boolean isAllDayEvent() {
		return allDayEvent;
	}
	
	
}
