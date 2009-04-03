
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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JOptionPane;
import javax.swing.Timer;

import omeroCal.view.IEventListener;

import omeroCal.util.ImageFactory;
import omeroCal.util.PreferencesManager;


/**
 * The role of this class is to monitor the alarm events in the calendar database,
 * and fire an alarm if the current time reaches an alarm time.
 * 
 * One way of doing this is to keep retrieving all the events that have an alarm set within
 * a certain time period (eg next 24 hours).
 * Then, every minute that passes, check if the current date and time match any of these
 * alarm times. 
 * 
 * @author will
 *
 */

public class AlarmChecker {
	
	/**
	 * A list of listers that will be notified when an alarm goes off. 
	 */
	List<IEventListener> alarmListeners;
	
	/**
	 * The property string for the notification of IEventListeners, 
	 * in order to define the fact that an alarm has been activated. 
	 */
	public static final String ALARM_ACTIVATED = "alarmActivated";
	
	/**
	 * A date-time for the next retrieval of alarmed events from the database. 
	 * This is reset after each database update, being set to some time in the
	 * future, when the next database retrieval should take place.
	 */
	Calendar nextUpdate;
	
	/**
	 * A timer to fire off events.
	 * At each timeElapsed event, a check is made to see whether any of the alarm times in the 
	 * local list <code>alarmedEvents</code> matches the current time. 
	 * Also, a check is made to see whether it is time for the next update of alarmed events 
	 * from the database. 
	 */
	Timer timer;
	
	/**
	 * The time interval in milliseconds at which the time fires the actionPerformed, 
	 * which calls timeElapsed().
	 */
	public static int timerTickInMillis = 10 * 1000;
	
	/**
	 * The time between database updates is defined by a value and a unit.
	 * eg 24 hours. This variable is the value.
	 */
	public static int TIME_VALUE_BETWEEN_UPDATES = 1;		// eg 1 hour
	
	/**
	 * The time between database updates is defined by a value and a unit.
	 * eg 24 hours. This variable is the unit.
	 */
	public static int TIME_UNITS_BETWEEN_UPDATES = Calendar.MINUTE;
	
	/**
	 * A list to hold the local set of alarmed events. 
	 * This is updated periodically from the database, and is more frequently
	 * checked to see if any alarm times are before or equal to the current time.
	 * If so, the alarm is fired, and the event is removed from the list. 
	 */
	List<CalendarEvent> alarmedEvents;
	
	/**
	 * The database which is queried for alarmed events. 
	 */
	ICalendarDB calDB;
	
	/**
	 * Allows other classes to disable the default pop-up when an alarm is activated. 
	 * For example, if another class wants to take responsibility for alarm events
	 * via addAlarmEventListener();
	 */
	boolean defaultAlarmPopupEnabled = true;
	
	
	public AlarmChecker() {
		
		// instantiate the DB
		calDB = new CalendarDataBase();
		
		// instantiate the list of listeners
		alarmListeners = new ArrayList<IEventListener>();
		
		/*
		 * Add the shutdown process to the Runtime shutdown,
		 * so that when the application quits, a note is made of the time.  
		 */
		Runtime.getRuntime().addShutdownHook(new ShutdownThread());
		
		
		/*
		 * nextUpdate is usually set to some time in the future, when you next need
		 * to retrieve alarm times from the database.
		 * 
		 * However, when starting up, the database won't have been checked for a while,
		 * so the time of nextUpdate should be set to the time of shutdown 
		 * (or now if no shutdown time).
		 */
		nextUpdate = new GregorianCalendar();

		String lastShutdownTime = PreferencesManager.getPreference(PreferencesManager.SHUTDOWN_TIME_UTC);
		if (lastShutdownTime != null) {
			Long utcMillis = new Long(lastShutdownTime);
			nextUpdate.setTimeInMillis(utcMillis);
		}
		
		/*
		 * Get alarm times between nextUpdate and some point in the future.
		 * Then check these for alarm times before Now. 
		 */
		getAlarmedEventsFromDB();
		checkAlarmedEvents();
		
		// Start the timer.
		timer = new Timer(timerTickInMillis, new TimeElapsedListener());
		System.out.println("Created alarm timer...");
		timer.start();
	}
	
	public class TimeElapsedListener implements ActionListener {
		public void actionPerformed(ActionEvent arg0) {
			
			timeElapsed();
		}
	}
	
	/**
	 * This method may be called every minute, but should only refresh the
	 * list of events from the DB if the fixed time (eg 24 hours) has passed
	 * since the last time the list was refreshed. 
	 */
	public void timeElapsed() {
		
		// System.out.println("AlarmChecker timeElapsed()");
		
		/*
		 * First check the local list of alarmed events 
		 */
		checkAlarmedEvents();
		
		
		/*
		 * Then update the alarmed events list from the DB
		 * if the time of nextUpdate has been reached. 
		 */
		Calendar now = new GregorianCalendar();
		now.setTime(new Date());
		
		if (now.compareTo(nextUpdate) >= 0 ) {
			getAlarmedEventsFromDB();
			
			// check the new list of events immediately
			checkAlarmedEvents();
		}
		
	}
	
