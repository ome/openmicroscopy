/*
 * blitzgateway.service.gateway.IQueryGatewayImpl 
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
import omero.ServerError;
import omero.api.IQueryPrx;
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
class IQueryGatewayImpl
	implements IQueryGateway
{	

	/** The BlitzGateway. */
	private BlitzGateway blitzGateway;
	
	/**
	 * The constructor for the IQuery Gateway.
	 * @param gateway the blitzGateway.
	 */
	IQueryGatewayImpl(BlitzGateway gateway)
	{
		blitzGateway = gateway;
	}
	
	/**
	 * Find an object by a query.
	 * @param query the query
	 * @return the object, or null.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public IObject findByQuery(String query) 
		throws DSOutOfServiceException, DSAccessException
	{
		IQueryPrx queryService = blitzGateway.getQueryService();
		try
		{
			return queryService.findByQuery(query, null);
		}
		catch (ServerError e)
		{
			ServiceUtilities.handleException(e, "In Query findByQuery: error in query"+ query);
		}
		return null;
	}
			
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
		throws DSOutOfServiceException, DSAccessException
	{
		IQueryPrx queryService =  blitzGateway.getQueryService();
		try
		{
			return queryService.findByString(klass, field, value);
		}
		catch (ServerError e)
		{
			ServiceUtilities.handleException(e, "In Query findByString: error in query with "
				+ "class : " +klass + " field : " + field + " value " + value);
		}
		return null;
	}
	
	/**
	 * Find all objects by a query.
	 * @param query the query
	 * @return the list object, or null.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public List<IObject> findAllByQuery(String query) 
		throws DSOutOfServiceException, DSAccessException
	{
		IQueryPrx queryService =  blitzGateway.getQueryService();
		try
		{
			return queryService.findAllByQuery(query, null);
		}
		catch (ServerError e)
		{
			ServiceUtilities.handleException(e, "In Query findAllByQuery: error in query"+ query);
		}
		return null;
	}
	
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
 		throws DSOutOfServiceException, DSAccessException
 	{
 		if (klass == null)
 			throw new NullPointerException("Expecting not-null klass.");
 		if (value == null) return null;

 		return findByString(klass.getName(), "value", value);
 	}
	
}


