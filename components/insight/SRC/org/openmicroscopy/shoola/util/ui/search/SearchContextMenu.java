/*
 * org.openmicroscopy.shoola.util.ui.search.SearchContextMenu 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2014 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.util.ui.search;


//Java imports
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;


//Third-party libraries

//Application-internal dependencies

/** 
 * Popup menu displaying the possible context of a search.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class SearchContextMenu
	extends JPopupMenu
	implements ActionListener
{

    /** Bound property indicating that a new item is selected. */
    public static final String SEARCH_CONTEXT_PROPERTY = "searchContext";
	
	/** The width of the component. */
    private int			width;

    /** The type of node to create. */
    private Class 		type;
    
    /** Collections of UI nodes hosting the <code>SearchObject</code>s. */
    private List		items;
    
    private ButtonGroup group;
    
    /**
     * Builds and lays out the UI.
     * 
     * @param nodes 			The context nodes to lay out.
     * @param ratedNodes 		The rated nodes if any.
     * @param selectedNode 		The default selected node.
     * @param singleSelection 	Pass <code>true</code> if only one context can 
     * 							be selected at a time to <code>false</code> 
     * 							otherwise.
     */
    private void buildGUI(List<SearchObject> nodes, 
    		List<SearchObject> ratedNodes, SearchObject selectedNode, 
    		boolean singleSelection)
    {
    	Iterator i; 
		group = new ButtonGroup();
		SearchObject node;
		if (type.equals(NodeRadioMenuItem.class)) {
			NodeRadioMenuItem uiNode;
			int index = -1;
			if (selectedNode != null) index = selectedNode.getIndex();
			i = ratedNodes.iterator();
			while (i.hasNext()) {
				node = (SearchObject) i.next();
				uiNode = new NodeRadioMenuItem(node);
				if (node.getIndex() == index)
					uiNode.setSelected(true);
				uiNode.addActionListener(this);
				add(uiNode);
				items.add(uiNode);
				if (singleSelection) group.add(uiNode);
			}
			if (ratedNodes.size() > 0)
				add(new JSeparator());
			i = nodes.iterator();
			while (i.hasNext()) {
				node = (SearchObject) i.next();
				uiNode = new NodeRadioMenuItem(node);
				if (node.getIndex() == index)
					uiNode.setSelected(true);
				uiNode.addActionListener(this);
				add(uiNode);
				items.add(uiNode);
				if (singleSelection) group.add(uiNode);
			}
		} else if (type.equals(NodeCheckMenuItem.class)) {
			NodeCheckMenuItem uiNode;
			int index = -1;
			if (selectedNode != null) index = selectedNode.getIndex();
			i = ratedNodes.iterator();
			while (i.hasNext()) {
				node = (SearchObject) i.next();
				uiNode = new NodeCheckMenuItem(node);
				if (node.getIndex() == index)
					uiNode.setSelected(true);
				uiNode.addActionListener(this);
				add(uiNode);
				items.add(uiNode);
				if (singleSelection) group.add(uiNode);
			}
			if (ratedNodes.size() > 0)
				add(new JSeparator());
			i = nodes.iterator();
			while (i.hasNext()) {
				node = (SearchObject) i.next();
				uiNode = new NodeCheckMenuItem(node);
				if (node.getIndex() == index)
					uiNode.setSelected(true);
				uiNode.addActionListener(this);
				add(uiNode);
				items.add(uiNode);
				if (singleSelection) group.add(uiNode);
			}
			
		} else {
			NodeMenuItem uiNode;
			i = ratedNodes.iterator();
			while (i.hasNext()) {
				node = (SearchObject) i.next();
				uiNode = new NodeMenuItem(node);
				uiNode.addActionListener(this);
				items.add(uiNode);
				add(uiNode);
			}
			if (ratedNodes.size() > 0)
				add(new JSeparator());
			i = nodes.iterator();
			while (i.hasNext()) {
				node = (SearchObject) i.next();
				uiNode = new NodeMenuItem(node);
				uiNode.addActionListener(this);
				add(uiNode);
				items.add(uiNode);
			}
		}
		int n = nodes.size()+ratedNodes.size();
		int  height = getFontMetrics(getFont()).getHeight()*n+10;
        setPopupSize(new Dimension(width, height));
    }
    
    /**
     * Creates a new instance.
     * 
     * @param nodes 			The context nodes.
     * @param ratedNodes 		The rated nodes if any.
     * @param width				The popup width.
     * @param singleSelection 	Pass <code>true</code> if only one context can 
     * 							be selected at a time to <code>false</code> 
     * 							otherwise.
     */
    public SearchContextMenu(List<SearchObject> nodes, List<SearchObject> ratedNodes,
    				int width, boolean singleSelection)
    {
    	this(nodes, ratedNodes, width, NodeMenuItem.class, null, 
    		singleSelection);
    }

    /**
     * Creates a new instance.
     * 
     * @param nodes 			The context nodes.
     * @param ratedNodes 		The rated nodes if any.
     * @param width				The pop-up width.
     * @param selectedNode 		The default selected node.
     * @param singleSelection 	Pass <code>true</code> if only one context can 
     * 							be selected at a time to <code>false</code> 
     * 							otherwise.
     */
    public SearchContextMenu(List<SearchObject> nodes, List<SearchObject> ratedNodes,
    				int width, SearchObject selectedNode, 
    				boolean singleSelection)
    {
    	this(nodes, ratedNodes, width, NodeRadioMenuItem.class, selectedNode,
    			singleSelection);
    }
    
    /**
     * Creates a new instance.
     * 
     * @param nodes 			The context nodes.
     * @param ratedNodes 		The rated nodes if any.
     * @param width				The pop-up width.
     * @param type				The type of item to create.
     * @param selectedNode 		The default selected node.
     * @param singleSelection 	Pass <code>true</code> if only one context can 
     * 							be selected at a time to <code>false</code> 
     * 							otherwise.
     */
    SearchContextMenu(List<SearchObject> nodes, List<SearchObject> ratedNodes,
    				int width, Class type, SearchObject selectedNode, 
    				boolean singleSelection)
    {
    	this.type = type;
    	this.width = width;
    	items = new ArrayList();
    	buildGUI(nodes, ratedNodes, selectedNode, singleSelection);
    }
    
    /**
     * Selects the UI node corresponding to the passed 
     * <code>SearchObject</code>.
     * 
     * @param node The <code>SearchObject</code> to handle.
     */
    public void setSelectedNode(SearchObject node)
    {
        if(node==null) {
            group.clearSelection();
        }
        else {
        	Iterator i = items.iterator();
        	Object uiNode;
        	NodeMenuItem nmItem;
        	NodeCheckMenuItem ncmItem;
        	NodeRadioMenuItem rItem;
        	while (i.hasNext()) {
    		uiNode = i.next();
			if (uiNode instanceof NodeMenuItem) {
				nmItem = (NodeMenuItem) uiNode;
				if (nmItem.getSearchObject().getIndex() == node.getIndex()) {
					nmItem.removeActionListener(this);
					nmItem.setSelected(true);
					nmItem.addActionListener(this);
				}
			} else if (uiNode instanceof NodeCheckMenuItem) {
				ncmItem = (NodeCheckMenuItem) uiNode;
				if (ncmItem.getSearchObject().getIndex() == node.getIndex()) {
					ncmItem.removeActionListener(this);
					ncmItem.setSelected(true);
					ncmItem.addActionListener(this);
				}
			} else if (uiNode instanceof NodeRadioMenuItem) {
				rItem = (NodeRadioMenuItem) uiNode;
				if (rItem.getSearchObject().getIndex() == node.getIndex()) {
					rItem.removeActionListener(this);
					rItem.setSelected(true);
					rItem.addActionListener(this);
				}
			}
		}
        }
    }
	/**
	 * Fires a property change when a new menu item is selected.
	 * @see ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e)
	{
		Object src = e.getSource();
		if (src instanceof NodeMenuItem) {
			NodeMenuItem item = (NodeMenuItem) src;
			firePropertyChange(SEARCH_CONTEXT_PROPERTY, null, 
								item.getSearchObject());
		} else if (src instanceof NodeCheckMenuItem) {
			NodeCheckMenuItem item = (NodeCheckMenuItem) src;
			firePropertyChange(SEARCH_CONTEXT_PROPERTY, null, 
								item.getSearchObject());
		} else if (src instanceof NodeRadioMenuItem) {
			NodeRadioMenuItem item = (NodeRadioMenuItem) src;
			firePropertyChange(SEARCH_CONTEXT_PROPERTY, null, 
								item.getSearchObject());
		}
	}
    
}
