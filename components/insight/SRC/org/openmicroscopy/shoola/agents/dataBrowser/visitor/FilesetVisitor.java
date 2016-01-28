/*
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

import java.awt.Color;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.openmicroscopy.shoola.agents.dataBrowser.Colors;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageDisplayVisitor;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageNode;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageSet;
import omero.gateway.model.ImageData;

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

	/** The selected images.*/
	private final Collection<ImageData> selected;
	
	/** The de-selected images.*/
	private final Collection<ImageData> deselected;
	
	/** The color to set when the node is a sibling.*/
	private final Color borderColor;
	
	/** The collection of selected fileset Ids.*/
	private final Set<Long> filesetIds;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param selected The selected nodes.
	 * @param deselected The de-selected nodes
	 */
	public FilesetVisitor(Collection<ImageData> selected,
			Collection<ImageData> deselected)
	{
		this.selected = selected;
		this.deselected = deselected;
		borderColor = Colors.getInstance().getColor(
				Colors.TITLE_BAR_HIGHLIGHT).brighter();
		filesetIds = new HashSet<Long>();
		if (selected != null) {
			Iterator<ImageData> i = selected.iterator();
			ImageData img;
			while (i.hasNext()) {
				img = i.next();
				if (img.isFSImage())
					filesetIds.add(img.getFilesetId());
			}
		}
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
		Iterator<ImageData> i;
		ImageData ref;
		if (selected != null) {
			i = selected.iterator();
			while (i.hasNext()) {
				ref = i.next();
				if (ref.isFSImage() && data.getId() != ref.getId()
					&& data.getFilesetId() == ref.getFilesetId())
					node.setBorderColor(borderColor);
			}
		}
		if (deselected != null) {
			i = deselected.iterator();
			while (i.hasNext()) {
				ref = i.next();
				if (ref.isFSImage()) {
					if (data.getFilesetId() == ref.getFilesetId() &&
							!filesetIds.contains(data.getFilesetId()))
							node.setBorderColor(null);
				}
			}
		}
	}

    /** 
     * Implemented as specified by {@link ImageDisplayVisitor}.
     * @see ImageDisplayVisitor#visit(ImageNode)
     */
	public void visit(ImageSet node) {}

}
