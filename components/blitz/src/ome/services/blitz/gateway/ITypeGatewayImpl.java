/*
 * blitzgateway.service.gateway.ITypeServiceGatewayImpl 
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
import omero.api.ITypesPrx;
import omero.model.IObject;


import omero.gateway.DSAccessException;
import omero.gateway.DSOutOfServiceException;



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
public class ITypeGatewayImpl
	implements ITypeGateway
{	
	/** The Blitzgateway. */
	private BlitzGateway blitzGateway;
	
	/**
	 * The constructor for the IType Gateway.
	 * @param gateway the blitzGateway.
	 */
	ITypeGatewayImpl(BlitzGateway gateway)
	{
		blitzGateway = gateway;
	}

	public List<IObject> allEnumerations(String klass) 
	throws DSOutOfServiceException, DSAccessException
	{
		ITypesPrx typesService = blitzGateway.getTypesService();
		try
		{
			return typesService.allEnumerations(klass);
		}
		catch(ServerError e)
		{
			ServiceUtilities.handleException(e, "Unable to retrieve Enumerations for : " + klass);
		}
		return null;
	}

}


