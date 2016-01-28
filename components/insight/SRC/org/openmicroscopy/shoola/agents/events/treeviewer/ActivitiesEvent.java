/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2014 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.events.treeviewer;


//Java imports
import omero.gateway.SecurityContext;
import org.openmicroscopy.shoola.env.event.RequestEvent;

/**
 * Events posted to check if we still have some on-going event e.g. import
 * before removing the group from the display and shutting it down.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since 5.0
 */
public class ActivitiesEvent
    extends RequestEvent
{

    /** The security context to remove.*/
    private SecurityContext ctx;

    /**
     * Creates a new instance.
     * 
     * @param ctx The context to remove.
     */
    public ActivitiesEvent(SecurityContext ctx)
    {
        this.ctx = ctx;
    }

    /**
     * Returns the security context.
     * 
     * @return See above.
     */
    public SecurityContext getContext() { return ctx; }
    
}
