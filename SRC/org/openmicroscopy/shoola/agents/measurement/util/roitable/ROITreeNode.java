/*
 * org.openmicroscopy.shoola.agents.measurement.util.roitable.ROITreeNode 
 *
  *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.measurement.util.roitable;

//Java imports
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeMap;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.roi.figures.ROIFigure;
import org.openmicroscopy.shoola.util.roi.model.ROI;
import org.openmicroscopy.shoola.util.roi.model.ROIShape;
import org.openmicroscopy.shoola.util.roi.model.util.Coord3D;
import org.openmicroscopy.shoola.util.ui.treetable.model.OMETreeNode;


/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class ROITreeNode
	extends OMETreeNode
{
	
	/** The map of the children, ROIShapes belonging to the ROITreeNode. */
	HashMap<ROIShape, ROITreeNode>				childMap;

	/** The map of the children, ROIShapes belonging to the ROITreeNode. */
	TreeMap<Coord3D, ROITreeNode>				childCoordMap;
		
	/**
	 * Constructor for parent node. 
	 * @param str parent type.
	 */
	public ROITreeNode(String str)
	{
		super(str);
		initMaps();
	}
	
	/**
	 * Construct a node with ROI tpye.
	 * @param nodeName see above.
	 */
	public ROITreeNode(ROI nodeName)
	{
		super(nodeName);
		initMaps();
	}
	
	/**
	 * Construct ROITreeNode with ROIShape type.
	 * @param nodeName see above.
	 */
	public ROITreeNode(ROIShape nodeName)
	{
		super(nodeName);
		initMaps();
	}

	/**
	 * Get the point in the parent where a child with coord should be inserted.
	 * 
	 * @param coord see above.
	 * @return see above.
	 */
	public int getInsertionPoint(Coord3D coord)
	{
		Iterator<Coord3D> i = childCoordMap.keySet().iterator();
		int index = 0;
		Coord3D nodeCoord;
		while (i.hasNext())
		{
			nodeCoord = i.next();
			if (nodeCoord.compare(nodeCoord, coord) != -1)
				return index;
			index++;
		}
		return index;
	}
	
	/** Initialises the maps for the child nodes. */
	private void initMaps()
	{
		childMap = new HashMap<ROIShape, ROITreeNode>();
		childCoordMap = new TreeMap<Coord3D, ROITreeNode>(new Coord3D());
	}
	
	/**
	 * Find the shape belonging to the ROITreeNode.
	 * @param shape see above.
	 * @return see above.
	 */
	public ROITreeNode findChild(ROIShape shape)
	{
		if(childMap.containsKey(shape))
			return childMap.get(shape);
		return null;
	}

	/**
	 * Find the shape belonging to the shapeCoord.
	 * @param shapeCoord see above.
	 * @return see above.
	 */
	public ROITreeNode findChild(Coord3D shapeCoord)
	{
		if(childCoordMap.containsKey(shapeCoord))
			return childCoordMap.get(shapeCoord);
		return null;
	}
	
	/**
	 * Is the cell editable. 
	 * @param column the column to edit.
	 * @return see above.
	 */
	public boolean isEditable(int column)
	{
		return false;
	}
	
	/**
	 * Add a child to the current node.
	 * @param child see above.
	 * @param index the index to place child. 
	 */
	 public void insert(ROITreeNode child, int index) 
	 {
		 super.insert(child, index);
		 Object userObject = child.getUserObject();
		 if (userObject instanceof ROIShape)
		 {
			 ROIShape shape = (ROIShape)userObject;
			 child.setExpanded(true);
			 childMap.put(shape, child);
			 childCoordMap.put(shape.getCoord3D(), child);
		 }
	 }

	 /**
	 * Remove a child to the current node.
	 * @param child see above.
	 */
	 public void remove(ROITreeNode child) 
	 {
		 super.remove(child);
		 Object userObject = child.getUserObject();
		 if(userObject instanceof ROIShape)
		 {
			 ROIShape shape = (ROIShape)userObject;
			 childMap.remove(shape);
			 childCoordMap.remove(shape.getCoord3D());
		 }
	 }
	 
	 /**
	 * Remove a child to the current node.
	 * @param childCoord see above.
	 */
	 public void remove(Coord3D childCoord) 
	 {
		 ROITreeNode childNode = childCoordMap.get(childCoord);
		 
		 super.remove(childNode);
		 Object userObject = childNode.getUserObject();
		 if (userObject instanceof ROIShape)
		 {
			 ROIShape shape = (ROIShape)userObject;
			 childMap.remove(shape);
			 childCoordMap.remove(shape.getCoord3D());
		 }
	 }
	 
	/**
	 * Returns the value for the node at column
	 * 
	 * @param column return the value of the element at column.
	 * @return see above.
	 */
	public Object getValueAt(int column)
	{
		Object userObject = getUserObject();
		if ((userObject instanceof ROI) || (userObject instanceof ROIShape))
			return userObject;
		return null;
	}
	
	/**
	 * Get the value for the node at column
	 * @param value  the value of the object to set.
	 * @param column the column.
	 */
	public void setValueAt(Object value, int column)
	{
		/*Object userObject=getUserObject();
		if (userObject instanceof ROI)
		{
			ROI roi=(ROI) userObject;
		
		}
		else if (userObject instanceof ROIShape)
		{
			ROIShape roiShape=(ROIShape) userObject;
			ROIFigure figure = roiShape.getFigure();
			
		}*/
	}
	
}


