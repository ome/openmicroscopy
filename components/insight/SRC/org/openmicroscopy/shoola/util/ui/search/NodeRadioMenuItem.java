/*
 * org.openmicroscopy.shoola.util.ui.search.NodeRadioMenuItem 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2009 University of Dundee. All rights reserved.
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
import javax.swing.JRadioButtonMenuItem;

//Third-party libraries

//Application-internal dependencies

/** 
 * Radio menu item.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
class NodeRadioMenuItem 	
	extends JRadioButtonMenuItem
{
	
	/** The node to host. */
	private SearchObject node;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param node	The node to host. Mustn't be <code>null</code>.
	 */
	NodeRadioMenuItem(SearchObject node) 
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
