/*
 * org.openmicroscopy.shoola.agents.dataBrowser.util.ImageTableRenderer 
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
package org.openmicroscopy.shoola.agents.dataBrowser.util;


//Java imports
import java.awt.Component;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.dataBrowser.IconManager;
import org.openmicroscopy.shoola.agents.dataBrowser.view.ImageTableNode;
import org.openmicroscopy.shoola.agents.util.EditorUtil;
import pojos.DatasetData;
import pojos.ImageData;
import pojos.ProjectData;

/** 
 * Renders for an {@link ImageTable}. 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta3
 */
public class ImageTableRenderer 
	extends DefaultTreeCellRenderer
{

	/** Helper reference to the {@link IconManager}. */
	private IconManager icons;
	
	/** Creates a new instance. */
	public ImageTableRenderer()
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
		if (selected) {
			if (value instanceof ImageTableNode) {
				setBackground(((ImageTableNode) value).getHighLight());
			} else setBackground(getBackgroundSelectionColor());
		} else setBackground(getBackgroundNonSelectionColor());
		if (!(value instanceof ImageTableNode)) return this;
		ImageTableNode node = (ImageTableNode) value;
		Object v = node.getHierarchyObject();
		if (v instanceof ImageData) {
			/*
			if (EditorUtil.isAnnotated(v))
				setIcon(icons.getIcon(IconManager.IMAGE_ANNOTATED));
		    else setIcon(icons.getIcon(IconManager.IMAGE));
		    */
			setIcon(icons.getIcon(IconManager.IMAGE));
			setText(node.getUserObject().toString());
		} else if (v instanceof DatasetData) {
			setIcon(icons.getIcon(IconManager.DATASET));
			setText(node.getUserObject().toString());
		} else if (v instanceof ProjectData) {
			setIcon(icons.getIcon(IconManager.PROJECT));
			setText(node.getUserObject().toString());
		}
		return this;
	}
	
}
