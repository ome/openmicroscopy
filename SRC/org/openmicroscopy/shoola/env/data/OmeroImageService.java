/*
 * org.openmicroscopy.shoola.env.data.OmeroImageService
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



//Java imports
import java.awt.image.BufferedImage;

//Third-party libraries

//Application-internal dependencies
import omeis.providers.re.data.PlaneDef;

import org.openmicroscopy.shoola.env.rnd.RenderingControl;
import org.openmicroscopy.shoola.env.rnd.RenderingServiceException;

import pojos.PixelsData;

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
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
     * @param pix   The pixels data object hosting the pixels set.
     * @param sizeX The width of the thumbnail.
     * @param sizeY The height of the thumnail.
     * @return See above.
     * @throws RenderingServiceException    If the server cannot retrieve the 
     *                                      thumbnail.
     */
    public BufferedImage getThumbnail(PixelsData pix, int sizeX, int sizeY)
        throws RenderingServiceException;
    
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
    
}
