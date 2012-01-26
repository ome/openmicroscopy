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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.Agent;
import org.openmicroscopy.shoola.env.Container;
import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.cache.CacheServiceFactory;
import org.openmicroscopy.shoola.env.config.AgentInfo;
import org.openmicroscopy.shoola.env.config.OMEROInfo;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.events.ReloadRenderingEngine;
import org.openmicroscopy.shoola.env.data.login.LoginService;
import org.openmicroscopy.shoola.env.data.login.UserCredentials;
import org.openmicroscopy.shoola.env.data.views.DataViewsFactory;
import org.openmicroscopy.shoola.env.log.LogMessage;
import org.openmicroscopy.shoola.env.rnd.RenderingControl;
import org.openmicroscopy.shoola.env.ui.UserNotifier;
import org.openmicroscopy.shoola.svc.proxy.ProxyUtil;
import org.openmicroscopy.shoola.util.ui.IconManager;
import org.openmicroscopy.shoola.util.ui.MessageBox;
import org.openmicroscopy.shoola.util.ui.login.ScreenLogin;
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
	
	/** Indicates that the connection has been lost. */
	public static final int LOST_CONNECTION = 0;
	
	/** Indicates that the server is out of service. */
	public static final int SERVER_OUT_OF_SERVICE = 1;
	
	/** The name of the fs configuration file in the configuration directory. */
	private static final String		FS_CONFIG_FILE = "fs.config";

    /** The sole instance. */
	private static DataServicesFactory		singleton;
	
	/** The dialog indicating that the connection is lost.*/
	private MessageBox connectionDialog;
	
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
        fsConfig = loadConfig(c.resolveFilePath(FS_CONFIG_FILE, 
        		Container.CONFIG_DIR));
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
     * <code>false</code> otherwise.
     * 
     * @param server The version of the server.
     * @param client The version of the client.
     * @return See above.
     */
    private boolean checkClientServerCompatibility(String server, String client)
    {
    	if (server == null || client == null) return false;
    	if (server.contains("-"))
    		server = server.split("-")[0];
    	if (client.contains("-"))
    		client = client.split("-")[0];
    	if (client.startsWith("Beta"))
    		client = client.substring(4);
    	String[] values = server.split("\\.");
    	String[] valuesClient = client.split("\\.");
    	//Integer.parseInt(values[0]);
    	if (values.length < 2 || valuesClient.length < 2) return false;
    	int s1 = Integer.parseInt(values[0]);
    	int s2 = Integer.parseInt(values[1]);
    	int c1 = Integer.parseInt(valuesClient[0]);
    	int c2 = Integer.parseInt(valuesClient[1]);
    	if (s1 < c1) return false;
    	if (s2 < c2) return false;
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
    	String message = "The client version ("+clientVersion+") is not " +
    			"compatible with the following server:"+hostname;
    	if (serverVersion != null) {
    		message += " version:"+serverVersion;
    	}
    	message += ".";//\nThe application will now exit. ";
    	un.notifyInfo("Client Server not compatible", message);
		//exitApplication();
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
		UserNotifier un = registry.getUserNotifier();
		if (exc != null) {
			LogMessage msg = new LogMessage();
			msg.print("Session Expired");
			msg.print(exc);
			registry.getLogger().debug(this, msg);
		}
		switch (index) {
			case LOST_CONNECTION:
				message = "The connection has been lost. \nDo you want " +
						"to reconnect? If no, the application will now exit.";
				connectionDialog = new MessageBox(
						registry.getTaskBar().getFrame(), "Lost Connection", 
						message);
				connectionDialog.setModal(true);
				int v = connectionDialog.centerMsgBox();
				if (v == MessageBox.NO_OPTION) {
					connectionDialog = null;
					exitApplication(true, true);
				} else if (v == MessageBox.YES_OPTION) {
					UserCredentials uc = (UserCredentials) 
					registry.lookup(LookupNames.USER_CREDENTIALS);
					List<Long> l = omeroGateway.getRenderingServices();
					boolean b =  omeroGateway.reconnect(uc.getUserName(), 
            				uc.getPassword());
					connectionDialog = null;
					if (b) {
						//reactivate the rendering engine.
						Iterator<Long> i = l.iterator();
						OmeroImageService svc = registry.getImageService();
						Long id;
						List<Long> failure = new ArrayList<Long>();
						while (i.hasNext()) {
							id = i.next();
							try {
								svc.reloadRenderingService(id);
							} catch (Exception e) {
								failure.add(id);
							}
						}
						message = "You are reconnected to the server.";
						un.notifyInfo("Reconnection Success", message);
						if (failure.size() > 0) {
							//notify user.
							registry.getEventBus().post(
									new ReloadRenderingEngine(failure));
						}
					} else {
						message = "A failure occurred while attempting to " +
								"reconnect.\nThe application will now exit.";
						un.notifyInfo("Reconnection Failure", message);
						exitApplication(true, true);
					}
				}
				break;
			case SERVER_OUT_OF_SERVICE:
				message = "The server is no longer " +
				"running. \nPlease contact your system administrator." +
				"\nThe application will now exit.";
				un.notifyInfo("Connection Refused", message);
				exitApplication(true, true);
				break;	
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
        ExperimenterData exp = omeroGateway.login(uc.getUserName(), 
                				uc.getPassword(), uc.getHostName(),
                                 determineCompression(uc.getSpeedLevel()),
                                uc.getGroup(), uc.isEncrypted());
        compatible = true;
        //Register into log file.
        Object v = container.getRegistry().lookup(LookupNames.VERSION);
    	String clientVersion = "";
    	if (v != null && v instanceof String)
    		clientVersion = (String) v;
    	
        //Check if client and server are compatible.
        String version = omeroGateway.getServerVersion();
        if (!checkClientServerCompatibility(version, clientVersion)) {
        	compatible = false;
        	notifyIncompatibility(clientVersion, version, uc.getHostName());
        	omeroGateway.logout();
        	return;
        }
        
        //Register into log file.
        Map<String, String> info = ProxyUtil.collectOsInfoAndJavaVersion();
        LogMessage msg = new LogMessage();
        msg.println("Server version: "+version);
        msg.println("Client version: "+clientVersion);
        Entry entry;
        Iterator k = info.entrySet().iterator();
        while (k.hasNext()) {
        	entry = (Entry) k.next();
        	msg.println((String) entry.getKey()+": "+(String) entry.getValue());
		}
        registry.getLogger().info(this, msg);
        
        KeepClientAlive kca = new KeepClientAlive(container, omeroGateway);
        executor = new ScheduledThreadPoolExecutor(1);
        executor.scheduleWithFixedDelay(kca, 60, 60, TimeUnit.SECONDS);
        
        String ldap = omeroGateway.lookupLdapAuthExperimenter(exp.getId());
        //replace Server string in fs config
        /*
        Iterator k = fsConfig.keySet().iterator();
        String value, key;
        String regex = LookupNames.FS_HOSTNAME;
        while (k.hasNext()) {
        	key = (String) k.next();
			value = fsConfig.getProperty(key);
			value = value.replaceAll(regex, uc.getHostName());
			fsConfig.setProperty(key, value);
		}
        omeroGateway.startFS(fsConfig);
        */ 
        registry.bind(LookupNames.USER_AUTHENTICATION, ldap);
        registry.bind(LookupNames.CURRENT_USER_DETAILS, exp);
        registry.bind(LookupNames.CONNECTION_SPEED, 
        		isFastConnection(uc.getSpeedLevel()));
        
        Set<GroupData> groups;
        Set<GroupData> available;
        List<ExperimenterData> exps = new ArrayList<ExperimenterData>();
        try {
        	groups = omeroGateway.getAvailableGroups(exp);
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
		Iterator i = agents.iterator();
		AgentInfo agentInfo;
		Registry reg;
		Boolean b = (Boolean) registry.lookup(LookupNames.BINARY_AVAILABLE);
		while (i.hasNext()) {
			agentInfo = (AgentInfo) i.next();
			if (agentInfo.isActive()) {
				reg = agentInfo.getRegistry();
				reg.bind(LookupNames.USER_AUTHENTICATION, ldap);
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
	
    /** Shuts down the connection. */
	public void shutdown()
    { 
		//Need to write the current group.
		Set groups = (Set) registry.lookup(LookupNames.USER_GROUP_DETAILS);
		if (groups != null && groups.size() > 0) {
			ExperimenterData exp = (ExperimenterData) 
			registry.lookup(LookupNames.CURRENT_USER_DETAILS);
			GroupData group = exp.getDefaultGroup();	
			Iterator i = groups.iterator();
			GroupData g;
			Map<Long, String> names = new LinkedHashMap<Long, String>();
			while (i.hasNext()) {
				g = (GroupData) i.next();
				if (g.getId() != group.getId()) {
					if (!omeroGateway.isSystemGroup(g.asGroup()))
						names.put(g.getId(), g.getName());
				}
			}
			if (!omeroGateway.isSystemGroup(group.asGroup()))
				names.put(group.getId(), group.getName());
			if (names.size() == 0) names = null;
			ScreenLogin.registerGroup(names);
		} else ScreenLogin.registerGroup(null);
		CacheServiceFactory.shutdown(container);
        ((OmeroImageServiceImpl) is).shutDown();
        omeroGateway.logout(); 
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

				MessageBox box = new MessageBox(
						singleton.registry.getTaskBar().getFrame(),
						"Exit Application", message,
						IconManager.getInstance().getIcon(
								IconManager.INFORMATION_MESSAGE_48));
				box.setNoText("OK");
				box.setYesText("Force Quit");
				box.setSize(400, 250);
				if (box.centerMsgBox() == MessageBox.NO_OPTION)
					return;
			}
		}
		shutdown();
		if (exit) container.exit();
	}

	/**
	 * Checks if the session is alive.
	 * 
	 * @param context 	The context to make sure that agents do not
	 * 					access the method.
	 */
	public static void isSessionAlive(Registry context)
	{
		if (context == registry) omeroGateway.isSessionAlive();
	}
	
}
