/*
 * org.openmicroscopy.shoola.util.ui.treetable.TreeTable 
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
package org.openmicroscopy.shoola.util.ui.treetable;


//Java imports
import java.awt.Color;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JTextField;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.TreePath;

//Third-party libraries

import org.jdesktop.swingx.JXTreeTable;
import org.jdesktop.swingx.treetable.TreeTableModel;

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.treetable.editors.BooleanCellEditor;
import org.openmicroscopy.shoola.util.ui.treetable.editors.NumberCellEditor;
import org.openmicroscopy.shoola.util.ui.treetable.editors.StringCellEditor;
import org.openmicroscopy.shoola.util.ui.treetable.model.OMETreeNode;
import org.openmicroscopy.shoola.util.ui.treetable.renderers.BooleanCellRenderer;
import org.openmicroscopy.shoola.util.ui.treetable.renderers.ColourCellRenderer;
import org.openmicroscopy.shoola.util.ui.treetable.renderers.NumberCellRenderer;
import org.openmicroscopy.shoola.util.ui.treetable.renderers.StringCellRenderer;

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
public class OMETreeTable
	extends JXTreeTable
{	
	/** 
	 * A map of the default cell editors in the table. 
	 */
	private static HashMap<Class<?>, DefaultCellEditor> defaultEditors;
	
	static
	{
		defaultEditors = new HashMap<Class<?>, DefaultCellEditor>();
		defaultEditors.put(Boolean.class, new BooleanCellEditor(new JCheckBox()));
		defaultEditors.put(Integer.class, new NumberCellEditor(new JTextField()));
		defaultEditors.put(String.class, new StringCellEditor(new JTextField()));
	}

	/** 
	 * A map of the default cell renderers in the table. 
	 */
	private static HashMap<Class<?>, TableCellRenderer> defaultTableRenderers;
	
	static
	{
		defaultTableRenderers = 
							new HashMap<Class<?>, TableCellRenderer>();
		defaultTableRenderers.put(Boolean.class, new BooleanCellRenderer());
//		defaultTableRenderers.put(Integer.class, new NumberCellRenderer());
//		defaultTableRenderers.put(String.class, new StringCellRenderer());
//		defaultTableRenderers.put(Color.class, new ColourCellRenderer());
//		defaultTableRenderers.put(String.class, new StringCellRenderer());
	}
	
	/** A reference to the tree table model. */
	TreeTableModel						model;
	
	/** Tree expansion listener. */
	private TreeExpansionListener 		treeExpansionListener;
	
	/**
	 * Create an instance of the treetable. 
	 * @param model the tree model.
	 */
	public OMETreeTable(TreeTableModel model)
	{
		super(model);
		this.model = model;
		setTreeExpansionListener();
		setDefaultRenderers();
		setDefaultEditors();
	}
	
	/**
	 * Set the tree expansion listener for the tree. 
	 * Attach the collapse and expand events to the onNodeNavigation
	 * method.
	 */
	protected void setTreeExpansionListener()
	{
		treeExpansionListener = new TreeExpansionListener() 
		{
			public void treeCollapsed(TreeExpansionEvent e) 
			{
                onNodeNavigation(e, false);
            }
            public void treeExpanded(TreeExpansionEvent e) 
            {
                onNodeNavigation(e, true);  
            }   
        };
        addTreeExpansionListener(treeExpansionListener);
	}
	
	/**
	 * This method is called when a node in the tree is expanded or 
	 * collapsed. 
	 * @param e the tree event.
	 * @param expanded true if the node was expanded.
	 */
	protected void onNodeNavigation(TreeExpansionEvent e, boolean expanded)
	{
		OMETreeNode node = (OMETreeNode) e.getPath().getLastPathComponent();
        node.setExpanded(expanded);
	}
	
	/**
	 * Set the default editors for the cells in the table. This includes
	 * editors for cells containing: int, long, string, booleans,
	 * floats, longs, doubles. 
	 *
	 */
	private void setDefaultEditors()
	{
		Iterator<Class<?>> classIterator = defaultEditors.keySet().iterator();
		while(classIterator.hasNext())
		{
			Class<?> classType = classIterator.next();
			DefaultCellEditor editorType = defaultEditors.get(classType);
			this.setDefaultEditor(classType, editorType);
		}
	}
	
	/**
	 * Set the default renderers for the cells in the table. This includes
	 * renderers for cells containing: dates, int, long, string, booleans,
	 * floats, longs, doubles, colour. 
	 *
	 */
	private void setDefaultRenderers()
	{
		Iterator<Class<?>> classIterator = defaultTableRenderers.keySet().iterator();
		while(classIterator.hasNext())
		{
			Class<?> classType = classIterator.next();
			TableCellRenderer rendererType = defaultTableRenderers.get(classType);
			this.setDefaultRenderer(classType, rendererType);
		}
	}
	
	/**
	 * Overrides the {@link JXTreeTable#expandPath(TreePath)}
	 * Adds extra control to set the expanded flag in the OMETreeNode.
	 */
	public void expandPath(TreePath path)
	{
		super.expandPath(path);
		OMETreeNode node = (OMETreeNode)path.getLastPathComponent();
		node.setExpanded(true);
	}
	
	/**
	 * Overrides the {@link JXTreeTable#expandRow(int)}
	 * Adds extra control to set the expanded flag in the OMETreeNode.
	 */
	public void expandRow(int row)
	{
		super.expandRow(row);
		OMETreeNode node = getNodeAtRow(row);
		node.setExpanded(true);
	}
	
	/**
	 * Overrides the {@link JXTreeTable#expandAll()}
	 * Adds extra control to set the expanded flag in the OMETreeNode.
	 */
	public void expandAll()
	{
		super.expandAll();
		OMETreeNode rootNode = (OMETreeNode)this.getTreeTableModel().getRoot();
		for(OMETreeNode node : rootNode.getChildList())
			node.setExpanded(true);
	}
	
	/**
	 * Expand the row with the node in it.
	 * @param node see above.
	 */
	public void expandNode(OMETreeNode node)
	{
		expandPath(node.getPath());
	}
	
	/**
	 * Overrides the {@link JXTreeTable#collapsePath(TreePath)}
	 * Adds extra control to set the expanded flag in the OMETreeNode.
	 */
	public void collapsePath(TreePath path)
	{
		super.collapsePath(path);
		OMETreeNode node = (OMETreeNode)path.getLastPathComponent();
		node.setExpanded(false);
	}
	
	/**
	 * Overrides the {@link JXTreeTable#collapseRow(int)}
	 * Adds extra control to set the expanded flag in the OMETreeNode.
	 */
	public void collapseRow(int row)
	{
		super.collapseRow(row);
		OMETreeNode node = getNodeAtRow(row);
		node.setExpanded(false);
	}
	
	/**
	 * Overrides the {@link JXTreeTable#collapseAll()}
	 * Adds extra control to set the expanded flag in the OMETreeNode.
	 */
	public void collapseAll()
	{
		super.collapseAll();
		OMETreeNode rootNode = (OMETreeNode)this.getTreeTableModel().getRoot();
		for(OMETreeNode node : rootNode.getChildList())
			node.setExpanded(false);
	}
	
	/**
	 * Collapse the row with the node in it.
	 * @param node see above.
	 */
	public void collapseNode(OMETreeNode node)
	{
		collapsePath(node.getPath());
	}
	
	/**
	 * Helper method to get the node at row.
	 * @param row see above.
	 * @return see above.
	 */
	public OMETreeNode getNodeAtRow(int row)
	{
		return (OMETreeNode)getPathForRow(row).getLastPathComponent();
	}
	
	/**
	 * Get the row a node is at.
	 * @param node the node.
	 * @return the row.
	 */
	public int getRow(OMETreeNode node)
	{
		return getRowForPath(node.getPath());
	}
	
	/**
	 * Is the cell editable for this node and column.
	 * @param node the node of the tree.
	 * @param column the field to edit.
	 * @return see above.
	 */
	public boolean isCellEditable(Object node, int column) 
	{
		return model.isCellEditable(node, column);
	}
	
	/**
	 * Select a node. 
	 * @param node see above.
	 */
	public void selectNode(OMETreeNode node)
	{
		int row = getRow(node);
		selectionModel.addSelectionInterval(row, row);
	}
}


