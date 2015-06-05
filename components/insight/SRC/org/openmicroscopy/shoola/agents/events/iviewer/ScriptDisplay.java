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
package org.openmicroscopy.shoola.agents.events.iviewer;

import java.awt.Component;
import java.awt.Point;

import org.openmicroscopy.shoola.env.event.RequestEvent;


/**
 * Event posted to load the script to run.
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since 5.1
 */
public class ScriptDisplay
    extends RequestEvent
{

    /** The component triggering the event.*/
    private Component source;

    /** The location of the mouse pressed.*/
    private Point location;

    /**
     * Creates a new instance.
     *
     * @param source The invoker.
     */
    public ScriptDisplay(Component source)
    {
        this.source = source;
        location = new Point(0, 0);
    }

    /**
     * Returns the component invoking the event.
     *
     * @return See above.
     */
    public Component getSource() { return source; }

    /**
     * Returns the location of the mouse pressed.
     *
     * @return See above.
     */
    public Point getLocation() { return location; }

}
