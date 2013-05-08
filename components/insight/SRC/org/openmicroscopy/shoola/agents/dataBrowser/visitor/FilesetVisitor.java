/*
 * org.openmicroscopy.shoola.agents.dataBrowser.visitor.FilesetVisitor
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2013 University of Dundee & Open Microscopy Environment.
 *  All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.agents.dataBrowser.visitor;


//Java imports
import java.awt.Color;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.dataBrowser.Colors;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageDisplayVisitor;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageNode;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageSet;
import pojos.ImageData;

/**
 * Founds the siblings of the specified image.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since 5.0
 */
public class FilesetVisitor
	implements ImageDisplayVisitor
{

	/** The image to handle.*/
	private final ImageData refNode;
	
	/** The color to set when the node is a sibling.*/
	private final Color borderColor;
	
	/** Flag indicating if the node is selected or de-selected.*/
	private final boolean selected;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param refNode The node to handle. Mustn't be <code>null</code>.
	 * @param selected Pass <code>true</code> if the passed not is selected,
	 * <code>false</code> otherwise.
	 */
	public FilesetVisitor(ImageData refNode, boolean selected)
	{
		if (refNode == null)
			throw new IllegalArgumentException("Ref Image cannot be null.");
		this.refNode = refNode;
		this.selected = selected;
		borderColor = Colors.getInstance().getColor(
				Colors.TITLE_BAR_HIGHLIGHT).brighter();
	}
    /** 
     * Implemented as specified by {@link ImageDisplayVisitor}.
     * @see ImageDisplayVisitor#visit(ImageNode)
     */
	public void visit(ImageNode node)
	{
		Object ho = node.getHierarchyObject();
		if (!(ho instanceof ImageData)) return;
		ImageData data = (ImageData) ho;
		if (data.getId() == refNode.getId()) return;
		if (data.getFilesetId() == refNode.getFilesetId()) {
			if (selected) node.setBorderColor(borderColor);
			else node.setBorderColor(null);
		}
	}

    /** 
     * Implemented as specified by {@link ImageDisplayVisitor}.
     * @see ImageDisplayVisitor#visit(ImageNode)
     */
	public void visit(ImageSet node) {}

}
