/*
 * org.openmicroscopy.shoola.agents.browser.ThumbnailImageLoader
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

/*------------------------------------------------------------------------------
 *
 * Written by:    Jeff Mellen <jeffm@alum.mit.edu>
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.agents.browser;

import java.awt.image.BufferedImage;

import org.openmicroscopy.is.CompositingSettings;
import org.openmicroscopy.shoola.env.data.model.ImageSummary;

/**
 * For each Image, returns a BufferedImage from the ImageServer with the
 * specified compositing settings.
 * 
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
public class ThumbnailImageLoader
{
    private static ThumbnailImageLoader loader;
    // include variables to get STS reference
    
    private ThumbnailImageLoader()
    {
        // include code to access STS and get necessary objects
        // (may require Registry as parameter?  not exactly sure yet)
    }
    
    /**
     * Returns the instance of the ThumbnailImageLoader.
     * @return See above.
     */
    public static ThumbnailImageLoader getInstance()
    {
        if(loader == null)
        {
            loader = new ThumbnailImageLoader();
        }
        return loader;
    }
    
    /**
     * For the specified Image and using the specified CompositingSettings,
     * return the BufferedImage from the image server to get the desired
     * thumbnail.  If CompositingSettings is null, this will call the
     * getThumb() method, which will return the default thumbnail for the
     * specified Image.  If the DTO is null, this method will return null.
     * 
     * @param imageData The image from which to grab the pixels.
     * @param settings The compositing/size settings for the desired thumbnail.
     * @return See above.
     */
    public BufferedImage getImage(ImageSummary imageData,
                                  CompositingSettings settings)
    {
        // TODO fix
        return null;
    }
}
