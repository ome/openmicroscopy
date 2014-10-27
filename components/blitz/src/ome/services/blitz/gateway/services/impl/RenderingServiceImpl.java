/*
 * blitzgateway.service.RenderingServiceImpl 
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
package ome.services.blitz.gateway.services.impl;

import java.awt.Color;

import ome.services.blitz.gateway.services.RenderingService;
import omero.ServerError;
import omero.api.RenderingEnginePrx;
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
public class RenderingServiceImpl
	implements RenderingService
{	
	/** RenderingEngine */
	private RenderingEnginePrx renderingEngine;
	
	/**
	 * Create the ImageService passing the gateway.
	 * @param renderingEngine To generate new instances of the 
	 * renderingEngine.
	 */
	public RenderingServiceImpl(RenderingEnginePrx renderingEngine) 
	{
		this.renderingEngine = renderingEngine;
	}

	/**
	 * Lookup the pixels for the renderingEngine for pixelsId 
	 * @param pixelsId see above.
	 * @throws ServerError
	 */
	private void lookupPixels(long pixelsId) throws ServerError
	{
		renderingEngine.lookupPixels(pixelsId);
	}
	
	/**
	 * Lookup the pixels for the renderingEngine for pixelsId 
	 * @param pixelsId see above.
	 * @throws ServerError
	 */
	private void setPixelsId(long pixelsId) throws ServerError
	{
		renderingEngine.lookupPixels(pixelsId);
		if(!renderingEngine.lookupRenderingDef(pixelsId))
			renderingEngine.resetDefaultSettings(true);
		renderingEngine.lookupRenderingDef(pixelsId);
		renderingEngine.load();
	}
	
	
	/* (non-Javadoc)
	 * @see blitzgateway.service.RenderingService#getChannelWindowEnd(java.lang.Long, int)
	 */
	public synchronized double getChannelWindowEnd(Long pixelsId, int w)
			throws  omero.ServerError
	{
		setPixelsId(pixelsId);
		return renderingEngine.getChannelWindowEnd(w);
	}


	/* (non-Javadoc)
	 * @see blitzgateway.service.RenderingService#getChannelWindowStart(java.lang.Long, int)
	 */
	public synchronized double getChannelWindowStart(Long pixelsId, int w)
			throws  omero.ServerError
	{
		setPixelsId(pixelsId);
		return renderingEngine.getChannelWindowStart(w);
	}


	/* (non-Javadoc)
	 * @see blitzgateway.service.RenderingService#getDefaultT(java.lang.Long)
	 */
	public synchronized int getDefaultT(Long pixelsId) throws 
			omero.ServerError
	{
		setPixelsId(pixelsId);
		return renderingEngine.getDefaultT();
	}


	/* (non-Javadoc)
	 * @see blitzgateway.service.RenderingService#getDefaultZ(java.lang.Long)
	 */
	public synchronized int getDefaultZ(Long pixelsId) throws 
			omero.ServerError
	{
		setPixelsId(pixelsId);
		return renderingEngine.getDefaultZ();
	}


	/* (non-Javadoc)
	 * @see blitzgateway.service.RenderingService#getPixels(java.lang.Long)
	 */
	public synchronized Pixels getPixels(Long pixelsId) throws 
			omero.ServerError
	{
		setPixelsId(pixelsId);
		return renderingEngine.getPixels();
	}


	/* (non-Javadoc)
	 * @see blitzgateway.service.RenderingService#isActive(java.lang.Long, int)
	 */
	public synchronized boolean isActive(Long pixelsId, int w)
			throws  omero.ServerError
	{
		setPixelsId(pixelsId);
		return renderingEngine.isActive(w);
	}


	/* (non-Javadoc)
	 * @see blitzgateway.service.RenderingService#renderAsPackedInt(java.lang.Long, int, int)
	 */
	public synchronized  int[] renderAsPackedInt(Long pixelsId, int z, int t)
			throws  omero.ServerError
	{
		PlaneDef def = new PlaneDef();
		def.t = t;
		def.z = z;
		def.x = 0;
		def.y = 0;
		def.slice = 0;
		setPixelsId(pixelsId);
		return renderingEngine.renderAsPackedInt(def);
	}

	/* (non-Javadoc)
	 * @see blitzgateway.service.RenderingService#setActive(java.lang.Long, int, boolean)
	 */
	public synchronized void setActive(Long pixelsId, int w, boolean active)
			throws  omero.ServerError
	{
		setPixelsId(pixelsId);
		renderingEngine.setActive(w, active);
	}


	/* (non-Javadoc)
	 * @see blitzgateway.service.RenderingService#setChannelWindow(java.lang.Long, int, double, double)
	 */
	public synchronized void setChannelWindow(Long pixelsId, int w, double start, double end)
			throws  omero.ServerError
	{
		setPixelsId(pixelsId);
		renderingEngine.setChannelWindow(w, start, end);
	}


	/* (non-Javadoc)
	 * @see blitzgateway.service.RenderingService#setDefaultT(java.lang.Long, int)
	 */
	public synchronized void setDefaultT(Long pixelsId, int t)
			throws  omero.ServerError
	{
		setPixelsId(pixelsId);
		renderingEngine.setDefaultT(t);
	}


	/* (non-Javadoc)
	 * @see blitzgateway.service.RenderingService#setDefaultZ(java.lang.Long, int)
	 */
	public synchronized void setDefaultZ(Long pixelsId, int z)
			throws  omero.ServerError
	{
		setPixelsId(pixelsId);
		renderingEngine.setDefaultZ(z);
	}


	/* (non-Javadoc)
	 * @see blitzgateway.service.RenderingService#getRenderedImage(long, int, int)
	 */
	public synchronized int[] getRenderedImage(long pixelsId, int z, int t)
			throws  omero.ServerError
	{
		Pixels pixels = getPixels(pixelsId);
		return renderAsPackedInt(pixelsId, z, t);
		
	}


	/* (non-Javadoc)
	 * @see blitzgateway.service.RenderingService#getRenderedImageMatrix(long, int, int)
	 */
	public synchronized int[][][] getRenderedImageMatrix(long pixelsId, int z, int t)
			throws  omero.ServerError
	{
		Pixels pixels = getPixels(pixelsId);
		int width = pixels.getSizeX().getValue();
		int height = pixels.getSizeY().getValue();
		int [][][] data = new int[width][height][3];
		int[] buff = renderAsPackedInt(pixelsId, z, t);
		for(int x = 0 ; x < width ; x++)
			for(int y = 0 ; y < height ; y++)
			{
				int offset = width*y+x;
				Color col = new Color(buff[offset]);
				data[x][y][0] = col.getRed();
				data[x][y][1] = col.getGreen();
				data[x][y][2] = col.getBlue();
			}
		return data;
	}

	public synchronized int[] renderAsPackedIntAsRGBA(long pixelsId, int z, int t)
			throws ServerError 
	{
		return null;
	}

	
}


