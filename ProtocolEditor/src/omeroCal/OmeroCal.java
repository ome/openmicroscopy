
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

package omeroCal;

import java.sql.SQLException;

import javax.swing.JOptionPane;

import omeroCal.model.AlarmChecker;
import omeroCal.model.CalendarDataBase;
import omeroCal.model.DBConnectionSingleton;
import omeroCal.model.ICalendarDB;
import omeroCal.model.ICalendarModel;
import omeroCal.model.CalendarModel;
import omeroCal.view.CalendarDisplay;
import omeroCal.view.Controller;

/**
 * This is the Main class of the OmeroCal package / application.
 * 
 * Calling the main() method instantiates this class, then opens the database and
 * displays the calendar. 
 * 
 * This class can also be instantiated without showing the UI.
 * This should occur when any application that uses this calendar starts-up.
 * Creating a new instance of this class opens a new database connection 
 * and starts the alarm checker.
 * These will be maintained for the whole time that the application is running.
 * When this or another application quits, the database should be shut down. 
 * 
 * @see openDataBaseConnection()
 * 
 * @author will
 *
 */

public class OmeroCal {
	
	/**
	 * A reference to the database interface used by the calendar. 
	 * This is the database manager that creates and executes SQL queries.
	 */
	ICalendarDB calDB;
	
	/**
	 * This is the Model that sits between the Database manager and the UI.
	 * Generally delegates data requests to the DB. 
	 */
	ICalendarModel calendarModel;
	
	/**
	 * The controller listens for events from the CalendarEvent labels and other UI components. 
	 * These are forwarded to other UI components or to the calendarModel as appropriate. 
	 */
	Controller controller;
	
	/**
	 * This is the main UI class. It holds the MonthView etc and the InfoPanel. 
	 * This class extends JPanel and is displayed in it's own JFrame. 
	 */
	CalendarDisplay calendarView;
	
	/**
	 * A class for checking and firing alarm events. 
	 * Gets alarm times from the DB, then periodically (every minute) checks 
	 * to see if alarm times have been reached.
	 */
	AlarmChecker alarmChecker;
	
	
	/**
	 * Creates an instance of this class. 
	 * Also sets up a database connection, instantiates the DB so that files can be added,
	 * and starts the alarm checker.  
	 */
	public OmeroCal() {
		
		openDataBaseConnection();
		
		calDB = new CalendarDataBase();
		
		//System.out.println("OmeroCal constructor...  calDB has been created");
		
		alarmChecker = new AlarmChecker();
	}
	
	/**
	 * main method for starting OmeroCal as a stand-alone application. 
	 * 
	 * Creates an instance of this class. Then opens the database and 
	 * displays the calendar in a JFrame. 
	 * 
	 * @param args
	 * @throws SQLException 
	 */
	public static void main(String[] args) {

		OmeroCal omeroCal = new OmeroCal();
		
		omeroCal.openDBAndDisplayUI(true);
	}
	
	
	/**
	 * This instantiates the DB connection required for calendar and alarm functions. 
	 * Throws an exception if another instance of this application is 
	 * running (will be trying to use the same DB). 
	 * 
	 * When this application quits, DBConnectionSingleton.shutDownConnection()
	 * should be called. This will be done under the quit dialog in ui.XMLView. 
	 */
	public void openDataBaseConnection() {
		
		try {
			DBConnectionSingleton.getConnection();
		} catch (SQLException ex) {
			
			int quit = JOptionPane.showConfirmDialog(null,
					"Failed to connect to the Calendar database. \n " +
					"This application may already be open. \n" +
					"Quit the startup now?",
					"Can't connect to database", JOptionPane.ERROR_MESSAGE);
			
			if (quit == JOptionPane.OK_OPTION) {
				System.exit(0);
			}
		};
		
	}

	
	public void openDBAndDisplayUI(boolean standAloneApplication) {
		
		calendarModel = new CalendarModel(calDB);
		
		controller = new Controller(calendarModel);
		
		calendarView = new CalendarDisplay(controller, standAloneApplication);
		
	}
	
	
	/**
	 * Allows other classes to reference the database
	 * @return
	 */
	public ICalendarDB getCalendarDataBase() {
		return calDB;
	}
	
	
	/**
	 * Allows other classes to get the Controller, 
	 * eg. to add listeners for calendar events. 
	 * @return	 The Controller that processes events from the UI components. 
	 */
	public Controller getController() {
		return controller;
	}
	
	
	/**
	 * Allows other classes to get the alarmChecker,
	 * so they can add listeners for alarm events. 
	 * 
	 * @return	the AlarmChecker that is initiated at start-up, keeps checking for alarms..
	 */
	public AlarmChecker getAlarmChecker() {
		return alarmChecker;
	}
	
	
	/**
	 * Allows other classes to get the calendar display,
	 * so they can eg. setEnabled() etc. 
	 * 
	 * @return	the JPanel that is shown in the main content display window. 
	 */
	public CalendarDisplay getCalendarDisplay() {
		return calendarView;
	}
	
}