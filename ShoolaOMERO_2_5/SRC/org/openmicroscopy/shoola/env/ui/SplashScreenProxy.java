/*
 * org.openmicroscopy.shoola.env.ui.SplashScreenProxy
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
import java.awt.event.ActionListener;
import javax.swing.SwingUtilities;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.data.login.UserCredentials;

/** 
 * Proxy for the splash screen component.
 * <p>Takes care, with the help of the {@link UserCredentials} class, of
 * threading issues that rise from having the initialization thread access the
 * splash screen and, at the same time, the <i>Swing</i> dispatching thread
 * manage that component's event handling.</p>
 * <p>In order to separate threading issues from the actual component's
 * functionality, we use Active Object.  This class plays the role of the Proxy,
 * implementing the Active Object interface ({@link SplashScreen}, which 
 * declares what is the functionality provided to clients) and forwarding
 * requests for executions of methods on the Servant &#151; 
 * {@link SplashScreenManager}, which provides the actual splash screen's
 * functionality and runs within the <i>Swing</i> dispatching thread as
 * opposite to the proxy, which runs within the initialization thread.</p>
 * <p>Requests for method execution are constructed by extending the
 * {@link Runnable} interface with anonymous inner classes and are scheduled
 * for execution by using the <code>invokeLater</code> method of 
 * {@link SwingUtilities} &#151; this represents our interface to the Scheduler
 * (which is part of the <i>Swing</i> innards).  Notice that even though
 * method requests are constructed and forwarded within the initialization
 * thread, the Servant's methods are always executed within the <i>Swing</i>
 * dispatching thread &#151; this frees the splash screen component from dealing
 * with threads.</p>
 * <p>The last thing that we have to deal with is how to collect the result
 * of a method call on the Servant.  We only have one method that returns
 * a value to the client: {@link #getUserCredentials(boolean)}.
 * However, this one has no corresponding method on the Servant &#151;
 * obviously enough because the user's credentials are entered by the user.  
 * So we would need a sort of Publisher-Subscriber mechanism in order to 
 * retrieve them when they are available.  This is not so straightforward
 * because these two methods above are supposed to be blocking.  Instead we  
 * post a request to the Servant to fill in the credentials when available.
 * We pass a {@link SplashScreenFuture} along with the request so that the
 * Servant may set the credentials in it as soon as the user enters them and
 * the Proxy may block on the <code>get</code> method of the Future waiting
 * for this event to occur.</p>
 * <p><small>
 * Our Future is not exactly the same as the one in Active Object: in our case
 * the Future is known by the Servant and never returned to clients.
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

class SplashScreenProxy
	implements SplashScreen
{

	/** The real subject where the component's functionality sits. */
	private SplashScreenManager		servant;
	
	/** 
	 * Tells whether or not the reference to the servant is still valid.
	 * This reference is valid any time between the first call to 
	 * {@link #open()} and the first call to {@link #close()}.
	 * If the reference is not valid, then calls silently return without
	 * forwarding requests.  This will prevent deadlock if 
	 * {@link #getUserCredentials(boolean)} is called before {@link #open()}.
	 */
	private boolean					isValid;	
	/* NOTE: Even though we send requests in the order:
	 * open ... update ... update ... close
	 * they may be received in a different order (we make no assumption on
	 * Swing scheduling), for example:
	 * update open update ... update close update
	 * This is not big deal however, b/c SplashScreenManager will ignore
	 * the first and last call above b/c the window is not open (isOpen field).
	 * If update is called b/f setTotalTasks no big harm is made and we prefer
	 * to avoid writing the code for checking this.
	 */
    
    
	/**
	 * Creates the proxy, the servant and configures the servant with a
	 * Future for later collection of user credentials. 
     * 
     * @param listener A listener for {@link SplashScreenView#cancel} button.
	 */
	SplashScreenProxy(ActionListener listener)
	{
		servant = new SplashScreenManager(listener);
		isValid = false;
	}

	/**
	 * Implemented as specified by {@link SplashScreen}.
	 * @see SplashScreen#open()
	 */
	public void open()
	{
		if (isValid) return;  //Somebody's already called open().
		
		/* NOTE: If open() is called again after close(), then we get in 
		 * here. However, no big harm can be made.
		 */
		
		//Construct request of mehtod execution.
 		Runnable doOpen = new Runnable() {
			public void run() { servant.open(); }
		};

		//Schedule execution within Swing dispatching thread.
		SwingUtilities.invokeLater(doOpen);
		
		isValid = true;
	}

	/** 
	 * Implemented as specified by {@link SplashScreen}.
	 * @see SplashScreen#close()
	 */
	public void close()
	{	
		if (!isValid) return;  //Somebody's already called close().
		
		//Construct request of mehtod execution.
		Runnable doClose = new Runnable() {
			public void run() { servant.close(); }
		};
		
		//Schedule execution within Swing dispatching thread.
		SwingUtilities.invokeLater(doClose);
		
		isValid = true;
	}

	/**
	 * Implemented as specified by {@link SplashScreen}.
	 * @see SplashScreen#setTotalTasks(int)
	 */
	public void setTotalTasks(final int value)
	{
		if (!isValid) return;  //Somebody's already called close().
		
		//Construct request of mehtod execution.
		Runnable doSetTotalTasks = new Runnable() {
			public void run() { servant.setTotalTasks(value); }
		};

		//Schedule execution within Swing dispatching thread.
		SwingUtilities.invokeLater(doSetTotalTasks);
	}

	/**
	 * Implemented as specified by {@link SplashScreen}.
	 * @see SplashScreen#updateProgress(String)
	 */
	public void updateProgress(final String task)
	{
		if (!isValid) return;  //Somebody's already called close().
		
		//Construct request of mehtod execution.
		Runnable doUpdateProgress = new Runnable() {
			public void run() { servant.updateProgress(task); }
		};

		//Schedule execution within Swing dispatching thread.
		SwingUtilities.invokeLater(doUpdateProgress);
	}

	/**
	 * Implemented as specified by {@link SplashScreen}.
	 * @see SplashScreen#getUserCredentials(boolean)
	 */
	public UserCredentials getUserCredentials(final boolean init)
	{
		//First off, let's make sure that this.open() has already been called.
		//If not we return to prevent deadlock.
		if (!isValid) return null;  
		
        //Construct request of method execution.
        final SplashScreenFuture future = new SplashScreenFuture();
        Runnable doCollectUserCredentials = new Runnable() {
            public void run() { servant.collectUserCredentials(future, init); }
        };

        //Schedule execution within Swing dispatching thread.
        SwingUtilities.invokeLater(doCollectUserCredentials);
        
		//Now we can safely wait for the user to enter their credentials.
		return (UserCredentials) future.get(); 
	}
   
}
