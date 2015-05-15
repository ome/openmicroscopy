/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2015 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.env.event;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Notification that the thumbnails of the specified images have to
 * be reloaded.
 * 
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 * @since 5.1
 */

public class ReloadThumbsEvent extends RequestEvent {

    /** The list of images ids */
    private List<Long> imageIds = new ArrayList<Long>();

    /**
     * Create a new instance for a single image id
     * @param imageId The image id
     */
    public ReloadThumbsEvent(long imageId) {
        this(Arrays.asList(imageId));
    }
    
    /**
     * Creates a new instance for a list of images ids
     * @param imageIds The image ids
     */
    public ReloadThumbsEvent(List<Long> imageIds) {
        super();
        this.imageIds = imageIds;
    }

    /**
     * Get the image ids to reload the thumbnails for
     * @return See above.
     */
    public List<Long> getImageIds() {
        return imageIds;
    }

    /**
     * Set the image ids to reload the thumbnails for
     * @param imageIds The image ids
     */
    public void setImageIds(List<Long> imageIds) {
        this.imageIds = imageIds;
    }
    
}
