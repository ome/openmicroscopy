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
package ome.services.blitz.gateway.services.impl;


import ome.services.blitz.gateway.services.RawPixelsStoreService;
import omero.api.RawPixelsStorePrx;

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
	/** RawPixelsStore proxy. */
	private RawPixelsStorePrx 		rawPixelsStore;
	
	/**
	 * Create the ImageService passing the gateway.
	 * @param gatewayFactory To generate new instances of the 
	 * RenderingEngineGateway.
	 */
	public RawPixelsStoreServiceImpl(RawPixelsStorePrx rawPixelsStore) 
	{
		this.rawPixelsStore = rawPixelsStore;
	}

	private void setPixelsId(long pixelsId, boolean bypassOriginalFile) throws omero.ServerError
	{
		rawPixelsStore.setPixelsId(pixelsId, bypassOriginalFile);
	}
	
	/* (non-Javadoc)
	 * @see blitzgateway.service.RawPixelsStoreService#getByteWidth(long)
	 */
	public synchronized int getByteWidth(long pixelsId) throws 
			omero.ServerError
	{
		setPixelsId(pixelsId, false);
		return rawPixelsStore.getByteWidth();
	}

	/* (non-Javadoc)
	 * @see blitzgateway.service.RawPixelsStoreService#getPlane(long, int, int, int)
	 */
	public synchronized byte[] getPlane(long pixelsId, int z, int c, int t)
			throws omero.ServerError
	{
		setPixelsId(pixelsId, false);
		return rawPixelsStore.getPlane(z, c, t);
	}

	/* (non-Javadoc)
	 * @see blitzgateway.service.RawPixelsStoreService#getPlaneSize(long)
	 */
	public synchronized long getPlaneSize(long pixelsId) throws 
			omero.ServerError
	{
		setPixelsId(pixelsId, false);
		return rawPixelsStore.getPlaneSize();
	}

	/* (non-Javadoc)
	 * @see blitzgateway.service.RawPixelsStoreService#getRowSize(long)
	 */
	public synchronized int getRowSize(long pixelsId) throws 
			omero.ServerError
	{
		setPixelsId(pixelsId, false);
		return rawPixelsStore.getRowSize();
	}

	/* (non-Javadoc)
	 * @see blitzgateway.service.RawPixelsStoreService#getStackSize(long)
	 */
	public synchronized long getStackSize(long pixelsId) throws 
			omero.ServerError
	{
		setPixelsId(pixelsId, false);
		return rawPixelsStore.getStackSize();
	}

	/* (non-Javadoc)
	 * @see blitzgateway.service.RawPixelsStoreService#getTimepointSize(long)
	 */
	public synchronized long getTimepointSize(long pixelsId) throws 
			omero.ServerError
	{
		setPixelsId(pixelsId, false);
		return rawPixelsStore.getTimepointSize();
	}

	/* (non-Javadoc)
	 * @see blitzgateway.service.RawPixelsStoreService#getTotalSize(long)
	 */
	public synchronized long getTotalSize(long pixelsId) throws 
			omero.ServerError
	{
		setPixelsId(pixelsId, false);
		return rawPixelsStore.getTotalSize();
	}

	/* (non-Javadoc)
	 * @see blitzgateway.service.RawPixelsStoreService#isFloat(long)
	 */
	public synchronized boolean isFloat(long pixelsId) throws 
			omero.ServerError
	{
		setPixelsId(pixelsId, false);
		return rawPixelsStore.isFloat();
	}

	/* (non-Javadoc)
	 * @see blitzgateway.service.RawPixelsStoreService#isSigned(long)
	 */
	public synchronized boolean isSigned(long pixelsId) throws 
			omero.ServerError
	{
		setPixelsId(pixelsId, false);
		return rawPixelsStore.isSigned();
	}

	/* (non-Javadoc)
	 * @see blitzgateway.service.RawPixelsStoreService#setPlane(long, byte[], int, int, int)
	 */
	public synchronized void setPlane(long pixelsId, byte[] buf, int z, int c, int t)
			throws omero.ServerError
	{
		setPixelsId(pixelsId, false);
		rawPixelsStore.setPlane(buf, z, c, t);		
	}

}


