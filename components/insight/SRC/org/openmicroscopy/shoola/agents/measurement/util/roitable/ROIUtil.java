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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import omero.gateway.model.FolderData;

import org.jdesktop.swingx.treetable.MutableTreeTableNode;
import org.openmicroscopy.shoola.util.roi.model.ROI;
import org.openmicroscopy.shoola.util.roi.model.ROIShape;
import org.openmicroscopy.shoola.util.roi.model.util.Coord3D;

/**
 * Some utility methods for ROI/Folder/Shape handling.
 * 
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 */
public class ROIUtil {

    /**
     * The type of objects selected
     */
    public enum SelectionType {
        /**
         * ROIs selection
         */
        ROIS, /**
         * Shapes selection
         */
        SHAPES, /**
         * Folders selection
         */
        FOLDERS, /**
         * Mixed selection
         */
        MIXED
    }

    /**
     * Returns <code>true</code> if all the roishapes in the shapelist have the
     * same id, <code>false</code> otherwise.
     * 
     * @param shapeList
     *            The list to handle.
     * @return See above.
     */
    public static boolean haveSameID(List<ROIShape> shapeList) {
        long id = -1;
        for (ROIShape s : shapeList) {
            if (id == -1)
                id = s.getID();
            else if (id != s.getID())
                return false;
        }
        return true;
    }

    /**
     * Returns the id that the shapes in the list contain, if they do not
     * contain the same id return -1;
     * 
     * @param shapeList
     *            The list to handle.
     * @return See above.
     */
    public static long getSameID(List<ROIShape> shapeList) {
        long id = -1;
        for (ROIShape s : shapeList) {
            if (id == -1)
                id = s.getID();
            else if (id != s.getID())
                return -1;
        }
        return id;
    }

    /**
     * Are all the roishapes in the shapelist on separate planes.
     * 
     * @param shapeList
     *            The list to handle.
     * @return See above.
     */
    public static boolean onSeparatePlanes(List<ROIShape> shapeList) {
        Set<Coord3D> set = new HashSet<Coord3D>();
        for (ROIShape shape : shapeList) {
            if (set.contains(shape.getCoord3D()))
                return false;
            else
                set.add(shape.getCoord3D());
        }
        return true;
    }

    /**
     * Returns the ids of objects in the given list.
     * 
     * @param selectedObjects
     *            The objects
     * @return see above.
     */
    public static List<Long> getIDList(List selectedObjects) {
        List<Long> ids = new ArrayList<Long>();
        ROI roi;
        for (Object node : selectedObjects) {
            if (node instanceof ROI)
                roi = (ROI) node;
            else
                roi = ((ROIShape) node).getROI();
            if (!ids.contains(roi.getID())) {
                ids.add(roi.getID());
            }
        }
        return ids;
    }

    /**
     * Determines which type of objects are selected
     * 
     * @param selection
     *            The objects
     * @return The {@link SelectionType}
     */
    public static SelectionType getSelectionType(Collection<Object> selection) {
        SelectionType result = null;
        for (Object obj : selection) {
            SelectionType tmp = null;
            if (obj instanceof ROI)
                tmp = SelectionType.ROIS;
            else if (obj instanceof ROIShape)
                tmp = SelectionType.SHAPES;
            else if (obj instanceof FolderData)
                tmp = SelectionType.FOLDERS;

            if (result == null) {
                result = tmp;
            } else {
                if (result != tmp) {
                    return SelectionType.MIXED;
                }
            }
        }
        return result;
    }

    /**
     * Provides an unique ID based on the object's type and it's id if it is an
     * ROI related object ({@link Object#hashCode()} otherwise).
     * 
     * @param obj
     *            The object
     * @return See above
     */
    public static String getUUID(Object obj) {
        if (obj instanceof ROI)
            return "ROI_" + ((ROI) obj).getID();
        if (obj instanceof ROIShape)
            return "ROIShape_" + ((ROIShape) obj).getID();
        if (obj instanceof FolderData)
            return "FolderData_" + ((FolderData) obj).getId();
        return "" + obj.hashCode();
    }

    /**
     * Gathers all sub nodes of a node and adds them to the provided nodes
     * collection (the node itself will be added to the collection, too)
     * 
     * @param node
     *            The node for which to gather the sub nodes for
     * @param nodes
     *            The collection to put the sub nodes into
     */
    public static void gatherNodes(ROINode node, Collection<ROINode> nodes) {
        nodes.add(node);
        for (MutableTreeTableNode n : node.getChildList()) {
            gatherNodes((ROINode) n, nodes);
        }
    }

    /**
     * Get all nodes which represent shapes, descending from the given node
     * 
     * @param root
     *            The root node.
     * 
     * @return See above.
     */
    public static Collection<ROINode> getShapeNodes(ROINode root) {
        Collection<ROINode> nodes = new ArrayList<ROINode>();
        gatherNodes(root, nodes);
        Iterator<ROINode> it = nodes.iterator();
        while (it.hasNext()) {
            ROINode next = it.next();
            if (!next.isShapeNode())
                it.remove();
        }
        return nodes;
    }

    /**
     * Get all subnodes of the given node, which represent the given shape
     *
     * @param shapeId
     *            The id of the shape
     * @param root
     *            The root node
     * @return See above
     */
    public static Collection<ROINode> getShapeNodes(long shapeId, ROINode root) {
        Collection<ROINode> nodes = getShapeNodes(root);
        Iterator<ROINode> it = nodes.iterator();
        while (it.hasNext()) {
            ROINode next = it.next();
            ROIShape shape = (ROIShape) next.getUserObject();
            if (shape.getROIShapeID() != shapeId)
                it.remove();
        }
        return nodes;
    }

    /**
     * Gathers all sub nodes of the given node and adds them to the provided
     * nodes collection (the node itself will be added to the collection, too)
     * 
     * @param node
     *            The root node
     * @param nodes
     *            The collection to put the sub nodes into
     */
    public static void getAllDecendants(ROINode node, Collection<ROINode> nodes) {
        nodes.add(node);
        for (MutableTreeTableNode n : node.getChildList()) {
            gatherNodes((ROINode) n, nodes);
        }
    }
}
