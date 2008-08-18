/*
 * blitzgateway.service.gateway.GatewayFactory 
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
package ome.services.blitz.gateway;

import omero.api.ServiceFactoryPrx;
import omero.gateways.DSAccessException;
import omero.gateways.DSOutOfServiceException;


//Java imports

//Third-party libraries

//Application-internal dependencies


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
{	 
	/** The Blitzgateway for this session. */
	private BlitzGateway 			blitzGateway;
	
	/** The IPixelsGateway for this session. */
	private IPixelsGateway 			iPixelsGateway;
	
	/** The IPojoGateway for this session. */
	private IPojoGateway  			iPojoGateway;

	/** The IScriptGateway for this session. */
	private IScriptGateway  		iScriptGateway;
	
	/** The IQueryGateway for this session. */
	private IQueryGateway			iQueryGateway;
	
	/** The IUpdateGateway for this session. */
	private IUpdateGateway			iUpdateGateway;
	
	/** The ITypeGateway for this session. */
	private ITypeGateway			iTypeGateway;
		
	/** The RawPixelsStoreGateway for this session. */
	private RawPixelsStoreGateway 	rawPixelsStoreGateway;
		
	/** The lock on the stateful service. This may change to a re-entrant lock
	 * later?*/
	private String statefulServiceLock;
	
	/**
	 * Create the blitzGateway object and instantiate the service gateways.
	 * @param client the already existing client object.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public GatewayFactory(ServiceFactoryPrx prx) 
			throws DSOutOfServiceException, DSAccessException 
	{
		statefulServiceLock = new String("RenderingEngine/Thumbnail ServiceLock");
		blitzGateway = new BlitzGateway(prx);
		startServices();
	}

	/**
	 * Create the default gateways from the blitzGateway.
	 */
	private void startServices()
	{
		iPixelsGateway = new IPixelsGatewayImpl(blitzGateway);
		iPojoGateway = new IPojoGatewayImpl(blitzGateway);
		iScriptGateway = new IScriptGatewayImpl(blitzGateway);
		iQueryGateway = new IQueryGatewayImpl(blitzGateway);
		iUpdateGateway = new IUpdateGatewayImpl(blitzGateway);
		iTypeGateway = new ITypeGatewayImpl(blitzGateway);
	}
	
	/** Close the connection to the blitz server. */
	public void close()
	{
		blitzGateway.close();
	}
	
	/**
	 * Is the session closed?
	 * @return true if closed.
	 */
	public boolean isClosed()
	{
		return blitzGateway.isClosed();
	}
	
	/** 
	 * Get the username.
	 * @return see above.
	 */
	public String getUsername() throws DSOutOfServiceException, DSAccessException
	{
		return blitzGateway.getUserName();
	}

	/**
	 * Get the RenderingEngineGateway instance for this session.
	 * @param pixelsId create the gateway for pixels.
	 * @return see above.
	 * @throws DSAccessException 
	 * @throws DSOutOfServiceException 
	 */
	public RenderingEngineGateway getRenderingEngineGateway(Long pixelsId) throws DSOutOfServiceException, DSAccessException
	{
		synchronized(statefulServiceLock)
		{
			RenderingEngineGateway renderingEngine = new RenderingEngineGatewayImpl(pixelsId, blitzGateway);
			return renderingEngine;
		}
	}
	
	/**
	 * Get the RenderingEngineGateway instance for this session.
	 * @param fileId create the gateway for pixels.
	 * @return see above.
	 * @throws DSAccessException 
	 * @throws DSOutOfServiceException 
	 */
	public RawFileStoreGateway getRawFileStoreGateway(Long fileId) throws DSOutOfServiceException, DSAccessException
	{
		RawFileStoreGateway rawFileStoreGateway = new RawFileStoreGatewayImpl(fileId, blitzGateway);
		return rawFileStoreGateway;
	}

	/**
	 * Get the RenderingEngineGateway instance for this session.
	 * @param pixelsId create the gateway for pixels.
	 * @return see above.
	 * @throws DSAccessException 
	 * @throws DSOutOfServiceException 
	 */
	public RawPixelsStoreGateway getRawPixelsStoreGateway(Long pixelsId) throws DSOutOfServiceException, DSAccessException
	{
		RawPixelsStoreGateway rawPixelsStoreGateway = new RawPixelsStoreGatewayImpl(pixelsId, blitzGateway);
		return rawPixelsStoreGateway;
	}
	
	/**
	 * Get the ThumbnailGateway instance for this session.
	 * @param pixelsId create the gateway for pixels.
	 * @return see above.
	 * @throws DSAccessException 
	 * @throws DSOutOfServiceException 
	 */
	public ThumbnailGateway getThumbnailGateway(Long pixelsId) throws DSOutOfServiceException, DSAccessException
	{
		synchronized(statefulServiceLock)
		{
			ThumbnailGateway thumbnailGateway = new ThumbnailGatewayImpl(pixelsId, blitzGateway);
			return thumbnailGateway;
		}
	}

	/**
	 * Get the IPixelsGateway instance for this session.
	 * @return see above.
	 */
	public IPixelsGateway getIPixelsGateway()
	{
		return iPixelsGateway;
	}

	/**
	 * Get the IPojoGateway instance for this session.
	 * @return see above.
	 */
	public IPojoGateway getIPojoGateway()
	{
		return iPojoGateway;
	}
	
	/**
	 * Get the IScriptGateway instance for this session.
	 * @return see above.
	 */
	public IScriptGateway getIScriptGateway()
	{
		return iScriptGateway;
	}

	/**
	 * Get the IQueryGateway instance for this session.
	 * @return see above.
	 */
	public IQueryGateway getIQueryGateway()
	{
		return iQueryGateway;
	}

	/**
	 * Get the IUpdateGateway instance for this session.
	 * @return see above.
	 */
	public IUpdateGateway getIUpdateGateway()
	{
		return iUpdateGateway;
	}
	
	/**
	 * Get the IUpdateGateway instance for this session.
	 * @return see above.
	 */
	public ITypeGateway getITypeGateway()
	{
		return iTypeGateway;
	}
	
	/**
	 * Keep the services alive which are created by the blitzGateway.
	 */
	public void keepAlive()
	{
		blitzGateway.keepAlive();
	}

}


