/*
 * blitzgateway.service.gateway.ThunbnailServiceGateway 
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

import omero.gateway.DSAccessException;
import omero.gateway.DSOutOfServiceException;

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
public interface ThumbnailGateway
{		
	/**
	 * Keep the thumbnail service alive.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	void keepAlive() throws  DSOutOfServiceException, DSAccessException;
	
	/**
	 * Set the rendering def from the default to antoher.
	 * @param renderingDefId see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	void setRenderingDefId(long renderingDefId) throws  DSOutOfServiceException, DSAccessException;
	
	/**
	 * Get the thumbnail of the image.
	 * @param sizeX size of thumbnail.
	 * @param sizeY size of thumbnail.
	 * @return see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	byte[] getThumbnail(omero.RInt sizeX, omero.RInt sizeY) throws  DSOutOfServiceException, DSAccessException;
	
	/**
	 * Get a set of thumbnails.
	 * @param sizeX size of thumbnail.
	 * @param sizeY size of thumbnail.
	 * @param pixelsIds list of ids.
	 * @return see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	Map<Long, byte[]>getThumbnailSet(omero.RInt sizeX, omero.RInt sizeY, List<Long> pixelsIds) throws  DSOutOfServiceException, DSAccessException;
	
	/**
	 * Get a set of thumbnails, maintaining aspect ratio.
	 * @param size size of thumbnail.
	 * @param pixelsIds list of ids.
	 * @return see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	Map<Long, byte[]>getThumbnailByLongestSideSet(omero.RInt size, List<Long> pixelsIds) throws  DSOutOfServiceException, DSAccessException;
	
	/**
	 * Get the thumbnail of the image, maintain aspect ratio.
	 * @param size size of thumbnail.
	 * @return see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	byte[] getThumbnailByLongestSide(omero.RInt size) throws  DSOutOfServiceException, DSAccessException;
	
	
	/*
	 * 
	 * ThunbnailServiceGateway java to ICE Mappings from the API.ice slice definition.
	 * Below are all the calls in the ThunbnailServiceGateway service. 
	 * As the are created in the ThunbnailServiceGateway they will be marked as 
	 * done.
	 * 
	 * 
	 * 
	 *
DONE	boolean setPixelsId(long pixelsId) throws  DSOutOfServiceException, DSAccessException;
DONE	void setRenderingDefId(long renderingDefId) throws  DSOutOfServiceException, DSAccessException;
DONE	byte[] getThumbnail(omero.RInt sizeX, omero.RInt sizeY) throws  DSOutOfServiceException, DSAccessException;
DONE	Map<long, byte[]>getThumbnailSet(omero.RInt sizeX, omero.RInt sizeY, List<Long> pixelsIds) throws  DSOutOfServiceException, DSAccessException;
DONE	Map<long, byte[]>getThumbnailByLongestSideSet(omero.RInt size, List<Long> pixelsIds) throws  DSOutOfServiceException, DSAccessException;
DONE	byte[] getThumbnailByLongestSide(omero.RInt size) throws  DSOutOfServiceException, DSAccessException;
		byte[] getThumbnailByLongestSideDirect(omero.RInt size) throws  DSOutOfServiceException, DSAccessException;
		byte[] getThumbnailDirect(omero.RInt sizeX, omero.RInt sizeY) throws  DSOutOfServiceException, DSAccessException;
		byte[] getThumbnailForSectionDirect(int theZ, int theT, omero.RInt sizeX, omero.RInt sizeY) throws  DSOutOfServiceException, DSAccessException;
		byte[] getThumbnailForSectionByLongestSideDirect(int theZ, int theT, omero.RInt size) throws  DSOutOfServiceException, DSAccessException;
		void createThumbnails() throws  DSOutOfServiceException, DSAccessException;
		void createThumbnail(omero.RInt sizeX, omero.RInt sizeY) throws  DSOutOfServiceException, DSAccessException;
		boolean thumbnailExists(omero.RInt sizeX, omero.RInt sizeY) throws  DSOutOfServiceException, DSAccessException;
		void resetDefaults() throws  DSOutOfServiceException, DSAccessException;
	*/
}


