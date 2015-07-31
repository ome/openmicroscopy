/*
 * org.openmicroscopy.shoola.env.AbnormalExitHandler
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
import javax.swing.JOptionPane;

//Third-party libraries

import org.openmicroscopy.shoola.env.init.StartupException;
//Application-internal dependencies
import omero.log.LogMessage;
import omero.log.Logger;
import org.openmicroscopy.shoola.env.ui.UIFactory;
import org.openmicroscopy.shoola.env.ui.UserNotifier;

/** 
 * Handles uncaught exceptions thrown in the AWT event-dispatch thread and in
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
	
    /**
     * Indicates whether termination is already in progress.
     * Latches to true after the first call to the 
     * {@link #doTermination(Throwable) doTermination} method.
     */
    private boolean inProgress = false;
    
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
        //We need to make sure calls to this method are serialized.  
        //The synchronized keywork ensures that only one thread at
        //a time can proceed.  However, the same thread is allowed
        //to call this method again -- locks are re-entrant, so in
        //the case of recursion the original caller would enter again.
        //So just exit to avoid possible infinite loops if another
        //exception is thrown by the user notifier dialog (see below).
        if (inProgress) System.exit(1);
        
        //First call, set termination flag in case this method is 
        //called again.
        inProgress = true;
				
		//Now try to log.  There may be no logger yet (or even no 
        //container) if the exception was thrown at start up.		
		LogMessage msg = new LogMessage();
		msg.println("Abnormal termination due to an uncaught exception.");
		msg.print(t);
		Container c = Container.getInstance();
		Logger logger = null;
		if (c != null) logger = c.getRegistry().getLogger();
		if (logger != null) logger.fatal(this, msg);
		else System.err.println(msg);
		StringBuffer buffer = new StringBuffer();
		buffer.append("An unforeseen error occurred, the application" +
				" will exit.");
		buffer.append("\n");
		String message = msg.toString();
		int length = message.length();
		if (length > 200) length = 200;
		buffer.append(message.substring(0, length));
		buffer.append("...");
		try {
			UserNotifier un = UIFactory.makeUserNotifier(c);
			un.notifyError("Abnormal Termination", buffer.toString(),
						msg.toString());
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, buffer.toString(), 
					"Abnormal Termination", JOptionPane.ERROR_MESSAGE);
		}
		//Quit the app. 
		System.exit(1);
	}

}
