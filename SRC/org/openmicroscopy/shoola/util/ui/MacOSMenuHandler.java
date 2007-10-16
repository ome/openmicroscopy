/*
 * org.openmicroscopy.shoola.util.ui.MacOSMenuHandler 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.util.ui;


//Java imports
import javax.swing.JFrame;

//Third-party libraries
import com.apple.eawt.Application;
import com.apple.eawt.ApplicationEvent;
import com.apple.eawt.ApplicationListener;

//Application-internal dependencies

/** 
 * Controls the behaviour of the <code>About</code> and <code>Quit</code>
 * menu items.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class MacOSMenuHandler
	extends Application
	implements ApplicationListener
{
	
	/** 
	 * Bound property indicating that the <code>About</code>
	 * menu item is selected.
	 */
	public static final String ABOUT_APPLICATION_PROPERTY = "aboutApplication";
	
	/** 
	 * Bound property indicating that the <code>Quit</code>
	 * menu item is selected.
	 */
	public static final String QUIT_APPLICATION_PROPERTY = "quitpplication";
	
	
	/** The parent of this class. */
	private JFrame parent;
	
	/** 
	 * Creates a new instance. 
	 * 
	 * @param parent The parent of this class. Mustn't be <code>null</code>.
	 */
	public MacOSMenuHandler(JFrame parent)
	{
		if (parent == null) 
			throw new IllegalArgumentException("No parent specified.");
		this.parent = parent;
		addApplicationListener(this);
	}

	/** 
	 * Handles the selection of the <code>About</code> 
	 * menu item.
	 * @see ApplicationListener#handleAbout(ApplicationEvent)
	 */
	public void handleAbout(ApplicationEvent ae) 
	{
		if (parent != null) {
			ae.setHandled(true);
			parent.firePropertyChange(ABOUT_APPLICATION_PROPERTY, 0, 1);
		}
	}

	/** 
	 * Handles the selection of the <code>About</code> 
	 * menu item.
	 * @see ApplicationListener#handleQuit(ApplicationEvent)
	 */
	public void handleQuit(ApplicationEvent ae)
	{
		if (parent != null) {
			ae.setHandled(true);
			parent.firePropertyChange(QUIT_APPLICATION_PROPERTY, 0, 1);
		}
	}

	/**
	 * Required by the {@linkApplicationListener} I/F but no-op implementation
	 * in our case.
	 * @see ApplicationListener#handleOpenApplication(ApplicationEvent)
	 */
	public void handleOpenApplication(ApplicationEvent ae) {}

	/**
	 * Required by the {@linkApplicationListener} I/F but no-op implementation
	 * in our case.
	 * @see ApplicationListener#handleOpenFile(ApplicationEvent)
	 */
	public void handleOpenFile(ApplicationEvent ae) {}

	/**
	 * Required by the {@linkApplicationListener} I/F but no-op implementation
	 * in our case.
	 * @see ApplicationListener#handlePreferences(ApplicationEvent)
	 */
	public void handlePreferences(ApplicationEvent ae) {}

	/**
	 * Required by the {@linkApplicationListener} I/F but no-op implementation
	 * in our case.
	 * @see ApplicationListener#handlePrintFile(ApplicationEvent)
	 */
	public void handlePrintFile(ApplicationEvent ae) {}

	/**
	 * Required by the {@linkApplicationListener} I/F but no-op implementation
	 * in our case.
	 * @see ApplicationListener#handleReOpenApplication(ApplicationEvent)
	 */
	public void handleReOpenApplication(ApplicationEvent ae) {}
	
}
