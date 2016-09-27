/*
 * org.openmicroscopy.shoola.agents.measurement.util.roitable.ROITableModel 
 *
  *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2016 University of Dundee. All rights reserved.
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
import java.util.Vector;

import javax.swing.tree.TreePath;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.graphutils.ShapeType;
import org.openmicroscopy.shoola.util.ui.treetable.model.OMETreeTableModel;

/**
 * 
 * The ROITableModel is the model for the ROITable class
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
public class ROITableModel 
	extends OMETreeTableModel
{
	
	/** ROI ID Column no for the wizard. */
	public static final int				ROIID_COLUMN = 0;

	/** Time point Column no for the wizard. */
	public static final int				TIME_COLUMN = 2;
	
	/** Z-Section Column no for the wizard. */
	public static final int				Z_COLUMN = 1;

	/** Type Column no for the wizard. */
	public static final int				SHAPE_COLUMN = 3;
	
	/** Annotation Column no for the wizard. */
	public static final int				ANNOTATION_COLUMN = 4;

	/** Show Column no for the wizard. */
	public static final int				SHOW_COLUMN = 5;
	
	/**
	 * Set the model to use ROI nodes and columns as a vector.
	 * 
	 * @param node root node for model.
	 * @param columns column names.
	 */
	public ROITableModel(ROINode node, Vector columns)
	{
		super(node, columns);
	}
	
	/**
	 * The node has been updated.
	 * @param node see above.
	 */
	public void nodeUpdated(ROINode node)
	{
		Object[] objects = new Object[2];
		objects[0] = getRoot();
		objects[1] = node.getParent();
		TreePath path = new TreePath(objects);
		modelSupport.fireChildChanged(path, 
				node.getParent().getIndex(node), node);
	}
	
	/**
	 * Set the value of column field of the node object to the value param.
	 * @param value the new value of the object.
	 * @param nodeObject the node.
	 * @param column the field.
	 */
	public void setValueAt(Object value, Object nodeObject, int column)
	{
		if (nodeObject instanceof ROINode)
		{
			ROINode node = (ROINode) nodeObject;
			if (column == ANNOTATION_COLUMN+1) {
				if (value == null) value = "";
				else if (value.equals("")) value = " ";
			}
				
			node.setValueAt(value, column);
		}
	}
		
	/**
	 * Get the value of the column field of the node.
	 * @param nodeObject the node.
	 * @param column the field.
	 * @return see above.
	 */
	public Object getValueAt(Object nodeObject, int column)
	{
		if (nodeObject instanceof ROINode)
		{
			ROINode roiNode = (ROINode) nodeObject;
			return roiNode.getValueAt(column);
		}
		return null;
	}
	
	/**
	 * Returns <code>true</code> if the cell can be edited, 
	 * <code>false</code> otherwise.
	 * 
	 * @param node The node of the tree.
	 * @param column The field to edit.
	 * @return See above.
	 */
	public boolean isCellEditable(Object node, int column) 
	{
		return isCellEditable((ROINode) node, column);
	}
 
	/**
	 * Get the class of each column.
	 * @param column the field in the data for the node.
	 * @return see above.
	 */
	public Class<?> getColumnClass(int column) 
	{
		switch (column)
		{
			case 0:
				return ROINode.class;
			case ROIID_COLUMN+1:
				return Long.class;
			case TIME_COLUMN+1:
				return Integer.class;
			case Z_COLUMN+1:
				return Integer.class;
			case SHAPE_COLUMN+1:
				return ShapeType.class;
			case ANNOTATION_COLUMN+1:
				return String.class;
			case SHOW_COLUMN+1:
				return Boolean.class;
			default:
				return null;
		}
	}

}