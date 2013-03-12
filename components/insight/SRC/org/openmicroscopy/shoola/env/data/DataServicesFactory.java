/*
 * org.openmicroscopy.shoola.env.data.DataServicesFactory
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

package org.openmicroscopy.shoola.env.data;

//Java imports
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.swing.JDialog;
import javax.swing.JFrame;

//Third-party libraries

//Application-internal dependencies
import omero.client;

import org.openmicroscopy.shoola.env.Agent;
import org.openmicroscopy.shoola.env.Container;
import org.openmicroscopy.shoola.env.Environment;
import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.cache.CacheServiceFactory;
import org.openmicroscopy.shoola.env.config.AgentInfo;
import org.openmicroscopy.shoola.env.config.OMEROInfo;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.events.ConnectedEvent;
import org.openmicroscopy.shoola.env.data.events.ReloadRenderingEngine;
import org.openmicroscopy.shoola.env.data.login.LoginService;
import org.openmicroscopy.shoola.env.data.login.UserCredentials;
import org.openmicroscopy.shoola.env.data.util.SecurityContext;
import org.openmicroscopy.shoola.env.data.views.DataViewsFactory;
import org.openmicroscopy.shoola.env.event.EventBus;
import org.openmicroscopy.shoola.env.log.LogMessage;
import org.openmicroscopy.shoola.env.rnd.PixelsServicesFactory;
import org.openmicroscopy.shoola.env.rnd.RenderingControl;
import org.openmicroscopy.shoola.env.ui.AbstractIconManager;
import org.openmicroscopy.shoola.env.ui.UserNotifier;
import org.openmicroscopy.shoola.svc.proxy.ProxyUtil;
import org.openmicroscopy.shoola.util.ui.IconManager;
import org.openmicroscopy.shoola.util.ui.MessageBox;
import org.openmicroscopy.shoola.util.ui.NotificationDialog;
import org.openmicroscopy.shoola.util.ui.ShutDownDialog;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.file.IOUtil;
import pojos.ExperimenterData;
import pojos.GroupData;

/** 
 * A factory for the {@link OmeroDataService} and the {@link OmeroImageService}.
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
public class DataServicesFactory
{

	
	/** The name of the fs configuration file in the configuration directory. */
	private static final String		FS_CONFIG_FILE = "fs.config";

    /** The sole instance. */
	private static DataServicesFactory		singleton;
	
	/** The dialog indicating that the connection is lost.*/
	private JDialog connectionDialog;
	
	/** Flag indicating that the client and server are not compatible.*/
	private boolean compatible;
	
	/**
	 * Creates a new instance. This can't be called outside of container 
	 * b/c agents have no references to the singleton container.
	 * So we can be sure this method is going to create services just once.
	 * 
	 * @param c Reference to the singleton container. Mustn't be 
	 * 			<code>null</code>.
	 * @return See above.
	 * @throws DSOutOfServiceException 
	 */
	public static DataServicesFactory getInstance(Container c)
		throws DSOutOfServiceException
	{
		if (c == null)
			throw new NullPointerException();  //An agent called this method?
		if (singleton == null)	
			singleton = new DataServicesFactory(c);
		return singleton;
	}
	
	/** 
	 * Reference to the container, to exit the application when the session
	 * has expired.
	 */
	private Container					container;

	/** A reference to the container's registry. */
	private static Registry         	registry;

	/** Unified access point to the various OMERO services. */
	private static OMEROGateway			omeroGateway;

	/** The OMERO service adapter. */
	private OmeroDataService			ds;

	/** The image service adapter. */
	private OmeroImageService			is;

	/** The metadata service adapter. */
	private OmeroMetadataService 		ms;
 
	/** The Administration service adapter. */
	private AdminService				admin;
	
    /** Keeps the client's session alive. */
	private ScheduledThreadPoolExecutor	executor;
	
    /** The fs properties. */
    private Properties 					fsConfig;

    /**
	 * Reads in the specified file as a property object.
	 * 
	 * @param file	Absolute pathname to the file.
	 * @return	The content of the file as a property object or
	 * 			<code>null</code> if an error occurred.
	 */
	private static Properties loadConfig(String file)
	{
		Properties config = new Properties();
		InputStream fis = null;
		try {
			fis = IOUtil.readConfigFile(file);
			config.load(fis);
		} catch (Exception e) {
			return null;
		} finally {
			try {
				if (fis != null) fis.close();
			} catch (Exception ex) {}
		}
		return config;
	}
    
	/**
	 * Attempts to create a new instance.
     * 
	 * @param c	Reference to the container.
	 * @throws DSOutOfServiceException If the connection can't be established
	 * 									or the credentials are invalid.	
	 */
	private DataServicesFactory(Container c)
		throws DSOutOfServiceException
	{
		registry = c.getRegistry();
		container = c;
		
        OMEROInfo omeroInfo = (OMEROInfo) registry.lookup(LookupNames.OMERODS);
        
		//Try and read the Ice config file.
		//Properties config = loadConfig(c.resolveConfigFile(ICE_CONFIG_FILE));
		
		//Check what to do if null.
        omeroGateway = new OMEROGateway(omeroInfo.getPortSSL(), this);
        
        //omeroGateway = new OMEROGateway(omeroInfo.getPort(), this);
		//Create the adapters.
        ds = new OmeroDataServiceImpl(omeroGateway, registry);
        is = new OmeroImageServiceImpl(omeroGateway, registry);
        ms = new OmeroMetadataServiceImpl(omeroGateway, registry);
        admin = new AdminServiceImpl(omeroGateway, registry);
        
        
        //fs stuff
        fsConfig = loadConfig(c.getConfigFileRelative(FS_CONFIG_FILE));
        //Initialize the Views Factory.
        DataViewsFactory.initialize(c);
        if (omeroGateway.isUpgradeRequired()) {
        	
        }
	}
	
	/**
	 * Determines the quality of the compression depending on the
	 * connection speed.
	 * 
	 * @param connectionSpeed The connection speed.
	 * @return See above.
	 */
	private float determineCompression(int connectionSpeed)
	{
		Float value;
		switch (connectionSpeed) {
			case UserCredentials.MEDIUM:
			case UserCredentials.HIGH:
				value = (Float) registry.lookup(
						LookupNames.COMPRESSIOM_MEDIUM_QUALITY);
				return value.floatValue();
			case UserCredentials.LOW:
			default:
				value = (Float) registry.lookup(
						LookupNames.COMPRESSIOM_LOW_QUALITY);
				return value.floatValue();
		}
	}
	
	/**
	 * Returns <code>true</code> if the connection is fast,
	 * <code>false</code> otherwise.
	 * 
	 * @param connectionSpeed The connection speed.
	 * @return See above.
	 */
	private int isFastConnection(int connectionSpeed)
	{
		switch (connectionSpeed) {
			case UserCredentials.HIGH:
				return RenderingControl.UNCOMPRESSED;
			case UserCredentials.MEDIUM:
				return RenderingControl.MEDIUM;
			case UserCredentials.LOW:
			default:
				return RenderingControl.LOW;
		}
	}
	
    /**
     * Returns <code>true</code> if the server and the client are compatible,
     * <code>false</code> otherwise. Return <code>null</code> if an error
     * occurred while comparing the versions and the user does not want to
     * connect.
     * 
     * @param server The version of the server.
     * @param client The version of the client.
     * @return See above.
     */
    private Boolean checkClientServerCompatibility(String server, String client)
    {
    	if (server == null || client == null) return false;
    	if (client.startsWith("@")) return true;
    	if (server.contains("-"))
    		server = server.split("-")[0];
    	if (client.contains("-"))
    		client = client.split("-")[0];
    	String[] values = server.split("\\.");
    	String[] valuesClient = client.split("\\.");
    	if (values.length < 2 || valuesClient.length < 2) return false;
    	try {
    		int s1 = Integer.parseInt(values[0]);
        	int s2 = Integer.parseInt(values[1]);
        	int c1 = Integer.parseInt(valuesClient[0]);
        	int c2 = Integer.parseInt(valuesClient[1]);
        	if (s1 < c1) return false;
        	if (s2 < c2) return false;
		} catch (Exception e) {
			//Record error
			LogMessage msg = new LogMessage();
			msg.print("Client server compatibility");
			msg.print(e);
			registry.getLogger().debug(this, msg);
			//Notify user that it is not possible to parse
			String message = "An error occurred while checking " +
					"the compatibility between client and server." +
					"\nDo you " +
					"still want to connect (further errors might occur)?";
			JFrame f = new JFrame();
			f.setIconImage(IconManager.getOMEImageIcon());
			MessageBox box = new MessageBox(f, "Version Check", message);
			box.setAlwaysOnTop(true);
			if (box.centerMsgBox() == MessageBox.YES_OPTION) {
				return true;
			}
			return null;
		}
    	
    	return true;
    }
    
    /** 
     * Notifies the user that the client and the server are not compatible.
     * 
     * @param clientVersion The version of the client.
     * @param serverVersion The version of the server.
     * @param hostname The name of the server.
     */
    private void notifyIncompatibility(String clientVersion,
    		String serverVersion, String hostname)
    {
    	UserNotifier un = registry.getUserNotifier();
    	StringBuffer buffer = new StringBuffer();
    	buffer.append("The client version ("+clientVersion+") is not " +
    			"compatible with the server:\n"+hostname);
    	if (serverVersion != null) {
    		buffer.append(" version:"+serverVersion);
    	}
    	buffer.append(".");
    	un.notifyInfo("Client Server not compatible", buffer.toString());
    }
    
    /**
     * Returns the credentials.
     * 
     * @return See above.
     */
    UserCredentials getCredentials()
    {
    	return (UserCredentials) 
    		registry.lookup(LookupNames.USER_CREDENTIALS);
    }
    
    /**
     * Brings up a notification dialog.
     * 
     * @param title     The dialog title.
     * @param message   The dialog message.
     */
    private void showNotificationDialog(String title, String message)
    {
    	showNotificationDialog(title, message, false);
    }
    /**
     * Brings up a notification dialog.
     * 
     * @param title     The dialog title.
     * @param message   The dialog message.
     * @param shutdown Pass <code>true</code> to shut down the application 
     * <code>false otherwise</code>
     */
    private void showNotificationDialog(String title, String message, boolean
    		shutdown)
    {
    	JFrame f = new JFrame();
    	f.setIconImage(AbstractIconManager.getOMEImageIcon());
    	
    	if (shutdown) {
    		connectionDialog = new ShutDownDialog(f, title, message);
    	} else
    		connectionDialog = new NotificationDialog(f, title, message, null);
        //connectionDialog.setModal(false);
        connectionDialog.addPropertyChangeListener(new PropertyChangeListener()
        {

			public void propertyChange(PropertyChangeEvent evt) {
				String name = evt.getPropertyName();
				if (NotificationDialog.CLOSE_NOTIFICATION_PROPERTY.equals(name))
				{
					connectionDialog = null;
					exitApplication(true, true);
				} else if (
					NotificationDialog.CANCEL_NOTIFICATION_PROPERTY.equals(
							name))
				{
					connectionDialog = null;
					omeroGateway.resetNetwork();
				}
			}
		});
        UIUtilities.centerAndShow(connectionDialog);
    }
    
	/** 
	 * Brings up a dialog indicating that the session has expired and
	 * quits the application.
	 * 
	 * @param index One of the connection constants defined by the gateway.
	 * @param exc The exception to register.
	 */
	public void sessionExpiredExit(int index, Throwable exc)
	{
		if (connectionDialog != null) return;
		String message;
		if (exc != null) {
			LogMessage msg = new LogMessage();
			msg.print("Connection Error");
			msg.print(exc);
			registry.getLogger().debug(this, msg);
		}
		switch (index) {
			case ConnectionExceptionHandler.DESTROYED_CONNECTION:
				message = "The connection has been destroyed." +
						"\nThe application will now exit.";
				showNotificationDialog("Connection Refused", message);
				break;
			case ConnectionExceptionHandler.NETWORK:
				message = "The network is down.\n";
				showNotificationDialog("Network", message, true);
				break;
			case ConnectionExceptionHandler.LOST_CONNECTION:
				UserCredentials uc = (UserCredentials) 
				registry.lookup(LookupNames.USER_CREDENTIALS);
				Map<SecurityContext, Set<Long>> l =
						omeroGateway.getRenderingEngines();
				boolean b =  omeroGateway.reconnect(uc.getUserName(), 
						uc.getPassword());
				connectionDialog = null;
				if (b) {
					//reactivate the rendering engine. Need to review that
					Iterator<Entry<SecurityContext, Set<Long>>> i =
							l.entrySet().iterator();
					OmeroImageService svc = registry.getImageService();
					Long id;
					Entry<SecurityContext, Set<Long>> entry;
					Map<SecurityContext, List<Long>> 
					failure = new HashMap<SecurityContext, List<Long>>();
					Iterator<Long> j;
					SecurityContext ctx;
					List<Long> f;
					while (i.hasNext()) {
						entry = i.next();
						j = entry.getValue().iterator();
						ctx = entry.getKey();
						while (j.hasNext()) {
							id = j.next();
							try {
								svc.reloadRenderingService(ctx, id);
							} catch (Exception e) {
								f = failure.get(ctx);
								if (f == null) {
									f = new ArrayList<Long>();
									failure.put(ctx, f);
								}
								f.add(id);
							}
						}
					}
					message = "You are reconnected to the server.";
					//un.notifyInfo("Reconnection Success", message);
					if (failure.size() > 0) {
						//notify user.
						registry.getEventBus().post(
								new ReloadRenderingEngine(failure));
					}
				} else {
					message = "A failure occurred while attempting to " +
							"reconnect.\nThe application will now exit.";
					showNotificationDialog("Reconnection Failure", message);
				}
				//}
				break;
			case ConnectionExceptionHandler.SERVER_OUT_OF_SERVICE:
				message = "The server is no longer " +
				"running. \nPlease contact your system administrator." +
				"\nThe application will now exit.";
				showNotificationDialog("Connection Refused", message);
		}
	}
	
    /**
     * Returns the {@link OmeroDataService}.
     * 
     * @return See above.
     */
    public OmeroDataService getOS() { return ds; }
    
    /**
     * Returns the {@link OmeroImageService}.
     * 
     * @return See above.
     */
    public OmeroImageService getIS() { return is; }
    
    /**
     * Returns the {@link OmeroMetadataService}.
     * 
     * @return See above.
     */
    public OmeroMetadataService getMS() { return ms; }
    
    /**
     * Returns the {@link AdminService}.
     * 
     * @return See above.
     */
    public AdminService getAdmin() { return admin; }
    
    /**
     * Returns the {@link LoginService}. 
     * 
     * @return See above.
     */
    public LoginService getLoginService()
    {
        return (LoginService) registry.lookup(LookupNames.LOGIN);
    }
    
	/**
	 * Attempts to connect to <i>OMERO</i> server.
	 * 
     * @param uc The user's credentials for logging onto <i>OMERO</i> server.
	 * @throws DSOutOfServiceException If the connection can't be established
     *                                 or the credentials are invalid.
	 */
	public void connect(UserCredentials uc)
		throws DSOutOfServiceException
	{
		if (uc == null)
            throw new NullPointerException("No user credentials.");
		omeroGateway.setPort(uc.getPort());
		String name = (String) 
		 container.getRegistry().lookup(LookupNames.MASTER);
		if (name == null) name = LookupNames.MASTER_INSIGHT;
		client client = omeroGateway.createSession(uc.getUserName(), 
				uc.getPassword(), uc.getHostName(), uc.isEncrypted(), name);
		if (client == null) {
			omeroGateway.logout();
        	return;
		}
		//check client server version
		compatible = true;
        //Register into log file.
        Object v = container.getRegistry().lookup(LookupNames.VERSION);
    	String clientVersion = "";
    	if (v != null && v instanceof String)
    		clientVersion = (String) v;
    	
        //Check if client and server are compatible.
        String version = omeroGateway.getServerVersion();
        Boolean check = checkClientServerCompatibility(version, clientVersion);
        if (check == null) {
        	compatible = false;
        	omeroGateway.logout();
        	return;
        }
        if (!check.booleanValue()) {
        	compatible = false;
        	notifyIncompatibility(clientVersion, version, uc.getHostName());
        	omeroGateway.logout();
        	return;
        }
        
        ExperimenterData exp = omeroGateway.login(client, uc.getUserName(), 
        		uc.getHostName(), determineCompression(uc.getSpeedLevel()),
        		uc.getGroup());
        //Post an event to indicate that the user is connected.
        EventBus bus = container.getRegistry().getEventBus();
        bus.post(new ConnectedEvent());
        //Post an event to notify 
        compatible = true;
        //Register into log file.
        Map<String, String> info = ProxyUtil.collectOsInfoAndJavaVersion();
        LogMessage msg = new LogMessage();
        msg.println("Server version: "+version);
        msg.println("Client version: "+clientVersion);
        Entry<String, String> entry;
        Iterator<Entry<String, String>> k = info.entrySet().iterator();
        while (k.hasNext()) {
        	entry = k.next();
        	msg.println(entry.getKey()+": "+entry.getValue());
		}
        registry.getLogger().info(this, msg);
        
        KeepClientAlive kca = new KeepClientAlive(container, omeroGateway);
        executor = new ScheduledThreadPoolExecutor(1);
        executor.scheduleWithFixedDelay(kca, 60, 60, TimeUnit.SECONDS);
        
        //String ldap = omeroGateway.lookupLdapAuthExperimenter(exp.getId());
        //registry.bind(LookupNames.USER_AUTHENTICATION, ldap);
        registry.bind(LookupNames.CURRENT_USER_DETAILS, exp);
        registry.bind(LookupNames.CONNECTION_SPEED, 
        		isFastConnection(uc.getSpeedLevel()));
        
        Collection<GroupData> groups;
        Set<GroupData> available;
        List<ExperimenterData> exps = new ArrayList<ExperimenterData>();
        try {
        	SecurityContext ctx = new SecurityContext(
        			exp.getDefaultGroup().getId());
        	groups = omeroGateway.getAvailableGroups(ctx, exp);
        	//Check if the current experimenter is an administrator 
        	Iterator<GroupData> i = groups.iterator();
        	GroupData g;
        	available = new HashSet<GroupData>();
        	while (i.hasNext()) {
        		g = i.next();
        		if (!omeroGateway.isSystemGroup(g.asGroup())) {
        			available.add(g);
        		} else {
        			if (GroupData.SYSTEM.equals(g.getName())) {
        				available.add(g);
        				uc.setAdministrator(true);
        			}
        		}
        	}
        	registry.bind(LookupNames.USER_GROUP_DETAILS, available);
        	List<Long> ids = new ArrayList<Long>();
        	i = available.iterator();
        	Set set;
        	Iterator j;
        	ExperimenterData e;
        	while (i.hasNext()) {
        		g = (GroupData) i.next();
        		set = g.getExperimenters();
        		j = set.iterator();
        		while (j.hasNext()) {
        			e = (ExperimenterData) j.next();
        			if (!ids.contains(e.getId())) {
        				ids.add(e.getId());
        				exps.add(e);
        			}
        		}
        	}
        	registry.bind(LookupNames.USERS_DETAILS, exps);	
        	registry.bind(LookupNames.USER_ADMINISTRATOR, uc.isAdministrator());
		} catch (DSAccessException e) {
			throw new DSOutOfServiceException("Cannot retrieve groups", e);
		} 
        //Bind user details to all agents' registry.
        List agents = (List) registry.lookup(LookupNames.AGENTS);
		Iterator kk = agents.iterator();
		AgentInfo agentInfo;
		Registry reg;
		Boolean b = (Boolean) registry.lookup(LookupNames.BINARY_AVAILABLE);
		while (kk.hasNext()) {
			agentInfo = (AgentInfo) kk.next();
			if (agentInfo.isActive()) {
				reg = agentInfo.getRegistry();
				//reg.bind(LookupNames.USER_AUTHENTICATION, ldap);
				reg.bind(LookupNames.CURRENT_USER_DETAILS, exp);
				reg.bind(LookupNames.USER_GROUP_DETAILS, available);
				reg.bind(LookupNames.USERS_DETAILS, exps);
				reg.bind(LookupNames.USER_ADMINISTRATOR, uc.isAdministrator());
				reg.bind(LookupNames.CONNECTION_SPEED, 
						isFastConnection(uc.getSpeedLevel()));
				reg.bind(LookupNames.BINARY_AVAILABLE, b);
			}
		}
	}
	
	/**
	 * Tells whether the communication channel to <i>OMEDS</i> is currently
	 * connected.
	 * This means that we have established a connection and have successfully
	 * logged in.
	 * 
	 * @return	<code>true</code> if connected, <code>false</code> otherwise.
	 */
	public boolean isConnected() { return omeroGateway.isConnected(); }
	
	/**
	 * Returns <code>true</code> if the client and server are compatible, 
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	public boolean isCompatible() { return compatible; }
	
    /** 
     * Shuts down the connection.
     * 
     * @param ctx The security context.
     */
	public void shutdown(SecurityContext ctx)
    { 
		//Need to write the current group.
		if (!omeroGateway.isConnected()) return;
		omeroGateway.logout();
		DataServicesFactory.registry.getCacheService().clearAllCaches();
		PixelsServicesFactory.shutDownRenderingControls(container.getRegistry());
		 
        if (executor != null) executor.shutdown();
        executor = null;
    }
	
	/** Shuts the services down and exits the application.
	 * 
	 * @param forceQuit Pass <code>true</code> to force i.e. do not check if
	 * 					the application can terminate,
	 * 					<code>false</code> otherwise.
	 * @param exit		Pass <code>true</code> to quit, <code>false</code> to
	 * 					only shut down the services.
	 */
	public void exitApplication(boolean forceQuit, boolean exit)
	{
		if (!forceQuit) {
			List<AgentInfo> agents = (List<AgentInfo>)
				registry.lookup(LookupNames.AGENTS);
			Iterator<AgentInfo> i = agents.iterator();
			AgentInfo agentInfo;
			Agent a;
			//Agents termination phase.
			i = agents.iterator();
			List<AgentInfo> notTerminated = new ArrayList<AgentInfo>();
			while (i.hasNext()) {
				agentInfo = i.next();
				if (agentInfo.isActive()) {
					a = agentInfo.getAgent();
					if (a.canTerminate()) {
						a.terminate();
					} else notTerminated.add(agentInfo);
				}
			}
			if (notTerminated.size() > 0) {
				i = notTerminated.iterator();
				StringBuffer buffer = new StringBuffer();
				while (i.hasNext()) {
					agentInfo = i.next();
					buffer.append(agentInfo.getName());
					buffer.append("\n");
				}
				String message = "The following components " +
				"could not be closed safely:\n"+buffer.toString()+"\n" +
				"Please check.";
				String title = "Exit Application";
				Environment env = (Environment) registry.lookup(LookupNames.ENV);
				if (env != null && env.isRunAsPlugin())
					title = "Exit Plugin";
				MessageBox box = new MessageBox(
						DataServicesFactory.registry.getTaskBar().getFrame(),
						title, message,
						IconManager.getInstance().getIcon(
								IconManager.INFORMATION_MESSAGE_48));
				box.setNoText("OK");
				box.setYesText("Force Quit");
				box.setSize(400, 250);
				if (!env.isRunAsPlugin() && 
						box.centerMsgBox() == MessageBox.NO_OPTION)
					return;
			}
		}
		shutdown(null);
		if (exit) {
			CacheServiceFactory.shutdown(container);
			singleton = null;
			container.exit();
		}
	}

	/**
	 * Remove the security group.
	 * 
	 * @param ctx The security context to handle.
	 * @throws Throwable Thrown if the connector cannot be closed.
	 */
	public void removeGroup(SecurityContext ctx)
		throws Exception
	{
		omeroGateway.removeGroup(ctx);
	}

}
