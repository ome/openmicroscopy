/*
 * ome.ij.data.ServicesFactory 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2009 University of Dundee. All rights reserved.
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
package ome.ij.data;



//Java imports
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.openmicroscopy.shoola.util.ui.login.LoginCredentials;

//Third-party libraries
import ij.IJ;
import Glacier2.PermissionDeniedException;
import Ice.ConnectionRefusedException;
import Ice.DNSException;

//Application-internal dependencies
import omero.SecurityViolation;
import pojos.ExperimenterData;

/** 
 *  A factory for the services.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class ServicesFactory 
{

	/** Bound property indicating to disconnect. */
	public static final String	DISCONNECT_PROPERTY = "disconnect";
	
	/** 
	 * Indicates that the client couldn't connect b/c a 
	 * <code>DNSException</code> was thrown by the server.
	 */
	public static final int		DNS_INDEX = 100;
	
	/** 
	 * Indicates that the client couldn't connect b/c a 
	 * <code>PermissionException</code> was thrown by the server.
	 */
	public static final int		PERMISSION_INDEX = 101;
	
	/** 
	 * Indicates that the client couldn't connect b/c a 
	 * <code>ConnectionException</code> was thrown by the server.
	 */
	public static final int		CONNECTION_INDEX = 102;
	
	/** 
	 * Indicates that the client couldn't connect b/c a 
	 * <code>ConnectionException</code> was thrown by the server.
	 */
	public static final int		SUCCESS_INDEX = 103;
	
	/** Indicates that the passed password if not valid. */
	public static final int		PASSWORD_FAILURE_INDEX = 104;
	
	/** Indicates that the passed name if not valid. */
	public static final int		NAME_FAILURE_INDEX = 105;
	
	/** 
	 * Indicates that the client couldn't connect b/c the user is no longer 
	 * active
	 */
	public static final int		ACTIVE_INDEX = 106;
	
	/** 
	 * Indicates that the client couldn't connect b/c a 
	 * <code>Ice.FileException</code> was thrown by the server.
	 */
	public static final int		CONFIGURATION_INDEX = 107;
	
	/** The sole instance. */
	private static ServicesFactory	singleton;

	/**
	 * Creates a new instance. 
	 * 
	 * @return See above.
	 * @throws DSOutOfServiceException 
	 */
	public static ServicesFactory getInstance()
	{
		if (singleton == null) singleton = new ServicesFactory();
		return singleton;
	}
	
	/** Unified access point to the various OMERO services. */
	private static Gateway	gateway;

	/** The data service adapter. */
	private DataService		dataService;
	
    /** Keeps the client's session alive. */
	private ScheduledThreadPoolExecutor	executor;
	
	/**
	 * Attempts to create a new instance.
     * 
	 * @param c	Reference to the container.
	 * @throws DSOutOfServiceException If the connection can't be established
	 * 									or the credentials are invalid.	
	 */
	private ServicesFactory()
	{
		//Check what to do if null.
		gateway = new Gateway(this);
		//Create the adapters.
		dataService = new DataServiceImpl(gateway);
	}
	
	/** 
	 * Returns the <code>DataService</code>.
	 * 
	 * @return See above.
	 */
	public DataService getDataService() { return dataService; }
	
	/**
	 * Returns <code>true</code> if already connected, <code>false</code>
	 * otherwise.
	 * 
	 * @return See above.
	 */
	public boolean isConnected() { return gateway.isConnected(); }

	/**
	 * Returns the user currently logged in.
	 * 
	 * @return See above.
	 */
	public ExperimenterData getUser() { return dataService.getCurrentUser(); }
	
	/**
	 * Attempts to connect.
	 * 
	 * @param lc The credentials
	 * @return See above.
	 * @throws DSOutOfServiceException If an error occured while connecting.
	 */
	public int login(LoginCredentials lc)
	{
		if (lc == null) return NAME_FAILURE_INDEX;
		String name = lc.getUserName();
		if (name == null || name.trim().length() == 0) 
			return NAME_FAILURE_INDEX;
		String password = lc.getPassword();
    	if (password == null || password.trim().length() == 0)
    		return PASSWORD_FAILURE_INDEX;
		try {
			gateway.login(lc.getUserName(), lc.getPassword(), lc.getHostName(), 
					lc.getPort(), lc.getGroup(), lc.isEncrypted());
			
			KeepClientAlive kca = new KeepClientAlive(gateway);
	        executor = new ScheduledThreadPoolExecutor(1);
	        executor.scheduleWithFixedDelay(kca, 60, 60, TimeUnit.SECONDS);
			return SUCCESS_INDEX;
		} catch (DSOutOfServiceException e) {
			if (e != null) {
				Throwable cause = e.getCause();
	        	if (cause instanceof ConnectionRefusedException) {
	        		return CONNECTION_INDEX;
	        	} else if (cause instanceof DNSException) {
	        		return DNS_INDEX;
	        	} else if (cause instanceof PermissionDeniedException) {
	        		return PERMISSION_INDEX;
	        	} else if (cause instanceof Ice.FileException) {
	        		return CONFIGURATION_INDEX;
	        	} else if (cause.getCause() instanceof SecurityViolation) {
            		return ACTIVE_INDEX;
            	}
			}
			return PERMISSION_INDEX;
		}
	}
	
	/** 
	 * Brings up a dialog indicating that the session has expired and
	 * quits the application.
	 * 
	 * @param index One of the connection constants defined by the gateway.
	 */
	void sessionExpiredExit(int index)
	{
		String message = "The server is no longer " +
			"running. \nPlease contact your system administrator.";
		if (index == Gateway.LOST_CONNECTION) {
			message = "The connection has been lost. \nThe application will " +
					"exit.";
		}
		IJ.showMessage("Connection Refused", message);
		//Need to notify the window.
		IJ.getInstance().firePropertyChange(DISCONNECT_PROPERTY, 0, 1);
		return;
	}
	
	/** Exits the plugin. */
	public void exitPlugin()
	{
		if (executor != null) executor.shutdown();
		executor = null;
		gateway.logout();
	}
	
}
