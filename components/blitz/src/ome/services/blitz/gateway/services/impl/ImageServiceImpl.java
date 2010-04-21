/*
 * ome.services.blitz.omerogateway.services.impl.ImageServiceImpl 
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

import ome.services.blitz.gateway.services.GatewayFactory;
import ome.services.blitz.gateway.services.ImageService;
import ome.services.blitz.gateway.services.RawPixelsStoreService;
import ome.services.blitz.gateway.services.RenderingService;
import ome.services.blitz.gateway.services.ThumbnailService;
import omero.RInt;
import omero.ServerError;
import omero.api.IPixelsPrx;
import omero.api.IQueryPrx;
import omero.model.Image;
import omero.model.Pixels;
import omero.model.PixelsType;

import static omero.rtypes.rint;

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
public class ImageServiceImpl
	implements ImageService 
{	
	
	GatewayFactory 				gatewayFactory;
	
	/**
	 * Instantiate the imageService with the serviceFactory.
	 * @param serviceFactory see above.
	 */
	public ImageServiceImpl(GatewayFactory gatewayFactory)
	{
		this.gatewayFactory = gatewayFactory;
	}
	
	/* (non-Javadoc)
	 * @see blitzgateway.service.ImageService#createImage(int, int, int, int, java.util.List, omero.model.PixelsType, java.lang.String, java.lang.String)
	 */
	public Long createImage(int sizeX, int sizeY, int sizeZ, int sizeT,
			List<Integer> channelList, PixelsType pixelsType, String name,
			String description) throws omero.ServerError
	{
		IPixelsPrx iPixels = gatewayFactory.getIPixels();
		return iPixels.createImage(sizeX, sizeY, sizeZ, sizeT, channelList, pixelsType, name, description).getValue();
	}
	

	/* (non-Javadoc)
	 * @see blitzgateway.service.ImageService#copyPixels(long, int, int, int, int, java.lang.String)
	 */
	public Long copyPixels(long pixelsID, int x, int y, int t, int z, List<Integer> channelList,
			String methodology) throws omero.ServerError
	{
		IPixelsPrx iPixels = gatewayFactory.getIPixels();
		Long newID = iPixels.copyAndResizePixels
						(pixelsID, rint(x), rint(y), rint(z), rint(t), channelList, methodology, true).getValue();
		return newID;
	}
	
	/* (non-Javadoc)
	 * @see blitzgateway.service.ImageService#copyPixels(long, java.lang.String)
	 */
	public Long copyPixels(long pixelsID, List<Integer> channelList,
			String imageName) throws omero.ServerError
	{
		IPixelsPrx iPixels = gatewayFactory.getIPixels();
		Pixels pixels = getPixels(pixelsID);
		Long newID = iPixels.copyAndResizePixels
						(pixelsID, pixels.getSizeX(), pixels.getSizeY(), 
						 pixels.getSizeZ(),pixels.getSizeT(),
						 channelList, imageName, true).getValue();
		return newID;
	}

	/* (non-Javadoc)
	 * @see blitzgateway.service.ImageService#copyImage(long, int, int, int, int, java.lang.String)
	 */
	public Long copyImage(long imageId, int x, int y, int t, int z, List<Integer> channelList,
			String imageName) throws omero.ServerError
	{
		IPixelsPrx iPixels = gatewayFactory.getIPixels();
		Long newID = iPixels.copyAndResizeImage
						(imageId, rint(x), rint(y), rint(z), rint(t), channelList, imageName, true).getValue();
		return newID;
	}
	
	/* (non-Javadoc)
	 * @see ome.services.blitz.omerogateway.services.ImageService#getImage(long)
	 */
	public Image getImage(long imageID) throws ServerError
	{
		String queryStr = new String("from Image as i left outer join fetch " +
				"i.pixels as p where i.id= "+ imageID);
		IQueryPrx query = gatewayFactory.getIQuery();
		return (Image)query.findByQuery(queryStr, null);
	}

	/* (non-Javadoc)
	 * @see ome.services.blitz.omerogateway.services.ImageService#getPixels(long)
	 */
	public Pixels getPixels(long pixelsId) throws ServerError
	{
		RenderingService renderingService = gatewayFactory.getRenderingService();
		return renderingService.getPixels(pixelsId);
	}

	/* (non-Javadoc)
	 * @see ome.services.blitz.omerogateway.services.ImageService#getRawPlane(long, int, int, int)
	 */
	public byte[] getRawPlane(long pixelsId, int z, int c, int t)
			throws ServerError
	{
		RawPixelsStoreService rawPixelsStoreService = gatewayFactory.getRawPixelsStoreService();
		return rawPixelsStoreService.getPlane(pixelsId, z, c, t);
	}

	/* (non-Javadoc)
	 * @see ome.services.blitz.omerogateway.services.ImageService#getRenderedImage(long, int, int)
	 */
	public int[] getRenderedImage(long pixelsId, int z, int t)
			throws ServerError
	{
		RenderingService renderingService = gatewayFactory.getRenderingService();
		return renderingService.getRenderedImage(pixelsId, z, t);
	}		
	
	/* (non-Javadoc)
	 * @see ome.services.blitz.omerogateway.services.ImageService#getRenderedImage(long, int, int)
	 */
	public int[] renderAsPackedIntAsRGBA(long pixelsId, int z, int t)
			throws ServerError
	{
		RenderingService renderingService = gatewayFactory.getRenderingService();
		return renderingService.renderAsPackedIntAsRGBA(pixelsId, z, t);
	}	
	
	/* (non-Javadoc)
	 * @see blitzgateway.service.ImageService#getRenderedImageMatrix(long, int, int)
	 */
	public int[][][] getRenderedImageMatrix(long pixelsId, int z, int t)
			throws omero.ServerError
	{
		RenderingService renderingService = gatewayFactory.getRenderingService();
		return renderingService.getRenderedImageMatrix(pixelsId, z, t);
	}

	/* (non-Javadoc)
	 * @see ome.services.blitz.omerogateway.services.ImageService#getThumbnail(long, omero.RInt, omero.RInt)
	 */
	public byte[] getThumbnail(long pixelsId, RInt sizeX, RInt sizeY)
			throws ServerError
	{
		ThumbnailService thumbnailService = gatewayFactory.getThumbnailStoreService();
		return thumbnailService.getThumbnail(pixelsId, sizeX, sizeY);
	}

	/* (non-Javadoc)
	 * @see ome.services.blitz.omerogateway.services.ImageService#getThumbnailSet(omero.RInt, omero.RInt, java.util.List)
	 */
	public Map<Long, byte[]> getThumbnailSet(RInt sizeX, RInt sizeY,
			List<Long> pixelsIds) throws ServerError
	{
		ThumbnailService thumbnailService = gatewayFactory.getThumbnailStoreService();
		return thumbnailService.getThumbnailSet(sizeX, sizeY, pixelsIds);
	}

	/* (non-Javadoc)
	 * @see ome.services.blitz.omerogateway.services.ImageService#setActive(java.lang.Long, int, boolean)
	 */
	public void setActive(Long pixelsId, int w, boolean active)
			throws ServerError
	{
		RenderingService renderingService = gatewayFactory.getRenderingService();
		renderingService.setActive(pixelsId, w, active);
	}
	
	/**
	 * Upload the plane to the server, on pixels id with channel and the 
	 * time, + z section. the data is the client 2d data values. This will
	 * be converted to the raw server bytes.
	 * @param pixelsId pixels id to upload to .  
	 * @param z z section. 
	 * @param c channel.
	 * @param t time point.
	 * @param data plane data. 
	 * @throws DSOutOfServiceException
	 * @throws omero.ServerError
	 */
	public void uploadPlane(long pixelsId, int z, int c, int t, 
			byte[] data) throws omero.ServerError
	{
		RawPixelsStoreService rawPixelsStore = gatewayFactory.getRawPixelsStoreService();
		rawPixelsStore.setPlane(pixelsId, data, z, c, t);
	}

	
}


