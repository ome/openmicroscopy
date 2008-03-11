
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
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import tree.DataFieldConstants;
import util.XMLMethods;


public class CalendarDataBase {

	/**
	 * Database connection.
	 */
	private Connection conn;
	
	public static SimpleDateFormat sqlDateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	public static SimpleDateFormat sqlDateFormat = new SimpleDateFormat("yyyy-MM-dd");
	
	public static final String FILE_TABLE = "file_table";
	public static final String EVENT_TABLE = "event_table";
	
	List<CalendarFile> calendarFiles;
	
	
	public CalendarDataBase() {
		
		calendarFiles = new ArrayList<CalendarFile>();
		
		// set up DB connection
		//connectToDB();
		
	}
	
	public void connectToDB() {
		String saveDirectory = System.getProperty("user.home") + File.separator + "omeroEditor";
		
		if (!new File(saveDirectory).exists()) {
           new File(saveDirectory).mkdir();
		}
		
	
		try {
			// Load the HSQL Database Engine JDBC driver
			Class.forName("org.hsqldb.jdbcDriver");
		 	       
			// Connect to the database
			System.out.println("Calendar: Trying to create database at " + saveDirectory + File.separator + "calendar");
			conn = DriverManager.getConnection(
					"jdbc:hsqldb:file:" + saveDirectory + File.separator + "calendar",  // filenames
					"sa",                   // userName
					"");                    // password
			
			// create the file table
			update( "CREATE TABLE " + FILE_TABLE + " ( " +
				"uID INT IDENTITY, " +
				"filePath VARCHAR(128), " +
				"fileName VARCHAR(128) " +
				" )" );
			
			// create the event table
			update( "CREATE TABLE " + EVENT_TABLE + " ( " +
					"uID INT IDENTITY, " +
					"fileID BIGINT, " +
					"eventName VARCHAR(128), " +
					"date DATETIME " +
					" )" );
			
			
		} catch (ClassNotFoundException e) {
			System.out.println("Couldn't find Class at org.hsqldb.jdbcDriver");
			e.printStackTrace();
		} catch (SQLException e) {
			//ignore SQL error if table already exists
			System.out.println("CalendarDataBase - Constructor SQLException " + e.getMessage());
			// e.printStackTrace();
		}
	}
	
