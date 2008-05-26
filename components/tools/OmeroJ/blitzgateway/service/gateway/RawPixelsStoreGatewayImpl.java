/*
 * blitzgateway.service.gateway.RawPixelsStoreImpl 
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
package blitzgateway.service.gateway;



//Java imports

//Third-party libraries

//Application-internal dependencies
import omero.api.RawPixelsStorePrx;
import org.openmicroscopy.shoola.env.data.DSAccessException;
import org.openmicroscopy.shoola.env.data.DSOutOfServiceException;

import blitzgateway.util.ServiceUtilities;

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
class RawPixelsStoreGatewayImpl
	implements RawPixelsStoreGateway
{	
	
	private BlitzGateway blitzGateway;
	
	RawPixelsStoreGatewayImpl(BlitzGateway gateway)
	{
		blitzGateway = gateway;
	}

	
	/* (non-Javadoc)
	 * @see blitzgateway.service.gateway.RawPixelsStoreGateway#setPixelsId()
	 */
	public void setPixelsId(long id) 
			throws DSOutOfServiceException, DSAccessException
	{
		try
		{
			blitzGateway.getPixelsStore().setPixelsId(id);
		}
		catch (Exception e)
		{
			ServiceUtilities.handleException(e, "Cannot setPixelsId in PixelsStore to : " + id);
		}
		
	}

	/* (non-Javadoc)
	 * @see blitzgateway.service.gateway.RawPixelsStoreGateway#getPlane()
	 */
	public byte[] getPlane(int z, int c, int t) 
			throws DSOutOfServiceException, DSAccessException
	{
		try
		{
			return blitzGateway.getPixelsStore().getPlane(z, c, t);
		}
		catch (Exception e)
		{
			ServiceUtilities.handleException(e, "Cannot retrieve plane .");
		}
		return null;
	}


	/* (non-Javadoc)
	 * @see blitzgateway.service.gateway.RawPixelsStoreGateway#getByteWidth()
	 */
	public int getByteWidth() throws DSOutOfServiceException, DSAccessException
	{	
		try
		{
			RawPixelsStorePrx service = blitzGateway.getPixelsStore();
			return service.getByteWidth(); 
		}
		catch (Exception e)
		{
			ServiceUtilities.handleException(e, "RawPixelsStore.getByteWidth");
		}
		return -1;
	}


	/* (non-Javadoc)
	 * @see blitzgateway.service.gateway.RawPixelsStoreGateway#getPlaneSize()
	 */
	public int getPlaneSize() throws DSOutOfServiceException, DSAccessException
	{	
		try
		{
			RawPixelsStorePrx service = blitzGateway.getPixelsStore();
			return service.getPlaneSize(); 
		}
		catch (Exception e)
		{
			ServiceUtilities.handleException(e, "RawPixelsStore.getPlaneSize");
		}
		return -1;
	}


	/* (non-Javadoc)
	 * @see blitzgateway.service.gateway.RawPixelsStoreGateway#getRowSize()
	 */
	public int getRowSize() throws DSOutOfServiceException, DSAccessException
	{	
		try
		{
			RawPixelsStorePrx service = blitzGateway.getPixelsStore();
			return service.getRowSize(); 
		}
		catch (Exception e)
		{
			ServiceUtilities.handleException(e, "RawPixelsStore.getRowSize");
		}
		return -1;
	}


	/* (non-Javadoc)
	 * @see blitzgateway.service.gateway.RawPixelsStoreGateway#getStackSize()
	 */
	public int getStackSize() throws DSOutOfServiceException, DSAccessException
	{	
		try
		{
			RawPixelsStorePrx service = blitzGateway.getPixelsStore();
			return service.getStackSize(); 
		}
		catch (Exception e)
		{
			ServiceUtilities.handleException(e, "RawPixelsStore.getStackSize");
		}
		return -1;
	}


	/* (non-Javadoc)
	 * @see blitzgateway.service.gateway.RawPixelsStoreGateway#getTimepointSize()
	 */
	public int getTimepointSize() throws DSOutOfServiceException,
			DSAccessException
	{	
		try
		{
			RawPixelsStorePrx service = blitzGateway.getPixelsStore();
			return service.getTimepointSize(); 
		}
		catch (Exception e)
		{
			ServiceUtilities.handleException(e, "RawPixelsStore.getTimepointSize");
		}
		return -1;
	}

	/* (non-Javadoc)
	 * @see blitzgateway.service.gateway.RawPixelsStoreGateway#getTotalSize()
	 */
	public int getTotalSize() throws DSOutOfServiceException, DSAccessException
	{	
		try
		{
			RawPixelsStorePrx service = blitzGateway.getPixelsStore();
			return service.getTotalSize(); 
		}
		catch (Exception e)
		{
			ServiceUtilities.handleException(e, "RawPixelsStore.getTotalSize");
		}
		return -1;
	}


	/* (non-Javadoc)
	 * @see blitzgateway.service.gateway.RawPixelsStoreGateway#isFloat()
	 */
	public boolean isFloat() throws DSOutOfServiceException, DSAccessException
	{
		try
		{
			RawPixelsStorePrx service = blitzGateway.getPixelsStore();
			return service.isFloat(); 
		}
		catch (Exception e)
		{
			ServiceUtilities.handleException(e, "RawPixelsStore.isFloat");
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see blitzgateway.service.gateway.RawPixelsStoreGateway#isSigned()
	 */
	public boolean isSigned() throws DSOutOfServiceException, DSAccessException
	{
		try
		{
			RawPixelsStorePrx service = blitzGateway.getPixelsStore();
			return service.isSigned(); 
		}
		catch (Exception e)
		{
			ServiceUtilities.handleException(e, "RawPixelsStore.isSigned");
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see blitzgateway.service.gateway.RawPixelsStoreGateway#setPlane()
	 */
	public void setPlane(byte[] buf, int z, int c, int t)
					throws DSOutOfServiceException, DSAccessException
	{
		try
		{
			RawPixelsStorePrx service = blitzGateway.getPixelsStore();
			service.setPlane(buf, z, c, t); 
		}
		catch (Exception e)
		{
			ServiceUtilities.handleException(e, "RawPixelsStore.setPlane");
		}
	}
	
}


