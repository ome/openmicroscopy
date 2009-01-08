/*
 * ome.services.blitz.omerogateway.services.ImageService 
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
package ome.services.blitz.gateway.services;


//Java imports
import java.util.List;
import java.util.Map;

//Third-party libraries

//Application-internal dependencies
import omero.ServerError;
import omero.api.BufferedImage;
import omero.model.Image;
import omero.model.Pixels;

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
public interface ImageService
{
	 /**
	  * Create a new image of specified X,Y, Z, T and channels plus pixelsType
	  * with name and description 
	  * 
	  * @param sizeX
	  * @param sizeY
	  * @param sizeZ
	  * @param sizeT
	  * @param channelList
	  * @param pixelsType
	  * @param name
	  * @param description
	  * @return new image id.
	  * @throws DSOutOfServiceException
	  * @throws DSAccessException
	  */
	 Long createImage(int sizeX, int sizeY, int sizeZ, int sizeT,
           List<Integer> channelList, 
           omero.model.PixelsType pixelsType,
           String name, String description) 	throws  ServerError;
	 
	/**
	 * Copy the pixels set from pixels to a new set.
	 * @param pixelsID pixels id to copy.
	 * @param x width of plane.
	 * @param y height of plane.
	 * @param t num timepoints
	 * @param z num zsections.
	 * @param channelList the list of channels to copy.
	 * @param methodology what created the pixels.
	 * @return new id.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public Long copyPixels(long pixelsID, int x, int y, int t, int z, 
			List<Integer> channelList, String methodology) 
		throws ServerError;
		
		
	/**
	 * Copy the pixels set from pixels to a new set.
	 * @param pixelsID pixels id to copy.
	 * @param channelList the list of channels to copy.
	 * @param methodology what created the pixels.
	 * @return new id.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public Long copyPixels(long pixelsID,
			List<Integer> channelList, String methodology) 
	throws ServerError;
	
	/**
	 * Copy the image and pixels from image.
	 * @param imageId image id to copy.
	 * @param x width of plane.
	 * @param y height of plane.
	 * @param t number of timepoints.
	 * @param z number of zsections.
	 * @param channelList the list of channels to copy.
	 * @param methodology what created the pixels.
	 * @return new id.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public Long copyImage(long imageId, int x, int y, int t, int z, 
			List<Integer> channelList, String methodology) 
	throws ServerError;
	
	/**
	 * Get the raw plane from the server with id pixelsId, and channels, c, timepoint
	 * t, and z-section z. This is the plane as bytes, not converted to doubles.
	 * 
	 * @param pixelsId see above.
	 * @param c see above.
	 * @param t see above.
	 * @param z see above.
	 * @return see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public byte[] getRawPlane(long pixelsId, int z, int c, int t) 
	throws ServerError;
	
	/**
	 * Get the pixels information for an image.
	 * @param imageID image id relating to the pixels.
	 * @return see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public Pixels getPixels(long imageID)
	throws ServerError;
	
	/**
	 * Get the image information for an image.
	 * @param imageID image id relating to the iamge.
	 * @return see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public Image getImage(long imageID)
	throws ServerError;
	
	/**
	 * Render image as Buffered image. 
	 * @param pixelsId pixels id of the plane to render
	 * @param z z section to render
	 * @param t timepoint to render
	 * @return packed int
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public int[] getRenderedImage(long pixelsId, int z, int t)	throws ServerError;

	/**
	 * Render image as 3d matrix. 
	 * @param pixelsId pixels id of the plane to render
	 * @param z z section to render
	 * @param t timepoint to render
	 * @return packed int
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public int[][][] getRenderedImageMatrix(long pixelsId, int z, int t)	throws ServerError;
	
	/**
	 * Set the active channels in the pixels.
	 * @param pixelsId the pixels id.
	 * @param w the channel
	 * @param active set active?
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public void setActive(Long pixelsId, int w, boolean active) throws  ServerError;
	
	/**
	 * Get the thumbnail of the image.
	 * @param pixelsId for pixelsId 
	 * @param sizeX size of thumbnail.
	 * @param sizeY size of thumbnail.
	 * @return see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public byte[] getThumbnail(long pixelsId, omero.RInt sizeX, omero.RInt sizeY) throws  ServerError;
	
	/**
	 * Get a set of thumbnails.
	 * @param sizeX size of thumbnail.
	 * @param sizeY size of thumbnail.
	 * @param pixelsIds list of ids.
	 * @return see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public Map<Long, byte[]>getThumbnailSet(omero.RInt sizeX, omero.RInt sizeY, List<Long> pixelsIds) throws  ServerError;
	
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
	 * @throws DSAccessException
	 */
	public void uploadPlane(long pixelsId, int z, int c, int t, 
			byte [] data) throws ServerError;

	
}


