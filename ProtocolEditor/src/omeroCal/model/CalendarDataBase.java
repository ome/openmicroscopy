
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

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Observable;


import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


import tree.DataFieldConstants;
import util.XMLMethods;


public class CalendarDataBase 
	extends Observable 
	implements ICalendarDB {

	/**
	 * Database connection.
	 */
	//private Connection conn;
	

	public static SimpleDateFormat sqlDateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//	public static SimpleDateFormat sqlDateFormat = new SimpleDateFormat("yyyy-MM-dd");
	
	/**
	 * TABLE NAMES
	 */
	
	/**
	 *	Calendar Table stores calendars, such as "Home", "Work" etc...
	 *	Each has a name "calendarName" and description "calInfo"
	 */
	public static final String CALENDAR_TABLE = "calendar_table";
	
	/**
	 *  Event Table stores events that are represented on the calendar.
	 *  Each has a name, a start DateTime, an end DateTime, an alarm DateTime, and a calendar "calID"
	 */
	public static final String EVENT_TABLE = "event_table";
	
	/**
	 * COLUMN NAMES
	 */
	
	/**
	 * Both tables have a column called UID. Unique, automatically incremented ID. 
	 */
	public static final String UID = "uID";
	
	/**
	 * CALENDAR_TABLE columns
	 */
	
	/**
	 * The Name of a calendar. eg "Home" or "Work"
	 */
	public static final String CAL_NAME = "calendarName";
	
	/**
	 * A Description of a calendar. eg Used as a link or mouse-over. 
	 */
	public static final String CAL_INFO = "calInfo";
	
	/**
	 * The color used to represent this calendar in the UI. Stored as Integer, as returned by color.getRGB()
	 */
	public static final String CAL_COLOUR = "calColour";
	
	/**
	 * For display purposes, so that the user can show and hide calendars
	 */
	public static final String CAL_VISIBLE = "calVisible";
	
	/**
	 * EVENT_TABLE columns
	 */
	
	/**
	 * An ID (BIGINT) referring to a calendar, to which this event belongs.
	 * Matches the uID of the calendar table.
	 */
	public static final String CAL_ID = "calID";
	
	/**
	 * The name of an event. eg "Mum's Birthday"
	 */
	public static final String EVENT_NAME = "eventName";
	
	/**
	 * The start of an event. DateTime.
	 */
	public static final String START_DATE = "startDate";
	
	/**
	 * The end of an event. DateTime.
	 */
	public static final String END_DATE = "endDate";
	
	/**
	 * The DateTime of an alarm for this event.
	 */
	public static final String ALARM_DATE = "alarmDate";
	
	/**
	 * A boolean to indicate that an event occurs on a day (or days), rather than at a particular time
	 */
	public static final String ALL_DAY_EVENT = "allDayEvent";
	
	
	
	/** 
	 * Create an instance of this class.
	 * Tries to connect to the database and create tables.
	 */
	public CalendarDataBase() {
		
		// try to create tables 
		createTables();
	}
	

	public void createTables() {
		
		// create the calendar table
		try {
			update( "CREATE TABLE " + CALENDAR_TABLE + " ( " +
				UID + " INT IDENTITY, " +
				CAL_NAME + " VARCHAR(128), " +
				CAL_INFO + " VARCHAR(256), " +
				CAL_COLOUR + " INT, " +
				CAL_VISIBLE + " BOOLEAN " +
				")" );
			
			// create the event table
			update( "CREATE TABLE " + EVENT_TABLE + " ( " +
					UID + " INT IDENTITY, " +
					CAL_ID + " BIGINT, " +
					EVENT_NAME + " VARCHAR(128), " +
					START_DATE + " DATETIME, " +
					END_DATE + " DATETIME, " +
					ALARM_DATE + " DATETIME, " +
					ALL_DAY_EVENT + " BOOLEAN " +
					")" );
		} catch (SQLException e) {
			//ignore SQL error if table already exists
			System.out.println("Error creating tables in the database (Tables may already exist): " + e.getMessage());
			// e.printStackTrace();
		}
		
	}
	
	/**
	 * Exact copy from Brian's ome.formats.importer.HistoryDB.java
	 * use for SQL commands CREATE, DROP, INSERT and UPDATE
	 */
	public synchronized int update(String expression) throws SQLException {
	
		Connection conn = DBConnectionSingleton.getConnection();
	
		Statement st = null;
	
		st = conn.createStatement();    // statements
	
		int i = st.executeUpdate(expression);    // run the query
	
		if (i == -1) {
			System.out.println("db error : " + expression);
		}
	
		st.close();
		return i;
	} // Update()
	
	
	/**
	 * Exact copy from Brian's ome.formats.importer.HistoryDB.java
	 */
	public synchronized ResultSet getQueryResults(String expression) throws SQLException {
		
		Connection conn = DBConnectionSingleton.getConnection();
		
		Statement st = null;
		ResultSet rs = null;
		
		//System.err.println(expression);
		st = conn.createStatement();         
		
		// statement objects can be reused with
		// repeated calls to execute but we
		// choose to make a new one each time
		
		return st.executeQuery(expression);    // run the query
		}   
	
	/**
	 * Exact copy from Brian's ome.formats.importer.HistoryDB.java
	 */
	public synchronized ResultSet getGeneratedKeys() throws SQLException {
		
		Connection conn = DBConnectionSingleton.getConnection();
		Statement st = null;
		
		st = conn.createStatement();    // statements
		
		String sql = "CALL IDENTITY()";
		
		PreparedStatement ps = conn.prepareCall(sql);
		
		ResultSet rs = st.executeQuery(sql);
		
		st.close();
	return rs;
	}

	/**
	 * Exact copy from Brian's ome.formats.importer.HistoryDB.java
	 */
	public int getLastKey() throws SQLException {
		ResultSet rs = getGeneratedKeys();
		rs.next();
		return rs.getInt(1);
	}
	
	
	
	/**
	 * Adds CalendarFile as a new entry in Table CALENDAR_TABLE
	 * Then adds the CalendarEvents as entries in EVENT_TABLE, with fileID linking to FILE_TABLE
	 * 
	 * @param calendarFile		The CalendarFile to add to the DB.
	 */
	public int saveCalendar(CalendarObject calendar) {
		
		String calendarInfo = calendar.getInfo();
    	String calendarName = calendar.getName();
    	int calendarColour = calendar.getColourInt();
    	boolean calendarVisible = calendar.isVisible();
    	
    	System.out.println("Saving CalendarObject : " + calendarName + " to database");
    	
    	String query = "INSERT INTO " + CALENDAR_TABLE + 
			" ( " + CAL_NAME + ", " + CAL_INFO + ", " + CAL_COLOUR + ", " + CAL_VISIBLE + " ) " +
			"VALUES (" + 
			"'" + calendarName + "', " +
			"'" + calendarInfo + "', " +
			calendarColour + ", " +
			calendarVisible + 
			" )";
    	
    	System.out.println();
    	System.out.println(query);
    	System.out.println();
    	
    	try {
    		
			update(query);
			
			int lastKey = getLastKey();
			
			System.out.println("Calendar added to DB. LastKey = " + lastKey);
			
			return lastKey;
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			System.out.println("SQLException: Calendar failed to add calendar " + e.getMessage());
			e.printStackTrace();
			
			return 0;
		}
		
	}
	
	/**
	 * Add a CalendarEvent to the database, and associate it with a calendar, identified by ID
	 * 
	 * @param calEvt		The CalendarEvent object to add to the database	
	 * @param calendar_ID	The unique_ID for the calendar to which this event should be added
	 */
	public void saveEvent(CalendarEvent calEvt, int calendar_ID) {
		
		
		String eventName = calEvt.getName();
		Date startDate = calEvt.getStartTime();
		Date endDate = calEvt.getEndTime();
		Date alarmTime = calEvt.getAlarmTime();
		boolean allDayEvent = calEvt.isAllDayEvent();
			
		System.out.println("Adding Event: " + eventName + " to DB.");
			
		String query = "INSERT INTO " + EVENT_TABLE + 
				"( " + CAL_ID + ", " + EVENT_NAME + ", " + START_DATE + ", " + END_DATE + ", " + ALARM_DATE + ", " + ALL_DAY_EVENT + " )" + 
				" VALUES (" +
				calendar_ID + ", " +
				formatStringForSQLQuery(eventName) + " , " +
				formatDateForSQLQuery(startDate) + " , " +
				formatDateForSQLQuery(endDate) + " , " +
				formatDateForSQLQuery(alarmTime) + " , " +
				allDayEvent +
				")" ;
			
		System.out.println();
	    System.out.println(query);
	    System.out.println();	
				
		try {
			update(query);
						
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	/**
	 * Updates a CalendarEvent which is already in the database. 
	 * This will replace all the values in the corresponding row of the Events table
	 * with values from the calendarEvent. 
	 * The correct row in the database is identified by the UID attribute returned by
	 * calendarEvent.getUID();
	 * If no entries are modified by this method, this method returns false. 
	 * 
	 * @param calendarEvent		The calendar 
	 * @return				If the update is successful.
	 */
	public boolean updateEvent(CalendarEvent calendarEvent) {
		
		int uID = calendarEvent.getUID();
		int calendarID = calendarEvent.getCalendarID();
		String eventName = calendarEvent.getName();
		Date startDate = calendarEvent.getStartTime();
		Date endDate = calendarEvent.getEndTime();
		Date alarmDate = calendarEvent.getAlarmTime();
		boolean allDayEvent = calendarEvent.isAllDayEvent();
			
		System.out.println("Updating Event: " + eventName + " to DB.");
			
		String query = "UPDATE " + EVENT_TABLE + " " +
				"SET " + 
				CAL_ID + " = " + calendarID + ", " +
				EVENT_NAME + " = " + formatStringForSQLQuery(eventName) + ", " +
				START_DATE + " = " + formatDateForSQLQuery(startDate) + ", " +
				END_DATE + " = " + formatDateForSQLQuery(endDate) + ", " +
				ALARM_DATE + " = " + formatDateForSQLQuery(alarmDate) + ", " +
				ALL_DAY_EVENT + " = " + allDayEvent + " " +
				"WHERE " +
				UID + " = " + uID ;
				
			
		System.out.println();
	    System.out.println(query);
	    System.out.println();	
				
		try {
			int rowsAffected = update(query);
			
			if (rowsAffected > 0) 
				return true;
			else 
				return false;
						
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}

	}
	
	
	/** 
	 * Convenience method for converting Date to formatted " 'string' " 
	 * with single quotation marks, for use in SQL statement. 
	 * Returns "null" if date == null
	 * 
	 * @param date	The Date object
	 * @return		A String representing the " 'date' " (for adding to DB), or "null" if date is null
	 */
	public static String formatDateForSQLQuery(Date date) {
		
		if(date != null) {
			return "'" + sqlDateTimeFormat.format(date) + "'";
		} else {
			return "null";
		}
	}
	
	/** 
	 * Convenience method for converting String to " 'string' " 
	 * with single quotation marks, for use in SQL statement. 
	 * Returns "null" if date == null
	 * Important not to return " 'null' " or the string "null" will get stored in DB, instead of a null value.
	 * 
	 * @param date	The Date object
	 * @return		A String representing the " 'date' " (for adding to DB), or "null" if date is null
	 */
	public static String formatStringForSQLQuery(String string) {
		
		if(string != null) {
			return "'" + string + "'";
		} else {
			return "null";
		}
	}
	
	
	
	
	/**
	 * Get all the events for a specified Calendar ID
	 * 
	 * @param calendarID
	 * @return	A List of the CalendarEvents that belong to the calendar identified by the ID
	 */
	public List<CalendarEvent> getEvents (int calendarID) {
		
		ArrayList<CalendarEvent> calendarEvents = new ArrayList<CalendarEvent>();
		
		String query = "SELECT " +
			EVENT_TABLE + "." + UID + " AS " + UID + ", " +
			EVENT_TABLE + "." + CAL_ID + " AS " + CAL_ID + ", " +
			EVENT_TABLE + "." + EVENT_NAME + " AS " + EVENT_NAME + ", " +
			EVENT_TABLE + "." + START_DATE + " AS " + START_DATE + ", " +
			EVENT_TABLE + "." + END_DATE + " AS " + END_DATE + ", " +
			EVENT_TABLE + "." + ALARM_DATE + " AS " + ALARM_DATE + ", " +
			EVENT_TABLE + "." + ALL_DAY_EVENT + " AS " + ALL_DAY_EVENT + ", " +
			CALENDAR_TABLE + "." + CAL_COLOUR + " AS " + CAL_COLOUR + " " +
			"FROM " + EVENT_TABLE + " , " + CALENDAR_TABLE + " " +
			"WHERE " + 
			CALENDAR_TABLE + "." + UID + "=" + calendarID + " AND " + 
			EVENT_TABLE + "." + CAL_ID + "=" + calendarID;
		
		System.out.println();
		System.out.println(query);
		System.out.println();
		
		try {
			ResultSet calFileResult = getQueryResults(query);
			
			//printResultSet(calFileResult, 2);
			
			while(calFileResult.next()) {
				
				int uID = calFileResult.getInt(UID);
				int cal_ID = calFileResult.getInt(CAL_ID);
				String eventName = null;
				if (calFileResult.getObject(EVENT_NAME) != null)
					eventName = calFileResult.getObject(EVENT_NAME).toString();
				Date startDate = calFileResult.getTimestamp(START_DATE);
				Date endDate = calFileResult.getTimestamp(END_DATE);
				Date alarmDate = calFileResult.getTimestamp(ALARM_DATE);
				boolean allDayEvent = calFileResult.getBoolean(ALL_DAY_EVENT);
				int colorInt = calFileResult.getInt(CAL_COLOUR);
				
				System.out.println("Event from DB: eventName: " + eventName + 
						" uID: " + uID +
						" cal_ID " + cal_ID + 
						" startDate: " + formatDateForSQLQuery(startDate) +
						" endDate: " + formatDateForSQLQuery(endDate) +
						" alarmDate: " + formatDateForSQLQuery(alarmDate) +
						" allDayEvent: " + allDayEvent +
						" calColour: " + colorInt);
					
				System.out.println();
				
				CalendarEvent calEvent = new CalendarEvent(eventName, startDate);
				calEvent.setUID(uID);
				calEvent.setCalendarID(cal_ID);
				calEvent.setEndTime(endDate);
				calEvent.setAlarmTime(alarmDate);
				calEvent.setAllDayEvent(allDayEvent);
				calEvent.setCalendarColour(colorInt);
				
				calendarEvents.add(calEvent);
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			System.out.println("CalendarDataBase SQLException " + query);
			e.printStackTrace();
		}
		
		return calendarEvents;
	}
	
	/**
	 * Gets a list of CalendarEvents for a given time period.
	 * Events are returned if they start before the end date, or end after the start date. 
	 * 
	 * @param   fromThisDate
	 * @param	toThisDate
	 * @return			A List of events 
	 */
	public List<CalendarEvent> getEventsForDates(Calendar fromThisDate, Calendar toThisDate) {
		
		ArrayList<CalendarEvent> calendarEvents = new ArrayList<CalendarEvent>();
		
		if ((fromThisDate == null) || (toThisDate == null)) {
			System.err.println("CalendarDataBase.getEventsForMonth() yearMonth == null");
			return calendarEvents;
		}
		
		String fromDate = sqlDateTimeFormat.format(fromThisDate.getTime());
		String toDate = sqlDateTimeFormat.format(toThisDate.getTime());
		
		String query = "SELECT " + EVENT_TABLE + "." + UID + " AS " + UID + ", " +
		EVENT_TABLE + "." + CAL_ID + " AS " + CAL_ID + ", " +
		EVENT_TABLE + "." + EVENT_NAME + " AS " + EVENT_NAME + ", " +
		EVENT_TABLE + "." + START_DATE + " AS " + START_DATE + ", " +
		EVENT_TABLE + "." + END_DATE + " AS " + END_DATE + ", " +
		EVENT_TABLE + "." + ALARM_DATE + " AS " + ALARM_DATE + ", " +
		EVENT_TABLE + "." + ALL_DAY_EVENT + " AS " + ALL_DAY_EVENT + ", " +
		CALENDAR_TABLE + "." + CAL_COLOUR + " AS " + CAL_COLOUR + " " +
		"FROM " + EVENT_TABLE + " , " + CALENDAR_TABLE + " " +
		"WHERE " + 
		EVENT_TABLE + "." + END_DATE + " >= '" + fromDate + "' " +
		 "AND " + 
		 EVENT_TABLE + "." + START_DATE + " <= '" + toDate + "' " +
		 "AND " +
		 CALENDAR_TABLE + "." + UID + "=" + 
			EVENT_TABLE + "." + CAL_ID ;
	
		System.out.println();
		System.out.println(query);
		System.out.println();
	
		try {
			ResultSet calFileResult = getQueryResults(query);
		
			//printResultSet(calFileResult, 2);
		
			while(calFileResult.next()) {
			
				int uID = calFileResult.getInt(UID);
				int cal_ID = calFileResult.getInt(CAL_ID);
				String eventName = null;
				if (calFileResult.getObject(EVENT_NAME) != null)
					eventName = calFileResult.getObject(EVENT_NAME).toString();
				Date startDate = calFileResult.getTimestamp(START_DATE);
				Date endDate = calFileResult.getTimestamp(END_DATE);
				Date alarmDate = calFileResult.getTimestamp(ALARM_DATE);
				boolean allDayEvent = calFileResult.getBoolean(ALL_DAY_EVENT);
				int colorInt = calFileResult.getInt(CAL_COLOUR);
			
				System.out.println("Event from DB: eventName: " + eventName + 
					" uID: " + uID +
					" cal_ID " + cal_ID + 
					" startDate: " + formatDateForSQLQuery(startDate) +
					" endDate: " + formatDateForSQLQuery(endDate) +
					" alarmDate: " + formatDateForSQLQuery(alarmDate) +
					" allDayEvent: " + allDayEvent +
					" calColour: " + colorInt);
					
				System.out.println();
			
				CalendarEvent calEvent = new CalendarEvent(eventName, startDate);
				calEvent.setUID(uID);
				calEvent.setCalendarID(cal_ID);
				calEvent.setEndTime(endDate);
				calEvent.setAlarmTime(alarmDate);
				calEvent.setAllDayEvent(allDayEvent);
				calEvent.setCalendarColour(colorInt);
			
				calendarEvents.add(calEvent);
			}
		
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			System.out.println("CalendarDataBase SQLException " + query);
			e.printStackTrace();
		}
		return calendarEvents;
	}
	
	
	/**
	 * Gets a list of CalendarEvents with alarm times that fall between 2 dates
	 * Events are returned if alarm time is before the end date, or after the start date.
	 * If fromThisDate is null, it will be set to the current time.
	 * 
	 * @param fromThisDate		Look for events with alarm times after this date-time
	 * @param toThisDate		Look for events with alarm times before this date-time
	 * @return			A List of events as CalendarEvent objects.
	 */
	public List<CalendarEvent> getEventsForAlarmTimes(Calendar fromThisTime, Calendar toThisTime) {
		
		ArrayList<CalendarEvent> calendarEvents = new ArrayList<CalendarEvent>();
		
		/*
		 * If no fromTime has been set, assume it is now. 
		 */
		if (fromThisTime == null) {
			fromThisTime.setTime(new Date());
		}

		if (toThisTime == null) {
			System.err.println("CalendarDataBase.getEventsForAlarmTimes() toThisTime == null");
			return calendarEvents;
		}
		
		String fromDate = sqlDateTimeFormat.format(fromThisTime.getTime());
		String toDate = sqlDateTimeFormat.format(toThisTime.getTime());
		
		String query = "SELECT " + 
		UID + ", " +
		CAL_ID + ", " +
		EVENT_NAME + ", " +
		START_DATE + ", " +
		END_DATE + ", " +
		ALARM_DATE + ", " +
		ALL_DAY_EVENT + " " +
		"FROM " + EVENT_TABLE + " " +
		"WHERE " + 
		ALARM_DATE + " >= '" + fromDate + "' " +
		 "AND " + 
		 ALARM_DATE + " <= '" + toDate + "' ";
	
		System.out.println();
		System.out.println(query);
		System.out.println();
	
		try {
			ResultSet calFileResult = getQueryResults(query);
		
			//printResultSet(calFileResult, 2);
		
			while(calFileResult.next()) {
			
				int uID = calFileResult.getInt(UID);
				int cal_ID = calFileResult.getInt(CAL_ID);
				String eventName = null;
				if (calFileResult.getObject(EVENT_NAME) != null)
					eventName = calFileResult.getObject(EVENT_NAME).toString();
				Date startDate = calFileResult.getTimestamp(START_DATE);
				Date endDate = calFileResult.getTimestamp(END_DATE);
				Date alarmDate = calFileResult.getTimestamp(ALARM_DATE);
				boolean allDayEvent = calFileResult.getBoolean(ALL_DAY_EVENT);
			
				System.out.println("Event from DB: eventName: " + eventName + 
					" uID: " + uID +
					" cal_ID " + cal_ID + 
					" startDate: " + formatDateForSQLQuery(startDate) +
					" endDate: " + formatDateForSQLQuery(endDate) +
					" alarmDate: " + formatDateForSQLQuery(alarmDate) +
					" allDayEvent: " + allDayEvent);
					
				System.out.println();
			
				CalendarEvent calEvent = new CalendarEvent(eventName, startDate);
				calEvent.setUID(uID);
				calEvent.setCalendarID(cal_ID);
				calEvent.setEndTime(endDate);
				calEvent.setAlarmTime(alarmDate);
				calEvent.setAllDayEvent(allDayEvent);
			
				calendarEvents.add(calEvent);
			}
		
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			System.out.println("CalendarDataBase SQLException " + query);
			e.printStackTrace();
		}
		return calendarEvents;
	}
	
	
	/**
	 * Simply gets the CalendarObject specified by its unique ID from the calendar table
	 * 
	 * @param calID		The unique ID used to identify a calendar
	 * @return		A CalendarObject that contains all the info from that row of the DB (or null if not found)
	 */
	public CalendarObject getCalendar(int calID) {
		
		CalendarObject calendarObject = new CalendarObject();
		
		String query = "SELECT " + 
		UID + ", " +
		CAL_NAME + ", " +
		CAL_INFO + ", " +
		CAL_COLOUR + ", " +
		CAL_VISIBLE + " " +
		"FROM " + CALENDAR_TABLE + " " +
		"WHERE " + 
		UID + "=" + calID ;
	
		System.out.println();
		System.out.println(query);
		System.out.println();
		
		try {
			ResultSet calFileResult = getQueryResults(query);
		
			if (calFileResult.next()) {
			
				int uID = calFileResult.getInt(UID);
				String calendarName = null;
				if (calFileResult.getObject(CAL_NAME) != null)
					calendarName = calFileResult.getObject(CAL_NAME).toString();
				String calendarInfo = null;
				if (calFileResult.getObject(CAL_INFO) != null)
					calendarInfo = calFileResult.getObject(CAL_INFO).toString();
				int colorInt = calFileResult.getInt(CAL_COLOUR);
				boolean calVisible = calFileResult.getBoolean(CAL_VISIBLE);
			
				System.out.println("Calendar from DB: calendarName: " + calendarName + 
					" uID: " + uID +
					" calendarInfo: " + calendarInfo +
					" calColour: " + colorInt +
					" calVisible: " + calVisible);
					
				System.out.println();
			
				calendarObject.setCalendarID(uID);
				calendarObject.setName(calendarName);
				calendarObject.setInfo(calendarInfo);
				calendarObject.setColour(colorInt);
				calendarObject.setVisibile(calVisible);
			
			}
		
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			System.out.println("CalendarDataBase SQLException " + query);
			e.printStackTrace();
			
			return null;
		}
		return calendarObject;
		
	}
	
	
	public void printResultSet(ResultSet resultSet, int cols) {
		
		System.out.println("printResultSet()....");
		
		try {
			while(resultSet.next()) {
				System.out.println("row: " + resultSet.getRow());
				
				for (int col=1; col<=cols; col++) {
					System.out.println("    col" + col + " = " + resultSet.getObject(col).toString());
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void clearTables() {
		try {
			update("DELETE FROM " + CALENDAR_TABLE);
			update("DELETE FROM " + EVENT_TABLE);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			System.out.println("SQLException query = " + "DELETE * FROM " + CALENDAR_TABLE);
			e.printStackTrace();
		}
	}
}
