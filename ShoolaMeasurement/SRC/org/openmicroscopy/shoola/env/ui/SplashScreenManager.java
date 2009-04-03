/*
 * org.openmicroscopy.shoola.env.ui.SplashScreenManager
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

package org.openmicroscopy.shoola.env.ui;

//Java imports
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.Container;
import org.openmicroscopy.shoola.env.data.login.UserCredentials;
import org.openmicroscopy.shoola.util.ui.login.LoginCredentials;
import org.openmicroscopy.shoola.util.ui.login.ScreenLogin;
import org.openmicroscopy.shoola.util.ui.login.ScreenLogo;

/** 
 * Manages the splash screen input, data and update.
 * Plays both the role of Controller and Model within the splash screen
 * component.
 * <p>Provides clients with the splash screen component's functionality &#151;
 * as specified by the the {@link SplashScreen} interface.  However, clients 
 * never get an instance of this class.  The reason is that this component is
 * meant to be used during the initialization procedure, which runs within its
 * own thread &#151; this component's event handling happens within the
 * <i>Swing</i> dispatching thread instead.  In order to separate threading
 * issues from the actual component's functionality, we use Active Object: this
 * class palys the role of the Servant and a proxy ({@link SplashScreenProxy})
 * is actually returned to clients &#151; by the {@link UIFactory}.</p>
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

class SplashScreenManager
	implements PropertyChangeListener
{
    
	/** The title of the splash screens. */
	static final String				TITLE = "Open Microscopy Environment";
	
    /** The client's version. */
    private static final String     VERSION = "3.0_Beta2 OLD/NEW SERVER";
    
	/** The component's UI. */
	private ScreenLogin			view;
	
	/** The component's UI. */
	private ScreenLogo			viewTop;
	
	/** Tells whether or not the splash screen window is open. */
	private boolean				isOpen;
	
	/** Filled in with the user's login when available. */
	private SplashScreenFuture	userCredentials;

	/** The current number of tasks to be executed. */
	private int					totalTasks;
	
	/** The current number of tasks that have been executed. */
	private int					doneTasks;
	
	/** Reference to the singleton {@link Container}. */
	private Container			container;
	
	/** Reference to the component. */
	private SplashScreen		component;
    
	/**
	 * Creates the splash screen component.
	 * Creates a new instance of this manager, of its corresponding UI
	 * ({@link SplashScreenView}), and links them as needed.
     * 
     * @param component	
     * @param c			Reference to the singleton {@link Container}.
	 */
	SplashScreenManager(SplashScreen component, Container c)
	{
		container = c;
		this.component = component;
		view = new ScreenLogin(TITLE, IconManager.getLoginBackground(), 
								VERSION);
		viewTop = new ScreenLogo(TITLE, IconManager.getSplashScreen());
		//viewTop = new SplashScreenLogo();
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension d = viewTop.getExtendedSize();
		Dimension dlogin = view.getPreferredSize();
		int totalHeight = d.height+dlogin.height;
		viewTop.setBounds((screenSize.width-d.width)/2, 
	    		 	(screenSize.height-totalHeight)/2, d.width, 
	    		 	viewTop.getSize().height);
	    Rectangle r = viewTop.getBounds();
		view.setBounds(r.x,  r.y+d.height, dlogin.width, dlogin.height);
		view.addPropertyChangeListener(this);
		viewTop.addPropertyChangeListener(this);
		isOpen = false;
		doneTasks = 0;
	}

	/**
	 * Implemented as specified by {@link SplashScreen}.
	 * @see SplashScreen#open()
	 */
	void open()
	{
		//close() has already been called.
		if (view == null || viewTop == null) return;  
		//view.setVisible(true);
		viewTop.setVisible(true);
		isOpen = true;	
	}

	/**
	 * Implemented as specified by {@link SplashScreen}.
	 * @see SplashScreen#close()
	 */
	void close()
	{
		//close() has already been called.
		if (view == null || viewTop == null) return;
		view.dispose();
		viewTop.dispose();
		view = null;
		viewTop = null;
		isOpen = false;
	}

	/**
	 * Sets the total number of initialization tasks that have to be
     * performed.
     * 
     * @param value The total number of tasks.
	 * @see SplashScreen#setTotalTasks(int)
	 */
	void setTotalTasks(int value)
	{
		if (!isOpen) return;
		totalTasks = value;
		//NB: Increment to show that the execution process is finished 
		// i.e. all tasks executed.
		totalTasks++;
		viewTop.initProgressBar(value);
	}
	
	/**
	 * Updates the display to the current state of the initialization
     * procedure.
     * 
     * @param task  The name of the initialization task that is about to
     *              be executed.
	 * @see SplashScreen#updateProgress(String)
	 */
	void updateProgress(String task)
	{
		if (!isOpen) return;
		int n = doneTasks++;
		viewTop.setStatus(task, n);
		if (doneTasks == totalTasks) {
			viewTop.setStatusVisible(false);
			view.setVisible(true);
		}
	}
    
    /**
     * Registers a request to fill in the given <code>future</code> with
     * the user's credentials when available.
     * 
     * @param future The Future to collect the credentials.
     * @param init   Flag to control if it's the first attempt. 
     */
    void collectUserCredentials(SplashScreenFuture future, boolean init)
    {
        userCredentials = future;
        view.setControlsEnabled(true);
       
        if (!init) {
            view.setCursor(Cursor.getDefaultCursor());
            view.cleanField(ScreenLogin.PASSWORD_FIELD);
        }
    }
	
    /**
     * Registers a request to fill in the given <code>future</code> with
     * the user's credentials when available.
     * 
     * @param future The Future to collect the credentials.
     */
    void collectUserCredentialsInit(SplashScreenFuture future)
    {
        userCredentials = future;
        view.setControlsEnabled(true);
    }
    
	/**
	 * Attempts to log onto <code>OMERO</code>.
	 * 
	 * @param lc The user's credentials.
	 */
	private void login(LoginCredentials lc)
	{
		try {
			UserCredentials uc = new UserCredentials(lc.getUserName(), 
					lc.getPassword(), lc.getHostName());
			userCredentials.set(uc);
		} catch (Exception e) {
			UserNotifier un = UIFactory.makeUserNotifier(container);
            un.notifyError("Login Incomplete", e.getMessage());
            view.setControlsEnabled(true);
		}
	}
	
	/**
	 * Reacts to property changes fired by the {@link ServerDialog}.
	 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt)
	{
		String name = evt.getPropertyName();
		if (ScreenLogin.LOGIN_PROPERTY.equals(name)) {
			LoginCredentials lc = (LoginCredentials) evt.getNewValue();
			if (userCredentials != null  && lc != null) login(lc);
		} else if (ScreenLogin.QUIT_PROPERTY.equals(name)) {
			 container.exit();
		     component.close();
		} else if (ScreenLogin.TO_FRONT_PROPERTY.equals(name)) {
			viewTop.toFront();
		} else if (ScreenLogo.MOVE_FRONT_PROPERTY.equals(name)) {
			view.toFront();
		}
	}

}
