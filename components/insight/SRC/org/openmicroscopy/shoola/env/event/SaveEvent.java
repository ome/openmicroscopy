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

//Java imports

/**
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since 5.1
 */

public class SaveEvent
    extends RequestEvent
{

    /** Indicates to save the rois.*/
    public static final Integer ROIS = new Integer(0);

    /** Indicates to save the results.*/
    public static final Integer RESULTS = new Integer(1);

    /** Indicates to save the results.*/
    public static final Integer ALL = new Integer(2);

    /** The plugin this event is related to.*/
    private int plugin;

    /** Indicates what to save e.g. rois.*/
    private int saveIndex;

    /**
     * Creates a new instance.
     *
     * @param plugin The plugin the event is for.
     * @param saveIndex The object to save.
     */
    public SaveEvent(int plugin, int saveIndex)
    {
        this.plugin = plugin;
        this.saveIndex = saveIndex;
    }

    /**
     * Returns the plugin the event is for.
     *
     * @return See above.
     */
    public int getPlugin() { return plugin; }

    /**
     * Returns the index indicating the object to save.
     *
     * @return See above.
     */
    public int getSaveIndex() { return saveIndex; }

}
