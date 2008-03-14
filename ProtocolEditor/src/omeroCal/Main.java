
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

import java.io.File;
import java.sql.SQLException;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import omeroCal.model.CalendarDataBase;
import omeroCal.model.CalendarEvent;
import omeroCal.model.IMonthModel;
import omeroCal.model.MonthModel;
import omeroCal.view.Controller;
import omeroCal.view.MonthView;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import tree.DataFieldConstants;
import ui.components.FileChooserReturnFile;
import util.XMLMethods;
import calendar.CalendarFile;


public class Main {
	
	/**
	 * For testing.
	 * @param args
	 * @throws SQLException 
	 */
	public static void main(String[] args) throws SQLException {
		
		openMonthViewWithDB();
	}
	
	

	
	public static void openMonthViewWithDB() {
		
		populateDB();
		
		CalendarDataBase calDB = new CalendarDataBase();
		
		IMonthModel monthModel = new MonthModel(calDB);
		
		Controller controller = new Controller(monthModel);
		
		MonthView monthView = new MonthView(monthModel);
		
		JFrame frame = new JFrame("Omero.Editor Calendar");
		//frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		frame.getContentPane().add(monthView);
		
		frame.pack();
		frame.setVisible(true);
		
		
	}
	
	
	public static void clearDBTables() throws SQLException {
		
		CalendarDataBase calDB = new CalendarDataBase();
		calDB.clearTables();
		
		calDB.shutdown();
	}
	
	
	
	public static void populateDB() {
		
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
			
			
			CalendarDataBase calDB = new CalendarDataBase();
			calDB.clearTables();
			
			try {
				indexFilesToCalendar(file, calDB);
				
				calDB.shutdown();
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
	public static void indexFilesToCalendar(File rootDirectory, CalendarDataBase calDB) throws SQLException {
		
		
		if (rootDirectory.isDirectory()) {
	        String[] files = rootDirectory.list();
	        // an IO error could occur
	        if (files != null) {
	          for (int i = 0; i < files.length; i++) {
	        	  indexFilesToCalendar(new File(rootDirectory, files[i]), calDB);
	          }
	        }
	      } else {
	        if (fileContainsDates(rootDirectory)) {
	        	CalendarFile calendarFile = new CalendarFile(rootDirectory);
	        	
	        	
	        	int calID = calDB.saveCalendar(calendarFile);
	        	
	        	List<CalendarEvent> events = calendarFile.getEvents();
	        	for(CalendarEvent evt: events) {
	        		calDB.saveEvent(evt, calID);
	        	}
	        		

	        }
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
				e.printStackTrace();
				return false;
			}
		
		} 
		
		return false;
		
	}
}