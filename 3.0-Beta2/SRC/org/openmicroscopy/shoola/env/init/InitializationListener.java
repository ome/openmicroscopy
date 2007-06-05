/*
 * org.openmicroscopy.shoola.env.init.InitializationListener
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

package org.openmicroscopy.shoola.env.init;

//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * Subscribers that need monitor the initialization process implement this
 * interface and register with the {@link Initializer}.
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

public interface InitializationListener
{
	
	/**
	 * Called just before the initialization process starts.
	 * No task has been executed yet.
	 * 
	 * @param totalTasks	The total number of initialization tasks that will
	 * 						be performed.
	 */
	public void onStart(int totalTasks);
	
	/**
	 * Called just before a task is executed.
	 * 
	 * @param taskName	The name of the task that is about to be executed.
	 */
	public void onExecute(String taskName);
	
	/**
	 * Called after the initialization process ends.
	 * All tasks will have already been executed when this method is called.
	 */
	public void onEnd();
	
}
