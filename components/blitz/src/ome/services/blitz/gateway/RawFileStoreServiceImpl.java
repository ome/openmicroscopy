/*
 * blitzgateway.service.RawFileStoreServiceImpl 
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
public class RawFileStoreServiceImpl
	implements RawFileStoreService
{	
	/** The gateway factory to create make connection, create and access 
	 *  services .
	 */
	private GatewayFactory 	gatewayFactory;

	/** 
	 * Map of the pixelsId and the gateway, this is used to store the created
	 * RawFileStoreGateway. 
	 */
	private Map<Long, RawFileStoreGateway> gatewayMap;
	
	/**
	 * Create the ImageService passing the gateway.
	 * @param gatewayFactory To generate new instances of the 
	 * RenderingEngineGateway.
	 */
	public RawFileStoreServiceImpl(GatewayFactory gatewayFactory) 
	{
		this.gatewayFactory = gatewayFactory;
		gatewayMap = new HashMap<Long, RawFileStoreGateway>();
	}

	/**
	 * Get the gateway for RawFileStoreGateway from the map, if it does not exist create it
	 * and add it to the map.
	 * @param fileId see above.
	 * @return see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	private RawFileStoreGateway getGateway(Long fileId) throws DSOutOfServiceException, DSAccessException
	{
		synchronized(gatewayMap)
		{
			if(gatewayMap.containsKey(fileId))
			{
				return gatewayMap.get(fileId);
			}
			else
			{
				RawFileStoreGateway gateway = gatewayFactory.getRawFileStoreGateway(fileId);
				gatewayMap.put(fileId, gateway);
				return gateway;
			}
		}
	}
	
	/**
	 * Does the gateway map contain the gateway for fileId.
	 * @param fileId see above.
	 * @return see above.
	 */
	public boolean containsGateway(long fileId)
	{
		synchronized(gatewayMap)
		{
			return gatewayMap.containsKey(fileId);
		}
	}
	
	/**
	 * Close the gateway for pixels = fileId
	 * @param fileId see above.
	 * @return true if the gateway was closed.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public boolean closeGateway(long fileId) throws DSOutOfServiceException, DSAccessException
	{
		synchronized(gatewayMap)
		{
			if(containsGateway(fileId))
			{
				gatewayMap.remove(fileId);
				return true;
			}
			else
				return false;
		}
	}

	/* (non-Javadoc)
	 * @see blitzgateway.service.RawFileStoreService#exists(long)
	 */
	public boolean exists(long fileId) throws DSOutOfServiceException,
			DSAccessException
	{
		RawFileStoreGateway gateway = getGateway(fileId);
		synchronized(gateway)
		{
			return gateway.exists();
		}
	}

	/* (non-Javadoc)
	 * @see blitzgateway.service.RawFileStoreService#read(long, long, int)
	 */
	public byte[] read(long fileId, long position, int length)
			throws DSOutOfServiceException, DSAccessException
	{
		RawFileStoreGateway gateway = getGateway(fileId);
		synchronized(gateway)
		{
			return gateway.read(position, length);
		}
	}

	/* (non-Javadoc)
	 * @see blitzgateway.service.RawFileStoreService#write(long, byte[], long, int)
	 */
	public void write(long fileId, byte[] buf, long position, int length)
			throws DSOutOfServiceException, DSAccessException
	{
		RawFileStoreGateway gateway = getGateway(fileId);
		synchronized(gateway)
		{
			gateway.write(buf, position, length);
		}
	}

	/* (non-Javadoc)
	 * @see blitzgateway.service.gateway.BaseServiceInterface#keepAlive()
	 */
	public void keepAlive() throws DSOutOfServiceException, DSAccessException
	{
		Iterator<RawFileStoreGateway> gatewayIterator = gatewayMap.values().iterator();
		while(gatewayIterator.hasNext())
		{
			RawFileStoreGateway gateway = gatewayIterator.next();
			gateway.keepAlive();
		}
	}

}


