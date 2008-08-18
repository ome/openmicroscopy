/*
 * blitzgateway.service.RawPixelsStoreServiceImpl 
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



//Java imports
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

//Third-party libraries

//Application-internal dependencies




import omero.gateway.DSAccessException;
import omero.gateway.DSOutOfServiceException;


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
public class RawPixelsStoreServiceImpl
	implements RawPixelsStoreService
{	
	/** The gateway factory to create make connection, create and access 
	 *  services .
	 */
	private GatewayFactory 	gatewayFactory;

	/** 
	 * Map of the pixelsId and the gateway, this is used to store the created
	 * RawFileStoreGateway. 
	 */
	private Map<Long, RawPixelsStoreGateway> gatewayMap;
	
	/**
	 * Create the ImageService passing the gateway.
	 * @param gatewayFactory To generate new instances of the 
	 * RenderingEngineGateway.
	 */
	public RawPixelsStoreServiceImpl(GatewayFactory gatewayFactory) 
	{
		this.gatewayFactory = gatewayFactory;
		gatewayMap = new HashMap<Long, RawPixelsStoreGateway>();
	}

	/**
	 * Get the gateway for RawPixelsStoreGateway from the map, if it does not exist create it
	 * and add it to the map.
	 * @param pixelsId see above.
	 * @return see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	private RawPixelsStoreGateway getGateway(Long pixelsId) throws DSOutOfServiceException, DSAccessException
	{
		synchronized(gatewayMap)
		{
			if(gatewayMap.containsKey(pixelsId))
			{
				return gatewayMap.get(pixelsId);
			}
			else
			{
				RawPixelsStoreGateway gateway = gatewayFactory.getRawPixelsStoreGateway(pixelsId);
				gatewayMap.put(pixelsId, gateway);
				return gateway;
			}
		}
	}
	
	/**
	 * Does the gateway map contain the gateway for fileId.
	 * @param pixelsId see above.
	 * @return see above.
	 */
	public boolean containsGateway(long pixelsId)
	{
		synchronized(gatewayMap)
		{
			return gatewayMap.containsKey(pixelsId);
		}
	}
	
	/**
	 * Close the gateway for pixels = pixelId
	 * @param pixelsId see above.
	 * @return true if the gateway was closed.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public boolean closeGateway(long pixelsId) throws DSOutOfServiceException, DSAccessException
	{
		synchronized(gatewayMap)
		{
			if(containsGateway(pixelsId))
			{
				gatewayMap.remove(pixelsId);
				return true;
			}
			else
				return false;
		}
	}

	/* (non-Javadoc)
	 * @see blitzgateway.service.RawPixelsStoreService#getByteWidth(long)
	 */
	public int getByteWidth(long pixelsId) throws DSOutOfServiceException,
			DSAccessException
	{
		RawPixelsStoreGateway gateway = getGateway(pixelsId);
		synchronized(gateway)
		{
			return gateway.getByteWidth();
		}
	}

	/* (non-Javadoc)
	 * @see blitzgateway.service.RawPixelsStoreService#getPlane(long, int, int, int)
	 */
	public byte[] getPlane(long pixelsId, int z, int c, int t)
			throws DSOutOfServiceException, DSAccessException
	{
		RawPixelsStoreGateway gateway = getGateway(pixelsId);
		synchronized(gateway)
		{
			return gateway.getPlane(z, c, t);
		}
	}

	/* (non-Javadoc)
	 * @see blitzgateway.service.RawPixelsStoreService#getPlaneSize(long)
	 */
	public int getPlaneSize(long pixelsId) throws DSOutOfServiceException,
			DSAccessException
	{
		RawPixelsStoreGateway gateway = getGateway(pixelsId);
		synchronized(gateway)
		{
			return gateway.getPlaneSize();
		}
	}

	/* (non-Javadoc)
	 * @see blitzgateway.service.RawPixelsStoreService#getRowSize(long)
	 */
	public int getRowSize(long pixelsId) throws DSOutOfServiceException,
			DSAccessException
	{
		RawPixelsStoreGateway gateway = getGateway(pixelsId);
		synchronized(gateway)
		{
			return gateway.getRowSize();
		}
	}

	/* (non-Javadoc)
	 * @see blitzgateway.service.RawPixelsStoreService#getStackSize(long)
	 */
	public int getStackSize(long pixelsId) throws DSOutOfServiceException,
			DSAccessException
	{
		RawPixelsStoreGateway gateway = getGateway(pixelsId);
		synchronized(gateway)
		{
			return gateway.getStackSize();
		}
	}

	/* (non-Javadoc)
	 * @see blitzgateway.service.RawPixelsStoreService#getTimepointSize(long)
	 */
	public int getTimepointSize(long pixelsId) throws DSOutOfServiceException,
			DSAccessException
	{
		RawPixelsStoreGateway gateway = getGateway(pixelsId);
		synchronized(gateway)
		{
			return gateway.getTimepointSize();
		}
	}

	/* (non-Javadoc)
	 * @see blitzgateway.service.RawPixelsStoreService#getTotalSize(long)
	 */
	public int getTotalSize(long pixelsId) throws DSOutOfServiceException,
			DSAccessException
	{
		RawPixelsStoreGateway gateway = getGateway(pixelsId);
		synchronized(gateway)
		{
			return gateway.getTotalSize();
		}
	}

	/* (non-Javadoc)
	 * @see blitzgateway.service.RawPixelsStoreService#isFloat(long)
	 */
	public boolean isFloat(long pixelsId) throws DSOutOfServiceException,
			DSAccessException
	{
		RawPixelsStoreGateway gateway = getGateway(pixelsId);
		synchronized(gateway)
		{
			return gateway.isFloat();
		}
	}

	/* (non-Javadoc)
	 * @see blitzgateway.service.RawPixelsStoreService#isSigned(long)
	 */
	public boolean isSigned(long pixelsId) throws DSOutOfServiceException,
			DSAccessException
	{
		RawPixelsStoreGateway gateway = getGateway(pixelsId);
		synchronized(gateway)
		{
			return gateway.isSigned();
		}
	}

	/* (non-Javadoc)
	 * @see blitzgateway.service.RawPixelsStoreService#setPlane(long, byte[], int, int, int)
	 */
	public void setPlane(long pixelsId, byte[] buf, int z, int c, int t)
			throws DSOutOfServiceException, DSAccessException
	{
		RawPixelsStoreGateway gateway = getGateway(pixelsId);
		synchronized(gateway)
		{
			gateway.setPlane(buf, z, c, t);		
		}
	}
	
	/* (non-Javadoc)
	 * @see blitzgateway.service.gateway.BaseServiceInterface#keepAlive()
	 */
	public void keepAlive() throws DSOutOfServiceException, DSAccessException
	{
		Iterator<RawPixelsStoreGateway> gatewayIterator = gatewayMap.values().iterator();
		while(gatewayIterator.hasNext())
		{
			RawPixelsStoreGateway gateway = gatewayIterator.next();
			gateway.keepAlive();
		}
	}

}


