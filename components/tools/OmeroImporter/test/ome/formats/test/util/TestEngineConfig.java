/*
 * ome.formats.test.util.TestEngineConfig
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
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
 */

package ome.formats.test.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.prefs.BackingStoreException;

import org.ini4j.IniPreferences;
import org.ini4j.InvalidIniFormatException;

public class TestEngineConfig extends IniPreferences
{
	/** Root node of the configuration file. */
	public static final String CONFIG_ROOT = "main";
	
	/** OMERO server hostname */
	public static final String CONFIG_HOSTNAME = "hostname";
	
	/** OMERO experimenter name (username) */
	public static final String CONFIG_USERNAME = "username";
	
    /** OMERO experimenter password */
	public static final String CONFIG_PASSWORD = "password";
	
	/** OMERO session key */
	public static final String CONFIG_SESSIONKEY = "sessionkey";
	
	/** OMERO server port */
	public static final String CONFIG_PORT = "port";
	
	/** Whether or not to populate initiation files with metadata */
	public static final String CONFIG_POPULATE = "populate";
	
	/** If we're performing a single directory test run */
	public static final String CONFIG_RECURSE = "recurse";
	
	/** Target directory for the test engine to work with */
	public static final String CONFIG_TARGET = "target_directory";
	
	/**
	 * Test engine configuration, backed by an initiation file.
	 * @param file The file to use as a backing store.
	 * @throws BackingStoreException
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 * @throws InvalidIniFormatException 
	 */
	public TestEngineConfig(InputStream stream)
		throws InvalidIniFormatException, FileNotFoundException, IOException
	{
		super(stream);
	}
	
	/**
	 * Sets the hostname configuration property.
	 * @param hostname Value to set.
	 */
	public void setHostname(String hostname)
	{
		this.node(CONFIG_ROOT).put(CONFIG_HOSTNAME, hostname);
	}
	
	/**
	 * Returns the hostname configuration property.
	 * @return See above.
	 */
	public String getHostname()
	{
		return node(CONFIG_ROOT).get(CONFIG_HOSTNAME, null);
	}
	
	/**
	 * Sets the username configuration property.
	 * @param username Value to set.
	 */
	public void setUsername(String username)
	{
		node(CONFIG_ROOT).put(CONFIG_USERNAME, username);
	}
	
	/**
	 * Returns the username configuration property.
	 * @return See above.
	 */
	public String getUsername()
	{
		return node(CONFIG_ROOT).get(CONFIG_USERNAME, null);
	}
	
	/**
	 * Sets the password configuration property.
	 * @param password Value to set.
	 */
	public void setPassword(String password)
	{
		node(CONFIG_ROOT).put(CONFIG_PASSWORD, password);
	}
	
	/**
	 * Returns the password configuration property.
	 * @return See above.
	 */
	public String getPassword()
	{
		return node(CONFIG_ROOT).get(CONFIG_PASSWORD, null);
	}
	
	/**
	 * Sets the session key configuration property.
	 * @param sessionKey Value to set.
	 */
	public void setSessionKey(String sessionKey)
	{
		node(CONFIG_ROOT).put(CONFIG_SESSIONKEY, sessionKey);
	}
	
	/**
	 * Returns the password configuration property.
	 * @return See above.
	 */
	public String getSessionKey()
	{
		return node(CONFIG_ROOT).get(CONFIG_SESSIONKEY, null);
	}
	
	/**
	 * Sets the port configuration property.
	 * @param port Value to set.
	 */
	public void setPort(int port)
	{
		node(CONFIG_ROOT).putInt(CONFIG_PORT, port);
	}
	
	/**
	 * Returns the port configuration property.
	 * @return See above.
	 */
	public int getPort()
	{
		return node(CONFIG_ROOT).getInt(CONFIG_PORT, 4063);
	}
	
	/**
	 * Sets the populate configuration property.
	 * @param populate Value to set.
	 */
	public void setPopulate(boolean populate)
	{
		node(CONFIG_ROOT).putBoolean(CONFIG_POPULATE, populate);
	}
	
	/**
	 * Returns the populate configuration property.
	 * @return See above.
	 */
	public boolean getPopulate()
	{
		return node(CONFIG_ROOT).getBoolean(CONFIG_POPULATE, false);
	}
	
	/**
	 * Sets the recursion configuration property.
	 * @param rescurse Value to set.
	 */
	public void setRecurse(boolean recurse)
	{
		node(CONFIG_ROOT).putBoolean(CONFIG_RECURSE, recurse);
	}
	
	/**
	 * Returns the recursion configuration property.
	 * @return See above.
	 */
	public boolean getRecurse()
	{
		return node(CONFIG_ROOT).getBoolean(CONFIG_RECURSE, true);
	}
	
	/**
	 * Sets the target directory configuration property.
	 * @param rescurse Value to set.
	 */
	public void setTarget(String target)
	{
		node(CONFIG_ROOT).put(CONFIG_TARGET, target);
	}
	
	/**
	 * Returns the target directory configuration property.
	 * @return See above.
	 */
	public String getTarget()
	{
		return node(CONFIG_ROOT).get(CONFIG_TARGET, null);
	}
	
	/**
	 * Checks if we have the configuration properties available to perform an
	 * OMERO server login.
	 * @return <code>true</code> if we have the configuration properties
	 * available, <code>false</code> otherwise.
	 */
	public boolean validateLogin()
	{
        if (((getUsername() == null || getPassword() == null)
        	  && getSessionKey() == null) || getHostname() == null)
        {
            return false;
        }
        return true;
	}
}
