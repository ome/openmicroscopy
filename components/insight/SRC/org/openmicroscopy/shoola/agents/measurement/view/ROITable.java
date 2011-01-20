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
import java.util.List;
import java.util.TreeMap;
import java.util.Vector;

import javax.swing.JPopupMenu;
import javax.swing.table.TableColumn;
import javax.swing.tree.TreePath;

//Third-party libraries
import org.jdesktop.swingx.JXTreeTable;

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.measurement.util.roimenu.ROIPopupMenu;
import org.openmicroscopy.shoola.agents.measurement.util.roitable.ROIActionController;
import org.openmicroscopy.shoola.agents.measurement.util.roitable.ROINode;
import org.openmicroscopy.shoola.agents.measurement.util.roitable.ROITableCellRenderer;
import org.openmicroscopy.shoola.agents.measurement.util.roitable.ROITableModel;
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
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class ROITable 
	extends OMETreeTable
	implements ROIActionController
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
	 * Returns <code>true</code> if all the roishapes in the shapelist 
	 * have the same id, <code>false</code> otherwise.
	 *  
	 * @param shapeList The list to handle.
	 * @return See above.
	 */
	private boolean haveSameID(List<ROIShape> shapeList)
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
	 * 
	 * @param shapeList The list to handle.
	 * @return See above.
	 */
	private long getSameID(List<ROIShape> shapeList)
	{
		TreeMap<Long, ROIShape> shapeMap = new TreeMap<Long, ROIShape>();
		if (shapeList.size() == 0) return -1;
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
	 * Creates a popup menu to show different options to act of ROI.
	 *
	 */
	private void createPopupMenu()
	{
		popupMenu = new ROIPopupMenu(this);
	}
	
	/**
	 * The constructor for the ROITable, taking the root node and
	 * column names as parameters.  
	 * 
	 * @param model the table model.
	 * @param columnNames the column names.
	 * @param manager Reference to the manager.
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
		for (int i = 0 ; i < model.getColumnCount() ; i++)
		{
			getColumn(i).setResizable(true);
		}
		setDefaultRenderer(ShapeType.class, new ShapeRenderer());
		setTreeCellRenderer(new ROITableCellRenderer());
		createPopupMenu();
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
		
		int row = this.getRowForPath(child.getPath());
		this.selectionModel.addSelectionInterval(row, row);
	}
	
	/**
	 * Scroll to the selected ROIShape. 
	 * @param shape see above.
	 */
	public void scrollToROIShape(ROIShape shape)
	{
		ROINode parent = findParent(shape.getROI());
		if(parent == null)
			return;
		expandROIRow(parent);
		ROINode child = parent.findChild(shape);
		this.scrollPathToVisible(child.getPath());
	}
	
	/**
	 * Extending the mouse pressed event to show menu. 
	 * @param e mouse event.
	 */
	protected void onMousePressed(MouseEvent e)
	{
		JPopupMenu menu = popupMenu.getPopupMenu();
		if (rightClick(e) && !menu.isVisible())
			menu.show(this, e.getX(), e.getY());
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
		ArrayList<ROIShape> shapeList = new ArrayList<ROIShape>();
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
		ROINode parent=null;
		for(ROIShape shape : shapeList)
		{
			parent = findParent(shape.getROI());
			if(parent == null)
			{
				parent = new ROINode(shape.getROI());
				parent.setExpanded(true);
				ROIMap.put(shape.getROI(), parent);
				int childCount = root.getChildCount();
				root.insert(parent,childCount);
			}
			ROINode roiShapeNode = parent.findChild(shape.getCoord3D());
			ROINode newNode = new ROINode(shape);
			newNode.setExpanded(true);
			if(roiShapeNode != null)
			{
				int index = parent.getIndex(roiShapeNode);
				parent.remove(shape.getCoord3D());
				parent.insert(newNode, index);
			}
			else
				parent.insert(newNode, parent.getInsertionPoint(shape.getCoord3D()));
		}
		this.setTreeTableModel(new ROITableModel(root, columnNames));
		if(parent!=null)
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
		this.expandROIRow(selectedNode);
	//	this.scrollCellToVisible(selectedNodeIndex, 0);
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
		ArrayList<ROIShape> selectionList = getSelectedROIShapes();
		manager.deleteROIShapes(selectionList);
	}
	
	/**
	 * Create a list of all the roi and roishapes selected in the table.
	 * This will only list an roi even if the roi and roishapes are selected.
	 * @return see above.
	 */
	ArrayList getSelectedObjects()
	{
		int [] selectedRows = this.getSelectedRows();
		TreeMap<Long, Object> roiMap = new TreeMap<Long, Object>(); 
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
	TreeMap<Coord3D, ROIShape> buildPlaneMap(ArrayList objectList)
	{
		TreeMap<Coord3D, ROIShape> planeMap = new TreeMap<Coord3D, ROIShape>(new Coord3D());
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
		TreeMap<Long,ROI> idMap = new TreeMap<Long, ROI>();
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
		TreeMap<Long, Object> roiMap = new TreeMap<Long, Object>(); 
		ArrayList<ROIShape> selectedList = new ArrayList<ROIShape>();
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
	 * 
	 * @param shapeList The list to handle.
	 * @return See above.
	 */
	private boolean onSeparatePlanes(List<ROIShape> shapeList)
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
	
	/* (non-Javadoc)
	 * @see org.openmicroscopy.shoola.agents.measurement.util.ROIActionController#duplicateROI()
	 */
	public void duplicateROI()
	{
		manager.showReadyMessage();
		ArrayList<ROIShape> selectedObjects = getSelectedROIShapes();
		if(onSeparatePlanes(selectedObjects) && haveSameID(selectedObjects))
			manager.duplicateROI(getSameID(selectedObjects), selectedObjects );
		else
			manager.showMessage("Duplicate: ROIs must be from the same ROI and on separate planes.");
				
	}

	/* (non-Javadoc)
	 * @see org.openmicroscopy.shoola.agents.measurement.util.ROIActionController#mergeROI()
	 */
	public void mergeROI()
	{
		manager.showReadyMessage();
		ArrayList<ROIShape> selectedObjects = getSelectedROIShapes();
		if(onSeparatePlanes(selectedObjects) && selectedObjects.size() > 1)
		{
			ArrayList<Long> idList = getIDList(selectedObjects);
			manager.mergeROI(idList, selectedObjects);
		}
			else
				manager.showMessage("Merge: ROIs must be on separate planes and must include more than one.");	
	}

	/* (non-Javadoc)
	 * @see org.openmicroscopy.shoola.agents.measurement.util.ROIActionController#propagateROI()
	 */
	public void propagateROI()
	{
		manager.showReadyMessage();
		if(this.getSelectedRows().length!=1)
		{
			manager.showMessage("Propagate: Only one ROI may be propagated at a time.");
			return;
		}
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
		manager.showReadyMessage();
		ArrayList<ROIShape> selectedObjects = getSelectedROIShapes();
		if(onSeparatePlanes(selectedObjects) && haveSameID(selectedObjects))
			manager.splitROI(getSameID(selectedObjects), selectedObjects);
		else
			manager.showMessage("Split: ROIs must be from the same ROI and on separate planes.");

	}
	
	/* (non-Javadoc)
	 * @see org.openmicroscopy.shoola.agents.measurement.util.ROIActionController#calculateStats()
	 */
	public void calculateStats()
	{
		manager.showReadyMessage();
		List<ROIShape> selectedObjects = getSelectedROIShapes();
		if (onSeparatePlanes(selectedObjects) && haveSameID(selectedObjects))
			manager.calculateStats(selectedObjects);
		else
			manager.showMessage("Calculate: ROIs must be from the same ROI " +
					"and on separate planes.");

	}
	
}

