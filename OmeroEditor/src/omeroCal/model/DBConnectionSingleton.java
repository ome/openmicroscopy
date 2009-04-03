
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
import java.sql.SQLException;
import java.sql.Statement;

/**
 * A singleton class to manage the connection to the calendar database. 
 * When this class is created, is sets up a new connection to the DB. 
 * 
 * @author will
 *
 */
public class DBConnectionSingleton {

	/**
	 * Database connection.
	 */
	private Connection conn;
	
	/**
	 * The folder that will be created (in the users home folder) to hold the database
	 */
	public static final String OMERO_EDITOR = "omero/EditorCalendar";
	
	/**
	 * A reference to the unique instance of this class.
	 */
	private static DBConnectionSingleton uniqueInstance;
	
	/**
	 * Private constructor, to ensure other classes cannot instantiate this class.
	 * This constructor causes a connection to the DB to be made.
	 * 
	 * @throws SQLException 	Thrown if connection to the DB is unsuccessful. 
	 */
	private DBConnectionSingleton() throws SQLException {
		
		String saveDirectory = getDataBaseSaveDirectory();
		
		System.out.println("Creating database here: " + saveDirectory);
		
		if (!new File(saveDirectory).exists()) {
           new File(saveDirectory).mkdir();
		}
		
		// Load the HSQL Database Engine JDBC driver
		try {
			Class.forName("org.hsqldb.jdbcDriver");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		conn = DriverManager.getConnection(
				"jdbc:hsqldb:file:" + saveDirectory + File.separator + "calendar",  // filenames
				"sa",                   // userName
				"");                    // password
		
		/*
		 * Add the database shutdown process to the Runtime shutdown,
		 * so that when the application quits, the database is shutdown. 
		 */
		Runtime.getRuntime().addShutdownHook(new ShutdownThread());
	}
	
	/**
	 * Returns a reference to the unique instance of this class, and
	 * instantiates this class if necessary.
	 * This instantiation creates a connection to the DB, which throws 
	 * an exception if unsuccessful. 
	 * 
	 * @return	A unique instance of this class.
	 * @throws SQLException		Thrown by trying to connect to the DB. 
	 */
	public static DBConnectionSingleton getInstance() throws SQLException {
		if (uniqueInstance == null) {
			uniqueInstance = new DBConnectionSingleton();
		}
		return uniqueInstance;
	}
	
	
	/**
	 * A convenience method for instantiating this class (if necessary) and
	 * getting the DB connection from that unique instance of this class.
	 * This should be called when the application starts, so that problems caused by 
	 * eg 2 applications trying to connect to the same DB are highlighted at start-up, 
	 * rather than some time later when trying to connect to the DB. 
	 * 
	 * @return				A DB connection. The DB will be saved at the location given by 
	 * 							the method getDataBaseSaveDirectory()
	 * @throws SQLException		If connecting to the DB fails. 
	 */
	public static Connection getConnection() throws SQLException {
		
		return getInstance().conn;
	}
	
	
	/**
	 * A convenience method for shutting down the DB connection.
	 * This should be called when the application quits. 
	 * 
	 * @throws SQLException
	 */
	private static void shutDownConnection() throws SQLException {
		
		getInstance().shutdown();
	}
	
	
	/**
	 * Exact copy from Brian's ome.formats.importer.HistoryDB.java
	 */
	public void shutdown() throws SQLException {
		
		Statement st = conn.createStatement();
		
		System.out.println("Shutting down database...");
		// db writes out to files and performs clean shuts down
		// otherwise there will be an unclean shutdown
		// when program ends
		st.execute("SHUTDOWN");
		conn.close();    // if there are no other open connection
	}
	
	
	/**
	 * Returns the directory where this class creates and stores the database
	 * 
	 * @return	the directory where this class creates and stores the database
	 */
	public String getDataBaseSaveDirectory() {
		return System.getProperty("user.home") + File.separator + OMERO_EDITOR;
	}
	
	class ShutdownThread extends Thread {

        public void run() {
            try {
				shutdown();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
    }
	
}
