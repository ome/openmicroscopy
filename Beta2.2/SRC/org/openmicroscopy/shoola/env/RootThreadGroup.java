/*
 * org.openmicroscopy.shoola.env.RootThreadGroup
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

package org.openmicroscopy.shoola.env;

//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * Groups, directly or indirectly, all threads running in the application.
 * The {@link Container} creates a single instance and makes sure that all
 * threads will be rooted by this thread group. 
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
final class RootThreadGroup 
	extends ThreadGroup
{

	/** The name of the thread group. */
	private static final String	NAME = "Shoola";
	

	/** Creates a new instance. */
	RootThreadGroup()
	{
		super(NAME);
	}
	
	/**
	 * Overrides the parent's method to activate the abnormal exit procedure
	 * in the case of an uncaught exception.
     * @see ThreadGroup#uncaughtException(Thread, Throwable)
	 */
	public void uncaughtException(Thread t, Throwable e)
	{
		AbnormalExitHandler.terminate(e);	
	}

}
