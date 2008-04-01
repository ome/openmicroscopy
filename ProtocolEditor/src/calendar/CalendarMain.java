
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

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import omeroCal.OmeroCal;
import omeroCal.model.CalendarDataBase;
import omeroCal.model.CalendarEvent;
import omeroCal.model.ICalendarDB;
import omeroCal.model.ICalendarModel;
import omeroCal.model.CalendarModel;
import omeroCal.view.Controller;
import omeroCal.view.MonthView;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import tree.DataFieldConstants;
import ui.IModel;
import ui.components.FileChooserReturnFile;
import util.XMLMethods;

public class CalendarMain extends OmeroCal {
	
	/**
	 * A reference to the model of the Omero.editor.
	 * So that files can be opened etc. 
	 * from the calendar. 
	 */
	IModel editorModel;
	
	
	public CalendarMain(IModel editorModel) {
		
		/*
		 * This instantiates the DB and alarm checker...
		 */
		super();
		
		this.editorModel = editorModel;
		
		/*
		 * Now add a custom listener to the alarm checker. 
		 */
		ICalendarDB db = getCalendarDataBase();
		getAlarmChecker().addAlarmEventListener(new AlarmActivatedListener(db, editorModel));
		
		// and turn off the default alarm
		getAlarmChecker().enableDefaultAlarmPopup(false);
	}
	
	/**
	 * This method instantiates the database, controller and UI.
	 * Once this is done, additional listeners can be added to the controller. 
	 */
	public void openDBAndDisplayUI(boolean standAloneApplication) {
	
		super.openDBAndDisplayUI(standAloneApplication);
		
		/*
		 * Now you can add listeners to the controller etc.
		 */
		ICalendarDB db = getCalendarDataBase();
		Controller cont = getController();
		
		cont.addEventListener(new OpenFileListener(db, editorModel));
		
		/*
		 * Turn off editing of info panel
		 */
		getCalendarDisplay().setInfoPanelEnabled(false);
	}
	

	public void repopulateDB() {
		
		/*
		 * User gets asked if they want to re-populate the DB (wipe old data)
		 */
		FileManager.populateDBfromFile(getCalendarDataBase());
	}
	
	/**
	 * This method takes an Omero.editor file (XML file) which is already referenced 
	 * in the database, and tries to update it's name and the calendarEvents that 
	 * belong to it. 
	 * It looks for a file in the DB with the same absolute file path.
	 * If it finds one (and only one), it updates the name, deletes the old 
	 * calendarEvents belonging to this calendarFile, and adds back all the 
	 * new events from the new file. 
	 * 
	 * @param file		The new calendarFile. 
	 * @return		true if there was only 1 file in DB that matched (and was updated)
	 */
	public boolean updateCalendarFileInDB(File file) {
		
		return FileManager.updateCalendarFileInDB(getCalendarDataBase(), file);
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
	public boolean addCalendarFileToDB(File xmlFile) {
		
		return FileManager.addCalendarFileToDB(xmlFile, getCalendarDataBase());
	}

}
