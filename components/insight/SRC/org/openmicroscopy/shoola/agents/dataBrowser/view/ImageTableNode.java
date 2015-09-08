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
package org.openmicroscopy.shoola.agents.dataBrowser.view;

import java.awt.Color;
import javax.swing.Icon;

import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageDisplay;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageNode;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.Thumbnail;
import org.openmicroscopy.shoola.agents.util.EditorUtil;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.treetable.model.OMETreeNode;
import omero.gateway.model.ExperimenterData;
import omero.gateway.model.ImageData;

/** 
 * Node hosting a data object.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since OME3.0
 */
public class ImageTableNode 
	extends OMETreeNode
{

	/** The default height for the column.*/
	static final int MIN_HEIGHT = Thumbnail.THUMB_MAX_HEIGHT/2+2;
	
	/** The medium height for the column.*/
	static final int MEDIUM_HEIGHT = Thumbnail.THUMB_MAX_HEIGHT+2;
	
	/** The maximum height for the column.*/
	static final int MAX_HEIGHT = (int) (Thumbnail.MAX_SCALING_FACTOR
			*Thumbnail.THUMB_MAX_HEIGHT)+2;
	
	/** The default magnification factor.*/
	static final double MIN_FACTOR = 0.5;
	
	/** The default magnification factor.*/
	static final double MEDIUM_FACTOR = 1.0;
	
	/** The icon associated to the image. */
	private Icon icon;
	
	/** The text displayed in the tool tip.*/
	private String toolTip;
	
	/** 
	 * The color used to indicate that the file is a sibling of the 
	 * selected node or <code>null</code>
	 */
	private Color siblingColor;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param refNode The node of reference. Mustn't be <code>null</code>;
	 */
	ImageTableNode(ImageDisplay refNode)
	{
		super(refNode);
		if (refNode == null)
			throw new IllegalArgumentException("No node specified.");
		setAllowsChildren(!(refNode instanceof ImageNode));
		Object o = getHierarchyObject();
		if (o instanceof ImageData) 
			toolTip = UIUtilities.formatToolTipText(
					EditorUtil.formatObjectTooltip((ImageData) o));
	}

	/** 
	 * Sets the color indicating that the node is part of a Multi-image fileset.
	 * 
	 * @param siblingColor The color to set or <code>null</code>.
	 */
	void setSibingColor(Color siblingColor)
	{
		this.siblingColor = siblingColor;
	}
	
	/**
	 * Returns the color indicating that the node is part of a Multi-image
	 * fileset.
	 * 
	 * @return See above.
	 */
	public Color getSibingColor() { return siblingColor; }
	
	/**
	 * Returns the text displayed in the tool tip.
	 * 
	 * @return See above.
	 */
	public String getToolTip() { return toolTip; }
	
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
	 * Returns the icon representing the thumbnail.
	 * 
	 * @return See above.
	 */
	public Icon getThumbnailIcon()
	{
		if (icon != null) return icon;
		Object o = getUserObject();
		if (o instanceof ImageNode) {
			ImageNode n = (ImageNode) o;
			Thumbnail thumb = n.getThumbnail();
			if (thumb != null) {
				icon = thumb.getIcon(thumb.getScalingFactor());
				return icon;
			}
			return null;
		}
		return null;
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
				if (ho instanceof ExperimenterData) {
					ExperimenterData exp = (ExperimenterData) ho;
					return exp.getUserName();
				}
				return node.toString();
				/*
			case ImageTable.DATE_COL:
				if (ho instanceof ImageData) {
					Timestamp time = 
						EditorUtil.getAcquisitionTime((ImageData) ho);
					if (time == null) return "--";
					String s = UIUtilities.formatShortDateTime(time);
					return s.split(" ")[0];
				} else if (ho instanceof ExperimenterData) {
					return node.toString();
				}
				return "";
				*/
			case ImageTable.THUMBNAIL_COL:
				return this;
				/*
			case ImageTable.ANNOTATED_COL:
				if (ho instanceof ExperimenterData) {
					ExperimenterData exp = (ExperimenterData) ho;
					return exp.getInstitution();
				}
				if (ho instanceof DataObject) {
					if (EditorUtil.isAnnotated(ho))
						return icons.getIcon(IconManager.ANNOTATION);
				} 
				return icons.getIcon(IconManager.TRANSPARENT);
				*/
		}
		return null;
	}

}
