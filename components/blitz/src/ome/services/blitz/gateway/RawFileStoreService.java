/*
 * blitzgateway.service.RawFileStoreService 
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
public interface RawFileStoreService
{	
	/**
	 * Keep service alive.
	 * @throws DSOutOfServiceException
	 * @throws omero.ServerError
	 */
	public void keepAlive() throws omero.ServerError;

	/**
	 * Close the gateway for pixels = pixelsId
	 * @param pixelsId see above.
	 * @return true if the gateway was closed.
	 * @throws omero.ServerError;
	 */
	public boolean closeGateway(long fileId) throws omero.ServerError;
	
	/**
	 * Read from the file in the rawFileStore.
	 * @param fileId id of the file to work on, 
	 * @param position starting position to read from.
	 * @param length 
	 * @return see above.
	 * @throws DSOutOfServiceException
	 * @throws omero.ServerError
	 */
	byte[] read(long fileId, long position, int length) 
							throws omero.ServerError;
	
	/**
	 * Write the contents of the buffer to the the file starting from position
	 * and for length bytes.
	 * @param fileId id of the file to work on, 
	 * @param buf see above.
	 * @param position see above.
	 * @param length see above.
	 * @throws DSOutOfServiceException
	 * @throws omero.ServerError
	 */
	void write(long fileId, byte[] buf, long position, int length) 
							throws omero.ServerError;
	
	/**
	 * Does the file exist in the RawFileStore.
	 * @param fileId id of the file to work on, 
	 * @return see above.
	 * @throws DSOutOfServiceException
	 * @throws omero.ServerError
	 */
	boolean exists(long fileId) 
							throws omero.ServerError;
}


