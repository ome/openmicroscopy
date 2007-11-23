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
import javax.swing.Icon;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;


//Third-party libraries

//Application-internal dependencies

/** 
 * 
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

	static final String SEARCH_CONTEXT_PROPERTY = "searchContext";
	
	/** The width of the component. */
    private int	width;

    private void buildGUI(List<SearchObject> nodes)
    {
    	Iterator i = nodes.iterator();
		JMenuItem uiNode;
    	SearchObject node;
		Icon icon = null;
		int j = 0;
		JPanel menu = new JPanel();
		menu.setOpaque(!menu.isOpaque());
		while (i.hasNext()) {
			node = (SearchObject) i.next();
			uiNode = new NodeMenuItem(node);
			uiNode.addActionListener(this);
			add(uiNode);
		}
		int  height =  getFontMetrics(getFont()).getHeight()*nodes.size()+10;
        setPopupSize(new Dimension(width, height));
    }
    
    SearchContextMenu(List<SearchObject> nodes, int width)
    {
    	this.width = width;
    	buildGUI(nodes);
    }

	
	public void actionPerformed(ActionEvent e)
	{
		Object src = e.getSource();
		if (src instanceof NodeMenuItem) {
			NodeMenuItem item = (NodeMenuItem) src;
			firePropertyChange(SEARCH_CONTEXT_PROPERTY, null, 
								item.getSearchObject());
		}
	}

	/** Helper inner class displaying the Search object. */
	class NodeMenuItem 
		extends JMenuItem
	{
		
		/** The node to host. */
		private SearchObject node;
		
		/**
		 * Creates a new instance.
		 * 
		 * @param node	The node to host. Mustn't be <code>null</code>.
		 */
		NodeMenuItem(SearchObject node) 
		{
			if (node == null)
				throw new IllegalArgumentException("No experimenter.");
			this.node = node;
			setIcon(node.getIcon());
			setText(node.getDescription());
		}
		
		/**
		 * Returns the node.
		 * 
		 * @return See above.
		 */
		SearchObject getSearchObject() { return node; }
		
	}
    
}
