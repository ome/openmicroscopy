/*
 * org.openmicroscopy.shoola.env.ui.TaskBarManager
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2014 University of Dundee. All rights reserved.
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



//Third-party libraries
import ij.IJ;
import ij.ImagePlus;


//Application-internal dependencies
import org.openmicroscopy.shoola.env.Agent;
import org.openmicroscopy.shoola.env.Container;
import org.openmicroscopy.shoola.env.Environment;
import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.config.AgentInfo;
import org.openmicroscopy.shoola.env.config.OMEROInfo;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.AdminService;
import org.openmicroscopy.shoola.env.data.DSOutOfServiceException;
import org.openmicroscopy.shoola.env.data.DataServicesFactory;
import org.openmicroscopy.shoola.env.data.events.ExitApplication;
import org.openmicroscopy.shoola.env.data.events.HeartbeatEvent;
import org.openmicroscopy.shoola.env.data.events.LogOff;
import org.openmicroscopy.shoola.env.data.events.ReconnectedEvent;
import org.openmicroscopy.shoola.env.data.events.RemoveGroupEvent;
import org.openmicroscopy.shoola.env.data.events.SaveEventResponse;
import org.openmicroscopy.shoola.env.data.events.ServiceActivationRequest;
import org.openmicroscopy.shoola.env.data.events.ServiceActivationResponse;
import org.openmicroscopy.shoola.env.data.events.SwitchUserGroup;
import org.openmicroscopy.shoola.env.data.events.ViewInPluginEvent;
import org.openmicroscopy.shoola.env.data.login.LoginService;
import org.openmicroscopy.shoola.env.data.login.UserCredentials;
import org.openmicroscopy.shoola.env.data.model.FileObject;
import org.openmicroscopy.shoola.env.data.util.AgentSaveInfo;
import org.openmicroscopy.shoola.env.data.util.SecurityContext;
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
	static final String TITLE_ABOUT = "About";
	
	/** Bound property indicating to display the activity dialog. */
	static final String ACTIVITIES_PROPERTY = "activities";

	/** The title displayed before closing the application. */
	private static final String CLOSE_APP_TITLE = "Exit Application";
		
	/** The text displayed before closing the application. */
	private static final String CLOSE_APP_TEXT =
		"Do you really want to close the application?";
		
	/** The title displayed before closing the application. */
	private static final String CLOSE_PLUGIN_TITLE = "Exit Plugin";
		
	/** The text displayed before closing the application. */
	private static final String CLOSE_PLUGIN_TEXT =
		"Do you really want to close the plugin?";
	
	/** The title displayed before logging out. */
	private static final String LOGOUT_TITLE = "Log out";
		
	/** The text displayed before logging out. */
	private static final String LOGOUT_TEXT = 
		"Do you really want to disconnect from the server?";
	
	/** The view this controller is managing. */
	private TaskBarView view;
	
	/** Reference to the container. */
	private Container container;

	/** The software update dialog. */
	private SoftwareUpdateDialog suDialog;
	
    /** Login dialog. */
    private ScreenLoginDialog login;
    
    /** Flag indicating if the connection was successful or not. */
    private boolean success;
    
    /** Dialog to reconnect to server.*/
    private ScreenLoginDialog reconnectDialog;
    
    /** The actions for the help menu */
    private Map<Integer, ActionListener> helpMenuActions;
    
    /**
     * Returns the icon for the splash screen if none set.
     * 
     * @param splashScreen The icon or <code>null</code>.
     * @return See above.
     */
    private Icon getSplashScreen(Icon splashScreen)
    {
    	if (splashScreen == null) {
    		Integer v = (Integer) container.getRegistry().lookup(
    				LookupNames.ENTRY_POINT);
    		if (v != null) {
    			switch (v.intValue()) {
    			case LookupNames.IMPORTER_ENTRY:
    				splashScreen = IconManager.getImporterSplashScreen();
    				break;
    			default:
    				splashScreen = IconManager.getSplashScreen();
    			}
    		}
		}
    	return splashScreen;
    }
	
	/**
	 * Reads the content of the specified file and returns it as a string.
	 * 
	 * @param file Absolute pathname to the file.
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
				f.shutdown(null);
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
	 * Views the image as an <code>ImageJ</code>.
	 * 
	 * @param id The image's id to view.
	 * @param ctx The security context.
	 */
	private void runAsImageJ(long id, SecurityContext ctx)
	{
		UserCredentials lc = (UserCredentials) container.getRegistry().lookup(
				LookupNames.USER_CREDENTIALS);
		StringBuffer buffer = new StringBuffer();
		try {
			buffer.append("location=[OMERO] open=[omero:server=");
			buffer.append(lc.getHostName());
			buffer.append("\nuser=");
			buffer.append(lc.getUserName());
			buffer.append("\nport=");
			buffer.append(lc.getPort());
			buffer.append("\npass=");
			buffer.append(lc.getPassword());
			buffer.append("\ngroupID=");
			buffer.append(ctx.getGroupID());
			buffer.append("\niid=");
			buffer.append(id);
			buffer.append("]");
			IJ.runPlugIn("loci.plugins.LociImporter", buffer.toString());
			ImagePlus img = IJ.getImage();
			img.setTitle(img.getTitle() + "--" + "OMERO ID:" + id);
			img.setProperty(FileObject.OMERO_ID, id);
			img.setProperty(FileObject.OMERO_GROUP, ctx.getGroupID());
		} catch (Exception e) {
			LogMessage message = new LogMessage();
			message.println("Opening in image J");
			message.print(e);
			container.getRegistry().getLogger().debug(this, message);
			IJ.showMessage("An error occurred while loading the image.");
		}
	}
	
	/**
	 * Handles the event.
	 * 
	 * @param evt The event to handle.
	 */
	private void handleViewInPluginEvent(ViewInPluginEvent evt)
	{
		if (evt == null) return;
		switch (evt.getPlugin()) {
			case LookupNames.IMAGE_J:
			case LookupNames.IMAGE_J_IMPORT:
				runAsImageJ(evt.getObjectID(), evt.getSecurityContext());
				break;
		}
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
		/*
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
		*/
	}
	
	/**
	 * Logs off from the current server.
	 * 
	 * @param evt The event to handle.
	 */
	private void handleLogOff(LogOff evt)
	{
		if (evt == null) return;
		SecurityContext ctx = evt.getSecurityContext();
		if (!evt.isAskQuestion()) {
			logOut(ctx);
			return;
		}
		IconManager icons = IconManager.getInstance(container.getRegistry());
		Map<Agent, AgentSaveInfo> instances = getInstancesToSave();
		CheckoutBox msg = new CheckoutBox(view, LOGOUT_TITLE, 
				LOGOUT_TEXT, icons.getIcon(IconManager.QUESTION), instances);
		if (msg.centerMsgBox() == MessageBox.YES_OPTION) {
			Map<Agent, AgentSaveInfo> map = msg.getInstancesToSave();
			if (map != null && map.size() > 0) {
				List<Object> nodes = new ArrayList<Object>();
				Iterator<Entry<Agent, AgentSaveInfo>>
				i = map.entrySet().iterator();
				Entry<Agent, AgentSaveInfo> entry;
				Agent agent;
				AgentSaveInfo info;
				while (i.hasNext()) {
					entry = i.next();
					agent = entry.getKey();
					info = entry.getValue();
					agent.save(info.getInstances());
					nodes.add(info);
				}
			}
			logOut(ctx);
		}
	}
	
	/**
	 * Removes the group.
	 * 
	 * @param evt The event to handle.
	 */
	private void handleRemoveGroupEvent(RemoveGroupEvent evt)
	{
		if (evt == null) return;
		SecurityContext ctx = evt.getContext();
		try {
			DataServicesFactory f =
				DataServicesFactory.getInstance(container);
			f.removeGroup(ctx);
		} catch (Exception e) {
			LogMessage msg = new LogMessage();
			msg.print("Remove group");
			msg.print(e);
			container.getRegistry().getLogger().error(this, msg);
		}
	}
	
	/**
	 * Handles the event sent at regular interval to check if rendering 
	 * engines are still active.
	 * 
	 * @param evt The event to handle.
	 */
	private void handleHeartbeatEvent(HeartbeatEvent evt)
	{
		if (evt == null) return;
		try {
			DataServicesFactory f = DataServicesFactory.getInstance(container);
			f.checkServicesStatus();
		} catch (Exception e) {
			LogMessage msg = new LogMessage();
			msg.print("checkServicesStatus");
			msg.print(e);
			container.getRegistry().getLogger().debug(this, msg);
		}
	}
	
	/** Reconnects to the server.*/
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
    	String f = container.getConfigFileRelative(Container.CONFIG_DIR);

    	String n = (String) container.getRegistry().lookup(
				LookupNames.SPLASH_SCREEN_LOGO);

		reconnectDialog = new ScreenLoginDialog(Container.TITLE,
				getSplashScreen(Factory.createIcon(n, f)), img, v, port);
		reconnectDialog.setStatusVisible(false);
		reconnectDialog.showConnectionSpeed(true);
		reconnectDialog.addPropertyChangeListener(new PropertyChangeListener() {
			
			public void propertyChange(PropertyChangeEvent evt) {
				String name = evt.getPropertyName();
				if (ScreenLogin.QUIT_PROPERTY.equals(name))
					exitApplication(null);
				else if (ScreenLogin.LOGIN_PROPERTY.equals(name)) {
					LoginCredentials lc = (LoginCredentials) evt.getNewValue();
					
					if (lc != null) {
						collectCredentials(lc,
								(ScreenLoginDialog) evt.getSource());
					}
				}
			}
		});
		reconnectDialog.setModal(true);
		UIUtilities.centerAndShow(reconnectDialog);
	}
	
	/**
	 * Disconnects from the current server.
	 * 
	 * @param ctx The security context.
	 */
	private void logOut(SecurityContext ctx)
	{
	    //Change group if context not null
	    if (ctx != null) {
	        try {
	            AdminService svc = container.getRegistry().getAdminService();
	            //To be turned on when 
	            //svc.changeExperimenterGroup(ctx, null, ctx.getGroupID());
	        } catch (Exception e) {
	            if (isRunAsIJPlugin()) IJ.log(e.getMessage());
	            Logger log = container.getRegistry().getLogger();
	            LogMessage msg = new LogMessage();
	            msg.print(e);
	            log.error(this, msg);
	        }
	    }
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
	 * @param askQuestion Pass <code>true</code> to pop up a message before
	 * 						quitting, <code>false</code> otherwise.
	 * @param ctx The security context so the default group can be set or
	 * <code>null</code>.
	 */
	private void doExit(boolean askQuestion, SecurityContext ctx)
    {
		if (reconnectDialog != null) {
			exitApplication(ctx);
			return;
		}
		Environment env = (Environment) 
			container.getRegistry().lookup(LookupNames.ENV);
		String title = CLOSE_APP_TITLE;
		String message = CLOSE_APP_TEXT;
		if (env != null && env.isRunAsPlugin()) {
			title = CLOSE_PLUGIN_TITLE;
			message = CLOSE_PLUGIN_TEXT;
		}
        IconManager icons = IconManager.getInstance(container.getRegistry());
        int option = MessageBox.YES_OPTION;
        Map<Agent, AgentSaveInfo> instances = getInstancesToSave();
        CheckoutBox msg = null;
        if (env.isRunAsPlugin()) askQuestion = false;
		if (askQuestion) {
			msg = new CheckoutBox(view, title, message,
					icons.getIcon(IconManager.QUESTION), instances);
			msg.setYesText("Quit");
			msg.setNoText("Do Not Quit");
			option = msg.centerMsgBox();
		}
		if (option == MessageBox.YES_OPTION) {
			if (msg == null) {
				exitApplication(ctx);
			} else {
				Map<Agent, AgentSaveInfo> map = msg.getInstancesToSave();
				if (map == null || map.size() == 0) {
					exitApplication(ctx);
				} else {
					List<Object> nodes = new ArrayList<Object>();
					Iterator<Entry<Agent, AgentSaveInfo>>
					i = map.entrySet().iterator();
					Entry<Agent, AgentSaveInfo> entry;
					Agent agent;
					AgentSaveInfo info;
					while (i.hasNext()) {
						entry = i.next();
						agent = entry.getKey();
						info = entry.getValue();
						agent.save(info.getInstances());
						nodes.add(info);
					}
					exitApplication(ctx);
				}
			}
		}
    }

	/**
	 * Returns <code>true</code> if the application is used as 
	 * plug-in e.g. ImageJ, KNIME, <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	private boolean isRunAsIJPlugin()
	{
		Environment env = (Environment)
		container.getRegistry().lookup(LookupNames.ENV);
    	if (env == null) return false;
    	return env.runAsPlugin() >= 0;
	}
	
	/** 
	 * Exits the application.
	 * 
	 * @param ctx The security context or <code>null</code>.
	 */
	private void exitApplication(SecurityContext ctx)
	{
		reconnectDialog = null;
		//Change group if context not null
		if (ctx != null) {
			try {
				AdminService svc = container.getRegistry().getAdminService();
				//to be reviewed when preferences are available.
				//svc.changeExperimenterGroup(ctx, null, ctx.getGroupID());
			} catch (Exception e) {
				if (isRunAsIJPlugin()) IJ.log(e.getMessage());
				Logger log = container.getRegistry().getLogger();
				LogMessage msg = new LogMessage();
				msg.print(e);
				log.error(this, msg);
			}
		}
		try {
			DataServicesFactory f = DataServicesFactory.getInstance(container);
			f.exitApplication(false, true);
		} catch (Exception e) {
			if (isRunAsIJPlugin()) IJ.log(e.getMessage());
			Logger log = container.getRegistry().getLogger();
			LogMessage msg = new LogMessage();
			msg.print("Error while exiting");
			msg.print(e);
			log.error(this, msg);
		}
	}
	
	/**  Displays information about software. */
    private void softwareAbout()
    {
    	//READ content of the about file.
    	String aboutFile = (String) container.getRegistry().lookup(
    			LookupNames.ABOUT_FILE);
    	String refFile = container.getConfigFileRelative(aboutFile);
    	String message = loadAbout(refFile);
    	String title = (String) container.getRegistry().lookup(
    			LookupNames.SOFTWARE_NAME);
        suDialog = new SoftwareUpdateDialog(view, message);
        suDialog.setTitle(TITLE_ABOUT+" "+title+"...");
        suDialog.setAlwaysOnTop(true);
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

    /** Instantiates the ActionListeners for the help menu */
    private void createHelpMenuActionListeners() {
    	
    	helpMenuActions = new HashMap<Integer, ActionListener>();
    	
    	ActionListener noOp = new ActionListener() {
			public void actionPerformed(ActionEvent ae) { 
				notAvailable(); 
				}
		};
		
    	helpMenuActions.put(TaskBarView.WELCOME_MI, noOp);
    	
    	helpMenuActions.put(TaskBarView.HELP_MI, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				 help();
			}
		});
    	
    	helpMenuActions.put(TaskBarView.HOWTO_MI, noOp);
    	
    	helpMenuActions.put(TaskBarView.UPDATES_MI, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				softwareAbout();
			}
		});
    	
    	helpMenuActions.put(TaskBarView.ABOUT_MI, noOp);
    	
    	helpMenuActions.put(TaskBarView.HELP_BTN, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				 help();
			}
		});
    	
    	helpMenuActions.put(TaskBarView.COMMENT_MI, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				sendComment();
			}
		});
    	
    	helpMenuActions.put(TaskBarView.FORUM_MI, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				forum();
			}
		});
    	
    	helpMenuActions.put(TaskBarView.ACTIVITY_MI, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				((UserNotifierImpl)
	            		container.getRegistry().getUserNotifier()).showActivity();
			}
		});
    	
    	helpMenuActions.put(TaskBarView.LOG_FILE_MI, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				logFile();
			}
		});
    }
    
    /**
     * Get the ActionListener for a specific help menu item
     * @param id The Id of the action
     * @return See above
     */
    public ActionListener getHelpMenuAction(int id) {
    	return helpMenuActions.get(id);
    }
    
	/**
	 * Attaches the {@link #notAvailable() not-available} action to all buttons
	 * whose functionality hasn't been implemented yet.
	 */
	private void attachMIListeners()
	{
		view.getButton(TaskBarView.WELCOME_MI).addActionListener(
				getHelpMenuAction(TaskBarView.WELCOME_MI));
		view.getButton(TaskBarView.HELP_MI).addActionListener(
				getHelpMenuAction(TaskBarView.HELP_MI));
		view.getButton(TaskBarView.HOWTO_MI).addActionListener(
				getHelpMenuAction(TaskBarView.HOWTO_MI));
		view.getButton(TaskBarView.UPDATES_MI).addActionListener(
				getHelpMenuAction(TaskBarView.UPDATES_MI));
		view.getButton(TaskBarView.ABOUT_MI).addActionListener(
				getHelpMenuAction(TaskBarView.ABOUT_MI));
		view.getButton(TaskBarView.HELP_BTN).addActionListener(
				getHelpMenuAction(TaskBarView.HELP_BTN));
		view.getButton(TaskBarView.COMMENT_MI).addActionListener(
				getHelpMenuAction(TaskBarView.COMMENT_MI));
		view.getButton(TaskBarView.FORUM_MI).addActionListener(
				getHelpMenuAction(TaskBarView.FORUM_MI));
		view.getButton(TaskBarView.ACTIVITY_MI).addActionListener(
				getHelpMenuAction(TaskBarView.ACTIVITY_MI));
		view.getButton(TaskBarView.LOG_FILE_MI).addActionListener(
				getHelpMenuAction(TaskBarView.LOG_FILE_MI));
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
			public void windowClosing(WindowEvent we) { doExit(true, null); }
			public void windowOpened(WindowEvent we) {
				synchConnectionButtons();
			}
		});
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
        bus.register(this, ViewInPluginEvent.class);
        bus.register(this, RemoveGroupEvent.class);
        bus.register(this, HeartbeatEvent.class);
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
		success = false;
		switch (svc.login(uc)) {
			case LoginService.CONNECTED:
				//needed b/c need to retrieve user's details later.
	            container.getRegistry().bind(LookupNames.USER_CREDENTIALS, uc);
	            dialog.close();
	            if (dialog == reconnectDialog) {
	            	reconnectDialog = null;
	            	container.getRegistry().getEventBus().post(
	            			new ReconnectedEvent());
	            }
	            success = true;
	            break;
			case LoginService.TIMEOUT:
				success = false;
				svc.notifyLoginTimeout();
				if (dialog != null) {
					dialog.cleanField(ScreenLogin.PASSWORD_FIELD);
					dialog.onLoginFailure();
				}
				break;
			case LoginService.NOT_CONNECTED:
				success = false;
				svc.notifyLoginFailure();
				if (dialog != null) {
					dialog.cleanField(ScreenLogin.PASSWORD_FIELD);
					dialog.onLoginFailure();
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
		// ActionListeneres for the help menu have to be created
		// prior to the TaskBarView:
		createHelpMenuActionListeners();
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
		try {
			DataServicesFactory factory =
				DataServicesFactory.getInstance(container);
			factory.sessionExpiredExit(index, null);
		} catch (Exception e) {}
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
		    	OMEROInfo info = 
		    		(OMEROInfo) container.getRegistry().lookup(
		    				LookupNames.OMERODS);
		        
		    	String port = ""+info.getPortSSL();
		    	String f = container.getConfigFileRelative(null);

				String n = (String) container.getRegistry().lookup(
						LookupNames.SPLASH_SCREEN_LOGO);

		    	login = new ScreenLoginDialog(Container.TITLE,
		    		getSplashScreen(Factory.createIcon(n, f)), img, v, port);
		    	login.setEncryptionConfiguration(info.isEncrypted(),
		    			info.isEncryptedConfigurable());
		    	login.setHostNameConfiguration(info.getHostName(),
		    			info.isHostNameConfigurable());
		    	login.setModal(true);
		    	login.setStatusVisible(false);
				login.showConnectionSpeed(true);
				login.addPropertyChangeListener(this);
	    	}
			
			UIUtilities.centerAndShow(login);
    		return success;
		} catch (Exception e) {
		    LogMessage msg = new LogMessage();
		    msg.print(e);
		    container.getRegistry().getLogger().debug(this, msg);
		}
		return success;
	}
	
	/**
	 * Returns the relative path to the <code>Libs</code> directory.
	 * 
	 * @param file The file to handle.
	 * @return See above.
	 */
	String getLibFileRelative(String file)
	{
		if (file == null) return "";
		return container.getFileRelative(Container.LIBS_DIR, file);
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
		else if (e instanceof ExitApplication) {
			ExitApplication a = (ExitApplication) e;
			doExit(a.isAskQuestion(), a.getContext());
		} else if (e instanceof SwitchUserGroup) 
			handleSwitchUserGroup((SwitchUserGroup) e);
        else if (e instanceof SaveEventResponse) 
        	handleSaveEventResponse((SaveEventResponse) e);
        else if (e instanceof LogOff)
        	handleLogOff((LogOff) e);
        else if (e instanceof ViewInPluginEvent)
        	handleViewInPluginEvent((ViewInPluginEvent) e);
        else if (e instanceof RemoveGroupEvent)
        	handleRemoveGroupEvent((RemoveGroupEvent) e);
        else if (e instanceof HeartbeatEvent)
        	handleHeartbeatEvent((HeartbeatEvent) e);
	}

	/**
	 * Reacts to property change fired by the <code>SoftwareUpdateDialog</code>.
	 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt)
	{
		String name = evt.getPropertyName();
		if (SoftwareUpdateDialog.OPEN_URL_PROPERTY.equals(name)) {
			String url = (String) evt.getNewValue();
			if (url != null) openURL(url);
		} else if (MacOSMenuHandler.ABOUT_APPLICATION_PROPERTY.equals(name)) {
			softwareAbout();
		} else if (MacOSMenuHandler.QUIT_APPLICATION_PROPERTY.equals(name)) {
			Registry reg = container.getRegistry();;
			Object exp = reg.lookup(LookupNames.CURRENT_USER_DETAILS);
			if (exp == null) container.exit(); //not connected
			//else doExit(true, null);
		} else if (ScreenLogin.LOGIN_PROPERTY.equals(name)) {
			LoginCredentials lc = (LoginCredentials) evt.getNewValue();
			if (lc != null) collectCredentials(lc, login);
		} else if (ScreenLogin.QUIT_PROPERTY.equals(name)) {
			login.close();
			success = false;
		} else if (ChangesDialog.DONE_PROPERTY.equals(name)) {
			SecurityContext value = (SecurityContext) evt.getNewValue();
			exitApplication(value);
		}
	}

}
