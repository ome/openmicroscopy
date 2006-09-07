/*
 * org.openmicroscopy.shoola.env.ui.TaskBarManager
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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.Container;
import org.openmicroscopy.shoola.env.data.DSOutOfServiceException;
import org.openmicroscopy.shoola.env.data.DataServicesFactory;
import org.openmicroscopy.shoola.env.data.events.ExitApplication;
import org.openmicroscopy.shoola.env.data.events.ServiceActivationRequest;
import org.openmicroscopy.shoola.env.data.events.ServiceActivationResponse;
import org.openmicroscopy.shoola.env.event.AgentEvent;
import org.openmicroscopy.shoola.env.event.AgentEventListener;
import org.openmicroscopy.shoola.env.event.EventBus;

/** 
 * Creates and manages the {@link TaskBarView}.
 * It acts as a controller.
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
class TaskBarManager
	implements AgentEventListener
{

	/** The view this controller is managing. */
	private TaskBarView		view;
	
	/** Reference to the container. */
	private Container		container;

	/**
	 * Synchronizes the enabled state of the connection-related buttons
	 * according to the current connection state. 
	 */
	private void synchConnectionButtons()
	{
		boolean connected = false;
		try {
			DataServicesFactory dsf = 
									DataServicesFactory.getInstance(container);
			 connected = dsf.isConnected();
		} catch (DSOutOfServiceException oose) {}
		view.getButton(TaskBarView.CONNECT_BTN).setEnabled(!connected);
		view.getButton(TaskBarView.CONNECT_MI).setEnabled(!connected);
		view.getButton(TaskBarView.DISCONNECT_BTN).setEnabled(connected);
		view.getButton(TaskBarView.DISCONNECT_MI).setEnabled(connected);	
	}
	
	/** The action associated to the connection-related buttons. */
	private void doManageConnection()
	{
		try {
			DataServicesFactory dsf = 
									DataServicesFactory.getInstance(container);
			 if (dsf.isConnected()) {
			 	dsf.shutdown();
			 	synchConnectionButtons();
			 } else {
			 	EventBus bus = container.getRegistry().getEventBus();
			 	bus.post(new ServiceActivationRequest(
									ServiceActivationRequest.DATA_SERVICES));
			 }
		} catch (DSOutOfServiceException oose) {
			synchConnectionButtons();
		} 
	}
	
	/**
	 * The exit action.
	 * Just forwards to the container.
	 */
	private void doExit() { container.exit(); }
	
	/**
	 * Temporary action to notify the user that the action associated to a
	 * given button hasn't been implemented yet.
	 */
	private void notAvailable()
	{
		UserNotifier un = container.getRegistry().getUserNotifier();
		un.notifyInfo("Not Available", 
						"Sorry, this functionality is not yet available.");
	}
	
	/**
	 * Attaches the {@link #notAvailable() not-available} action to all buttons
	 * whose functionality hasn't been implemented yet.
	 */
	private void attachNoOpListeners()
	{
		ActionListener noOp = new ActionListener() {		
			public void actionPerformed(ActionEvent ae) { notAvailable(); }
		};
		view.getButton(TaskBarView.WELCOME_MI).addActionListener(noOp);
		view.getButton(TaskBarView.HELP_MI).addActionListener(noOp);
		view.getButton(TaskBarView.HOWTO_MI).addActionListener(noOp);
		view.getButton(TaskBarView.UPDATES_MI).addActionListener(noOp);
		view.getButton(TaskBarView.ABOUT_MI).addActionListener(noOp);
		view.getButton(TaskBarView.HELP_BTN).addActionListener(noOp);
	}
	
	/**
	 * Attaches the {@link #doManageConnection() manage-connection} action to
	 * all connection-related buttons.
	 */
	private void attachConnectionListeners()
	{
		ActionListener conx = new ActionListener() {		
			public void actionPerformed(ActionEvent ae) { 
				doManageConnection();
			}
		};
		view.getButton(TaskBarView.CONNECT_BTN).addActionListener(conx);
		view.getButton(TaskBarView.CONNECT_MI).addActionListener(conx);
		view.getButton(TaskBarView.DISCONNECT_BTN).addActionListener(conx);
		view.getButton(TaskBarView.DISCONNECT_MI).addActionListener(conx);
	}
	
	/**
	 * Attaches the {@link #doExit() exit} action to all exit buttons and
	 * fires {@link #synchConnectionButtons()} when the window is first open.
	 */
	private void attachOpenExitListeners()
	{
		view.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we) { doExit(); }
			public void windowOpened(WindowEvent we) { 
				synchConnectionButtons();
			}
		});
		ActionListener exit = new ActionListener() {		
			public void actionPerformed(ActionEvent ae) { doExit(); }
		};
		view.getButton(TaskBarView.EXIT_MI).addActionListener(exit);
		view.getButton(TaskBarView.EXIT_BTN).addActionListener(exit);
	}
	
	/**
	 * Registers the necessary listeners with the view and also registers with
	 * the event bus.
	 */
	private void attachListeners()
	{
		attachOpenExitListeners();
		attachConnectionListeners();
		attachNoOpListeners();
		EventBus bus = container.getRegistry().getEventBus();
		bus.register(this, ServiceActivationResponse.class);
        bus.register(this, ExitApplication.class);
	}
	
	/**
	 * Creates this controller along with its view and registers the necessary
	 * listeners with the view.
	 *  
	 * @param c		Reference to the container.
	 */
	TaskBarManager(Container c) 
	{
		container = c;
		view = new TaskBarView(IconManager.getInstance(
													container.getRegistry()));
		attachListeners();												
	}
	
	/**
	 * Returns the view component.
	 * 
	 * @return	See above.
	 */
	TaskBar getView() { return view; }
	
	/**
	 * Intercepts {@link ServiceActivationResponse} events in order to keep
	 * the connection-related buttons in synch with the actual state of the
	 * connection.
     * @see AgentEventListener#eventFired(AgentEvent)
	 */
	public void eventFired(AgentEvent e) 
	{
		if (e instanceof ServiceActivationResponse)	synchConnectionButtons();
        else if (e instanceof ExitApplication) doExit();
	}

}
