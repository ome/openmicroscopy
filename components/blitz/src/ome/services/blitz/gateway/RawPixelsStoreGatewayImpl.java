/*
 * blitzgateway.service.gateway.RawPixelsStoreImpl 
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

//Third-party libraries

//Application-internal dependencies
import omero.api.RawPixelsStorePrx;


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
class RawPixelsStoreGatewayImpl
	implements RawPixelsStoreGateway
{	
	
	/** The blitzgateway. */
	private BlitzGateway blitzGateway;
	
	/** The PixelsId this PixelsStore accesses. */
	private long		pixelsId;
	
	/** The PixelsStore of the service.*/
	private RawPixelsStorePrx service;
	
	/**
	 * Create the pixelsStore for the pixelsId and get the service from the gateway.
	 * @param pixelsId see above.
	 * @param gateway see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	RawPixelsStoreGatewayImpl(long pixelsId, BlitzGateway gateway) throws DSOutOfServiceException, DSAccessException
	{
		blitzGateway = gateway;
		this.pixelsId = pixelsId;
		service = createRawPixelsService(pixelsId);
	}


	/**
	 * Creates a new rawPixels service for the specified pixelsId
	 * 
	 * @param pixelsId  The pixels set ID.
	 * @return See above.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occured while trying to 
	 * retrieve data from OMERO service. 
	 */
	private RawPixelsStorePrx createRawPixelsService(long pixelsId)
		throws DSOutOfServiceException, DSAccessException
	{
		try 
		{
			RawPixelsStorePrx service = blitzGateway.getPixelsStore();
			service.setPixelsId(pixelsId);
			return service;
		} 
		catch (Throwable t) 
		{
			ServiceUtilities.handleException(t, "Cannot start the RawPixelsStore for fileId : "+ pixelsId);
		}
		return null;
	}
	

	/* (non-Javadoc)
	 * @see blitzgateway.service.gateway.RawPixelsStoreGateway#getPlane()
	 */
	public byte[] getPlane(int z, int c, int t) 
			throws DSOutOfServiceException, DSAccessException
	{
		try
		{
			return service.getPlane(z, c, t);
		}
		catch (Exception e)
		{
			ServiceUtilities.handleException(e, "Cannot retrieve plane .");
		}
		return null;
	}


	/* (non-Javadoc)
	 * @see blitzgateway.service.gateway.RawPixelsStoreGateway#getByteWidth()
	 */
	public int getByteWidth() throws DSOutOfServiceException, DSAccessException
	{	
		try
		{
			return service.getByteWidth(); 
		}
		catch (Exception e)
		{
			ServiceUtilities.handleException(e, "RawPixelsStore.getByteWidth");
		}
		return -1;
	}


	/* (non-Javadoc)
	 * @see blitzgateway.service.gateway.RawPixelsStoreGateway#getPlaneSize()
	 */
	public int getPlaneSize() throws DSOutOfServiceException, DSAccessException
	{	
		try
		{
			return service.getPlaneSize(); 
		}
		catch (Exception e)
		{
			ServiceUtilities.handleException(e, "RawPixelsStore.getPlaneSize");
		}
		return -1;
	}


	/* (non-Javadoc)
	 * @see blitzgateway.service.gateway.RawPixelsStoreGateway#getRowSize()
	 */
	public int getRowSize() throws DSOutOfServiceException, DSAccessException
	{	
		try
		{
			return service.getRowSize(); 
		}
		catch (Exception e)
		{
			ServiceUtilities.handleException(e, "RawPixelsStore.getRowSize");
		}
		return -1;
	}


	/* (non-Javadoc)
	 * @see blitzgateway.service.gateway.RawPixelsStoreGateway#getStackSize()
	 */
	public int getStackSize() throws DSOutOfServiceException, DSAccessException
	{	
		try
		{
			return service.getStackSize(); 
		}
		catch (Exception e)
		{
			ServiceUtilities.handleException(e, "RawPixelsStore.getStackSize");
		}
		return -1;
	}


	/* (non-Javadoc)
	 * @see blitzgateway.service.gateway.RawPixelsStoreGateway#getTimepointSize()
	 */
	public int getTimepointSize() throws DSOutOfServiceException,
			DSAccessException
	{	
		try
		{
			return service.getTimepointSize(); 
		}
		catch (Exception e)
		{
			ServiceUtilities.handleException(e, "RawPixelsStore.getTimepointSize");
		}
		return -1;
	}

	/* (non-Javadoc)
	 * @see blitzgateway.service.gateway.RawPixelsStoreGateway#getTotalSize()
	 */
	public int getTotalSize() throws DSOutOfServiceException, DSAccessException
	{	
		try
		{
			return service.getTotalSize(); 
		}
		catch (Exception e)
		{
			ServiceUtilities.handleException(e, "RawPixelsStore.getTotalSize");
		}
		return -1;
	}


	/* (non-Javadoc)
	 * @see blitzgateway.service.gateway.RawPixelsStoreGateway#isFloat()
	 */
	public boolean isFloat() throws DSOutOfServiceException, DSAccessException
	{
		try
		{
			return service.isFloat(); 
		}
		catch (Exception e)
		{
			ServiceUtilities.handleException(e, "RawPixelsStore.isFloat");
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see blitzgateway.service.gateway.RawPixelsStoreGateway#isSigned()
	 */
	public boolean isSigned() throws DSOutOfServiceException, DSAccessException
	{
		try
		{
			return service.isSigned(); 
		}
		catch (Exception e)
		{
			ServiceUtilities.handleException(e, "RawPixelsStore.isSigned");
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see blitzgateway.service.gateway.RawPixelsStoreGateway#setPlane()
	 */
	public void setPlane(byte[] buf, int z, int c, int t)
					throws DSOutOfServiceException, DSAccessException
	{
		try
		{
			service.setPlane(buf, z, c, t); 
		}
		catch (Exception e)
		{
			ServiceUtilities.handleException(e, "RawPixelsStore.setPlane");
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


