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
package org.openmicroscopy.shoola.agents.events.hiviewer;

import omero.gateway.SecurityContext;

import org.openmicroscopy.shoola.agents.events.iviewer.ViewImageObject;
import org.openmicroscopy.shoola.env.event.RequestEvent;


/**
 * Event sent to
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since 5.1
 */
public class LaunchViewer
    extends RequestEvent
{

    /** The security context.*/
    private SecurityContext ctx;

    /** The data to open.*/
    private ViewImageObject data;

    /**
     * Creates a new instance.
     *
     * @param ctx The security context.
     * @param data The data to open.
     */
    public LaunchViewer(SecurityContext ctx, ViewImageObject data)
    {
        this.ctx = ctx;
        this.data = data;
    }

    /**
     * Returns the security context.
     *
     * @return See above.
     */
    public SecurityContext getSecurityContext() { return ctx; }

    /**
     * Returns the data.
     *
     * @return See above.
     */
    public ViewImageObject getData() { return data; }

}
