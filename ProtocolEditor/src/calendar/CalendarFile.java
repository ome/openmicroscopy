
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

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import omeroCal.model.CalendarEvent;
import omeroCal.model.CalendarObject;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import tree.DataFieldConstants;
import ui.components.FileChooserReturnFile;
import ui.components.TimeEditor;
import ui.fieldEditors.FieldEditorDate;
import ui.formFields.FormFieldDateTime;
import util.XMLMethods;

public class CalendarFile extends CalendarObject {
	
	

	/**
	 * A list of the date-time fields in the file
	 */
	ArrayList<CalendarEvent> scheduledDates;
	
	
	public CalendarFile(File xmlExperimentFile) {
		
		setInfo(xmlExperimentFile.getAbsolutePath());
		
		scheduledDates = new ArrayList<CalendarEvent>();
		
		buildScheduledDates(xmlExperimentFile);
		
	}
	
	public CalendarFile(String filePath, String fileName) {
		super(fileName, filePath);
		
		scheduledDates = new ArrayList<CalendarEvent>();
	}
	
	
	public void addEvent(String eventName, Calendar eventDate) {
		scheduledDates.add(new CalendarEvent(eventName, eventDate));
	}
	
	public void addEvent(CalendarEvent event) {
		scheduledDates.add(event);
	}
	
