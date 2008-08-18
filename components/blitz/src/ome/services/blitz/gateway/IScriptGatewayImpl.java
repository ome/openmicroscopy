/*
 * blitzgateway.service.gateway.IScriptGatewayImpl 
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
import java.util.Map;

//Third-party libraries

//Application-internal dependencies
import omero.RType;
import omero.api.IScriptPrx;


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
public class IScriptGatewayImpl
	implements IScriptGateway
{
	/**The BlitzGateway. */
	private BlitzGateway blitzGateway;
	
	/**
	 * The constructor for the IScript Gateway.
	 * @param gateway the blitzGateway.
	 */
	IScriptGatewayImpl(BlitzGateway gateway)
	{
		blitzGateway = gateway;
	}
	
	/* (non-Javadoc)
	 * @see blitzgateway.service.gateway.IScriptGateway#deleteScript(long)
	 */
	public void deleteScript(long id) throws DSOutOfServiceException,
			DSAccessException
	{
		IScriptPrx scriptService = blitzGateway.getScriptService();
		try
		{
			scriptService.deleteScript(id);
		}
		catch(Exception e)
		{
			ServiceUtilities.handleException(e, "cannot delete script : " + id);
		}
		
	}

	/* (non-Javadoc)
	 * @see blitzgateway.service.gateway.IScriptGateway#getParams(long)
	 */
	public Map<String, RType> getParams(long id) throws DSOutOfServiceException,
			DSAccessException
	{
		IScriptPrx scriptService = blitzGateway.getScriptService();
		try
		{
			return scriptService.getParams(id);
		}
		catch(Exception e)
		{
			ServiceUtilities.handleException(e, "cannot get params for script : " + id);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see blitzgateway.service.gateway.IScriptGateway#getScript(long)
	 */
	public String getScript(long id) throws DSOutOfServiceException,
			DSAccessException
	{
		IScriptPrx scriptService = blitzGateway.getScriptService();
		try
		{
			return scriptService.getScript(id);
		}
		catch(Exception e)
		{
			ServiceUtilities.handleException(e, "cannot get script : " + id);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see blitzgateway.service.gateway.IScriptGateway#getScriptID(java.lang.String)
	 */
	public long getScriptID(String name) throws DSOutOfServiceException,
			DSAccessException
	{
		IScriptPrx scriptService = blitzGateway.getScriptService();
		try
		{
			return scriptService.getScriptID(name);
		}
		catch(Exception e)
		{
			ServiceUtilities.handleException(e, "cannot find script with name "
				+ name);
		}
		return -1;
	}

	/* (non-Javadoc)
	 * @see blitzgateway.service.gateway.IScriptGateway#getScripts()
	 */
	public Map<Long, String> getScripts() throws DSOutOfServiceException,
			DSAccessException
	{
		IScriptPrx scriptService = blitzGateway.getScriptService();
		try
		{
			return scriptService.getScripts();
		}
		catch(Exception e)
		{
			ServiceUtilities.handleException(e, "cannot get scripts.");
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see blitzgateway.service.gateway.IScriptGateway#runScript(long, java.util.Map)
	 */
	public Map<String, RType> runScript(long id, Map<String, RType> map)
			throws DSOutOfServiceException, DSAccessException
	{
		IScriptPrx scriptService = blitzGateway.getScriptService();
		try
		{
			return scriptService.runScript(id, map);
		}
		catch(Exception e)
		{
			ServiceUtilities.handleException(e, "cannot run script : " + id);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see blitzgateway.service.gateway.IScriptGateway#uploadScript(java.lang.String)
	 */
	public long uploadScript(String script) throws DSOutOfServiceException,
			DSAccessException
	{
		IScriptPrx scriptService = blitzGateway.getScriptService();
		try
		{
			return scriptService.uploadScript(script);
		}
		catch(Exception e)
		{
			ServiceUtilities.handleException(e, "cannot get upload script.");
		}
		return -1;
	}

}


