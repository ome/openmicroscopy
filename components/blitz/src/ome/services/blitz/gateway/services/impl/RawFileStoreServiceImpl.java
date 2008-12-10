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
package ome.services.blitz.gateway.services.impl;


import ome.services.blitz.gateway.services.RawFileStoreService;
import omero.ServerError;
import omero.api.RawFileStorePrx;


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
	private RawFileStorePrx 	rawFileStorePrx;
	
	/**
	 * set the service to the file. 
	 * @param fileId see above.
	 * @throws ServerError
	 */
	private void setFileId(long fileId) throws ServerError
	{
		rawFileStorePrx.setFileId(fileId);
	}
	
	/**
	 * Create the ImageService passing the gateway.
	 * @param gatewayFactory To generate new instances of the 
	 * RenderingEngineGateway.
	 */
	public RawFileStoreServiceImpl(RawFileStorePrx rawFileStorePrx) 
	{
		this.rawFileStorePrx = rawFileStorePrx;
	}


	/* (non-Javadoc)
	 * @see blitzgateway.service.RawFileStoreService#exists(long)
	 */
	public synchronized boolean exists(long fileId) throws omero.ServerError
	{
		setFileId(fileId);
		return rawFileStorePrx.exists();
	}

	/* (non-Javadoc)
	 * @see blitzgateway.service.RawFileStoreService#read(long, long, int)
	 */
	public synchronized byte[] read(long fileId, long position, int length)
			throws omero.ServerError
	{
		setFileId(fileId);
		return rawFileStorePrx.read(position, length);
	}

	/* (non-Javadoc)
	 * @see blitzgateway.service.RawFileStoreService#write(long, byte[], long, int)
	 */
	public synchronized void write(long fileId, byte[] buf, long position, int length)
			throws omero.ServerError
	{
		setFileId(fileId);
		rawFileStorePrx.write(buf, position, length);
	}

}


