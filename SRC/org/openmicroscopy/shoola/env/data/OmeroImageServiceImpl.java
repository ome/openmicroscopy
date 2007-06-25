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
import java.util.List;
import javax.imageio.ImageIO;

//Third-party libraries

//Application-internal dependencies
import ome.model.core.Pixels;
import ome.model.core.PixelsDimensions;
import omeis.providers.re.RenderingEngine;
import omeis.providers.re.data.PlaneDef;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.rnd.RenderingControl;
import org.openmicroscopy.shoola.env.rnd.RenderingServiceException;
import org.openmicroscopy.shoola.env.rnd.RenderingServicesFactory;


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
        RenderingServicesFactory.shutDownRenderingControls(context);
    }

    /** 
     * Implemented as specified by {@link OmeroImageService}. 
     * @see OmeroImageService#loadRenderingControl(long)
     */
    public RenderingControl loadRenderingControl(long pixelsID)
            throws DSOutOfServiceException, DSAccessException
    {
        RenderingControl proxy = 
                RenderingServicesFactory.getRenderingControl(context, 
                                                new Long(pixelsID));
        if (proxy == null) {
            RenderingEngine re = gateway.createRenderingEngine(pixelsID);
            PixelsDimensions pixDims = gateway.getPixelsDimensions(pixelsID);
            List l = context.getDataService().getChannelsMetadata(pixelsID);
            proxy = RenderingServicesFactory.createRenderingControl(context, re,
                                                    pixDims, l);
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
            return RenderingServicesFactory.render(context, new Long(pixelsID), 
                                                    pDef);
        } catch (Exception e) {
            throw new RenderingServiceException("RenderImage", e);
        }
    }
    
    /** 
     * Implemented as specified by {@link OmeroImageService}. 
     * @see OmeroImageService#renderImage(long)
     */
    public BufferedImage renderImage(long pixelsID)
            throws RenderingServiceException
    {
        return renderImage(pixelsID, null);
    }

    /** 
     * Implemented as specified by {@link OmeroImageService}. 
     * @see OmeroImageService#shutDown(long)
     */
    public void shutDown(long pixelsID)
    {
        RenderingServicesFactory.shutDownRenderingControl(context, pixelsID);
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
	public void reloadRenderingService(long pixelsID)
		throws RenderingServiceException
	{
		RenderingControl proxy = 
            RenderingServicesFactory.getRenderingControl(context, 
                                            new Long(pixelsID));
		if (proxy == null) return;
		try {
			RenderingEngine re = gateway.createRenderingEngine(pixelsID);
			RenderingServicesFactory.resetRenderingControl(context, pixelsID, 
								re);
		} catch (Exception e) {
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
	
}
