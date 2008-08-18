/*
 * blitzgateway.service.gateway.IUpdateImpl 
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
import omero.api.IUpdatePrx;
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
class IUpdateGatewayImpl
	implements IUpdateGateway
{

	/** The BlitzGateway. */
	private BlitzGateway blitzGateway;
	
	/**
	 * The constructor for the IUpdate Gateway.
	 * @param gateway the blitzGateway.
	 */
	IUpdateGatewayImpl(BlitzGateway gateway)
	{
		blitzGateway = gateway;
	}
	
	/* (non-Javadoc)
	 * @see blitzgateway.service.gateway.IUpdateGateway#deleteObject(omero.model.IObject)
	 */
	public void deleteObject(IObject row) throws DSOutOfServiceException,
			DSAccessException
	{
		try
		{
			IUpdatePrx service = blitzGateway.getUpdateService(); 
			service.deleteObject(row);
		}
		catch (Exception e)
		{
			ServiceUtilities.handleException(e, "Cannot delete Object.");
		}
	}

	/* (non-Javadoc)
	 * @see blitzgateway.service.gateway.IUpdateGateway#saveAndReturnArray(java.util.List)
	 */
	public <T extends IObject> List<T> saveAndReturnArray(List<IObject> graph)
			throws DSOutOfServiceException, DSAccessException
	{
		try
		{
			IUpdatePrx service = blitzGateway.getUpdateService(); 
			return (List<T>)service.saveAndReturnArray(graph);
		}
		catch (Exception e)
		{
			ServiceUtilities.handleException(e, "Cannot saveand Return array.");
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see blitzgateway.service.gateway.IUpdateGateway#saveAndReturnObject(omero.model.IObject)
	 */
	public IObject saveAndReturnObject(IObject obj)
			throws DSOutOfServiceException, DSAccessException
	{
		try
		{
			IUpdatePrx service = blitzGateway.getUpdateService(); 
			return service.saveAndReturnObject(obj);
		}
		catch (Exception e)
		{
			ServiceUtilities.handleException(e, "Cannot saveAndReturn Object.");
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see blitzgateway.service.gateway.IUpdateGateway#saveArray(java.util.List)
	 */
	public void saveArray(List<IObject> graph) throws DSOutOfServiceException,
			DSAccessException
	{	
		try
		{
			IUpdatePrx service = blitzGateway.getUpdateService(); 
			service.saveArray(graph);
		}
		catch (Exception e)
		{
			ServiceUtilities.handleException(e, "Cannot saveArray.");
		}
	}

	/* (non-Javadoc)
	 * @see blitzgateway.service.gateway.IUpdateGateway#saveObject(omero.model.IObject)
	 */
	public void saveObject(IObject obj) throws DSOutOfServiceException,
		DSAccessException
	{	
		try
		{
			IUpdatePrx service = blitzGateway.getUpdateService(); 
			service.saveObject(obj);
		}
		catch (Exception e)
		{
			ServiceUtilities.handleException(e, "Cannot save Object.");
		}
	}



}


