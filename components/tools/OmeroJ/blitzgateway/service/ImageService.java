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
package blitzgateway.service;

//Java imports

//Third-party libraries

//Application-internal dependencies
import java.util.List;

import omero.model.Image;
import omero.model.Pixels;

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
public interface ImageService
{	
	
	/**
	 * Get the raw plane from the server with id imageID, and channels, c, timepoint
	 * t, and z-section z. This is the plane as bytes, not converted to doubles.
	 * 
	 * @param imageID see above.
	 * @param c see above.
	 * @param t see above.
	 * @param z see above.
	 * @return see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public byte[] getRawPlane(long imageID, int z, int c, int t) 
		throws DSOutOfServiceException, DSAccessException;
	
	/**
	 * Get the plane from the server with id imageID, and channels, c, timepoint
	 * t, and z-section z. This is the plane converted to doubles.
	 * 
	 * @param imageID see above.
	 * @param c see above.
	 * @param t see above.
	 * @param z see above.
	 * @return see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public double[][] getPlane(long imageID, int c, int t, int z) 
	throws DSOutOfServiceException, DSAccessException;
	
	/**
	 * Get the pixels information for an image.
	 * @param imageID image id relating to the pixels.
	 * @return see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public Pixels getPixels(long imageID)
		throws DSOutOfServiceException, DSAccessException;
	
	/**
	 * Get the image information for an image.
	 * @param imageID image id relating to the iamge.
	 * @return see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public Image getImage(long imageID)
		throws DSOutOfServiceException, DSAccessException;
	
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
	throws DSOutOfServiceException, DSAccessException;
	
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
	throws DSOutOfServiceException, DSAccessException;
	
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
	 * @param c channel.
	 * @param t time point.
	 * @param z z section. 
	 * @param data plane data. 
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public void uploadPlane(long pixelsId, int c, int t, int z, 
			double [][] data) throws DSOutOfServiceException, DSAccessException;
	public Pixels updatePixels(Pixels object) 
	throws DSOutOfServiceException, DSAccessException;

}


