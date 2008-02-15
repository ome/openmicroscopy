package util;

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


import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class PropertiesManager {
	
	public static final String CURRENT_FILES_FOLDER = "currentFilesFolder";
	public static final String CURRENT_EXPORT_FOLDER = "currentExportFolder";
	
	public static final String PROPERTIES_FILE_NAME = "properties";
	
	PropertiesManager uniqueInstance = new PropertiesManager();
	
	private PropertiesManager() {
		
		Properties properties = new Properties();

		try {
			FileInputStream in;
			in = new FileInputStream(PROPERTIES_FILE_NAME);
			properties.load(in);
			in.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/*
	 * These properties set by the methods below are lost when this application closes.
	 * To maintain properties between sessions, need to save them to a particular location ??
	 * using methods described here	
	 * http://java.sun.com/docs/books/tutorial/essential/environment/properties.html
	 */
	
	public static String getProperty(String key) {
		
		//System.out.println("PropertiesManager getProperty(" + key + ") = " + System.getProperty(key));
		
		return System.getProperty(key);
	}
	
	public static void setProperty(String key, String value) {
		
		//System.out.println("PropertiesManager setProperty() key = " + key + ", value = " + value);
		
		System.setProperty(key, value);
	}

}
