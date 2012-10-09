/*
 * org.openmicroscopy.shoola.util.ui.login.LoginCredentials 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
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
 */
package org.openmicroscopy.shoola.util.ui.login;

//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * Holds the user's credentials for logging onto server.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class LoginCredentials
{
	
	/** Identifies a high speed connection. */
	public static final int HIGH = 0;
	
	/** Identifies a medium speed connection. */
	public static final int MEDIUM = 1;
	
	/** Identifies a low speed connection. */
	public static final int LOW = 2;
	
	/**
     * The <i>OMERO</i> login name of the user.
     * This is the <code>OME Name</code> that was assinged to the user when
     * it was created in the DB.
     */
    private String  userName;
    
    /**
     * The <i>OMERO</i> login password of the user.
     * This is the password that was chosen for the user when it was created
     * in the DB.
     */
    private String  password;
    
    /** The name of the <i>OMERO</i> server. */
    private String  hostName;
    
    /** The connection speed level. */
    private int 	speedLevel;
    
    /** The port used. */
    private int		port;
    
    /** The selected group. */
    private long	group;
    
    /** Flag indicating to encrypt or not the data transfer. */
    private boolean encrypted;
    
    /** 
     * Controls if the passed speed index is supported.
     * 
     * @param level The value to handle.
     */
    private void checkSpeedLevel(int level)
    {
    	switch (level) {
			case HIGH:
			case MEDIUM:
			case LOW:
				return;
			default:
				throw new IllegalArgumentException("Speed level not valid.");
		}
    }
    
    /**
     * Creates a new instance.
     * 
     * @param userName   The <i>OMERO</i> login name of the user.
     *                   This is the <code>OME Name</code> that was assigned to 
     *                   the user when it was created in the DB.
     * @param password   The <i>OMERO</i> login password of the user.
     *                   This is the password that was chosen for the user when
     *                   it was created in the DB.
     * @param hostName 	 The name of the selected server.
     * @param speedLevel The connection speed.
     * @param port		 The port used.
     * @param encrypted  Pass <code>true</code> to encrypt data transfer,
     * 					 <code>false</code> otherwise.
     * @throws IllegalArgumentException If the user name and/or the password is
     *                 <code>null</code> or has <code>0</code>-length.
     */
    public LoginCredentials(String userName, String password, String hostName,
    						int speedLevel, int port, boolean encrypted)
    {
    	this(userName, password, hostName, speedLevel, port, -1L, encrypted);
    }
    
    /**
     * Creates a new instance.
     * 
     * @param userName   The <i>OMERO</i> login name of the user.
     *                   This is the <code>OME Name</code> that was assigned to 
     *                   the user when it was created in the DB.
     * @param password   The <i>OMERO</i> login password of the user.
     *                   This is the password that was chosen for the user when
     *                   it was created in the DB.
     * @param hostName 	 The name of the selected server.
     * @param speedLevel The connection speed.
     * @param port		 The port used.
     * @param group      The group the user is member of.
     * @param encrypted  Pass <code>true</code> to encrypt data transfer,
     * 					 <code>false</code> otherwise.
     * @throws IllegalArgumentException If the user name and/or the password is
     *                 <code>null</code> or has <code>0</code>-length.
     */
    public LoginCredentials(String userName, String password, String hostName,
    						int speedLevel, int port, long group, 
    						boolean encrypted)
    {
    	checkSpeedLevel(speedLevel);
    	this.speedLevel = speedLevel;
        this.userName = userName;
        this.password = password;
        this.hostName = hostName;
        this.port = port;
        this.group = group;
        this.encrypted = encrypted;
    }
    
    /** 
     * Returns the group.
     * 
     * @return See above.
     */
    public long getGroup() { return group; }
    
    /**
     * Returns the name of the <i>OMERO</i> server.
     * 
     * @return See above.
     */
    public String getHostName() { return hostName; }
    
    /**
     * Returns the <i>OMERO</i> login name of the user.
     * This is the <code>OME Name</code> that was assigned to the user when
     * it was created in the DB.
     * This field is always a non-<code>null</code> string with a positive
     * length.
     * 
     * @return See above.
     */
    public String getUserName() { return userName; }

    /**
     * Returns the <i>OMERO</i> login password of the user.
     * This is the password that was chosen for the user when it was created
     * in the DB.
     * This field is always a non-<code>null</code> string with a positive
     * length.
     * 
     * @return See above.
     */
    public String getPassword() { return password; }
    
    /**
     * Returns the selected connection speed.
     * 
     * @return See above.
     */
    public int getSpeedLevel() { return speedLevel; }
    
    /**
     * Returns the selected port.
     * 
     * @return See above.
     */
    public int getPort() { return port; }

    /**
     * Returns <code>true</code> if the data transfer is encrypted,
     * <code>false</code> otherwise.
     * 
     * @return See above.
     */
    public boolean isEncrypted() { return encrypted; }
    
    /**
     * Formats user name and password.
     * Each character of the password is replaced by a star.
     * @see Object#toString()
     */
    public String toString()
    {
        StringBuffer buf = new StringBuffer("User Name: ");
        buf.append(userName);
        buf.append(" -- Password: ");
        for (int i = 0; i < password.length(); ++i) buf.append('*');
        return buf.toString();
    }
    
}
