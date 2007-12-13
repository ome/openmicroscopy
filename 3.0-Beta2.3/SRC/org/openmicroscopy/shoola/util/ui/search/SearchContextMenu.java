/*
 * org.openmicroscopy.shoola.util.ui.search.SearchContextMenu 
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
package org.openmicroscopy.shoola.util.ui.search;


//Java imports
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.List;
import javax.swing.JPopupMenu;


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
class SearchContextMenu
	extends JPopupMenu
	implements ActionListener
{

	/** Bound property indicating that a new item is selected. */
	static final String SEARCH_CONTEXT_PROPERTY = "searchContext";
	
	/** The width of the component. */
    private int			width;

    /** The type of node to create. */
    private Class 		type;
    
    /**
     * Builds and lays out the UI.
     * 
     * @param nodes 		The context nodes to lay out.
     * @param selectedNode 	The default selected node.
     */
    private void buildGUI(List<SearchObject> nodes, SearchObject selectedNode)
    {
    	Iterator i = nodes.iterator();
		
		SearchObject node;
		if (type.equals(NodeCheckMenuItem.class)) {
			NodeCheckMenuItem uiNode;
			int index = -1;
			if (selectedNode != null) index = selectedNode.getIndex();
			while (i.hasNext()) {
				node = (SearchObject) i.next();
				uiNode = new NodeCheckMenuItem(node);
				if (node.getIndex() == index)
					uiNode.setSelected(true);
				uiNode.addActionListener(this);
				add(uiNode);
			}
		} else {
			NodeMenuItem uiNode;
			while (i.hasNext()) {
				node = (SearchObject) i.next();
				uiNode = new NodeMenuItem(node);
				uiNode.addActionListener(this);
				add(uiNode);
			}
		}
		int  height =  getFontMetrics(getFont()).getHeight()*nodes.size()+10;
        setPopupSize(new Dimension(width, height));
    }
    
    /**
     * Creates a new instance.
     * 
     * @param nodes The context nodes.
     * @param width	The popup width.
     */
    SearchContextMenu(List<SearchObject> nodes, int width)
    {
    	this(nodes, width, NodeMenuItem.class, null);
    }

    /**
     * Creates a new instance.
     * 
     * @param nodes 		The context nodes.
     * @param width			The popup width.
     * @param type			The type of item to create.
     * @param selectedNode 	The default selected node.
     */
    SearchContextMenu(List<SearchObject> nodes, int width, Class type,
    		SearchObject selectedNode)
    {
    	this.type = type;
    	this.width = width;
    	buildGUI(nodes, selectedNode);
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
		}
	}
    
}
