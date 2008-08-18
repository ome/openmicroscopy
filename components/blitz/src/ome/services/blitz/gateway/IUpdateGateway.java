/*
 * blitzgateway.service.gateway.IUpdateGateway 
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

//Third-party libraries

//Application-internal dependencies

import omero.gateways.DSAccessException;
import omero.gateways.DSOutOfServiceException;

import omero.model.IObject;

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
public interface IUpdateGateway
{	
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

	
	/*
	 * UpdateService java to ICE Mappings from the API.ice slice definition.
	 * Below are all the calls in the IUpdate service. 
	 * As the are created in the IUpdateServiceGateway they will be marked
	 * as done.
	 * 
	 * 
	 * 
	 * 
	 *
DONE	void saveObject(IObject obj) throws  DSOutOfServiceException, DSAccessException;
		void saveCollection(List<IObject> objs) throws  DSOutOfServiceException, DSAccessException;
DONE	IObject saveAndReturnObject(IObject obj) throws  DSOutOfServiceException, DSAccessException;
DONE	void saveArray(List<IObject> graph) throws  DSOutOfServiceException, DSAccessException;
DONE	List<IObject> saveAndReturnArray(List<IObject> graph) throws  DSOutOfServiceException, DSAccessException;
DONE	void deleteObject(IObject row) throws  DSOutOfServiceException, DSAccessException;
		void indexObject(IObject row) throws  DSOutOfServiceException, DSAccessException;
	*/
}


