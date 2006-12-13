/*
 * .MainPanel 
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
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */
package src.adminTool.main;

//Java imports
import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;

import src.adminTool.omero.LoginHandler;
import src.adminTool.ui.StatusBar;
import src.adminTool.ui.messenger.DebugMessenger;

//Third-party libraries

//Application-internal dependencies
/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class MainPanel
	extends JPanel
{
	public  boolean				loggedIn;
	public StatusBar			statusBar;
	private	LoginHandler 		loginHandler;
	private	AdminActions 		adminActions;
	private JFrame 				window;
	private	String				currentUser;
	
	public MainPanel(JFrame parentWindow)
	{
		window = parentWindow;
		loggedIn = false;
		createStatusBar();
		addStatusBar();
	}
	
	
	public void startLogin()
	{
		statusBar.setStatusIcon("resources/graphx/server_trying16.png",
	        "Trying to connect.");
	    loginHandler = new LoginHandler(this);
		if(loggedIn)
		{
			createAdminActions();
		}
		window.setSize(800,500);	
	}
	
	void createStatusBar()
	{
		statusBar = new StatusBar();
		statusBar.setStatusIcon("resources/graphx/server_disconn16.png",
         "Not connected to Server. Please login for administration options.");

	}
	
	void addStatusBar()
	{
		this.setLayout(new BorderLayout());
		this.add(statusBar, BorderLayout.SOUTH);
	}

	void createAdminActions()
	{
		try
		{
		adminActions = new AdminActions(loginHandler.getMetadataStore(), loginHandler.getUsername());
		this.add(adminActions, BorderLayout.CENTER);
		}
		catch(Exception e)
		{
			DebugMessenger debug = new DebugMessenger(null,"An Unexpected " +
					"Error has Occurred", true, e);

		}
	}
}


