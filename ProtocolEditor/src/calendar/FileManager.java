
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
import java.sql.SQLException;
import java.util.List;

import javax.swing.JOptionPane;

import omeroCal.model.CalendarEvent;
import omeroCal.model.CalendarObject;
import omeroCal.model.ICalendarDB;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import tree.DataFieldConstants;
import ui.components.FileChooserReturnFile;
import util.XMLMethods;


public class FileManager {

	
	/**
	 * This method takes an Omero.editor file (XML file) which is already referenced 
	 * in the database, and tries to update it's name and the calendarEvents that 
	 * belong to it. 
	 * It looks for a file in the DB with the same absolute file path.
	 * If it finds one (and only one), it updates the name, deletes the old 
	 * calendarEvents belonging to this calendarFile, and adds back all the 
	 * new events from the new file. 
	 * 
	 * @param calDB		The database to update.
	 * @param file		The new calendarFile. 
	 * @return		true if there was only 1 file in DB that matched (and was updated)
	 */
	public static boolean updateCalendarFileInDB(ICalendarDB calDB, File file) {
		
		CalendarFile calendarFile = new CalendarFile(file);
		
		String filePath = calendarFile.getAbsoluteFilePath();
		
		System.out.println("FileManager  updateCalendarFile  filePath = " + filePath);
		
		/*
		 * This should return a single calendar file, since filePath should be unique!
		 */
		List<CalendarObject> calendarFilesInDB = calDB.getCalendarsByInfo(filePath);
		
		int files = calendarFilesInDB.size();
		
		System.out.println("FileManager updateCalendarFileInDB  retrieved " + files + " file");
		
		if (files == 1) {
			CalendarObject fileInDB = calendarFilesInDB.get(0);
			
			// update existing DB calendar file with new data (not color, visibility)
			// filePath should be same too!
			fileInDB.setName(calendarFile.getName());
			
			// send it back to the database
			calDB.updateCalendar(fileInDB);
			
			// now, need to replace old Events with new.
			// Delete the old ones.
			int calendarID = fileInDB.getCalendarID();
			calDB.deleteEventsForCalendar(calendarID);
			
			// add back the new events for the new calendarFile. 
			List<CalendarEvent> events = calendarFile.getEvents();
        	for(CalendarEvent evt: events) {
        		calDB.saveEvent(evt, calendarID);
        	}
        	
        	return true;
		}
		
		else
			return false;
		
	}
	
	public static void populateDBfromFile(ICalendarDB calDB) {
		
		int confirm = JOptionPane.showConfirmDialog(null, 
				"Do you want to refresh the database? " +
				"\n This will overwrite all existing data in the Calendar." +
				"\n " +
				"\n Please choose a root folder that contains all your OMERO.editor files.");
		
		if (confirm == JOptionPane.OK_OPTION) {
			
			// Create a file chooser, get a file from user
			FileChooserReturnFile fc = new FileChooserReturnFile(new String[] {"xml", ".pro", ".exp"}, null);
			File file = fc.getFileFromUser();
			
			if (file == null)
				return;
			
			calDB.clearTables();
			
			try {
				indexFilesToCalendar(file, calDB);
				
			} catch (SQLException e) {
				
				System.out.println("Problem populating the database from file system: " + e.getMessage());
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}
	
	
	/**
	 * Iterate through a file system (starting at root directory), adding files to the calendar.
	 * For each file, check to see if it contains DateTime Fields. Needs to contain at least one DateTime
	 * field in order to be added to the list of calendarFiles. 
	 * 
	 * @param rootDirectory
	 * @throws SQLException 
	 */
	public static void indexFilesToCalendar(File rootDirectory, ICalendarDB calDB) throws SQLException {
		
		
		if (rootDirectory.isDirectory()) {
	        String[] files = rootDirectory.list();
	        // an IO error could occur
	        if (files != null) {
	          for (int i = 0; i < files.length; i++) {
	        	  indexFilesToCalendar(new File(rootDirectory, files[i]), calDB);
	          }
	        }
	      } else {
	        {
	        	
	        	addCalendarFileToDB(rootDirectory, calDB);	

	        }
	      }
	}
	
	
	/**
	 * Adds an Omero.Editor (XML) file to the calendar database as a calendarFile. 
	 * Creates a new calendarFile from the XML file, adds this to the DB (retrieving UID).
	 * Then gets the list of events from the calendarFile and adds them to the DB
	 * with a reference to the calendarFile. 
	 * 
	 * @param xmlFile	The XML file to be added to the DB.
	 * @param calDB		The database to add the files to.
	 * @return 			False if the xmlFile had no date fields (not added to DB). 
	 */
	public static boolean addCalendarFileToDB(File xmlFile, ICalendarDB calDB) {
		
		if (!fileContainsDates(xmlFile)) {
			return false;
		}
		
		CalendarFile calendarFile = new CalendarFile(xmlFile);
    	
    	int calID = calDB.saveCalendar(calendarFile);
    	
    	List<CalendarEvent> events = calendarFile.getEvents();
    	for(CalendarEvent evt: events) {
    		calDB.saveEvent(evt, calID);
    	}
    	
    	return true;
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
		if ((fileName.endsWith(".xml") || fileName.endsWith(".exp") || fileName.endsWith(".pro"))) {
			
		
			// Try to open XML file, and see if it has any Elements named <DateTimeField>
			try {
				Document domDoc = XMLMethods.readXMLtoDOM(file);
				NodeList dateTimeNodes = domDoc.getElementsByTagName(DataFieldConstants.DATE_TIME_FIELD);
				if (dateTimeNodes.getLength() > 0)
					return true;
				// try the old date field "DateField"
				dateTimeNodes = domDoc.getElementsByTagName(DataFieldConstants.DATE);
				return (dateTimeNodes.getLength() > 0);
				
			} catch (SAXException e) {
				// TODO Auto-generated catch block
				// e.printStackTrace();
				return false;
			}
		
		} 
		
		return false;
		
	}
}
