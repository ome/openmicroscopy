/*
 * org.openmicroscopy.shoola.agents.browser.layout.LayoutComparator
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

import java.util.Comparator;

import org.openmicroscopy.shoola.agents.browser.images.Thumbnail;

/**
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
public abstract class LayoutComparator implements Comparator
{
    /**
     * overrides the Comparator method to sort thumbnails.
     * 
     * @param o1 The first object to compare.
     * @param o2 The second object to compare.
     * @return see compareThumbnails.
     */
    public int compare(Object o1, Object o2)
    {
        if (o1 instanceof Thumbnail && o2 instanceof Thumbnail)
        {
            return compareThumbnails((Thumbnail) o1, (Thumbnail) o2);
        }
        else
            return 0;
    }

    /**
     * Returns whether or not a thumbnail should be laid out before another
     * thumbnail. If the first should be placed before the second, this method
     * returns -1.  If they are placed at the same time (which hopefully will
     * only happen if the two thumbnails are the same), this method returns 0.  If
     * the first should be placed after the second, this method will return 1.
     * 
     * @param t1 The first thumbnail to compare.
     * @param t2 The second thumbnail to compare.
     * @return See above.
     * @see java.util.Comparator#compare(java.lang.Object,java.lang.Object)
     */
    public abstract int compareThumbnails(Thumbnail t1, Thumbnail t2);
}
