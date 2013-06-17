/*
 * training.ConfigurationInfo
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2013 University of Dundee & Open Microscopy Environment.
 *  All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package training;

//Java imports

//Third-party libraries

//Application-internal dependencies

/**
 * Helper class hosting the configuration information.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since 4.4
 */
public class ConfigurationInfo
{

	/** The default port.*/
	public static final int DEFAULT_PORT = 4064;
	
	/** The server address.*/
	private String hostName = "howe.openmicroscopy.org.uk";//"serverName";
	
	/** The port to use.*/
	private int port;
	
	/** The username.*/
	private String userName = "user-1";
	
	/** The password.*/
	private String password = "ome";
	
	/** Creates a new instance.*/
	public ConfigurationInfo()
	{
		setPort(DEFAULT_PORT);
	}
	
	/**
	 * Returns the hostname.
	 * 
	 * @return See above.
	 */
	public String getHostName() { return hostName; }
	
	/**
	 * Returns the password.
	 * 
	 * @return See above.
	 */
	public String getPassword() { return password; }
	
	/**
	 * Returns the port.
	 * 
	 * @return See above.
	 */
	public int getPort() { return port; }
	
	/**
	 * Returns the user name.
	 * 
	 * @return See above.
	 */
	public String getUserName() { return userName; }
	
	/**
	 * Sets the hostname.
	 * 
	 * @param hostName The value to set.
	 */
	public void setHostName(String hostName) { this.hostName = hostName; }
	
	/**
	 * Sets the password.
	 * 
	 * @param password The value to set.
	 */
	public void setPassword(String password) { this.password = password; }
	
	/**
	 * Sets the port.
	 * 
	 * @param port The value to set.
	 */
	public void setPort(int port) { this.port = port; }
	
	/**
	 * Sets the userName.
	 * 
	 * @param userName The value to set.
	 */
	public void setUserName(String userName) { this.userName = userName; }
}
