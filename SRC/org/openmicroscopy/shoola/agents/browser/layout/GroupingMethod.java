/*
 * org.openmicroscopy.shoola.agents.browser.layout.GroupingMethod
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
 * Interface for a method that separates thumbnails into groups.
 * 
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
public interface GroupingMethod
{
    /**
     * Assigns a particular thumbnail to a group and adds it to the GroupModel.
     * The model returned is the group model that the thumbnail was assigned to.
     * 
     * @param t The thumbnail to assign.
     * @return Which group the thumbnail was assigned to.
     */
    public GroupModel assignGroup(Thumbnail t);

    /**
     * Gets the currently assigned group of the specified thumbnail, if there
     * is one.
     * @param t The thumbnail to check membership.
     * @return Which group the thumbnail belongs to.
     */
    public GroupModel getGroup(Thumbnail t);
}
