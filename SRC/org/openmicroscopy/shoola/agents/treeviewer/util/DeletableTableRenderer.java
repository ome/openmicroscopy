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
import javax.swing.Icon;
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

	/** Reference to the <code>Image</code> icon. */
	private static final Icon IMAGE_ICON;
	
	/** Reference to the <code>Dataset</code> icon. */
	private static final Icon DATASET_ICON;
	
	/** Reference to the <code>Project</code> icon. */
	private static final Icon PROJECT_ICON;
	
	/** Reference to the <code>Screen</code> icon. */
	private static final Icon SCREEN_ICON;
	
	/** Reference to the <code>Plate</code> icon. */
	private static final Icon PLATE_ICON;
	
	/** Reference to the <code>File</code> icon. */
	private static final Icon FILE_ICON;
	
	static { 
		IconManager icons = IconManager.getInstance();
		IMAGE_ICON = icons.getIcon(IconManager.IMAGE);
		DATASET_ICON = icons.getIcon(IconManager.DATASET);
		PROJECT_ICON = icons.getIcon(IconManager.PROJECT);
		SCREEN_ICON = icons.getIcon(IconManager.SCREEN);
		PLATE_ICON = icons.getIcon(IconManager.PLATE);
		FILE_ICON = icons.getIcon(IconManager.FILE);
	}
	
	/** Helper reference to the {@link IconManager}. */
	private IconManager icons;
	
	/** Creates a new instance. */
	DeletableTableRenderer()
	{
		setOpaque(true);
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
			setIcon(IMAGE_ICON);
		else if (DeletableTableNode.DATASET_TYPE.equals(type)) 
			setIcon(DATASET_ICON);
		else if (DeletableTableNode.PROJECT_TYPE.equals(type)) 
			setIcon(PROJECT_ICON);
		else if (DeletableTableNode.PLATE_TYPE.equals(type)) 
			setIcon(PLATE_ICON);
		else if (DeletableTableNode.SCREEN_TYPE.equals(type)) 
			setIcon(SCREEN_ICON);
		else if (DeletableTableNode.FILE_TYPE.equals(type)) 
			setIcon(FILE_ICON);
		setText(type);
		return this;
	}
	
}
