
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
import omeroCal.model.ICalendarModel;
import omeroCal.model.CalendarModel;
import omeroCal.view.MonthView;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import tree.DataFieldConstants;
import ui.components.FileChooserReturnFile;
import util.XMLMethods;

public class CalendarMain extends OmeroCal {

	public void populateDBthenDisplay(boolean standAloneApplication) {
		
		/*
		 * This will instantiate the DB and show the UI. 
		 */
		openDBAndDisplayUI(standAloneApplication);
		
		
		FileManager.populateDBfromFile(getCalendarDataBase());
	}
	
	public void updateCalendarFileInDB(File file) {
		
		
	}
	
	public void addCalendarFileToDB(File file) {
		
	}

}
