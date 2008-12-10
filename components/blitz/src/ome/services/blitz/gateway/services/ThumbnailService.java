/*
 * blitzgateway.service.stateful.ThumbnailService 
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

import java.util.List;
import java.util.Map;

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
public interface ThumbnailService
{	
	
	/**
	 * Set the rendering def from the default to another.
	 * @param pixelsId for pixelsId 
	 * @param renderingDefId see above.
	 * @throws DSOutOfServiceException
	 * @throwsomero.ServerError
	 */
	void setRenderingDefId(long pixelsId, long renderingDefId) throws omero.ServerError;
	
	/**
	 * Get the thumbnail of the image.
	 * @param pixelsId for pixelsId 
	 * @param sizeX size of thumbnail.
	 * @param sizeY size of thumbnail.
	 * @return see above.
	 * @throws DSOutOfServiceException
	 * @throwsomero.ServerError
	 */
	byte[] getThumbnail(long pixelsId, omero.RInt sizeX, omero.RInt sizeY) throws omero.ServerError;
	
	/**
	 * Get a set of thumbnails.
	 * @param sizeX size of thumbnail.
	 * @param sizeY size of thumbnail.
	 * @param pixelsIds list of ids.
	 * @return see above.
	 * @throws DSOutOfServiceException
	 * @throwsomero.ServerError
	 */
	Map<Long, byte[]>getThumbnailSet(omero.RInt sizeX, omero.RInt sizeY, List<Long> pixelsIds) throws omero.ServerError;
	
	/**
	 * Get a set of thumbnails, maintaining aspect ratio.
	 * @param size size of thumbnail.
	 * @param pixelsIds list of ids.
	 * @return see above.
	 * @throws DSOutOfServiceException
	 * @throwsomero.ServerError
	 */
	Map<Long, byte[]>getThumbnailByLongestSideSet(omero.RInt size, List<Long> pixelsIds) throws omero.ServerError;
	
	/**
	 * Get the thumbnail of the image, maintain aspect ratio.
	 * @param pixelsId for pixelsId 
	 * @param size size of thumbnail.
	 * @return see above.
	 * @throws DSOutOfServiceException
	 * @throwsomero.ServerError
	 */
	byte[] getThumbnailByLongestSide(long pixelsId, omero.RInt size) throws omero.ServerError;
	
}


