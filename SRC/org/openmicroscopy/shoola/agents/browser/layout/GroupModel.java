/*
 * org.openmicroscopy.shoola.agents.browser.layout.GroupModel
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

import java.util.*;

import org.openmicroscopy.shoola.agents.browser.images.Thumbnail;

/**
 * Specifies a region that is a subspace of the canvas, and contains a
 * certain number of thumbnails that correspond to a particular category or
 * criteria.
 * 
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
public class GroupModel
{
    private Set thumbnails;
    private String groupName;

    private void init()
    {
        thumbnails = new HashSet();
    }

    /**
     * Creates a group model with the specified name.
     * @param name The name of the group.
     */
    public GroupModel(String name)
    {
        init();
        // set default layout method
        this.groupName = name;
    }

    /**
     * Adds a thumbnail to the group.
     * 
     * @param t The thumbnail to add.
     */
    public void addThumbnail(Thumbnail t)
    {
        if (t != null)
        {
            thumbnails.add(t);
        }
    }

    /**
     * Removes a thumbnail from the group.
     * 
     * @param t The thumbnail to remove.
     */
    public void removeThumbnail(Thumbnail t)
    {
        if (thumbnails.contains(t))
        {
            thumbnails.remove(t);
        }
    }
    
    /**
     * Returns whether or not this group model contains this particular
     * thumbnail.
     * @param t The thumbnail to determine membership
     * @return Whether or not this thumbnail is a member of this group.
     */
    public boolean containsThumbnail(Thumbnail t)
    {
        return thumbnails.contains(t);
    }
    
    /**
     * Removes all thumbnails from this group (effectively resets it)
     */
    public void clearThumbnails()
    {
        thumbnails.clear();
    }

    /**
     * Gets the name of the group.
     * 
     * @return The name of the group.
     */
    public String getName()
    {
        return groupName;
    }

    /**
     * Sets the name of the group to the specified name.
     * 
     * @param name The new name of the group.
     */
    public void setName(String name)
    {
        this.groupName = name;
    }

    /**
     * Gets an unmodifiable set of thumbnails from this group.
     * @return See above.
     */
    public Set getThumbnails()
    {
        return Collections.unmodifiableSet(thumbnails);
    }
}
