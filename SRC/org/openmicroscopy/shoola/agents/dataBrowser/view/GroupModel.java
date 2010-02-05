/*
 * org.openmicroscopy.shoola.agents.dataBrowser.view.GroupModel 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2010 University of Dundee. All rights reserved.
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.openmicroscopy.shoola.agents.dataBrowser.DataBrowserAgent;
import org.openmicroscopy.shoola.agents.dataBrowser.DataBrowserLoader;
import org.openmicroscopy.shoola.agents.dataBrowser.DataBrowserTranslator;
import org.openmicroscopy.shoola.agents.dataBrowser.ThumbnailLoader;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.BrowserFactory;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageDisplay;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageNode;

import pojos.DatasetData;
import pojos.ExperimenterData;
import pojos.ImageData;

//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * A concrete Model for a collection of Groups consisting of a single 
 * tree rooted by given Group.
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
class GroupModel 
	extends DataBrowserModel
{

	/**
	 * Creates a new instance.
	 * 
	 * @param parent	The parent of the experimenters.
	 * @param datasets 	The collection to experimenters the model is for.
	 */
	GroupModel(Object parent, Collection<ExperimenterData> experimenters)
	{
		super();
		if (experimenters  == null) 
			throw new IllegalArgumentException("No experimenters.");
		this.parent = parent;
		Set visTrees = DataBrowserTranslator.transformExperimenters(
				experimenters);
		numberOfImages = experimenters.size();
        browser = BrowserFactory.createBrowser(visTrees);
        layoutBrowser();
	}
	
	/**
	 * Creates a concrete loader.
	 * @see DataBrowserModel#createDataLoader(boolean, Collection)
	 */
	protected DataBrowserLoader createDataLoader(boolean refresh, 
			Collection ids)
	{
		if (refresh) imagesLoaded = 0;
		if (imagesLoaded != 0 && ids != null)
			imagesLoaded = imagesLoaded-ids.size();
		if (imagesLoaded == numberOfImages) return null;
		//only load thumbnails not loaded.
		List<ImageNode> nodes = browser.getVisibleImageNodes();
		if (nodes == null || nodes.size() == 0) return null;
		Iterator<ImageNode> i = nodes.iterator();
		ImageNode node;
		List<ImageData> imgs = new ArrayList<ImageData>();
		ImageData img;
		List<Long> loaded = new ArrayList<Long>();
		if (ids != null) {
			while (i.hasNext()) {
				node = i.next();
				if (node.getThumbnail().getFullScaleThumb() == null) {
					img = (ImageData) node.getHierarchyObject();
					if (ids.contains(img.getId())) {
						if (!loaded.contains(img.getId())) {
							imgs.add(img);
							loaded.add(img.getId());
							imagesLoaded++;
						}
					}
				}
			}
		} else {
			while (i.hasNext()) {
				node = i.next();
				if (node.getThumbnail().getFullScaleThumb() == null) {
					img = (ImageData) node.getHierarchyObject();
					if (!loaded.contains(img.getId())) {
						imgs.add(img);
						loaded.add(img.getId());
						imagesLoaded++;
					}
				}
			}
		}
		if (imgs.size() == 0) return null;
		return new ThumbnailLoader(component, sorter.sort(imgs));
	}
	
	/**
	 * Returns the type of this model.
	 * @see DataBrowserModel#getType()
	 */
	protected int getType() { return DataBrowserModel.GROUP; }

	/**
	 * No-operation implementation in our case.
	 * @see DataBrowserModel#getNodes()
	 */
	protected List<ImageDisplay> getNodes() { return null; }
	
}
