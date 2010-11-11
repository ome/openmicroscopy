/*
 * org.openmicroscopy.shoola.util.ui.treetable.OMETreeTableModel 
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
package org.openmicroscopy.shoola.util.ui.treetable.model;


//Java imports
import java.util.Map;
import java.util.Vector;

//Third-party libraries
import org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode;
import org.jdesktop.swingx.treetable.DefaultTreeTableModel;

//Application-internal dependencies

/** 
 * The model of an <code>OMETree</code>.
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
public class OMETreeTableModel
	extends DefaultTreeTableModel
{	
	
	/** Map indicating how to render each column. */
	private Map<Integer, Class> columnsRenderer;
	
	/**
	 * Sets the model to use OMETreeNodes and columns as a vector.
	 * 
	 * @param node 				The root node for model.
	 * @param columns 			The column names.
	 * @param columnsRenderer 	The map indicating how to render the columns.
	 */
	public OMETreeTableModel(OMETreeNode node, Vector columns, 
						Map<Integer, Class> columnsRenderer)
	{
		super(node, columns);
		this.columnsRenderer = columnsRenderer;
	} 
	
	/**
	 * Sets the model to use OMETreeNodes and columns as a vector.
	 * 
	 * @param node 		The root node for model.
	 * @param columns 	The column names.
	 */
	public OMETreeTableModel(OMETreeNode node, Vector columns)
	{
		this(node, columns, null);
	} 
	
	/**
	 * Returns <code>true</code> if the cell can be edited for this node and 
	 * column, <code>false</code> otherwise.
	 * 
	 * @param node 		The node of the tree.
	 * @param column 	The field to edit.
	 * @return See above.
	 */
	public boolean isCellEditable(DefaultMutableTreeTableNode node, int column) 
	{
		return node.isEditable(column);
	}
	
	/**
	 * Returns the value of the column field of the node.
	 * 
	 * @param node 		The selected node.
	 * @param column	The selected column.
	 * @return see above.
	 */
	public Object getValueAt(Object node, int column)
	{
		if (node instanceof OMETreeNode)
			return ((OMETreeNode) node).getValueAt(column);
		
		return null;
	}
	
	/**
	 * Sets the map indicating how to render each column.
	 * 
	 * @param columnsRenderer The value to set.
	 */
	public void setColumnsRenderer(Map<Integer, Class> columnsRenderer)
	{
		this.columnsRenderer = columnsRenderer;
	}
	
	/**
	 * Returns the class corresponding to the specified column.
	 * 
	 * @param column The selected column.
	 * @return See above.
	 */
	public Class<?> getColumnClass(int column) 
	{
		if (columnsRenderer == null) return null;
		return columnsRenderer.get(column);
	}
	
}


