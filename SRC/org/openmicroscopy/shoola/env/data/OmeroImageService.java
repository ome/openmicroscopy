/*
 * org.openmicroscopy.shoola.env.data.OmeroImageService
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



//Java imports
import java.awt.image.BufferedImage;

//Third-party libraries

//Application-internal dependencies
import ome.model.core.Pixels;
import ome.model.core.PixelsDimensions;
import omeis.providers.re.data.PlaneDef;
import org.openmicroscopy.shoola.env.rnd.RenderingControl;
import org.openmicroscopy.shoola.env.rnd.RenderingServiceException;

/** 
 * List of methods to view images or thumbnails.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since OME2.2
 */
public interface OmeroImageService
{
    
    /**
     * Initializes a {@link RenderingControl} proxy for the specified pixels
     * set.
     * 
     * @param pixelsID The ID of the pixels set.
     * @return See above
     * @throws DSOutOfServiceException  If the connection is broken, or logged
     *                                  in.
     * @throws DSAccessException        If an error occured while trying to 
     *                                  retrieve data from OMEDS service.
     */
    public RenderingControl loadRenderingControl(long pixelsID)
        throws DSOutOfServiceException, DSAccessException;
    
    /**
     * Renders the specified 2D-plane. 
     * 
     * @param pixelsID  The ID of the pixels set.
     * @param pd        The plane to render.
     * @return          The buffered image representing the plane.
     * @throws RenderingServiceException If the server cannot render the image.
     */
    public BufferedImage renderImage(long pixelsID, PlaneDef pd)
        throws RenderingServiceException;    
    
    /**
     * Renders the specified 2D-plane. In that case, the plane is set 
     * to the default z-section and default timepoint.
     * 
     * @param pixelsID  The ID of the pixels set.
     * @return          The buffered image representing the 2D-plane.
     * @throws RenderingServiceException If the server cannot render the image.
     */
    public BufferedImage renderImage(long pixelsID)
        throws RenderingServiceException; 
    
    /**
     * Shuts downs the rendering service attached to the specified 
     * pixels set.
     *
     * @param pixelsID  The ID of the pixels set.
     */
    public void shutDown(long pixelsID);
    
    /**
     * Returns a thumbnail of the currently selected 2D-plane for the
     * passed pixels set.
     * 
     * @param pixelsID  The id of the pixels set.
     * @param sizeX     The width of the thumbnail.
     * @param sizeY     The height of the thumnail.
     * @return See above.
     * @throws RenderingServiceException    If the server cannot retrieve the 
     *                                      thumbnail.
     */
    public BufferedImage getThumbnail(long pixelsID, int sizeX, int sizeY)
        throws RenderingServiceException;
    
    /**
     * Reloads the rendering engine for the passed set of pixels.
     * 
     * @param pixelsID The id of the pixels set.
     */
    public void reloadRenderingService(long pixelsID);
    
    /**
     * Loads the dimensions in microns of the pixels set.
     * 
     * @param pixelsID The id of the pixels set.
     * @return See above
     * @throws DSOutOfServiceException  If the connection is broken, or logged
     *                                  in.
     * @throws DSAccessException        If an error occured while trying to 
     *                                  retrieve data from OMEDS service.
     */
    public PixelsDimensions loadPixelsDimensions(long pixelsID)
    	throws DSOutOfServiceException, DSAccessException;
    
    /**
     * Loads the pixels set.
     * 
     * @param pixelsID The id of the pixels set.
     * @return See above
     * @throws DSOutOfServiceException  If the connection is broken, or logged
     *                                  in.
     * @throws DSAccessException        If an error occured while trying to 
     *                                  retrieve data from OMEDS service.
     */
    public Pixels loadPixels(long pixelsID)
    	throws DSOutOfServiceException, DSAccessException;
    
}
