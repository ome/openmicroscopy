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
package ome.services.blitz.gateway;


//Java imports
import java.util.List;
import java.util.Map;

import omero.gateway.ContainerClass;
import omero.gateway.DSAccessException;
import omero.gateway.DSOutOfServiceException;
import omero.model.Dataset;
import omero.model.IObject;
import omero.model.Image;
import omero.model.Pixels;
import omero.model.PixelsType;
import omero.model.Project;






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
	 * Keep service alive.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public void keepAlive() throws DSOutOfServiceException, DSAccessException;

	/**
	 * Retrieves the images contained in containers specified by the 
	 * node type.
	 * 
	 * @param nodeType	The type of container. Can either be Project, 
	 * 					Dataset, CategoryGroup, Category or Image.
	 * @param nodeIds   Set of node ids..
	 * @return A <code>Set</code> of retrieved images.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occurred while trying to 
	 * retrieve data from OMERO service. 
	 */
	public List<Image> getImages(ContainerClass nodeType, List<Long> nodeIds)
	throws DSOutOfServiceException, DSAccessException;
	
	/**
	 * Get the pixels associated with the image.
	 * @param imageId
	 * @return the list of pixels.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occurred while trying to 
	 */
	public List<Pixels> getPixelsFromImage(long imageId) 
		throws DSOutOfServiceException, DSAccessException;
	
	/**
	 * Get the Datasets from the projects with id's
	 * @param ids see above.
	 * @param getLeaves should image data be populated.
	 * @return see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public List<Dataset> getDatasets(List<Long> ids, boolean getLeaves)
		throws DSOutOfServiceException, DSAccessException;

	/**
	 * Get the projects in the users.
	 * @param ids The ids of the users.
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

	/**
	 * Run the query against the iQuery interface. 
	 * @param myQuery the string containing the query.
	 * @return result of query
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public List<IObject> findAllByQuery(String myQuery)
	throws DSOutOfServiceException, DSAccessException;
	
	/**
	 * Run the query against the iQuery interface. 
	 * @param myQuery the string containing the query.
	 * @return result of query
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public IObject findByQuery(String myQuery)
	throws DSOutOfServiceException, DSAccessException;

	/**
	 * Attach an image to a dataset.
	 * @param dataset 
	 * @param image 
	 * @throws DSOutOfServiceException 
	 * @throws DSAccessException 
	 * 
	 */
	public void attachImageToDataset(Dataset dataset, Image image)  
	throws DSOutOfServiceException, DSAccessException;
	
	/**
	 * Get the images, with pixels from a project. 
	 * @param project see above.
	 * @return see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public List<Image> getImagesFromProject(Project project) 
	throws DSOutOfServiceException, DSAccessException;


	/**
	 * Get the images with pixels from a dataset with id. 
	 * @param dataset see above.
	 * @return see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public List<Image> getImagesFromDataset(Dataset dataset)
	throws DSOutOfServiceException, DSAccessException;
	
	/**
	 * Get the datasets from a project.
	 * @param project see above.
	 * @return see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public List<Dataset> getDatasetsFromProject(Project project)
	throws DSOutOfServiceException, DSAccessException;
	
	/**
	 * Get the Pixels list from the dataset.
	 * @param dataset see above.
	 * @return see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public List<Pixels> getPixelsFromDataset(Dataset dataset)
	throws DSOutOfServiceException, DSAccessException;
	
	/**
	 * Get the Pixels list from the project.
	 * @param project see above.
	 * @return see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public List<Pixels> getPixelsFromProject(Project project)
	throws DSOutOfServiceException, DSAccessException;
	
	/**
	 * Get the pixels from the images in the list.
	 * @param images see above.
	 * @return map of the pixels-->imageId.
	 */
	public Map<Long, Pixels> getPixelsImageMap(List<Image> images);

	/**
	 * Get the pixels from the images in the list.
	 * @param images see above.
	 * @return list of the pixels.
	 */
	public List<Pixels> getPixelsFromImageList(List<Image> images);
	
	/**
	 * Get the images from the dataset with name, this can use wild cards.
	 * @param datasetId see above.
	 * @param imageName see above.
	 * @return see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public List<Image> getImageFromDatasetByName(Long datasetId, String imageName)
	throws DSOutOfServiceException, DSAccessException;

	/**
	 * Get the list of images with name containing imageName.
	 * @param imageName see above.
	 * @return see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public List<Image> getImageByName(String imageName)
	throws DSOutOfServiceException, DSAccessException;

	/**
	 * Save the object to the db . 
	 * @param obj see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	void saveObject(IObject obj) 
							throws  DSOutOfServiceException, DSAccessException;
	
	/**
	 * Save and return the Object.
	 * @param obj see above.
	 * @return see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	IObject saveAndReturnObject(IObject obj) 
							throws  DSOutOfServiceException, DSAccessException;
	
	/**
	 * Save the array.
	 * @param graph see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	void saveArray(List<IObject> graph) 
							throws  DSOutOfServiceException, DSAccessException;
	
	/**
	 * Save and return the array.
	 * @param <T> The Type to return.
	 * @param graph the object
	 * @return see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	 <T extends omero.model.IObject>List<T> 
	 			saveAndReturnArray(List<IObject> graph)
	 						throws  DSOutOfServiceException, DSAccessException;
	 
	 /**
	  * Delete the object.
	  * @param row the object.(commonly a row in db)
	  * @throws DSOutOfServiceException
	  * @throws DSAccessException
	  */
	void deleteObject(IObject row) 
							throws  DSOutOfServiceException, DSAccessException;
}


