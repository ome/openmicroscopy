/*
 * org.openmicroscopy.shoola.agents.browser.ThumbnailSourceModel
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

import java.util.*;

import org.openmicroscopy.shoola.env.data.model.ImageData;

/**
 * Specifies a mapping between image IDs and image DTOs, so that thumbnails
 * can reference their original image backing.  Should be one per browser
 * window (or even per Shoola environment, as image IDs are distinct)
 * 
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
public class ThumbnailSourceModel
{
    private Map sourceMap;

    /**
     * Creates a Thumbnail-to-data source mappping.
     */
    public ThumbnailSourceModel()
    {
        sourceMap = new HashMap();
    }

    /**
     * Adds image data to the mapping.  The image is bound to the corresponding
     * thumbnail by the image ID (that should be the same for both).  This
     * is an implied relation.
     * 
     * @param image The image to add.  Will accomplish nothing if it is null.
     */
    public void putImageData(ImageData imageData)
    {
        if(imageData != null)
        {
            sourceMap.put(new Integer(imageData.getID()),imageData);
        }
    }
    
    /**
     * Return the concrete image metadata associated with the image with
     * the specified ID.  If no mapping exists, will return null.
     * @param imageID The Image to access.
     * @return See above.
     */
    public ImageData getImageData(int imageID)
    {
        return (ImageData)sourceMap.get(new Integer(imageID));
    }
    
    /**
     * Returns a list of image IDs (integer objects), ordered by ID.
     * @return
     */
    public List getImageKeys()
    {
        List list = new ArrayList(sourceMap.keySet());
        Collections.sort(list);
        return Collections.unmodifiableList(list);
    }
}
