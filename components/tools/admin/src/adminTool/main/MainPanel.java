/*
 * .MainPanel 
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package src.adminTool.main;

//Java imports
import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;

import src.adminTool.omero.LoginHandler;
import src.adminTool.ui.ImageFactory;
import src.adminTool.ui.StatusBar;
import src.adminTool.ui.messenger.DebugMessenger;

//Third-party libraries

//Application-internal dependencies

/** 
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
		statusBar.setStatusIcon(ImageFactory.get().image(ImageFactory.SERVER_CONNECT_TRYING),
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
		statusBar.setStatusIcon(ImageFactory.get().image(ImageFactory.SERVER_CONNECT_FAILED),
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


