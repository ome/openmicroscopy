/*
 * org.openmicroscopy.shoola.env.data.OmeroImageServiceImpl
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
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

package org.openmicroscopy.shoola.env.data;


//Java import
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.imageio.ImageIO;

//Third-party libraries

//Application-internal dependencies
import ome.model.core.Pixels;
import ome.model.core.PixelsDimensions;
import ome.model.display.RenderingDef;
import omeis.providers.re.RenderingEngine;
import omeis.providers.re.data.PlaneDef;
import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.login.UserCredentials;
import org.openmicroscopy.shoola.env.rnd.RenderingControl;
import org.openmicroscopy.shoola.env.rnd.RenderingServiceException;
import org.openmicroscopy.shoola.env.rnd.PixelsServicesFactory;

import pojos.ExperimenterData;


/** 
* Implementation of the {@link OmeroImageService} I/F.
*
* @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
* 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
* @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
* 	<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
* @version 3.0
* <small>
* (<b>Internal version:</b> $Revision: $ $Date: $)
* </small>
* @since OME3.0
*/
class OmeroImageServiceImpl
 	implements OmeroImageService
{

	/** Uses it to gain access to the container's services. */
	private Registry                context;

	/** Reference to the entry point to access the <i>OMERO</i> services. */
	private OMEROGateway            gateway;

	/**
	 * Creates a <code>BufferedImage</code> from the passed array of bytes.
	 * 
	 * @param values    The array of bytes.
	 * @return See above.
	 * @throws RenderingServiceException If we cannot create an image.
	 */
	private BufferedImage createImage(byte[] values) 
		throws RenderingServiceException
	{
		try {
			ByteArrayInputStream stream = new ByteArrayInputStream(values);
			return ImageIO.read(stream);
		} catch (Exception e) {
			throw new RenderingServiceException("Cannot create buffered image",
					e);
		}
	}

	/**
	 * Creates a new instance.
	 * 
	 * @param gateway   Reference to the OMERO entry point.
	 *                  Mustn't be <code>null</code>.
	 * @param registry  Reference to the registry. Mustn't be <code>null</code>.
	 */
	OmeroImageServiceImpl(OMEROGateway gateway, Registry registry)
	{
		if (registry == null)
			throw new IllegalArgumentException("No registry.");
		if (gateway == null)
			throw new IllegalArgumentException("No gateway.");
		context = registry;
		this.gateway = gateway;
	}

	/** Shuts down all active rendering engines. */
	void shutDown()
	{
		PixelsServicesFactory.shutDownRenderingControls(context);
	}

	/** 
	 * Implemented as specified by {@link OmeroImageService}. 
	 * @see OmeroImageService#loadRenderingControl(long)
	 */
	public RenderingControl loadRenderingControl(long pixelsID)
		throws DSOutOfServiceException, DSAccessException
	{
		RenderingControl proxy = 
			PixelsServicesFactory.getRenderingControl(context, 
					new Long(pixelsID));
		if (proxy == null) {
			UserCredentials uc = 
				(UserCredentials) context.lookup(LookupNames.USER_CREDENTIALS);
			int compressionLevel;
			switch (uc.getSpeedLevel()) {
				case UserCredentials.MEDIUM:
					compressionLevel = RenderingControl.MEDIUM;
					break;
				case UserCredentials.LOW:
					compressionLevel = RenderingControl.LOW;
					break;
				default:
					compressionLevel = RenderingControl.UNCOMPRESSED;
			}
			ExperimenterData exp = (ExperimenterData) context.lookup(
					LookupNames.CURRENT_USER_DETAILS);
			RenderingEngine re = gateway.createRenderingEngine(pixelsID);
			PixelsDimensions pixDims = gateway.getPixelsDimensions(pixelsID);
			RenderingDef def = gateway.getRenderingDef(pixelsID, exp.getId());
			List l = context.getDataService().getChannelsMetadata(pixelsID);
			proxy = PixelsServicesFactory.createRenderingControl(context, re,
					pixDims, l, compressionLevel, def);
		}
		return proxy;
	}

	/** 
	 * Implemented as specified by {@link OmeroImageService}. 
	 * @see OmeroImageService#renderImage(long, PlaneDef)
	 */
	public BufferedImage renderImage(long pixelsID, PlaneDef pDef)
		throws RenderingServiceException
	{
		try {
			return PixelsServicesFactory.render(context, new Long(pixelsID), 
					pDef);
		} catch (Exception e) {
			throw new RenderingServiceException("RenderImage", e);
		}
	}

	/** 
	 * Implemented as specified by {@link OmeroImageService}. 
	 * @see OmeroImageService#shutDown(long)
	 */
	public void shutDown(long pixelsID)
	{
		PixelsServicesFactory.shutDownRenderingControl(context, pixelsID);
	}

	/** 
	 * Implemented as specified by {@link OmeroImageService}. 
	 * @see OmeroImageService#getThumbnail(long, int, int)
	 */
	public BufferedImage getThumbnail(long pixID, int sizeX, int sizeY)
		throws RenderingServiceException
	{
		try {
			return createImage(gateway.getThumbnail(pixID, sizeX, sizeY));
		} catch (Exception e) {
			if (e instanceof DSOutOfServiceException) {
				context.getLogger().error(this, e.getMessage());
				return getThumbnail(pixID, sizeX, sizeY);
			}
			throw new RenderingServiceException("Get Thumbnail", e);
		}
	}

	/** 
	 * Implemented as specified by {@link OmeroImageService}. 
	 * @see OmeroImageService#reloadRenderingService(long)
	 */
	public RenderingControl reloadRenderingService(long pixelsID)
		throws RenderingServiceException
	{
		RenderingControl proxy = 
			PixelsServicesFactory.getRenderingControl(context, 
					new Long(pixelsID));
		if (proxy == null) return null;
		try {
			RenderingEngine re = gateway.createRenderingEngine(pixelsID);
			return PixelsServicesFactory.reloadRenderingControl(context, 
					pixelsID, re);
		} catch (Exception e) {
			throw new RenderingServiceException("Cannot restart the " +
					"rendering engine for : "+pixelsID, e);
		}
	}
	
	/** 
	 * Implemented as specified by {@link OmeroImageService}. 
	 * @see OmeroImageService#resetRenderingService(long)
	 */
	public RenderingControl resetRenderingService(long pixelsID)
		throws RenderingServiceException
	{
		RenderingControl proxy = 
			PixelsServicesFactory.getRenderingControl(context, 
					new Long(pixelsID));
		if (proxy == null) return null;
		try {
			RenderingEngine re = gateway.createRenderingEngine(pixelsID);
			ExperimenterData exp = (ExperimenterData) context.lookup(
					LookupNames.CURRENT_USER_DETAILS);
			RenderingDef def = gateway.getRenderingDef(pixelsID, exp.getId());
			return PixelsServicesFactory.resetRenderingControl(context, 
					pixelsID, re, def);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RenderingServiceException("Cannot restart the " +
					"rendering engine for : "+pixelsID, e);
		}
	}

	/** 
	 * Implemented as specified by {@link OmeroImageService}. 
	 * @see OmeroImageService#loadPixelsDimensions(long)
	 */
	public PixelsDimensions loadPixelsDimensions(long pixelsID) 
		throws DSOutOfServiceException, DSAccessException
	{
		return gateway.getPixelsDimensions(pixelsID);
	}

	/** 
	 * Implemented as specified by {@link OmeroImageService}. 
	 * @see OmeroImageService#loadPixels(long)
	 */
	public Pixels loadPixels(long pixelsID)
		throws DSOutOfServiceException, DSAccessException
	{
		return gateway.getPixels(pixelsID);
	}

	/** 
	 * Implemented as specified by {@link OmeroImageService}. 
	 * @see OmeroImageService#getThumbnailByLongestSide(long, int)
	 */
	public BufferedImage getThumbnailByLongestSide(long pixelsID, int maxLength) 
		throws RenderingServiceException 
	{
		try {
			return createImage(gateway.getThumbnailByLongestSide(pixelsID, 
					maxLength));
		} catch (Exception e) {
			if (e instanceof DSOutOfServiceException) {
				context.getLogger().error(this, e.getMessage());
				return getThumbnailByLongestSide(pixelsID, maxLength);
			}
			throw new RenderingServiceException("Get Thumbnail", e);
		}
	}

	/** 
	 * Implemented as specified by {@link OmeroImageService}. 
	 * @see OmeroImageService#getPlane(long, int, int, int)
	 */
	public byte[] getPlane(long pixelsID, int z, int t, int c)
		throws DSOutOfServiceException, DSAccessException
	{
		return gateway.getPlane(pixelsID, z, t, c);
	}

	/** 
	 * Implemented as specified by {@link OmeroImageService}. 
	 * @see OmeroImageService#pasteRenderingSettings(long, Class, List)
	 */
	public Map pasteRenderingSettings(long pixelsID, Class rootNodeType, 
			Set nodesID) 
		throws DSOutOfServiceException, DSAccessException 
	{
		if (nodesID == null || nodesID.size() == 0)
			throw new IllegalArgumentException("No nodes specified.");
		return gateway.pasteRenderingSettings(pixelsID, rootNodeType, nodesID);
	}

	/** 
	 * Implemented as specified by {@link OmeroImageService}. 
	 * @see OmeroImageService#resetRenderingSettings(Class, List)
	 */
	public Map resetRenderingSettings(Class rootNodeType, Set nodesID) 
		throws DSOutOfServiceException, DSAccessException 
	{
		if (nodesID == null || nodesID.size() == 0)
			throw new IllegalArgumentException("No nodes specified.");
		return gateway.resetRenderingSettings(rootNodeType, nodesID);
	}

	/** 
	 * Implemented as specified by {@link OmeroImageService}. 
	 * @see OmeroImageService#getRenderingSettings(long)
	 */
	public Map getRenderingSettings(long pixelsID) 
		throws DSOutOfServiceException, DSAccessException
	{
		Map m = gateway.getRenderingSettings(pixelsID);
		if (m == null) return null;
		Iterator i = m.keySet().iterator();
		Object key;
		Map results = new HashMap(m.size());
		while (i.hasNext()) {
			key = i.next();
			results.put(key, 
					PixelsServicesFactory.convert((RenderingDef) m.get(key)));
		}
		return results;
	}

	
}
