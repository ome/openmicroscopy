package ome.services.blitz.gateway;
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

//Third-party libraries

//Application-internal dependencies
import java.util.HashMap;
import java.util.Iterator;

import omero.ServerError;
import omero.client;
import omero.api.IAdmin;
import omero.api.IAdminPrx;
import omero.api.IPixels;
import omero.api.IPixelsPrx;
import omero.api.IPojos;
import omero.api.IPojosPrx;
import omero.api.IQuery;
import omero.api.IQueryPrx;
import omero.api.IRenderingSettings;
import omero.api.IRenderingSettingsPrx;
import omero.api.IRepositoryInfo;
import omero.api.IRepositoryInfoPrx;
import omero.api.IScriptPrx;
import omero.api.ITypesPrx;
import omero.api.IUpdate;
import omero.api.IUpdatePrx;
import omero.api.RawFileStore;
import omero.api.RawFileStorePrx;
import omero.api.RawPixelsStore;
import omero.api.RawPixelsStorePrx;
import omero.api.RenderingEngine;
import omero.api.RenderingEnginePrx;
import omero.api.ServiceFactoryPrx;
import omero.api.ServiceInterfacePrx;
import omero.api.ThumbnailStore;
import omero.api.ThumbnailStorePrx;
import omero.gateways.DSAccessException;
import omero.gateways.DSOutOfServiceException;



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
	
	/** The proxy to the session object. */
	private final ServiceFactoryPrx session;
	
	/** Session closed variable, determining if session closed. */
	private volatile boolean sessionClosed = true;
	
	private HashMap<String, ServiceInterfacePrx> serviceMap;
	
	private static final String IRenderingSettings = "IRenderingSettings";
	private static final String IPixels = "IPixels";
	private static final String RenderingEngine = "RenderingEngine";
	private static final String IScript = "IScript";
	private static final String IRepositoryInfoService = "IRepositoryInfoService";
	private static final String IPojosService = "IPojosService";
	private static final String IQueryService = "IQueryService";
	private static final String IAdminService = "IAdminService";
	private static final String ThumbnailStore = "ThumbnailStore";
	private static final String PixelsStore = "PixelsStore";
	private static final String RawFileStore = "RawFileStore";
	private static final String IUpdateService = "IUpdateService";
	private static final String ITypeService = "ITypeService";
	
	/**
	 * Instantiate the BlitzGateway object.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	BlitzGateway(ServiceFactoryPrx prx) throws DSOutOfServiceException, DSAccessException
	{
		session = prx;
		sessionClosed = false;
		serviceMap = new HashMap<String, ServiceInterfacePrx>();
		createServices();
	}
	
	/**
	 * This makes sure all services have been created before the other gateways
	 * need to call them. 
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	private void createServices() throws DSOutOfServiceException, DSAccessException
	{
		getRenderingSettingsService();
		getPixelsService();
		getScriptService();
		getRepositoryService();
		getPojosService();
		getQueryService();
		getUpdateService();
		getAdminService();
		getTypesService();
	}
	
	/**
	 * Keep all services in the serviceMap alive.
	 */
	public void keepAlive()
	{
		Iterator<ServiceInterfacePrx> iterator = serviceMap.values().iterator();
		while(iterator.hasNext())
			session.keepAlive(iterator.next());
	}
	
	/**
	 * Keep service alive.
	 * @param prx service proxy to keep alive.
	 */
	public void keepAlive(ServiceInterfacePrx prx)
	{
		session.keepAlive(prx);
	}
	
	/** Close the connection to the blitz server. */
	public void close()
	{
		synchronized(serviceMap)
		{
		    session.destroy();
			sessionClosed = true;
		}
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
		String currentService = IRenderingSettings;
		try
		{
			synchronized(serviceMap)
			{
				if(serviceMap.containsKey(currentService))
					return (IRenderingSettingsPrx)serviceMap.get(currentService);
				else
				{
					IRenderingSettingsPrx service = 
									session.getRenderingSettingsService();
					serviceMap.put(currentService, service);
					return service;
				}
			}
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
		String currentService = IPixels;
		try
		{
			synchronized(serviceMap)
			{
				if(serviceMap.containsKey(currentService))
					return (IPixelsPrx)serviceMap.get(currentService);
				else
				{
					IPixelsPrx service = 
						session.getPixelsService();
					serviceMap.put(currentService, service);
					return service;
				}
			}
		}
		catch (ServerError e)
		{
			ServiceUtilities.handleException(e, statelessServiceAccessException+currentService);
		}
		return null;
	}
	
	/**
	 * Returns a reference to the rendering engine {@link RenderingEngine}.
	 * @return see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public RenderingEnginePrx getRenderingEngineService()
	throws DSOutOfServiceException, DSAccessException 
	{
		String currentService = "RenderingEngine";
		try
		{
			synchronized(serviceMap)
			{
				RenderingEnginePrx renderingEngine = session.createRenderingEngine();
				return renderingEngine;
			}
		}
		catch (ServerError e)
		{
			ServiceUtilities.handleException(e, statefulServiceAccessException+currentService);
		}
		return null;
	}
	
	/**
	 * Returns the {@link IScript} service.
	 * 
	 * @return See above.
	 * @throws DSOutOfServiceException 
	 * @throws DSAccessException 
	 */
	public IScriptPrx getScriptService()
	throws DSOutOfServiceException, DSAccessException 
	{
		String currentService = IScript;
		try
		{
			synchronized(serviceMap)
			{
				if(serviceMap.containsKey(currentService))
					return (IScriptPrx)serviceMap.get(currentService);
				else
				{
					IScriptPrx service = 
							session.getScriptService();
					serviceMap.put(currentService, service);
					return service;
				}
			}
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
		String currentService = IRepositoryInfoService;
		try
		{
			synchronized(serviceMap)
			{
				if(serviceMap.containsKey(currentService))
				return (IRepositoryInfoPrx)serviceMap.get(currentService);
				else
				{
					IRepositoryInfoPrx service = 
						session.getRepositoryInfoService();
					serviceMap.put(currentService, service);
					return service;
				}
			}
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
		String currentService = IPojosService;
		try
		{
			synchronized(serviceMap)
			{
				if(serviceMap.containsKey(currentService))
					return (IPojosPrx)serviceMap.get(currentService);
				else
				{
					IPojosPrx service = 
						session.getPojosService();
					serviceMap.put(currentService, service);
					return service;
				}
			}
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
		String currentService = IQueryService;
		try
		{
			synchronized(serviceMap)
			{
				if(serviceMap.containsKey(currentService))
					return (IQueryPrx)serviceMap.get(currentService);
				else
				{
					IQueryPrx service = 
						session.getQueryService();
					serviceMap.put(currentService, service);
					return service;
				}
			}
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
		String currentService = IUpdateService;
		try
		{
			synchronized(serviceMap)
			{
				if(serviceMap.containsKey(currentService))
					return (IUpdatePrx)serviceMap.get(currentService);
				else
				{
					IUpdatePrx service = 
						session.getUpdateService();
					serviceMap.put(currentService, service);
					return service;
				}
			}
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
		String currentService = IAdminService;
		try
		{
			synchronized(serviceMap)
			{
				if(serviceMap.containsKey(currentService))
					return (IAdminPrx)serviceMap.get(currentService);
				else
				{
					IAdminPrx service = 
						session.getAdminService();
					serviceMap.put(currentService, service);
					return service;
				}
			}
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
	public ThumbnailStorePrx getThumbnailService()
		throws DSOutOfServiceException, DSAccessException 
	{ 
		String currentService = "ThumbnailStore";
		try
		{
			synchronized(serviceMap)
			{
				ThumbnailStorePrx thumbnailStore = session.createThumbnailStore();
				return thumbnailStore;
			}
		}
		catch (ServerError e)
		{
			ServiceUtilities.handleException(e, statefulServiceAccessException + currentService);
		}
		return null;
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
		String currentService = RawFileStore;
		try
		{
			synchronized(serviceMap)
			{
				return session.createRawFileStore();
			}
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
		String currentService = PixelsStore;
		try
		{
			synchronized(serviceMap)
			{
				RawPixelsStorePrx pixelsStore =  session.createRawPixelsStore();
				return pixelsStore;
			}
		}
		catch (ServerError e)
		{
			ServiceUtilities.handleException(e, statefulServiceAccessException + currentService);
		}
		return null;
	}
	
	/**
	 * Returns the {@link TypeService} service.
	 * 
	 * @return See above.
	 * @throws DSOutOfServiceException 
	 * @throws DSAccessException 
	 */
	public ITypesPrx getTypesService() 
	throws DSOutOfServiceException, DSAccessException 
	{ 
		String currentService = ITypeService;
		try
		{
			synchronized(serviceMap)
			{
				if(serviceMap.containsKey(currentService))
					return (ITypesPrx)serviceMap.get(currentService);
				else
				{
					ITypesPrx service = 
						session.getTypesService();
					serviceMap.put(currentService, service);
					return service;
				}
			}
		}
		catch (ServerError e)
		{
			ServiceUtilities.handleException(e, statelessServiceAccessException + currentService);
		}
		return null;
	}
			
	/** 
	 * Get the username.
	 * @return see above.
	 */
	public String getUserName()
	throws DSOutOfServiceException, DSAccessException 
	{
	    try {
	        return getAdminService().getEventContext().userName;
	    } catch (ServerError e) {
	        ServiceUtilities.handleException(e, "Failed to get username");
	    }
	    return null; 
	}
}



