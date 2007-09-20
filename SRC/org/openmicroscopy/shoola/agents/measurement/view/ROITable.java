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

import javax.swing.table.TableColumn;
import javax.swing.tree.TreePath;

//Third-party libraries
import org.jdesktop.swingx.JXTreeTable;

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.measurement.util.ROINode;
import org.openmicroscopy.shoola.agents.measurement.util.ROITableCellRenderer;
import org.openmicroscopy.shoola.util.roi.model.ROI;
import org.openmicroscopy.shoola.util.roi.model.ROIShape;

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
	extends JXTreeTable
{

	/** The root node of the tree. */
	private ROINode			root;
	
	/** Column names of the table. */
	private Vector<String>	columnNames;
	
	/** The map to relate ROI to ROINodes. */
	private HashMap<ROI, ROINode> ROIMap;
	
	
	/**
	 * The constructor for the ROITable, taking the root node and
	 * column names as parameters.  
	 * 
	 */
	ROITable(ROITableModel model, Vector columnNames)
	{
		super(model);
		this.root = (ROINode) model.getRoot();
		this.columnNames = columnNames;
		this.setAutoResizeMode(JXTreeTable.AUTO_RESIZE_ALL_COLUMNS);
		ROIMap = new HashMap<ROI, ROINode>();
		for( int i = 0 ; i < model.getColumnCount() ; i++)
		{
			this.setDefaultRenderer(model.getColumnClass(i), new ROITableCellRenderer());
			TableColumn column = this.getColumn(i);
			column.setResizable(true);
		}
		this.setTreeCellRenderer(new ROITableCellRenderer());
	}

	/**
	 * is the cell at node with column editable.
	 * @param node the ROINode.
	 * @param column the column of the object in the roiNode.
	 * @return see above.
	 */
	public boolean isCellEditable(Object node, int column) 
	{
        ROINode aNode = (ROINode)node;
        switch(column) {
              case 0  : return true;
              default : 
            	  if (aNode.isLeaf()) 
            		  return aNode.isEditable(column); 
            	  else
            		  return false; 
        }
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
		ROINode child = parent.findChild(shape);
		expandROIRow(parent);
		int roiIndex = root.getIndex(parent);
		int roiShapeIndex = parent.getIndex(child) + roiIndex+1;
		this.selectionModel.addSelectionInterval(roiShapeIndex, roiShapeIndex);
		this.scrollCellToVisible(roiShapeIndex, 0);
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
	 * Set the value of the object at row and col to value object.
	 * @param obj see above.
	 * @param row see above.
	 * @param col see above.
	 */
	public void setValueAt(Object obj, int row, int col)
	{
		TreePath path = this.getPathForRow(row);
		ROINode node = (ROINode)path.getLastPathComponent();
		node.setValueAt(obj, col);
		this.refresh();
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
		parent.insert(new ROINode(shape), parent.getChildCount());
		this.setTreeTableModel(new ROITableModel(root, columnNames));
		expandROIRow(parent);
	}
	
	/**
	 * Get the ROI Shape at row index.
	 * @param index see above.
	 * @return see above.
	 */
	public ROIShape getROIShapeAtRow(int index)
	{
		TreePath path = this.getPathForRow(index);
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
		this.expandRow(addedNodeIndex);
		this.scrollCellToVisible(addedNodeIndex+parent.getChildCount(), 0);
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

