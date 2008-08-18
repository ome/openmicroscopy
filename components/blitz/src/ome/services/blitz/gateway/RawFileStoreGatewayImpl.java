/*
 * blitzgateway.service.gateway.RawFileStoreImpl 
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
import omero.api.RawFileStorePrx;


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
class RawFileStoreGatewayImpl
	implements RawFileStoreGateway
{
	
	/** The Blitz gateway. */
	private BlitzGateway blitzGateway;
	
	/** The fileId this fileStore refers. */
	private long 		 fileId;
	
	/** The Service. */
	private RawFileStorePrx service;
	
	/**
	 * Create the fileStore for the fileId and get the service from the gateway.
	 * @param fileId see above.
	 * @param gateway see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	RawFileStoreGatewayImpl(long fileId, BlitzGateway gateway) throws DSOutOfServiceException, DSAccessException
	{
		this.fileId = fileId;
		blitzGateway = gateway;
		service = createRawFileService(fileId);
	}
	
	/**
	 * Creates a new rawFile service for the specified fileId
	 * 
	 * @param fileId  The pixels set ID.
	 * @return See above.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occured while trying to 
	 * retrieve data from OMERO service. 
	 */
	private RawFileStorePrx createRawFileService(long fileId)
		throws DSOutOfServiceException, DSAccessException
	{
		try 
		{
			RawFileStorePrx service = blitzGateway.getRawFileService();
			service.setFileId(fileId);
			return service;
		} 
		catch (Throwable t) 
		{
			ServiceUtilities.handleException(t, "Cannot start the RawFileStore for fileId : "+ fileId);
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see blitzgateway.service.gateway.RawFileStoreGateway#exists()
	 */
	public boolean exists() throws DSOutOfServiceException, DSAccessException
	{
		try
		{
			return service.exists();
		}
		catch(Exception e)
		{
			ServiceUtilities.handleException(e,"RawFileStore.exists()");
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see blitzgateway.service.gateway.RawFileStoreGateway#read(long, int)
	 */
	public byte[] read(long position, int length)
			throws DSOutOfServiceException, DSAccessException
	{
		try
		{
			return service.read(position, length);
		}
		catch(Exception e)
		{
			ServiceUtilities.handleException(e,"RawFileStore.read("+position+","
																+length+")");
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see blitzgateway.service.gateway.RawFileStoreGateway#setFileId(long)
	 */
	public void setFileId(long fileId) throws DSOutOfServiceException,
			DSAccessException
	{
		try
		{
			service.setFileId(fileId);
		}
		catch(Exception e)
		{
			ServiceUtilities.handleException(e,"RawFileStore.setFileId("+
															fileId+")");
		}
	}

	/* (non-Javadoc)
	 * @see blitzgateway.service.gateway.RawFileStoreGateway#write(java.lang.Byte[], long, int)
	 */
	public void write(byte[] buf, long position, int length)
			throws DSOutOfServiceException, DSAccessException
	{
		try
		{
			service.write(buf, position, length);
		}
		catch(Exception e)
		{
			ServiceUtilities.handleException(e,"RawFileStore.write(bytes,"+
												position+","+length+")");
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


