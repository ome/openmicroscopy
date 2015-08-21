/*
 * org.openmicroscopy.shoola.env.data.views.calls.SwitchUserLoader 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2010 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
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
package org.openmicroscopy.shoola.env.data.views.calls;

import org.openmicroscopy.shoola.env.Agent;
import org.openmicroscopy.shoola.env.data.util.AgentSaveInfo;
import org.openmicroscopy.shoola.env.data.views.BatchCall;
import org.openmicroscopy.shoola.env.data.views.BatchCallTree;
import pojos.ExperimenterData;


/**
 * Saves data before switching user's group.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class SwitchUserGroupLoader
extends BatchCallTree
{

    /** The experimenter to handle. */
    private ExperimenterData experimenter;

    /** The partial result. */
    private Object result;

    /** Switches the user group. */
    private void switchUserGroup()
    {
        try {
            //context.getAdminService().changeExperimenterGroup(experimenter, 
            //		groupID);
        } catch (Exception e) {
            context.getLogger().error(this, 
                    "Cannot switch user's group: "+e.getMessage());
        }
        result = experimenter;
    }

    /**
     * Adds a {@link BatchCall} to the tree for each Agent.
     * @see BatchCallTree#buildTree()
     */
    protected void buildTree()
    {
        String description = "Switching the user's group.";
        add(new BatchCall(description) {
            public void doCall() { 
                switchUserGroup();
            }
        }); 
    }

    /**
     * Returns the result.
     * @see BatchCallTree#getPartialResult()
     */
    protected Object getPartialResult() { return result; }

    /**
     * Returns <code>null</code> as there's no final result.
     * @see BatchCallTree#getResult()
     */
    protected Object getResult() { return null; }

    /**
     * Creates a new instance.
     * 
     * @param experimenter The experimenter to handle.
     * @param groupID The identifier of the group.
     */
    public SwitchUserGroupLoader(ExperimenterData experimenter, long groupID)
    {
        if (experimenter == null)
            throw new IllegalArgumentException();
        this.experimenter = experimenter;
        this.groupID = groupID;
    }

}
