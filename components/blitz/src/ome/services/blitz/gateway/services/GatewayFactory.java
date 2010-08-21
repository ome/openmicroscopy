/*
 * ome.services.blitz.omerogateway.services.GatewayFactory 
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
package ome.services.blitz.gateway.services;


//Java imports

//Third-party libraries

//Application-internal dependencies
import ome.services.blitz.gateway.services.impl.DataServiceImpl;
import ome.services.blitz.gateway.services.impl.ImageServiceImpl;
import ome.services.blitz.gateway.services.impl.RawFileStoreServiceImpl;
import ome.services.blitz.gateway.services.impl.RawPixelsStoreServiceImpl;
import ome.services.blitz.gateway.services.impl.RenderingServiceImpl;
import ome.services.blitz.gateway.services.impl.ThumbnailServiceImpl;
import omero.ServerError;
import omero.api.IPixelsPrx;
import omero.api.IContainerPrx;
import omero.api.IProjectionPrx;
import omero.api.IQueryPrx;
import omero.api.IScriptPrx;
import omero.api.ITypesPrx;
import omero.api.IUpdatePrx;
import omero.api.RawFileStorePrx;
import omero.api.RawPixelsStorePrx;
import omero.api.RenderingEnginePrx;
import omero.api.ServiceFactoryPrx;
import omero.api.ThumbnailStorePrx;

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
public class GatewayFactory
	implements GatewayService
{	
	
	/** The service factory from which all services are created. */
	private ServiceFactoryPrx			serviceFactory;
	
	/** The iQuery Service. */ 
	private IQueryPrx 					iQuery;

	/** The iPojo Service. */ 
	private IContainerPrx 					iContainer;

	/** The IScript Service. */ 
	private IScriptPrx 					iScript;

	/** The ITypes Service. */ 
	private ITypesPrx					iTypes;
	
	/** The iPixels service. */
	private IPixelsPrx 					iPixels;
	
	/** The iUpdate service. */
	private IUpdatePrx 					iUpdate;
	
	/** The Projection service. */
	private IProjectionPrx				iProjection;
	
	/** The rendering Engine service. */
	private RenderingService 			renderingService;
	
	/** The thumbnailService service. */
	private ThumbnailService 			thumbnailService;

	/** The pixelsStore service. */
	private RawPixelsStoreService 		rawPixelsService;
	
	/** The pixelsStore service. */
	private RawFileStoreService 		rawFileService;
	
	/** The rawPixelsStore. */
	private RawPixelsStorePrx 			rawPixelsStore;
	
	/** The RawFileStore. */
	private RawFileStorePrx 			rawFileStore;
	
	/** The ThumbnailStore. */
	private	ThumbnailStorePrx			thumbnailStore;
	
	/** The RenderingEngine. */
	private RenderingEnginePrx			renderingEngine;
	
	/** 
	 * The image Service which manages any interaction with image data, 
	 * either Pixels, or raw. 
	 */
	private ImageService				imageService;
	
	/**
	 * Manages manipulations of datastructures, projects, dataset and image.
	 * As well as interactions with scripts. 
	 */
	private DataService					dataService;
	
	/** 
	 * Create services from the serviceFactory. 
	 * @param serviceFactory see above.
	 * @throws ServerError 
	 */
	public GatewayFactory(ServiceFactoryPrx serviceFactory) 
			throws ServerError
	{
		this.serviceFactory = serviceFactory;
		createServices();
	}
	
	/**
	 * Create the services.
	 * @throws ServerError 
	 */
	private void createServices() throws ServerError
	{
		iQuery = serviceFactory.getQueryService();
		iPixels = serviceFactory.getPixelsService();
		iUpdate = serviceFactory.getUpdateService();
		iScript	= serviceFactory.getScriptService();
		iContainer = serviceFactory.getContainerService();
		iProjection = serviceFactory.getProjectionService();
		rawPixelsStore = serviceFactory.createRawPixelsStore();
		rawFileStore = serviceFactory.createRawFileStore();
		renderingEngine = serviceFactory.createRenderingEngine();
		thumbnailStore = serviceFactory.createThumbnailStore();
		iTypes = serviceFactory.getTypesService();
		rawFileService = new RawFileStoreServiceImpl(rawFileStore);
		rawPixelsService = new RawPixelsStoreServiceImpl(rawPixelsStore);
		renderingService = new RenderingServiceImpl(renderingEngine);
		thumbnailService = new ThumbnailServiceImpl(thumbnailStore);
		dataService = new DataServiceImpl(this);
		imageService = new ImageServiceImpl(this);
	}
	
	public ImageService getImageService()
	{
		return imageService;
	}
	
	public DataService getDataService()
	{
		return dataService;
	}
	
	/**
	 * Return the raw file store service.
	 * @return see above.
	 */
	public RawFileStoreService getRawFileStoreService()
	{
		return rawFileService;
	}
	
	/**
	 * Return the raw pixels store service.
	 * @return see above.
	 */
	public RawPixelsStoreService getRawPixelsStoreService()
	{
		return rawPixelsService;
	}
		
	/**
	 * Return the thumbnail service.
	 * @return see above.
	 */
	public ThumbnailService getThumbnailStoreService()
	{
		return thumbnailService;
	}
	
	/**
	 * Return the rendering service. 
	 * @return see above.
	 */
	public RenderingService getRenderingService()
	{
		return renderingService;
	}
	
	
	/* (non-Javadoc)
	 * @see ome.services.blitz.omerogateway.services.GatewayService#closeService()
	 */
	public void closeService() throws ServerError
	{
		serviceFactory = null;
	}

	/* (non-Javadoc)
	 * @see ome.services.blitz.omerogateway.services.GatewayService#keepAlive()
	 */
	public void keepAlive() throws ServerError
	{
		
	}

	/**
	 * Return the IScript service.
	 * @return see above.
	 */
	public IScriptPrx getIScript()
	{
		return iScript;
	}
	
	/**
	 * Return the IContainer service.
	 * @return see above.
	 */
	public IContainerPrx getIContainer()
	{
		return iContainer;
	}
		
	/**
	 * Get the types service.  
	 * @return see above.
	 */
	public ITypesPrx getITypes()
	{
		return iTypes;
	}
	
	/**
	 * Get the IQuery Service. 
	 * @return see above.
	 */
	public IQueryPrx getIQuery()
	{
		return iQuery;
	}

	/**
	 * Get the iPixels Service. 
	 * @return see above.
	 */
	public IPixelsPrx getIPixels()
	{
		return iPixels;
	}

	/**
	 * Get the iUpdate Service. 
	 * @return see above.
	 */
	public IUpdatePrx getIUpdate()
	{
		return iUpdate;
	}

}


