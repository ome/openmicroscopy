/*
 * org.openmicroscopy.shoola.agents.dataBrowser.view.ImagesModel 
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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;



//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.dataBrowser.DataBrowserAgent;
import org.openmicroscopy.shoola.agents.dataBrowser.DataBrowserLoader;
import org.openmicroscopy.shoola.agents.dataBrowser.DataBrowserTranslator;
import org.openmicroscopy.shoola.agents.dataBrowser.ThumbnailLoader;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.BrowserFactory;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageNode;

import pojos.ImageData;

/** 
 * 
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
class ImagesModel 
	extends DataBrowserModel
{

	/** The images to lay out. */
	private Set<ImageData> images;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param images The collection to images the model is for.
	 */
	ImagesModel(Set<ImageData> images)
	{
		super();
		if (images  == null) 
			throw new IllegalArgumentException("No images.");
		this.images = images;
		numberOfImages = images.size();
		long userID = DataBrowserAgent.getUserDetails().getId();
		Set visTrees = DataBrowserTranslator.transformImages(images, userID, 0);
        browser = BrowserFactory.createBrowser(visTrees);
        layoutBrowser();
	}
	
	/**
	 * Creates a concrete loader.
	 * @see DataBrowserModel#createDataLoader(boolean)
	 */
	protected DataBrowserLoader createDataLoader(boolean refresh)
	{
		if (refresh) return new ThumbnailLoader(component, images);
		//only load thumbnails not loaded.
		if (imagesLoaded == numberOfImages) return null;
		List<ImageNode> nodes = browser.getVisibleImageNodes();
		if (nodes == null || nodes.size() == 0) return null;
		Iterator<ImageNode> i = nodes.iterator();
		ImageNode node;
		Set<ImageData> imgs = new HashSet<ImageData>();
		while (i.hasNext()) {
			node = i.next();
			if (node.getThumbnail().getFullScaleThumb() == null) {
				imgs.add((ImageData) node.getHierarchyObject());
				imagesLoaded++;
			}
		}
		return new ThumbnailLoader(component, imgs);
	}
	
}
