/*
 * org.openmicroscopy.shoola.agents.browser.ThumbnailDataLoader
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

import org.openmicroscopy.shoola.agents.browser.images.*;
import org.openmicroscopy.shoola.env.data.model.ImageData;

/**
 * Loads individual image data from the data server, including any
 * associated Semantic Types.  Creates a ThumbnailDataModel.
 * 
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
public class ThumbnailDataLoader
{
    private static ThumbnailDataLoader loader;
    
    private ThumbnailDataLoader() // may need to get Registry on construction
    {
        // do any initialization commands (getting STS from Registry, etc.)
    }
    
    /**
     * Initializes the thumbnail data loader.
     */
    public static ThumbnailDataLoader getInstance()
    {
        if(loader == null)
        {
            loader = new ThumbnailDataLoader();
        }
        return loader;
    }
    
    /**
     * Access the SemanticTypeManager and SemanticTypeService in order to
     * obtain image data from the image and place it in the ThumbnailDataModel.
     * This could take a while.
     * 
     * @param image
     * @param tdm
     */
    public ThumbnailDataModel loadDataFrom(ImageData imageData)
    {
        // TODO: wait until STS is done to fill in information
        // establish what TDM should have in terms if image fields
        // (just use ImageData object, wrap semantic types in TDM?)
        return new ThumbnailDataModel(imageData);
    }
}
