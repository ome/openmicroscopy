/*
 * org.openmicroscopy.shoola.agents.browser.layout.LayoutMethod
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
package org.openmicroscopy.shoola.agents.browser.layout;

import java.awt.geom.Point2D;
import java.util.Map;

import org.openmicroscopy.shoola.agents.browser.images.Thumbnail;

/**
 * Interface for a method that lays out thumbnails.
 * 
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since 2.2
 */
public interface LayoutMethod
{
    /**
     * Returns the position of the thumbnail in its relative container.  This
     * is appropriate if the layout method maintains some model state.
     * 
     * @param t The thumbnail for which to determine the position.
     * @return The assigned position of the thumbnail.
     */
    public Point2D getAnchorPoint(Thumbnail t);

    /**
     * Returns a map with the thumbnail keys mapped to their specified
     * positions.  This is good if the layout method has no state, and the
     * layout is computed at runtime.
     * 
     * @param ts The array of thumbnails to place.
     * @return A map mapping the thumbnails to their specified locations.
     */
    public Map getAnchorPoints(Thumbnail[] ts);
}
