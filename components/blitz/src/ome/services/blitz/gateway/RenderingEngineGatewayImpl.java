/*
 * blitzgateway.service.gateway.RenderingEngineGatewayImpl 
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

//Third-party libraries

//Application-internal dependencies
import omero.api.RenderingEnginePrx;
import omero.gateways.DSAccessException;
import omero.gateways.DSOutOfServiceException;
import omero.model.Pixels;
import omero.romio.PlaneDef;




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
public class RenderingEngineGatewayImpl
	implements RenderingEngineGateway
{	
	/** The blitzGateway. */
	private BlitzGateway 		blitzGateway;
	
	/** The current pixelsId of the renderingGateway. */
	private Long		 		pixelsId;
	
	/** The RenderingEngine Proxy from Ice. */
	private RenderingEnginePrx 	service;
	
	/**
	 * Create the RenderingEngine for the pixelsId and get the service from the gateway.
	 * @param pixelsId see above.
	 * @param gateway see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	RenderingEngineGatewayImpl(Long pixelsId, BlitzGateway gateway) 
						throws DSOutOfServiceException, DSAccessException
	{
		blitzGateway = gateway;
		this.pixelsId = pixelsId;
		service = createRenderingEngine(pixelsId);
	}
	/**
	 * Creates a new rendering service for the specified pixels set.
	 * 
	 * @param pixelsId  The pixels set ID.
	 * @return See above.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occured while trying to 
	 * retrieve data from OMERO service. 
	 */
	private RenderingEnginePrx createRenderingEngine(long pixelsId)
		throws DSOutOfServiceException, DSAccessException
	{
		try 
		{
			RenderingEnginePrx service = blitzGateway.getRenderingEngineService();
			service.lookupPixels(pixelsId);
			if(!service.lookupRenderingDef(pixelsId))
				service.resetDefaults();
			service.load();
			return service;
		} 
		catch (Throwable t) 
		{
			ServiceUtilities.handleException(t, "Cannot start the Rendering Engine for pixels : "+ pixelsId);
		}
		return null;
	}
	
	/*
	 * (non-Javadoc)
	 * @see blitzgateway.service.gateway.RenderingEngineGateway#renderAsPackedInt(java.lang.Long, int, int)
	 */
	public int[] renderAsPackedInt(int z, int t) throws DSOutOfServiceException, DSAccessException
	{
		PlaneDef def = new PlaneDef();
		def.t = t;
		def.z = z;
		def.x = 0;
		def.y = 0;
		def.slice = 0;
		try
		{
			return service.renderAsPackedInt(def);
		}
		catch(Exception e)
		{
			ServiceUtilities.handleException(e, "Cannot renderAsPackedInt for def : " + def);
		}
		return null;
	}
	/* (non-Javadoc)
	 * @see blitzgateway.service.gateway.RenderingEngineGateway#isActive(java.lang.Long, int)
	 */
	public boolean isActive(int w)
			throws DSOutOfServiceException, DSAccessException
	{
		try
		{
			return service.isActive(w);
		}
		catch(Exception e)
		{
			ServiceUtilities.handleException(e,"Unable to call isActive on Pixels : " + pixelsId);
		}
		return false;
	}
	/* (non-Javadoc)
	 * @see blitzgateway.service.gateway.RenderingEngineGateway#setActive(java.lang.Long, int, boolean)
	 */
	public void setActive(int w, boolean active)
			throws DSOutOfServiceException, DSAccessException
	{
		try
		{
			service.setActive(w, active);
		}
		catch(Exception e)
		{
			ServiceUtilities.handleException(e,"Unable to call setActive on Pixels : " + pixelsId);
		}
	}
	/* (non-Javadoc)
	 * @see blitzgateway.service.gateway.RenderingEngineGateway#getChannelWindowEnd(int)
	 */
	public double getChannelWindowEnd(int w) throws DSOutOfServiceException,
			DSAccessException
	{
		try
		{
			return service.getChannelWindowEnd(w);
		}
		catch(Exception e)
		{
			ServiceUtilities.handleException(e,"Unable to call getChannelWindowEnd for Pixels :" + pixelsId);
		}
		return -1;
	}
	/* (non-Javadoc)
	 * @see blitzgateway.service.gateway.RenderingEngineGateway#getChannelWindowStart(int)
	 */
	public double getChannelWindowStart(int w) throws DSOutOfServiceException,
			DSAccessException
	{
		try
		{
			return service.getChannelWindowStart(w);
		}
		catch(Exception e)
		{
			ServiceUtilities.handleException(e,"Unable to call getChannelWindowStart for Pixels :" + pixelsId);
		}
		return -1;
	}
	/* (non-Javadoc)
	 * @see blitzgateway.service.gateway.RenderingEngineGateway#getDefaultT()
	 */
	public int getDefaultT() throws DSOutOfServiceException, DSAccessException
	{
		try
		{
			return service.getDefaultT();
		}
		catch(Exception e)
		{
			ServiceUtilities.handleException(e,"Unable to call getDefaultT for Pixels :" + pixelsId);
		}
		return -1;
	}
	/* (non-Javadoc)
	 * @see blitzgateway.service.gateway.RenderingEngineGateway#getDefaultZ()
	 */
	public int getDefaultZ() throws DSOutOfServiceException, DSAccessException
	{
		try
		{
			return service.getDefaultZ();
		}
		catch(Exception e)
		{
			ServiceUtilities.handleException(e,"Unable to call getDefaultZ for Pixels :" + pixelsId);
		}
		return -1;
	}
	/* (non-Javadoc)
	 * @see blitzgateway.service.gateway.RenderingEngineGateway#getPixels()
	 */
	public Pixels getPixels() throws DSOutOfServiceException, DSAccessException
	{
		try
		{
			return service.getPixels();
		}
		catch(Exception e)
		{
			ServiceUtilities.handleException(e,"Unable to call getPixels for Pixels :" + pixelsId);
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see blitzgateway.service.gateway.RenderingEngineGateway#setChannelWindow(int, double, double)
	 */
	public void setChannelWindow(int w, double start, double end)
			throws DSOutOfServiceException, DSAccessException
	{
		try
		{
			service.setChannelWindow(w, start, end);
		}
		catch(Exception e)
		{
			ServiceUtilities.handleException(e,"Unable to call setChannelWindow for Pixels :" + pixelsId);
		}
	}
	
	/* (non-Javadoc)
	 * @see blitzgateway.service.gateway.RenderingEngineGateway#setDefaultT(int)
	 */
	public void setDefaultT(int t) throws DSOutOfServiceException,
			DSAccessException
	{
		try
		{
			service.setDefaultT(t);
		}
		catch(Exception e)
		{
			ServiceUtilities.handleException(e,"Unable to call setDefaultT for Pixels :" + pixelsId);
		}
	}
	/* (non-Javadoc)
	 * @see blitzgateway.service.gateway.RenderingEngineGateway#setDefaultZ(int)
	 */
	public void setDefaultZ(int z) throws DSOutOfServiceException,
			DSAccessException
	{
		try
		{
			service.setDefaultZ(z);
		}
		catch(Exception e)
		{
			ServiceUtilities.handleException(e,"Unable to call setDefaultZ for Pixels :" + pixelsId);
		}
	}
	/* (non-Javadoc)
	 * @see blitzgateway.service.gateway.RenderingEngineGateway#getCompressionLevel()
	 */
	public float getCompressionLevel() throws DSOutOfServiceException,
			DSAccessException
	{
		try
		{
			return service.getCompressionLevel();
		}
		catch(Exception e)
		{
			ServiceUtilities.handleException(e,"Unable to call getCompressionLevel on Pixels : " + pixelsId);
		}
		return 0;	
	}
	/* (non-Javadoc)
	 * @see blitzgateway.service.gateway.RenderingEngineGateway#renderCompressed(int, int)
	 */
	public byte[] renderCompressed(int z, int t)
			throws DSOutOfServiceException, DSAccessException
	{
		PlaneDef def = new PlaneDef();
		def.t = t;
		def.z = z;
		def.x = 0;
		def.y = 0;
		def.slice = 0;
		try
		{
			return service.renderCompressed(def);
		}
		catch(Exception e)
		{
			ServiceUtilities.handleException(e,"Unable to call renderCompressed on Pixels : " + pixelsId);
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see blitzgateway.service.gateway.RenderingEngineGateway#setCompressionLevel(float)
	 */
	public void setCompressionLevel(float percentage)
			throws DSOutOfServiceException, DSAccessException
	{
		try
		{
			service.setCompressionLevel(percentage);
		}
		catch(Exception e)
		{
			ServiceUtilities.handleException(e,"Unable to call setCompressionLevel on Pixels : " + pixelsId);
		}
	}
	/* (non-Javadoc)
	 * @see blitzgateway.service.gateway.BaseServiceInterface#keepAlive()
	 */
	public void keepAlive() throws DSOutOfServiceException, DSAccessException
	{
		blitzGateway.keepAlive(service);
	}
		
}


