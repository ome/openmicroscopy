
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

import omeroCal.model.CalendarEvent;
import omeroCal.model.CalendarObject;
import omeroCal.model.ICalendarDB;
import omeroCal.view.EventLabel;
import omeroCal.view.IEventListener;
import ui.IModel;

/**
 * This event listener can be added to the Controller to listener for double-clicks from 
 * EventLabels displayed on the calendar. 
 * In response to a calendarChangedEvent with the property DOUBLE_CLICKED, this class will
 * open a file specified by the String calendarFile.getInfo()
 * 
 * @author will
 *
 */
public class OpenFileListener 
	implements IEventListener {
	
	IModel editorModel;
	
	ICalendarDB calDB;

	public OpenFileListener(ICalendarDB calDB, IModel editorModel) {
		
		this.calDB = calDB;
		
		this.editorModel = editorModel;
	}
	
	public void calendarEventChanged(CalendarEvent calendarEvent, String propertyChanged, Object newProperty) {
		
		if (propertyChanged.equals(EventLabel.DOUBLE_CLICKED)) {
			
			System.out.println("OpenFileListener  double-click event...");
			
			// get the file info from the calendarFile in the DB. 
			int calendarID = calendarEvent.getCalendarID();
			CalendarObject calendarFile = calDB.getCalendar(calendarID);
			
			String filePath = calendarFile.getInfo();
			
			editorModel.openThisFile(new File(filePath));
		}
		
	}

}
