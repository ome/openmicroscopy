/*
 * org.openmicroscopy.shoola.agents.events.iviewer.RndSettingsSaved 
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
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.agents.events.iviewer;

import org.openmicroscopy.shoola.env.event.RequestEvent;
import org.openmicroscopy.shoola.env.rnd.RndProxyDef;

/** 
 * Event posted when the rendering settings have been changed.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since 5.2.0
 */
public class RndSettingsChanged
    extends RequestEvent
{

    /** The identifier of the image. */
    private long imageID;

    /**
     * Creates a new instance.
     *
     * @param imageID The identifier of the image.
     */
    public RndSettingsChanged(long imageID)
    {
        this.imageID = imageID;
    }

    /**
     * Returns the id of the image.
     *
     * @return See above.
     */
    public long getImageID() { return imageID; }

}
