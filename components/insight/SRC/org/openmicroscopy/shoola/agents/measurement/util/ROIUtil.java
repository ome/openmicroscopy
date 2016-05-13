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

package org.openmicroscopy.shoola.agents.measurement.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

import omero.gateway.model.FolderData;

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
        ROIS, SHAPES, FOLDERS, MIXED
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
        TreeMap<Long, ROIShape> shapeMap = new TreeMap<Long, ROIShape>();
        for (ROIShape shape : shapeList) {
            if (!shapeMap.containsKey(shape.getID())) {
                if (shapeMap.size() == 0)
                    shapeMap.put(shape.getID(), shape);
                else
                    return false;
            }
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
        TreeMap<Long, ROIShape> shapeMap = new TreeMap<Long, ROIShape>();
        if (shapeList.size() == 0)
            return -1;
        for (ROIShape shape : shapeList) {
            if (!shapeMap.containsKey(shape.getID())) {
                if (shapeMap.size() == 0)
                    shapeMap.put(shape.getID(), shape);
                else
                    return -1;
            }
        }
        return shapeList.get(0).getID();
    }

    /**
     * Are all the roishapes in the shapelist on separate planes.
     * 
     * @param shapeList
     *            The list to handle.
     * @return See above.
     */
    public static boolean onSeparatePlanes(List<ROIShape> shapeList) {
        TreeMap<Coord3D, ROIShape> shapeMap = new TreeMap<Coord3D, ROIShape>(
                new Coord3D());
        for (ROIShape shape : shapeList) {
            if (shapeMap.containsKey(shape.getCoord3D()))
                return false;
            else
                shapeMap.put(shape.getCoord3D(), shape);
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
        TreeMap<Long, ROI> idMap = new TreeMap<Long, ROI>();
        List<Long> idList = new ArrayList<Long>();
        ROI roi;
        for (Object node : selectedObjects) {
            if (node instanceof ROI)
                roi = (ROI) node;
            else
                roi = ((ROIShape) node).getROI();
            if (!idMap.containsKey(roi.getID())) {
                idMap.put(roi.getID(), roi);
                idList.add(roi.getID());
            }
        }
        return idList;
    }

    /**
     * Build the plane map from the selected object list. This builds a map of
     * all the planes that have objects reside on them.
     * 
     * @param objectList
     *            see above.
     * @return see above.
     */
    public static TreeMap<Coord3D, ROIShape> buildPlaneMap(ArrayList objectList) {
        TreeMap<Coord3D, ROIShape> planeMap = new TreeMap<Coord3D, ROIShape>(
                new Coord3D());
        ROI roi;
        TreeMap<Coord3D, ROIShape> shapeMap;
        Iterator i;
        Coord3D coord;
        ROIShape shape;
        Entry entry;
        for (Object node : objectList) {
            if (node instanceof ROI) {
                roi = (ROI) node;
                shapeMap = roi.getShapes();
                i = shapeMap.entrySet().iterator();
                while (i.hasNext()) {
                    entry = (Entry) i.next();
                    coord = (Coord3D) entry.getKey();
                    if (planeMap.containsKey(coord))
                        return null;
                    planeMap.put(coord, (ROIShape) entry.getValue());
                }
            } else if (node instanceof ROIShape) {
                shape = (ROIShape) node;
                if (planeMap.containsKey(shape.getCoord3D()))
                    return null;
                else
                    planeMap.put(shape.getCoord3D(), shape);
            }
        }
        return planeMap;
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
}
