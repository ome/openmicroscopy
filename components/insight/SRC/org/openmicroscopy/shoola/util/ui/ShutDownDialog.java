/*
 * org.openmicroscopy.shoola.util.ui.ShutDownDialog
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2012 University of Dundee & Open Microscopy Environment.
 *  All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.Timer;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.NetworkChecker;

/**
 *  Window uses to let the user know the time before the application will shut
 *  down. At any point, the user can decide to cancel.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since Beta4.4
 */
public class ShutDownDialog 
	extends NotificationDialog
	implements ActionListener
{

	/** The time to wait before shutting down.*/
	private static final int DEFAULT_TIME = 120;//2mins
	
	/** The default shutdown message.*/
	private static final String SHUTDOWN_MESSAGE = 
			"If the connection cannot be re-established,\n"+
			"the application will shut down in ";
	
	/** The time used to indicates the remaining time.*/
	private Timer timer;
	
	/** The remaining time before shutting down.*/
	private int remainingTime;
	
	/** Use to check if the network is up.*/
	private NetworkChecker checker;
	
	/** 
	 * Formats the displayed text. 
	 * 
	 * @param time The time to format.
	 */
	private void formatText(int time)
	{
		StringBuffer buffer = new StringBuffer();
		buffer.append(message);
		buffer.append(SHUTDOWN_MESSAGE);
		buffer.append(UIUtilities.formatTimeInSeconds(time));
		buffer.append(".");
		contentPanel.setDescription(buffer.toString());
		repaint();
	}
	/**
	 * Initializes the component.
	 * 
	 * @param time The time to wait before shutting down.
	 */
	private void initialize(int time)
	{
		checker = new NetworkChecker();
		removeWindowListener(windowAdapter);
		remainingTime = time;
		okButton.setText("Shut Down");
		okButton.setToolTipText("Shut down the application.");
		formatText(time);
		int speed = 1000;
		int pause = 1000;
		timer = new Timer(speed, this);
		timer.setInitialDelay(pause);
		timer.start();
		pack();
	}
	
	/**
	 * Creates a new dialog.
	 * You have to call {@link #setVisible(boolean)} to actually display it
     * on screen.
	 * 
	 * @param owner The parent window.
	 * @param title The title to display on the title bar.
	 * @param message The notification message.
	 * @param time The time to wait before shutting down.
	 */
	public ShutDownDialog(JDialog owner, String title, String message,
			int time) {
		super(owner, title, message, null);
		initialize(time);
	}
	
	/**
	 * Creates a new dialog.
	 * You have to call {@link #setVisible(boolean)} to actually display it
     * on screen.
	 * 
	 * @param owner The parent window.
	 * @param title The title to display on the title bar.
	 * @param message The notification message.
	 * @param time The time to wait before shutting down.
	 */
	public ShutDownDialog(JFrame owner, String title, String message, int time)
	{
		super(owner, title, message, null);
		initialize(time);
	}

	/**
	 * Creates a new dialog.
	 * You have to call {@link #setVisible(boolean)} to actually display it
     * on screen. The default time is set to {@link #DEFAULT_TIME}
	 * 
	 * @param owner The parent window.
	 * @param title The title to display on the title bar.
	 * @param message The notification message.
	 */
	public ShutDownDialog(JDialog owner, String title, String message) {
		this(owner, title, message, DEFAULT_TIME);
	}
	
	/**
	 * Creates a new dialog.
	 * You have to call {@link #setVisible(boolean)} to actually display it
     * on screen. The default time is set to {@link #DEFAULT_TIME}
	 * 
	 * @param owner The parent window.
	 * @param title The title to display on the title bar.
	 * @param message The notification message.
	 */
	public ShutDownDialog(JFrame owner, String title, String message)
	{
		this(owner, title, message, DEFAULT_TIME);
	}
	
	/** 
	 * Overridden to stop the timer.
	 */
	protected void close()
	{
		if (timer != null) timer.stop();
		super.close();
	}

	/** 
	 * Overridden to stop the timer.
	 */
	protected void cancel()
	{
		if (timer != null) timer.stop();
		super.cancel();
	}
	
	/**
	 * Decreases the time remaining before shutting down.
	 * 
	 * @param e The action to handle.
	 */
	public void actionPerformed(ActionEvent e)
	{
		remainingTime--;
		formatText(remainingTime);
		try {
			checker.isNetworkup();
			cancel();
			return;
		} catch (Exception ex) {
			//continue the network is still down.
		}
		if (remainingTime == 0) close();
	}
	
}
