/*
 * org.openmicroscopy.shoola.agents.measurement.util.roitable.ROINode 
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
import org.openmicroscopy.shoola.util.roi.model.annotation.AnnotationKeys;
import org.openmicroscopy.shoola.util.roi.model.annotation.MeasurementAttributes;
import org.openmicroscopy.shoola.util.roi.model.util.Coord3D;
import org.openmicroscopy.shoola.util.ui.treetable.model.OMETreeNode;

/**
 * The ROINode is an extension of the DefaultMutableTreeTableNode
 * to use in the ROITable, this creates the structure for mapping
 * nodes in the table to ROI and ROIShapes.
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
public class ROINode 
	extends OMETreeNode
{

	/** ROI ID Column no for the wizard. */
	private static final int				ROIID_COLUMN = 0;

	/** Time point Column no for the wizard. */
	private static final int				TIME_COLUMN = 1;
	
	/** Z-Section Column no for the wizard. */
	private static final int				Z_COLUMN = 2;

	/** Type Column no for the wizard. */
	private static final int				SHAPE_COLUMN = 3;

	/** Annotation Column no for the wizard. */
	private static final int				ANNOTATION_COLUMN = 4;

	/** Visible Column no for the wizard. */
	private static final int				VISIBLE_COLUMN = 5;
	
	/** The map of the children, ROIShapes belonging to the ROINode. */
	HashMap<ROIShape, ROINode>				childMap;

	/** The map of the children, ROIShapes belonging to the ROINode. */
	TreeMap<Coord3D, ROINode>				childCoordMap;
		
	/**
	 * Constructor for parent node. 
	 * @param str parent type.
	 */
	public ROINode(String str)
	{
		super(str);
		initMaps();
	}
	
	/**
	 * Construct a node with ROI tpye.
	 * @param nodeName see above.
	 */
	public ROINode(ROI nodeName)
	{
		super(nodeName);
		initMaps();
	}
	
	/**
	 * Construct ROINode with ROIShape type.
	 * @param nodeName see above.
	 */
	public ROINode(ROIShape nodeName)
	{
		super(nodeName);
		initMaps();
	}

	/**
	 * Get the point in the parent where a child with coord should be inserted.
	 * @param coord see above.
	 * @return see above.
	 */
	public int getInsertionPoint(Coord3D coord)
	{
		Iterator<Coord3D> i = childCoordMap.keySet().iterator();
		int index = 0;
		while(i.hasNext())
		{
			Coord3D nodeCoord = i.next();
			if(nodeCoord.compare(nodeCoord, coord)!=-1)
				return index;
			index++;
		}
		return index;
	}
	
	/** 
	 * Initialise the maps for the child nodes. 
	 *
	 */
	private void initMaps()
	{
		childMap = new HashMap<ROIShape, ROINode>();
		childCoordMap = new TreeMap<Coord3D, ROINode>(new Coord3D());
	}
	
	/**
	 * Find the shape belonging to the ROINode.
	 * @param shape see above.
	 * @return see above.
	 */
	public ROINode findChild(ROIShape shape)
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
	public ROINode findChild(Coord3D shapeCoord)
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
		switch(column)
		{
			case VISIBLE_COLUMN+1:
			case ANNOTATION_COLUMN+1:
				return true;
			default:
				return false;
		}
	}
	
	/**
	 * Add a child to the current node.
	 * @param child see above.
	 * @param index the index to place child. 
	 */
	 public void insert(ROINode child, int index) 
	 {
		 super.insert(child, index);
		 Object userObject = child.getUserObject();
		 if(userObject instanceof ROIShape)
		 {
			 ROIShape shape = (ROIShape)userObject;
			 child.setExpanded(true);
			 childMap.put(shape, (ROINode)child);
			 childCoordMap.put(shape.getCoord3D(), (ROINode)child);
		 }
	 }

	 /**
	 * Remove a child to the current node.
	 * @param child see above.
	 */
	 public void remove(ROINode child) 
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
	 * @param child see above.
	 */
	 public void remove(Coord3D childCoord) 
	 {
		 ROINode childNode = childCoordMap.get(childCoord);
		 
		 super.remove(childNode);
		 Object userObject = childNode.getUserObject();
		 if(userObject instanceof ROIShape)
		 {
			 ROIShape shape = (ROIShape)userObject;
			 childMap.remove(shape);
			 childCoordMap.remove(shape.getCoord3D());
		 }
	 }
	 
	/**
	 * Get the value for the node at column
	 * @param column return the value of the element at column.
	 */
	public Object getValueAt(int column)
	{
		Object userObject=getUserObject();
		if (userObject instanceof ROI)
		{
			ROI roi = (ROI) userObject;
			switch (column)
			{
				case 0:
					return null;
				case ROIID_COLUMN+1:
					return Long.valueOf(roi.getID());
				case TIME_COLUMN+1:
					return roi.getTRange();
				case Z_COLUMN+1:
					return roi.getZRange();
				case SHAPE_COLUMN+1:
					return roi.getShapeTypes();
				case ANNOTATION_COLUMN+1:
					return AnnotationKeys.TEXT.get(roi);
				case VISIBLE_COLUMN+1:
					return roi.isVisible();
				default:
					return null;
			}
		}
		else if (userObject instanceof ROIShape)
		{
			ROIShape roiShape = (ROIShape) userObject;
			switch (column)
			{
				case 0:
					return null;
				case ROIID_COLUMN+1:
					return ((Long) roiShape.getID()).toString();
				case TIME_COLUMN+1:
					return ((Integer) (roiShape.getT()+1)).toString();
				case Z_COLUMN+1:
					Integer z = roiShape.getZ()+1;
					return z.toString();
				case SHAPE_COLUMN+1:
					return roiShape.getFigure().getType();
				case ANNOTATION_COLUMN+1:
					return AnnotationKeys.TEXT.get(roiShape);
				case VISIBLE_COLUMN+1:
					return Boolean.valueOf(roiShape.getFigure().isVisible());
				default:
					return null;
			}
		}
		return null;
	}
	
	/**
	 * Get the value for the node at column
	 * @param value  the value of the object to set.
	 * @param column the column.
	 */
	public void setValueAt(Object value, int column)
	{
		Object userObject=getUserObject();
		if (userObject instanceof ROI)
		{
			ROI roi=(ROI) userObject;
			switch (column)
			{
				case 0:
				case ROIID_COLUMN+1:
				case TIME_COLUMN+1:
				case Z_COLUMN+1:
				case SHAPE_COLUMN+1:
					break;
				case ANNOTATION_COLUMN+1:
					if(value instanceof String)
						roi.setAnnotation(AnnotationKeys.TEXT, (String) value);
					break;
				case VISIBLE_COLUMN+1:
					if(value instanceof Boolean)
					{
						Iterator<ROIShape> roiIterator = 
										roi.getShapes().values().iterator();
						while(roiIterator.hasNext())
						{
							ROIShape shape = roiIterator.next();
							shape.getFigure().setVisible((Boolean)value);
						}
					}
					break;
					default:
					break;
			}
		}
		else if (userObject instanceof ROIShape)
		{
			ROIShape roiShape=(ROIShape) userObject;
			ROIFigure figure = roiShape.getFigure();
			switch (column)
			{
				case 0:
				case ROIID_COLUMN+1:
				case TIME_COLUMN+1:
				case Z_COLUMN+1:
				case SHAPE_COLUMN+1:
				case ANNOTATION_COLUMN+1:
					if(value instanceof String)
					{
						AnnotationKeys.TEXT.set(roiShape, (String)value);
						MeasurementAttributes.TEXT.set(figure, (String)value);
						if(((String)value).equals(""))
							MeasurementAttributes.SHOWTEXT.set(figure, false);
						else
							MeasurementAttributes.SHOWTEXT.set(figure, true);
					}
					break;
				case VISIBLE_COLUMN+1:
					if(value instanceof Boolean)
						roiShape.getFigure().setVisible((Boolean)value);
					break;
				default:
					break;
			}
		}
	}
	
}
	

