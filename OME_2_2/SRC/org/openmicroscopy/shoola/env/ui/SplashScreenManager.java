/*
 * org.openmicroscopy.shoola.env.ui.SplashScreenManager
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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

//Third-party libraries

//Application-internal dependencies

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
	implements ActionListener
{
	
	/** The component's UI. */
	private SplashScreenView	view;
	
	/** Tells whether or not the splash screen window is open. */
	private boolean				isOpen;
	
	/** Filled in with the user's login when available. */
	private UserCredentials		userCredentials;

	/** The current number of tasks to be executed. */
	private int					totalTasks;
	
	/** The current number of tasks that have been executed. */
	private int					doneTasks;
	
	/**
	 * Creates the splash screen component.
	 * Creates a new instance of this manager, of its corresponding UI
	 * ({@link SplashScreenView}), and links them as needed.
	 * 
	 * @param uc	This will be filled in with the user's login when available.
	 */
	SplashScreenManager(UserCredentials uc)
	{
		userCredentials = uc;
		view = new SplashScreenView();
		view.user.addActionListener(this);
		view.pass.addActionListener(this);
		view.login.addActionListener(this);
		isOpen = false;
		doneTasks = 0;
	}

	/**
	 * Implemented as specified by {@link SplashScreen}.
	 * @see SplashScreenInit#open()
	 */
	public void open()
	{
		if (view == null)	return;  //close() has already been called.
		view.setVisible(true);
		isOpen = true;	
	}

	/**
	 * Implemented as specified by {@link SplashScreen}.
	 * @see SplashScreenInit#close()
	 */
	public void close()
	{
		if (view == null)	return;  //close() has already been called.
		view.dispose();
		view = null;
		isOpen = false;
	}

	/**
	 * Implemented as specified by {@link SplashScreen}.
	 * @see SplashScreen#setTotalTasks(int)
	 */
	public void setTotalTasks(int value)
	{
		if (!isOpen)	return;
		totalTasks = value;
		//NB: Increment to show that the execution process is finished 
		// i.e. all tasks executed.
		totalTasks++;	
		view.progressBar.setMinimum(0);
		view.progressBar.setMaximum(value);
		view.progressBar.setValue(0);
	}
	
	/**
	 * Implemented as specified by {@link SplashScreen}.
	 * @see SplashScreen#updateProgress(java.lang.String, int)
	 */
	public void updateProgress(String task)
	{
		if (!isOpen)	return;
		view.currentTask.setText(task);
		//view.progressBar.setValue(count);
		view.progressBar.setValue(doneTasks++);
		if (doneTasks == totalTasks) view.progressBar.setVisible(false);
	}
	
	/** 
	 * Handles action events fired by the login fields and button.
	 * Once user name and password have been entered, the login fields and
	 * button will be disabled. 
	 */
	public void actionPerformed(ActionEvent e)
	{
		StringBuffer buf = new StringBuffer();
		buf.append(view.pass.getPassword());
		String usr = view.user.getText(), psw = buf.toString();
		if (usr == null || usr.length() == 0) {
			UserNotifier un = UIFactory.makeUserNotifier();
			un.notifyWarning("Login Incomplete", "Please enter a user name");
		} else {
			if (psw == null || psw.length() == 0) {
				UserNotifier un = UIFactory.makeUserNotifier();
				un.notifyWarning("Login Incomplete", "Please enter a password");
			} else {
				userCredentials.set(usr, psw);
				view.user.setEnabled(false);
				view.pass.setEnabled(false);
				view.login.setEnabled(false);
			}
		}
	}

}
