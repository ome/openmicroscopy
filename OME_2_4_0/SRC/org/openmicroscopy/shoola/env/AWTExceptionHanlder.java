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
 * <p><small>
 * <b>NOTE</b>: If no exception hanlder is attached to the AWT event-dispatch
 * thread, then any {@link RuntimeException} or {@link Error}, which is thrown
 * by a non-modal component, is re-thrown for the thread group to handle &#151;
 * see {@link EventDispatchThread#processException(Throwable, boolean)}.
 * Because of the way container initialization happens, the AWT event-dispatch
 * thread is part of the {@link RootThreadGroup}.  This means, that any such
 * exception would be forwarded to the {@link AbnormalExitHandler} anyway.  So
 * do we need this class?  Unfortunately yes.  In fact, if no exception handler
 * is specified, any {@link Throwable} thrown within a modal dialog results in
 * a stack trace being printed and the exception being discarded.
 * </small></p>
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
		//This is the hook method called by the code in the
		//EventDispatchThread.handleException(Throwable) method.
		
		AbnormalExitHandler.terminate(t);
	}

}
