/*
 * org.openmicroscopy.shoola.agents.dataBrowser.view.ImageTableNode 
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
package org.openmicroscopy.shoola.agents.dataBrowser.view;


//Java imports
import java.awt.Color;
import java.sql.Timestamp;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.dataBrowser.IconManager;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageDisplay;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageNode;
import org.openmicroscopy.shoola.agents.util.EditorUtil;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.treetable.model.OMETreeNode;
import pojos.DataObject;
import pojos.ImageData;

/** 
 * Node hosting a data object.
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
public class ImageTableNode 
	extends OMETreeNode
{
	
	/** Helper reference to the icon manager. */
	private IconManager icons;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param refNode The node of reference. Mustn't be <code>null</code>;
	 */
	public ImageTableNode(ImageDisplay refNode)
	{
		super(refNode);
		if (refNode == null)
			throw new IllegalArgumentException("No node specified.");
		setAllowsChildren(!(refNode instanceof ImageNode));
		icons = IconManager.getInstance();
	}
	
	/**
	 * Returns the hierarchy object related to this node.
	 * 
	 * @return See above.
	 */
	public Object getHierarchyObject()
	{
		return ((ImageDisplay) getUserObject()).getHierarchyObject();
	}
	
	/**
	 * Returns the highLight color.
	 * 
	 * @return See above.
	 */
	public Color getHighLight()
	{
		return ((ImageDisplay) getUserObject()).getHighlight();
	}
	
	/**
	 * Overridden to return the correct value depending on the column
	 * @see OMETreeNode#getValueAt(int)
	 */
	public Object getValueAt(int column)
	{
		ImageDisplay node = (ImageDisplay) getUserObject();
		Object ho = node.getHierarchyObject();
		switch (column) {
			case ImageTable.NAME_COL:
				return node.toString();
			case ImageTable.DATE_COL:
				if (ho instanceof ImageData) {
					Timestamp time = 
						EditorUtil.getAcquisitionTime((ImageData) ho);
					if (time == null) return "--";
					return UIUtilities.formatWDMYDate(time);
				}
				return "--";
			case ImageTable.ANNOTATED_COL:
				if (ho instanceof DataObject) {
					if (EditorUtil.isAnnotated(ho))
						return icons.getIcon(IconManager.ANNOTATION);
				}
				return icons.getIcon(IconManager.TRANSPARENT);
				
		}
		return null;
	}

}
