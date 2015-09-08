/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.dataBrowser.util;

import java.awt.Color;
import java.awt.Component;
import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.openmicroscopy.shoola.agents.dataBrowser.IconManager;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageDisplay;
import org.openmicroscopy.shoola.agents.dataBrowser.view.ImageTableNode;
import omero.gateway.model.DatasetData;
import omero.gateway.model.ExperimenterData;
import omero.gateway.model.ImageData;
import omero.gateway.model.ProjectData;

/** 
 * Renders the table.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since 3.0-Beta3
 */
public class ImageTableRenderer
	extends DefaultTreeCellRenderer
{

	/** Reference to the <code>Dataset</code> icon. */
	private static final Icon DATASET_ICON;
	
	/** Reference to the <code>Project</code> icon. */
	private static final Icon PROJECT_ICON;
	
	static { 
		IconManager icons = IconManager.getInstance();
		DATASET_ICON = icons.getIcon(IconManager.DATASET);
		PROJECT_ICON = icons.getIcon(IconManager.PROJECT);
	}
	
	/** The default foreground color.*/
	private final Color color;
	
	/** Creates a new instance. */
	public ImageTableRenderer()
	{
		setOpaque(false);
		color = getForeground();
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
		if (!(value instanceof ImageTableNode)) return this;
		ImageTableNode node = (ImageTableNode) value;
		Object v = node.getHierarchyObject();
		if (v instanceof ImageData) {
			setIcon(null);
			setText(((ImageDisplay) node.getUserObject()).toString());
			setToolTipText(node.getToolTip());
			Color c = node.getSibingColor();
			if (c == null) c = color;
			setForeground(c);
		} else if (v instanceof DatasetData) {
			setIcon(DATASET_ICON);
			setText(node.getUserObject().toString());
		} else if (v instanceof ProjectData) {
			setIcon(PROJECT_ICON);
			setText(node.getUserObject().toString());
		} else if (v instanceof ExperimenterData) {
			setIcon(null);
			ExperimenterData exp = (ExperimenterData) v;
			setText(exp.getUserName());
		}
		return this;
	}
	
}
