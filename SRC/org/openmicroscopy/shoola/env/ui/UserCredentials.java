/*
 * org.openmicroscopy.shoola.env.ui.UserCredentials
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

package org.openmicroscopy.shoola.env.ui;

//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * Stores the user's login credentials.
 * Used as a Future by {@link SplashScreenProxy}.
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

public class UserCredentials 
{

	/** The login name. */
	private String		userName;
	
	/** The login password. */
	private String		password;
	
	/** The user's ID. */
	private int			userID;
	
	/** Tells whether or not the credentials have been filled in. */
	private boolean		isFilledIn;
	
	/**
	 * Creates a new instance with no credentials.
	 */
	UserCredentials() 
	{
		isFilledIn = false;
	}
	
	/**
	 * Fills the user's credentials in.
	 * This method is thread-safe and awakes threads that were suspended
	 * on {@link #get()}.
	 * 
	 * @param userName	The login name.
	 * @param password	The login password.
	 */
	synchronized void set(String userName, String password)
	{
		this.userName = userName;
		this.password = password;
		isFilledIn = true;
		notify();
	}
	
	/**
	 * Waits until the user's credentials have been filled in and then
	 * returns this object.
	 * The caller can then invoke the getter methods to retrieve the
	 * credentials.
	 * 
	 * @return	This object.
	 * @see #set(String, String)
	 */
	synchronized UserCredentials get()
	{
		try {
			while (!isFilledIn)		wait();	
		} catch (InterruptedException ie) {
			//Ignore, not relevant in our case.
		}
		return this;
	}
	
	/**
	 * Returns the login name.
	 * 
	 * @return	The user name or <code>null</code> if that hasn't been
	 * 			entered yet.
	 */
	public String getUserName()
	{
		return userName;
	}

	/**
	 * Returns the password.
	 * 
	 * @return	The password or <code>null</code> if that hasn't been
	 * 			entered yet.
	 */
	public String getPassword()
	{
		return password;
	}

	/** 
	 * Returns the userID.
	 * 
	 * @return	The user's ID stored, value retrieve from DB.
	 */
	public int getUserID()
	{
		return userID;
	}
	
	/**
	 * Sets the user's ID.
	 * 
	 * @param userID	user's ID retrieved from DB.
	 */
	public void setUserID(int userID)
	{
		this.userID = userID;
	}
	
}
