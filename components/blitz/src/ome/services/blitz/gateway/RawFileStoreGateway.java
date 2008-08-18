/*
 * blitzgateway.service.gateway.RawFileStore 
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


//Java imports

//Third-party libraries

//Application-internal dependencies
import omero.gateway.DSAccessException;
import omero.gateway.DSOutOfServiceException;

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
public interface RawFileStoreGateway
{	
	void keepAlive() throws DSOutOfServiceException, DSAccessException;
	/**
	 * Set the FileId of the rawFile store.
	 * @param fileId
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	void setFileId(long fileId) 
							throws DSOutOfServiceException, DSAccessException;
	
	/**
	 * Read from the file in the rawFileStore. 
	 * @param position starting position to read from.
	 * @param length 
	 * @return see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	byte[] read(long position, int length) 
							throws DSOutOfServiceException, DSAccessException;
	
	/**
	 * Write the contents of the buffer to the the file starting from position
	 * and for length bytes.
	 * @param buf see above.
	 * @param position see above.
	 * @param length see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	void write(byte[] buf, long position, int length) 
							throws DSOutOfServiceException, DSAccessException;
	
	/**
	 * Does the file exist in the RawFileStore.
	 * @return see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	boolean exists() 
							throws DSOutOfServiceException, DSAccessException;

	
	/* 
	 * RawFileStore java to ICE Mappings from the API.ice slice definition.
	 * Below are all the calls in the RawFileStore service. 
	 * As the are created in the RawFileStoreGateway they will be marked as done.
	 *
	 *
	 *
	 *
DONE	void setFileId(long fileId) 
							throws DSOutOfServiceException, DSAccessException;
DONE	Byte[] read(long position, int length) 
							throws DSOutOfServiceException, DSAccessException;
DONE	void write(Byte[] buf, long position, int length) 
							throws DSOutOfServiceException, DSAccessException;
DONE	boolean exists() 
							throws DSOutOfServiceException, DSAccessException;
 	*/
}


