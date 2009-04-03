
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

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Calendar;

import javax.swing.BoxLayout;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import javax.swing.text.JTextComponent;
import javax.swing.text.MaskFormatter;

import omeroCal.model.CalendarEvent;

import uiComponents.CustomComboBox;


public class AlarmEditor
	extends JPanel{
	
	JFormattedTextField unitsField;
	
	public static final int DAYS = 0;
	public static final int HOURS = 1;
	public static final int MINS = 2;
	public static final String[] timeUnits = {"Days", "Hours", "Minutes"};
	public static final int[] secondsInTimeUnit = {24*3600, 3600, 60};
	CalendarComboBox timeUnitChooser;
	
	public static final int BEFORE = 0;
	public static final int AFTER = 1;
	public static final String[] beforeAfterOptions = {"Before", "After"};
	CalendarComboBox beforeAfterChooser;

	
	CalendarEvent calendarEvent;
	
	public AlarmEditor(CalendarEvent calendarEvent) {
		
		this.calendarEvent = calendarEvent;
		
		this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		this.setBackground(null);
		
		unitsField = new CalendarFormattedTextField();
		unitsField.setText("00");
		this.add(unitsField);
		
		timeUnitChooser = new CalendarComboBox(timeUnits);
		//timeUnitChooser.addActionListener(timeActionListener);
		timeUnitChooser.setMaximumWidth(80);
		this.add(timeUnitChooser);
		
		
		beforeAfterChooser = new CalendarComboBox(beforeAfterOptions);
		//beforeAfterChooser.addActionListener(timeActionListener);
		beforeAfterChooser.setMaximumWidth(80);
		this.add(beforeAfterChooser);
		
		refreshAlarmTimeFromEvent();
	}
	
	
	public void refreshAlarmTimeFromEvent() {
		if (calendarEvent == null) 
			return;
		
		
		Calendar eventStart = calendarEvent.getStartCalendar();
		Calendar eventAlarm = calendarEvent.getAlarmCalendar();
		
		if ((eventStart != null) && (eventAlarm != null)) {
			int alarmSeconds = (int)(eventAlarm.getTimeInMillis() - eventStart.getTimeInMillis())/1000;
			setAlarmTime(alarmSeconds);
		}
		else {
			setAlarmTime(0);
		}
	}
	
	
	/** 
	 * Sets the display of the timeSpinner and timeUnitChooser to reflect the time in seconds.
	 * 
	 * @param seconds	Time in seconds. This should be equal to an integer of days, hours OR minutes.
	 */
	public void setAlarmTime(int secs) {
		
		if (secs == 0) {
			unitsField.setText("00");
			return;
		}
	
		
		if (secs < 0) {
			setBeforeAfterChooserIndex(BEFORE);
			secs = secs * -1;
		} else {
			setBeforeAfterChooserIndex(AFTER);
		}
		
		int timeUnit;
		int timeValue;
		
		// need to work out what the units are
		for (timeUnit = 0; timeUnit<timeUnits.length; timeUnit++) {
			timeValue = secs/secondsInTimeUnit[timeUnit];
			// if timeValue > 0 then this is the units you want
			if (timeValue > 0) {
				
				
				// if it is an exact match 
				if (secs == timeValue * secondsInTimeUnit[timeUnit]) {
					System.out.println("timeUnits: " + timeUnits[timeUnit] + " timeValue: " + timeValue);
					setTimeUnitChooserIndex(timeUnit);
					unitsField.setText(timeValue <10 ? "0" + timeValue : timeValue + "");
					return;
				} else {
				
				// if any seconds are left over (eg user sets 90 minutes or 36 hours)
					// try smaller units
					if (timeUnit < timeUnits.length -1) {
						timeUnit++;
						timeValue = secs/secondsInTimeUnit[timeUnit];
						setTimeUnitChooserIndex(timeUnit);
						unitsField.setText(timeValue <10 ? "0" + timeValue : timeValue + "");
						return;
					}
				
				}
				
			}
		}
	}
	
	public void setTimeUnitChooserIndex(int index) {
	//	timeUnitChooser.removeActionListener(timeActionListener);
		timeUnitChooser.setSelectedIndex(index);
	//	timeUnitChooser.addActionListener(timeActionListener);
	}
	
	public void setBeforeAfterChooserIndex(int index) {
	//	beforeAfterChooser.removeActionListener(timeActionListener);
		beforeAfterChooser.setSelectedIndex(index);
	//	beforeAfterChooser.addActionListener(timeActionListener);
	}
	
	/**
	 * Set enabled state of all components (unitsField, timeUnitChooser, beforeAfterChooser) 
	 */
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		System.out.println("AlarmEditor setEnabled: " + enabled);
		
		unitsField.setEnabled(enabled);
		timeUnitChooser.setEnabled(enabled);
		beforeAfterChooser.setEnabled(enabled);
		
		System.out.println("	 timeUnitChooser.isEnabled() " + timeUnitChooser.isEnabled());
	}
	
}

