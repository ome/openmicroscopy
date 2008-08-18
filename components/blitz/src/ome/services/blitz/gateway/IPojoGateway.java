/*
 * blitzgateway.service.IPojoGateway 
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

//Third-party libraries

//Application-internal dependencies
import omero.RType;

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
public interface IPojoGateway
{	
	
	/**
	 * Get the container for the class klass, and return the containers (project, dataset, etc. )
	 * that matches the ids in the list leaves. Populate the leaves of the container
	 * (dataset for project, images for dataset, etc.) depending on the options
	 * passed. 
	 * @param <T> Type. 
	 * @param rootType See above.
	 * @param rootIds see above.
	 * @param options see above.
	 * @return see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public <T extends omero.model.IObject>List<T>  loadContainerHierarchy
	(String rootType, List<Long> rootIds, Map<String, RType> options) 
						throws DSAccessException, DSOutOfServiceException;
	
	/**
	 * Retrieves the images contained in containers specified by the 
	 * node type.
	 * @param <T> type to cast IObject. 
	 * @param rootType  The type of container. Can be either Project, Dataset,
	 *                  CategoryGroup, Category.
	 * @param rootIds   Set of containers' IDS.
	 * @param options   Options to retrieve the data.
	 * @return A <code>Set</code> of retrieved images.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occurred while trying to 
	 * retrieve data from OMERO service. 
	 */
	public <T extends omero.model.IObject>List<T> getImages(String rootType, 
		List<Long> rootIds, Map<String, RType>  options) throws 
		DSAccessException, DSOutOfServiceException;
	
	/* 
	 * IPojos java to ICE Mappings from the API.ice slice definition.
	 * Below are all the calls in the IPojos service. 
	 * As the are created in the IPojoGateway they will be marked as done.
	 *
	 *
	 *
	 *
DONE	List<IObject> loadContainerHierarchy(String rootType, List<Long> rootIds, 
		Map options) throws DSAccessException, DSOutOfServiceException;
		List<IObject> findContainerHierarchies(String rootType, List<Long> imageIds, 
		Map options) throws DSAccessException, DSOutOfServiceException;
		Map<Long, IObject> findAnnotations(String rootType, List<Long> rootIds, 
		List<Long> annotatorIds, Map options) throws DSAccessException, DSOutOfServiceException;
		List<IObject> findCGCPaths(List<Long> imageIds, String algo, Map options) throws DSAccessException, DSOutOfServiceException;
DONE	List<ImageI> getImages(String rootType, List<Long> rootIds, Map options) throws DSAccessException, DSOutOfServiceException;
		List<ImageI> getUserImages(Map options) throws DSAccessException, DSOutOfServiceException;
		List<ImageI> getImagesByOptions(Map options) throws DSAccessException, DSOutOfServiceException;
		Map<String, omero.model.Experimenter> getUserDetails(List<String> names, Map options) throws DSAccessException, DSOutOfServiceException;
		Map<Integer, Integer> getCollectionCount(String type, String property, List<Long> ids, Map options) throws DSAccessException, DSOutOfServiceException;
		List<IObject> retrieveCollection(IObject obj, String collectionName, Map options) throws DSAccessException, DSOutOfServiceException;
		IObject createDataObject(IObject obj, Map options) throws DSAccessException, DSOutOfServiceException;
		List<IObject> createDataObjects(List<IObject> dataObjects, Map options) throws DSAccessException, DSOutOfServiceException;
		void unlink(List<IObject> links, Map options) throws DSAccessException, DSOutOfServiceException;
		List<IObject> link(List<IObject> links, Map options) throws DSAccessException, DSOutOfServiceException;
		IObject updateDataObject(IObject obj, Map options) throws DSAccessException, DSOutOfServiceException;
		List<IObject> updateDataObjects(List<IObject> objs, Map options) throws DSAccessException, DSOutOfServiceException;
		void deleteDataObject(IObject obj, Map options) throws DSAccessException, DSOutOfServiceException;
		void deleteDataObjects(List<IObject> objs, Map options) throws DSAccessException, DSOutOfServiceException;
	*/
}


