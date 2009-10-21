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
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Map;
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
import org.openmicroscopy.shoola.env.config.OMEROInfo;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.DSOutOfServiceException;
import org.openmicroscopy.shoola.env.data.DataServicesFactory;
import org.openmicroscopy.shoola.env.data.events.ExitApplication;
import org.openmicroscopy.shoola.env.data.events.SaveEventResponse;
import org.openmicroscopy.shoola.env.data.events.ServiceActivationRequest;
import org.openmicroscopy.shoola.env.data.events.ServiceActivationResponse;
import org.openmicroscopy.shoola.env.data.login.LoginService;
import org.openmicroscopy.shoola.env.data.login.UserCredentials;
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
	
	/** The value of the tag to find. */
	private static final String		A_TAG = "a";
	

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
    
	private Map<Agent, Integer> 	exitResponses;
	
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
        	//UserNotifier un = container.getRegistry().getUserNotifier();
			//un.notifyInfo("Launch Browser", "Cannot launch the web browser.");
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
			FileInputStream fis = new FileInputStream(file);
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
	
	private void handleSaveEventResponse(SaveEventResponse e)
	{
		if (e == null) return;
		Agent a = e.getAgent();
		Integer r = exitResponses.get(a);
		if (r != null) {
			int v = r.intValue()-1;
			if (v == 0) exitResponses.remove(a);
			//else exitResponses.put(a, v);
		}
		if (exitResponses.size() == 0) container.exit();
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
		un.submitMessage("");
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
        /*
        JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		List agents = (List) container.getRegistry().lookup(LookupNames.AGENTS);
		Iterator i = agents.iterator();
		AgentInfo agentInfo;
		Agent a;
		//Agents termination phase.
		i = agents.iterator();
		Map m;
		List<SaveEventBox> boxes = null;
		SaveEventBox box;
		Iterator k, v;
		String key;
		JPanel item;
		Set values;
		Map<Agent, List> results = new HashMap<Agent, List>();
		while (i.hasNext()) {
			agentInfo = (AgentInfo) i.next();
			a = agentInfo.getAgent();
			m = a.hasDataToSave();
			if (m != null && m.size() > 0) {
				boxes = new ArrayList<SaveEventBox>();
				k = m.keySet().iterator();
				while (k.hasNext()) {
					key = (String) k.next();
					item = new JPanel();
					item.setLayout(new BoxLayout(item, BoxLayout.Y_AXIS));
					p.add(UIUtilities.setTextFont(key));
					p.add(item);
					values = (Set) m.get(key);
					v = values.iterator();
					while (v.hasNext()) {
						box = new SaveEventBox((RequestEvent) v.next());
						boxes.add(box);
						item.add(box);
					}
				}
				if (boxes != null && boxes.size() != 0) results.put(a, boxes);
			}
		}
		
		MessageBox msg;
		if (results.size() != 0) {
			EventBus bus = container.getRegistry().getEventBus();
			msg = new MessageBox(view, "Exit application", 
        			"Before closing the application, do you want to save" +
        			" data from : ", icons.getIcon(IconManager.QUESTION));
			msg.addCancelButton();
			msg.addBodyComponent(p);
			exitResponses = new HashMap<Agent, Integer>();
			switch (msg.centerMsgBox()) {
				case MessageBox.YES_OPTION:
					i = results.keySet().iterator();
					Integer number;
					while (i.hasNext()) {
						a = (Agent) i.next();
						boxes = results.get(a);
						k = boxes.iterator();
						while (k.hasNext()) {
							box = (SaveEventBox) k.next();
							if (box.isSelected()) {
								bus.post(box.getEvent());
								number = exitResponses.get(a);
								System.err.println("Agent: "+a);
								if (number == null) {
									exitResponses.put(a, new Integer(1));
								} else {
									number = new Integer(number.intValue()+1);
								}
							}
						}
					}
					//container.exit();
					break;
				case MessageBox.NO_OPTION:
					container.exit();
					break;
				case MessageBox.CANCEL:
					break;
			}
		} else {
			msg = new MessageBox(view, "Exit application", 
        			"Do you really want to close the application?", 
        			icons.getIcon(IconManager.QUESTION));
			if (msg.centerMsgBox() == MessageBox.YES_OPTION)
				container.exit();
		}
		*/
        int option = MessageBox.YES_OPTION; 
		if (askQuestion) {
			 MessageBox msg = new MessageBox(view, "Exit application", 
		    			"Do you really want to close the application?", 
		    			icons.getIcon(IconManager.QUESTION));
			 option = msg.centerMsgBox();
		}
       
        
		if (option == MessageBox.YES_OPTION) {
			try {
				DataServicesFactory f = 
					DataServicesFactory.getInstance(container);
				f.exitApplication();
			} catch (Exception e) {}
		}
    }

	/**  Displays information about software. */
    private void softwareAbout()
    {
    	//READ content of the about file.
    	String aboutFile = (String) container.getRegistry().lookup(
    			LookupNames.ABOUT_FILE);
    	String refFile = container.resolveConfigFile(aboutFile);
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
     */
    private void collectCredentials(LoginCredentials lc)
    {
    	UserCredentials uc = new UserCredentials(lc.getUserName(), 
				lc.getPassword(), lc.getHostName(), lc.getSpeedLevel());
		uc.setPort(lc.getPort());
		LoginService svc = (LoginService) 
			container.getRegistry().lookup(LookupNames.LOGIN);
		switch (svc.login(uc)) {
			case LoginService.CONNECTED:
				//needed b/c need to retrieve user's details later.
	            container.getRegistry().bind(LookupNames.USER_CREDENTIALS, uc);
	            login.close();
	            success = true;
	            break;
			case LoginService.TIMEOUT:
				success = false;
				svc.notifyLoginTimeout();
				if (login != null) {
					login.cleanField(ScreenLogin.PASSWORD_FIELD);
					login.requestFocusOnField();
				}
				break;
			case LoginService.NOT_CONNECTED:
				success = false;
				svc.notifyLoginFailure();
				if (login != null) {
					login.cleanField(ScreenLogin.PASSWORD_FIELD);
					login.requestFocusOnField();
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
				AbstractIconManager.getOMEIcon());
		launcher.openURL(url);
		if (suDialog != null) suDialog.close();
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
		        
		    	String port = ""+omeroInfo.getPort();
		    	String f = container.resolveConfigFile(null);

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
		if (e instanceof ServiceActivationResponse)	{
			synchConnectionButtons();
		} else if (e instanceof ExitApplication) 
        	doExit(((ExitApplication) e).isAskQuestion());
        else if (e instanceof SaveEventResponse) 
        	handleSaveEventResponse((SaveEventResponse) e);
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
			if (lc != null) {
				collectCredentials(lc);
			}
		} else if (ScreenLogin.QUIT_PROPERTY.equals(name)) {
			login.close();
			success = false;
		}
	}

}
