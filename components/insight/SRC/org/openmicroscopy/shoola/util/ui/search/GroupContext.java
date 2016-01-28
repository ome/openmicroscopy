/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2014 University of Dundee & Open Microscopy Environment.
 *  All rights reserved.
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
package org.openmicroscopy.shoola.util.ui.search;

import java.util.ArrayList;
import java.util.List;

import omero.gateway.model.GroupData;
import omero.gateway.model.ExperimenterData;

import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * Host information about the group to search into.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since Beta4.4
 */
public class GroupContext
{
    /** Maximum characters to use for the name */
    private static final int MAX_CHARS = 25;

    /** ID indicating all groups should be included in the search */
    public static final int ALL_GROUPS_ID = Integer.MAX_VALUE;

    /** The group to handle.*/
    private String group;

    /** The identifier of the group.*/
    private long id;

    /** The experimenters of this group */
    private List<ExperimenterContext> experimenters = new ArrayList<ExperimenterContext>();

    /**
     * Creates a new instance.
     * 
     * @param group The name of the group to handle.
     * @param id The identifier of the group.
     */
    public GroupContext(String group, long id)
    {
        if (group.length() > MAX_CHARS) {
            group = UIUtilities.truncate(group, MAX_CHARS, false);
        }

        this.group = group;
        this.id = id;
    }

    /**
     * Creates a new instance.
     * 
     * @param group The name of the group to handle.
     */
    public GroupContext(GroupData group)
    {
        String groupName = group.getName();

        if(groupName.length()>MAX_CHARS) {
            groupName = UIUtilities.truncate(groupName, MAX_CHARS, false);
        }

        this.group = groupName;
        this.id = group.getId();

        for(Object exp : group.getExperimenters()) {
            this.experimenters.add(new ExperimenterContext((ExperimenterData)exp));
        }
    }

    /**
     * Returns the id of the group hosted by the component.
     * 
     * @return See above.
     */
    public long getId() { return id; }


    /**
     * Get this group's experimenters
     */
    public List<ExperimenterContext> getExperimenters() {
        return experimenters;
    }

    /**
     * Set this group's experimenters
     */
    public void setExperimenters(List<ExperimenterContext> experimenters) {
        this.experimenters = experimenters;
    }

    /**
     * Overridden to return the name of the group.
     * @see Object#toString()
     */
    public String toString() { return group; }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (id ^ (id >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        GroupContext other = (GroupContext) obj;
        if (id != other.id)
            return false;
        return true;
    }

}
