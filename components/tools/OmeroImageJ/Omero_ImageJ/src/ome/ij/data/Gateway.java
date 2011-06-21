/*
 * ome.ij.data.Gateway 
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
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import ome.conditions.ResourceError;
import ome.system.UpgradeCheck;
import omero.AuthenticationException;
import omero.SecurityViolation;
import omero.SessionException;
import omero.client;
import omero.api.ExporterPrx;
import omero.api.GatewayPrx;
import omero.api.IAdminPrx;
import omero.api.IContainerPrx;
import omero.api.IPixelsPrx;
import omero.api.RawPixelsStorePrx;
import omero.api.ServiceFactoryPrx;
import omero.api.ServiceInterfacePrx;
import omero.model.Dataset;
import omero.model.ExperimenterGroupI;
import omero.model.Image;
import omero.model.Pixels;
import omero.model.Project;
import omero.sys.Parameters;
import pojos.DatasetData;
import pojos.ExperimenterData;
import pojos.GroupData;
import pojos.ImageData;
import pojos.ProjectData;

/** 
 * Unified access point to the various <i>OMERO</i> services.
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
class Gateway
{

	/** Indicates that the connection has been lost. */
	static final int LOST_CONNECTION = 0;
	
	/** Indicates that the server is out of service.. */
	static final int SERVER_OUT_OF_SERVICE = 1;
	
	/** Identifies the client. */
	private static final String			AGENT = "OMERO.imagej";
	
	/** Maximum size of pixels read at once. */
	private static final int				INC = 262144;
	
	/** 
	 * Used whenever a broken link is detected to get the Login Service and
	 * try reestablishing a valid link to <i>OMERO</i>. 
	 */
	private ServicesFactory				factory;

	/** The container service. */
	private IContainerPrx				pojosService;
	
	/** The Admin service. */
	private IAdminPrx					adminService;
	
	/** The pixels service. */
	private IPixelsPrx					pixelsService;
	
	/** The gateway service. */
	private GatewayPrx					gService;
	
	/**
	 * The entry point provided by the connection library to access the various
	 * <i>OMERO</i> services.
	 */
	private ServiceFactoryPrx 			entryEncrypted;
	
	/**
	 * The entry point provided by the connection library to access the various
	 * <i>OMERO</i> services.
	 */
	private ServiceFactoryPrx 			entryUnencrypted;

	/** Collection of services to keep alive. */
	private List<ServiceInterfacePrx> 	services;
	
	/** 
	 * The Blitz client object, this is the entry point to the 
	 * OMERO Server using a secure connection. 
	 */
	private client 						secureClient;

	/** 
	 * The Blitz client object, this is the entry point to the 
	 * OMERO Server using non secure data transfer
	 */
	private client 						unsecureClient;
	
	/** Tells whether we're currently connected and logged into <i>OMERO</i>. */
	private boolean 					connected;

	/** The currently logged in user. */
	private ExperimenterData			currentUser;
	
	/**
	 * Utility method to print the error message
	 * 
	 * @param e The exception to handle.
	 * @return  See above.
	 */
	private String printErrorText(Throwable e) 
	{
		if (e == null) return "";
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		return sw.toString();
	}
	
	/**
	 * Helper method to handle exceptions thrown by the connection library.
	 * Methods in this class are required to fill in a meaningful context
	 * message.
	 * This method is not supposed to be used in this class' constructor or in
	 * the login/logout methods.
	 *  
	 * @param t     	The exception.
	 * @param message	The context message.    
	 * @throws DSOutOfServiceException  A connection problem.
	 * @throws DSAccessException    A server-side error.
	 */
	private void handleException(Throwable t, String message) 
		throws DSOutOfServiceException, DSAccessException
	{
		Throwable cause = t.getCause();
		if (cause instanceof SecurityViolation) {
			String s = "For security reasons, cannot access data. \n"; 
			throw new DSAccessException(s+message, t);
		} else if (cause instanceof SessionException) {
			String s = "Session is not valid. \n"; 
			throw new DSOutOfServiceException(s+message, t);
		} else if (cause instanceof AuthenticationException) {
			String s = "Cannot initialize the session. \n"; 
			throw new DSOutOfServiceException(s+message, t);
		} else if (cause instanceof ResourceError) {
			String s = "Fatal error. Please contact the administrator. \n"; 
			throw new DSOutOfServiceException(s+message, t);
		}
		throw new DSAccessException("Cannot access data. \n"+message, t);
	}
	
	/**
	 * Returns the {@link ExporterPrx} service.
	 *   
	 * @return See above.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occurred while trying to 
	 * retrieve data from OMERO service. 
	 */
	private ExporterPrx getExporterService()
		throws DSAccessException, DSOutOfServiceException
	{ 
		try {
			ExporterPrx store = null;
			//if (exporterService == null) {
				if (entryUnencrypted != null)
					store = entryUnencrypted.createExporter();
				else 
					store = entryEncrypted.createExporter();
				//services.add(exporterService);
			//}
			return store;//exporterService; 
		} catch (Throwable e) {
			handleException(e, "Cannot access Exporter service.");
		}
		return null;
	}
	
	/**
	 * Returns the {@link IContainerPrx} service.
	 * 
	 * @return See above.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occurred while trying to 
	 * retrieve data from OMERO service. 
	 */
	private IContainerPrx getPojosService()
		throws DSAccessException, DSOutOfServiceException
	{ 
		try {
			if (pojosService == null) {
				pojosService = entryEncrypted.getContainerService();
				services.add(pojosService);
			}
			return pojosService; 
		} catch (Throwable e) {
			handleException(e, "Cannot access Container service.");
		}
		return null;
	}
	
	/**
	 * Returns the {@link IAdminPrx} service.
	 * 
	 * @return See above.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occurred while trying to 
	 * retrieve data from OMERO service. 
	 */
	private IAdminPrx getAdminService()
		throws DSAccessException, DSOutOfServiceException
	{ 
		try {
			if (adminService == null) {
				adminService = entryEncrypted.getAdminService(); 
				services.add(adminService);
			}
			return adminService; 
		} catch (Throwable e) {
			handleException(e, "Cannot access Admin service.");
		}
		return null;
	}
	
	/**
	 * Returns the {@link IPixelsPrx} service.
	 * 
	 * @return See above.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occurred while trying to 
	 * retrieve data from OMERO service. 
	 */
	private IPixelsPrx getPixelsService()
		throws DSAccessException, DSOutOfServiceException
	{ 
		try {
			if (pixelsService == null) {
				pixelsService = entryEncrypted.getPixelsService(); 
				services.add(pixelsService);
			}
			return pixelsService;
		} catch (Throwable e) {
			handleException(e, "Cannot access Pixels service.");
		}
		return null;
	}
	
	/** Clears the data. */
	private void clear()
	{
		pojosService = null;
		adminService = null;
		pixelsService = null;
		services.clear();
	}
	
	/**
	 * Retrieves the details on the current user and maps the result calling
	 * {@link PojoMapper#asDataObjects(Map)}.
	 * 
	 * @param name  The user's name.
	 * @return The {@link ExperimenterData} of the current user.
	 * @throws DSOutOfServiceException If the connection is broken, or
	 * logged in.
	 * @see IPojosPrx#getUserDetails(Set, Map)
	 */
	private ExperimenterData getUserDetails(String name)
		throws DSOutOfServiceException
	{
		try {
			IAdminPrx service = getAdminService();
			if (currentUser == null)
				currentUser = (ExperimenterData) 
				PojoMapper.asDataObject(service.lookupExperimenter(name));
			return currentUser;
		} catch (Exception e) {
			throw new DSOutOfServiceException("Cannot retrieve user's data " +
					printErrorText(e), e);
		}
	}
	
	/**
	 * Converts the specified POJO into the corresponding model.
	 *  
	 * @param nodeType The POJO class.
	 * @return The corresponding class.
	 */
	private String convertPojos(Class nodeType)
	{
		if (ProjectData.class.equals(nodeType)) 
			return Project.class.getName();
		else if (DatasetData.class.equals(nodeType)) 
			return Dataset.class.getName();
		else if (ImageData.class.equals(nodeType)) 
			return Image.class.getName();
		throw new IllegalArgumentException("NodeType not supported");
	}
	
	/** Checks if the session is still alive. */
	private void isSessionAlive()
	{
		/*
		try {
			EventContext ctx = getAdminService().getEventContext();
		} catch (Exception e) {
			Throwable cause = e.getCause();
			int index = SERVER_OUT_OF_SERVICE;
			if (cause instanceof ConnectionLostException)
				index = LOST_CONNECTION;
			factory.sessionExpiredExit(index);
		}
		*/
	}
	
	/**
	 * Changes the default group of the currently logged in user.
	 * 
	 * @param exp The experimenter to handle
	 * @param groupID The id of the group.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in.
	 * @throws DSAccessException If an error occurred while trying to 
	 * retrieve data from OMERO service. 
	 */
	private void changeCurrentGroup(ExperimenterData exp, long groupID)
		throws DSOutOfServiceException, DSAccessException
	{
		List<GroupData> groups = exp.getGroups();
		Iterator<GroupData> i = groups.iterator();
		GroupData group = null;
		boolean in = false;
		while (i.hasNext()) {
			group = i.next();
			if (group.getId() == groupID) {
				in = true;
				break;
			}
		}
		String s = "Can't modify the current group.\n\n";
		if (!in) {
			throw new DSOutOfServiceException(s);  
		}
		try {
			
			getAdminService().setDefaultGroup(exp.asExperimenter(), 
					group.asGroup());
			entryEncrypted.setSecurityContext(
					new ExperimenterGroupI(groupID, false));
		} catch (Exception e) {
			handleException(e, s);
		}
	}
	
	/**
	 * Returns the {@link RawPixelsStorePrx} service.
	 * 
	 * @return See above.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occurred while trying to 
	 * retrieve data from OMERO service. 
	 */
	private RawPixelsStorePrx getPixelsStore()
		throws DSAccessException, DSOutOfServiceException
	{
		try {
			if (entryUnencrypted != null)
				return entryUnencrypted.createRawPixelsStore();
			return entryEncrypted.createRawPixelsStore();
		} catch (Throwable e) {
			handleException(e, "Cannot access RawPixelsStore service.");
		}
		return null;
	}
	
	/** 
	 * Creates a new instance. 
	 * 
	 * @param factory	A reference to the factory. Used whenever a broken 
	 * 					link is detected to get the Login Service and try 
	 *                  reestablishing a valid link to <i>OMERO</i>.
	 *                  Mustn't be <code>null</code>.
	 */
	Gateway(ServicesFactory factory)
	{

                ResourceBundle bundle = ResourceBundle.getBundle("omero");
                String version = bundle.getString("omero.version");
                String url = bundle.getString("omero.upgrades.url");
                UpgradeCheck check = new UpgradeCheck(url, version, "ij");
                check.run();

		services = new ArrayList<ServiceInterfacePrx>();
		this.factory = factory;
	}
	
	/**
	 * Returns the current user.
	 * 
	 * @return See above.
	 */
	ExperimenterData getUserDetails() { return currentUser; }
	
	/** Keeps the services alive. */
	void keepSessionAlive()
	{
		int n = services.size();
		ServiceInterfacePrx[] entries = new ServiceInterfacePrx[n];
		Iterator<ServiceInterfacePrx> i = services.iterator();
		int index = 0;
		while (i.hasNext()) {
			entries[index] = i.next();
			index++;
		}
		try {
			entryEncrypted.keepAllAlive(entries);
		} catch (Exception e) {
			// TODO: handle exception
		}
		try {
			if (entryUnencrypted != null)
				entryUnencrypted.keepAllAlive(entries);
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	
	/**
	 * Tries to connect to <i>OMERO</i> and log in by using the supplied
	 * credentials.
	 * 
	 * @param userName	The user name to be used for login.
	 * @param password	The password to be used for login.
	 * @param hostName	The name of the server.
	 * @param port		The port to use.
	 * @param groupID	The id of the group or <code>-1</code>.
	 * @param encrypted Pass <code>true</code> to encrypt data transfer,
     * 					<code>false</code> otherwise.
	 * @return The user's details.
	 * @throws DSOutOfServiceException If the connection can't be established
	 *                                  or the credentials are invalid.
	 * @see #getUserDetails(String)
	 */
	ExperimenterData login(String userName, String password, String hostName, 
			int port,  long groupID, boolean encrypted)
		throws DSOutOfServiceException
	{
		try {

			if (port > 0) secureClient = new client(hostName, port);
			else secureClient = new client(hostName);
			secureClient.setAgent(AGENT);
			entryEncrypted = secureClient.createSession(userName, password);
			if (!encrypted) {
				unsecureClient = secureClient.createClient(false);
				entryUnencrypted = unsecureClient.getSession();
			}
			//omeroClient.getProperties().setProperty("Ice.Override.Timeout", 
			///		""+5000);
			connected = true;
			ExperimenterData exp = getUserDetails(userName);
			if (groupID >= 0) {
				long defaultID = exp.getDefaultGroup().getId();
				if (defaultID == groupID) return exp;
				try {
					changeCurrentGroup(exp, groupID);
					exp = getUserDetails(userName);
				} catch (Exception e) {
					/*
					connected = false;
					String s = "Can't connect to OMERO. Group not valid.\n\n";
					throw new DSOutOfServiceException(s, e);
					*/
				}
			}
			
			return exp;
		} catch (Exception e) {
			connected = false;
			String s = "Can't connect to OMERO. OMERO info not valid.\n\n";
			s += printErrorText(e);
			throw new DSOutOfServiceException(s, e);  
		}
	}
	
	/** Logs out. */
	void logout()
	{
		connected = false;
		currentUser = null;
		try {
			clear();
			secureClient.closeSession();
			secureClient = null;
			entryEncrypted = null;
		} catch (Exception e) {
			//session already dead.
		} finally {
			secureClient = null;
			entryEncrypted = null;
		}
		try {
			if (unsecureClient != null) secureClient.closeSession();
			unsecureClient = null;
			entryUnencrypted = null;
		} catch (Exception e) {
			// TODO: handle exception
		} finally {
			unsecureClient = null;
			entryUnencrypted = null;
		}
	}
	
	/**
	 * Tells whether the communication channel to <i>OMERO</i> is currently
	 * connected.
	 * This means that we have established a connection and have successfully
	 * logged in.
	 * 
	 * @return  <code>true</code> if connected, <code>false</code> otherwise.
	 */
	boolean isConnected() { return connected; }
	
	/**
	 * Retrieves hierarchy trees rooted by a given node.
	 * i.e. the requested node as root and all of its descendants.
	 * The annotation for the current user is also linked to the object.
	 * Annotations are currently possible only for Image and Dataset.
	 * Wraps the call to the 
	 * {@link IPojos#loadContainerHierarchy(Class, List, Map)}
	 * and maps the result calling {@link PojoMapper#asDataObjects(Set)}.
	 * 
	 * @param rootType  The top-most type which will be searched for 
	 *                  Can be <code>Project</code>. 
	 *                  Mustn't be <code>null</code>.
	 * @param rootIDs   A set of the IDs of top-most containers. 
	 *                  Passed <code>null</code> to retrieve all container
	 *                  of the type specified by the rootNodetype parameter.
	 * @param options   The Options to retrieve the data.
	 * @return  A set of hierarchy trees.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occurred while trying to 
	 * retrieve data from OMERO service. 
	 * @see IPojos#loadContainerHierarchy(Class, List, Map)
	 */
	Set loadContainerHierarchy(Class rootType, List rootIDs, Parameters options)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		try {
			IContainerPrx service = getPojosService();
			return PojoMapper.asDataObjects(service.loadContainerHierarchy(
				convertPojos(rootType), rootIDs, options));
		} catch (Throwable t) {
			handleException(t, "Cannot load hierarchy for " + rootType+".");
		}
		return new HashSet();
	}
	
	/**
	 * Retrieves the dimensions in microns of the specified pixels set.
	 * 
	 * @param pixelsID  The pixels set ID.
	 * @return See above.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occurred while trying to 
	 * retrieve data from OMERO service. 
	 */
	Pixels getPixels(long pixelsID)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		try {
			IPixelsPrx service = getPixelsService();
			return service.retrievePixDescription(pixelsID);
		} catch (Throwable t) {
			handleException(t, "Cannot retrieve the pixels set: "+pixelsID);
		}
		return null;
	}
	
	/**
	 * Loads the plane.
	 * 
	 * @param pixelsID The pixels set id.
	 * @param z The selected z-section.
     * @param c The selected channel.
     * @param t The selected time-point.
	 * @return See above.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occurred while trying to 
	 * retrieve data from OMERO service. 
	 */
	byte[] getPlane(long pixelsID, int z, int c, int t)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		try {
			RawPixelsStorePrx service = getPixelsStore();
			if (service == null) service = getPixelsStore();
			service.setPixelsId(pixelsID, false);
			byte[] plane = service.getPlane(z, c, t);
			service.close();
			return plane;
		} catch (Throwable e) {
			handleException(e, "Cannot load plane: ("+z+", "+c+", "+t+")");
		}
		return null;
	}
	
	/**
	 * Returns the file 
	 * 
	 * @param file		The file to write the bytes.
	 * @param imageID	The id of the image.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	File exportImageAsOMETiff(File f, long imageID)
		throws DSAccessException, DSOutOfServiceException
	{
		isSessionAlive();
		FileOutputStream stream = null;
		ExporterPrx store = null;
		try {
			stream = new FileOutputStream(f);
			try {
				synchronized(new Object()) {
					store = getExporterService();
					store.addImage(imageID);
					long size = store.generateTiff();
					int offset = 0;
					int length = (int) size;
					try {
						try {
							for (offset = 0; (offset+INC) < size;) {
								stream.write(store.read(offset, INC));
								offset += INC;
							}	
						} finally {
							stream.write(store.read(offset, length-offset)); 
							stream.close();
						}
					} catch (Exception e) {
						if (stream != null) stream.close();
						if (f != null) f.delete();
					}
				}
				
			} finally {
				try {
					if (store != null) store.close();
				} catch (Exception e) {}
				return f;
			}
		} catch (Throwable t) {
			if (f != null) f.delete();
			try {
				if (store != null) store.close();
			} catch (Exception e) {}
			handleException(t, "Cannot export the image as an OME-TIFF");
			return null;
		}
	}
	
}
