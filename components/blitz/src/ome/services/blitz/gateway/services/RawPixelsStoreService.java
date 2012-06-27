/*
 * blitzgateway.service.RawPixelsStoreService 
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
public interface RawPixelsStoreService
{	
	
	/**
	 * Get the plane size of the current image.
	 * @param pixelsId the pixelsId of the image.
	 * @return see above.
	 * @throws DSOutOfServiceException
	 * @throwsomero.ServerError
	 */
	long getPlaneSize(long pixelsId) throws omero.ServerError;

	/**
	 * Get the total size of the current image.
	 * @param pixelsId the pixelsId of the image.
	 * @return see above.
	 * @throws DSOutOfServiceException
	 * @throwsomero.ServerError
	 */
	long getTotalSize(long pixelsId) throws omero.ServerError;
	
	/**
	 * Get the plane size of the current image.
	 * @param pixelsId the pixelsId of the image.
	 * @return see above.
	 * @throws DSOutOfServiceException
	 * @throwsomero.ServerError
	 */
	int getRowSize(long pixelsId) throws omero.ServerError;
	
	/**
	 * Get the stack size of the current image.
	 * @param pixelsId the pixelsId of the image.
	 * @return see above.
	 * @throws DSOutOfServiceException
	 * @throwsomero.ServerError
	 */
	long getStackSize(long pixelsId) throws omero.ServerError;
	
	/**
	 * Get the timepoint size of the current image.
	 * @param pixelsId the pixelsId of the image.
	 * @return see above.
	 * @throws DSOutOfServiceException
	 * @throwsomero.ServerError
	 */
	long getTimepointSize(long pixelsId) throws omero.ServerError;
	
	/**
	 * get bytes to the plane c, t, z
	 * @param pixelsId the pixelsId of the image.
	 * @param z see above.
	 * @param c see above.
	 * @param t the bytes.
	 * @return see above.
	 * @throws DSOutOfServiceException
	 * @throwsomero.ServerError
	 */
	public byte[] getPlane(long pixelsId, int z, int c, int t) 
			throws omero.ServerError;
	
	/**
	 * set the plane at z, c, t to the bytes of buf.
	 * @param pixelsId the pixelsId of the image.
	 * @param buf bytes of the plane.
	 * @param z z section
	 * @param c channel
	 * @param t timepoint
	 * @throws DSOutOfServiceException
	 * @throwsomero.ServerError
	 */
	void setPlane(long pixelsId, byte[] buf, int z, int c, int t) throws omero.ServerError;
	
	/**
	 * Get the bytewidth of the image
	 * @param pixelsId the pixelsId of the image.
	 * @return see above.
	 * @throws DSOutOfServiceException
	 * @throwsomero.ServerError
	 */
	int getByteWidth(long pixelsId) throws omero.ServerError;
	
	/**
	 * Are the bytes in the image signed.
	 * @param pixelsId the pixelsId of the image.
	 * @return see above.
	 * @throws DSOutOfServiceException
	 * @throwsomero.ServerError
	 */
	boolean isSigned(long pixelsId) throws omero.ServerError;
	
	/**
	 * Are the bytes in the image floats.
	 * @param pixelsId the pixelsId of the image.
	 * @return see above.
	 * @throws DSOutOfServiceException
	 * @throwsomero.ServerError
	 */
	boolean isFloat(long pixelsId) throws omero.ServerError;

	
}


