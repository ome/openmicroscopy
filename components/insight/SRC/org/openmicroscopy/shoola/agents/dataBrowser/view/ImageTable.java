/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2014 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
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
package org.openmicroscopy.shoola.agents.dataBrowser.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.SwingUtilities;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.tree.TreePath;

import org.jdesktop.swingx.JXTreeTable;
import org.openmicroscopy.shoola.agents.dataBrowser.Colors;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageDisplay;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageNode;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.RollOverNode;
import org.openmicroscopy.shoola.agents.dataBrowser.util.ImageTableIconRenderer;
import org.openmicroscopy.shoola.agents.dataBrowser.util.ImageTableRenderer;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.treetable.OMETreeTable;
import org.openmicroscopy.shoola.util.ui.treetable.model.OMETreeNode;
import org.openmicroscopy.shoola.util.ui.treetable.model.OMETreeTableModel;
import org.openmicroscopy.shoola.util.ui.treetable.renderers.NumberCellRenderer;

import omero.gateway.model.DataObject;
import omero.gateway.model.ImageData;

/** 
 * Tree table displaying the hierarchy.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since OME3.0
 */
class ImageTable
	extends OMETreeTable
{

	/** Identified the column displaying the file's path. */
	static final int  					NAME_COL = 0;
	
	/** Identified the column displaying the added date. */
	//static final int  					DATE_COL = 1;
	
	/** Identified the column displaying the added date. */
	static final int  					THUMBNAIL_COL = 1;
	
	/** The text of the {@link #NAME_COL}. */
	static final String					NAME = "Name";
	
	/** The text of the {@link #SIZE_COL}. */
	static final String					SIZE = "Size";
	
	/** The text of the {@link #THUMBNAIL_COL}. */
	static final String					VIEW = "Image";
	
	/** The text of the {@link #LOGIN_NAME_COL}. */
	static final String					LOGIN_NAME = "User Name";
	
	/** The text of the {@link #DATE_COL}. */
	static final String					DATE = "Acquired";
	
	/** The text of the {@link #ANNOTATED_COL}. */
	static final String					ANNOTATED = "Annotated";
	
	/** The text of the {@link #INSTITUTION_COL}. */
	static final String					INSTITUTION = "Institution";
	
	/** The columns of the table. */
	static Vector<String>		COLUMNS;
	
	/** Map indicating how to render each column. */
	private static Map<Integer, Class> RENDERERS;
	
	/** The columns of the table. */
	static Vector<String>		COLUMNS_GROUPS;
	
	/** Map indicating how to render each column. */
	private static Map<Integer, Class> RENDERERS_GROUPS;
	
	static {
		COLUMNS = new Vector<String>(2);
		COLUMNS.add(NAME);
		//COLUMNS.add(DATE);
		COLUMNS.add(VIEW);
		
		RENDERERS = new HashMap<Integer, Class>();
		RENDERERS.put(NAME_COL, ImageTableNode.class);
		//RENDERERS.put(DATE_COL, String.class);
		RENDERERS.put(THUMBNAIL_COL, ImageTableNode.class);
		
		COLUMNS_GROUPS = new Vector<String>(3);
		COLUMNS_GROUPS.add(LOGIN_NAME);
		COLUMNS_GROUPS.add(NAME);
		COLUMNS_GROUPS.add(INSTITUTION);
		RENDERERS_GROUPS = new HashMap<Integer, Class>();
	}
	
	/** The root node of the table. */
	private ImageTableNode 			tableRoot;
	
	/** Listen to the node selection. */
	private TreeSelectionListener	selectionListener;

	/** The component hosting the table. */
	private ImageTableView			view;
	
	/** Collection used when visiting the tree structure. */
	private List<ImageTableNode> 	nodes;
	
	/** Reference to the model. */
	private DataBrowserModel 		model;
	
	/** The default height of row.*/
	private int						rowHeight;
	
	/** Flag indicating if the mouse has entered the table.*/
	private boolean entered;
	
	/**
	 * Displays the node if roll over flag is <code>one</code> when the mouse 
	 * enters the table or is over the table.
	 * 
	 * @param p The location of the mouse.
	 */
	private void handleRollOver(Point p)
	{
		int column = columnAtPoint(p);
		if (column != THUMBNAIL_COL) {
			view.rollOverNode(null);
			return;
		}
		OMETreeNode n = getNodeAtRow(rowAtPoint(p));
		if (n instanceof ImageTableNode) {
			ImageTableNode node = (ImageTableNode) n;
			Object ho = node.getHierarchyObject();
			if (ho instanceof ImageData) {
				ImageNode image = (ImageNode) node.getUserObject();
				SwingUtilities.convertPointToScreen(p, this);
				view.rollOverNode(new RollOverNode(image, p));
			}
		}
	}
	
	/**
	 * Builds the node.
	 * 
	 * @param parent 	The parent node.
	 * @param comp		The nodes to add to the parent.
	 */
	private void buildTreeNode(ImageTableNode parent, List comp)
	{
		if (parent == null) return;
		if (comp == null || comp.size() == 0) return;
		ImageDisplay display;
		ImageTableNode n;
		Object c;
		Iterator i = comp.iterator();
		while (i.hasNext()) {
			c = i.next();
			if (c instanceof ImageDisplay) {
				display = (ImageDisplay) c;
				n = new ImageTableNode(display);
				parent.insert(n, parent.getChildCount());
				buildTreeNode(n, view.getSorter().sort(
						display.getInternalDesktop().getComponents()));
			}
		}
	}
	
	/** Reacts to nodes selection in the tree. */
	private void onNodeSelection()
	{
		TreePath[] paths = getTreeSelectionModel().getSelectionPaths();
    	if (paths == null) return;
        int n = paths.length;
        if (n == 0) return;
        
        List<ImageDisplay> nodes = new ArrayList<ImageDisplay>();
        Object node;
        for (int i = 0; i < n; i++) {
			node = paths[i].getLastPathComponent();
			if (node instanceof ImageTableNode)
				nodes.add((ImageDisplay)
						((ImageTableNode) node).getUserObject());
		}
        view.selectNodes(nodes);
	}
	
	/** Formats the table.*/
	private void formatTable()
	{
		OMETreeTableModel om;
		if (model.getType() == DataBrowserModel.GROUP)
			om = new OMETreeTableModel(tableRoot, COLUMNS_GROUPS, 
					RENDERERS_GROUPS);
		else 
			om = new OMETreeTableModel(tableRoot, COLUMNS, RENDERERS);
		double f = view.getMagnification();
		if (f <= ImageTableNode.MIN_FACTOR)
			setRowHeight(ImageTableNode.MIN_HEIGHT);
		else if (f > ImageTableNode.MEDIUM_FACTOR)
			setRowHeight(ImageTableNode.MAX_HEIGHT);
		else 
			setRowHeight(ImageTableNode.MEDIUM_HEIGHT);
		setTableModel(om);
		TableColumnModel tcm = getColumnModel();
		TableColumn tc = tcm.getColumn(THUMBNAIL_COL);
		tc.setCellRenderer(new ImageTableIconRenderer()); 
		setDefaultRenderer(String.class, new NumberCellRenderer());
	}
	
	/** Initializes the component. */
	private void initialize()
	{
		setBackground(UIUtilities.BACKGROUND_COLOR);
		nodes  = new ArrayList<ImageTableNode>();
		formatTable();
		setTreeCellRenderer(new ImageTableRenderer());
		setAutoResizeMode(JXTreeTable.AUTO_RESIZE_ALL_COLUMNS);
		setDefaultRenderer(String.class, new NumberCellRenderer());
		//setDefaultRenderers();
		setRootVisible(false);
		setColumnSelectionAllowed(true);
		setRowSelectionAllowed(true);
		setHorizontalScrollEnabled(true);
		setColumnControlVisible(true);
		selectionListener = new TreeSelectionListener() {
	        
            public void valueChanged(TreeSelectionEvent e)
            {
            	onNodeSelection();
            }
        };
        addTreeSelectionListener(selectionListener);
        addMouseMotionListener(new MouseMotionListener() {
			
			public void mouseMoved(MouseEvent e) {
				handleRollOver(e.getPoint());
			}
			
			public void mouseDragged(MouseEvent e) {}
		});
	}
	
	/**
	 * Visits the tree.
	 * 
	 * @param node		The node to visit.
	 * @param objects	The collection of <code>DataObject</code> to search for.
	 */
	private void visitAllNodes(ImageTableNode node, List<DataObject> objects)
	{
        // node is visited exactly once
        //process(node);
		if (objects == null) return;
		Object ho = node.getHierarchyObject();
		if (ho instanceof DataObject) {
			Iterator<DataObject> i = objects.iterator();
			DataObject object = (DataObject) ho;
			long objectId = object.getId();
			DataObject data;
			while (i.hasNext()) {
				data = i.next();
				if (data.getId() == objectId) {
					nodes.add(node);
					break;
				}
			}
		}
    
        if (node.getChildCount() >= 0) {
            for (Enumeration e = node.children(); e.hasMoreElements();) {
                visitAllNodes((ImageTableNode) e.nextElement(), objects);
            }
        }
    }

	/**
	 * Visits the table-tree and recurrently checks each element for the
	 * fileset id. Applies different highlighting if needed. In the future,
	 * this might become a stand-alone visitor.
	 *
	 * @param node The starting tree root node.
	 * @param selectedNodes The nodes selected in the UI by the user.
	 */
	private void visitAllNodesToHighlight(ImageTableNode node,
			List<ImageDisplay> selectedNodes) {
		if (selectedNodes == null) {
			return;
		}
		Color borderColor = Colors.getInstance().getColor(
				Colors.TITLE_BAR_HIGHLIGHT_SECONDARY);
		Object ho = node.getHierarchyObject();
		if (ho instanceof DataObject) {
			DataObject nodeDataObject = (DataObject) ho;
			long nodeId = nodeDataObject.getId();
			ImageData selected, sibling;
			Object refNode;
			for (ImageDisplay display : selectedNodes) {
				refNode = display.getHierarchyObject();
				node.setSibingColor(null);
				if (refNode instanceof ImageData) {
					selected = (ImageData) refNode;
					if (nodeDataObject instanceof ImageData) {
					    sibling = (ImageData) nodeDataObject;
					    if (selected.getId() != nodeId &&
	                            selected.getFilesetId() == sibling.getFilesetId()) {
	                        node.setSibingColor(borderColor);
	                        break;
	                    }
					}
				}
			}
		}

		if (node.getChildCount() >= 0) {
			for (Enumeration e = node.children(); e.hasMoreElements();) {
				visitAllNodesToHighlight((ImageTableNode) e.nextElement(),
						selectedNodes);
			}
		}
	}

	/**
	 * Visits the tree.
	 * 
	 * @param node	The node to visit.
	 * @param type	The type of node to handle.
	 * @param ids	The collection of <code>DataObject</code>'s ids.
	 */
	private void visitAllNodes(ImageTableNode node, Class type, 
			Collection<Long> ids)
	{
		if (node == null | ids == null) return;
		Object ho = node.getHierarchyObject();
		if (ho == null) return;
		if (ho.getClass().equals(type) && ho instanceof DataObject) {
			Iterator<Long> i = ids.iterator();
			DataObject object = (DataObject) ho;
			long objectId = object.getId();
			Long id;
			while (i.hasNext()) {
				id = i.next();
				if (id == objectId) {
					nodes.add(node);
					break;
				}
			}
		}
    
        if (node.getChildCount() >= 0) {
            for (Enumeration e = node.children(); e.hasMoreElements();) {
                visitAllNodes((ImageTableNode) e.nextElement(), type, ids);
            }
        }
    }
	
	/**
	 * Creates a new instance.
	 * 
	 * @param root The root of the tree.
	 * @param view The component hosting that table.
	 * @param model Reference to the model.
	 */
	ImageTable(ImageDisplay root, ImageTableView view, DataBrowserModel model)
	{
		super();
		if (view == null)
			throw new IllegalArgumentException("No view");
		if (model == null)
			throw new IllegalArgumentException("No Model");
		this.view = view;
		this.model = model;
		tableRoot = new ImageTableNode(root);
		Component[] comp = root.getInternalDesktop().getComponents();
		buildTreeNode(tableRoot, view.getSorter().sort(comp));
		initialize();
	}

	/** Refreshes the table. */
	void refreshTable()
	{
		ImageDisplay root = ((ImageDisplay) tableRoot.getUserObject());
		tableRoot = new ImageTableNode(root);
		Component[] comp = root.getInternalDesktop().getComponents();
		buildTreeNode(tableRoot, view.getSorter().sort(comp));
		formatTable();
		invalidate();
		repaint();
	}

	/**
	 * Sets the selected nodes, the nodes have been selected via other views.
	 * 
	 * @param objects The selected objects.
	 */
	void setSelectedNodes(List<DataObject> objects)
	{
		removeTreeSelectionListener(selectionListener);
		visitAllNodes(tableRoot, objects);
		Iterator<ImageTableNode> i = nodes.iterator();
		ImageTableNode node;
		int row = 0;
		selectionModel.clearSelection();
		while (i.hasNext()) {
			node = i.next();
			row = getRowForPath(node.getPath());
			selectionModel.addSelectionInterval(row, row);
		}
		nodes.clear();
		repaint();
		addTreeSelectionListener(selectionListener);
	}

	/**
	 * Sets the highlighting for nodes with the same fileset id as the supplied
	 * nodes.
	 *
	 * @param nodes The list of user-selected nodes.
	 */
	void setHighlightedNodes(List<ImageDisplay> objects)
	{
		visitAllNodesToHighlight(tableRoot, objects);
		repaint();
	}

	/**
	 * Marks the nodes on which a given operation could not be performed
	 * e.g. paste rendering settings.
	 * 
	 * @param type The type of data objects.
	 * @param ids  Collection of object's ids.
	 */
	void markUnmodifiedNodes(Class type, Collection<Long> ids)
	{
		removeTreeSelectionListener(selectionListener);
		visitAllNodes(tableRoot, type, ids);
		Iterator<ImageTableNode> i = nodes.iterator();
		ImageTableNode node;
		Object ho;
		int row = 0;
		long id;
		selectionModel.clearSelection();
		while (i.hasNext()) {
			node = i.next();
			ho = node.getHierarchyObject();
			if (ho.getClass().equals(type) && ho instanceof DataObject) {
				id = ((DataObject) ho).getId();
				if (ids.contains(id)) {
					row = getRowForPath(node.getPath());
					selectionModel.addSelectionInterval(row, row);
				}
			}
		}
		nodes.clear();
		repaint();
		addTreeSelectionListener(selectionListener);
	}

	/**
	 * Overridden to pop up a menu when the user right-clicks on a selected 
	 * item.
	 * @see OMETreeTable#onMousePressed(MouseEvent)
	 */
	protected void onMousePressed(MouseEvent e)
	{
		if (e.isPopupTrigger()) view.showMenu(e.getPoint());
	}
	
	/**
	 * Overridden to pop up a menu when the user right-clicks on a selected 
	 * item.
	 * @see OMETreeTable#onMouseReleased(MouseEvent)
	 */
	protected void onMouseReleased(MouseEvent e)
	{
		int count = e.getClickCount();
		if (count == 2) { 
			if (model.getType() != DataBrowserModel.GROUP)
				view.viewSelectedNode();
		} else {
			if (e.isPopupTrigger()) view.showMenu(e.getPoint());
		}
	}
	
	/**
	 * Overridden to display a larger thumbnail when mousing over.
	 * @see OMETreeTable#onMouseEnter(MouseEvent)
	 */
	protected void onMouseEnter(MouseEvent e)
	{
		handleRollOver(e.getPoint());
	}
	
	/**
	 * Overridden to hide the thumbnail.
	 * @see OMETreeTable#onMouseExited(MouseEvent)
	 */
	protected void onMouseExited(MouseEvent e)
	{
		view.rollOverNode(null);
	}

}

