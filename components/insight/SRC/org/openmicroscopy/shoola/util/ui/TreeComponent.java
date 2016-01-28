/*
 * org.openmicroscopy.shoola.util.ui.TreeComponent
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
package org.openmicroscopy.shoola.util.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JPanel;

/** 
 * Component laying out the {@link TreeComponentNode}s.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since OME3.0
 */
public class TreeComponent 
	extends JPanel
	implements PropertyChangeListener
{

	/** Indicates to lay out the components horizontally. */
	public static final int 	HORIZONTAL = 0;
	
	/** Indicates to lay out the components vertically. */
	public static final int 	VERTICAL = 1;
	
	/** Bound property indicating the displayed component. */
	public static final String	EXPANDED_PROPERTY = "expanded";
	
	/** The icon displayed when the node is collapsed. */
	private Icon 					collapseIcon;
	
	/** The icon displayed when the node is elapsed. */
	private Icon 					elapseIcon;
	
	/** One of the orientation constants defined by this class. */
	private int						orientation;
	
	/** Collection of nodes to handle. */
	private List<TreeComponentNode> nodes;
	
	/** Initializes the components.  */
	private void initialize()
	{
		nodes = new ArrayList<TreeComponentNode>();
		IconManager icons = IconManager.getInstance();
		
		switch (orientation) {
			case VERTICAL:
				collapseIcon = icons.getIcon(IconManager.RIGHT_ARROW_BLACK_10);
				elapseIcon = icons.getIcon(IconManager.DOWN_ARROW_BLACK_10);
				setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
				break;
			case HORIZONTAL:
				collapseIcon = icons.getIcon(IconManager.RIGHT_ARROW_BLACK_10);
				elapseIcon = icons.getIcon(IconManager.DOWN_ARROW_BLACK_10);
				setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		}
	}

	/**
	 * Collapses or expands the nodes depending on the passed value.
	 * 
	 * @param expanded Pass <code>true</code> to expand all the nodes,
	 *                 <code>false</code> to collapse.
	 */
	private void expandNodes(boolean expanded)
	{
		Iterator<TreeComponentNode> i = nodes.iterator();
		TreeComponentNode node;
		while (i.hasNext()) {
			node = i.next();
			node.setExpanded(expanded);
			node.setIcons(collapseIcon, elapseIcon);
			node.updateDisplay();
		}
	}
	
	/** Creates a new instance. */
	public TreeComponent()
	{
		this(VERTICAL);
	}
	
	/** 
	 * Creates a new instance. 
	 * 
	 * @param orientation 	One of the orientation constants defined by this 
	 * 						class.
	 */
	public TreeComponent(int orientation)
	{
		this.orientation = orientation;
		initialize();
	}
	
	/**
	 * Returns the orientation.
	 * 
	 * @return See above.
	 */
	int getOrientation() { return orientation; }
	
	/**
	 * Inserts a new node and expands it.
	 * 
	 * @param elapse	The component to display when the node is expanded.
	 * @param collapse	The component to display when the node is collapsed.
	 */
	public void insertNode(JComponent elapse, JComponent collapse)
	{
		insertNode(elapse, collapse, true);
	}
	
	/**
	 * Inserts a new node, collapses it if the passed flag is <code>true</code>
	 * expands it otherwise.
	 * 
	 * @param elapse	The component to display when the node is expanded.
	 * @param collapse	The component to display when the node is collapsed.
	 * @param expanded	Pass <code>true</code> to expand the node, 
	 * 					<code>false</code> to collapse it.
	 */
	public void insertNode(JComponent elapse, JComponent collapse, 
							boolean expanded)
	{
		TreeComponentNode node = new TreeComponentNode(elapse, collapse, 
														expanded);
		node.setIcons(collapseIcon, elapseIcon);
		node.addPropertyChangeListener(TreeComponentNode.EXPANDED_NODE_PROPERTY,
										this);
		add(node);
		nodes.add(node);
	}
	
	/** Collapses all the nodes. */
	public void collapseNodes() { expandNodes(false); }
	
	/** Expands all the nodes. */
	public void expandNodes() { expandNodes(true); }
	
	/**
	 * Sets the enables flag on all nodes of the tree.
	 * 
	 * @param enabled The flag to set.
	 */
	public void setTreeEnabled(boolean enabled)
	{
		Iterator<TreeComponentNode> i = nodes.iterator();
		TreeComponentNode node;
		while (i.hasNext()) {
			node = i.next();
			node.setNodeEnabled(enabled);
			node.updateDisplay();
		}
	}
	
	/**
	 * Revalidates the component and fires a property change.
	 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt)
	{
		TreeComponentNode node = (TreeComponentNode) evt.getSource();
		node.setIcons(collapseIcon, elapseIcon);
		revalidate();
		if (node.isExpanded())
			firePropertyChange(EXPANDED_PROPERTY, Boolean.FALSE, Boolean.TRUE);
		else 
			firePropertyChange(EXPANDED_PROPERTY, Boolean.TRUE, Boolean.FALSE);
	}
	
}
