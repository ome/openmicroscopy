/*
 * org.openmicroscopy.shoola.examples.LoginCredentials 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2010 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
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
package org.openmicroscopy.shoola.examples.data;

//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * 
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class LoginCredentials
{

	/** The default port value. */
	private static final int DEFAULT_PORT = 4064;
	
	/** The name of the user. */
	private String userName;
	
	/** The password of the user. */
	private String password;
	
	/** The address of the server. */
	private String hostName;
	
	/** The port. */
	private int	   port;
	
	/**
	 * Creates a new instance. 
	 * 
	 * @param userName The user name.
	 * @param password The password.
	 * @param hostname The name of the server.
	 * @param port The port to use.
	 */
	public LoginCredentials(String userName, String password, String hostname)
	{
		this(userName, password, hostname, DEFAULT_PORT);
	}
	
	/**
	 * Creates a new instance. 
	 * 
	 * @param userName The user name.
	 * @param password The password.
	 * @param hostName The name of the server.
	 * @param port The port to use.
	 */
	public LoginCredentials(String userName, String password, String hostName,
			int port)
	{
		this.userName = userName;
		this.password = password;
		this.hostName = hostName;
		this.port = port;
	}
	
	/** 
	 * Sets the port.
	 * 
	 * @param port The value to set.
	 */
	public void setPort(int port) { this.port = port; }
	
	/**
	 * Returns the name of the user.
	 * 
	 * @return See above.
	 */
	public String getUserName() { return userName; }
	
	/**
	 * Returns the address of the server
	 * 
	 * @return See above.
	 */
	public String getHostName() { return hostName; }
	
	/**
	 * Returns the address of the server
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
	
}
