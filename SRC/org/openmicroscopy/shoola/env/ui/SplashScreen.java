/*
 * org.openmicroscopy.shoola.env.ui.SplashScreen
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
 * Declares the interface to which the splash screen component has to
 * conform. 
 * The implementation component has to serve both as start up screen to provide
 * the user with feedback about the state of the initialization procedure and
 * as a login dialog to collect the user's credentials. 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * <br><b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
public interface SplashScreen
{
	
	/**
	 * Pops up the splash screen window.
	 */
	public void open();
	
	/**
	 * Closes the splash screen window and disposes of it.
	 */
	public void close();
	
	/**
	 * Sets the total number of initialization tasks that have to be
	 * performed.
	 * This method is guaranteed to be called before the first invocation
	 * of {@link #updateProgress(String, int) updateProgress()}.
	 *  
	 * @param value	The total number of tasks.
	 */
	public void setTotalTasks(int value);
	
	/**
	 * Updates the display to the current state of the initialization
	 * procedure.
	 * 
	 * @param task	The name of the initialization task that is about to
	 * 				be executed.
	 * @param count	The number of tasks that have already been executed.
	 */
	public void updateProgress(String task, int count);
	
	/**
	 * Returns the user name.
	 * 
	 * @return	The user name or <code>null</code> if that hasn't been
	 * 			entered yet.
	 */
	public String getUserName();
	
	/**
	 * Returns the password.
	 * 
	 * @return	The password or <code>null</code> if that hasn't been
	 * 			entered yet.
	 */
	public String getPassword();
	
}
