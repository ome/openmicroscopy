/*
 * blitzgateway.service.gateway.IAdminGatewayImpl 
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

import java.util.List;

import omero.api.IAdminPrx;
import omero.api.IPixelsPrx;
import omero.model.Experimenter;


import omero.gateway.DSAccessException;
import omero.gateway.DSOutOfServiceException;


//Java imports

//Third-party libraries

//Application-internal dependencies

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
public class IAdminGatewayImpl 
	implements IAdminGateway
{	
	/** The blitzgateway. */
	private BlitzGateway blitzGateway;
	
	/**
	 * Admin Gateway. 
	 * @param gateway
	 */
	IAdminGatewayImpl(BlitzGateway gateway)
	{
		blitzGateway = gateway;
	}

	/* (non-Javadoc)
	 * @see blitzgateway.service.gateway.IAdminGateway#lookupExperimenters()
	 */
	public List<Experimenter> lookupExperimenters()
			throws DSOutOfServiceException, DSAccessException
	{
		try
		{
			IAdminPrx service = blitzGateway.getAdminService(); 
			return service.lookupExperimenters();
		} 
		catch (Throwable t) 
		{
			ServiceUtilities.handleException(t, "cannot lookupExperiments in iAdminService.");
		}
		return null;
	}

}