	/**
	 * Iterate through the list of alarmed events,
	 * checking the alarm time of each, to see if it is before (or equal to) NOW.
	 */
	public void checkAlarmedEvents() {
		
		Calendar now = new GregorianCalendar();
		
		for (int i=alarmedEvents.size()-1 ; i >=0 ; i--) {
			
			Calendar alarmTime = alarmedEvents.get(i).getAlarmCalendar();
			
			if (alarmTime.compareTo(now) <= 0) {
				
				CalendarEvent event = alarmedEvents.get(i);
				
				/*
				 * First, notify any listeners, passing them the alarmed event
				 */
				notifyAlarmEventListeners(event);
				
				/*
				 * Then, if the default alarm is enabled, show the pop-up.
				 */
				if (defaultAlarmPopupEnabled)
					alarmActivated(event);				
				
				// remove from list
				alarmedEvents.remove(i);
			}
		}
		
	}
	
	/**
	 * This method is called when an alarm is activated for a particular calendarEvent. 
	 * 
	 * @param event		The calendar event for which the alarm was set. 
	 */
	public void alarmActivated(CalendarEvent event) {
		
		Calendar now = new GregorianCalendar();
		
		// get details
		String eventName = event.getName();
		String timeString = "";
		
		Calendar eventTime = event.getStartCalendar();
		if (eventTime.get(Calendar.DAY_OF_MONTH) == now.get(Calendar.DAY_OF_MONTH)) {
			timeString = "Today";
		}
		else {
			SimpleDateFormat day = new SimpleDateFormat("EEEE, MMM d");
			timeString = day.format(eventTime.getTime());
		}
		
		timeString = timeString + " at ";
		
		SimpleDateFormat hhmm = new SimpleDateFormat("HH:mm");
		timeString = timeString + hhmm.format(eventTime.getTime());
		
		String message = "<html><b>" + timeString + "</b><br>" +
			eventName + "</html>";
		
		System.out.println("AlarmChecker message: " + message);
		
		// Fire the alarm!! 
		Icon alarmIcon = ImageFactory.getInstance().getIcon(ImageFactory.ALARM_GIF_64);
		JOptionPane.showMessageDialog(null, 
				message, "Omero.Editor Alarm", JOptionPane.INFORMATION_MESSAGE, alarmIcon);
		
	}
	
	/**
	 * This retrieves events from the database that have alarms set for
	 * within the next eg 24 hours. 
	 * 
	 */
	public void getAlarmedEventsFromDB() {
		
		System.out.println("AlarmChecker getAlarmedEventsFromDB()");
		
		/*
		 * Need to define 2 date-times to get alarmed events from DB.
		 * 
		 * The last update will have retrieved events that have alarm times
		 * up to "nextUpdate".
		 * Therefore, this should be the fromDate for this DB request.
		 * This is most likely to be NOW, but if this method is delayed,
		 * it may be a short time in the past. 
		 * This means that no alarm times will be missed by a delay in calling this method. 
		 * 
		 * The future date-time, toDate, should be set ahead of NOW, by the time
		 * specified by time UNITS and VALUE.
		 */
		
		Calendar toDate = new GregorianCalendar();
		toDate.add(TIME_UNITS_BETWEEN_UPDATES, TIME_VALUE_BETWEEN_UPDATES);
		
		alarmedEvents = calDB.getEventsForAlarmTimes(nextUpdate, toDate);
		
		/*
		 * The next update should occur when we reach the end of time-span of the
		 * last update. ie, when we reach the toDate. 
		 */
		nextUpdate.setTime(toDate.getTime());
	}
	
	/**
	 * Allows other classes to disable the default pop-up when an alarm is activated. 
	 * For example, if another class wants to take responsibility for alarm events
	 * via addAlarmEventListener();
	 */
	public void enableDefaultAlarmPopup(boolean enabled) {
		defaultAlarmPopupEnabled = enabled;
	}
	
	/**
	 * @return		true if the default pop-up will show when an alarm goes off. 
	 */
	public boolean isDefaultAlarmPopupEnabled() {
		return defaultAlarmPopupEnabled;
	}
	
	/**
	 * Allow other IEventListener classes to listen for alarm events. 
	 * When an alarm goes off, 
	 * calendarEventChanged(calendarEvent, "alarmActivated", null)
	 * will be called for each listener. 
	 * 
	 * @param listener
	 */
	public void addAlarmEventListener(IEventListener listener) {
		alarmListeners.add(listener);
	}
	
	/**
	 * Classes can remove themselves from the alarmListeners list. 
	 * @param listener
	 */
	public void removeAlarmEventListener(IEventListener listener) {
		alarmListeners.remove(listener);
	}
	
	/**
	 * This is called when an alarm is activated, to notify any listeners.
	 * The event that the alarm belongs to is passed to the listeners.
	 * The property String is ALARM_ACTIVATED.
	 * 
	 * @param calendarEvent
	 */
	public void notifyAlarmEventListeners(CalendarEvent calendarEvent) {
		
		for (IEventListener listener : alarmListeners) {
			listener.calendarEventChanged(calendarEvent, ALARM_ACTIVATED, true);
		}
	}
	
	/**
	 * When the system shuts down, you need to record when this happened, so 
	 * that when you start up again, you can check the intervening time period 
	 * for alarms that should have gone off then. 
	 * 
	 * This thread runs at shutdown, and saves the current time as 
	 * UTC milliseconds to the system preferences. 
	 * 
	 * @author will
	 *
	 */
	class ShutdownThread extends Thread {

        public void run() {
        	Calendar now = new GregorianCalendar();
        	String UTCmillis = Long.toString(now.getTimeInMillis());
        	
        	System.out.println("AlarmChecker Saving shutdown time...");
        	PreferencesManager.setPreference(PreferencesManager.SHUTDOWN_TIME_UTC, UTCmillis);
        }
    }
	
}
