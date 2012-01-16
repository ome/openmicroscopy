/*
 * org.openmicroscopy.shoola.env.ui.TaskBarManager
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
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

package org.openmicroscopy.shoola.env.ui;

//Java imports
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.Icon;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

//Third-party libraries


//Application-internal dependencies
import org.openmicroscopy.shoola.env.Agent;
import org.openmicroscopy.shoola.env.Container;
import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.config.AgentInfo;
import org.openmicroscopy.shoola.env.config.OMEROInfo;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.DSOutOfServiceException;
import org.openmicroscopy.shoola.env.data.DataServicesFactory;
import org.openmicroscopy.shoola.env.data.events.ExitApplication;
import org.openmicroscopy.shoola.env.data.events.LogOff;
import org.openmicroscopy.shoola.env.data.events.ReconnectedEvent;
import org.openmicroscopy.shoola.env.data.events.SaveEventResponse;
import org.openmicroscopy.shoola.env.data.events.ServiceActivationRequest;
import org.openmicroscopy.shoola.env.data.events.ServiceActivationResponse;
import org.openmicroscopy.shoola.env.data.events.SwitchUserGroup;
import org.openmicroscopy.shoola.env.data.login.LoginService;
import org.openmicroscopy.shoola.env.data.login.UserCredentials;
import org.openmicroscopy.shoola.env.data.util.AgentSaveInfo;
import org.openmicroscopy.shoola.env.event.AgentEvent;
import org.openmicroscopy.shoola.env.event.AgentEventListener;
import org.openmicroscopy.shoola.env.event.EventBus;
import org.openmicroscopy.shoola.env.log.LogMessage;
import org.openmicroscopy.shoola.env.log.Logger;
import org.openmicroscopy.shoola.util.image.geom.Factory;
import org.openmicroscopy.shoola.util.ui.BrowserLauncher;
import org.openmicroscopy.shoola.util.ui.MacOSMenuHandler;
import org.openmicroscopy.shoola.util.ui.MessageBox;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.login.LoginCredentials;
import org.openmicroscopy.shoola.util.ui.login.ScreenLogin;
import org.openmicroscopy.shoola.util.ui.login.ScreenLoginDialog;
import org.openmicroscopy.shoola.util.file.IOUtil;

import pojos.ExperimenterData;

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
public class TaskBarManager
	implements AgentEventListener, PropertyChangeListener
{

	/** The window's title. */
	static final String				TITLE_ABOUT = "About";
	
	/** Bound property indicating to display the activity dialog. */
	static final String				ACTIVITIES_PROPERTY = "activities";
	
	/** The value of the tag to find. */
	private static final String		A_TAG = "a";
	
	/** The title displayed before switching group. */
	private static final String		SWITCH_GROUP_TITLE = "Switch Group";
	
	/** The text displayed before switching group. */
	private static final String		SWITCH_GROUP_TEXT = 
		"Switching group will remove data from the display. " +
		"\nDo you want to continue?";
	
	/** The title displayed before closing the application. */
	private static final String		CLOSE_APP_TITLE = "Exit Application";
		
	/** The text displayed before closing the application. */
	private static final String		CLOSE_APP_TEXT = 
		"Do you really want to close the application?";
		
	/** The title displayed before logging out. */
	private static final String		LOGOUT_TITLE = "Log out";
		
	/** The text displayed before logging out. */
	private static final String		LOGOUT_TEXT = 
		"Do you really want to disconnect from the server?";
	
	/** The view this controller is managing. */
	private TaskBarView				view;
	
	/** Reference to the container. */
	private Container				container;

	/** The software update dialog. */
	private SoftwareUpdateDialog	suDialog;
	
    /** Login dialog. */
    private ScreenLoginDialog 		login;
    
    /** Flag indicating if the connection was successful or not. */
    private boolean					success;
    
	/** 
	 * Parses the passed file to determine the value of the URL.
	 * 
	 * @param refFile	The file to parse
	 * @return See above.
	 */
	private String parse(String refFile)
	{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new File(refFile));
            //parse 
            
            NodeList list = document.getElementsByTagName(A_TAG); //length one
            Node n;
            String url = null;
            for (int i = 0; i < list.getLength(); ++i) {
                n = list.item(i);
                url = n.getFirstChild().getNodeValue();
            }
            return url;
        } catch (Exception e) { 
        	if (suDialog != null) suDialog.close();
        	Logger logger = container.getRegistry().getLogger();
			LogMessage msg = new LogMessage();
	        msg.print("Error while saving.");
	        msg.print(e);
	        logger.error(this, msg);
        }   
        return null;
	}
	
	/**
	 * Reads the content of the specified file and returns it as a string.
	 * 
	 * @param file	Absolute pathname to the file.
	 * @return See above.
	 */
	private String loadAbout(String file)
	{
		String message;
		try {
			InputStream fis = IOUtil.readConfigFile(file);
			BufferedReader in = new BufferedReader(new InputStreamReader(fis));
			StringBuffer buffer = new StringBuffer();
			int number = 0;
			String line;
			while (true) {
                line = in.readLine();
                if (line == null) break;
                if (number != 0) buffer.append(line);
                number++;
            }
			in.close();
            message = buffer.toString();
		} catch (Exception e) {
			message = "Error: Cannot find the About file.";
			Logger logger = container.getRegistry().getLogger();
			LogMessage msg = new LogMessage();
	        msg.print(message);
	        msg.print(e);
	        logger.error(this, msg);
		}
		return message;
	}
	
	/**
	 * Synchronizes the enabled state of the connection-related buttons
	 * according to the current connection state. 
	 */
	private void synchConnectionButtons()
	{
		boolean connected = false;
		try {
			DataServicesFactory f = DataServicesFactory.getInstance(container);
			 connected = f.isConnected();
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
			DataServicesFactory f = DataServicesFactory.getInstance(container);
			if (f.isConnected()) {
				f.shutdown();
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
	 * Handles event sent when the data has been saved.
	 * 
	 * @param e The event to handle.
	 */
	private void handleSaveEventResponse(SaveEventResponse e)
	{
		if (e == null) return;
		/*
		Agent a = e.getAgent();
		Integer r = exitResponses.get(a);
		if (r != null) {
			int v = r.intValue()-1;
			if (v == 0) exitResponses.remove(a);
			//else exitResponses.put(a, v);
		}
		if (exitResponses.size() == 0) container.exit();
		*/
	}

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
	
	/** Brings up on screen a dialog to send comment. */
	private void sendComment()
	{
		Registry reg = container.getRegistry();
		UserNotifier un = reg.getUserNotifier();
		ExperimenterData exp = (ExperimenterData) reg.lookup(
				LookupNames.CURRENT_USER_DETAILS);
		String email = "";
		if (exp != null) email = exp.getEmail();
		un.submitMessage(email, "");
	}
	
	/**
	 * Returns instances of <code>Agent</code> that can be saved.
	 * 
	 * @return See above.
	 */
	private Map<Agent, AgentSaveInfo> getInstancesToSave()
	{
		List agents = (List) container.getRegistry().lookup(LookupNames.AGENTS);
		Iterator i = agents.iterator();
		Agent agent;
		AgentSaveInfo info;
		Map<Agent, AgentSaveInfo> l = new HashMap<Agent, AgentSaveInfo>();
		AgentInfo ai;
		while (i.hasNext()) {
			ai = (AgentInfo) i.next();
			if (ai.isActive()) {
				agent = ai.getAgent();
				if (agent != null) {
					info = agent.getDataToSave();
					if (info != null && info.getCount() > 0)
						l.put(agent, info);
				}
			}
		}
		return l;
	}
	
	/**
	 * Switches user group, notifies the agents to save data before switching.
	 * 
	 * @param evt The event to handle.
	 */
	private void handleSwitchUserGroup(SwitchUserGroup evt)
	{
		if (evt == null) return;
		//Do we have data to save.
		
		CheckoutBox box = new CheckoutBox(view, SWITCH_GROUP_TITLE, 
				SWITCH_GROUP_TEXT, getInstancesToSave());
		if (box.centerMsgBox() == MessageBox.YES_OPTION) {
			Map<Agent, AgentSaveInfo> map = box.getInstancesToSave();
			UserNotifierImpl un = (UserNotifierImpl) 
				container.getRegistry().getUserNotifier();
			List<Object> nodes = new ArrayList<Object>();
			if (map != null) {
				Iterator i = map.entrySet().iterator();
				Entry entry;
				Agent agent;
				AgentSaveInfo info;
				while (i.hasNext()) {
					entry = (Entry) i.next();
					agent = (Agent) entry.getKey();
					info = (AgentSaveInfo) entry.getValue();
					agent.save(info.getInstances());
					nodes.add(info);
				}
			}
			//nodes.add(evt.getExperimenterData());
			//un.notifySaving(nodes, null);
			Registry reg = container.getRegistry();
			UserNotifierLoader loader = new SwitchUserLoader(
					reg.getUserNotifier(), reg, evt.getExperimenterData(), 
					evt.getGroupID());
			loader.load();
		}
	}
	
	/**
	 * Logs off from the current server.
	 * 
	 * @param evt The event to handle.
	 */
	private void handleLogOff(LogOff evt)
	{
		if (evt == null) return;
		if (!((LogOff) evt).isAskQuestion()) {
			logOut();
			return;
		}
		IconManager icons = IconManager.getInstance(container.getRegistry());
		Map<Agent, AgentSaveInfo> instances = getInstancesToSave();
		CheckoutBox msg = new CheckoutBox(view, LOGOUT_TITLE, 
				LOGOUT_TEXT, 
				icons.getIcon(IconManager.QUESTION), instances);
		if (msg.centerMsgBox() == MessageBox.YES_OPTION) {
			Map<Agent, AgentSaveInfo> map = msg.getInstancesToSave();
			if (map == null || map.size() == 0) {
				logOut();
			} else {
				List<Object> nodes = new ArrayList<Object>();
				Iterator i = map.entrySet().iterator();
				Entry entry;
				Agent agent;
				AgentSaveInfo info;
				while (i.hasNext()) {
					entry = (Entry) i.next();
					agent = (Agent) entry.getKey();
					info = (AgentSaveInfo) entry.getValue();
					agent.save(info.getInstances());
					nodes.add(info);
				}
				logOut();
			}
		}
	}
	
	private void reconnect()
	{
		Image img = IconManager.getOMEImageIcon();
    	Object version = container.getRegistry().lookup(
    			LookupNames.VERSION);
    	String v = "";
    	if (version != null && version instanceof String)
    		v = (String) version;
    	OMEROInfo omeroInfo = (OMEROInfo) container.getRegistry().lookup(
    				LookupNames.OMERODS);
        
    	String port = ""+omeroInfo.getPortSSL();
    	String f = container.resolveFilePath(null, Container.CONFIG_DIR);

		String n = (String) container.getRegistry().lookup(
				LookupNames.SPLASH_SCREEN_LOGIN);
		
		Icon splashLogin = Factory.createIcon(n, f);
		if (splashLogin == null)
			splashLogin = IconManager.getLoginBackground();
    	
    	
		ScreenLoginDialog dialog = new ScreenLoginDialog(Container.TITLE, 
				splashLogin, 
    			img, v, port);
		dialog.resetLoginText("Reconnect");
		
		dialog.showConnectionSpeed(true);
		dialog.addPropertyChangeListener(new PropertyChangeListener() {
			
			public void propertyChange(PropertyChangeEvent evt) {
				String name = evt.getPropertyName();
				if (ScreenLogin.QUIT_PROPERTY.equals(name))
					doExit(false);
				else if (ScreenLogin.LOGIN_PROPERTY.equals(name)) {
					LoginCredentials lc = (LoginCredentials) evt.getNewValue();
					if (lc != null) 
						collectCredentials(lc, 
								(ScreenLoginDialog) evt.getSource());
				}
			}
		});
		dialog.setModal(true);
		UIUtilities.centerAndShow(dialog);
		dialog.requestFocusOnField();
		if (success) {
			container.getRegistry().getEventBus().post(new ReconnectedEvent());
			success = false;
		}
	}
	
	/** Disconnects from the current server.*/
	private void logOut()
	{
		try {
			DataServicesFactory f = 
				DataServicesFactory.getInstance(container);
			f.exitApplication(false, false);
			reconnect();
		} catch (Exception e) {
			UserNotifier un = container.getRegistry().getUserNotifier();
			un.notifyInfo("Log out", "An error occurred while disconnecting" +
					" from the server.");
			LogMessage msg = new LogMessage();
			msg.print("Log out");
			msg.print(e);
			container.getRegistry().getLogger().debug(this, msg);
		}
	}
	
	/**
	 * The exit action.
	 * Just forwards to the container.
	 * 
	 * @param askQuestion 	Pass <code>true</code> to pop up a message before
	 * 						quitting, <code>false</code> otherwise.
	 */
	private void doExit(boolean askQuestion)
    {
        IconManager icons = IconManager.getInstance(container.getRegistry());
        int option = MessageBox.YES_OPTION; 
        Map<Agent, AgentSaveInfo> instances = getInstancesToSave();
        CheckoutBox msg = null;
		if (askQuestion) {
			 msg = new CheckoutBox(view, CLOSE_APP_TITLE, CLOSE_APP_TEXT, 
					 icons.getIcon(IconManager.QUESTION), instances);
			 option = msg.centerMsgBox();
		}
		if (option == MessageBox.YES_OPTION) {
			if (msg == null) {
				exitApplication();
			} else {
				Map<Agent, AgentSaveInfo> map = msg.getInstancesToSave();
				if (map == null || map.size() == 0) {
					exitApplication();
				} else {
					List<Object> nodes = new ArrayList<Object>();
					Iterator i = map.entrySet().iterator();
					Entry entry;
					Agent agent;
					AgentSaveInfo info;
					while (i.hasNext()) {
						entry = (Entry) i.next();
						agent = (Agent) entry.getKey();
						info = (AgentSaveInfo) entry.getValue();
						agent.save(info.getInstances());
						nodes.add(info);
					}
					exitApplication();
					//UserNotifierImpl un = (UserNotifierImpl) 
					//container.getRegistry().getUserNotifier();
					//un.notifySaving(nodes, this);
					/*
					Registry reg = container.getRegistry();
					UserNotifierLoader loader = new SwitchUserLoader(
							reg.getUserNotifier(), reg, null, -1);
					loader.load();
					*/
				}
			}
		}
    }

	/** Exits the application. */
	private void exitApplication()
	{
		try {
			DataServicesFactory f = 
				DataServicesFactory.getInstance(container);
			f.exitApplication(false, true);
		} catch (Exception e) {} //ignore
	}
	
	/**  Displays information about software. */
    private void softwareAbout()
    {
    	//READ content of the about file.
    	String aboutFile = (String) container.getRegistry().lookup(
    			LookupNames.ABOUT_FILE);
    	String refFile = container.resolveFilePath(aboutFile, 
    			Container.CONFIG_DIR);
    	String message = loadAbout(refFile);
    	String title = (String) container.getRegistry().lookup(
    			LookupNames.SOFTWARE_NAME);
        suDialog = new SoftwareUpdateDialog(view, message, refFile);
        suDialog.setTitle(TITLE_ABOUT+" "+title+"...");
        suDialog.addPropertyChangeListener(
        		SoftwareUpdateDialog.OPEN_URL_PROPERTY, this);
        UIUtilities.centerAndShow(suDialog);
    }
    
    /** Launches a browser with the documentation. */
    private void help()
    {
    	String path = (String) container.getRegistry().lookup(
    						LookupNames.HELP_ON_LINE);
    	openURL(path);
    }
    
    /** Launches a browser with the documentation. */
    private void forum()
    {
    	String path = (String) container.getRegistry().lookup(
    						LookupNames.FORUM);
    	openURL(path);
    }
    
    /** Opens the directory where the log file is. */
    private void logFile()
    {
    	//To be reviewed
    	String logDirName = (String) container.getRegistry().lookup(
    			LookupNames.LOG_DIR);	
		String name = (String) container.getRegistry().lookup(
				LookupNames.USER_HOME_OMERO);
    	String path = name+File.separator+logDirName;
    	String url = path;
    	try
        {
            url = new File(path).toURI().toURL().toString();
            url = url.replaceAll("^file:/", "file:///");
            openURL(url);
        } catch (Exception e) {
        	container.getRegistry().getLogger().error(this, 
        			"Unable to open log directory.");
        }
    }

	/**
	 * Attaches the {@link #notAvailable() not-available} action to all buttons
	 * whose functionality hasn't been implemented yet.
	 */
	private void attachMIListeners()
	{
		ActionListener noOp = new ActionListener() {		
			public void actionPerformed(ActionEvent ae) { notAvailable(); }
		};
		view.getButton(TaskBarView.WELCOME_MI).addActionListener(noOp);
		//view.getButton(TaskBarView.HELP_MI).addActionListener(
		//		new ActionListener() {       
        //    public void actionPerformed(ActionEvent ae) { help(); }
        //});
		//view.getButton(TaskBarView.HELP_MI).addActionListener(noOp);
		view.getButton(TaskBarView.HELP_MI).addActionListener(
				new ActionListener() {       
            public void actionPerformed(ActionEvent ae) { help(); }
        });
		view.getButton(TaskBarView.HOWTO_MI).addActionListener(noOp);
		view.getButton(TaskBarView.UPDATES_MI).addActionListener(
                new ActionListener() {       
            public void actionPerformed(ActionEvent ae) { softwareAbout(); }
        });
		view.getButton(TaskBarView.ABOUT_MI).addActionListener(noOp);
		view.getButton(TaskBarView.HELP_BTN).addActionListener(
				new ActionListener() {       
            public void actionPerformed(ActionEvent ae) { help(); }
        });
		view.getButton(TaskBarView.COMMENT_MI).addActionListener(
                new ActionListener() {       
            public void actionPerformed(ActionEvent ae) { sendComment(); }
        });
		view.getButton(TaskBarView.FORUM_MI).addActionListener(
				new ActionListener() {       
            public void actionPerformed(ActionEvent ae) { forum(); }
        });
		view.getButton(TaskBarView.ACTIVITY_MI).addActionListener(
				new ActionListener() {       
            public void actionPerformed(ActionEvent ae) { 
            	((UserNotifierImpl) 
            		container.getRegistry().getUserNotifier()).showActivity();
            }
        });
		view.getButton(TaskBarView.LOG_FILE_MI).addActionListener(
				new ActionListener() {       
            public void actionPerformed(ActionEvent ae) { logFile(); }
        });
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
			public void windowClosing(WindowEvent we) { doExit(true); }
			public void windowOpened(WindowEvent we) { 
				synchConnectionButtons();
			}
		});
		/*
		ActionListener exit = new ActionListener() {		
			public void actionPerformed(ActionEvent ae) { doExit(); }
		};
		*/
		//view.getButton(TaskBarView.EXIT_MI).addActionListener(exit);
		//view.getButton(TaskBarView.EXIT_BTN).addActionListener(exit);
	}
	
	/**
	 * Registers the necessary listeners with the view and also registers with
	 * the event bus.
	 */
	private void attachListeners()
	{
		attachOpenExitListeners();
		attachConnectionListeners();
		attachMIListeners();
		EventBus bus = container.getRegistry().getEventBus();
		bus.register(this, ServiceActivationResponse.class);
        bus.register(this, ExitApplication.class);
        bus.register(this, SaveEventResponse.class);

        bus.register(this, SwitchUserGroup.class);
        bus.register(this, LogOff.class);
		if (UIUtilities.isMacOS()) {
			try {
				MacOSMenuHandler handler = new MacOSMenuHandler(view);
				handler.initialize();
				view.addPropertyChangeListener(this);
			} catch (Throwable e) {
				Logger logger = container.getRegistry().getLogger();
				LogMessage message = new LogMessage();
				message.print(e);
				logger.info(this, message);
			}
        }
     }

	/**
     * Collects the user credentials.
     * 
     * @param lc The value collected.
     * @param dialog the dialog to handle.
     */
    private void collectCredentials(LoginCredentials lc,
    		ScreenLoginDialog dialog)
    {
    	UserCredentials uc = new UserCredentials(lc.getUserName(), 
				lc.getPassword(), lc.getHostName(), lc.getSpeedLevel());
		uc.setPort(lc.getPort());
		uc.setEncrypted(lc.isEncrypted());
		uc.setGroup(lc.getGroup());
		LoginService svc = (LoginService) 
			container.getRegistry().lookup(LookupNames.LOGIN);
		switch (svc.login(uc)) {
			case LoginService.CONNECTED:
				//needed b/c need to retrieve user's details later.
	            container.getRegistry().bind(LookupNames.USER_CREDENTIALS, uc);
	            dialog.close();
	            success = true;
	            break;
			case LoginService.TIMEOUT:
				success = false;
				svc.notifyLoginTimeout();
				if (dialog != null) {
					dialog.cleanField(ScreenLogin.PASSWORD_FIELD);
					dialog.requestFocusOnField();
				}
				break;
			case LoginService.NOT_CONNECTED:
				success = false;
				svc.notifyLoginFailure();
				if (dialog != null) {
					dialog.cleanField(ScreenLogin.PASSWORD_FIELD);
					dialog.requestFocusOnField();
				}
		}
    }
    
	/**
	 * Creates this controller along with its view and registers the necessary
	 * listeners with the view.
	 *  
	 * @param c	Reference to the container.
	 */
	TaskBarManager(Container c) 
	{
		container = c;
		view = new TaskBarView(this, IconManager.getInstance(c.getRegistry()));
		attachListeners();												
	}
	
	/**
	 * Returns the view component.
	 * 
	 * @return	See above.
	 */
	TaskBar getView() { return view; }
	
	/** 
	 * Returns the name of the software.
	 * 
	 * @return See above.
	 */
	String getSoftwareName()
	{ 
		return (String) container.getRegistry().lookup(
				LookupNames.SOFTWARE_NAME);
	}
	
	/**
	 * Opens the URL. 
	 * 
	 * @param url The URL to open.
	 */
	void openURL(String url)
	{
		BrowserLauncher launcher = new BrowserLauncher(
				AbstractIconManager.getOMEImageIcon());
		launcher.openURL(url);
		if (suDialog != null) suDialog.close();
	}
	
	/**
	 * Notifies that the connection is lost or the server is out of service.
	 * @param index
	 */
	void sessionExpired(int index)
	{
		switch (index) {
			case DataServicesFactory.LOST_CONNECTION:
			case DataServicesFactory.SERVER_OUT_OF_SERVICE:
			try {
				DataServicesFactory factory = 
					DataServicesFactory.getInstance(container);
				factory.sessionExpiredExit(index, null);
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
	}
	
	/**
	 * Returns <code>true</code> if already connected,
     * <code>false</code> otherwise.
     * 
     * @return See above.
	 */
	boolean login()
	{
		try {
			DataServicesFactory factory = 
				DataServicesFactory.getInstance(container);
			if (factory.isConnected()) return true;
			if (login == null) {
				Image img = IconManager.getOMEImageIcon();
		    	Object version = container.getRegistry().lookup(
		    			LookupNames.VERSION);
		    	String v = "";
		    	if (version != null && version instanceof String)
		    		v = (String) version;
		    	OMEROInfo omeroInfo = 
		    		(OMEROInfo) container.getRegistry().lookup(
		    				LookupNames.OMERODS);
		        
		    	String port = ""+omeroInfo.getPortSSL();
		    	String f = container.resolveFilePath(null, Container.CONFIG_DIR);

				String n = (String) container.getRegistry().lookup(
						LookupNames.SPLASH_SCREEN_LOGIN);
				
				Icon splashLogin = Factory.createIcon(n, f);
				if (splashLogin == null)
					splashLogin = IconManager.getLoginBackground();
		    	
		    	
		    	login = new ScreenLoginDialog(Container.TITLE, splashLogin, 
		    			img, v, port);
		    	//login.setModal(true);
				login.showConnectionSpeed(true);
				login.addPropertyChangeListener(this);
	    	}
			
			UIUtilities.centerAndShow(login);
    		return success;
		} catch (Exception e) {
			// TODO: handle exception
		}
		return success;
	}
	
	/**
	 * Intercepts {@link ServiceActivationResponse} events in order to keep
	 * the connection-related buttons in synch with the actual state of the
	 * connection.
     * @see AgentEventListener#eventFired(AgentEvent)
	 */
	public void eventFired(AgentEvent e) 
	{
		if (e instanceof ServiceActivationResponse)	
			synchConnectionButtons();
		else if (e instanceof ExitApplication) 
        	doExit(((ExitApplication) e).isAskQuestion());
		else if (e instanceof SwitchUserGroup) 
			handleSwitchUserGroup((SwitchUserGroup) e);
        else if (e instanceof SaveEventResponse) 
        	handleSaveEventResponse((SaveEventResponse) e);
        else if (e instanceof LogOff)
        	handleLogOff((LogOff) e);
	}

	/**
	 * Reacts to property change fired by the <code>SoftwareUpdateDialog</code>.
	 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt)
	{
		String name = evt.getPropertyName();
		if (SoftwareUpdateDialog.OPEN_URL_PROPERTY.equals(name)) {
			String refFile = (String) evt.getNewValue();
			if (refFile != null) openURL(parse(refFile));
		} else if (MacOSMenuHandler.ABOUT_APPLICATION_PROPERTY.equals(name)) {
			softwareAbout();
		} else if (MacOSMenuHandler.QUIT_APPLICATION_PROPERTY.equals(name)) {
			Registry reg = container.getRegistry();;
			Object exp = reg.lookup(LookupNames.CURRENT_USER_DETAILS);
			if (exp == null) container.exit(); //not connected
			else doExit(true);
		} else if (ScreenLogin.LOGIN_PROPERTY.equals(name)) {
			LoginCredentials lc = (LoginCredentials) evt.getNewValue();
			if (lc != null) collectCredentials(lc, login);
		} else if (ScreenLogin.QUIT_PROPERTY.equals(name)) {
			login.close();
			success = false;
		} else if (ChangesDialog.DONE_PROPERTY.equals(name)) {
			Boolean value = (Boolean) evt.getNewValue();
			if (value.booleanValue())
				exitApplication();
		}
	}

}
