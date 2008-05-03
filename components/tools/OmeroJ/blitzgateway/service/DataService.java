/*
 * blitzgateway.service.DataServuce 
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
import java.util.List;

//Third-party libraries

//Application-internal dependencies
import omero.model.Dataset;
import omero.model.Image;
import omero.model.Pixels;
import omero.model.PixelsType;
import omero.model.Project;

import org.openmicroscopy.shoola.env.data.DSAccessException;
import org.openmicroscopy.shoola.env.data.DSOutOfServiceException;

import blitzgateway.util.OMEROClass;


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
interface DataService
{		
	/**
	 * Retrieves the images contained in containers specified by the 
	 * node type.
	 * 
	 * @param nodeType	The type of container. Can either be Project, 
	 * 					Dataset, CategoryGroup, Category or Image.
	 * @param nodeIDs   Set of node ids..
	 * @param userID	The Id of the root.
	 * @return A <code>Set</code> of retrieved images.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occured while trying to 
	 * retrieve data from OMERO service. 
	 */
	public List<Image> getImages(OMEROClass nodeType, List<Long> nodeIds)
	throws DSOutOfServiceException, DSAccessException;
	
	/**
	 * Get the pixels associated with the image.
	 * @param imageId
	 * @return the list of pixels.
	 */
	public List<Pixels> getPixelsFromImage(long imageId) 
		throws DSOutOfServiceException, DSAccessException;
	
	/**
	 * Get the Datasets from the user account with id's
	 * @param ids see above.
	 * @param getLeaves should image data be populated.
	 * @return see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public List<Dataset> getDatasets(List<Long> ids, boolean getLeaves)
		throws DSOutOfServiceException, DSAccessException;

	/**
	 * Get the projects in the datasets.
	 * @param getLeaves see above.
	 * @return see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public List<Project> getProjects(List<Long> ids, boolean getLeaves) 
	throws DSOutOfServiceException, DSAccessException;
	
	/**
	 * Get the PixelTypes available in the system.
	 * @return see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public List<PixelsType> getPixelTypes() 
	throws DSOutOfServiceException, DSAccessException;

	/**
	 * Get the pixelsType with type.
	 * @param type see above.
	 * @return see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public PixelsType getPixelType(String type) 
	throws DSOutOfServiceException, DSAccessException;


}


