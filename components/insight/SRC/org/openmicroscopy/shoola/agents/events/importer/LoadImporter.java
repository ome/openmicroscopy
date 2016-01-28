/*
 * org.openmicroscopy.shoola.agents.events.importer.LoadImporter 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2013 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.agents.events.importer;

import java.util.List;
import java.util.Map;

import org.openmicroscopy.shoola.agents.util.browser.TreeImageDisplay;
import org.openmicroscopy.shoola.env.event.RequestEvent;


/** 
 * Event to bring the Importer's chooser.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since 3.0-Beta4
 */
public class LoadImporter
    extends RequestEvent
{

    /** The currently selected container or <code>null</code>. */
    private TreeImageDisplay selectedContainer;

    /** The objects used for selection. */
    private Map<Long, Map<Long, List<TreeImageDisplay>>> objects;

    /** The type of the import to handle. */
    private int type;

    /** The group context.*/
    private long groupId;

    /** The id of the user to import as or <code>-1</code>.*/
    private long userId;

    /**
     *  Creates a new instance.
     * 
     * @param selectedContainer The selected container.
     * @param type The of import.
     */
    public LoadImporter(TreeImageDisplay selectedContainer, int type)
    {
        this.selectedContainer = selectedContainer;
        groupId = -1;
        userId = -1;
        this.type = type;
    }

    /**
     * Sets the identifier of the user.
     * 
     * @param userId The id of the user.
     */
    public void setUser(long userId) { this.userId = userId; }

    /**
     * Returns the id of the user.
     * 
     * @return See above.
     */
    public long getUser() { return userId; }

    /**
     * Sets the identifier of the group.
     * 
     * @param groupId The id of the group.
     */
    public void setGroup(long groupId) { this.groupId = groupId; }

    /**
     * Returns the id of the group.
     * 
     * @return See above.
     */
    public long getGroup() { return groupId; }

    /**
     * Returns the container.
     * 
     * @return See above.
     */
    public TreeImageDisplay getSelectedContainer() { return selectedContainer; }

    /**
     * Returns the type of import.
     * 
     * @return See above.
     */
    public int getType() { return type; }

    /**
     * Returns the top nodes.
     * 
     * @return See above.
     */
    public Map<Long, Map<Long, List<TreeImageDisplay>>> getObjects()
    {
        return objects;
    }

    /**
     * Returns the top nodes.
     * 
     * @param objects The values to set.
     */
    public void setObjects(Map<Long, Map<Long, List<TreeImageDisplay>>> objects)
    {
        this.objects = objects;
    }

}
