/*
 * blitzgateway.service.gateway.RawPixelsStoreGateway 
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
import omero.gateways.DSAccessException;
import omero.gateways.DSOutOfServiceException;

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
public interface RawPixelsStoreGateway
{	
	
	void keepAlive() throws  DSOutOfServiceException, DSAccessException;
	/**
	 * Get the plane size of the current image.
	 * @return see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	int getPlaneSize() throws  DSOutOfServiceException, DSAccessException;

	/**
	 * Get the total size of the current image.
	 * @return see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	int getTotalSize() throws  DSOutOfServiceException, DSAccessException;
	
	/**
	 * Get the plane size of the current image.
	 * @return see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	int getRowSize() throws  DSOutOfServiceException, DSAccessException;
	
	/**
	 * Get the stack size of the current image.
	 * @return see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	int getStackSize() throws  DSOutOfServiceException, DSAccessException;
	
	/**
	 * Get the timepoint size of the current image.
	 * @return see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	int getTimepointSize() throws  DSOutOfServiceException, DSAccessException;
	
	/**
	 * get bytes to the plane c, t, z
	 * @param z see above.
	 * @param c see above.
	 * @param t the bytes.
	 * @return see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public byte[] getPlane(int z, int c, int t) 
			throws DSOutOfServiceException, DSAccessException;
	
	/**
	 * set the plane at z, c, t to the bytes of buf.
	 * @param buf bytes of the plane.
	 * @param z z section
	 * @param c channel
	 * @param t timepoint
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	void setPlane(byte[] buf, int z, int c, int t) throws  DSOutOfServiceException, DSAccessException;
	
	/**
	 * Get the bytewidth of the image
	 * @return see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	int getByteWidth() throws  DSOutOfServiceException, DSAccessException;
	
	/**
	 * Are the bytes in the image signed.
	 * @return see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	boolean isSigned() throws  DSOutOfServiceException, DSAccessException;
	
	/**
	 * Are the bytes in the image floats.
	 * @return see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	boolean isFloat() throws  DSOutOfServiceException, DSAccessException;

	/*
	 * 
	 * RawPixelsStore java to ICE Mappings from the API.ice slice definition.
	 * Below are all the calls in the RawPixelsStore service. 
	 * As the are created in the RawPixelsStoreGateway they will be marked as 
	 * done.
	 * 
	 * 
	 * 
	 *
DONE	void setPixelsId(long pixelsId) throws  DSOutOfServiceException, DSAccessException;
DONE	int getPlaneSize() throws  DSOutOfServiceException, DSAccessException;
DONE	int getRowSize() throws  DSOutOfServiceException, DSAccessException;
DONE	int getStackSize() throws  DSOutOfServiceException, DSAccessException;
DONE	int getTimepointSize() throws  DSOutOfServiceException, DSAccessException;
DONE	int getTotalSize() throws  DSOutOfServiceException, DSAccessException;
		long getRowOffset(int y, int z, int c, int t) throws  DSOutOfServiceException, DSAccessException;
		long getPlaneOffset(int z, int c, int t) throws  DSOutOfServiceException, DSAccessException;
		long getStackOffset(int c, int t) throws  DSOutOfServiceException, DSAccessException;
		long getTimepointOffset(int t) throws  DSOutOfServiceException, DSAccessException;
		byte[] getRegion(int size, long offset) throws  DSOutOfServiceException, DSAccessException;
		byte[] getRow(int y, int z, int c, int t) throws  DSOutOfServiceException, DSAccessException;
DONE	byte[] getPlane(int z, int c, int t) throws  DSOutOfServiceException, DSAccessException;
		byte[] getPlaneRegion(int z, int c, int t, int size, int offset) throws  DSOutOfServiceException, DSAccessException;
		byte[] getStack(int c, int t) throws  DSOutOfServiceException, DSAccessException;
		byte[] getTimepoint(int t) throws  DSOutOfServiceException, DSAccessException;
		void setRegion(int size, long offset, byte[] buffer) throws  DSOutOfServiceException, DSAccessException;
		void setRow(byte[] buf, int y, int z, int c, int t) throws  DSOutOfServiceException, DSAccessException;
DONE	void setPlane(byte[] buf, int z, int c, int t) throws  DSOutOfServiceException, DSAccessException;
		void setStack(byte[] buf, int z, int c, int t) throws  DSOutOfServiceException, DSAccessException;
		void setTimepoint(byte[] buf, int t) throws  DSOutOfServiceException, DSAccessException;
DONE	int getByteWidth() throws  DSOutOfServiceException, DSAccessException;
DONE	boolean isSigned() throws  DSOutOfServiceException, DSAccessException;
DONE	boolean isFloat() throws  DSOutOfServiceException, DSAccessException;
		byte[] calculateMessageDigest() throws  DSOutOfServiceException, DSAccessException;
	*/
}


