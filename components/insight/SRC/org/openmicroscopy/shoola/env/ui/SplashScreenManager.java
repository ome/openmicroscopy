/*
 * org.openmicroscopy.shoola.env.ui.SplashScreenManager
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2014 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
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
import org.openmicroscopy.shoola.util.CommonsLangUtils;
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
 * class plays the role of the Servant and a proxy ({@link SplashScreenProxy})
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
	 * The credentials stored if the login button is pressed before the
	 * end of the initialization sequence.
	 */
	private LoginCredentials lc;
	
	/**
	 * Attempts to log onto <code>OMERO</code>.
	 * 
	 * @param lc The user's credentials.
	 */
	private void login(LoginCredentials lc)
	{
		if (doneTasks != totalTasks) {
			this.lc = lc;
			return;
		}
		try {
			UserCredentials uc = new UserCredentials(lc.getUserName(), 
					lc.getPassword(), lc.getHostName(), lc.getSpeedLevel());
			uc.setPort(lc.getPort());
			uc.setEncrypted(lc.isEncrypted());
			uc.setGroup(lc.getGroup());
			userCredentials.set(uc);
			this.lc = null;
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
    	if (view != null) {
    	    view.setAlwaysOnTop(true);
    		view.requestFocusOnField();
    	}
    }
    
    /** 
     * Initializes the view. 
     * 
     * @param splashscreen The splash-screen
     */
    private void initializedView(Icon splashscreen)
    {
    	if (view != null) return;	
    	Image img = IconManager.getOMEImageIcon();
    	Object version = container.getRegistry().lookup(LookupNames.VERSION);
    	String v = "";
    	if (version != null && version instanceof String)
    		v = (String) version;
    	OMEROInfo info = 
    		(OMEROInfo) container.getRegistry().lookup(LookupNames.OMERODS);
    	int p = -1;
    	String port = ""+ info.getPortSSL();
    	String host = info.getHostName();
    	boolean configurable = info.isHostNameConfigurable();
    	//check if we have jnlp option
    	String jnlpHost = System.getProperty("jnlp.omero.host");
    	if (CommonsLangUtils.isNotBlank(jnlpHost)) {
    	    host = jnlpHost;
    	    configurable = false;
    	}
        String jnlpPort = System.getProperty("jnlp.omero.port");
        if (CommonsLangUtils.isNotBlank(jnlpPort)) {
            port = jnlpPort;
            p = Integer.parseInt(port);
            configurable = false;
        }
        String jnlpSession = System.getProperty("jnlp.omero.sessionid");
        boolean serverAvailable = connectToServer();
        if (CommonsLangUtils.isNotBlank(jnlpSession)) {
            serverAvailable = false;
        }
    	view = new ScreenLogin(Container.TITLE, splashscreen, img, v, port,
    			host, serverAvailable);
    	view.setEncryptionConfiguration(info.isEncrypted(),
    			info.isEncryptedConfigurable());
    	view.setHostNameConfiguration(host, configurable, p);
		view.showConnectionSpeed(true);
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension d = view.getPreferredSize();
		view.setBounds((screenSize.width-d.width)/2, 
	    		 	(screenSize.height-d.height)/2, d.width, d.height);
		
		view.addPropertyChangeListener(this);
		view.addWindowStateListener(this);
		view.addWindowFocusListener(this);
    }
    
    /**
     * Returns <code>true</code> if the client connects to a server, 
     * <code>false</code> otherwise.
     * 
     * @return See above.
     */
    private boolean connectToServer()
    {
    	Integer v = (Integer) container.getRegistry().lookup(
				LookupNames.ENTRY_POINT);
		if (v != null) {
		    return v.intValue() == LookupNames.INSIGHT_ENTRY ||
		            v.intValue() == LookupNames.IMPORTER_ENTRY;
		}
		return false;
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
		Registry reg = c.getRegistry();
		String n = (String) reg.lookup(LookupNames.SPLASH_SCREEN_LOGO);
		
		String f = container.getConfigFileRelative(null);
		Icon splashscreen = Factory.createIcon(n, f);
		if (splashscreen == null) {
			Integer v = (Integer) container.getRegistry().lookup(
					LookupNames.ENTRY_POINT);
			if (v != null) {
				switch (v.intValue()) {
					case LookupNames.IMPORTER_ENTRY:
						splashscreen = IconManager.getImporterSplashScreen();
						break;
					default:
						splashscreen = IconManager.getSplashScreen();
				}
			}
		}
		isOpen = false;
		doneTasks = 0;
		initializedView(splashscreen);
	}
    
	/**
	 * Implemented as specified by {@link SplashScreen}.
	 * @see SplashScreen#open()
	 */
	void open()
	{
		//close() has already been called.
		view.setVisible(true);
		view.setStatusVisible(true, false);
		isOpen = true;
		container.getRegistry().bind(LookupNames.LOGIN_SPLASHSCREEN, 
				Boolean.valueOf(true));
	}

	/**
	 * Implemented as specified by {@link SplashScreen}.
	 * @see SplashScreen#close()
	 */
	void close()
	{
		//close() has already been called.
		if (view == null) return;
		Boolean b = (Boolean) container.getRegistry().lookup(
		        LookupNames.SESSION_KEY);
		if (b != null && b.booleanValue()) {
		    view.setUserName("");
		}
		view.close();
		view = null;
		isOpen = false;
		container.getRegistry().bind(LookupNames.LOGIN_SPLASHSCREEN,
				Boolean.valueOf(false));
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
		view.initProgressBar(value);
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
		view.setStatus(task, n);
		if (doneTasks == totalTasks) {
			view.setStatusVisible(false, lc == null);
			if (lc == null) view.requestFocusOnField();
			if (!connectToServer()) {
			    view.setVisible(false);
			}
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
        if (lc != null) {
        	login(lc);
        	return;
        }
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

    /** Fails to log in. */
    void onLoginFailure()
    {
    	if (view != null) view.onLoginFailure();
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
			this.lc = lc;
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
	 * Reacts to state changes fired by the {@link ScreenLogin}.
	 * @see WindowStateListener#windowStateChanged(WindowEvent)
	 */
	public void windowStateChanged(WindowEvent e)
	{
		Object src = e.getSource();
		int state = e.getNewState();
		if (src instanceof ScreenLogin) setWindowState(view, state);
		if (view != null) view.setAlwaysOnTop(state == JFrame.NORMAL);
	}

	/**
	 * Resets the flag when one of the windows loses focus.
	 * @see WindowFocusListener#windowLostFocus(WindowEvent)
	 */
	public void windowLostFocus(WindowEvent e)
	{
		if (e.getOppositeWindow() == null) {
			if (view != null) view.setAlwaysOnTop(false);
		}
	}
	
	/**
	 * Required by the {@link WindowFocusListener} I/F but no-operation
	 * implementation in our case.
	 * @see WindowFocusListener#windowGainedFocus(WindowEvent)
	 */
	public void windowGainedFocus(WindowEvent e) {}
	
}
