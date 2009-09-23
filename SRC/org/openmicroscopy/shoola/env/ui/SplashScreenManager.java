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
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.awt.event.WindowStateListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.Icon;
import javax.swing.JFrame;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.Container;
import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.config.OMEROInfo;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.login.UserCredentials;
import org.openmicroscopy.shoola.util.image.geom.Factory;
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
	implements PropertyChangeListener, WindowFocusListener, WindowStateListener
{
	
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
    
	/** The splash login. */
	private Icon 				splashLogin;
	
	/**
	 * Attempts to log onto <code>OMERO</code>.
	 * 
	 * @param lc The user's credentials.
	 */
	private void login(LoginCredentials lc)
	{
		try {
			UserCredentials uc = new UserCredentials(lc.getUserName(), 
					lc.getPassword(), lc.getHostName(), lc.getSpeedLevel());
			uc.setPort(lc.getPort());
			userCredentials.set(uc);
		} catch (Exception e) {
			UserNotifier un = UIFactory.makeUserNotifier(container);
            un.notifyError("Login Incomplete", e.getMessage());
            view.setControlsEnabled(true);
            updateView();
 		}
	}
	
	/**
	 * Sets the state of the specified window.
	 * 
	 * @param f		The window to handle.
	 * @param state	The state to set.
	 */
	private void setWindowState(JFrame f, int state)
	{
		if (f == null) return;
		f.removeWindowStateListener(this);
		f.setState(state);
		f.addWindowStateListener(this);
	}
	
	/** Sets the views on top. */
    private void updateView()
    {
    	if (view != null) view.setAlwaysOnTop(true);
    	viewTop.setAlwaysOnTop(true); 
    	if (view != null) {
    		view.requestFocusOnField();
    	}
    }
    
    /** Initializes the view. */
    private void initializedView()
    {
    	if (view != null) return;	
    	Image img = IconManager.getOMEImageIcon();
    	Object version = container.getRegistry().lookup(LookupNames.VERSION);
    	String v = "";
    	if (version != null && version instanceof String)
    		v = (String) version;
    	OMEROInfo omeroInfo = 
    		(OMEROInfo) container.getRegistry().lookup(LookupNames.OMERODS);
        
    	String port = ""+omeroInfo.getPort();
    	view = new ScreenLogin(Container.TITLE, splashLogin, img, v, port);
		view.showConnectionSpeed(true);
		Dimension d = viewTop.getExtendedSize();
		Dimension dlogin = view.getPreferredSize();
		Rectangle r = viewTop.getBounds();
		view.setBounds(r.x, r.y+d.height, dlogin.width, dlogin.height);
		view.addPropertyChangeListener(this);
		view.addWindowStateListener(this);
		view.addWindowFocusListener(this);
    }
    
	/**
	 * Creates the splash screen component.
	 * Creates a new instance of this manager and links them as needed.
     * 
     * @param component	Reference to the {@link SplashScreen}.
     * @param c			Reference to the singleton {@link Container}.
	 */
	SplashScreenManager(SplashScreen component, Container c)
	{
		container = c;
		this.component = component;
		Image img = IconManager.getOMEImageIcon();
		Registry reg = c.getRegistry();
		String n = (String) reg.lookup(LookupNames.SPLASH_SCREEN_LOGO);
		
		String f = container.resolveConfigFile(null);
		Icon splashScreen = Factory.createIcon(n, f);
		if (splashScreen == null) {
			Boolean online = (Boolean) container.getRegistry().lookup(
					LookupNames.SERVER_AVAILABLE);
			if (!online) splashScreen = IconManager.getEditorSplashScreen();
			else splashScreen = IconManager.getSplashScreen();
		}
		n = (String) reg.lookup(LookupNames.SPLASH_SCREEN_LOGIN);
		
		splashLogin = Factory.createIcon(n, f);
		if (splashLogin == null)
			splashLogin = IconManager.getLoginBackground();
		
    	view = new ScreenLogin(Container.TITLE, splashLogin, img);
		viewTop = new ScreenLogo(Container.TITLE, splashScreen, img);
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension d = viewTop.getExtendedSize();
		Dimension dlogin = view.getPreferredSize();
		int totalHeight = d.height+dlogin.height;
		viewTop.setBounds((screenSize.width-d.width)/2, 
	    		 	(screenSize.height-totalHeight)/2, d.width, 
	    		 	viewTop.getSize().height);
		view = null;
		viewTop.addPropertyChangeListener(this);
		viewTop.addWindowStateListener(this);
		viewTop.addWindowFocusListener(this);
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
		if (viewTop == null) return;
		initializedView();
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
		view.setVisible(false);
		viewTop.setVisible(false);
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
			Boolean online = (Boolean) container.getRegistry().lookup(
					LookupNames.SERVER_AVAILABLE);
			if (online) view.setVisible(true);
			else viewTop.setVisible(false);
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
        if (view != null) view.setControlsEnabled(true);
        if (!init) {
        	if (view != null) view.cleanField(ScreenLogin.PASSWORD_FIELD);
            updateView();
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
        if (view != null) view.setControlsEnabled(true);
    }

    void onLoginFailure()
    {
    	 view.setControlsEnabled(true);
         updateView();
    }
    
	/**
	 * Reacts to property changes fired by the {@link ScreenLogin} and
	 * {@link ScreenLogo}.
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
		} else if (ScreenLogin.TO_FRONT_PROPERTY.equals(name) || 
				ScreenLogo.MOVE_FRONT_PROPERTY.equals(name)) {
			updateView();
		} 
	}

	/**
	 * Reacts to state changes fired by the {@link ScreenLogo} and
	 * {@link ScreenLogin}.
	 * @see WindowStateListener#windowStateChanged(WindowEvent)
	 */
	public void windowStateChanged(WindowEvent e)
	{
		Object src = e.getSource();
		int state = e.getNewState();
		if (src instanceof ScreenLogo) {
			//setWindowState(view, state);
		}
		else if (src instanceof ScreenLogin) setWindowState(viewTop, state);
		if (view != null) view.setAlwaysOnTop(state == JFrame.NORMAL);
		viewTop.setAlwaysOnTop(state == JFrame.NORMAL);
	}

	/**
	 * Resets the flag when one of the windows loses focus.
	 * @see WindowFocusListener#windowLostFocus(WindowEvent)
	 */
	public void windowLostFocus(WindowEvent e)
	{
		if (e.getOppositeWindow() == null) {
			if (view != null) view.setAlwaysOnTop(false);
			if (view != null) viewTop.setAlwaysOnTop(false);
		}
	}
	/**
	 * Required by the {@link WindowFocusListener} I/F but no-op implementation
	 * in our case.
	 * @see WindowFocusListener#windowGainedFocus(WindowEvent)
	 */
	public void windowGainedFocus(WindowEvent e) {}
	
}
