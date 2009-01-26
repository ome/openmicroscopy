 /*
 * org.openmicroscopy.shoola.agents.editor.browser.TextAreasView 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.editor.browser;

//Java imports

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.JViewport;
import javax.swing.Scrollable;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

//Third-party libraries

//Application-internal dependencies

import org.openmicroscopy.shoola.agents.editor.model.IField;
import org.openmicroscopy.shoola.agents.editor.model.TreeIterator;

/** 
 * This is the UI panel that displays the tree model in a "Text Document" 
 * representation.
 * Contains a number of text components, corresponding to the fields (or nodes)
 * of the tree, but these are not arranged hierarchically. 
 * This class implements {@link TreeModelListener} and 
 * {@link TreeSelectionListener} interfaces to respond to changes in the 
 * model and navigation JTree. 
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class TextAreasView
	extends JPanel 
	implements Scrollable,
	TreeModelListener,
	TreeSelectionListener
	{
	
	/**
	 * A reference to the tree UI used for selection coordination.
	 */
	private JTree 				navTree;
	
	/**
	 * Controller for edits etc.
	 */
	private BrowserControl 		controller;
	
	/**
	 * The model that this UI represents. 
	 * This class is a {@link TreeModelListener} of this treeModel. 
	 */
	private TreeModel 			treeModel;
	
	/**
	 * A collection of all the fields displayed, mapped to their path in the
	 * treeModel. 
	 */
	private Map<TreePath, FieldTextArea> textAreas;
	 
	/**
	 * Refreshes the contents of this UI. 
	 * Removes all the text components and rebuilds the UI with new 
	 * text components based on the {@link #treeModel}. 
	 * This method is called when the {@link #treeModel} changes structure. 
	 */
	private void refreshTreeDisplay() {
		
		removeAll();
		textAreas.clear();
		
		if (treeModel != null)	{
		
			Object r = treeModel.getRoot();
			if (! (r instanceof TreeNode)) 		return;
			TreeNode root = (TreeNode)r;
			
			Iterator<TreeNode> iterator = new TreeIterator(root);
			
			TreeNode tn;
			IField f;
			FieldTextArea tc;
			Object userOb;
			DefaultMutableTreeNode node;
			TreePath path;
			while (iterator.hasNext()) {
				tn = iterator.next();
				if (tn instanceof DefaultMutableTreeNode) {
					node = (DefaultMutableTreeNode)tn;
					path = new TreePath(node.getPath());
					userOb = node.getUserObject();
					if (userOb instanceof IField) {
						f = (IField)userOb;
						tc = new FieldTextArea(f, navTree, node, controller);
						textAreas.put(path, tc);
						add(tc);
					}
				}
			}
		}
		
		JPanel spacer = new JPanel();
		spacer.setBackground(null);
		add(spacer);
		revalidate();
		repaint();
	}

	/**
	 * Iterates through all the text components contained in this UI and 
	 * if they are {@link FieldTextArea} objects, the selection is refreshed.
	 * This is needed to de-select all fields that are not selected.
	 * If the parent of this UI is a {@link JViewport}, scroll to selected field
	 * 
	 * This method is called when the {@link #navTree} selection changes.
	 */
	private void refreshSelection()
	{
		Component comp;
		for (int i=0; i<getComponentCount(); i++) {
			comp = getComponent(i);
			if (comp instanceof FieldTextArea) {
				((FieldTextArea)comp).refreshSelection();
			}
			
			if (navTree.getSelectionCount() == 0)	return;
			
			TreePath path = navTree.getSelectionPath();
			JPanel selectedField = textAreas.get(path);
			if (selectedField != null) {
				Rectangle rect = selectedField.getBounds();
				int y = (int)rect.getY();
				
				if (getParent() instanceof JViewport) {
					JViewport scroller = (JViewport)getParent();
					// if the selected field is not wholly visible..
					// scroll to show it.
					//scroller.scrollRectToVisible() doesn't work up the page!
					if (! scroller.getViewRect().contains(rect)) {
						scroller.setViewPosition(new Point(0, y));
					}
				}
			}
		}
	}
	
	

	/**
	 * Creates an instance. 
	 * 
	 * @param tree			Navigation tree for selection
	 * @param controller	Controller for edits.
	 */
	TextAreasView(JTree tree, BrowserControl controller) 
	{
		this.navTree = tree;
		this.controller = controller;
		
		textAreas = new HashMap<TreePath, FieldTextArea>();
		
		if (navTree != null) {
			navTree.addTreeSelectionListener(this);
		}
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBackground(Color.white);
		setBorder(new EmptyBorder(10,10,10,10));
	}
	
	/**
	 * Sets the model and calls {@link #refreshTreeDisplay()}
	 * 
	 * @param treeModel		The new treeModel
	 */
	void setTreeModel(TreeModel treeModel)
	{
		this.treeModel = treeModel;
		if (treeModel != null)
			treeModel.addTreeModelListener(this);
		
		refreshTreeDisplay();
	}
	
	/**
	 * Implemented as specified by the {@link Scrollable} interface.
	 * Null implementation. 
	 */
	public Dimension getPreferredScrollableViewportSize() {
		return new Dimension(400, 200);
	}

	/**
	 * Implemented as specified by the {@link Scrollable} interface.
	 * Null implementation. 
	 */
	public int getScrollableBlockIncrement(Rectangle visibleRect,
			int orientation, int direction) {
		return visibleRect.height;
	}

	/**
	 * Implemented as specified by the {@link Scrollable} interface.
	 * Null implementation. 
	 */
	public boolean getScrollableTracksViewportHeight() {
		return false;
	}

	/**
	 * Implemented as specified by the {@link Scrollable} interface.
	 * Returns true, so that this UI component resizes to the width of
	 * the split pane.  
	 */
	public boolean getScrollableTracksViewportWidth() {
		return true;
	}

	/**
	 * Implemented as specified by the {@link Scrollable} interface.
	 * Null implementation. 
	 */
	public int getScrollableUnitIncrement(Rectangle visibleRect,
			int orientation, int direction) {
		return 10;
	}

	/**
	 * Implemented as specified by the {@link TreeModelListener} interface.
	 * Attempts to refresh the nodes affected by the event, identifying the
	 * nodes within the {@link #textAreas} map by their {@link TreePath} and 
	 * calling {@link FieldTextArea#refreshText()} to update their text. 
	 * Don't want to update the text of all fields, as the scroll pane will 
	 * scroll to the last updated (bottom of page). 
	 * 
	 * @see TreeModelListener#treeNodesChanged(TreeModelEvent)
	 */
	public void treeNodesChanged(TreeModelEvent e) {
		//TreePath path = e.getTreePath();
		Object[] children = e.getChildren();
		
		TreePath path;
		FieldTextArea ta;
		
		if (children == null)	{ 
			// Usually means that the root of the tree has changed;
			DefaultMutableTreeNode root = 
								(DefaultMutableTreeNode)treeModel.getRoot();
			path = new TreePath(root.getPath());
			ta = textAreas.get(path);
			ta.refreshText();
			return;
		}
		
		DefaultMutableTreeNode node;
		for(int i=0; i<children.length; i++) {
			if (children[i] instanceof DefaultMutableTreeNode) {
				node = (DefaultMutableTreeNode)children[i];
				path = new TreePath(node.getPath());
				ta = textAreas.get(path);
				ta.refreshText();
			}
		}
	}

	/**
	 * Implemented as specified by the {@link TreeModelListener} interface.
	 * Calls {@link #refreshTreeDisplay()} to rebuild the UI with the 
	 * new structure of the {@link #treeModel};
	 * 
	 * @see TreeModelListener#treeNodesInserted(TreeModelEvent)
	 */
	public void treeNodesInserted(TreeModelEvent e) {
		refreshTreeDisplay();
	}

	/**
	 * Implemented as specified by the {@link TreeModelListener} interface.
	 * Calls {@link #refreshTreeDisplay()} to rebuild the UI with the 
	 * new structure of the {@link #treeModel};
	 * 
	 * @see TreeModelListener#treeNodesRemoved(TreeModelEvent)
	 */
	public void treeNodesRemoved(TreeModelEvent e) {
		refreshTreeDisplay();
	}

	/**
	 * Implemented as specified by the {@link TreeModelListener} interface.
	 * Calls {@link #refreshTreeDisplay()} to rebuild the UI with the 
	 * new structure of the {@link #treeModel};
	 * 
	 * @see TreeModelListener#treeStructureChanged(TreeModelEvent)
	 */
	public void treeStructureChanged(TreeModelEvent e) {
		refreshTreeDisplay();
	}

	/**
	 * Implemented as specified by the {@link TreeSelectionListener} interface.
	 * Sets the currently selected nodes as highlighted, etc. 
	 * 
	 * @see TreeSelectionListener#valueChanged(TreeSelectionEvent)
	 */
	public void valueChanged(TreeSelectionEvent e) {
		refreshSelection();
	}

}