	public void buildScheduledDates(File xmlExperimentFile) {
		
		try {
			Document domDoc = XMLMethods.readXMLtoDOM(xmlExperimentFile);
			NodeList dateTimeNodes = domDoc.getElementsByTagName("*");
			
			Element titleElement = domDoc.getDocumentElement();
			setName(titleElement.getAttribute(DataFieldConstants.ELEMENT_NAME));
			
			
			boolean fileContainsDate = false;
			
			GregorianCalendar gc = new GregorianCalendar();
			String eventName;
			
			for (int i=0; i<dateTimeNodes.getLength(); i++) {
				
				// Check whether a field is a DateTimeField
				if (dateTimeNodes.item(i).getNodeName().equals(DataFieldConstants.DATE_TIME_FIELD)) {
					
					Element dateTimeElement = (Element)dateTimeNodes.item(i);
					String millisecs = dateTimeElement.getAttribute(DataFieldConstants.UTC_MILLISECS);
					System.out.println("CalendarFile millisecs = " + millisecs);
					
					if (millisecs == null) {
						// no time is set, ignore this field
						continue;
					}
					
					// create a test calendar (see below).
					Calendar testForAbsoluteDate = new GregorianCalendar();
					testForAbsoluteDate.setTimeInMillis(new Long(millisecs));
					
					
					// First, need to know if this is an absolute Date (eg April 23rd 2008) or
					// if this is a relative date (eg 3 days later).
					// If it is a relative date, Year will be 1970 (epoch) since relative days will 
					// be in millisecs. 
					// If an absolute date, Year will be == 1970
					
					int year = testForAbsoluteDate.get(Calendar.YEAR);
					if (year != 1970) {
						// create a new calendar (don't want to change time of calendars added to list).
						gc = new GregorianCalendar();
						gc.setTimeInMillis(new Long(millisecs));
				
						// you have at least one date in this file
						fileContainsDate = true;
						
					} else 
					// this field is not an absolute date... must be relative date...
					// only interested if you know that absolute dates already exist in this file
					if (fileContainsDate) {
						
						// get the relative days
						try {
							int days = FormFieldDateTime.convertMillisecsToDays(testForAbsoluteDate.getTimeInMillis());
							System.out.println(" days = " + days);
							
							// take the last date for gc (updated for the last DATE_TIME_FIELD or DATE_FIELD),
							Date previousDate = gc.getTime();
							gc = new GregorianCalendar();	// don't want to change existing date in list
							gc.setTime(previousDate);
							
							// set the hours, mins, secs to 0.  (Only interested in the date)
							gc.set(Calendar.HOUR_OF_DAY, 0);
							gc.set(Calendar.MINUTE, 0);
							gc.set(Calendar.SECOND, 0);
							
							System.out.println("Relative date before adding days : " 
									+ CalendarTestCode.dateFormat.format(gc.getTime()) + " " +
									 CalendarTestCode.timeFormat.format(gc.getTime()));
							
							
							// now increment the number of days
							gc.add(Calendar.DAY_OF_MONTH, days);
							
							
							System.out.println("Relative date after adding days : " 
									+ CalendarTestCode.dateFormat.format(gc.getTime()) + " " +
									 CalendarTestCode.timeFormat.format(gc.getTime()));
							
						} catch (NumberFormatException ex) {
							// millisecs could not be converted to integer. 
							// Ignore - just don't add to calendar
							continue;
						}
					}
					
					// by this point, you have the date of the new Event... 
					
					// need a name
					eventName = dateTimeElement.getAttribute(DataFieldConstants.ELEMENT_NAME).trim();
					
					// Create a new Event
					CalendarEvent newEvent = new CalendarEvent(eventName, gc);
					
					
					// This date has time 00:00 unless time has been set...
					String time = dateTimeElement.getAttribute(DataFieldConstants.SECONDS);
					if ((time != null) && (time.length() > 0)) {
						int secs = Integer.parseInt(time);
						gc.add(Calendar.SECOND, secs);	// this will add hours and minutes (in secs)
						newEvent.setAllDayEvent(false);
						
						System.out.println("Date, with time : " 
								+ CalendarTestCode.dateFormat.format(gc.getTime()) + " " +
								 CalendarTestCode.timeFormat.format(gc.getTime()));
					}
				
					
					System.out.println("CalendarFile " + eventName + ": " 
							+ CalendarTestCode.dateFormat.format(gc.getTime()) + " " +
							 CalendarTestCode.timeFormat.format(gc.getTime()));
					
					
					// Check to see if alarm has been set. If not attribute is null, string returns ""
					String alarm = dateTimeElement.getAttribute(DataFieldConstants.ALARM_SECONDS);
					if ((alarm != null) && (alarm.length() > 0)){
						System.out.println("alarmSeconds = " + alarm);
						int alarmSecs = Integer.parseInt(alarm);
						System.out.println("alarmSeconds = " + alarmSecs);
						// create an alarm time ...
						Calendar alarmTime = new GregorianCalendar();
						alarmTime.setTime(gc.getTime());
						// ... which is the event time +/- the alarm time 
						// (alarm time will be -ve if it is before the event)
						alarmTime.add(Calendar.SECOND, alarmSecs);
						
						
						System.out.println("alarmTime is " 
								+ CalendarTestCode.dateFormat.format(alarmTime.getTime()) + " " +
								 CalendarTestCode.timeFormat.format(alarmTime.getTime()));
						
						newEvent.setAlarmTime(alarmTime.getTime());
					}
				
					
					scheduledDates.add(newEvent);
					
					
					// just to make sure that fileName gets assigned to something (ie if it didn't get name from root)
					if (getName() == null)
						setName(eventName);


				} else
				
					// Check for the deprecated DateField, for older files. 
				if (dateTimeNodes.item(i).getNodeName().equals(DataFieldConstants.DATE)) {
					
					Element dateTimeElement = (Element)dateTimeNodes.item(i);
					
					// DateField elements store date as a String "Dec 3, 2007" in "value" attribute..
					String formattedDate = dateTimeElement.getAttribute(DataFieldConstants.VALUE);
					
					Date date = null;
					if ((formattedDate != null) && (formattedDate.length() > 0)) {
						date = FieldEditorDate.getDateFromString(formattedDate);
					}
					
					// create a new calendar (don't want to change time of calendars added to list).
					gc = new GregorianCalendar();
					if (date != null)
						gc.setTime(date);
					else {
						// System.out.println("DATE == null.  formattedDate.length() = " + formattedDate.length());
						// if no date was set, ignore this DateField
						continue;
					}
			
					fileContainsDate = true;
					eventName = dateTimeElement.getAttribute(DataFieldConstants.ELEMENT_NAME);
					//System.out.println("CalendarFile " + eventName + ": " + dateFormat.format(gc.getTime()));
					
					scheduledDates.add(new CalendarEvent(eventName, gc));
					
					// just to make sure that fileName gets assigned to something (ie if it didn't get name from root)
					if (getName() == null)
						setName(eventName);
				} 
				
			}
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void printDatesList() {
		
		for (CalendarEvent event: scheduledDates) {
			//Date eventTime = event.getTime();
			
			// String time = timeFormat.format(eventTime);
			// String date = dateFormat.format(eventTime);
			String name = event.getName();
			
			// System.out.println(name + ": " + date + ", " + time);
		}
	}
	
	/**
	 * For testing.
	 * @param args
	 */
	public static void main(String[] args) {
		
		// Create a file chooser, get a file from user
		FileChooserReturnFile fc = new FileChooserReturnFile(new String[] {"xml"}, null);
		File file = fc.getFileFromUser();
		
		new CalendarFile(file).printDatesList();
	}
	
	public List<CalendarEvent> getEvents() {
		return scheduledDates;
	}
	
	/**
	 * Since this class uses the Info attribute to store the file path.
	 * This method makes this clearer.
	 * 
	 * @return
	 */
	public String getAbsoluteFilePath() {
		return getInfo();
	}
}
