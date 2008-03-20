
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

import java.sql.SQLException;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.swing.JFrame;

import omeroCal.model.CalendarDataBase;
import omeroCal.model.CalendarEvent;
import omeroCal.model.CalendarObject;
import omeroCal.model.DBConnectionSingleton;
import omeroCal.model.ICalendarDB;
import omeroCal.model.ICalendarModel;
import omeroCal.model.CalendarModel;
import omeroCal.view.MonthView;

/**
 * This is just somewhere to put various bits of code that test adding and querying the DB.
 * 
 * @author will
 *
 */
public class CalendarTestCode {

	public static DateFormat timeFormat = DateFormat.getTimeInstance (DateFormat.DEFAULT);
	public static DateFormat dateFormat = DateFormat.getDateInstance( DateFormat.DEFAULT);
	
	
	
	public static void main(String[] args) throws SQLException {
		
		updateEvent();
		
		//clearDBTables();
		
		// addEvent();
		
		//  testGetEvents();
		
		// testAddCalendar();
	}
	
	
	public static void updateEvent() throws SQLException {
		
		ICalendarDB calDB = new CalendarDataBase();
		
		CalendarEvent test = new CalendarEvent("test", new Date());
		test.setUID(0);
		test.setCalendarID(0);
		
		boolean success = calDB.updateEvent(test);
		
		System.out.println("CalendarTestCode updateEvent = " + success);
		
		
	}
	
	public static void clearDBTables() throws SQLException {
		
		CalendarDataBase calDB = new CalendarDataBase();
		calDB.clearTables();
		
		
	}
	
	public static void getMonthResults() throws SQLException{
		
		
		CalendarDataBase calDB = new CalendarDataBase();
		
		ICalendarModel monthModel = new CalendarModel(calDB);
		
		MonthView monthView = new MonthView(monthModel);
		
		JFrame frame = new JFrame("MonthView");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		frame.getContentPane().add(monthView);
		
		frame.pack();
		frame.setVisible(true);
		
		
	}
	
	
	public static void addEvent() throws SQLException {
		
		CalendarDataBase calDB = new CalendarDataBase();
		
		GregorianCalendar thisMonth = new GregorianCalendar();
		thisMonth.setTime(new Date());
		//thisMonth.add(Calendar.MONTH, 3);
		//thisMonth.add(Calendar.HOUR_OF_DAY, 2);
		
		System.out.println(CalendarDataBase.formatDateForSQLQuery(thisMonth.getTime()));
		
		
		CalendarEvent calEvent = new CalendarEvent("Drink G&T!", thisMonth);
		
		calDB.saveEvent(calEvent, 243);
		
		

	}
	
	public static void testGetEvents() throws SQLException {
		
		CalendarDataBase calDB = new CalendarDataBase();
		
		GregorianCalendar lastHour = new GregorianCalendar();
		lastHour.add(Calendar.DAY_OF_MONTH, -1);
		
		GregorianCalendar nextHour = new GregorianCalendar();
		nextHour.add(Calendar.DAY_OF_MONTH, +1);
		
		calDB.getEventsForDates(lastHour, nextHour);
		
		
		//calDB.getEvents(0);
		

	}
	
	
	public static void testAddCalendar() throws SQLException{
		
		CalendarObject calendar = new CalendarObject("Home", "I do stuff at home");
		
		CalendarDataBase calDB = new CalendarDataBase();
		
		int calID = calDB.saveCalendar(calendar);
		
		System.out.println("testAddCalendar ID = " + calID);
		
		
	}
}
