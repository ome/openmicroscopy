
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

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.SocketException;
import java.util.List;

import omeroCal.model.CalendarEvent;
import omeroCal.model.CalendarObject;
import omeroCal.model.ICalendarModel;

import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Dur;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.ValidationException;
import net.fortuna.ical4j.model.component.VAlarm;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.parameter.Value;
import net.fortuna.ical4j.model.property.Action;
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.model.property.Version;
import net.fortuna.ical4j.util.UidGenerator;

public class ExportCalendar {
	
	public ExportCalendar(ICalendarModel model) {
	}
	
	public static void iCalendarExport(CalendarFile calObject, String exportFileName) {
		
		Calendar calendar = new Calendar();
		calendar.getProperties().add(new ProdId("-//OmeroEditor //iCal4j 1.0//EN"));
		//calendar.getProperties().add(new Uid("19960401T080045Z-4000F192713-0052@dundee.ac.uk"));
		calendar.getProperties().add(Version.VERSION_2_0);
		calendar.getProperties().add(CalScale.GREGORIAN);
		
		/*
		 * Some details from the calendar file
		 */
		String fileInfo = calObject.getInfo();
		String calName = calObject.getName().trim();
		String calDescription = calName + " " + fileInfo;
		
		
		List<CalendarEvent> events = calObject.getEvents();
		
		for (CalendarEvent calEvent : events) {
		//if (!events.isEmpty()) {
			//CalendarEvent calEvent = events.get(0);
		
			// get the values 
			String eventName = calEvent.getName();
			java.util.Calendar startTime = calEvent.getStartCalendar();
			DateTime start = new DateTime(startTime.getTime());
			
			boolean allDayEvent = calEvent.isAllDayEvent();
		
			// make a new event
			VEvent iCalendarEvt = null;
			
			if (allDayEvent) {
				iCalendarEvt = new VEvent(new Date(startTime.getTime()), eventName);
			} else {
				iCalendarEvt = new VEvent(start, eventName);
			}
			
			// add description
			iCalendarEvt.getProperties().add(new Description(calDescription));
			
			// add alarm if set
			java.util.Calendar alarmTime = calEvent.getAlarmCalendar();
			if (alarmTime != null) {
				int alarmSecs = (int)(alarmTime.getTimeInMillis() - startTime.getTimeInMillis())/1000;
				VAlarm vAlarm = new VAlarm(new Dur(0,0,0,alarmSecs));	// days, hrs, mins, secs
				vAlarm.getProperties().add(Action.DISPLAY);
				vAlarm.getProperties().add(new Description(eventName));
				
				iCalendarEvt.getAlarms().add(vAlarm);
			}
			
			// Generate a UID for the event..
			
			try {
				UidGenerator ug = new UidGenerator(startTime.getTimeInMillis() + "");
				iCalendarEvt.getProperties().add(ug.generateUid());
			} catch (SocketException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			// Add to the calendar
			calendar.getComponents().add(iCalendarEvt);
		}
		
		System.out.println("ExportCalendar  calendar.toString: " + calendar.toString());
		
		FileOutputStream fout;
		try {
			
			fout = new FileOutputStream(exportFileName);
			CalendarOutputter outputter = new CalendarOutputter();
			outputter.output(calendar, fout);
			
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ValidationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
