/*
 * omeds.DBManager
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2004 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */

package omeds;

//Java imports
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

//Third-party libraries

//Application-internal dependencies

/** 
 * Manages the connection to the test DB.
 * <p>The connection is configured by means of a properties file
 * (<i>db_connection.cfg</i>) which is placed in the same directory where
 * the <i>.class</i> file of this class lives.  The user name and password
 * that are specified in the configuration file must correspond to those of
 * an existing <i>OME</i> experimenter &#151; normally the test user.</p>
 * <p>This class has a singleton instance that can be retrived via
 * {@link #getInstance()}.
 * All test cases that need interact with the test DB will use this singleton
 * to retrieve a {@link #getConnection() connection}, 
 * to {@link #getPreparedStatement(String) pre-compile} SQL statements, and to
 * {@link #getUserID() find} out what is the user ID of the test user.</p> 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class DBManager
{
	
	/** 
	 * SQL we use to see if the connection is still alive.
	 * We also use this to retrieve the user ID of the test user.
	 */
	private static String   PING_STATEMENT = 
					"SELECT attribute_id FROM experimenters WHERE ome_name = ?";
	
	/**
	 * The sole instance that provides the connection for all test cases.
	 */
	private static DBManager	singleton;
	
	
	/**
	 * Returns the <code>DBManager</code> object that will handle the 
	 * connection to the test DB.
	 * 
	 * @return	See above.
	 */
	public static DBManager getInstance()
	{
		if (singleton == null) {
			try {
				Properties config = new Properties();
				config.load(DBManager.class.
									getResourceAsStream("db_connection.cfg"));
				singleton = new DBManager(config.getProperty("DRIVER"),
											config.getProperty("URL"),
											config.getProperty("USER"),
											config.getProperty("PASS"));	
			} catch (Exception e) {
				throw new RuntimeException("Can't initialize the DBManager", e);
			}
		}
		return singleton;
	}
	
	
	
	
	/**
	 * The <i>FQN</i> of the <i>JDBC</i> driver that we use for connecting
	 * to the test DB.
	 */
	private String				driver;
	
	/** Tells the driver how to connect to the test DB. */
	private String				dbURL;
	
	/** The user name of the test user. */
	private String				user;
	
	/** The password of the test user. */
	private String				password;
	
	/** The ID of the test user. */
	private int					userID;
	
	/** The connection to the DB. */
	private Connection  		connection;
	
	/** The ping statement we use to see if the connection is alive. */
	private PreparedStatement   ping;
	
	/**
	 * Contains a pool of prepared statements that have already been used.
	 * Test cases call the {#getPreparedStatement(String) getPreparedStatement}
	 * method to get prepared statements to run.  If a statement has already
	 * been compiled then it will be in the pool and just returned to the
	 * caller.
	 */
	private Map		     		prepStmsPool; 
	
	
	
	/**
	 * Creates a new instance and configures the connection parameters.
	 * 
	 * @param driver	The <i>FQN</i> of the <i>JDBC</i> driver that we use
	 * 					for connecting to the test DB.
	 * @param dbURL		Tells the driver how to connect to the test DB.
	 * @param user		The user name of the test user.
	 * @param pass		The password of the test user.
	 */
	private DBManager(String driver, String dbURL, String user, String pass)
	{
		if (driver == null || dbURL == null || user == null || pass == null)
			throw new NullPointerException("Invalid connection parameters.");
		this.driver = driver;
		this.dbURL = dbURL;
		this.user = user;
		this.password = pass;
		userID = -1;
		prepStmsPool = new HashMap();
	}
	
	/**
	 * Returns a connection to the test DB.
	 * Connects to the DB and retrieves the user ID if no connection has
	 * already been established.
	 * 
	 * @throws Exception	If the connection couldn't be established or the
	 * 						user ID couldn't be retrieved.
	 */
	public Connection getConnection()
		throws Exception
	{
		if (connection == null || !isConnectionAlive()) {    
			try {
				Class.forName(driver);
				connection = DriverManager.getConnection(dbURL, user, password);
				connection.setAutoCommit(true);
				ping = connection.prepareStatement(PING_STATEMENT);
				ping.setString(1, user);
				ResultSet rs = ping.executeQuery();
				rs.next();
				userID = rs.getInt(1);
				rs.close();
			} catch (Exception e) {
				connection = null;
				userID = -1;
				throw e;
			}
		}
		return connection;
	}
	
	/**
	 * Creates a prepared statement from the specified <code>def</code>.
	 * The DB manager maintains a pool of prepared statements that have already
	 * been used.  This avoids compiling twice the same statement.
	 * Test cases call this method to get prepared statements to run.
	 * 
	 * @param def	Defines the prepared statement.
	 * @return	A prepared statement built from <code>def</code>.
	 * @throws Exception	If the statement couldn't be compiled.
	 */
	public PreparedStatement getPreparedStatement(String def) 
		throws Exception
	{
		PreparedStatement   ps = null;
		if (connection != null) {   
			ps = (PreparedStatement) prepStmsPool.get(def);
			if (ps == null) {
				ps = connection.prepareStatement(def);
				prepStmsPool.put(def, ps);
			}
		}
		return ps;
	}
    
    /**
     * Returns the user name that was used to establish the connection.
     * This is specified in the configuration file (<i>db_connection.cfg</i>)
     * and must correspond to an <code>ome_name</code> field within the 
     * <code>experimenters</code> table in the test DB &#151;
     * normally the test user.
     *  
     * @return	See above.
     */
	public String getUserName()
	{
		return user;
	}

	/**
	 * Returns the user ID of the user that was used to establish the
	 * connection.
	 * That is, the content of the <code>attribute_id</code> field within the 
	 * <code>experimenters</code> table in the test DB.  If no connection is
	 * active, then <code>-1</code> is returned.
	 *  
	 * @return	See above.
	 */
	public int getUserID()
	{
		return userID;
	}
	
	/**
	 * Makes sure we disconnect when the singleton is garbage collected.
	 */
	public void finalize()
	{
		try {
			if (connection != null)    connection.close();
		} catch(Exception e) {
			//Ignore
		}
	}
	    
    /**
     * Tells whether or not the current connection is still valid.
     * 
     * @return	<code>true</code> for valid, <code>false</code> otherwise.
     */
	private boolean isConnectionAlive()
	{
		boolean isAlive = false;
		try {
		   ping.execute();
		   isAlive = true;
		} catch(Exception e) {}
		return isAlive;
	}

}
