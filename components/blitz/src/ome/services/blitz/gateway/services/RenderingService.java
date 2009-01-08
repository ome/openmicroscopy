/*
 * blitzgateway.service.gateway.RenderingService 
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

import omero.api.BufferedImage;
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
public interface RenderingService
{	

	/**
	 * Render image as Buffered image. 
	 * @param pixelsId pixels id of the plane to render
	 * @param z z section to render
	 * @param t timepoint to render
	 * @return packed int
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public int[] getRenderedImage(long pixelsId, int z, int t)	throws omero.ServerError;

	/**
	 * Render image as 3d matrix. 
	 * @param pixelsId pixels id of the plane to render
	 * @param z z section to render
	 * @param t timepoint to render
	 * @return packed int
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public int[][][] getRenderedImageMatrix(long pixelsId, int z, int t)	throws omero.ServerError;

	
	/**
	 * Render as a packedInt 
	 * @param pixelsId pixels id of the plane to render
	 * @param z z section to render
	 * @param t timepoint to render
	 * @return packed int
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public int[] renderAsPackedInt(Long pixelsId, int z, int t) throws omero.ServerError;
	
	/**
	 * Set the active channels in the pixels.
	 * @param pixelsId the pixels id.
	 * @param w the channel
	 * @param active set active?
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	void setActive(Long pixelsId, int w, boolean active) throws  omero.ServerError;

	/**
	 * Is the channel active.
	 * @param pixelsId the pixels id.
	 * @param w channel
	 * @return true if the channel active.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	boolean isActive(Long pixelsId, int w) throws  omero.ServerError;

	/**
	 * Get the default Z section of the image
	 * @param pixelsId the pixelsId of the image.
	 * @return see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	int getDefaultZ(Long pixelsId) throws  omero.ServerError;
	
	/**
	 * Get the default T point of the image
	 * @param pixelsId the pixelsId of the image.
	 * @return see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	int getDefaultT(Long pixelsId) throws  omero.ServerError;
	
	/**
	 * Set the default Z section of the image.
	 * @param pixelsId the pixelsId of the image.
	 * @param z see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	void setDefaultZ(Long pixelsId, int z) throws  omero.ServerError;
	
	/**
	 * Set the default timepoint of the image.
	 * @param pixelsId the pixelsId of the image.
	 * @param t see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	void setDefaultT(Long pixelsId, int t) throws  omero.ServerError;
	
	/**
	 * Get the pixels of the Rendering engine.
	 * @param pixelsId the pixelsId of the image.
	 * @return see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	Pixels getPixels(Long pixelsId) throws  omero.ServerError;
	
	/**
	 * Set the channel min, max.
	 * @param pixelsId the pixelsId of the image.
	 * @param w channel.
	 * @param start min.
	 * @param end max.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	void setChannelWindow(Long pixelsId, int w, double start, double end) throws  omero.ServerError;
	
	/**
	 * Get the channel min.
	 * @param pixelsId the pixelsId of the image.
	 * @param w channel.
	 * @return see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	double getChannelWindowStart(Long pixelsId, int w) throws  omero.ServerError;
	
	/**
	 * Get the channel max.
	 * @param pixelsId the pixelsId of the image.
	 * @param w channel.
	 * @return see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	double getChannelWindowEnd(Long pixelsId, int w) throws  omero.ServerError;
}


