/*
 * org.openmicroscopy.shoola.env.ui.AWTExceptionHanlder
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
import org.openmicroscopy.shoola.env.Container;
import org.openmicroscopy.shoola.env.log.Logger;

/** 
 * Handles uncaught exeptions thrown in the AWT event-dispatch thread.
 * The handling policy is very easy for now: the exception is logged and the
 * app is terminated.
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
	
	private static Logger		logger;

	/**
	 * Sets up exception handling.
	 * 
	 * @param c 	The container.
	 */
	public static void configure(Container c)
	{
		//NB: this can't be called outside of container b/c agents have no refs
		//to the singleton container. 
		if (c != null) {
			logger = c.getRegistry().getLogger();
			System.setProperty("sun.awt.exception.handler", 
						"org.openmicroscopy.shoola.env.ui.AWTExceptionHanlder");
		}
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
	 * Handles any uncaught exception in the AWT event-dispatch thread.
	 * 
	 * @param t	The exception that was thrown.
	 */
	public void handle(Throwable t)
	{
		//This method is required by the reflection code in the
		//EventDispatchThread.handleException(Throwable) method.
		if (logger != null)
			logger.fatal(this, extractStackInfo(t));
		System.exit(1);
	}
	
	private String extractStackInfo(Throwable t)
	{
		StackTraceElement top = t.getStackTrace()[0];
		StringBuffer buf = new StringBuffer();
		buf.append("Abnormal termination due to an uncaught exception.  ");
		buf.append("Message: ");
		String msg = t.getMessage();
		buf.append(msg == null || msg.length() == 0 ? "<none>" : msg);
		buf.append(".  ");
		buf.append("Stack trace (last call): ");
		buf.append(top.toString());
		return buf.toString();
	}

}
