/*
 * blitzgateway.service.stateful.ThumbnailServiceImpl 
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


//Java imports
import java.util.List;
import java.util.Map;

//Third-party libraries

//Application-internal dependencies
import ome.services.blitz.gateway.services.ThumbnailService;
import omero.RInt;
import omero.ServerError;
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
public class ThumbnailServiceImpl
	implements ThumbnailService
{	
	
	/** The thumbnail store. */
	private ThumbnailStorePrx	thumbnailStore;

	/**
	 * Create the ThumbnailService passing the gateway.
	 * @param ThumbnailStorePrx To generate new instances of the 
	 * ThumbnailStore.
	 * @throws omero.ServerError 
	 */
	public ThumbnailServiceImpl(ThumbnailStorePrx thumbnailStore) 
		throws omero.ServerError 
	{
		this.thumbnailStore = thumbnailStore; 
	}
	
	/**
	 * Lookup the pixels for the renderingEngine for pixelsId 
	 * @param pixelsId see above.
	 * @throws ServerError
	 */
	public void setPixelsId(long pixelsId) throws ServerError
	{
		thumbnailStore.setPixelsId(pixelsId);
	}
	
	/* (non-Javadoc)
	 * @see blitzgateway.service.stateful.ThumbnailService#getThumbnail(long, omero.RInt, omero.RInt)
	 */
	public synchronized byte[] getThumbnail(long pixelsId, RInt sizeX, RInt sizeY)
			throws omero.ServerError
	{
		return thumbnailStore.getThumbnail(sizeX, sizeY);
	}

	/* (non-Javadoc)
	 * @see blitzgateway.service.stateful.ThumbnailService#getThumbnailByLongestSide(long, omero.RInt)
	 */
	public synchronized byte[] getThumbnailByLongestSide(long pixelsId, RInt size)
			throws omero.ServerError
	{
		return thumbnailStore.getThumbnailByLongestSide(size);
	}

	/* (non-Javadoc)
	 * @see blitzgateway.service.stateful.ThumbnailService#getThumbnailByLongestSideSet(omero.RInt, java.util.List)
	 */
	public synchronized Map<Long, byte[]> getThumbnailByLongestSideSet(RInt size,
			List<Long> pixelsIds) throws omero.ServerError
	{
		return thumbnailStore.getThumbnailByLongestSideSet(size, pixelsIds);
	}

	/* (non-Javadoc)
	 * @see blitzgateway.service.stateful.ThumbnailService#getThumbnailSet(omero.RInt, omero.RInt, java.util.List)
	 */
	public synchronized Map<Long, byte[]> getThumbnailSet(RInt sizeX, RInt sizeY,
			List<Long> pixelsIds) throws omero.ServerError
	{
		return thumbnailStore.getThumbnailSet(sizeX, sizeY, pixelsIds);
	}

	/* (non-Javadoc)
	 * @see blitzgateway.service.stateful.ThumbnailService#setRenderingDefId(long, long)
	 */
	public synchronized void setRenderingDefId(long pixelsId, long renderingDefId)
			throws omero.ServerError
	{
		thumbnailStore.setRenderingDefId(renderingDefId);
	}
}


