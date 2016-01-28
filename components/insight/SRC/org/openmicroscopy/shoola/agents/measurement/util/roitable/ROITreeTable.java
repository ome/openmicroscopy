/*
 * org.openmicroscopy.shoola.agents.measurement.util.roitable.ROITreeTable 
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

import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;
import java.util.Map.Entry;

import javax.swing.table.TableColumn;
import javax.swing.tree.TreePath;

import org.jdesktop.swingx.JXTreeTable;
import org.jdesktop.swingx.treetable.MutableTreeTableNode;

import org.openmicroscopy.shoola.agents.measurement.util.model.AnnotationField;
import org.openmicroscopy.shoola.agents.measurement.util.ui.ShapeRenderer;
import org.openmicroscopy.shoola.util.roi.model.ROI;
import org.openmicroscopy.shoola.util.roi.model.ROIShape;
import org.openmicroscopy.shoola.util.roi.model.util.Coord3D;
import org.openmicroscopy.shoola.util.ui.graphutils.ShapeType;
import org.openmicroscopy.shoola.util.ui.treetable.OMETreeTable;

/**
* The ROITable is the class extending the JXTreeTable, this shows the 
* ROI as the parent object and the ROIShapes belonging to the ROI as the
* child objects. 
*
* @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
* 	<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
* @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
* 	<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
* @since OME3.0
*/
public class ROITreeTable 
	extends OMETreeTable
{

	/** The root node of the tree. */
	private ROITreeNode			root;
	
	/** Column names of the table. */
	private Vector<String>	columnNames;
	
	/** Collection of column names. */
	private List<AnnotationField>			fields;
	
	/** The map to relate ROI to ROITreeNodes. */
	private Map<ROI, ROITreeNode> ROIMap;
	
	/** The tree model. */
	private ROITreeTableModel	model;
	
	/**
	 * The constructor for the ROITable, taking the root node and
	 * column names as parameters.  
	 * @param model the table model.
	 * @param columnNames the column names.
	 */
	public ROITreeTable(ROITreeTableModel model, Vector columnNames, 
			List<AnnotationField> fields)
	{
		super(model);
		this.model = model;
		this.root = (ROITreeNode) model.getRoot();
		this.fields = fields;
		this.columnNames = columnNames;
		this.setAutoResizeMode(JXTreeTable.AUTO_RESIZE_ALL_COLUMNS);
		ROIMap = new HashMap<ROI, ROITreeNode>();
		for (int i = 0 ; i < model.getColumnCount() ; i++)
			getColumn(i).setResizable(true);
		setDefaultRenderer(ShapeType.class, new ShapeRenderer());
		setTreeCellRenderer(new ROITreeTableCellRenderer());
	}
	
	/* TABLE META METHODS: MANIPULATE FIELDS. */
	
	/**
	 * Set the fields of the Table. Each field represents a column in the table
	 * and will represent an annotation in the ROI, ROIShape. 
	 * @param newFields see above.
	 */
	public void setFields(Vector<String> columnNames, 
			List<AnnotationField> newFields)
	{
		fields = newFields;
	}
	
	
	/* ADD METHODS. */
	
	/**
	 * Add the ROI to the table, placing the ROIShapes in the ROI
	 * to it. This method will create the ROI if it does not exist 
	 * already. This method will also collapse all the ROI in the treetable
	 * and expand the ROI of the ROIShape, moving the viewport to 
	 * the ROIShape. 
	 * @param roi see above.
	 */
	public void addROI(ROI roi)
	{
		TreeMap<Coord3D, ROIShape> shapeMap = roi.getShapes();
		List<ROIShape> shapeList = new ArrayList<ROIShape>();
		
		Iterator<ROIShape> shapeIterator = shapeMap.values().iterator();
		while (shapeIterator.hasNext())
			shapeList.add(shapeIterator.next());
		if (shapeList.size() == 0)
			return;
		addROIShapeList(shapeList);
	}
	
	/**
	 * Add the ROIShape to the table, placing it under the ROI the ROIShape
	 * belongs to. This method will create the ROI if it does not exist 
	 * already. This method will also collapse all the ROI in the treetable
	 * and expand the ROI of the ROIShape, moving the viewport to 
	 * the ROIShape. 
	 * @param shape see above.
	 */
	public void addROIShape(ROIShape shape)
	{
		List<ROIShape> shapeList = new ArrayList<ROIShape>();
		shapeList.add(shape);
		addROIShapeList(shapeList);
	}
	

	/**
	 * Add the ROIShapes in the shapeList to the table, placing it under the 
	 * ROI the ROIShape belongs to. This method will create the ROI if it does 
	 * not exist already. This method will also collapse all the ROI in the 
	 * treetable  and expand the ROI of the ROIShape, moving the viewport to 
	 * the ROIShape. 
	 * @param shapeList see above.
	 */
	public void addROIShapeList(List<ROIShape> shapeList)
	{
		ROITreeNode parent=null;
		for (ROIShape shape : shapeList)
		{
			parent = findParent(shape.getROI());
			if (parent == null)
			{
				parent = new ROITreeNode(shape.getROI());
				parent.setExpanded(true);
				ROIMap.put(shape.getROI(), parent);
				int childCount = root.getChildCount();
				root.insert(parent,childCount);
			}
			ROITreeNode roiShapeNode = parent.findChild(shape.getCoord3D());
			ROITreeNode newNode = new ROITreeNode(shape);
			newNode.setExpanded(true);
			if (roiShapeNode != null)
			{
				int index = parent.getIndex(roiShapeNode);
				parent.remove(shape.getCoord3D());
				parent.insert(newNode, index);
			}
			else
				parent.insert(newNode, 
						parent.getInsertionPoint(shape.getCoord3D()));
		}
		setTreeTableModel(new ROITreeTableModel(root, columnNames, fields));
		if (parent != null)
			expandROIRow(parent);
	}
	
	/* REMOVE METHODS. */
	
	/**
	 * Clear the table. 
	 *
	 */
	public void clear()
	{
		int childCount = root.getChildCount();
		for (int i = 0 ; i < childCount ; i++ )
			root.remove(0);
		this.setTreeTableModel(new ROITreeTableModel(root, 
				columnNames, fields));
		ROIMap = new HashMap<ROI, ROITreeNode>();
		this.invalidate();
		this.repaint();
	}
	
	/**
	 * Remove the ROIShape from the table, and delete the ROI if the 
	 * ROIShape was the last ROIShape in the ROI.
	 * @param shape see above.
	 */
	public void removeROIShape(ROIShape shape)
	{
		ROITreeNode parent = findParent(shape.getROI());
		if (parent == null)
			return;
		ROITreeNode child = parent.findChild(shape);
		parent.remove(child);
		if(parent.getChildCount()==0)
			root.remove(parent);
		this.setTreeTableModel(
				new ROITreeTableModel(root, columnNames, fields));
	}

	/**
	 * Remove the ROI from the table.
	 * @param roi see above.
	 */
	public void removeROI(ROI roi)
	{
		ROITreeNode ROITreeNode = findParent(roi);
		root.remove(ROITreeNode);
		this.setTreeTableModel(
				new ROITreeTableModel(root, columnNames, fields));
	}
	
	/* FIND METHODS. */
	
	/**
	 * Find the parent ROITreeNode of the ROI.
	 * @param roi see above.
	 * @return see above.
	 */
	public ROITreeNode findParent(ROI roi)
	{
		if (ROIMap.containsKey(roi))
			return ROIMap.get(roi);
		return null;
	}
	
	/* GET METHODS. */
	
	/**
	 * Get the ROI Shape at row index.
	 * @param index see above.
	 * @return see above.
	 */
	public ROIShape getROIShapeAtRow(int index)
	{
		TreePath path = this.getPathForRow(index);
		if (path == null) return null;
		ROITreeNode node = (ROITreeNode) path.getLastPathComponent();
		if (node.getUserObject() instanceof ROIShape)
			return (ROIShape) node.getUserObject();
		return null;
	}
	
	/**
	 * Get the ROI at row index.
	 * @param index see above.
	 * @return see above.
	 */
	public ROI getROIAtRow(int index)
	{
		TreePath path = this.getPathForRow(index);
		ROITreeNode node = (ROITreeNode) path.getLastPathComponent();
		if (node.getUserObject() instanceof ROI)
			return (ROI) node.getUserObject();
		return null;
	}
	
	/** 
	 * Returns <code>true</code> if the column is hosting a shape.
	 * @return see above 
	 */
	public boolean isShapeTypeColumn(int column)
	{
		TableColumn col = this.getColumn(column);
		return model.isShapeColumn(col.getModelIndex());
	}

	
	/* SET METHODS. */
	
	/**
	 * Set the value of the object at row and col to value object, 
	 * this will also expand the row of the object set, unless the ROI
	 * of the object is not visible it will then collapse the row.
	 * @param obj see above.
	 * @param row see above.
	 * @param column see above.
	 */
	public void setValueAt(Object obj, int row, int column)
	{
		ROITreeNode node = (ROITreeNode)getNodeAtRow(row);
		super.setValueAt(obj, row, column);
		ROITreeNode expandNode;
		if (node.getUserObject() instanceof ROI)
		{
			ROI roi = (ROI) node.getUserObject();
			expandNode = node;
			if (roi.isVisible())
				expandROIRow(expandNode);
			else
				collapseROIRow(expandNode);
		}
		else
		{
			expandNode = (ROITreeNode) node.getParent();
			ROIShape roiShape = (ROIShape) node.getUserObject();
			if (roiShape.getROI().isVisible())
				expandROIRow(expandNode);
			else
				collapseROIRow(expandNode);
		}
	}
	
	/* TREE VIEW METHODS: EXPANSION, COLLAPSE. */
	
	/** 
	 * Expand the row of the node 
	 * @param node see above.
	 */
	public void expandNode(ROITreeNode node)
	{
		if (node.getUserObject() instanceof ROI)
			expandROIRow((ROI)node.getUserObject());
	}

	/** 
	 * Expand the row with node parent.
	 * @param parent see above.
	 */
	public void expandROIRow(ROITreeNode parent)
	{
		int addedNodeIndex = root.getIndex(parent);
		parent.setExpanded(true);
		this.expandRow(addedNodeIndex);
		ROITreeNode node;
		for (int i = 0; i < root.getChildCount(); i++)
		{
				node = (ROITreeNode) root.getChildAt(i);
				if (node.isExpanded()) 
					expandPath(node.getPath());
		}
	}
	
	/** 
	 * Collapse the row with node parent.
	 * @param parent see above.
	 */
	public void collapseROIRow(ROITreeNode parent)
	{
		int addedNodeIndex = root.getIndex(parent);
		this.collapseRow(addedNodeIndex);
		parent.setExpanded(false);
		ROITreeNode node;
		for (int i=0; i<root.getChildCount(); i++)
		{
				node = (ROITreeNode) root.getChildAt(i);
				if (node.isExpanded()) 
					expandROIRow((ROITreeNode) node);
		}
	}
	
	/** 
	 * Expand the row with ROI.
	 * @param roi see above.
	 */
	public void expandROIRow(ROI roi)
	{
		ROITreeNode selectedNode = findParent(roi);
		this.expandROIRow(selectedNode);
	//	this.scrollCellToVisible(selectedNodeIndex, 0);
	}
	
	
	/**
	 * Scroll to the selected ROIShape. 
	 * @param shape see above.
	 */
	public void scrollToROIShape(ROIShape shape)
	{
		ROITreeNode parent = findParent(shape.getROI());
		if (parent == null)
			return;
		expandROIRow(parent);
		ROITreeNode child = parent.findChild(shape);
		this.scrollPathToVisible(child.getPath());
	}
	
	/* MOUSE HANDLING. */
	
	/**
	 * Extending the mouse pressed event to show menu. 
	 * @param e mouse event.
	 */
	protected void onMousePressed(MouseEvent e)
	{

	}
	
	/* VIEW UPDATE METHODS. */
	
	/**
	 * Refresh the data in the table. 
	 *
	 */
	public void refresh()
	{
		this.setTreeTableModel(new ROITreeTableModel(root, columnNames, fields));
	}
	
	/**
	 * The ROIShape has changed it's properties. 
	 * @param shape the roiShape which has to be updated.
	 */
	public void setROIAttributesChanged(ROIShape shape)
	{
		ROITreeNode parent = findParent(shape.getROI());
		ROITreeNode child = parent.findChild(shape);
		model.nodeUpdated(child);
	}
	
	/* SELECTION METHODS. */
		
	/** 
	 * Select the ROIShape in the TreeTable and move the view port of the 
	 * table to the shape selected. 
	 * @param shape
	 */
	public void selectROIShape(ROIShape shape)
	{
		ROITreeNode parent = findParent(shape.getROI());
		if (parent == null)
			return;
		expandROIRow(parent);
		ROITreeNode child = parent.findChild(shape);
		
		int row = this.getRowForPath(child.getPath());
		this.selectionModel.addSelectionInterval(row, row);
	}
	
	/** 
	 * Select the ROI in the TreeTable and move the view port of the 
	 * table to the ROI selected. 
	 * @param roi
	 */
	public void selectROI(ROI roi)
	{
		ROITreeNode parent = findParent(roi);
		if (parent == null)
			return;
		expandROIRow(parent);
		List<MutableTreeTableNode> childList = parent.getChildList();
		for (MutableTreeTableNode child : childList)
			selectROIShape((ROIShape) ((ROITreeNode) child).getUserObject());
	}	
	
	/**
	 * Create a list of all the roi and roishapes selected in the table.
	 * This will only list an roi even if the roi and roishapes are selected.
	 * @return see above.
	 */
	public List getSelectedObjects()
	{
		int [] selectedRows = this.getSelectedRows();
		TreeMap<Long, Object> roiMap = new TreeMap<Long, Object>(); 
		List selectedList = new ArrayList();
		for(int i = 0 ; i < selectedRows.length ; i++)
		{	
			Object nodeObject = this.getNodeAtRow(
					selectedRows[i]).getUserObject();
			if(nodeObject instanceof ROI)
			{
				ROI roi = (ROI)nodeObject;
				roiMap.put(roi.getID(), roi);
				selectedList.add(roi);
			}
		}
		for (int i = 0 ; i < selectedRows.length ; i++)
		{	
			Object nodeObject = this.getNodeAtRow(
					selectedRows[i]).getUserObject();
			if (nodeObject instanceof ROIShape)
			{
				ROIShape roiShape = (ROIShape) nodeObject;
				if (!roiMap.containsKey(roiShape.getID()))
					selectedList.add(roiShape);
			}
		}
		return selectedList;
	}

	/**
	 * Get the roishapes of the selected objects, this method will split ROI
	 * into their respective ROIshapes. 
	 * @return see above.
	 */
	public List<ROIShape> getSelectedROIShapes()
	{
		int [] selectedRows = this.getSelectedRows();
		TreeMap<Long, Object> roiMap = new TreeMap<Long, Object>(); 
		List<ROIShape> selectedList = new ArrayList<ROIShape>();
		for(int i = 0 ; i < selectedRows.length ; i++)
		{	
			Object nodeObject = this.getNodeAtRow(
					selectedRows[i]).getUserObject();
			if (nodeObject instanceof ROI)
			{
				ROI roi = (ROI) nodeObject;
				roiMap.put(roi.getID(), roi);
				Iterator<ROIShape> shapeIterator = 
					roi.getShapes().values().iterator();
				while(shapeIterator.hasNext())
					selectedList.add(shapeIterator.next());
			}
		}
		for(int i = 0 ; i < selectedRows.length ; i++)
		{	
			Object nodeObject = this.getNodeAtRow(
					selectedRows[i]).getUserObject();
			if (nodeObject instanceof ROIShape)
			{
				ROIShape roiShape = (ROIShape)nodeObject;
				if(!roiMap.containsKey(roiShape.getID()))
					selectedList.add(roiShape);
			}
		}

		return selectedList;
	}
	
	/* OBJECT ORGANISATION, QUERY METHODS. */
	
	/**
	 * Build the plane map from the selected object list. This builds a map
	 * of all the planes that have objects reside on them.
	 * @param objectList see above.
	 * @return see above.
	 */
	public TreeMap<Coord3D, ROIShape> buildPlaneMap(ArrayList objectList)
	{
		TreeMap<Coord3D, ROIShape> planeMap = new TreeMap<Coord3D, ROIShape>
		(new Coord3D());
		for (Object node : objectList)
		{
			if (node instanceof ROI)
			{
				ROI roi = (ROI) node;
				TreeMap<Coord3D, ROIShape> shapeMap =  roi.getShapes();
				Iterator i = shapeMap.entrySet().iterator();
				Entry entry;
				Coord3D coord;
				while (i.hasNext())
				{
					entry = (Entry) i.next();
					coord = (Coord3D) entry.getKey();
					if (planeMap.containsKey(coord))
						return null;
					planeMap.put(coord, (ROIShape) entry.getValue());
				}
			} else if (node instanceof ROIShape)
			{
				ROIShape shape = (ROIShape)node;
				if(planeMap.containsKey(shape.getCoord3D()))
					return null;
				else
					planeMap.put(shape.getCoord3D(), shape);
			}
		}
		return planeMap;
	}

	/**
	 * Get the id of objects in the selected list. 
	 * @param selectedObjects
	 * @return see above.
	 */
	public List<Long> getIDList(List selectedObjects)
	{
		TreeMap<Long,ROI> idMap = new TreeMap<Long, ROI>();
		List<Long> idList = new ArrayList<Long>();
		for (Object node : selectedObjects)
		{
			ROI roi;
			if (node instanceof ROI)
				roi = (ROI)node;
			else
				roi = ((ROIShape)node).getROI();
			if (!idMap.containsKey(roi.getID()))
			{
				idMap.put(roi.getID(), roi);
				idList.add(roi.getID());
			}
		}
		return idList;
	}
	
	/**
	 * Are all the roishapes in the shapelist on separate planes. 
	 * @param shapeList see above.
	 * @return see above.
	 */
	public boolean onSeparatePlanes(ArrayList<ROIShape> shapeList)
	{
		TreeMap<Coord3D, ROIShape> 
		shapeMap = new TreeMap<Coord3D, ROIShape>(new Coord3D());
		for (ROIShape shape : shapeList)
		{
			if (shapeMap.containsKey(shape.getCoord3D()))
				return false;
			else
				shapeMap.put(shape.getCoord3D(), shape);
		}
		return true;
	}
	
	/**
	 * Return true if all the roishapes in the shapelist have the same id. 
	 * @param shapeList see above.
	 * @return see above.
	 */
	public boolean haveSameID(ArrayList<ROIShape> shapeList)
	{
		TreeMap<Long, ROIShape> shapeMap = new TreeMap<Long, ROIShape>();
		for (ROIShape shape : shapeList)
		{
			if (!shapeMap.containsKey(shape.getID()))
			{
				if (shapeMap.size() == 0)
					shapeMap.put(shape.getID(), shape);
				else
					return false;
			}
		}
		return true;
	}
	
	/**
	 * Get the id that the roishapes in the shapelist contain, if they
	 * do not contain the same id return -1;
	 * @param shapeList see above.
	 * @return see above.
	 */
	public long getSameID(ArrayList<ROIShape> shapeList)
	{
		TreeMap<Long, ROIShape> shapeMap = new TreeMap<Long, ROIShape>();
		if (shapeList.size() == 0)
			return -1;
		for (ROIShape shape : shapeList)
		{
			if (!shapeMap.containsKey(shape.getID()))
			{
				if (shapeMap.size() == 0)
					shapeMap.put(shape.getID(), shape);
				else
					return -1;
			}
		}
		return shapeList.get(0).getID();
	}
	
	/**
	 * Check the list of to make sure the ROIShapes in the list are from the
	 * same ROI and if they are then return true;
	 * @param shapeList see above.
	 * @return see above.
	 */
	public boolean shapesInSameROI(ArrayList shapeList)
	{
		long id = -1;
		if (shapeList.size() == 0)
			return false;
		boolean first = true;
		for (Object node : shapeList)
		{
			if (node instanceof ROI)
				return false;
			else if (node instanceof ROIShape)
			{
				ROIShape shape = (ROIShape)node;
				if (first)
				{
					id = shape.getID();
					first = false;
				}
				if (id != shape.getID())
					return false;
			}
		}
		return true;
	}

}

