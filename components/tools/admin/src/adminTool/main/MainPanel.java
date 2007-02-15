/*
 * .MainPanel 
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package src.adminTool.main;

// Java imports
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.swing.JFrame;
import javax.swing.JPanel;

import src.adminTool.omero.OMEROMetadataStore;
import src.adminTool.ui.ImageFactory;
import src.adminTool.ui.SplashScreenManager;
import src.adminTool.ui.StatusBar;
import src.adminTool.ui.messenger.DebugMessenger;

// Third-party libraries

// Application-internal dependencies

/**
 * 
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0 <small> (<b>Internal version:</b> $Revision$Date: $)
 *          </small>
 * @since OME3.0
 */
public class MainPanel extends JPanel implements ActionListener {
    /**
     * 
     */
    private static final long serialVersionUID = -3324298308687880815L;

    public StatusBar statusBar;

    private AdminActions adminActions;

    private JFrame window;

    private String currentUser;
    
    private SplashScreenManager splashManager;
    
    private OMEROMetadataStore store;

    public MainPanel(JFrame parentWindow) {
        window = parentWindow;
        adminActions = null;
        this.setLayout(new BorderLayout());
        createStatusBar();
        addStatusBar();
        statusBar.setStatusIcon(ImageFactory.get().image(
                ImageFactory.SERVER_CONNECT_TRYING), "Trying to connect.");
        splashManager = new SplashScreenManager(this);
        splashManager.open();
    }

 
    void createStatusBar() {
        statusBar = new StatusBar();
        statusBar
                .setStatusIcon(ImageFactory.get().image(
                        ImageFactory.SERVER_CONNECT_FAILED),
                        "Not connected to Server. Please login for administration options.");

    }

    void addStatusBar() {
        this.add(statusBar, BorderLayout.SOUTH);
    }

    void createAdminActions() {
        try {
        	if(adminActions == null)
        	{
        		adminActions = new AdminActions(store,
                    splashManager.username);
        		this.add(adminActions, BorderLayout.CENTER);
        		//adminActions.setSize(window.getWidth()-10, window.getHeight()-10);
        		adminActions.setVisible(true);
        		window.repaint();
        	}
        } catch (Exception e) {
            DebugMessenger debug = new DebugMessenger(null, "An Unexpected "
                    + "Error has Occurred", true, e);

        }
    }
    
    
    private boolean isValidLogin(String username, String password, 
    		String server, String port)  {
    
    	try {
			InetAddress address = 
			    InetAddress.getByName(server);
	        store = new OMEROMetadataStore(username, password, server, port);
		} catch (UnknownHostException e) {
			store = null;
		}
       
    	if(store == null)
    	 return false;
       return true;
    }

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		if( e.getActionCommand().equals("Login"))
		{
			
			if( isValidLogin(splashManager.username, splashManager.password, splashManager.server, "1099") )
			{
				splashManager.close();
				createAdminActions();
				window.setSize(800, 500);
				statusBar.setStatusIcon(ImageFactory.get().image(
	                ImageFactory.SERVER_CONNECTED), "Server connected.");

			}
			else
			{
				splashManager.incorrectLogin("Could not log into Server : " 
						+ splashManager.server + "\n" + "Please check username/password.");
				statusBar.setStatusIcon(ImageFactory.get().image(
	                    ImageFactory.SERVER_CONNECT_FAILED),
	                    "Server connection failure. Please try to login again.");

			}
		}
		if( e.getActionCommand().equals("Quit"))
		{
			splashManager.close();
			window.dispose();
		}
		
	}
}
