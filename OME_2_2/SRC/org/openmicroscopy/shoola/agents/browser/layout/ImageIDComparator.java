/*
 * org.openmicroscopy.shoola.agents.browser.layout.ImageIDComparator
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

import org.openmicroscopy.shoola.agents.browser.images.Thumbnail;

/**
 * Compares all thumbnails by ID.  When used with a sort method, orders
 * images by image ID.
 * 
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
public class ImageIDComparator extends LayoutComparator
{
    /**
     * Compares the IDs of the two thumbnails.
     */
    public int compareThumbnails(Thumbnail t1, Thumbnail t2)
    {
        // null: undefined behavior (maybe tighten this up)
        if(t1 == null || t2 == null)
        {
            throw new NullPointerException();
        }
        int ID1 = t1.getBaseID();
        int ID2 = t2.getBaseID();
        
        if(ID1 < ID2)
        {
            return -1;
        }
        else if(ID1 == ID2)
        {
            return 0;
        }
        else
        {
            return 1;
        }
    }
}
