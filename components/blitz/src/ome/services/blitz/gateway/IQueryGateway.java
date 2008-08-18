/*
 * blitzgateway.service.gateway.IQueryGateway 
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
import omero.model.IObject;

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
public interface IQueryGateway
{	
	
	/**
	 * Find an object by a query.
	 * @param query the query
	 * @return the object, or null.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public IObject findByQuery(String query) 
		throws DSOutOfServiceException, DSAccessException;
		
	/**
	 * Find an object by a query.
	 * @param klass the query
	 * @param field the query
	 * @param value the query
	 * @return the object, or null.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public IObject findByString(String klass, String field, String value) 
		throws DSOutOfServiceException, DSAccessException;

		/**
	 * Find all objects by a query.
	 * @param query the query
	 * @return the list object, or null.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public List<IObject> findAllByQuery(String query) 
		throws DSOutOfServiceException, DSAccessException;

	
	 /**
     * Retrieves a server side enumeration.
     * 
     * @param klass the enumeration's class from <code>ome.model.enum</code>
     * @param value the enumeration's string value.
     * @return enumeration object.
	 * @throws DSAccessException 
	 * @throws DSOutOfServiceException 
     */
    public IObject getEnumeration(Class<? extends IObject> klass, String value) 
    	throws DSOutOfServiceException, DSAccessException;
	
	/* 
	 * IQuery java to ICE Mappings from the API.ice slice definition.
	 * Below are all the calls in the IQuery service. 
	 * As the are created in the IPojoGateway they will be marked as done.
	 *
	 *
	 *
	 *

		IObject get(string klass, long id) 
							throws DSOutOfServiceException, DSAccessException;
		IObject find(string klass, long id) 
							throws DSOutOfServiceException, DSAccessException;
		List<IObject> findAll(string klass, omero.sys.Filter filter) 
							throws DSOutOfServiceException, DSAccessException;
		IObject findByExample(IObject example) 
							throws DSOutOfServiceException, DSAccessException;
		List<IObject> findAllByExample(IObject example, omero.sys.Filter filter) 
							throws DSOutOfServiceException, DSAccessException;
DONE	IObject findByString(string klass, string field, string value) 
							throws DSOutOfServiceException, DSAccessException;
		List<IObject> findAllByString(string klass, string field, string value,
		 					bool caseSensitive, omero.sys.Filter filter) 
		 					throws DSOutOfServiceException, DSAccessException;
DONE	IObject findByQuery(string query, omero.sys.Parameters params) 
							throws DSOutOfServiceException, DSAccessException;
DONE	List<IObject> findAllByQuery(string query, omero.sys.Parameters params)
		 					throws DSOutOfServiceException, DSAccessException;
		List<IObject> findAllByFullText(string klass, string query, 
							omero.sys.Parameters params) 
							throws DSOutOfServiceException, DSAccessException;
		IObject refresh(IObject iObject) 
							throws DSOutOfServiceException, DSAccessException;
    */
}


