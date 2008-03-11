
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
import java.util.GregorianCalendar;

import ui.components.FileChooserReturnFile;

public class Calendar {
	
	/**
	 * For testing.
	 * @param args
	 */
	public static void main(String[] args) {
		
		//populateDB();
		
		// Create a file chooser, get a file from user
		FileChooserReturnFile fc = new FileChooserReturnFile(new String[] {"xml"}, null);
		File file = fc.getFileFromUser();
		
		if (file == null)
			return;
		
		CalendarDataBase calDB = new CalendarDataBase();
		//calDB.clearTables();
		calDB.indexFilesToCalendar(file);
		
		
		
		/*
		GregorianCalendar thisMonth = new GregorianCalendar();
		thisMonth.set(GregorianCalendar.DAY_OF_MONTH, 1);
		thisMonth.set(GregorianCalendar.HOUR_OF_DAY, 0);
		
		calDB.getCalendarFilesForMonth(thisMonth);
		*/
		
	}
	
	public static void populateDB() {
		// Create a file chooser, get a file from user
		FileChooserReturnFile fc = new FileChooserReturnFile(new String[] {"xml"}, null);
		File file = fc.getFileFromUser();
		
		if (file == null)
			return;
		
		CalendarDataBase calDB = new CalendarDataBase();
		//calDB.clearTables();
		calDB.indexFilesToCalendar(file);
	}
	
	

}
