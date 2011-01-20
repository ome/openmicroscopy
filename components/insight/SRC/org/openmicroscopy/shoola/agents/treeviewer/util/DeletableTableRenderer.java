/*
 * org.openmicroscopy.shoola.agents.treeviewer.util.DeletableTableRenderer 
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
package org.openmicroscopy.shoola.agents.treeviewer.util;


//Java imports
import java.awt.Component;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.IconManager;

/** 
 * Renders the node that could not be deleted.
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
class DeletableTableRenderer
	extends DefaultTreeCellRenderer
{

	/** Helper reference to the {@link IconManager}. */
	private IconManager icons;
	
	/** Creates a new instance. */
	DeletableTableRenderer()
	{
		setOpaque(true);
		icons = IconManager.getInstance();
	}
	
	/**
	 * Sets the icon associated to the data object.
	 * @see DefaultTreeCellRenderer#getTreeCellRendererComponent(JTree, 
	 * 								Object, boolean, boolean, boolean, 
	 * 								int, boolean)
	 */
	public Component getTreeCellRendererComponent(JTree tree, 
			Object value, boolean selected, boolean expanded, 
			boolean leaf, int row, boolean hasFocus)
	{
		if (selected) setBackground(getBackgroundSelectionColor());
		else setBackground(getBackgroundNonSelectionColor());
		if (!(value instanceof DeletableTableNode)) return this;
		DeletableTableNode node = (DeletableTableNode) value;
		String type = node.getType();
		if (DeletableTableNode.IMAGE_TYPE.equals(type)) 
			setIcon(icons.getIcon(IconManager.IMAGE));
		else if (DeletableTableNode.DATASET_TYPE.equals(type)) 
			setIcon(icons.getIcon(IconManager.DATASET));
		else if (DeletableTableNode.PROJECT_TYPE.equals(type)) 
			setIcon(icons.getIcon(IconManager.PROJECT));
		else if (DeletableTableNode.PLATE_TYPE.equals(type)) 
			setIcon(icons.getIcon(IconManager.PLATE));
		else if (DeletableTableNode.PLATE_TYPE.equals(type)) 
			setIcon(icons.getIcon(IconManager.PLATE_ACQUISITION));
		else if (DeletableTableNode.SCREEN_TYPE.equals(type)) 
			setIcon(icons.getIcon(IconManager.SCREEN));
		else if (DeletableTableNode.FILE_TYPE.equals(type)) 
			setIcon(icons.getIcon(IconManager.FILE));
		setText(type);
		return this;
	}
	
}
