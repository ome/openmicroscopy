/*
 * org.openmicroscopy.shoola.util.ui.login.ScreenLoginDialog 
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
package org.openmicroscopy.shoola.util.ui.login;


//Java imports
import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.Icon;
import javax.swing.JDialog;

//Third-party libraries

//Application-internal dependencies

/** 
 * Creates a login dialog.
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
public class ScreenLoginDialog 
	extends JDialog
	implements PropertyChangeListener
{

	/** Reference to the view. */
	private ScreenLogin view;
	
	/** Sets the properties of the dialog. */
	private void setProperties()
	{
		setTitle(view.getTitle());
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setResizable(false);
		setUndecorated(true);
		toFront();
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param title		The frame's title.
	 * @param logo		The frame's background logo. 
	 * 					Mustn't be <code>null</code>.
	 * @param frameIcon The image icon for the window.
	 */
	public ScreenLoginDialog(String title, Icon logo, Image frameIcon)
	{
		this(title, logo, frameIcon, null, null);
	}

	/**
	 * Creates a new instance.
	 * 
	 * @param logo		The frame's background logo. 
	 * 					Mustn't be <code>null</code>.
	 * @param frameIcon The image icon for the window.
	 * @param version	The version of the software.
	 */
	public ScreenLoginDialog(Icon logo, Image frameIcon, String version)
	{
		this(null, logo, frameIcon, version, null);
	}

	/**
	 * Creates a new instance.
	 * 
	 * @param logo		The frame's background logo. 
	 * 					Mustn't be <code>null</code>.
	 * @param frameIcon The image icon for the window.
	 */
	public ScreenLoginDialog(Icon logo, Image frameIcon)
	{
		this(null, logo, frameIcon, null, null);
	}

	/**
	 * Creates a new instance.
	 * 
	 * @param title		 The frame's title.
	 * @param logo		 The frame's background logo. 
	 * 					 Mustn't be <code>null</code>.
	 * @param frameIcon  The image icon for the window.
	 * @param version	 The version of the software.
	 * @param defaultPort The default port.
	 */
	public ScreenLoginDialog(String title, Icon logo, Image frameIcon, 
			String version, String defaultPort)
	{
		view = new ScreenLogin(title, logo, frameIcon, version, defaultPort);
		view.addPropertyChangeListener(this);
		setProperties();
		getContentPane().add(view.getContentPane().getComponent(0));
		setSize(view.getSize());
		setPreferredSize(view.getPreferredSize());
		getRootPane().setDefaultButton(view.getRootPane().getDefaultButton());
		setCursor(view.getCursor());
	}
	
	/** 
     * Shows or hides the progress bar and the tasks label. 
     * 
     * @param b Pass <code>true</code> to show, <code>false</code> to hide.
     */
    public void setStatusVisible(boolean b) { view.setStatusVisible(b, !b); }
    
	/** 
	 * Modifies the text and the tool tip of the <code>Quit</code> button.
	 * 
	 * @param text The text to display.
	 */
	public void setQuitButtonText(String text) { view.setQuitButtonText(text); }
	
	/** Closes the dialog. */
	public void close()
	{
		view.close();
		setVisible(false);
		dispose();
	}

	/**
	 * Forwards the call to the <code>ScreenLogin</code>.
	 * 
	 * @param fieldID 	The textField's id. One of the following constants:
	 * 					{@link ScreenLogin#USERNAME_FIELD} or 
	 * 					{@link ScreenLogin#PASSWORD_FIELD}.
	 * @see ScreenLogin#cleanField(int)
	 */
	public void cleanField(int fieldID)
	{ 
		view.cleanField(fieldID);
		setCursor(view.getCursor());
	}

	/**
	 * Forwards the call to the <code>ScreenLogin</code>.
	 * 
	 * @see ScreenLogin#requestFocusOnField()
	 */
	public void onLoginFailure() 
	{
		view.onLoginFailure();
		view.requestFocusOnField();
		setCursor(view.getCursor());
	}
	
	/**
	 * Forwards the call to the <code>ScreenLogin</code>.
	 * 
	 * @param connectionSpeed The value to set.
	 * @see ScreenLogin#showConnectionSpeed(boolean)
	 */
	public void showConnectionSpeed(boolean connectionSpeed)
	{
		view.showConnectionSpeed(connectionSpeed);
	}

	/**
     * Sets the encryption parameters.
     * 
     * @param encrypted Pass <code>true</code> to encrypt the data transfer,
     * 					<code>false</code> otherwise.
     * @param configurable Pass <code>true</code> to allow the user to interact
     * with the encryption controls, <code>false</code> otherwise.
     */
    public void setEncryptionConfiguration(boolean encrypted,
    		boolean configurable)
    {
    	view.setEncryptionConfiguration(encrypted, configurable);
    }
    
    /**
     * Indicates if the user can modify or not the host name from the UI.
     * 
     * @param hostName The hostname.
     * @param configurable Pass <code>true</code> to allow to change the 
     * host name, <code>false</code> otherwise.
     */
    public void setHostNameConfiguration(String hostName, boolean configurable)
    {
    	view.setHostNameConfiguration(hostName, configurable);
    }
    
	/**
	 * Forwards property fired by the view.
	 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt)
	{
		String name = evt.getPropertyName();
		if (ScreenLogin.LOGIN_PROPERTY.equals(name)) {
			setCursor(view.getCursor());
			repaint();
		}
		firePropertyChange(name, evt.getOldValue(), evt.getNewValue());
	}

}
