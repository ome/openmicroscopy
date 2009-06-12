 /*
 * org.openmicroscopy.shoola.agents.editor.browser.NavTree 
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

import javax.swing.JTree;

import org.openmicroscopy.shoola.util.ui.UIUtilities;

//Third-party libraries

//Application-internal dependencies

/** 
 * This class extends JTree and provides an outline of the Tree (text only).
 * This is used to navigate the tree and select nodes.
 * Other views of the same tree-model may update their tree-selection or display
 * of a single node when selection changes on this tree. 
 * Conversely, the selection path of this navTree mimics that of the mainTree,
 * using a TreeSelectionListener on the main Tree. 
 * 
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class NavTree 
	extends JTree
{
	
	/**
	 * Creates an instance.
	 * Also calls {@link #initialise()}
	 */
	NavTree() 
	{
		initialise();
	}
	
	/**
	 * Called by constructor. 
	 * Sets the CellRenderer, SelectionModel and adds appropriate listeners
	 * to the NavTree and the main display Tree. 
	 */
	private void initialise() 
	{
		setCellRenderer(new TreeOutlineCellRenderer());
		setSelectionModel(new ContiguousChildSelectionModel());
		setBackground(UIUtilities.BACKGROUND_COLOR);
	}
	
}
