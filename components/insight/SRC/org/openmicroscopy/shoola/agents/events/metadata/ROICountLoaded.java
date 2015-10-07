/*
 * org.openmicroscopy.shoola.agents.events.FocusGainedEvent 
 *
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
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.agents.events.metadata;

import org.openmicroscopy.shoola.env.event.AgentEvent;

/**
 * Event indicating that the ROI count for a specific image has been loaded.
 * 
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 */
public class ROICountLoaded extends AgentEvent {

    /** The image id */
    private long imageId = -1;

    /** The number of ROIs the image has */
    private int roiCount = -1;

    /**
     * Creates a new instance
     * 
     * @param imageId
     *            The image id
     * @param roiCount
     *            The number of ROIs the image has
     */
    public ROICountLoaded(long imageId, int roiCount) {
        this.imageId = imageId;
        this.roiCount = roiCount;
    }

    /**
     * Get the number of ROIs the image has
     * 
     * @return See above
     */
    public int getRoiCount() {
        return roiCount;
    }

    /**
     * Get the image id
     * 
     * @return See above
     */
    public long getImageId() {
        return imageId;
    }

}
