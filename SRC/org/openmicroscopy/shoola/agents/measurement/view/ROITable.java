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
import java.util.HashMap;
import java.util.Vector;

import javax.swing.JCheckBox;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.table.TableColumn;
import javax.swing.tree.TreePath;

//Third-party libraries
import org.jdesktop.swingx.JXTreeTable;

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.measurement.util.ROINode;
import org.openmicroscopy.shoola.agents.measurement.util.ROITableCellRenderer;
import org.openmicroscopy.shoola.util.roi.model.ROI;
import org.openmicroscopy.shoola.util.roi.model.ROIShape;
import org.openmicroscopy.shoola.util.ui.treetable.OMETreeTable;
import org.openmicroscopy.shoola.util.ui.treetable.editors.BooleanCellEditor;

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
{

	/** The root node of the tree. */
	private ROINode			root;
	
	/** Column names of the table. */
	private Vector<String>	columnNames;
	
	/** The map to relate ROI to ROINodes. */
	private HashMap<ROI, ROINode> ROIMap;
	
	/** The tree model. */
	private ROITableModel	model;
	
	/** Cell editor for the boolean values. */
	private BooleanCellEditor booleanCellEditor;
	
    
	/**
	 * The constructor for the ROITable, taking the root node and
	 * column names as parameters.  
	 * @param model the table model.
	 * @param columnNames the column names.
	 */
	ROITable(ROITableModel model, Vector columnNames)
	{
		super(model);
		this.model = model;
		this.root = (ROINode) model.getRoot();
		this.columnNames = columnNames;
		this.setAutoResizeMode(JXTreeTable.AUTO_RESIZE_ALL_COLUMNS);
		ROIMap = new HashMap<ROI, ROINode>();
		for( int i = 0 ; i < model.getColumnCount() ; i++)
		{
			TableColumn column = this.getColumn(i);
			column.setResizable(true);
		}
		setTreeCellRenderer(new ROITableCellRenderer());
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

}

