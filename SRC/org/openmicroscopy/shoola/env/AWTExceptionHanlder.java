/*
 * org.openmicroscopy.shoola.env.AWTExceptionHanlder
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

package org.openmicroscopy.shoola.env;

//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * Forwards any uncaught exception in the AWT event-dispatch thread to
 * the {@link AbnormalExitHandler}.
 * This class is a temporary hack to work around the absence of a Java API that
 * provides the ability to catch unhandled exceptions in the AWT
 * event-dispatch thread.  It is subject to change at any time Sun will modify
 * the implementation of the <code>EventDispatchThread</code> class.
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
public final class AWTExceptionHanlder 
{

	/**
	 * Sets up exception handling relay.
	 * Only called by {@link AbnormalExitHandler}. 
	 */
	static void register()
	{
		System.setProperty("sun.awt.exception.handler", 
						"org.openmicroscopy.shoola.env.AWTExceptionHanlder");
	}
	
	/**
	 * Disables exception handling relay.
	 * Only called by {@link AbnormalExitHandler}.
	 */
	static void unregister()
	{
		System.setProperty("sun.awt.exception.handler", "");
	}
	
	/**
	 * Creates a new instance.
	 */
	public AWTExceptionHanlder()
	{
		//This constructor is required by the reflection code in the
		//EventDispatchThread.handleException(Throwable) method.
	}
	
	/**
	 * Forwards any uncaught exception in the AWT event-dispatch thread to
	 * the {@link AbnormalExitHandler}.
	 * 
	 * @param t	The exception that was thrown.
	 */
	public void handle(Throwable t)
	{
		//This method is required by the reflection code in the
		//EventDispatchThread.handleException(Throwable) method.
		
		AbnormalExitHandler.terminate(t);
	}

}
