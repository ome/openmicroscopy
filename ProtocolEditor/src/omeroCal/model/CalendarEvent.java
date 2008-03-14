
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

import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Observable;
import java.util.Observer;

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
	extends Observable 
	implements Observer {

	/**
	 * A unique ID that corresponds to the uID of this event in the database.
	 * Will be set if this object is returned from a call to the CalendarDataBase.
	 */
	private int uID;
	
	/**
	 * An identifier for the calendar to which this Event belongs.
	 */
	private int calendarID;
	
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
	
	/**
	 * The color to display this event. Determined by the calendar.
	 * This attribute is stored in the DB in the Calendar table. 
	 * Ignored when adding this CalendarEvent to the DB, but can be set when retrieving it from the DB
	 */
	private Color calendarColour;
	
	
	public static final String SELECTION_PROPERTY = "selectionProperty";
	
	/**
	 * 
	 * @param name
	 * @param startTime
	 */
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
	
	/**
	 * GETTERS AND SETTERS 
	 */
	
	public void setUID(int uID) {
		this.uID = uID;
	}
	
	public int getUID() {
		return uID;
	}
	
	public void setCalendarID(int cal_ID) {
		calendarID = uID;
	}
	
	public int getCalendarID() {
		return calendarID;
	}
	
	public void setName(String name) {
		eventName = name;
	}
	
	public String getName() {
		return eventName;
	}
	
	
	public void setStartTime(Date time) {
		if (time == null)
			startTime = null;
		else
			startTime.setTime(time);
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
	
	
	public void setEndTime(Date time) {
		if (time == null)
			endTime = null;
		else
			endTime.setTime(time);
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
	
	
	public void setAlarmTime(Date date) {
		if (date == null) {
			alarmTime = null;
		} else {
			alarmTime.setTime(date);
		}
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
	
	public void setCalendarColour(int colourInt) {
		calendarColour = new Color(colourInt);
	}
	
	/**
	 * returns calendarColour.getRGB()
	 * @return
	 */
	public int getCalendarColourInt() {
		if (calendarColour != null)
			return calendarColour.getRGB();
		else
			return 0;
	}
	
	public Color getCalendarColour() {
		return calendarColour;
	}

	public void update(Observable o, Object arg) {
		
		if (arg instanceof PropertyChangeEvent) {
			
			PropertyChangeEvent evt = (PropertyChangeEvent)arg;
			System.out.println("CalendarEvent update() PropertyChangeEvent " + evt.getPropertyName());
			
			if (evt.getPropertyName().equals(SELECTION_PROPERTY));
		}
		
	}
	
	/**
	 * Called by objects that know about this class (eg EventLabel)
	 * to notify observers that the event has changed.
	 * eg. Event has been clicked on. 
	 */
	public void selectionChanged (PropertyChangeEvent changeEvent) {
		
		System.out.println("CalendarEvent selectionChanged() to " + changeEvent.getNewValue());
		
		PropertyChangeEvent selectionChanged = new PropertyChangeEvent(this, SELECTION_PROPERTY, changeEvent.getOldValue(), changeEvent.getNewValue());
		
		setChanged();
		notifyObservers(selectionChanged);
	}
	
}
