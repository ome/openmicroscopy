/*
 * blitzgateway.service.ImageService 
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

import java.util.List;
import java.util.Map;

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
	 * Keep service alive.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public void keepAlive() throws ServerError;

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
	 * Get the zSection stack from the pixels at timepoint t
	 * @param pixelsId The pixelsId from the imageStack.
	 * @param c The channel.
	 * @param t The time-point.
	 * @return see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public double[][][] getPlaneStack(long pixelsId, int c, int t)
			throws ServerError;
	
	/**
	 * Get the plane from the server with id pixelsId, and channels, c, timepoint
	 * t, and z-section z. This is the plane converted to doubles.
	 * 
	 * @param pixelsId see above.
	 * @param z see above.
	 * @param c see above.
	 * @param t see above.
	 * @return see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public double[][] getPlane(long pixelsId, int z, int c, int t) 
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
	 * Test method to make sure the client converts it's data to the server data
	 * correctly. 
	 * @param pixelsId pixels id to upload to .  
	 * @param c channel.
	 * @param t time point.
	 * @param z z section.
	 * @return the converted data. 
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException */
	public double[][] testVal(long pixelsId, int z, int c, int t) 
	throws ServerError;
	
	/**
	 * convert the client data pixels to server byte array, also sets the data
	 * pixel size to the size of the pixels in the pixels Id param.
	 * @param pixels the pixels in the server.
	 * @param data the data on the client. 
	 * @return the bytes for server.
	 */
	public byte[] convertClientToServer(Pixels pixels, double [][] data);
	
	
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
			double [][] data) throws ServerError;
	
	/**
	 * Update the pixels object in the server.
	 * @param object see above.
	 * @return The newly updated object.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public Pixels updatePixels(Pixels object) 
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
	public BufferedImage getRenderedImage(long pixelsId, int z, int t)	throws ServerError;

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
	 * Render as a packedInt 
	 * @param pixelsId pixels id of the plane to render
	 * @param z z section to render
	 * @param t timepoint to render
	 * @return packed int
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public int[] renderAsPackedInt(Long pixelsId, int z, int t) throws ServerError;
	
	/**
	 * Set the active channels in the pixels.
	 * @param pixelsId the pixels id.
	 * @param w the channel
	 * @param active set active?
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	void setActive(Long pixelsId, int w, boolean active) throws  ServerError;

	/**
	 * Is the channel active.
	 * @param pixelsId the pixels id.
	 * @param w channel
	 * @return true if the channel active.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	boolean isActive(Long pixelsId, int w) throws  ServerError;

	/**
	 * Get the default Z section of the image
	 * @param pixelsId the pixelsId of the image.
	 * @return see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	int getDefaultZ(Long pixelsId) throws  ServerError;
	
	/**
	 * Get the default T point of the image
	 * @param pixelsId the pixelsId of the image.
	 * @return see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	int getDefaultT(Long pixelsId) throws  ServerError;
	
	/**
	 * Set the default Z section of the image.
	 * @param pixelsId the pixelsId of the image.
	 * @param z see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	void setDefaultZ(Long pixelsId, int z) throws  ServerError;
	
	/**
	 * Set the default timepoint of the image.
	 * @param pixelsId the pixelsId of the image.
	 * @param t see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	void setDefaultT(Long pixelsId, int t) throws  ServerError;
	
	/**
	 * Get the pixels of the Rendering engine.
	 * @param pixelsId the pixelsId of the image.
	 * @return see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	Pixels getPixels(Long pixelsId) throws  ServerError;
	
	/**
	 * Set the channel min, max.
	 * @param pixelsId the pixelsId of the image.
	 * @param w channel.
	 * @param start min.
	 * @param end max.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	void setChannelWindow(Long pixelsId, int w, double start, double end) throws  ServerError;
	
	/**
	 * Get the channel min.
	 * @param pixelsId the pixelsId of the image.
	 * @param w channel.
	 * @return see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	double getChannelWindowStart(Long pixelsId, int w) throws  ServerError;
	
	/**
	 * Get the channel max.
	 * @param pixelsId the pixelsId of the image.
	 * @param w channel.
	 * @return see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	double getChannelWindowEnd(Long pixelsId, int w) throws  ServerError;
	
	/**
	 * Set the rendering def from the default to another.
	 * @param pixelsId for pixelsId 
	 * @param renderingDefId see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	void setRenderingDefId(long pixelsId, long renderingDefId) throws  ServerError;
	
	/**
	 * Get the thumbnail of the image.
	 * @param pixelsId for pixelsId 
	 * @param sizeX size of thumbnail.
	 * @param sizeY size of thumbnail.
	 * @return see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	byte[] getThumbnail(long pixelsId, omero.RInt sizeX, omero.RInt sizeY) throws  ServerError;
	
	/**
	 * Get a set of thumbnails.
	 * @param sizeX size of thumbnail.
	 * @param sizeY size of thumbnail.
	 * @param pixelsIds list of ids.
	 * @return see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	Map<Long, byte[]>getThumbnailSet(omero.RInt sizeX, omero.RInt sizeY, List<Long> pixelsIds) throws  ServerError;
	
	/**
	 * Get a set of thumbnails, maintaining aspect ratio.
	 * @param size size of thumbnail.
	 * @param pixelsIds list of ids.
	 * @return see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	Map<Long, byte[]>getThumbnailByLongestSideSet(omero.RInt size, List<Long> pixelsIds) throws  ServerError;
	
	/**
	 * Get the thumbnail of the image, maintain aspect ratio.
	 * @param pixelsId for pixelsId 
	 * @param size size of thumbnail.
	 * @return see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	byte[] getThumbnailByLongestSide(long pixelsId, omero.RInt size) throws  ServerError;
	

}


