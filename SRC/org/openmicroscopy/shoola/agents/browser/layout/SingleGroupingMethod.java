/*
 * org.openmicroscopy.shoola.agents.browser.layout.SingleGroupingMethod
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
 * Defines a single grouping (such that all thumbnails belong to a single
 * group)
 * 
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
public class SingleGroupingMethod implements GroupingMethod
{
    private GroupModel singleGroup;
    
    /**
     * Constructs the single grouping method with a single backing group.
     * @param model The group to add all thumbnails to.
     */
    public SingleGroupingMethod()
        throws IllegalArgumentException
    {
        singleGroup = new GroupModel("images");
    }
    
    /**
     * Returns the single group that all thumbnails should belong to, and
     * adds the thumbnail to that group.
     * 
     * @see org.openmicroscopy.shoola.agents.browser.layout.GroupingMethod#assignGroup(org.openmicroscopy.shoola.agents.browser.images.Thumbnail)
     */
    public GroupModel assignGroup(Thumbnail t)
    {
        if(t != null)
        {
            singleGroup.addThumbnail(t);
        }
        return singleGroup;
    }
    
    /**
     * Returns the single backing group model regardless of thumbnail.
     * @return See above.
     */
    public GroupModel getGroup()
    {
        return singleGroup;
    }
    
    /**
     * Gets the (single) group that the thumbnail belongs to.  For all t, the
     * result should be the same.
     */
    public GroupModel getGroup(Thumbnail t)
    {
        return singleGroup;
    }
    
    /**
     * Returns an array with just the single available group model.
     * @return see above.
     */
    public GroupModel[] getGroups()
    {
        return new GroupModel[] { singleGroup };
    }

}
