/*
 * org.openmicroscopy.shoola.agents.measurement.view.ROITable 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2016 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.measurement.util.roitable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import omero.gateway.model.FolderData;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

/**
 * Helper class for managing {@link ROINode}s
 * 
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 */
public class ROINodeMap {

    /** References to all ROINodes */
    private ListMultimap<String, ROINode> nodesMap;

    /**
     * Creates a new instance
     */
    public ROINodeMap() {
        nodesMap = ArrayListMultimap.create();
    }

    /**
     * Clears the map
     */
    public void clear() {
        nodesMap.clear();
    }

    /**
     * Adds a node to the map
     * 
     * @param node
     *            The node to add
     */
    public void add(ROINode node) {
        nodesMap.put(ROIUtil.getUUID(node.getUserObject()), node);
    }

    /**
     * Retrieve the nodes for the given object
     * 
     * @param obj
     *            The object
     * @return See above
     */
    public Collection<ROINode> get(Object obj) {
        String key = ROIUtil.getUUID(obj);
        if (key != null)
            return nodesMap.get(key);
        return Collections.EMPTY_LIST;
    }

    /**
     * Get all nodes
     * 
     * @return See above
     */
    public Collection<ROINode> values() {
        return nodesMap.values();
    }

    /**
     * Get the node for the given {@link FolderData}
     * 
     * @param folder
     *            The folder
     * @return See above
     */
    public ROINode findFolderNode(FolderData folder) {
        Collection<ROINode> tmp = get(folder);
        switch (tmp.size()) {
        case 0:
            return null;
        case 1:
            return tmp.iterator().next();
        default:
            throw new RuntimeException("Multiple ROINodes found for " + folder);
        }
    }

    /**
     * Get the child folder ids of the given folders
     * 
     * @param folders
     *            The folders
     * @return See above
     */
    public Collection<Long> getChildFolderIds(Collection<FolderData> folders) {
        Set<Long> ids = new HashSet<Long>();
        for (FolderData f : folders) {
            if (ids.contains(f.getId()))
                continue;

            ROINode fnode = findFolderNode(f);
            Collection<ROINode> subNodes = new ArrayList<ROINode>();
            ROIUtil.getAllDecendants(fnode, subNodes);
            for (ROINode subNode : subNodes)
                if (subNode.isFolderNode())
                    ids.add(((FolderData) subNode.getUserObject()).getId());

            ids.remove(f.getId());
        }
        return ids;
    }

}
