/*
 * org.openmicroscopy.shoola.env.AbnormalExitHandler
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
import org.openmicroscopy.shoola.env.log.LogMessage;
import org.openmicroscopy.shoola.env.log.Logger;
import org.openmicroscopy.shoola.env.ui.UIFactory;
import org.openmicroscopy.shoola.env.ui.UserNotifier;

/** 
 * Handles uncaught exeptions thrown in the AWT event-dispatch thread and in
 * the main thread.
 * The handling policy is very easy for now: notify the user, log the exception,
 * and quit the application.
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
class AbnormalExitHandler
{
	
	/** The sole instance. */
	private static AbnormalExitHandler	singleton;
	
	
	/**
	 * Creates the singleton and sets up AWT exception relay.
	 * This must be called by the container just once and before initialization
	 * takes place.
	 */
	static void configure()
	{
		//Don't need check for singleton existence, 
		//the container will call this only once.
		singleton = new AbnormalExitHandler();
		AWTExceptionHanlder.register();	
	}
	
	/**
	 * Notifies the user, logs the exception, and quits the application.
	 * Called by the container if an unhandled exception is detected in the
	 * main thread or by the {@link AbnormalExitHandler} if an exception is
	 * detected in the AWT event-dispatch thread.
	 * 
	 * @param t		The exception that went unhandled.
	 */
	static void terminate(Throwable t)
	{
		singleton.doTermination(t);
		//TODO: use another policy for InternalError.  This is thrown in the
		//case of a bug which doesn't compromise normal functioning "too much".
		//So in this case we can just notify the user and log the error. 
	}
	
	/** Only used for the singleton. */
	private AbnormalExitHandler() {}
	
	/**
	 * Actual implementation of the {@link #terminate(Throwable) terminate}
	 * method.
	 * This method is thread-safe.  An exception might go unhandled in more
	 * then one thread (initialization is performed while the splash screen
	 * is showing), so we have to take into account the possibility that
	 * multiple threads call this method concurrently.
	 * 
	 * @param t		The exception that went unhandled.
	 */
	private synchronized void doTermination(Throwable t)
	{
		String diagnostic = makeDiagnosticMessage(t);
		
		//Disable exception relay to avoid possible infinite loops if another
		//exception is thrown by the user notifier dialog.
		AWTExceptionHanlder.unregister();
				
		//Now try to log.  There may be no logger yet (or even no container)
		//if the exception was thrown at start up.		
		LogMessage msg = new LogMessage();
		msg.println("Abnormal termination due to an uncaught exception.");
		msg.print(diagnostic);
		Container c = Container.getInstance();
		Logger logger = null;
		if (c != null)	logger = c.getRegistry().getLogger();
		if (logger != null)		logger.fatal(this, msg.toString());
		else	System.err.println(msg.toString());
		
		//Finally tell the user.  
		//(Notification service may not be up yet, so we create a temp one.)
		UserNotifier un = UIFactory.makeUserNotifier();
		un.notifyError("Abnormal Termination", 
					"An unforeseen error occurred, the application will exit.",
					diagnostic);
					
		//Quit the app. 
		System.exit(1);
	}
	
	/**
	 * Formats the information from the exception context into a diagnostic
	 * message.
	 * This information includes the exception class name, the exception
	 * message, a snapshot of the current stack, and the name of the current
	 * thread.
	 * 
	 * @param t	The exception.
	 * @return	A formatted string containing the exception information.
	 */
	private String makeDiagnosticMessage(Throwable t)
	{
		LogMessage buf = new LogMessage();
		t.printStackTrace(buf);
		buf.print("Exception in thread \"");
		buf.print(Thread.currentThread().getName());	
		buf.println("\"");
		return buf.toString();
	}

}
