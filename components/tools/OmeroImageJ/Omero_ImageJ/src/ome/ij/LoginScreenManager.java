/*
 * ome.ij.LoginScreenManager 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2009 University of Dundee. All rights reserved.
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
package ome.ij;


//Java imports
import java.awt.Dimension;
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
import ij.IJ;

//Application-internal dependencies
import ome.ij.data.ServicesFactory;
import ome.ij.dm.TreeViewer;
import ome.ij.dm.TreeViewerFactory;
import org.openmicroscopy.shoola.util.ui.IconManager;
import org.openmicroscopy.shoola.util.ui.login.LoginCredentials;
import org.openmicroscopy.shoola.util.ui.login.ScreenLogin;
import org.openmicroscopy.shoola.util.ui.login.ScreenLogo;

/** 
 * Handles the log in sequence.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
class LoginScreenManager 
	implements PropertyChangeListener, WindowFocusListener, WindowStateListener
{

	/** The title of the splash screens. */
	static final String	TITLE = "Open Microscopy Environment";
	 
	/** The version the plugin. */
	private static final String VERSION = "v1.0";
	
	/** The default port value. */
	private static final String PORT = ""+4064;
	
	/** The title of the dialog if we cannot connect. */
	private static final String CONNECTION = "Connection";
	
	/** Reference to the logo. */
	private ScreenLogo	logo;
	
	/** Reference to the login. */
	private ScreenLogin login;
	
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
	
	/** 
	 * Attempts to log in.
	 *  
	 * @param lc The login credentials.
	 */
	private void login(LoginCredentials lc)
	{
		int index = ServicesFactory.getInstance().login(lc);
		switch (index) {
			case ServicesFactory.NAME_FAILURE_INDEX:
				IJ.showMessage(CONNECTION, "User name is not valid.");
				login.cleanFields();
				login.requestFocusOnField();
				return;
			case ServicesFactory.PASSWORD_FAILURE_INDEX:
				IJ.showMessage(CONNECTION, "Password is not valid.");
				login.cleanField(ScreenLogin.PASSWORD_FIELD);
				login.requestFocusOnField();
				return;
			case ServicesFactory.DNS_INDEX:
				IJ.showMessage(CONNECTION, "Failed to log onto OMERO.\n" +
		                "Please check the server address or try again later.");
				login.setControlsEnabled(true);
				return;
			case ServicesFactory.CONNECTION_INDEX:
				IJ.showMessage(CONNECTION, "Failed to log onto OMERO.\n" +
		                "Please check the port or try again later.");
				login.setControlsEnabled(true);
				return;
			case ServicesFactory.ACTIVE_INDEX:
				IJ.showMessage(CONNECTION, "Your user account is no " +
						"longer active.\nPlease" +
						" contact your administrator.");
				login.setControlsEnabled(true);
				return;
			case ServicesFactory.CONFIGURATION_INDEX:
				IJ.showMessage(CONNECTION, "Please unset ICE_CONFIG.");
				login.setControlsEnabled(true);
				return;
			case ServicesFactory.PERMISSION_INDEX:
				IJ.showMessage(CONNECTION, "Failed to log onto OMERO.\n" +
		                "Please check the username and/or password\n " +
		                "or try again later.");
				login.setControlsEnabled(true);
				login.cleanField(ScreenLogin.PASSWORD_FIELD);
				login.requestFocusOnField();
				return;
			case ServicesFactory.SUCCESS_INDEX:
				close(false);
				TreeViewer viewer = TreeViewerFactory.getTreeViewer();
				if (viewer != null) viewer.activate();
		}
	}
	
	/** 
	 * Closes the application. 
	 * 
	 * @param cancel Pass <code>true</code> to cancel any on-going
	 * 				 connection's attempt, <code>false</code> otherwise.
	 */
	private void close(boolean cancel)
	{
		if (cancel) ServicesFactory.getInstance().exitPlugin();
		logo.close();
		login.close();
	}
	
	/** Creates a new instance. */
	LoginScreenManager()
	{
		
	}

	/** Starts. */
	void start()
	{
		//if we are already connected, bring up the window.
		if (ServicesFactory.getInstance().isConnected()) {
			TreeViewer viewer = TreeViewerFactory.getTreeViewer();
			if (viewer != null) viewer.activate();
		} else {
			Icon splashLogin = IconManager.getLoginBackground();
			login = new ScreenLogin(TITLE, splashLogin, 
					IconManager.getOMEImageIcon(), VERSION, PORT);
			logo = new ScreenLogo(TITLE, IconManager.getImageJSplashscreen(), 
					IconManager.getOMEImageIcon());
			logo.setStatusVisible(false);
			login.addPropertyChangeListener(this);
			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			Dimension d = logo.getExtendedSize();
			Dimension dlogin = login.getPreferredSize();
			int totalHeight = d.height+dlogin.height;
			logo.setBounds((screenSize.width-d.width)/2, 
		    		 	(screenSize.height-totalHeight)/2, d.width, 
		    		 	logo.getSize().height);
			logo.addWindowStateListener(this);
			logo.addWindowFocusListener(this);
			Rectangle r = logo.getBounds();
			login.setBounds(r.x, r.y+d.height, dlogin.width, dlogin.height);
			login.addWindowStateListener(this);
			login.addWindowFocusListener(this);
			logo.setVisible(true);
			login.setVisible(true);
		}
	}
	
	/**
	 * Listens to property fired by the {@link ScreenLogin}.
	 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt)
	{
		String name = evt.getPropertyName();
		if (ScreenLogin.LOGIN_PROPERTY.equals(name)) {
			LoginCredentials lc = (LoginCredentials) evt.getNewValue();
			if (lc != null) login(lc);
		} else if (ScreenLogin.QUIT_PROPERTY.equals(name)) {
			 close(true);
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
		if (src instanceof ScreenLogo) setWindowState(login, state);
		else if (src instanceof ScreenLogin) setWindowState(logo, state);
		if (login != null) login.setAlwaysOnTop(state == JFrame.NORMAL);
		logo.setAlwaysOnTop(state == JFrame.NORMAL);
	}

	/**
	 * Resets the flag when one of the windows loses focus.
	 * @see WindowFocusListener#windowLostFocus(WindowEvent)
	 */
	public void windowLostFocus(WindowEvent e)
	{
		if (e.getOppositeWindow() == null) {
			if (login != null) login.setAlwaysOnTop(false);
			if (login != null) logo.setAlwaysOnTop(false);
		}
	}
	/**
	 * Required by the {@link WindowFocusListener} I/F but no-operation
	 * implementation in our case.
	 * @see WindowFocusListener#windowGainedFocus(WindowEvent)
	 */
	public void windowGainedFocus(WindowEvent e) {}
	
}
