/*
 * org.openmicroscopy.shoola.agents.browser.images.OverlayMethod
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
package org.openmicroscopy.shoola.agents.browser.images;

import edu.umd.cs.piccolo.util.PPaintContext;

/**
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
public interface OverlayMethod
{
    /**
     * Display (add/remove) the overlay node.
     * @param t The thumbnail to display against.
     * @param context The paint context to display against.
     * @param offset The offset of the display node.
     */
    public void display(Thumbnail t, PPaintContext context);
    
    /**
     * Determines whether or not the overlay node should be displayed,
     * based on the condition of the thumbnail and the context of the
     * graphical space.
     * @param t The thumbnail to check against.
     * @param context The graphical context to check against.
     * @return Whether or not the overlay node should be added atop the
     *         thumbnail.
     */
    public boolean displayCondition(Thumbnail t, PPaintContext context);
    
    /**
     * Returns the node display.
     * @return
     */
    public String getDisplayNodeType();
}
