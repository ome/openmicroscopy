/*
 * org.openmicroscopy.shoola.util.ui.treetable.TreeTable 
 *
  *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2015 University of Dundee. All rights reserved.
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
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.swing.DefaultCellEditor;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.TreePath;

//Third-party libraries
import org.jdesktop.swingx.JXTreeTable;
import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.jdesktop.swingx.treetable.MutableTreeTableNode;
import org.jdesktop.swingx.treetable.TreeTableModel;

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.treetable.editors.BooleanCellEditor;
import org.openmicroscopy.shoola.util.ui.treetable.editors.NumberCellEditor;
import org.openmicroscopy.shoola.util.ui.treetable.editors.StringCellEditor;
import org.openmicroscopy.shoola.util.ui.treetable.model.OMETreeNode;
import org.openmicroscopy.shoola.util.ui.treetable.renderers.BooleanCellRenderer;
import org.openmicroscopy.shoola.util.ui.treetable.renderers.IconCellRenderer;
import org.openmicroscopy.shoola.util.ui.treetable.renderers.NumberCellRenderer;
import org.openmicroscopy.shoola.util.ui.treetable.renderers.SelectionHighLighter;

/** 
 * A customized {@link JXTreeTable}.
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
	
	/** A map of the default cell editors in the table.  */
	protected static final Map<Class<?>, DefaultCellEditor> DEFAULT_EDITORS;


	/** A map of the default cell renderers in the table. */
	protected static final Map<Class<?>, TableCellRenderer> DEFAULT_RENDERERS;
	
	static
	{
		DEFAULT_RENDERERS = new HashMap<Class<?>, TableCellRenderer>();
		DEFAULT_RENDERERS.put(Boolean.class, new BooleanCellRenderer());
		DEFAULT_RENDERERS.put(Long.class, new NumberCellRenderer());
		DEFAULT_RENDERERS.put(Integer.class, new NumberCellRenderer());
		DEFAULT_RENDERERS.put(Float.class, new NumberCellRenderer());
		DEFAULT_RENDERERS.put(Double.class, new NumberCellRenderer());
		DEFAULT_RENDERERS.put(String.class, 
								new NumberCellRenderer(SwingConstants.LEFT));
		DEFAULT_RENDERERS.put(Icon.class, new IconCellRenderer());
//		defaultTableRenderers.put(Color.class, new ColourCellRenderer());
		DEFAULT_EDITORS = new HashMap<Class<?>, DefaultCellEditor>();
		DEFAULT_EDITORS.put(Boolean.class, 
			//	new BooleanCellEditor(new JCheckBox()));
				new BooleanCellEditor((JCheckBox)
						DEFAULT_RENDERERS.get(Boolean.class)));
		DEFAULT_EDITORS.put(Integer.class, 
							new NumberCellEditor(new JTextField()));
		DEFAULT_EDITORS.put(String.class, 
							new StringCellEditor(new JTextField()));
	}

	/** Tree expansion listener. */
	protected TreeExpansionListener	treeExpansionListener;

	/** The mouse listener. */
	//protected MouseListener			mouseListener;
	
	/** Initializes the table. */
	private void initialize()
	{
		JLabel l = 
			(JLabel) getTableHeader().getDefaultRenderer();
		l.setHorizontalAlignment(SwingConstants.CENTER);
	}
	
	/** Creates a new instance. */
	public OMETreeTable()
	{
		super();
		initialize();
	}
	
	/**
	 * Create an instance of the treetable.
	 *  
	 * @param model The tree model.
	 */
	public OMETreeTable(TreeTableModel model)
	{
		super(model);
		setTableModel(model);
		initialize();
	}
	
	/**
	 * Sets the tree model.
	 * 
	 * @param model The value to set.
	 */
	public void setTableModel(TreeTableModel model)
	{
		setTreeTableModel(model);
	}
	
	/** Sets the default high lighter for this table. */
	protected void setDefaultHighLighter()
	{
		Highlighter h = HighlighterFactory.createAlternateStriping(
							UIUtilities.BACKGROUND_COLOUR_EVEN, 
							UIUtilities.BACKGROUND_COLOUR_ODD);
		addHighlighter(h);
		addHighlighter(new SelectionHighLighter(this));
	}
	
	/**
	 * Return <code>true</code> if the left button was clicked, 
	 * <code>false</code> otherwise.
	 * 
	 * @param e The mouse event to handle.
	 * @return See above.
	 */
	protected boolean leftClick(MouseEvent e)
	{
		return (e.getButton() == MouseEvent.BUTTON1);
	}
	
	/**
	 * Sets the mouse listener for mouse events and attach it to the methods
	 * onLeftMouseDown(), onRightMouseDown()
	 */
	protected void setListeners()
	{
		MouseListener mouseListener = new MouseListener()
		{

			public void mouseClicked(MouseEvent e) { onMouseClicked(e); }

			public void mouseEntered(MouseEvent e) { onMouseEnter(e); }

			public void mouseExited(MouseEvent e) { onMouseExit(e); }

			public void mousePressed(MouseEvent e) { onMousePressed(e); }

			public void mouseReleased(MouseEvent e) { onMouseReleased(e); }
			
		};
		addMouseListener(mouseListener);
	}
	
	/**
	 * MouseEvent called from mouseListener. 
	 * This Event responds to mouseClicked events.
	 * 
	 * @param e The mouse event to handle.
	 */
	protected void onMouseClicked(MouseEvent e) {}
	
	/**
	 * MouseEvent called from mouseListener. 
	 * This Event responds to mouse released events.
	 * 
	 * @param e The mouse event to handle.
	 */
	protected void onMouseReleased(MouseEvent e) {}
	
	/**
	 * MouseEvent called from mouseListener. 
	 * This Event responds to mouse pressed events.
	 * 
	 * @param e The mouse event to handle.
	 */
	protected void onMousePressed(MouseEvent e) {}
	
	/**
	 * MouseEvent called from mouseListener. 
	 * This Event responds to mouse enter events.
	 * 
	 * @param e The mouse event to handle.
	 */
	protected void onMouseEnter(MouseEvent e) {}

	/**
	 * MouseEvent called from mouseListener. 
	 * This Event responds to mouseExit events.
	 * 
	 * @param e The mouse event to handle.
	 */
	protected void onMouseExit(MouseEvent e) {}

	/**
	 * Sets the tree expansion listener for the tree. 
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
	 * 
	 * @param e 		The tree event.
	 * @param expanded 	Pass <code>true</code> if the node was expanded,
	 * 					<code>false</code> otherwise.
	 */
	protected void onNodeNavigation(TreeExpansionEvent e, boolean expanded)
	{
		OMETreeNode node = (OMETreeNode) e.getPath().getLastPathComponent();
        node.setExpanded(expanded);
	}
	
	/**
	 * Sets the default editors for the cells in the table. This includes
	 * editors for cells containing: int, long, string, booleans,
	 * floats, longs, doubles. 
	 */
	protected void setDefaultEditors()
	{
		Iterator<Class<?>> classIterator = DEFAULT_EDITORS.keySet().iterator();
		Class<?> classType;
		DefaultCellEditor editorType;
		while(classIterator.hasNext()) {
			classType = classIterator.next();
			editorType = DEFAULT_EDITORS.get(classType);
			this.setDefaultEditor(classType, editorType);
		}
	}
	
	/**
	 * Sets the default renderers for the cells in the table. This includes
	 * renderers for cells containing: dates, int, long, string, booleans,
	 * floats, longs, doubles, colour. 
	 */
	protected void setDefaultRenderers()
	{
		Iterator<Class<?>> 
			classIterator = DEFAULT_RENDERERS.keySet().iterator();
		Class<?> classType;
		TableCellRenderer rendererType;
		while (classIterator.hasNext()) {
			classType = classIterator.next();
			rendererType = DEFAULT_RENDERERS.get(classType);
			setDefaultRenderer(classType, rendererType);
		}
	}
	
	/**
	 * Expands the row with the node in it.
	 * 
	 * @param node The node to handle.
	 */
	public void expandNode(OMETreeNode node)
	{ 
		if (node != null) expandPath(node.getPath());
	}
	
	/**
	 * Collapses the row with the node in it.
	 * 
	 * @param node  The node to handle.
	 */
	public void collapseNode(OMETreeNode node)
	{ 
		if (node != null) collapsePath(node.getPath());
	}
	
	/**
	 * Overrides the {@link JXTreeTable#expandPath(TreePath)}
	 * Adds extra control to set the expanded flag in the OMETreeNode.
	 * @see JXTreeTable#expandPath(TreePath)
	 */
	public void expandPath(TreePath path)
	{
		super.expandPath(path);
		if (path == null) return;
		OMETreeNode node = (OMETreeNode) path.getLastPathComponent();
		if (node != null) node.setExpanded(true);
	}
	
	/**
	 * Overrides the {@link JXTreeTable#expandRow(int)}
	 * Adds extra control to set the expanded flag in the OMETreeNode.
	 * @see JXTreeTable#expandRow(int)
	 */
	public void expandRow(int row)
	{
		super.expandRow(row);
		OMETreeNode node = getNodeAtRow(row);
		if (node != null) node.setExpanded(true);
	}
	
	/**
	 * Overrides the {@link JXTreeTable#expandAll()}
	 * Adds extra control to set the expanded flag in the OMETreeNode.
	 * @see JXTreeTable#expandAll()
	 */
	public void expandAll()
	{
		super.expandAll();
		MutableTreeTableNode root = 
			(MutableTreeTableNode) getTreeTableModel().getRoot();
		if (root == null) return;
		for (MutableTreeTableNode node : ((OMETreeNode) root).getChildList())
			((OMETreeNode) node).setExpanded(true);
	}

	/**
	 * Helper method to get the node at row.
	 * 
	 * @param row The selected row.
	 * @return See above.
	 */
	public OMETreeNode getNodeAtRow(int row)
	{
		TreePath path = getPathForRow(row);
		if (path == null) return null;
		return (OMETreeNode) path.getLastPathComponent();
	}
	
	/**
	 * Gets the row a node is at.
	 * 
	 * @param node The node.
	 * @return See above.
	 */
	public int getRow(OMETreeNode node)
	{
		if (node == null) return -1;
		return getRowForPath(node.getPath());
	}
	
	/**
	 * Returns <code>true</code>  if the cell editable for this node and column,
	 * <code>false</code> otherwise.
	 * 
	 * @param node 		The node of the tree.
	 * @param column 	The field to edit.
	 * @return See above.
	 */
	public boolean isCellEditable(Object node, int column) 
	{
		return getTreeTableModel().isCellEditable(node, column);
	}
	
	/**
	 * Selects a node. 
	 * 
	 * @param node The node to select.
	 */
	public void selectNode(OMETreeNode node)
	{
		int row = getRow(node);
		selectionModel.addSelectionInterval(row, row);
	}
	
	/**
	 * Overrides the {@link JXTreeTable#collapsePath(TreePath)}
	 * Adds extra control to set the expanded flag in the OMETreeNode.
	 * @see JXTreeTable#collapsePath(TreePath)
	 */
	public void collapsePath(TreePath path)
	{
		super.collapsePath(path);
		if (path == null) return;
		OMETreeNode node = (OMETreeNode) path.getLastPathComponent();
		if (node != null) node.setExpanded(false);
	}
	
	/**
	 * Overrides the {@link JXTreeTable#collapseRow(int)}
	 * Adds extra control to set the expanded flag in the OMETreeNode.
	 * @see JXTreeTable#collapseRow(int)
	 */
	public void collapseRow(int row)
	{
		super.collapseRow(row);
		OMETreeNode node = getNodeAtRow(row);
		if (node != null) node.setExpanded(false);
	}
	
	/**
	 * Overrides the {@link JXTreeTable#collapseAll()}
	 * Adds extra control to set the expanded flag in the OMETreeNode.
	 * @see JXTreeTable#collapseAll()
	 */
	public void collapseAll()
	{
		super.collapseAll();
		MutableTreeTableNode root = 
			(MutableTreeTableNode) getTreeTableModel().getRoot();
		if (root == null) return;
		for (MutableTreeTableNode node : ((OMETreeNode) root).getChildList())
			((OMETreeNode) node).setExpanded(true);
	}
	
	/**
	 * Overridden to set the various renderer.
	 * @see JXTreeTable#setTreeTableModel(TreeTableModel)
	 */
	public void setTreeTableModel(TreeTableModel model)
	{
		super.setTreeTableModel(model);
		setColumnSelectionAllowed(false);
		setRowSelectionAllowed(true);
		setCellSelectionEnabled(false);
		setTreeExpansionListener();
		setListeners();
		setDefaultRenderers();
		setDefaultEditors();
		setDefaultHighLighter();
	}
	
}


