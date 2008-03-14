
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

import omeroCal.CalendarDataBase;
import omeroCal.CalendarEvent;
import omeroCal.CalendarObject;
import omeroCal.IMonthModel;
import omeroCal.MonthModel;
import omeroCal.MonthView;

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
		
		testGetEvents();
	}
	
	
	public static void clearDBTables() throws SQLException {
		
		CalendarDataBase calDB = new CalendarDataBase();
		calDB.clearTables();
		
		calDB.shutdown();
	}
	
	public static void getMonthResults() throws SQLException{
		
		
		CalendarDataBase calDB = new CalendarDataBase();
		
		IMonthModel monthModel = new MonthModel(calDB);
		
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
		thisMonth.add(Calendar.MONTH, 3);
		thisMonth.add(Calendar.HOUR_OF_DAY, 2);
		
		System.out.println(CalendarDataBase.formatDateForSQLQuery(thisMonth.getTime()));
		
		
		CalendarEvent calEvent = new CalendarEvent("Drink G&T!", thisMonth);
		
		calDB.saveEvent(calEvent, 3);
		
		calDB.shutdown();
	}
	
	public static void testGetEvents() throws SQLException {
		
		CalendarDataBase calDB = new CalendarDataBase();
		
		GregorianCalendar thisMonth = new GregorianCalendar();
		calDB.getEventsForMonth(thisMonth);
		
		//calDB.getEvents(94);
		
		calDB.shutdown();
	}
	
	
	public static void testAddCalendar() throws SQLException{
		
		CalendarObject calendar = new CalendarObject("Home", "I do stuff at home");
		
		CalendarDataBase calDB = new CalendarDataBase();
		
		int calID = calDB.saveCalendar(calendar);
		
		
		calDB.shutdown();
	}
}
