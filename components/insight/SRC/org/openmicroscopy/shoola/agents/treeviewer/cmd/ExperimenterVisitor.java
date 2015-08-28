/*
 * org.openmicroscopy.shoola.agents.treeviewer.cmd.ExperimenterVisitor 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2012 University of Dundee & Open Microscopy Environment.
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
package org.openmicroscopy.shoola.agents.treeviewer.cmd;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.openmicroscopy.shoola.agents.treeviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageDisplay;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageNode;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageSet;
import pojos.ExperimenterData;
import pojos.GroupData;

/** 
 * Finds the experimenters.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since Beta4.4
 */
public class ExperimenterVisitor
extends BrowserVisitor
{

    /** The nodes found.*/
    private List<TreeImageDisplay> nodes;

    /** The id of the user to find or <code>-1</code>.*/
    private long userID;

    /** The ids of the group to find or <code>-1</code>.*/
    private Collection<Long> groupIDs;

    /** Flag indicating to check only the group.*/
    private boolean groupOnly;

    /**
     * Creates a new instance.
     * 
     * @param model Reference to the {@link Browser}.
     *              Mustn't be <code>null</code>.
     * @param groupID The id of the group.
     */
    public ExperimenterVisitor(Browser model, long groupID)
    {
        super(model);
        groupOnly = true;
        groupIDs = new ArrayList<Long>();
        if (groupID >= 0) groupIDs.add(groupID);
        nodes = new ArrayList<TreeImageDisplay>();
    }

    /**
     * Creates a new instance.
     * 
     * @param model Reference to the {@link Browser}.
     *              Mustn't be <code>null</code>.
     * @param userID The id of the user.
     * @param groupID The id of the group.
     */
    public ExperimenterVisitor(Browser model, long userID, long groupID)
    {
        super(model);
        this.userID = userID;
        groupIDs = new ArrayList<Long>();
        if (groupID >= 0) groupIDs.add(groupID);
        nodes = new ArrayList<TreeImageDisplay>();
    }

    /**
     * Creates a new instance.
     * 
     * @param model Reference to the {@link Browser}.
     *              Mustn't be <code>null</code>.
     * @param userID The id of the user.
     * @param groupIDs The id of the groups.
     */
    public ExperimenterVisitor(Browser model, long userID,
            Collection<Long> groupIDs)
    {
        super(model);
        this.userID = userID;
        if (groupIDs == null) groupIDs = new ArrayList<Long>();
        this.groupIDs = groupIDs;
        nodes = new ArrayList<TreeImageDisplay>();
    }

    /** 
     * Returns the nodes.
     * 
     * @return See above.
     */
    public List<TreeImageDisplay> getNodes() { return nodes; }

    /**
     * No operation.
     * @see BrowserVisitor#visit(TreeImageNode)
     */
    public void visit(TreeImageNode node) {}

    /**
     * Retrieves the specified nodes.
     * @see BrowserVisitor#visit(TreeImageSet)
     */
    public void visit(TreeImageSet node)
    {
        Object ho = node.getUserObject();
        if (ho instanceof ExperimenterData && !groupOnly) {
            if (userID < 0) nodes.add(node);
            else {
                if (userID == ((ExperimenterData) ho).getId()) {
                    if (groupIDs.size() == 0) nodes.add(node);
                    else {
                        TreeImageDisplay parent = node.getParentDisplay();
                        Object pho = parent.getUserObject();
                        if (pho instanceof GroupData && 
                                groupIDs.contains(((GroupData) pho).getId()))
                            nodes.add(node);
                    }
                }
            }
        } else if (ho instanceof GroupData) {
            if (groupOnly) {
                if (groupIDs.contains(((GroupData) ho).getId())) {
                    nodes.add(node);
                } else {
                    if (groupIDs.size() == 0)
                        nodes.add(node);
                }
            }
        }
    }
}
