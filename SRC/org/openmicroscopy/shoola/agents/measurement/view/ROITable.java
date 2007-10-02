/*
 * org.openmicroscopy.shoola.agents.measurement.view.ROITable 
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
package org.openmicroscopy.shoola.agents.measurement.view;



//Java imports
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.Vector;

import javax.swing.table.TableColumn;
import javax.swing.tree.TreePath;

//Third-party libraries
import org.jdesktop.swingx.JXTreeTable;

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.measurement.util.ROIActionController;
import org.openmicroscopy.shoola.agents.measurement.util.ROINode;
import org.openmicroscopy.shoola.agents.measurement.util.ROITableCellRenderer;
import org.openmicroscopy.shoola.agents.measurement.util.ShapeRenderer;
import org.openmicroscopy.shoola.agents.measurement.util.roimenu.ROIPopupMenu;
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
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class ROITable 
	extends OMETreeTable implements ROIActionController
{

	/** The root node of the tree. */
	private ROINode			root;
	
	/** Column names of the table. */
	private Vector<String>	columnNames;
	
	/** The map to relate ROI to ROINodes. */
	private HashMap<ROI, ROINode> ROIMap;
	
	/** The tree model. */
	private ROITableModel	model;
	
	/** The roi popup menu creation class .*/
	private ROIPopupMenu 	popupMenu;
	
	/** Reference to the object manager. */
	private ObjectManager   manager;
    
	/**
	 * The constructor for the ROITable, taking the root node and
	 * column names as parameters.  
	 * @param model the table model.
	 * @param columnNames the column names.
	 */
	ROITable(ROITableModel model, Vector columnNames, ObjectManager manager)
	{
		super(model);
		this.model = model;
		this.manager = manager;
		this.root = (ROINode) model.getRoot();
		this.columnNames = columnNames;
		this.setAutoResizeMode(JXTreeTable.AUTO_RESIZE_ALL_COLUMNS);
		ROIMap = new HashMap<ROI, ROINode>();
		for( int i = 0 ; i < model.getColumnCount() ; i++)
		{
			TableColumn column = this.getColumn(i);
			column.setResizable(true);
		}
		setDefaultRenderer(ShapeType.class, new ShapeRenderer());
		setTreeCellRenderer(new ROITableCellRenderer());
		createPopupMenu();
	}

	/**
	 * Create a popup menu to show different options to act of ROI.
	 *
	 */
	private void createPopupMenu()
	{
		popupMenu = new ROIPopupMenu(this);
	}
	
	/** 
	 * Select the ROIShape in the TreeTable and move the view port of the 
	 * table to the shape selected. 
	 * @param shape
	 */
	public void selectROIShape(ROIShape shape)
	{
		ROINode parent = findParent(shape.getROI());
		if(parent == null)
			return;
		expandROIRow(parent);
		ROINode child = parent.findChild(shape);
		ROINode pathList[] = new ROINode[3];
		pathList[0] = root;
		pathList[1] = parent;
		pathList[2] = child;
		TreePath path = new TreePath(pathList);
		
		int row = this.getRowForPath(path);
		this.selectionModel.addSelectionInterval(row, row);
		this.scrollCellToVisible(row, 0);
	}
	
	/**
	 * Extending the mouse pressed event to show menu. 
	 * @param e mouse event.
	 */
	protected void onMousePressed(MouseEvent e)
	{
		if(rightClick(e))
			popupMenu.getPopupMenu().show(this, e.getX(), e.getY());
	}
	
	
	/**
	 * Refresh the data in the table. 
	 *
	 */
	public void refresh()
	{
		this.setTreeTableModel(new ROITableModel(root, columnNames));
	}
	
	/**
	 * Clear the table. 
	 *
	 */
	public void clear()
	{
		int childCount = root.getChildCount();
		for(int i = 0 ; i < childCount ; i++ )
			root.remove(0);
		this.setTreeTableModel(new ROITableModel(root, columnNames));
		ROIMap = new HashMap<ROI, ROINode>();
		this.invalidate();
		this.repaint();
	}

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
		ROINode node = (ROINode)getNodeAtRow(row);
		super.setValueAt(obj, row, column);
		ROINode expandNode;
		if(node.getUserObject() instanceof ROI)
		{
			ROI roi = (ROI)node.getUserObject();
			expandNode = node;
			if(roi.isVisible())
				expandROIRow(expandNode);
			else
				collapseROIRow(expandNode);
		}
		else
		{
			expandNode = (ROINode)node.getParent();
			ROIShape roiShape = (ROIShape)node.getUserObject();
			if(roiShape.getROI().isVisible())
				expandROIRow(expandNode);
			else
				collapseROIRow(expandNode);
		}
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
		ROINode parent = findParent(shape.getROI());
		if(parent == null)
		{
			parent = new ROINode(shape.getROI());
			parent.setExpanded(true);
			ROIMap.put(shape.getROI(), parent);
			int childCount = root.getChildCount();
			root.insert(parent,childCount);
		}
		ROINode newNode = new ROINode(shape);
		newNode.setExpanded(true);
		parent.insert(newNode, parent.getChildCount());
		this.setTreeTableModel(new ROITableModel(root, columnNames));
		expandROIRow(parent);
	}
	
	/** 
	 * Expand the row of the node 
	 * @param node see above.
	 */
	public void expandNode(ROINode node)
	{
		if(node.getUserObject() instanceof ROI)
			expandROIRow((ROI)node.getUserObject());
	}
	
	/**
	 * Get the ROI Shape at row index.
	 * @param index see above.
	 * @return see above.
	 */
	public ROIShape getROIShapeAtRow(int index)
	{
		TreePath path = this.getPathForRow(index);
		if(path==null) return null;
		ROINode node = (ROINode)path.getLastPathComponent();
		if(node.getUserObject() instanceof ROIShape)
			return (ROIShape)node.getUserObject();
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
		ROINode node = (ROINode)path.getLastPathComponent();
		if(node.getUserObject() instanceof ROI)
			return (ROI)node.getUserObject();
		return null;
	}
	
	/** 
	 * Expand the row with node parent.
	 * @param parent see above.
	 */
	public void expandROIRow(ROINode parent)
	{
		int addedNodeIndex = root.getIndex(parent);
		parent.setExpanded(true);
		this.expandRow(addedNodeIndex);
		ROINode node;
		for (int i=0; i<root.getChildCount(); i++)
		{
				node = (ROINode) root.getChildAt(i);
				if (node.isExpanded()) 
					expandPath(node.getPath());
		}
		this.scrollCellToVisible(addedNodeIndex+parent.getChildCount(), 0);
	}
	
	/** 
	 * Collapse the row with node parent.
	 * @param parent see above.
	 */
	public void collapseROIRow(ROINode parent)
	{
		int addedNodeIndex = root.getIndex(parent);
		this.collapseRow(addedNodeIndex);
		parent.setExpanded(false);
		ROINode node;
		for (int i=0; i<root.getChildCount(); i++)
		{
				node = (ROINode) root.getChildAt(i);
				if (node.isExpanded()) 
					expandROIRow((ROINode) node);
		}
	}
	
	/** 
	 * Expand the row with ROI.
	 * @param roi see above.
	 */
	public void expandROIRow(ROI roi)
	{
		ROINode selectedNode = findParent(roi);
		int selectedNodeIndex = root.getIndex(selectedNode);
		this.expandROIRow(selectedNode);
		this.scrollCellToVisible(selectedNodeIndex, 0);
	}
	
	/**
	 * Remove the ROIShape from the table, and delete the ROI if the 
	 * ROIShape was the last ROIShape in the ROI.
	 * @param shape see above.
	 */
	public void removeROIShape(ROIShape shape)
	{
		ROINode parent = findParent(shape.getROI());
		if(parent == null)
			return;
		ROINode child = parent.findChild(shape);
		parent.remove(child);
		if(parent.getChildCount()==0)
			root.remove(parent);
		this.setTreeTableModel(new ROITableModel(root, columnNames));
	}

	/**
	 * Remove the ROI from the table.
	 * @param roi see above.
	 */
	public void removeROI(ROI roi)
	{
		ROINode roiNode = findParent(roi);
		root.remove(roiNode);
		this.setTreeTableModel(new ROITableModel(root, columnNames));
	}
	
	/**
	 * Find the parent ROINode of the ROI.
	 * @param roi see above.
	 * @return see above.
	 */
	public ROINode findParent(ROI roi)
	{
		if(ROIMap.containsKey(roi))
			return ROIMap.get(roi);
		return null;
	}
	
	/**
	 * The ROIShape has changed it's properties. 
	 * @param shape the roiShape which has to be updated.
	 */
	public void setROIAttributesChanged(ROIShape shape)
	{
		ROINode parent = findParent(shape.getROI());
		ROINode child = parent.findChild(shape);
		model.nodeUpdated(child);
	}
	
	
	/** is this column the shapeType column.
	 * @param column see above
	 * @return see above 
	 */
	public boolean isShapeTypeColumn(int column)
	{
		TableColumn col = this.getColumn(column);
		if(col.getModelIndex()==(ROITableModel.SHAPE_COLUMN+1))
			return true;
		return false;
	}

	/* (non-Javadoc)
	 * @see org.openmicroscopy.shoola.agents.measurement.util.ROIActionController#deleteROI()
	 */
	public void deleteROI()
	{
		ArrayList selectionList = getSelectedObjects();
		for(Object nodeObject : selectionList)
		{
			if(nodeObject instanceof ROI)
				manager.deleteROI((ROI)nodeObject);
			else if(nodeObject instanceof ROIShape)
				manager.deleteROIShape((ROIShape)nodeObject);
		}
	}
	
	/**
	 * Create a list of all the roi and roishapes selected in the table.
	 * This will only list an roi even if the roi and roishapes are selected.
	 * @return see above.
	 */
	ArrayList getSelectedObjects()
	{
		int [] selectedRows = this.getSelectedRows();
		HashMap<Long, Object> roiMap = new HashMap<Long, Object>(); 
		ArrayList selectedList = new ArrayList();
		for(int i = 0 ; i < selectedRows.length ; i++)
		{	
			Object nodeObject = this.getNodeAtRow(selectedRows[i]).getUserObject();
			if(nodeObject instanceof ROI)
			{
				ROI roi = (ROI)nodeObject;
				roiMap.put(roi.getID(), roi);
				selectedList.add(roi);
			}
		}
		for(int i = 0 ; i < selectedRows.length ; i++)
		{	
			Object nodeObject = this.getNodeAtRow(selectedRows[i]).getUserObject();
			if (nodeObject instanceof ROIShape)
			{
				ROIShape roiShape = (ROIShape)nodeObject;
				if(!roiMap.containsKey(roiShape.getID()))
					selectedList.add(roiShape);
			}
		}
		return selectedList;
	}

	/**
	 * Build the plane map from the selected object list. This builds a map
	 * of all the planes that have objects reside on them.
	 * @param objectList see above.
	 * @return see above.
	 */
	HashMap<Coord3D, ROIShape> buildPlaneMap(ArrayList objectList)
	{
		HashMap<Coord3D, ROIShape> planeMap = new HashMap<Coord3D, ROIShape>();
		for(Object node : objectList)
		{
			if(node instanceof ROI)
			{
				ROI roi = (ROI)node;
				TreeMap<Coord3D, ROIShape> shapeMap =  roi.getShapes();
				Iterator<Coord3D> coordIterator = shapeMap.keySet().iterator();
				while(coordIterator.hasNext())
				{
					Coord3D coord = coordIterator.next();
					if(planeMap.containsKey(coord))
						return null;
					planeMap.put(coord, shapeMap.get(coord));
					System.err.println("ROI.id= "+shapeMap.get(coord).getID());
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
	ArrayList<Long> getIDList(ArrayList selectedObjects)
	{
		HashMap<Long,ROI> idMap = new HashMap<Long, ROI>();
		ArrayList<Long> idList = new ArrayList<Long>();
		for(Object node : selectedObjects)
		{
			ROI roi;
			if(node instanceof ROI)
				roi = (ROI)node;
			else
				roi = ((ROIShape)node).getROI();
			if(!idMap.containsKey(roi.getID()))
			{
				idMap.put(roi.getID(), roi);
				idList.add(roi.getID());
			}
		}
		return idList;
	}
	
	/**
	 * Get the roishapes of the selected objects, this method will split ROI
	 * into their respective ROIshapes. 
	 * @return see above.
	 */
	ArrayList<ROIShape> getSelectedROIShapes()
	{
		int [] selectedRows = this.getSelectedRows();
		HashMap<Long, Object> roiMap = new HashMap<Long, Object>(); 
		ArrayList selectedList = new ArrayList();
		for(int i = 0 ; i < selectedRows.length ; i++)
		{	
			Object nodeObject = this.getNodeAtRow(selectedRows[i]).getUserObject();
			if(nodeObject instanceof ROI)
			{
				ROI roi = (ROI)nodeObject;
				roiMap.put(roi.getID(), roi);
				Iterator<ROIShape> shapeIterator = roi.getShapes().values().iterator();
				while(shapeIterator.hasNext())
					selectedList.add(shapeIterator.next());
			}
		}
		for(int i = 0 ; i < selectedRows.length ; i++)
		{	
			Object nodeObject = this.getNodeAtRow(selectedRows[i]).getUserObject();
			if (nodeObject instanceof ROIShape)
			{
				ROIShape roiShape = (ROIShape)nodeObject;
				if(!roiMap.containsKey(roiShape.getID()))
					selectedList.add(roiShape);
			}
		}

		return selectedList;
	}
	
	/**
	 * Are all the roishapes in the shapelist on separate planes. 
	 * @param shapeList see above.
	 * @return see above.
	 */
	boolean onSeparatePlanes(ArrayList<ROIShape> shapeList)
	{
		HashMap<Coord3D, ROIShape> shapeMap = new HashMap<Coord3D, ROIShape>();
		for(ROIShape shape : shapeList)
		{
			if(shapeMap.containsKey(shape.getCoord3D()))
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
	boolean haveSameID(ArrayList<ROIShape> shapeList)
	{
		HashMap<Long, ROIShape> shapeMap = new HashMap<Long, ROIShape>();
		for(ROIShape shape : shapeList)
		{
			if(!shapeMap.containsKey(shape.getID()))
			{
				if(shapeMap.size()==0)
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
	long getSameID(ArrayList<ROIShape> shapeList)
	{
		HashMap<Long, ROIShape> shapeMap = new HashMap<Long, ROIShape>();
		if(shapeList.size()==0)
			return -1;
		for(ROIShape shape : shapeList)
		{
			if(!shapeMap.containsKey(shape.getID()))
			{
				if(shapeMap.size()==0)
					shapeMap.put(shape.getID(), shape);
				else
					return -1;
			}
		}
		return shapeList.get(0).getID();
	}
	
	/* (non-Javadoc)
	 * @see org.openmicroscopy.shoola.agents.measurement.util.ROIActionController#duplicateROI()
	 */
	public void duplicateROI()
	{
		ArrayList<ROIShape> selectedObjects = getSelectedROIShapes();
		if(onSeparatePlanes(selectedObjects) && haveSameID(selectedObjects))
			manager.duplicateROI(getSameID(selectedObjects), selectedObjects );
				
	}

	/* (non-Javadoc)
	 * @see org.openmicroscopy.shoola.agents.measurement.util.ROIActionController#mergeROI()
	 */
	public void mergeROI()
	{
		ArrayList<ROIShape> selectedObjects = getSelectedROIShapes();
		if(onSeparatePlanes(selectedObjects))
		{
			ArrayList<Long> idList = getIDList(selectedObjects);
			manager.mergeROI(idList, selectedObjects);
		}
			
	}

	/* (non-Javadoc)
	 * @see org.openmicroscopy.shoola.agents.measurement.util.ROIActionController#propagateROI()
	 */
	public void propagateROI()
	{
		if(this.getSelectedRows().length!=1)
			return;
		ROINode node = (ROINode)this.getNodeAtRow(this.getSelectedRow());
		Object nodeObject = node.getUserObject(); 
		if(nodeObject instanceof ROI)
			manager.propagateROI(((ROI)nodeObject));
		if(nodeObject instanceof ROIShape)
			manager.propagateROI(((ROIShape)nodeObject).getROI());
	}

	/* (non-Javadoc)
	 * @see org.openmicroscopy.shoola.agents.measurement.util.ROIActionController#splitROI()
	 */
	public void splitROI()
	{
		ArrayList<ROIShape> selectedObjects = getSelectedROIShapes();
		if(onSeparatePlanes(selectedObjects) && haveSameID(selectedObjects))
			manager.splitROI(getSameID(selectedObjects), selectedObjects);
	}
	
	/* (non-Javadoc)
	 * @see org.openmicroscopy.shoola.agents.measurement.util.ROIActionController#calculateStats()
	 */
	public void calculateStats()
	{
		ArrayList<ROIShape> selectedObjects = getSelectedROIShapes();
		if(onSeparatePlanes(selectedObjects) && haveSameID(selectedObjects))
			manager.calculateStats(getSameID(selectedObjects), selectedObjects );
	}
	
	/**
	 * Check the list of to make sure the ROIShapes in the list are from the
	 * same ROI and if they are then return true;
	 * @param shapeList see above.
	 * @return see above.
	 */
	boolean shapesInSameROI(ArrayList shapeList)
	{
		long id=-1;
		if(shapeList.size() == 0)
			return false;
		boolean first = true;
		for(Object node : shapeList)
		{
			if(node instanceof ROI)
				return false;
			else if(node instanceof ROIShape)
			{
				ROIShape shape = (ROIShape)node;
				if(first)
				{
					id = shape.getID();
					first = false;
				}
				if(id != shape.getID())
					return false;
			}
		}
		return true;
	}

}