	/**
	 * Exact copy from Brian's ome.formats.importer.HistoryDB.java
	 * use for SQL commands CREATE, DROP, INSERT and UPDATE
	 */
	public synchronized int update(String expression) throws SQLException {
	
		//System.err.println(expression);
	
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
	 * Iterate through a file system (starting at root directory), adding files to the calendar.
	 * For each file, check to see if it contains DateTime Fields. Needs to contain at least one DateTime
	 * field in order to be added to the list of calendarFiles. 
	 * 
	 * @param rootDirectory
	 */
	public void indexFilesToCalendar(File rootDirectory) {
		
		if (calendarFiles == null) {
			calendarFiles = new ArrayList<CalendarFile>();
		}
		
		if (rootDirectory.isDirectory()) {
	        String[] files = rootDirectory.list();
	        // an IO error could occur
	        if (files != null) {
	          for (int i = 0; i < files.length; i++) {
	        	  indexFilesToCalendar(new File(rootDirectory, files[i]));
	          }
	        }
	      } else {
	        if (fileContainsDates(rootDirectory)) {
	        	CalendarFile calendarFile = new CalendarFile(rootDirectory);
	        	
	        	// DB not fully working yet!
	        	//saveCalendarFileToDB(calendarFile);
	        	
	        	// use this for now! 
	        	calendarFiles.add(calendarFile);
	        }
	      }
	}
	
	/**
	 * Adds CalendarFile as a new entry in Table FILE_TABLE
	 * Then adds the CalendarEvents as entries in EVENT_TABLE, with fileID linking to FILE_TABLE
	 * 
	 * @param calendarFile		The CalendarFile to add to the DB.
	 */
	public void saveCalendarFileToDB(CalendarFile calendarFile) {
		
		String filePath = calendarFile.getCalendarFilePath();
    	String fileTitle = calendarFile.getCalendarFileTitle();
    	
    	System.out.println("Indexing CalendarFile: " + filePath + " Title: " + fileTitle);
    	
    	List<CalendarEvent> events = calendarFile.getEvents();
    	
    	try {
    		int fileID;
			update(
					"INSERT INTO " + FILE_TABLE + " (filePath, fileName) " +
					"VALUES(" +
					"'" + filePath + "', " +
					"'" + fileTitle + "'" +
					" )");
			
			// need to get the uID for the last row created
			ResultSet maxUID = getQueryResults("SELECT MAX(uID) FROM " + FILE_TABLE);
			if (maxUID.next()) {
				
				//System.out.println("maxUID returned at least one row! ");
				
				fileID = maxUID.getInt(1);
			
				System.out.println("      maxUID = " + fileID);
			
				for (CalendarEvent calEvt: events) {
				
					System.out.println("    Indexing Event: " + calEvt.getName() + ", fileID " + fileID + " " + sqlDateTimeFormat.format(calEvt.getTime()));
					update(
							"INSERT INTO " + EVENT_TABLE + "(fileID, eventName, date) " +
							"VALUES(" +
							fileID + ", " +
							"'" + calEvt.getName() + "', " +
							"'" + sqlDateTimeFormat.format(calEvt.getTime()) + "' " +
						")");
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			System.out.println("SQLException: Calendar failed to index file " + filePath);
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Test to see if a file contains at least one DateTimeField
	 * ie. Does the XML file contain at least one Element named "DateTimeField" (DataFieldConstants.DATE_TIME_FIELD).
	 * 
	 * @param file		The XML file
	 * @return			True if at least one Element <DateTimeField /> exists in the XML file
	 * 					returns false if file can't be read. 
	 */
	public static boolean fileContainsDates(File file) {
		
		// if it isn't an XML file, forget it!
		String fileName = file.getName();
		if (!(fileName.endsWith(".xml"))) {
			return false;
		}
		
		// Try to open XML file, and see if it has any Elements named <DateTimeField>
		try {
			Document domDoc = XMLMethods.readXMLtoDOM(file);
			NodeList dateTimeNodes = domDoc.getElementsByTagName(DataFieldConstants.DATE_TIME_FIELD);
			return (dateTimeNodes.getLength() > 0);
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		
	}
	
	public List<CalendarFile> getCalendarFilesForMonth(GregorianCalendar monthYear) {
		
		ArrayList<CalendarFile> calendarFiles = new ArrayList<CalendarFile>();
		
		String fromDate = sqlDateFormat.format(monthYear.getTime());
		monthYear.add(GregorianCalendar.MONTH, 1);
		String toDate = sqlDateFormat.format(monthYear.getTime());
		
		/*
		String query = "SELECT " + FILE_TABLE + ".filePath, " +
				FILE_TABLE + ".fileName " +
				"FROM " + FILE_TABLE + ", " + EVENT_TABLE + " " +
				"WHERE " + FILE_TABLE + ".uID=" + EVENT_TABLE + ".fileID";
				// "AND " + EVENT_TABLE + ".date >= '" + fromDate + "' " +
				// "AND " + EVENT_TABLE + ".date <= '" + toDate + "'";
		*/
		
		/*
		String query = "SELECT * FROM " + EVENT_TABLE + ", " + FILE_TABLE + " " +
			"WHERE " + FILE_TABLE + ".uID=" + EVENT_TABLE + ".fileID ";
		*/
		
		
		String query = "SELECT * FROM " + EVENT_TABLE;
		
		System.out.println();
		System.out.println(query);
		try {
			ResultSet calFileResult = getQueryResults(query);
			
			//calFileResult.setFetchDirection(ResultSet.FETCH_REVERSE);	// not supported!
			
			//if (calFileResult.first())	// can't!
			if (calFileResult.previous())
				System.out.println("CalendarDataBase query: previous() = true");
			
			// while still within the file result set
			while(calFileResult.next()) {
				String filePath = calFileResult.getObject(0).toString();
				String fileName = calFileResult.getObject(1).toString();
				// int fileID = calFileResult.getInt("fileID");
				//int fileID = calFileResult.getInt("uID");
				System.out.println("CalendarDataBase query: row " + calFileResult.getRow());
				System.out.println("    filePath = " + filePath);
				System.out.println("    fileName = " + fileName);
				//System.out.println("    fileID = " + fileID);
			}
			
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			System.out.println("CalendarDataBase SQLException " + query);
			e.printStackTrace();
		}
		
		return calendarFiles;
	}
	
	public void clearTables() {
		try {
			update("DELETE * FROM " + FILE_TABLE);
			update("DELETE * FROM " + EVENT_TABLE);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			System.out.println("SQLException query = " + "DELETE * FROM " + FILE_TABLE);
			e.printStackTrace();
		}
	}
}
