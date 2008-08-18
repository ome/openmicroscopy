/*
 * blitzgateway.service.gateway.IPojoGatewayImpl 
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

//Third-party libraries

//Application-internal dependencies
import omero.RType;
import omero.api.IPojosPrx;
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
class IPojoGatewayImpl
	implements IPojoGateway
{
	/** The BlitzGateway. */
	private BlitzGateway blitzGateway;
	
	/**
	 * The constructor for the IPojos Gateway.
	 * @param gateway the blitzGateway.
	 */
	IPojoGatewayImpl(BlitzGateway gateway)
	{
		blitzGateway = gateway;
	}
	
	/* (non-Javadoc)
	 * @see IPojoGateway#getImages(String, List, Map)
	 */
	public <T extends omero.model.IObject>List<T> getImages(String nodeType, 
			List<Long> nodeIDs, Map<String, RType> options)	throws 
			DSOutOfServiceException, DSAccessException
	{
		try 
		{
			IPojosPrx service = blitzGateway.getPojosService();
			return  (List<T>)service.getImages(nodeType, nodeIDs, options);
		} 
		catch (Throwable t) 
		{
			ServiceUtilities.handleException(t, "Cannot find images for "+
				nodeType+".");
		}
	return new ArrayList();
	}


	/* (non-Javadoc)
	 * @see IPojoGateway#loadContainerHierarchy(String, List, Map)
	 */
	@SuppressWarnings("unchecked")
	public <T extends IObject>List<T> loadContainerHierarchy(String rootType,
		List<Long> leaves, Map<String, RType> options) throws 
		DSOutOfServiceException, DSAccessException
	{
		try {
			IPojosPrx service = blitzGateway.getPojosService();
			return (List<T>) (service.loadContainerHierarchy
				(rootType, leaves, options));
		} catch (Throwable t) {
			ServiceUtilities.handleException(t, "In loadContainerHierarchy : " +
					"Cannot find hierarchy for "+ rootType+".");
		}
		return new ArrayList<T>();
	}

	
}


