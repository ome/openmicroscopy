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
package org.openmicroscopy.shoola.agents.dataBrowser.visitor;

import java.util.Collection;

import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageDisplayVisitor;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageNode;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageSet;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.Thumbnail;
import omero.gateway.model.ImageData;

/** 
 * Sets the thumnail to <code>null</code> for each visited {@link ImageNode}s.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since OME3.0
 */
public class ResetThumbnailVisitor 
	implements ImageDisplayVisitor
{
	
	/** The collection of images. */
	private Collection ids;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param ids The collection of images to reload the thumbnail
	 *            or <code>null</code>.
	 */
	public ResetThumbnailVisitor(Collection ids)
	{
		this.ids = ids;
	}
	
	/** 
     * Implemented as specified by {@link ImageDisplayVisitor}. 
     * @see ImageDisplayVisitor#visit(ImageNode)
     */
	public void visit(ImageNode node)
	{
		Thumbnail th = node.getThumbnail();
		if (th == null) return;
		if (ids == null || ids.size() == 0)
			th.setFullScaleThumb(null);
		else {
			Object ho = node.getHierarchyObject(); 
			if (ho instanceof ImageData) {
				ImageData d = (ImageData) ho;
				if (ids.contains(d.getId())) th.setFullScaleThumb(null);
			}
		}
	}

    /** 
     * Implemented as specified by {@link ImageDisplayVisitor}. 
     * @see ImageDisplayVisitor#visit(ImageSet)
     */
	public void visit(ImageSet node) {}
	
}
