/*
 * ome.ij.data.DataService 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2009 University of Dundee. All rights reserved.
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
package ome.ij.data;


//Java imports
import java.io.File;
import java.util.Collection;

import pojos.ExperimenterData;

//Third-party libraries

//Application-internal dependencies

/** 
 * Provides methods to load data from an <code>OMERO</code> server.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public interface DataService
{

	/**
	 * Loads all the projects/datasets owned by the currently logged user.
	 * 
	 * @return See above.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occurred while trying to 
	 * retrieve data from OMERO service. 
	 */
	public Collection loadProjects()
		throws DSAccessException, DSOutOfServiceException;
	
	/**
	 * Loads all the images contained in the specified dataset.
	 * 
	 * @param datasetId The id of the dataset.
	 * @return See above.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occurred while trying to 
	 * retrieve data from OMERO service. 
	 */
	public Collection loadImages(long datasetId)
		throws DSAccessException, DSOutOfServiceException;
	
	/**
	 * Returns the user currently logged in.
	 * 
	 * @return See above.
	 */
	public ExperimenterData getCurrentUser();
	
	/**
	 * Loads the specified image.
	 * 
	 * @param pixelsID The id of the pixels set.
	 * @return See above.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occurred while trying to 
	 * retrieve data from OMERO service. 
	 */
	public ImageObject getImage(long pixelsID)
		throws DSAccessException, DSOutOfServiceException;
	
	/**
	 * Retrieves the specified plane for the given pixels set.
	 * 
	 * @param pixelsID The identifier of the pixels set.
	 * @param z The selected z-section.
	 * @param c The selected channel.
	 * @param t The time-point
	 * @return See above.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occurred while trying to 
	 * retrieve data from OMERO service. 
	 */
	public byte[] getPlane(long pixelsID, int z, int c, int t)
		throws DSAccessException, DSOutOfServiceException;
	
	/**
	 * Exports the specified image as OME-TIFF.
	 * 
	 * @param file The file to write the image to.
	 * @param imageID The identifier of the image.
	 * @return See above.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occurred while trying to 
	 * retrieve data from OMERO service. 
	 */
	public File exportImageAsOMETiff(File file, long imageID)
		throws DSAccessException, DSOutOfServiceException;
}
