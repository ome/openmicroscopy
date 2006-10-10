/*
 * org.openmicroscopy.shoola.env.data.OmeroImageServiceImpl
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2004 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */

package org.openmicroscopy.shoola.env.data;


//Java import
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import javax.imageio.ImageIO;

//Third-party libraries

//Application-internal dependencies
import ome.model.core.PixelsDimensions;
import omeis.providers.re.RenderingEngine;
import omeis.providers.re.data.PlaneDef;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.log.LogMessage;
import org.openmicroscopy.shoola.env.log.Logger;
import org.openmicroscopy.shoola.env.rnd.RenderingControl;
import org.openmicroscopy.shoola.env.rnd.RenderingServiceException;
import org.openmicroscopy.shoola.env.rnd.RenderingServicesFactory;
import pojos.PixelsData;


/** 
 * Implementation of the {@link OmeroImageService} I/F.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
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
            proxy = RenderingServicesFactory.createRenderingControl(context, re,
                                                    pixDims);
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
            RenderingControl proxy = 
                RenderingServicesFactory.getRenderingControl(context, 
                                                        new Long(pixelsID));
            if (proxy == null) 
                throw new RuntimeException("No rendering service " +
                        "initialized for the specified pixels set.");
            return proxy.render(pDef);
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
     * @see OmeroImageService#getThumbnail(PixelsData, int, int)
     */
    public BufferedImage getThumbnail(PixelsData pix, int sizeX, int sizeY)
        throws RenderingServiceException
    {
        try {
            LogMessage msg = new LogMessage();
            msg.println("Get thumbnail direct, PixelsID: "+pix.getId());
            Logger logger = context.getLogger();
            logger.debug(this, msg);
            byte[] v = gateway.getThumbnail(pix.asPixels(), sizeX, sizeY);
            return createImage(v);
        } catch (Exception e) {
            throw new RenderingServiceException("Get Thumbnail", e);
        }
    }
    
    /** 
     * Implemented as specified by {@link OmeroImageService}. 
     * @see OmeroImageService#getThumbnail(long, int, int)
     */
    public BufferedImage getThumbnail(long pixID, int sizeX, int sizeY)
        throws RenderingServiceException
    {
        try {
            LogMessage msg = new LogMessage();
            msg.println("Get thumbnail direct from ID, PixelsID: "+pixID);
            Logger logger = context.getLogger();
            logger.debug(this, msg);
            byte[] v = gateway.getThumbnail(pixID, sizeX, sizeY);
            return createImage(v);
        } catch (Exception e) {
            throw new RenderingServiceException("Get Thumbnail", e);
        }
    }
    
    /** Shuts down all active rendering engines. */
    void shutDown()
    {
        RenderingServicesFactory.shutDownRenderingControls(context);
    }

}
