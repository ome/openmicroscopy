/*
 * org.openmicroscopy.shoola.agents.browser.layout.AbstractOrderedLayoutMethod
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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.openmicroscopy.shoola.agents.browser.images.Thumbnail;

/**
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
public abstract class AbstractOrderedLayoutMethod implements LayoutMethod
{
    /**
     * The comparator that orders the thumbnails.
     */
    protected LayoutComparator comparator;

    /**
     * Returns the ordered list of thumbnails based on the comparator.
     * 
     * @param ts The thumbnails to order.
     * @return Those same thumbnails, placed in layout order.
     */
    protected List getThumbnailOrder(Thumbnail[] ts)
    {
        if (comparator == null)
        {
            return Arrays.asList(ts);
        }
        else
        {
            List thumbnailList = Arrays.asList(ts);
            Collections.sort(thumbnailList, comparator);
            return thumbnailList;
        }
    }

    /**
     * Constructs a layout method that orders thumbnails by the specified
     * comparison criteria.
     * 
     * @param comparator The comparator that will order the thumbnails.
     */
    protected AbstractOrderedLayoutMethod(LayoutComparator comparator)
    {
        this.comparator = comparator;
    }

}
