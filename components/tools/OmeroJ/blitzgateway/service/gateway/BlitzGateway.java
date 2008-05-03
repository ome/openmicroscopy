package blitzgateway.service.gateway;
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
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

//Third-party libraries
import Glacier2.CannotCreateSessionException;
import Glacier2.PermissionDeniedException;

//Application-internal dependencies
import org.openmicroscopy.shoola.env.data.DSAccessException;
import org.openmicroscopy.shoola.env.data.DSOutOfServiceException;
import blitzgateway.util.OMEROClass;
import blitzgateway.util.ServiceUtilities;

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
import omero.model.ExperimenterGroup;
import omero.model.IObject;
import omero.model.Pixels;

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
	 * Return the Session, this will allow us to protect the access to 
	 * server for multithreaded calls.
	 * @return see above.
	 */
	private ServiceFactoryPrx getSession()
	{
		return session;
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
			ServiceUtilities.handleException(e, "Cannot create session");
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
			return getSession().getRenderingSettingsService();
		}
		catch (ServerError e)
		{
			ServiceUtilities.handleException(e, statelessServiceAccessException+currentService);
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
			return getSession().getPixelsService();
		}
		catch (ServerError e)
		{
			ServiceUtilities.handleException(e, statelessServiceAccessException+currentService);
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
			return getSession().getRepositoryInfoService();
		}
		catch (ServerError e)
		{
			ServiceUtilities.handleException(e, statelessServiceAccessException+currentService);
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
			return getSession().getPojosService();
		}
		catch (ServerError e)
		{
			ServiceUtilities.handleException(e, statelessServiceAccessException + currentService);
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
			return getSession().getQueryService();
		}
		catch (ServerError e)
		{
			ServiceUtilities.handleException(e, statelessServiceAccessException + currentService);
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
			return getSession().getUpdateService();
		}
		catch (ServerError e)
		{
			ServiceUtilities.handleException(e, statelessServiceAccessException + currentService);
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
			return getSession().getAdminService();
		}
		catch (ServerError e)
		{
			ServiceUtilities.handleException(e, statelessServiceAccessException + currentService);
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
				thumbnailStore = getSession().createThumbnailStore();
			}
			catch (ServerError e)
			{
				ServiceUtilities.handleException(e, statefulServiceAccessException + currentService);
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
			return getSession().createRawFileStore();
		}
		catch (ServerError e)
		{
			ServiceUtilities.handleException(e, statefulServiceAccessException + currentService);
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
			engine = getSession().createRenderingEngine();
			engine.setCompressionLevel(compression);
			return engine;
		}
		catch (ServerError e)
		{
			ServiceUtilities.handleException(e, statefulServiceAccessException + currentService);
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
				pixelsStore =  getSession().createRawPixelsStore();
			return pixelsStore;
		}
		catch (ServerError e)
		{
			ServiceUtilities.handleException(e, statefulServiceAccessException + currentService);
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
			ServiceUtilities.handleException(e, "Cannot create session");
		}
		catch (PermissionDeniedException e)
		{
			ServiceUtilities.handleException(e, "Permission Denied");
		}
		
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
			ServiceUtilities.handleException(e, "Cannot update pixels.");
		}
		return null;
	}
	
	public ITypesPrx getTypesService() 
	throws DSOutOfServiceException, DSAccessException 
	{ 
		String currentService = "ITypeService";
		try
		{
			return getSession().getTypesService();
		}
		catch (ServerError e)
		{
			ServiceUtilities.handleException(e, statelessServiceAccessException + currentService);
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
			ServiceUtilities.handleException(e, "Unable to retrieve Enumerations for : " + klass);
		}
		return null;
	}
}


