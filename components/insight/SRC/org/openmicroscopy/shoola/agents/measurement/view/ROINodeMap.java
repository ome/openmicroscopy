package org.openmicroscopy.shoola.agents.measurement.view;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import omero.gateway.model.FolderData;

import org.openmicroscopy.shoola.agents.measurement.util.roitable.ROINode;
import org.openmicroscopy.shoola.util.roi.model.ROI;

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
        nodesMap.put(node.getUUID(), node);
    }

    /**
     * Generates an unique id for the {@link ROI} or {@link FolderData}
     * 
     * @param obj
     *            The ROI or FolderData
     * @return The unique id
     */
    private String getUUID(Object obj) {
        if (obj instanceof ROI)
            return "ROI_" + ((ROI) obj).getID();
        if (obj instanceof FolderData)
            return "FolderData_" + ((FolderData) obj).getId();
        return null;
    }

    /**
     * Retrieve the nodes for the given object
     * 
     * @param obj
     *            The object
     * @return See above
     */
    public Collection<ROINode> get(Object obj) {
        String key = getUUID(obj);
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
     * @return
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
            fnode.getAllDecendants(subNodes);
            for (ROINode subNode : subNodes)
                if (subNode.isFolderNode())
                    ids.add(((FolderData) subNode.getUserObject()).getId());

            ids.remove(f.getId());
        }
        return ids;
    }

}
