/*
 * blitzgateway.service.gateway.ThumbnailServiceGatewayImpl 
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

import java.util.List;
import java.util.Map;

import omero.RInt;
import omero.ServerError;
import omero.api.RenderingEnginePrx;
import omero.api.ThumbnailStorePrx;
import omero.model.ThumbnailPrx;


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
public class ThumbnailGatewayImpl
	implements ThumbnailGateway
{
	/** The blitzGateway. */
	private BlitzGateway 		blitzGateway;
	
	/** The current pixelsId of the renderingGateway. */
	private Long		 		pixelsId;
	
	/** The ThumbNail Proxy from Ice. */
	private ThumbnailStorePrx 	service;
	

	/**
	 * Create the ThumbnailService for the pixelsId and get the service from the gateway.
	 * @param pixelsId see above.
	 * @param gateway see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	ThumbnailGatewayImpl(Long pixelsId, BlitzGateway gateway) 
						throws DSOutOfServiceException, DSAccessException
	{
		blitzGateway = gateway;
		this.pixelsId = pixelsId;
		service = createThumbnailService(pixelsId);
	}
	/**
	 * Creates a new thumbnail service for the specified pixels set.
	 * 
	 * @param pixelsId  The pixels set ID.
	 * @return See above.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occured while trying to 
	 * retrieve data from OMERO service. 
	 */
	private ThumbnailStorePrx createThumbnailService(long pixelsId)
		throws DSOutOfServiceException, DSAccessException
	{
		try 
		{
			ThumbnailStorePrx service = blitzGateway.getThumbnailService();
			if(!service.setPixelsId(pixelsId))
				service.resetDefaults();
			return service;
		} 
		catch (Throwable t) 
		{
			ServiceUtilities.handleException(t, "Cannot start the ThumbnailService for pixels : "+ pixelsId);
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see blitzgateway.service.gateway.ThumbnailGateway#getThumbnail(omero.RInt, omero.RInt)
	 */
	public byte[] getThumbnail(RInt sizeX, RInt sizeY)
			throws DSOutOfServiceException, DSAccessException
	{
		try
		{
			return service.getThumbnail(sizeX, sizeY);
		}
		catch(Exception e)
		{
			ServiceUtilities.handleException(e, "getThumbnail for pixelsId : " + pixelsId);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see blitzgateway.service.gateway.ThumbnailGateway#getThumbnailByLongestSide(omero.RInt)
	 */
	public byte[] getThumbnailByLongestSide(RInt size)
			throws DSOutOfServiceException, DSAccessException
	{
		try
		{
			return service.getThumbnailByLongestSide(size);
		}
		catch(Exception e)
		{
			ServiceUtilities.handleException(e, "getThumbnailLongestSide for pixelsId : " + pixelsId);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see blitzgateway.service.gateway.ThumbnailGateway#getThumbnailByLongestSideSet(omero.RInt, java.util.List)
	 */
	public Map<Long, byte[]> getThumbnailByLongestSideSet(RInt size,
			List<Long> pixelsIds) throws DSOutOfServiceException,
			DSAccessException
	{
		try
		{
			return service.getThumbnailByLongestSideSet(size, pixelsIds);
		}
		catch(Exception e)
		{
			ServiceUtilities.handleException(e, "getThumbnailLongestSide for pixelsId : " + pixelsIds);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see blitzgateway.service.gateway.ThumbnailGateway#getThumbnailSet(omero.RInt, omero.RInt, java.util.List)
	 */
	public Map<Long, byte[]> getThumbnailSet(RInt sizeX, RInt sizeY,
			List<Long> pixelsIds) throws DSOutOfServiceException,
			DSAccessException
	{
		try
		{
			return service.getThumbnailSet(sizeX, sizeY, pixelsIds);
		}
		catch(Exception e)
		{
			ServiceUtilities.handleException(e, "getThumbnailSet for pixelsId : " + pixelsIds);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see blitzgateway.service.gateway.ThumbnailGateway#setRenderingDefId(long)
	 */
	public void setRenderingDefId(long renderingDefId)
			throws DSOutOfServiceException, DSAccessException
	{
		try
		{
			service.setRenderingDefId(renderingDefId);
		}
		catch(Exception e)
		{
			ServiceUtilities.handleException(e, "setRenderingDefId for pixelsId : " + pixelsId);
		}
	}
	/* (non-Javadoc)
	 * @see blitzgateway.service.gateway.BaseServiceInterface#keepAlive()
	 */
	public void keepAlive() throws DSOutOfServiceException, DSAccessException
	{
		blitzGateway.keepAlive(service);
	}

}


