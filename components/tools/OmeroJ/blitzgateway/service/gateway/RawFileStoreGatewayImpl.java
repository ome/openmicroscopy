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
package blitzgateway.service.gateway;

import omero.api.RawFileStorePrx;

import org.openmicroscopy.shoola.env.data.DSAccessException;
import org.openmicroscopy.shoola.env.data.DSOutOfServiceException;

import blitzgateway.util.ServiceUtilities;

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
class RawFileStoreGatewayImpl
	implements RawFileStoreGateway
{
	
	private BlitzGateway blitzGateway;
	
	RawFileStoreGatewayImpl(BlitzGateway gateway)
	{
		blitzGateway = gateway;
	}
	
	/* (non-Javadoc)
	 * @see blitzgateway.service.gateway.RawFileStoreGateway#exists()
	 */
	public boolean exists() throws DSOutOfServiceException, DSAccessException
	{
		RawFileStorePrx rawFileStore = blitzGateway.getRawFileService();
		try
		{
			return rawFileStore.exists();
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
		RawFileStorePrx rawFileStore = blitzGateway.getRawFileService();
		try
		{
			return rawFileStore.read(position, length);
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
		RawFileStorePrx rawFileStore = blitzGateway.getRawFileService();
		try
		{
			rawFileStore.setFileId(fileId);
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
		RawFileStorePrx rawFileStore = blitzGateway.getRawFileService();
		try
		{
			rawFileStore.write(buf, position, length);
		}
		catch(Exception e)
		{
			ServiceUtilities.handleException(e,"RawFileStore.write(bytes,"+
												position+","+length+")");
		}
	}


}


