package blitzgateway.service;
/*
 * .BlitzGateway 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
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


//Java imports
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

//Third-party libraries
import Glacier2.CannotCreateSessionException;
import Glacier2.PermissionDeniedException;
import Ice.ObjectPrx;

//Application-internal dependencies
import org.openmicroscopy.shoola.env.data.DSAccessException;
import org.openmicroscopy.shoola.env.data.DSOutOfServiceException;
import blitzgateway.util.OMEROClass;

import omero.ApiUsageException;
import omero.RType;
import omero.ServerError;
import omero.ValidationException;
import omero.client;
import omero.api.IAdminPrx;
import omero.api.IPixelsPrx;
import omero.api.IPojosPrx;
import omero.api.IQueryPrx;
import omero.api.IRenderingSettingsPrx;
import omero.api.IRepositoryInfo;
import omero.api.IRepositoryInfoPrx;
import omero.api.ITypesPrx;
import omero.api.IUpdatePrx;
import omero.api.RawFileStore;
import omero.api.RawFileStorePrx;
import omero.api.RawPixelsStore;
import omero.api.RawPixelsStorePrx;
import omero.api.RenderingEnginePrx;
import omero.api.ServiceFactoryPrx;
import omero.api.ThumbnailStore;
import omero.api.ThumbnailStorePrx;
import omero.model.Experimenter;
import omero.model.ExperimenterGroup;
import omero.model.IObject;
import omero.model.Pixels;
import omero.model.PixelsI;
import omero.model.PixelsPrx;
import omero.model.StatsInfo;
import omero.model.StatsInfoPrx;
import omero.model.StatsInfoPrxHelper;

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
class BlitzGateway
{	
	/** Start of the exception message for stateless service access exception. */
	private static final String statelessServiceAccessException = "Unable to access ";
	
	/** Start of the exception message for the stateful service access exception. */
	private static final String statefulServiceAccessException = "Unable to create ";
	
	/** The Blitz client object, this is the entry point to the OMERO.Blitz Server. */
	client blitzClient;
	
	/** The proxy to the session object. */
	ServiceFactoryPrx session;
	
	/**
	 * The Thumbnail service.
	 */
	ThumbnailStorePrx thumbnailStore;
	
	/**
	 * The pixels store.
	 */
	RawPixelsStorePrx pixelsStore;
	
	
	/** Maximum size of pixels read at once. */
	private static final int		INC = 256000;
	
	/** 
	 * The maximum number of thumbnails retrieved before restarting the
	 * thumbnails service.
	 */
	private static final int		MAX_RETRIEVAL = 100;
	
	/**
	 * The number of thumbnails already retrieved. Resets to <code>0</code>
	 * when the value equals {@link #MAX_RETRIEVAL}.
	 */
	private int						thumbRetrieval;

	/** The compression level. */
	private float					compression;
	
	/** The user name. */
	private String userName;
	
	/** Session closed variable, determining if session closed. */
	private boolean sessionClosed = true;
	
	/**
	 * Instantiate the BlitzGateway object.
	 * @param iceConfig passing the iceConfig file location.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	BlitzGateway(String iceConfig) throws DSOutOfServiceException, DSAccessException
	{
		blitzClient = new omero.client(iceConfig);
		sessionClosed = true;
	}

	/**
	 * Create a session using the current client object.
	 * @param user Username.
	 * @param password Password of the user.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public void createSession(String user, String password) throws DSOutOfServiceException, DSAccessException
	{
		try
		{
			session = blitzClient.createSession(user, password);
			userName = user;
			sessionClosed = false;
		}
		catch(Exception e)
		{
			handleException(e, "Cannot create session");
		}
	}
	
	/** Close the connection to the blitz server. */
	public void close()
	{
		blitzClient.closeSession();
		blitzClient.close();
		sessionClosed = true;
	}
	
	/**
	 * Is the session closed?
	 * @return true if closed.
	 */
	public boolean isClosed()
	{
		return sessionClosed;
	}
	
	/**
	 * Returns the {@link IRenderingSettings} service.
	 * 
	 * @return See above.
	 * @throws DSOutOfServiceException 
	 * @throws DSAccessException 
	 */
	public IRenderingSettingsPrx getRenderingSettingsService()
		throws DSOutOfServiceException, DSAccessException 
	{
		String currentService = "IRenderSettings";
		try
		{
			return session.getRenderingSettingsService();
		}
		catch (ServerError e)
		{
			handleException(e, statelessServiceAccessException+currentService);
		}
		return null;
	}
	
	/**
	 * Returns the {@link IPixels} service.
	 * 
	 * @return See above.
	 * @throws DSOutOfServiceException 
	 * @throws DSAccessException 
	 */
	public IPixelsPrx getPixelsService()
	throws DSOutOfServiceException, DSAccessException 
	{
		String currentService = "IPixels";
		try
		{
			return session.getPixelsService();
		}
		catch (ServerError e)
		{
			handleException(e, statelessServiceAccessException+currentService);
		}
		return null;
	}

	
	/**
	 * Returns the {@link IRepositoryInfo} service.
	 * 
	 * @return See above.
	 * @throws DSOutOfServiceException 
	 * @throws DSAccessException 
	 */
	public IRepositoryInfoPrx getRepositoryService() 
			throws DSOutOfServiceException, DSAccessException 
	{
		String currentService = "IRepositoryInfoService";
		try
		{
			return session.getRepositoryInfoService();
		}
		catch (ServerError e)
		{
			handleException(e, statelessServiceAccessException+currentService);
		}
		return null;
	}

	/**
	 * Returns the {@link IPojos} service.
	 * 
	 * @return See above.
	 * @throws DSAccessException 
	 * @throws DSOutOfServiceException 
	 */
	public IPojosPrx getPojosService() 
			throws DSOutOfServiceException, DSAccessException 
	{ 
		String currentService = "IPojosService";
		try
		{
			return session.getPojosService();
		}
		catch (ServerError e)
		{
			handleException(e, statelessServiceAccessException + currentService);
		}
		return null;
	}

	/**
	 * Returns the {@link IQuery} service.
	 *  
	 * @return See above.
	 * @throws DSOutOfServiceException 
	 * @throws DSAccessException 
	 */
	public IQueryPrx getQueryService() 
		throws DSOutOfServiceException, DSAccessException 
	{ 
		String currentService = "IQueryService";
		try
		{
			return session.getQueryService();
		}
		catch (ServerError e)
		{
			handleException(e, statelessServiceAccessException + currentService);
		}
		return null;
	}

	/**
	 * Returns the {@link IUpdate} service.
	 *  
	 * @return See above.
	 * @throws DSOutOfServiceException 
	 * @throws DSAccessException 
	 */
	public IUpdatePrx getUpdateService() 
		throws DSOutOfServiceException, DSAccessException 
	{ 
		String currentService = "IUpdateService";
		try
		{
			return session.getUpdateService();
		}
		catch (ServerError e)
		{
			handleException(e, statelessServiceAccessException + currentService);
		}
		return null;
	}

	/**
	 * Returns the {@link IAdmin} service.
	 * 
	 * @return See above.
	 * @throws DSOutOfServiceException 
	 * @throws DSAccessException 
	 */
	public IAdminPrx getAdminService() 
		throws DSOutOfServiceException, DSAccessException 
	{ 
		String currentService = "IAdminService";
		try
		{
			return session.getAdminService();
		}
		catch (ServerError e)
		{
			handleException(e, statelessServiceAccessException + currentService);
		}
		return null;
	}

	/**
	 * Returns the {@link ThumbnailStore} service.
	 *  
	 * @return See above.
	 * @throws DSOutOfServiceException 
	 * @throws DSAccessException 
	 */
	public ThumbnailStorePrx getThumbService()
		throws DSOutOfServiceException, DSAccessException 
	{ 
		String currentService = "ThumbnailStore";
		if (thumbRetrieval == MAX_RETRIEVAL) 
		{
			thumbRetrieval = 0;
			//to be on the save side
			if (thumbnailStore != null) thumbnailStore.close();
			thumbnailStore = null;
		}
		if (thumbnailStore == null) 
		{
			try
			{
				thumbnailStore = session.createThumbnailStore();
			}
			catch (ServerError e)
			{
				handleException(e, statefulServiceAccessException + currentService);
			}
		}
			
		thumbRetrieval++;
		return thumbnailStore; 
	}

	/**
	 * Returns the {@link RawFileStore} service.
	 *  
	 * @return See above.
	 * @throws DSOutOfServiceException 
	 * @throws DSAccessException 
	 */
	public RawFileStorePrx getRawFileService()
		throws DSOutOfServiceException, DSAccessException 
	{
		String currentService = "RawFileStore";
		try
		{
			return session.createRawFileStore();
		}
		catch (ServerError e)
		{
			handleException(e, statefulServiceAccessException + currentService);
		}
		return null;
	}

	/**
	 * Returns the {@link RenderingEngine Rendering service}.
	 * 
	 * @return See above.
	 * @throws DSOutOfServiceException 
	 * @throws DSAccessException 
	 */
	public RenderingEnginePrx getRenderingService()
		throws DSOutOfServiceException, DSAccessException 
	{
		String currentService = "RenderingEngine";
		RenderingEnginePrx engine;
		try
		{
			engine=session.createRenderingEngine();
			engine.setCompressionLevel(compression);
			return engine;
		}
		catch (ServerError e)
		{
			handleException(e, statefulServiceAccessException + currentService);
		}
		return null;
	}
	
	/**
	 * Returns the {@link RawPixelsStore} service.
	 * 
	 * @return See above.
	 * @throws DSOutOfServiceException 
	 * @throws DSAccessException 
	 */
	public RawPixelsStorePrx getPixelsStore()
		throws DSOutOfServiceException, DSAccessException 
	{
		String currentService = "PixelsStore";
		try
		{
			if(pixelsStore==null)
				pixelsStore =  session.createRawPixelsStore();
			return pixelsStore;
		}
		catch (ServerError e)
		{
			handleException(e, statefulServiceAccessException + currentService);
		}
		return null;
	}
	
	/**
	 * Get the container for the class klass, and return the containers (project, dataset, etc. )
	 * that matches the ids in the list leaves. Populate the leaves of the container
	 * (dataset for project, images for dataset, etc.) depending on the options
	 * passed. 
	 * @param <T> Type. 
	 * @param klass See above.
	 * @param leaves see above.
	 * @param options see above.
	 * @return see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	@SuppressWarnings("unchecked")
	public <T extends omero.model.IObject>List<T> loadContainerHierarchy(OMEROClass klass, List<Long> leaves, 
		Map<String, RType> options) throws DSOutOfServiceException, DSAccessException
	{
		try {
			IPojosPrx service = getPojosService();
			return (List<T>) (service.loadContainerHierarchy
				(convertPojos(klass), leaves, options));
		} catch (Throwable t) {
			handleException(t, "In loadContainerHierarchy : Cannot find hierarchy for "+klass.toString()+".");
		}
		return new ArrayList<T>();
	}
	/**
	 * Retrieves the images contained in containers specified by the 
	 * node type.
	 * Wraps the call to the {@link IPojos#getImages(Class, Set, Map)}
	 * and maps the result calling {@link PojoMapper#asDataObjects(Set)}.
	 * 
	 * @param nodeType  The type of container. Can be either Project, Dataset,
	 *                  CategoryGroup, Category.
	 * @param nodeIDs   Set of containers' IDS.
	 * @param options   Options to retrieve the data.
	 * @return A <code>Set</code> of retrieved images.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occured while trying to 
	 * retrieve data from OMERO service. 
	 * @see IPojos#getImages(Class, Set, Map)
	 */
	List getContainerImages(String nodeType, List<Long> nodeIDs, Map<String, RType> options)
		throws DSOutOfServiceException, DSAccessException
	{
		try {
			IPojosPrx service = getPojosService();
			return  service.getImages(nodeType, nodeIDs, options);
		} catch (Throwable t) {
			handleException(t, "Cannot find images for "+nodeType+".");
		}
		return new ArrayList();
	}

	/**
	 * Find an object by a query.
	 * @param query the query
	 * @return the object, or null.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public IObject findByQuery(String query) 
		throws DSOutOfServiceException, DSAccessException
	{
		IQueryPrx queryService = getQueryService();
		try
		{
			return queryService.findByQuery(query, null);
		}
		catch (ServerError e)
		{
			handleException(e, "In Query findByQuery: error in query"+ query);
		}
		return null;
	}
			
	/**
	 * Find an object by a query.
	 * @param klass the query
	 * @param field the query
	 * @param value the query
	 * @return the object, or null.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public IObject findByString(String klass, String field, String value) 
		throws DSOutOfServiceException, DSAccessException
	{
		IQueryPrx queryService = getQueryService();
		try
		{
			return queryService.findByString(klass, field, value);
		}
		catch (ServerError e)
		{
			handleException(e, "In Query findByString: error in query with "
				+ "class : " +klass + " field : " + field + " value " + value);
		}
		return null;
	}
	
	/**
	 * Find all objects by a query.
	 * @param query the query
	 * @return the list object, or null.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public List<IObject> findAllByQuery(String query) 
		throws DSOutOfServiceException, DSAccessException
	{
		IQueryPrx queryService = getQueryService();
		try
		{
			return queryService.findAllByQuery(query, null);
		}
		catch (ServerError e)
		{
			handleException(e, "In Query findAllByQuery: error in query"+ query);
		}
		return null;
	}
	
	 /**
     * Retrieves a server side enumeration.
     * 
     * @param klass the enumeration's class from <code>ome.model.enum</code>
     * @param value the enumeration's string value.
     * @return enumeration object.
	 * @throws DSAccessException 
	 * @throws DSOutOfServiceException 
     */
    private IObject getEnumeration(Class<? extends IObject> klass, String value) 
    	throws DSOutOfServiceException, DSAccessException
    {
        if (klass == null)
            throw new NullPointerException("Expecting not-null klass.");
        if (value == null) return null;

        return findByString(klass.getName(), "value", value);
    }

	/**
	 * Set the pixels id for the rawPixelsStore. 
	 * @param id see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public void setPixelsId(long id) 
			throws DSOutOfServiceException, DSAccessException
	{
		try
		{
			getPixelsStore().setPixelsId(id);
		}
		catch (Exception e)
		{
			handleException(e, "Cannot setPixelsId in PixelsStore to : " + id);
		}
		
	}

	/**
	 * Get the raw plane from the pixels store. 
	 * @param z The z-section.
	 * @param c The channel.
	 * @param t The time point.
	 * @return see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public byte[] getPlane(int z, int c, int t) 
			throws DSOutOfServiceException, DSAccessException
	{
		try
		{
			return getPixelsStore().getPlane(z, c, t);
		}
		catch (Exception e)
		{
			handleException(e, "Cannot retrieve plane .");
		}
		return null;
	}
	
	
	/**
	 * Returns <code>true</code> if the passed group is an experimenter group
	 * internal to OMERO, <code>false</code> otherwise.
	 * 
	 * @param group The experimenter group to handle.
	 * @return See above.
	 */
	private boolean isSystemGroup(ExperimenterGroup group)
	{
		String n = group.name.val;
		return ("system".equals(n) || "user".equals(n) || "default".equals(n));
	}

	/**
	 * Reconnects to server. This method should be invoked when the password
	 * is reset.
	 * 
	 * @param userName	The name of the user who modifies his/her password.
	 * @param password 	The new password.
	 */
	private void resetFactory(String userName, String password)
		throws DSOutOfServiceException, DSAccessException
	{
		try
		{
			session = blitzClient.createSession(userName, password);
			if (thumbnailStore != null) thumbnailStore.close();
			thumbnailStore = null;
			thumbRetrieval = 0;
		}
		catch (CannotCreateSessionException e)
		{
			handleException(e, "Cannot create session");
		}
		catch (PermissionDeniedException e)
		{
			handleException(e, "Permission Denied");
		}
		
	}

	
	/** 
	 * Helper method for the conversion of base types in containers(normally 
	 * of type IObject) to a concrete type.  
	 * @param <T> new type.
	 * @param klass new type class.
	 * @param list container.
	 * @return see above.
	 */
	public static <T extends IObject> List<T> 
    collectionCast(Class<T> klass, List<IObject> list)
    {
        List<T> newList = new ArrayList<T>(list.size());
        for (IObject o : list)
        {
            newList.add((T) o);
        }
        return newList;
    }

	/** 
	 * Get the username.
	 * @return see above.
	 */
	public String getUserName()
	{
		return userName;
	}
	
	/**
	 * Converts the specified POJO into the corresponding model.
	 *  
	 * @param nodeType The POJO class.
	 * @return The corresponding class.
	 */
	private String convertPojos(OMEROClass nodeType)
	{
		return nodeType.toString();
	}

	
	/**
	 * Determines the table name corresponding to the specified class.
	 * 
	 * @param klass The class to analyze.
	 * @return See above.
	 */
	private String getTableForLink(OMEROClass klass)
	{
		String table = null;
		if (klass.equals(OMEROClass.Category)) table = "CategoryImageLink";
		else if (klass.equals(OMEROClass.Dataset)) table = "DatasetImageLink";
		else if (klass.equals(OMEROClass.Project)) table = "ProjectDatasetLink";
		else if (klass.equals(OMEROClass.CategoryGroup)) 
			table = "CategoryGroupCategoryLink";
		return table;
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
		if (cause instanceof PermissionDeniedException) {
			String s = "Cannot access data for security reasons \n"; 
			throw new DSAccessException(s+message, t);
		} else if (cause instanceof PermissionDeniedException) {
			String s = "Cannot access data for security reasons \n"; 
			throw new DSAccessException(s+message, t);
		} else if (cause instanceof ApiUsageException) {
			String s = "Cannot access data, specified parameters not valid \n"; 
			throw new DSAccessException(s+message, t);
		} else if (cause instanceof ValidationException) {
			String s = "Cannot access data, specified parameters not valid \n"; 
			throw new DSAccessException(s+message, t);
		} else 
			throw new DSOutOfServiceException(message, t);
	}
	
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
	 * Copy the pixels set from pixels to a new set.
	 * @param pixelsId pixels id to copy.
	 * @param x width of plane.
	 * @param y height of plane.
	 * @param t num timepoints
	 * @param z num zsections.
	 * @param methodology what created the pixels.
	 * @return new id.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public Long copyPixels(long pixelsId, int x, int y, int t, int z, List<Integer> channelList,
			String methodology) throws DSOutOfServiceException, DSAccessException
	{		
		try
		{
			IPixelsPrx service = getPixelsService(); 
			omero.RLong val = service.copyAndResizePixels(pixelsId, 
				new omero.RInt(x), new omero.RInt(y), new omero.RInt(z), new omero.RInt(t),
				channelList, methodology);
			return val.val;
		}
		catch (Exception e)
		{
			handleException(e, "Cannot copy pixels.");
		}
		return null;
	}

	/**
	 * upload bytes to the plane c, t, z
	 * @param pixelsId see above.
	 * @param c see above.
	 * @param t see above.
	 * @param z see above.
	 * @param b the bytes.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public void uploadBytes(long pixelsId, int c, int t, int z, byte[] b)
		throws DSOutOfServiceException, DSAccessException
	{
		try
		{
			RawPixelsStorePrx service = getPixelsStore();
			service.setPixelsId(pixelsId);
			service.setPlane(b, z, c, t); 
		}
		catch (Exception e)
		{
			handleException(e, "Cannot upload plane.");
		}
	}
	
	public Pixels updatePixels(Pixels object) 
		throws DSOutOfServiceException, DSAccessException
	{
		try
		{
			IUpdatePrx service = getUpdateService();
			return (Pixels) service.saveAndReturnObject(object);
		}
		catch (Exception e)
		{
			handleException(e, "Cannot update pixels.");
		}
		return null;
	}
	
	private ITypesPrx getTypesService() 
	throws DSOutOfServiceException, DSAccessException 
	{ 
		String currentService = "ITypeService";
		try
		{
			return session.getTypesService();
		}
		catch (ServerError e)
		{
			handleException(e, statelessServiceAccessException + currentService);
		}
		return null;
	}
	
	public List<IObject> getAllEnumerations(String klass) 
		throws DSOutOfServiceException, DSAccessException
	{
		ITypesPrx typesService = getTypesService();
		try
		{
			return typesService.allEnumerations(klass);
		}
		catch(ServerError e)
		{
			handleException(e, "Unable to retrieve Enumerations for : " + klass);
		}
		return null;
	}
}


