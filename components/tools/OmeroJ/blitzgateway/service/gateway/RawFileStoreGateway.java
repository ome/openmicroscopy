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
package blitzgateway.service.gateway;


//Java imports

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.data.DSAccessException;
import org.openmicroscopy.shoola.env.data.DSOutOfServiceException;

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
	/**
	 * Set the fileId of the RawfileStore to fileId
	 * @param fileId see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	void setFileId(long fileId) 
							throws DSOutOfServiceException, DSAccessException;
	
	/**
	 * Read from the file, who's fileId has been set by setFileId.
	 * @param position position to start read.
	 * @param length length of read.
	 * @return raw file in bytes.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	byte[] read(long position, int length) 
							throws DSOutOfServiceException, DSAccessException;
	
	/**
	 * Write to the file at position with length bytes.
	 * @param buf bytes to write.
	 * @param position position to start write.
	 * @param length length of write.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	void write(byte[] buf, long position, int length) 
							throws DSOutOfServiceException, DSAccessException;
	
	/**
	 * Does file exist. 
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
		void setFileId(long fileId) 
							throws DSOutOfServiceException, DSAccessException;
		Byte[] read(long position, int length) 
							throws DSOutOfServiceException, DSAccessException;
		void write(Byte[] buf, long position, int length) 
							throws DSOutOfServiceException, DSAccessException;
		boolean exists() 
							throws DSOutOfServiceException, DSAccessException;
 	*/
}


