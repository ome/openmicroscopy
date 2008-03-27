
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
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.swing.Icon;
import javax.swing.JOptionPane;

import omeroCal.model.AlarmChecker;
import omeroCal.model.CalendarEvent;
import omeroCal.model.CalendarObject;
import omeroCal.model.ICalendarDB;
import omeroCal.util.ImageFactory;
import omeroCal.view.IEventListener;
import ui.IModel;


public class AlarmActivatedListener 
	implements IEventListener {
	
	IModel editorModel;
	
	ICalendarDB calDB;

	public AlarmActivatedListener(ICalendarDB calDB, IModel editorModel) {
		
		this.calDB = calDB;
		
		this.editorModel = editorModel;
	}
	
	public void calendarEventChanged(CalendarEvent event, String propertyChanged, Object newProperty) {
		
		System.out.println("OpenFileListener  alarm_activated event...");
		
		if (propertyChanged.equals(AlarmChecker.ALARM_ACTIVATED)) {
			
			/*
			 * Show alarm, and option to open the file....
			 */
			
			Calendar now = new GregorianCalendar();
			
			// get details
			String eventName = event.getName();
			String timeString = "";
			
			Calendar eventTime = event.getStartCalendar();
			if (eventTime.get(Calendar.DAY_OF_MONTH) == now.get(Calendar.DAY_OF_MONTH)) {
				timeString = "Today";
			}
			else {
				SimpleDateFormat day = new SimpleDateFormat("EEEE");
				timeString = day.format(eventTime.getTime());
			}
			
			timeString = timeString + " at ";
			
			SimpleDateFormat hhmm = new SimpleDateFormat("HH:mm");
			timeString = timeString + hhmm.format(eventTime.getTime());
		
			
			// get the file info from the calendarFile in the DB. 
			int calendarID = event.getCalendarID();
			CalendarObject calendarFile = calDB.getCalendar(calendarID);
			
			String fileName = calendarFile.getName();
			String filePath = calendarFile.getInfo();
			
			
			String message = "<html><b>" + timeString + "</b><br>" +
			eventName + "</b><br>" +
			"from the file: " + fileName + "</b><br>" +
			filePath + "</html>";
			
			System.out.println("AlarmActivatedListener message: " + message);
			
			Object[] options = {"Open File", "Cancel"};
			
			// Fire the alarm!! 
			Icon alarmIcon = ImageFactory.getInstance().getIcon(ImageFactory.ALARM_GIF_64);
			
			int yesNo = JOptionPane.showOptionDialog(null, 
					message, "Omero.Editor Alarm", JOptionPane.YES_NO_OPTION,
					JOptionPane.INFORMATION_MESSAGE, alarmIcon, options, options[0]);
			
			if (yesNo == 0) {
				
				editorModel.openThisFile(new File(filePath));
			}
			
		}
		
	}
}
